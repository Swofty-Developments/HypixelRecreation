package io.github.term4.polyp.vri;

import io.github.term4.polyp.entity.DroppedItemEntity;
import io.github.term4.polyp.item.Enchants;
import net.kyori.adventure.key.Key;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.Tool;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Block drops - Minestom drops nothing on break. First-match {@link DropRule} chain over a {@link DropContext}:
 * {@code chain(REQUIRE_CORRECT_TOOL, myOres, IDENTITY)}. Vanilla spawn: blockPos + rand*0.5+0.25 per axis,
 * motion (rand*0.2-0.1, 0.2, ..) b/t, 10t pickup delay; creative/spectator drop nothing.
 */
public final class BlockDrops {

    /** {@link #correctTool} = the 26.1 gate: no tool required or the held {@code minecraft:tool} is
     *  correct-for-drops (1.8's table differs - custom rule for exact legacy harvesting). */
    public record DropContext(@NotNull Player player, @NotNull Block block, @NotNull ItemStack tool,
                              int fortune, boolean silkTouch, boolean correctTool) {}

    /** One link of the chain: the drops for this break, or {@code null} to pass on. Empty list = drop nothing (handled). */
    @FunctionalInterface
    public interface DropRule {
        @Nullable List<ItemStack> drops(@NotNull DropContext ctx);
    }

    /** The block's own item, 1x; blocks without an item form drop nothing. */
    public static final DropRule IDENTITY = ctx -> {
        Material material = Material.fromKey(ctx.block().key());
        return material == null ? List.of() : List.of(ItemStack.of(material));
    };

    /** Vanilla harvest gate: a block that requires a tool drops nothing without the correct one; otherwise passes. */
    public static final DropRule REQUIRE_CORRECT_TOOL = ctx -> ctx.correctTool() ? null : List.of();

    /** First-match chain; an exhausted chain drops nothing. */
    public static DropRule chain(DropRule... rules) {
        return ctx -> {
            for (DropRule rule : rules) {
                List<ItemStack> drops = rule.drops(ctx);
                if (drops != null) return drops;
            }
            return List.of();
        };
    }

    /** Correct-tool gate, then the block's own item. */
    public static final DropRule VANILLA = chain(REQUIRE_CORRECT_TOOL, IDENTITY);

    private static final Key FORTUNE = Key.key("minecraft:fortune");
    private static final Key SILK_TOUCH = Key.key("minecraft:silk_touch");
    private static final int PICKUP_DELAY_TICKS = 10;

    private BlockDrops() {}

    public static void install(EventNode<@NotNull Event> node, Vri vri) {
        node.addListener(PlayerBlockBreakEvent.class, e -> {
            if (e.isCancelled()) return;
            GameMode mode = e.getPlayer().getGameMode();
            if (mode == GameMode.CREATIVE || mode == GameMode.SPECTATOR) return;
            VriConfig cfg = vri.configFor(e.getPlayer());
            if (cfg.blockDrops == null) return;

            ItemStack tool = e.getPlayer().getItemInMainHand();
            Tool component = tool.get(DataComponents.TOOL);
            boolean correct = !e.getBlock().registry().requiresTool()
                    || (component != null && component.isCorrectForDrops(e.getBlock()));
            DropContext ctx = new DropContext(e.getPlayer(), e.getBlock(), tool,
                    Enchants.level(tool, FORTUNE), Enchants.level(tool, SILK_TOUCH) > 0, correct);

            List<ItemStack> drops = cfg.blockDrops.drops(ctx);
            if (drops == null) return;
            var rnd = ThreadLocalRandom.current();
            for (ItemStack stack : drops) {
                if (stack.isAir()) continue;
                DroppedItemEntity.spawn(e.getInstance(),
                        new Pos(e.getBlockPosition().x() + rnd.nextDouble() * 0.5 + 0.25,
                                e.getBlockPosition().y() + rnd.nextDouble() * 0.5 + 0.25,
                                e.getBlockPosition().z() + rnd.nextDouble() * 0.5 + 0.25),
                        new Vec(rnd.nextDouble() * 0.2 - 0.1, 0.2, rnd.nextDouble() * 0.2 - 0.1),
                        stack, cfg.itemPhysics, PICKUP_DELAY_TICKS, ItemSpawnEvent.Cause.BLOCK_DROP, e.getPlayer());
            }
        });
    }
}
