package io.github.term4.polyp.mechanics.projectile.entities;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.world.MechanicsWorld;
import io.github.term4.polyp.world.WorldPolicy;
import io.github.term4.polyp.config.FieldValue;
import io.github.term4.polyp.mechanics.attribute.catalog.PotionColors;
import io.github.term4.polyp.mechanics.attribute.catalog.VanillaPotions;
import io.github.term4.polyp.mechanics.damage.types.magic.HealOrHarm;
import io.github.term4.polyp.mechanics.projectile.ProjectileConfigResolver;
import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.types.ProjectileTypeConfig;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.item.SplashPotionMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.PotionContents;
import net.minestom.server.network.packet.server.play.WorldEventPacket;
import net.minestom.server.potion.CustomPotionEffect;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Splash potion projectile: applies the item's effect payload to the vanilla splash volume at intensity
 * {@code 1 - sqrt(d)/4}. Two models via {@code modernSplash}: 1.8 (default) gates on center distance² {@code < 16}
 * with the directly-hit entity at 1.0; 26.1 measures box-to-box distance (no direct-hit case). Timed effects only
 * apply above 20 ticks; instant ones route through {@link HealOrHarm}; the glass break is per-viewer level event
 * 2002/2007 ({@link #broadcastSplashEvent}).
 */
public class SplashPotionEntity extends ManagedProjectile {

    private static final double SPLASH_RANGE_SQ = 16.0;
    private static final int MIN_EFFECT_TICKS = 20;
    private static final int SPLASH_LEVEL_EVENT = 2002;
    private static final int SPLASH_LEVEL_EVENT_INSTANT = 2007;

    /** Whether MODERN viewers get the 1.8 particle palette ({@code legacyPotionColors}). */
    private final boolean legacyPalette;
    /** 26.1 impact semantics ({@code modernSplash}). */
    private final boolean modernModel;

    public SplashPotionEntity(@Nullable Entity shooter, @NotNull EntityType entityType,
                              ProjectileSnapshot snap, ProjectileTypeConfig effectiveConfig) {
        super(shooter, entityType, snap, effectiveConfig);
        // a modern client renders the whole entity from this item, so an empty one spawns an invisible potion
        ItemStack item = snap.item();
        ((SplashPotionMeta) getEntityMeta()).setItem(
                item != null && !item.isAir() ? item : ItemStack.of(Material.SPLASH_POTION));
        var ctx = ProjectileConfigResolver.ProjectileContext.of(snap, services());
        this.legacyPalette = FieldValue.resolve(effectiveConfig.legacyPotionColors, ctx, Boolean.FALSE);
        this.modernModel = FieldValue.resolve(effectiveConfig.modernSplash, ctx, Boolean.FALSE);
    }

    @Override
    protected void onImpact(@Nullable Entity hitEntity) {
        Instance instance = getInstance();
        ItemStack item = ((SplashPotionMeta) getEntityMeta()).getItem();
        if (instance == null) return;
        List<CustomPotionEffect> payload = VanillaPotions.payload(item);
        Point at = getPosition();
        if (!payload.isEmpty()) {
            Float scale = item.get(DataComponents.POTION_DURATION_SCALE);
            float durationScale = scale != null ? scale : 1.0f;
            if (modernModel) splashModern(at, payload, durationScale);
            else splash(at, hitEntity, payload, durationScale);
        }
        broadcastSplashEvent(item, payload, at);
    }

    /**
     * The glass break + particle cloud (level event 2002), split per viewer: a legacy client reads the event data as a
     * raw 1.8 potion VALUE (Via passes it through untranslated - RGB would be garbage to it), a modern client as an RGB
     * color ({@code legacyPotionColors} picks the palette).
     *
     * <p>TODO: relocate to the vanilla featureset when it lands, with the other absent vanilla broadcasts.
     */
    private void broadcastSplashEvent(ItemStack item, List<CustomPotionEffect> payload, Point at) {
        PotionContents contents = item.get(DataComponents.POTION_CONTENTS);
        int color = legacyPalette ? PotionColors.legacyColor(contents, payload) : PotionColors.color(contents, payload);
        int legacyValue = VanillaPotions.legacySplashValue(contents != null ? contents.potion() : null);
        int event = modernModel && hasInstantPotion(contents) ? SPLASH_LEVEL_EVENT_INSTANT : SPLASH_LEVEL_EVENT;
        var polyp = Polyp.getInstance();
        var clientInfo = polyp.isInitialized() ? polyp.clientInfo() : null;
        for (Player viewer : getViewers()) {
            boolean legacy = clientInfo != null && clientInfo.isLegacy(viewer);
            viewer.sendPacket(legacy ? new WorldEventPacket(SPLASH_LEVEL_EVENT, at, legacyValue, false)
                    : new WorldEventPacket(event, at, color, false));
        }
    }

    /** 26.1 {@code Potion.hasInstantEffects} on the registry potion - custom effects don't flip the event. */
    private static boolean hasInstantPotion(@Nullable PotionContents contents) {
        if (contents == null || contents.potion() == null) return false;
        for (CustomPotionEffect e : VanillaPotions.effects(contents.potion())) {
            if (e.id() == PotionEffect.INSTANT_HEALTH || e.id() == PotionEffect.INSTANT_DAMAGE) return true;
        }
        return false;
    }

    private void splash(Point at, @Nullable Entity hitEntity,
                        List<CustomPotionEffect> payload, float durationScale) {
        for (Entity entity : MechanicsWorld.of(this).nearbyEntities(at, 8.0)) {
            if (!(entity instanceof LivingEntity living) || !WorldPolicy.canAffect(this, living)) continue;
            // vanilla gathers from the impact box grown (4, 2, 4); the y gate is what the box adds over the distance one
            if (Math.abs(living.getPosition().y() - at.y()) > 2.0 + living.getBoundingBox().height()) continue;
            double distSq = living.getPosition().distanceSquared(at);
            if (distSq >= SPLASH_RANGE_SQ && living != hitEntity) continue;
            double intensity = living == hitEntity ? 1.0 : 1.0 - Math.sqrt(distSq) / 4.0;
            applyPayload(living, at, payload, intensity, durationScale);
        }
    }

    /** 26.1: the potion's REAL box at the impact point (the physics box is a point) vs the target grown by the ramped
     *  hit margin ({@code ProjectileUtil.computeMargin}). */
    private void splashModern(Point at, List<CustomPotionEffect> payload, float durationScale) {
        double halfWidth = getEntityType().width() / 2.0, height = getEntityType().height();
        double margin = Math.max(0.0, Math.min(0.3, (getAliveTicks() - 2) / 20.0));
        for (Entity entity : MechanicsWorld.of(this).nearbyEntities(at, 8.0)) {
            if (!(entity instanceof LivingEntity living) || living.isDead()
                    || !WorldPolicy.canAffect(this, living)) continue; // isAffectedByPotions
            Pos ep = living.getPosition();
            var bb = living.getBoundingBox();
            double ey0 = ep.y() + bb.relativeStart().y(), ey1 = ep.y() + bb.relativeEnd().y();
            // candidates come from the potion box grown (4, 2, 4), un-margined; y is the only axis it gates beyond the distance check
            if (ey0 > at.y() + height + 2.0 || ey1 < at.y() - 2.0) continue;
            double gx = axisGap(ep.x() + bb.relativeStart().x() - margin, ep.x() + bb.relativeEnd().x() + margin,
                    at.x() - halfWidth, at.x() + halfWidth);
            double gy = axisGap(ey0 - margin, ey1 + margin, at.y(), at.y() + height);
            double gz = axisGap(ep.z() + bb.relativeStart().z() - margin, ep.z() + bb.relativeEnd().z() + margin,
                    at.z() - halfWidth, at.z() + halfWidth);
            double distSq = gx * gx + gy * gy + gz * gz;
            if (distSq >= SPLASH_RANGE_SQ) continue;
            applyPayload(living, at, payload, 1.0 - Math.sqrt(distSq) / 4.0, durationScale);
        }
    }

    /** Interval separation, {@code 0} when overlapping - one axis of the 26.1 AABB distance. */
    private static double axisGap(double minA, double maxA, double minB, double maxB) {
        return Math.max(0.0, Math.max(minB - maxA, minA - maxB));
    }

    private void applyPayload(LivingEntity living, Point at, List<CustomPotionEffect> payload,
                              double intensity, float durationScale) {
        for (CustomPotionEffect e : payload) {
            if (HealOrHarm.apply(services(), living, getShooter(), at, e, intensity)) continue;
            int duration = (int) (intensity * e.duration() * durationScale + 0.5);
            if (duration > MIN_EFFECT_TICKS) {
                VanillaPotions.addEffect(living, new Potion(e.id(), e.amplifier(), duration, potionFlags(e)));
            }
        }
    }

    private static byte potionFlags(CustomPotionEffect e) {
        return (byte) ((e.isAmbient() ? Potion.AMBIENT_FLAG : 0)
                | (e.showParticles() ? Potion.PARTICLES_FLAG : 0)
                | (e.showIcon() ? Potion.ICON_FLAG : 0));
    }
}
