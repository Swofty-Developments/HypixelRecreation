package net.swofty.type.skywarsgame.luckyblock.items.weapons;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockWeapon;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

public class Railgun implements LuckyBlockWeapon {

    public static final String ID = "railgun";
    private static final float TRUE_DAMAGE = 12.0f;
    private static final double MAX_RANGE = 50.0;
    private static final double HIT_RADIUS = 1.0;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "Railgun";
    }

    @Override
    public Material getBaseMaterial() {
        return Material.BLAZE_ROD;
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStacks.raw(Material.BLAZE_ROD, """
                        <6>Railgun

                        <7>Single-shot weapon.

                        <7>Right-click to fire a
                        <7>devastating ray that deals
                        <c>12 true damage<7> to the first
                        <7>target within 50 blocks.

                        <7>Uses: <a>1

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

        LivingEntity target = findTarget(holder, instance, eyePos, direction);

        if (target != null) {
            target.damage(Damage.fromEntity(holder, TRUE_DAMAGE));
            holder.sendMessage("<c>Direct hit!");
        } else {
            holder.sendMessage("<7>Missed!");
        }

        return true;
    }

    private LivingEntity findTarget(SkywarsPlayer holder, Instance instance, Pos eyePos, Vec direction) {
        for (double distance = 0; distance <= MAX_RANGE; distance += 0.5) {
            Vec rayPoint = eyePos.asVec().add(direction.mul(distance));

            for (Entity entity : instance.getEntities()) {
                if (entity == holder) continue;
                if (!(entity instanceof LivingEntity living)) continue;
                if (entity instanceof Player player && player.getGameMode().name().equals("SPECTATOR")) continue;

                double entityDistance = entity.getPosition().asVec().distance(rayPoint);
                if (entityDistance <= HIT_RADIUS) {
                    return living;
                }
            }
        }
        return null;
    }

    @Override
    public float onWeaponHit(SkywarsPlayer holder, Entity target, float damage) {
        return damage;
    }

    @Override
    public int getMaxUses() {
        return 1;
    }

    @Override
    public boolean hasUseEffect() {
        return true;
    }

    @Override
    public boolean hasOnHitEffect() {
        return false;
    }

    @Override
    public boolean dealsTrueDamage() {
        return true;
    }
}
