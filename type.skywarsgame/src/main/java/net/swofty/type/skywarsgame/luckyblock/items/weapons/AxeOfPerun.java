package net.swofty.type.skywarsgame.luckyblock.items.weapons;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockWeapon;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

public class AxeOfPerun implements LuckyBlockWeapon {

    public static final String ID = "axe_of_perun";
    private static final float LIGHTNING_DAMAGE = 5.0f;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "Axe of Perun";
    }

    @Override
    public Material getBaseMaterial() {
        return Material.DIAMOND_AXE;
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStackCreator.raw(Material.DIAMOND_AXE, """
                        <e>Axe of Perun

                        <7>Strikes <e>lightning</e> on hit.
                        <7>Deals <c>5</c> true damage.

                        <6><l>LUCKY BLOCK ITEM
                        """)
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, ID)
                .build();
    }

    @Override
    public float onWeaponHit(SkywarsPlayer holder, Entity target, float damage) {
        if (target instanceof LivingEntity living) {
            Entity lightning = new Entity(EntityType.LIGHTNING_BOLT);
            lightning.setInstance(target.getInstance(), target.getPosition());

            holder.scheduler().buildTask(lightning::remove)
                    .delay(java.time.Duration.ofMillis(500))
                    .schedule();

            living.damage(Damage.fromEntity(holder, LIGHTNING_DAMAGE));

            holder.sendMessage("<e>Thunder strikes!");
        }

        return damage;
    }

    @Override
    public double getAttackDamage() {
        return 9.0;
    }

    @Override
    public boolean hasOnHitEffect() {
        return true;
    }

    @Override
    public boolean dealsTrueDamage() {
        return true;
    }
}
