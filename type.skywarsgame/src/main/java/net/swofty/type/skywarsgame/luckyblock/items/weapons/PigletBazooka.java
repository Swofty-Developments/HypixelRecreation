package net.swofty.type.skywarsgame.luckyblock.items.weapons;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.utility.EntityUtility;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockWeapon;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

import java.time.Duration;

public class PigletBazooka implements LuckyBlockWeapon {

    public static final String ID = "piglet_bazooka";
    private static final float DAMAGE = 6.0f;
    private static final double KNOCKBACK_POWER = 25.0;
    private static final double PROJECTILE_SPEED = 40.0;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "Piglet Bazooka";
    }

    @Override
    public Material getBaseMaterial() {
        return Material.WHEAT;
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStackCreator.raw(Material.WHEAT, 3, """
                        <d>Piglet Bazooka

                        <7>Launch a piglet at
                        <7>your enemies!

                        <7>Deals <c>6 damage</c> on hit!

                        <7>Uses: <a>3

                        <6><l>LUCKY BLOCK ITEM
                        """)
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, ID)
                .build();
    }

    @Override
    public boolean onUse(SkywarsPlayer holder) {
        Instance instance = holder.getInstance();
        if (instance == null) return false;

        Pos eyePos = holder.getPosition().add(0, holder.getEyeHeight(), 0);
        Vec direction = holder.getPosition().direction();

        EntityCreature pig = new EntityCreature(EntityType.PIG);
        EntityUtility.nameEntityVisible(pig, "<d>Projectile Piglet");
        pig.setNoGravity(true);
        pig.setInstance(instance, eyePos);
        pig.setVelocity(direction.mul(PROJECTILE_SPEED));

        pig.scheduler().buildTask(() -> {
            if (pig.isRemoved()) return;

            for (Entity entity : instance.getEntities()) {
                if (entity == pig || entity == holder) continue;
                if (!(entity instanceof LivingEntity living)) continue;
                if (entity instanceof Player player && player.getGameMode().name().equals("SPECTATOR")) continue;

                double distance = pig.getPosition().distance(entity.getPosition());
                if (distance < 2.0) {
                    living.damage(new Damage(DamageType.MOB_ATTACK, pig, holder, null, DAMAGE));

                    Vec knockback = direction.mul(KNOCKBACK_POWER).add(0, 10, 0);
                    living.setVelocity(knockback);

                    pig.remove();
                    return;
                }
            }
        }).repeat(TaskSchedule.tick(1)).schedule();

        pig.scheduler().buildTask(pig::remove)
                .delay(Duration.ofSeconds(5))
                .schedule();

        return true;
    }

    @Override
    public float onWeaponHit(SkywarsPlayer holder, Entity target, float damage) {
        return damage;
    }

    @Override
    public int getMaxUses() {
        return 3;
    }

    @Override
    public boolean hasUseEffect() {
        return true;
    }

    @Override
    public boolean hasOnHitEffect() {
        return false;
    }
}
