package net.swofty.type.skywarsgame.luckyblock.items.usables;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.projectile.ProjectileBehavior;
import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.entities.ManagedProjectile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.Entity;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItem;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class WaterBalloon implements LuckyBlockItem {

    @Override
    public String getId() {
        return "water_balloon";
    }

    @Override
    public String getDisplayName() {
        return "Water Balloon";
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStack.builder(Material.SPLASH_POTION)
                .customName(Component.text(getDisplayName(), NamedTextColor.AQUA)
                        .decoration(TextDecoration.ITALIC, false)
                        .decoration(TextDecoration.BOLD, true))
                .lore(List.of(
                        Component.text("Throw this at your enemies", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("to splash water where it lands!", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.empty(),
                        Component.text("Right-click to throw!", NamedTextColor.YELLOW)
                                .decoration(TextDecoration.ITALIC, false)
                ))
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, getId())
                .build();
    }

    @Override
    public boolean onUse(SkywarsPlayer holder) {
        Instance instance = holder.getInstance();
        if (instance == null) return false;

        Polyp.getInstance().services().projectiles().launch(
                ProjectileSnapshot.of(holder, io.github.term4.polyp.mechanics.projectile.types.SplashPotion.INSTANCE)
                        .withItem(createItemStack())
                        .withBehavior(new ProjectileBehavior() {
                            @Override
                            public void onImpact(ManagedProjectile projectile, @Nullable Entity hit) {
                                placeWaterPool(projectile);
                            }
                        }));

        return true;
    }

    @Override
    public boolean hasUseEffect() {
        return true;
    }

    private static void placeWaterPool(ManagedProjectile projectile) {
        if (projectile.getInstance() == null) return;

        Point pos = projectile.getPosition();

            for (int dx = -1; dx <= 1; dx++) {
                for (int dz = -1; dz <= 1; dz++) {
                    Point waterPos = pos.add(dx, 0, dz);
                    Block blockAt = projectile.getInstance().getBlock(waterPos);
                    if (blockAt.isAir()) {
                        projectile.getInstance().setBlock(waterPos, Block.WATER);
                    } else {
                        Point waterPosAbove = pos.add(dx, 1, dz);
                        Block blockAbove = projectile.getInstance().getBlock(waterPosAbove);
                        if (blockAbove.isAir()) {
                            projectile.getInstance().setBlock(waterPosAbove, Block.WATER);
                        }
                    }
                }
            }
    }
}
