package io.github.term4.polyp.mechanics.projectile.entities.arrow;

import io.github.term4.polyp.world.MechanicsWorld;
import io.github.term4.polyp.world.WorldPolicy;
import io.github.term4.polyp.fx.FxContext;
import io.github.term4.polyp.fx.Fx;
import io.github.term4.polyp.mechanics.attribute.catalog.enchant.Flame;
import io.github.term4.polyp.mechanics.damage.types.burning.Ignite;
import io.github.term4.polyp.mechanics.projectile.ProjectileConfigResolver.ResolvedHit;
import io.github.term4.polyp.mechanics.attribute.catalog.VanillaPotions;
import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.ProjectileSystem;
import io.github.term4.polyp.mechanics.projectile.entities.ManagedProjectile;
import io.github.term4.polyp.mechanics.projectile.entities.ProjectileEntity;
import io.github.term4.polyp.mechanics.projectile.types.ProjectileTypeConfig;
import io.github.term4.polyp.util.tick.TickScaler;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.projectile.AbstractArrowMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.CollectItemPacket;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.potion.CustomPotionEffect;
import net.minestom.server.potion.Potion;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Arrow projectile: vanilla velocity-based damage ({@code ceil(speed * damage) (+ crit bonus)}), sticks in blocks
 * (frozen + periodic re-sync from {@link ProjectileEntity}) instead of breaking, and can be picked up while stuck.
 * {@link #setCritical} (set by the bow at full draw) adds the crit bonus + particles. Config: {@code Vanilla18.arrow()}.
 * Power adds {@code 0.5×level + 0.5} to the per-velocity damage and Punch the extra hit knockback (both captured at
 * launch; see {@link ProjectileEntity#punchLevel()}). TODO: a dedicated {@code minecraft:arrow} damage type.
 */
public class ArrowEntity extends ManagedProjectile {

    /** Vanilla {@code 7}. */
    private int shakeTicks = 7;

    // Pickup geometry (ProjectileTypeConfig.pickupBox, stamped at launch; default vanilla). Collected when the arrow's
    // boxWidth x boxHeight box intersects a player's bbox inflated (inflateH, inflateV, inflateH).
    private double pickupInflateH = ProjectileTypeConfig.PickupBox.VANILLA.inflateH();
    private double pickupInflateV = ProjectileTypeConfig.PickupBox.VANILLA.inflateV();
    private double pickupBoxHalfWidth = ProjectileTypeConfig.PickupBox.VANILLA.boxWidth() / 2;
    private double pickupBoxHeight = ProjectileTypeConfig.PickupBox.VANILLA.boxHeight();
    /** Broad-phase player scan radius derived from the geometry - a spatial pre-filter only; {@link #withinPickupBox} is the exact test. */
    private double pickupScanRange = computeScanRange(ProjectileTypeConfig.PickupBox.VANILLA);

    private boolean critical;
    /** Who may collect this arrow once stuck (vanilla {@code EntityArrow.fromPlayer}); the bow sets it per gamemode. */
    private Pickup pickup = Pickup.ALLOWED;
    /** Remaining pickup-cooldown ticks after sticking (vanilla shake). */
    private int shake;

    /** Tipped-arrow payload captured at launch off the item's {@code potion_contents}: effects applied to a struck entity. Empty = plain arrow. */
    private List<CustomPotionEffect> onHitEffects = List.of();
    /** The item's {@code potion_duration_scale} (vanilla bakes {@code 0.125} = 1/8 onto a crafted tipped arrow); default {@code 1.0}. */
    private float potionDurationScale = 1.0f;

    /** Who may pick up a stuck arrow (vanilla {@code AbstractArrow.Pickup}): none, any player (survival gets the item), or creative only. */
    public enum Pickup { DISALLOWED, ALLOWED, CREATIVE_ONLY }

    public ArrowEntity(@Nullable Entity shooter, @NotNull EntityType entityType,
                       ProjectileSnapshot snap, ProjectileTypeConfig effectiveConfig) {
        super(shooter, entityType, snap, effectiveConfig);
    }

    /** Marks the arrow critical (full draw): extra random damage + the client crit-particle trail. */
    public void setCritical(boolean critical) {
        this.critical = critical;
        if (getEntityMeta() instanceof AbstractArrowMeta meta) meta.setCritical(critical);
    }

    /** The optional {@code deflectParticles} cosmetic trail ({@link #deflectVisible}). */
    private void spawnDeflectTrail() {
        Pos p = getPosition();
        sendPacketToViewersAndSelf(new ParticlePacket(Particle.CRIT, p.x(), p.y(), p.z(), 0.05f, 0.05f, 0.05f, 0f, 2));
    }

    public void setPickup(Pickup pickup) { this.pickup = pickup; }

    public void setPickupBox(ProjectileTypeConfig.PickupBox box) {
        this.pickupInflateH = box.inflateH();
        this.pickupInflateV = box.inflateV();
        this.pickupBoxHalfWidth = box.boxWidth() / 2;
        this.pickupBoxHeight = box.boxHeight();
        this.pickupScanRange = computeScanRange(box);
    }

    /** Max arrow-to-player distance where the pickup boxes can overlap (+margin), from the geometry + a conservative player box. Bounds the pre-filter only. */
    private static double computeScanRange(ProjectileTypeConfig.PickupBox box) {
        double maxH = box.inflateH() + 0.3 + box.boxWidth() / 2;
        double maxV = Math.max(box.inflateV() + box.boxHeight(), 2.0 + box.inflateV());
        return Math.sqrt(maxH * maxH + maxV * maxV + maxH * maxH) + 0.25;
    }

    @Override
    protected float hitDamage(ResolvedHit hit, @NotNull Entity target) {
        // vanilla: ceil(speed * (damage + Power bonus)), +rand(i/2+2) on crit
        double damage = hit.damage();
        if (powerLevel() > 0) damage += powerLevel() * 0.5 + 0.5;
        int dmg = (int) Math.ceil(velocityBt.length() * damage);
        if (dmg < 0) dmg = 0;
        if (critical) dmg += ThreadLocalRandom.current().nextInt(dmg / 2 + 2);
        return dmg;
    }

    public void setShakeTicks(int ticks) { this.shakeTicks = ticks; }

    /** Flame rides the ARROW's burning state, not the enchant: a doused arrow ignites nobody, and vanilla wires
     *  the fire boolean to viewers ({@code setOnFire(100)} at shoot). */
    private boolean burning;

    @Override
    public void setProjectileEnchants(int power, int punch, int flame) {
        super.setProjectileEnchants(power, punch, flame);
        if (flame > 0 && !burning) {
            burning = true;
            getEntityMeta().setOnFire(true);
        }
    }

    // vanilla ignites BEFORE the damage roll, so an i-frame deflect still burns (EntityArrow sets fire, then damageEntity)
    @Override
    protected void beforeEntityDamage(@NotNull Entity target) {
        if (burning && target instanceof LivingEntity le) Ignite.ignite(le, Flame.FIRE_TICKS, ProjectileSystem.KEY);
    }

    @Override
    protected void onImpact(@Nullable Entity hitEntity) {
        if (hitEntity != null) Fx.play(services(), Fx.ARROW_HIT, FxContext.of(this)); // block hits go through onStuck
        if (!(hitEntity instanceof LivingEntity le)) return;
        // hit-marker "ding" to the shooter, on a real target - not a self-hit
        if (shooter instanceof Player && shooter != le) Fx.play(services(), Fx.ARROW_HIT_PLAYER, FxContext.of(shooter, le));
        StuckArrows.add(le, 1);
        applyOnHitEffects(le);
    }

    public void setOnHitEffects(List<CustomPotionEffect> effects, float durationScale) {
        this.onHitEffects = effects;
        this.potionDurationScale = durationScale;
    }

    /** Vanilla {@code Arrow.doPostHurtEffects}, routed through the attribute potion lifecycle (TPS scaling, source behavior). */
    private void applyOnHitEffects(LivingEntity le) {
        for (CustomPotionEffect e : onHitEffects) {
            int duration = Math.max(1, Math.round(e.duration() * potionDurationScale));
            VanillaPotions.addEffect(le, new Potion(e.id(), e.amplifier(), duration, (byte) (Potion.PARTICLES_FLAG | Potion.ICON_FLAG)));
        }
    }

    @Override
    protected boolean onStuck() {
        shake = TickScaler.duration(this, shakeTicks, ProjectileSystem.KEY); // decrements per server tick, so scale it
        if (getEntityMeta() instanceof AbstractArrowMeta meta) meta.setInGround(true);
        Fx.play(services(), Fx.ARROW_HIT, FxContext.of(this));
        return super.onStuck();
    }

    @Override
    protected void onUnstuck() {
        if (getEntityMeta() instanceof AbstractArrowMeta meta) meta.setInGround(false);
        super.onUnstuck();
    }

    @Override
    protected void updateProjectile(long time) {
        super.updateProjectile(time);
        if (burning && inWater()) {
            burning = false;
            getEntityMeta().setOnFire(false);
            Fx.play(services(), Fx.FIRE_EXTINGUISH, FxContext.of(this)); // 1.8 fizzes any doused entity, arrows included
        }
        if (deflectVisible && !isStuck()) spawnDeflectTrail();
        if (!isStuck()) return;
        if (shake > 0) { shake--; return; } // vanilla pickup delay: no collecting while the arrow is still shaking
        if (pickup == Pickup.DISALLOWED) return;
        var instance = getInstance();
        if (instance == null) return;
        Pos arrow = getPosition();
        Player[] collected = {null};
        MechanicsWorld.of(this).nearbyPlayers(arrow, pickupScanRange, p -> {
            if (collected[0] == null && WorldPolicy.canAffect(p, this) && canCollect(p) && withinPickupBox(arrow, p)) collected[0] = p;
        });
        Player p = collected[0];
        if (p == null) return;
        // vanilla EntityArrow.d gates on inventory.pickup succeeding; creative takes no item. TODO: offhand
        if (pickup == Pickup.ALLOWED && p.getGameMode() != GameMode.CREATIVE
                && !p.getInventory().addItemStack(ItemStack.of(Material.ARROW))) return; // inventory full -> arrow stays stuck
        // the arrow flies into the collector before remove() sends the destroy
        Fx.play(services(), Fx.ITEM_PICKUP, FxContext.of(this));
        sendPacketToViewersAndSelf(new CollectItemPacket(getEntityId(), p.getEntityId(), 1));
        remove();
    }

    /** Whether {@code p} may collect this arrow given its {@link Pickup} mode (vanilla {@code EntityArrow.d} gate). */
    private boolean canCollect(Player p) {
        // dead players collide with nothing (1.8 EntityPlayer.onUpdate gates the sweep on health > 0)
        if (p.isDead() || p.getGameMode() == GameMode.SPECTATOR) return false;
        return switch (pickup) {
            case DISALLOWED -> false;
            case ALLOWED -> true;
            case CREATIVE_ONLY -> p.getGameMode() == GameMode.CREATIVE;
        };
    }

    /** AABB overlap: the arrow's {@code boxWidth x boxHeight} box vs {@code player}'s bbox inflated by {@code (inflateH, inflateV, inflateH)}. */
    private boolean withinPickupBox(Pos arrow, Player p) {
        BoundingBox bb = p.getBoundingBox();
        Pos pp = p.getPosition();
        return arrow.x() + pickupBoxHalfWidth >= pp.x() + bb.relativeStart().x() - pickupInflateH
                && arrow.x() - pickupBoxHalfWidth <= pp.x() + bb.relativeEnd().x() + pickupInflateH
                && arrow.y() + pickupBoxHeight >= pp.y() + bb.relativeStart().y() - pickupInflateV
                && arrow.y() <= pp.y() + bb.relativeEnd().y() + pickupInflateV
                && arrow.z() + pickupBoxHalfWidth >= pp.z() + bb.relativeStart().z() - pickupInflateH
                && arrow.z() - pickupBoxHalfWidth <= pp.z() + bb.relativeEnd().z() + pickupInflateH;
    }
}
