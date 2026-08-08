package net.swofty.type.skywarsgame.luckyblock.items.weapons;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockWeapon;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

import java.util.List;
import java.util.Random;

public class Shotgun implements LuckyBlockWeapon {

    public static final String ID = "shotgun";
    private static final int PELLET_COUNT = 5;
    private static final double SPREAD_ANGLE = 15.0;
    private static final double POWER = 1.5;
    private static final Random RANDOM = new Random();

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "Shotgun";
    }

    @Override
    public Material getBaseMaterial() {
        return Material.COMPARATOR;
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStack.builder(Material.COMPARATOR)
                .amount(6)
                .customName(Component.text("Shotgun", NamedTextColor.GRAY)
                        .decoration(TextDecoration.ITALIC, false))
                .lore(List.of(
                        Component.empty(),
                        Component.text("Right-click to fire 5", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("short-range projectiles", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("in a spread pattern.", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.empty(),
                        Component.text("Uses: ", NamedTextColor.GRAY)
                                .append(Component.text("6", NamedTextColor.GREEN))
                                .decoration(TextDecoration.ITALIC, false),
                        Component.empty(),
                        Component.text("LUCKY BLOCK ITEM", NamedTextColor.GOLD)
                                .decoration(TextDecoration.ITALIC, false)
                                .decoration(TextDecoration.BOLD, true)
                ))
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, ID)
                .build();
    }

    @Override
    public boolean onUse(SkywarsPlayer holder) {
        Instance instance = holder.getInstance();
        if (instance == null) return false;

        Pos eyePos = holder.getPosition().add(0, holder.getEyeHeight(), 0);
        float baseYaw = holder.getPosition().yaw();
        float basePitch = holder.getPosition().pitch();

        for (int i = 0; i < PELLET_COUNT; i++) {
            float yawOffset = (float) ((RANDOM.nextDouble() - 0.5) * 2 * SPREAD_ANGLE);
            float pitchOffset = (float) ((RANDOM.nextDouble() - 0.5) * 2 * SPREAD_ANGLE);

            Polyp.getInstance().services().projectiles().launch(
                    ProjectileSnapshot.of(holder, io.github.term4.polyp.mechanics.projectile.types.Snowball.INSTANCE)
                            .withSpawnPos(eyePos)
                            .withVelocity(new Pos(0, 0, 0, baseYaw + yawOffset, basePitch + pitchOffset)
                                    .direction().mul(POWER)));
        }

        return true;
    }

    @Override
    public float onWeaponHit(SkywarsPlayer holder, Entity target, float damage) {
        return damage;
    }

    @Override
    public int getMaxUses() {
        return 6;
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
