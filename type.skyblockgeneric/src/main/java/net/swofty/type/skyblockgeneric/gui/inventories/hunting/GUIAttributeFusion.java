package net.swofty.type.skyblockgeneric.gui.inventories.hunting;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.*;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointHunting;
import net.swofty.type.skyblockgeneric.hunting.AttributeDefinition;
import net.swofty.type.skyblockgeneric.hunting.AttributeFusionService;
import net.swofty.type.skyblockgeneric.hunting.AttributeRegistry;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class GUIAttributeFusion implements StatefulView<GUIAttributeFusion.State> {
    public record State(int page, String first, String second) {
    }

    @Override
    public State initialState() {
        return new State(1, null, null);
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withString((state, ctx) -> "Fusion Box", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        layout.filler(Layouts.border(0, 53));
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        DatapointHunting.HuntingData data = player.getHuntingData();
        AttributeDefinition first = AttributeRegistry.get(state.first());
        AttributeDefinition second = AttributeRegistry.get(state.second());
        if (first == null || second == null) {
            List<AttributeDefinition> available = AttributeRegistry.values().stream()
                    .filter(value -> !value.id().toString().equals(state.first()))
                    .filter(value -> data.shardCount(value.id()) >= AttributeFusionService.required(value)).toList();
            int start = (state.page() - 1) * AttributeGUIItems.CONTENT_SLOTS.length;
            for (int i = 0; i < AttributeGUIItems.CONTENT_SLOTS.length; i++) {
                int index = start + i;
                if (index >= available.size()) continue;
                AttributeDefinition definition = available.get(index);
                layout.slot(AttributeGUIItems.CONTENT_SLOTS[i],
                        (s, c) -> AttributeGUIItems.shard(definition, AttributeFusionService.required(definition)),
                        (click, c) -> c.session(State.class).setState(first == null
                                ? new State(1, definition.id().toString(), null) : new State(1, first.id().toString(), definition.id().toString())));
            }
            int pages = AttributeGUIItems.pages(available.size());
            if (state.page() > 1)
                layout.slot(45, (s, c) -> ItemStacks.item(Material.ARROW, "<a>Previous Page"),
                        (click, c) -> c.session(State.class).setState(new State(state.page() - 1, state.first(), state.second())));
            if (state.page() < pages)
                layout.slot(53, (s, c) -> ItemStacks.item(Material.ARROW, "<a>Next Page"),
                        (click, c) -> c.session(State.class).setState(new State(state.page() + 1, state.first(), state.second())));
        } else {
            List<AttributeFusionService.FusionResult> results = AttributeFusionService.results(first, second);
            int[] slots = {29, 31, 33};
            for (int i = 0; i < results.size(); i++) {
                AttributeFusionService.FusionResult result = results.get(i);
                layout.slot(slots[i], (s, c) -> AttributeGUIItems.shard(result.definition(), result.amount()),
                        (click, c) -> {
                            if (AttributeFusionService.fuse(data, first, second, result)) {
                                player.getSkills().increase(player, SkillCategories.HUNTING, switch (result.definition().rarity()) {
                                    case COMMON -> 75D;
                                    case UNCOMMON -> 150D;
                                    case RARE -> 300D;
                                    case EPIC -> 500D;
                                    case LEGENDARY -> 1000D;
                                });
                                c.player().sendMessage("<a>Fusion complete! You received <e>{}x <a>{}!",
                                        result.amount(), result.definition().shardName());
                                c.session(State.class).setState(new State(1, null, null));
                            } else c.player().sendMessage("<c>You no longer have enough Shards for that fusion!");
                        });
            }
        }
        layout.slot(4, (s, c) -> ItemStacks.item(Material.BLAST_FURNACE, """
                <a>Fusion Box
                <7>Select two different Shards.
                <7>One of up to three result Shards
                <7>can then be chosen.

                <7>Shards: <2>{}""", data.getShards().values().stream().mapToInt(Integer::intValue).sum()));
        if (first != null)
            layout.slot(47, (s, c) -> AttributeGUIItems.shard(first, AttributeFusionService.required(first)));
        if (second != null)
            layout.slot(51, (s, c) -> AttributeGUIItems.shard(second, AttributeFusionService.required(second)));
        if (first != null) layout.slot(48, (s, c) -> ItemStacks.item(Material.RED_DYE, "<c>Reset"),
                (click, c) -> c.session(State.class).setState(new State(1, null, null)));
        Components.close(layout, 49);
    }
}
