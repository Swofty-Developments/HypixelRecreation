package net.swofty.type.skywarsgame.luckyblock.items.weapons;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.projectile.ProjectileBehavior;
import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.entities.ManagedProjectile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockWeapon;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class Paintballs implements LuckyBlockWeapon {

    public static final String ID = "paintballs";

    private static final Block[] TERRACOTTA_COLORS = {
            Block.WHITE_TERRACOTTA,
            Block.ORANGE_TERRACOTTA,
            Block.MAGENTA_TERRACOTTA,
            Block.LIGHT_BLUE_TERRACOTTA,
            Block.YELLOW_TERRACOTTA,
            Block.LIME_TERRACOTTA,
            Block.PINK_TERRACOTTA,
            Block.GRAY_TERRACOTTA,
            Block.LIGHT_GRAY_TERRACOTTA,
            Block.CYAN_TERRACOTTA,
            Block.PURPLE_TERRACOTTA,
            Block.BLUE_TERRACOTTA,
            Block.BROWN_TERRACOTTA,
            Block.GREEN_TERRACOTTA,
            Block.RED_TERRACOTTA,
            Block.BLACK_TERRACOTTA
    };

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "Paintballs";
    }

    @Override
    public Material getBaseMaterial() {
        return Material.SNOWBALL;
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStack.builder(Material.SNOWBALL)
                .amount(64)
                .customName(Component.text("Paintballs", NamedTextColor.LIGHT_PURPLE)
                        .decoration(TextDecoration.ITALIC, false))
                .lore(List.of(
                        Component.empty(),
                        Component.text("Throw to paint blocks", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("with random colors!", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.empty(),
                        Component.text("Uses: ", NamedTextColor.GRAY)
                                .append(Component.text("64", NamedTextColor.GREEN))
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
        if (holder.getInstance() == null) return false;

        Polyp.getInstance().services().projectiles().launch(
                ProjectileSnapshot.of(holder, io.github.term4.polyp.mechanics.projectile.types.Snowball.INSTANCE)
                        .withBehavior(new ProjectileBehavior() {
                            @Override
                            public void onImpact(ManagedProjectile projectile, @Nullable Entity hit) {
                                paintNearbyBlocks(projectile);
                            }
                        }));

        return true;
    }

    @Override
    public float onWeaponHit(SkywarsPlayer holder, Entity target, float damage) {
        return damage;
    }

    @Override
    public int getMaxUses() {
        return 64;
    }

    @Override
    public boolean hasUseEffect() {
        return true;
    }

    @Override
    public boolean hasOnHitEffect() {
        return false;
    }

    private static void paintNearbyBlocks(ManagedProjectile projectile) {
        if (projectile.getInstance() == null) return;

            Block color = TERRACOTTA_COLORS[ThreadLocalRandom.current().nextInt(TERRACOTTA_COLORS.length)];
        Point pos = projectile.getPosition();

            for (int dx = -1; dx <= 1; dx++) {
                for (int dy = -1; dy <= 1; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        Point blockPos = pos.add(dx, dy, dz);
                        Block block = projectile.getInstance().getBlock(blockPos);
                        if (!block.isAir() && !block.compare(Block.BEDROCK)) {
                            projectile.getInstance().setBlock(blockPos, color);
                        }
                    }
                }
            }
    }
}
