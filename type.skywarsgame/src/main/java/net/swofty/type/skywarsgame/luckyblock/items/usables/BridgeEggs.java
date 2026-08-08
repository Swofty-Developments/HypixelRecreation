package net.swofty.type.skywarsgame.luckyblock.items.usables;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.projectile.ProjectileBehavior;
import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.entities.ManagedProjectile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockConsumable;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

import java.util.List;

public class BridgeEggs implements LuckyBlockConsumable {

    private static final Block BRIDGE_BLOCK = Block.WHITE_WOOL;

    @Override
    public String getId() {
        return "bridge_eggs";
    }

    @Override
    public String getDisplayName() {
        return "Bridge Eggs";
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStack.builder(Material.EGG)
                .amount(4)
                .customName(Component.text(getDisplayName(), NamedTextColor.WHITE)
                        .decoration(TextDecoration.ITALIC, false)
                        .decoration(TextDecoration.BOLD, true))
                .lore(List.of(
                        Component.text("Throw an egg that creates", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("a bridge as it flies!", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.empty(),
                        Component.text("Right-click to throw!", NamedTextColor.YELLOW)
                                .decoration(TextDecoration.ITALIC, false)
                ))
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, getId())
                .build();
    }

    @Override
    public void onConsume(SkywarsPlayer player) {
        Polyp.getInstance().services().projectiles().launch(
                ProjectileSnapshot.of(player, io.github.term4.polyp.mechanics.projectile.types.Egg.INSTANCE)
                        .withBehavior(new ProjectileBehavior() {
                            @Override
                            public void onTick(ManagedProjectile egg, long time) {
                                if (egg.getInstance() == null) return;
                                Vec velocity = egg.velocityBt();
                                double length = Math.hypot(velocity.x(), velocity.z());
                                if (length == 0) return;
                                Point center = egg.getPosition().sub(0, 1, 0)
                                        .add(-velocity.x() / length, 0, -velocity.z() / length);
                                for (int x = -1; x <= 0; x++)
                                    for (int z = -1; z <= 0; z++) {
                                        Point at = center.add(x, 0, z);
                                        if (egg.getInstance().getBlock(at).isAir())
                                            egg.getInstance().setBlock(at, BRIDGE_BLOCK);
                                    }
                            }
                        }));
    }
}
