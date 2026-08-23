package net.swofty.type.skyblockgeneric.gui.inventories.hunting;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.HypixelSignGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.StatefulPaginatedView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointHunting;
import net.swofty.type.skyblockgeneric.hunting.AttributeDefinition;
import net.swofty.type.skyblockgeneric.hunting.AttributeRegistry;
import net.swofty.type.skyblockgeneric.item.components.AttributeShardComponent;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GUIHuntingBox extends StatefulPaginatedView<AttributeDefinition, GUIHuntingBox.State> {
    public enum Sort {ID_ASCENDING, ID_DESCENDING, NEW_SHARDS}

    public record State(List<AttributeDefinition> items, int page, Sort sort, String query)
            implements PaginatedState<AttributeDefinition> {
        public PaginatedState<AttributeDefinition> withPage(int page) {
            return new State(items, page, sort, query);
        }

        public PaginatedState<AttributeDefinition> withItems(List<AttributeDefinition> items) {
            return new State(items, page, sort, query);
        }
    }

    public State initialState() {
        return new State(List.of(), 0, Sort.ID_ASCENDING, "");
    }

    public void onOpen(State state, ViewContext ctx) {
        if (state.items().isEmpty()) refresh(ctx, state.sort(), state.query());
    }

    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withText((state, ctx) -> Text.of("({}/{}) Hunting Box", state.page() + 1,
                        Math.max(1, (state.items().size() + DEFAULT_SLOTS.length - 1) / DEFAULT_SLOTS.length)),
                InventoryType.CHEST_6_ROW);
    }

    protected int[] getPaginatedSlots() {
        return DEFAULT_SLOTS;
    }

    protected int getPreviousPageSlot() {
        return 45;
    }

    protected int getNextPageSlot() {
        return 53;
    }

    protected boolean shouldFilterFromSearch(State state, AttributeDefinition item) {
        return false;
    }

    protected ItemStack.Builder renderItem(AttributeDefinition definition, int index, HypixelPlayer rawPlayer) {
        SkyBlockPlayer player = (SkyBlockPlayer) rawPlayer;
        return AttributeGUIItems.huntingShard(definition, player.getHuntingData());
    }

    protected void onItemClick(ClickContext<State> click, ViewContext ctx, AttributeDefinition definition, int index) {
        handleShardClick(click.click(), (SkyBlockPlayer) ctx.player(), definition, ctx);
    }

    protected void layoutCustom(ViewLayout<State> layout, State state, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        DatapointHunting.HuntingData data = player.getHuntingData();
        layout.slot(4, ItemStacks.item(Material.CHEST, """
                <a>Hunting Box
                <7>This is where all your Shards are
                <7>stored!

                <7>From here you can Syphon shards,
                <7>or turn them into items!

                <7>Shards: <2>{}

                <e>Click to swap to the Attribute Menu!""",
                data.getShards().values().stream().mapToInt(Integer::intValue).sum()), (_, c) -> c.push(new GUIAttributeMenu()));
        layout.slot(46, ItemStacks.item(Material.OAK_SIGN, """
                <a>Search Shards
                <7>Search for specific shards in your
                <7>Hunting Box. You can enter the
                <7>Shard Name, ID, Family, or Attribute
                <7>Category!

                <e>Click to search!"""), (_, c) -> new HypixelSignGUI(c.player())
                .open(new String[]{"Enter query", state.query()}).thenAccept(q -> {
                    if (q != null) refresh(c, state.sort(), q);
                }));
        layout.slot(48, ItemStacks.item(Material.ARROW, """
                <a>Go Back
                <7>To Hunting Skill"""), (_, c) -> c.navigator().pop());
        layout.slot(50, sortItem(state.sort()), (click, c) -> {
            int direction = click.click() instanceof Click.Right || click.click() instanceof Click.RightShift ? -1 : 1;
            Sort next = Sort.values()[Math.floorMod(state.sort().ordinal() + direction, Sort.values().length)];
            refresh(c, next, state.query());
        });
        layout.slot(51, massSyphonItem(data), (_, c) -> massSyphon((SkyBlockPlayer) c.player(), c));
        layout.slot(52, ItemStacks.item(Material.IRON_HOE, """
                <a>Hunting Toolkit
                <7>Store all of your Hunting Tools in
                <7>one convenient place and swap
                <7>between them with ease.

                <8>Also accessible via /huntingtoolkit

                <e>Click to view!"""));
        Components.close(layout, 49);
    }

    private void refresh(ViewContext ctx, Sort sort, String query) {
        DatapointHunting.HuntingData data = ((SkyBlockPlayer) ctx.player()).getHuntingData();
        List<AttributeDefinition> result = new ArrayList<>(AttributeRegistry.values().stream()
                .filter(d -> data.shardCount(d.id()) > 0)
                .filter(d -> query.isBlank() || AttributeRegistry.search(query).contains(d)).toList());
        Comparator<AttributeDefinition> id = Comparator.comparingInt((AttributeDefinition d) -> d.rarity().ordinal()).thenComparingInt(AttributeDefinition::numericId);
        if (sort == Sort.ID_DESCENDING) result.sort(id.reversed());
        else if (sort == Sort.NEW_SHARDS)
            result.sort(Comparator.comparingLong((AttributeDefinition d) -> data.getDiscoveredAt().getOrDefault(d.id().toString(), 0L)).reversed());
        else result.sort(id);
        ctx.session(State.class).setState(new State(result, 0, sort, query));
    }

    private ItemStack.Builder sortItem(Sort selected) {
        List<Text> lore = new ArrayList<>(List.of(Text.empty()));
        for (Sort sort : Sort.values())
            lore.add(Text.of(sort == selected ? "<b>▶ {}" : "<7>   {}", switch (sort) {
                case ID_ASCENDING -> "ID (Lowest to Highest)";
                case ID_DESCENDING -> "ID (Highest to Lowest)";
                case NEW_SHARDS -> "New Shards";
            }));
        lore.add(Text.empty());
        lore.add(Text.of("<b>Right-click to go backwards!"));
        lore.add(Text.of("<e>Click to switch sort!"));
        return ItemStacks.item(Material.HOPPER, 1, Text.of("<a>Sort"), lore);
    }

    private ItemStack.Builder massSyphonItem(DatapointHunting.HuntingData data) {
        List<AttributeDefinition> usable = AttributeRegistry.values().stream().filter(d -> data.shardCount(d.id()) > 0 && data.level(d.id()) < 10).toList();
        int count = usable.stream().mapToInt(d -> data.shardCount(d.id())).sum();
        List<Text> lore = new ArrayList<>(List.of(
                Text.of("<7>You have <c>{} Shards <7>from <a>{} <7>Attributes", count, usable.size()),
                Text.of("<7>that are not maxed out yet."),
                Text.of("<7>Do you want to Syphon them all?"),
                Text.empty(),
                Text.of("<7>Shards to Syphon:")));
        usable.stream().limit(8).forEach(d -> lore.add(Text.of("<8>- {}x <color:{}>{}", data.shardCount(d.id()),
                d.rarity().itemRarity().getColor(), d.shardName())));
        lore.add(Text.empty());
        lore.add(Text.of("<e>Click to mass Syphon!"));
        return ItemStacks.item(Material.GOLDEN_APPLE, 1, Text.of("<6>Mass Syphon"), lore);
    }

    private void handleShardClick(Click click, SkyBlockPlayer player, AttributeDefinition d, ViewContext ctx) {
        DatapointHunting.HuntingData data = player.getHuntingData();
        if (click instanceof Click.Right || click instanceof Click.RightShift) {
            int amount = click instanceof Click.RightShift ? data.shardCount(d.id()) : 1;
            if (data.removeShards(d.id(), amount)) player.addAndUpdateItem(AttributeShardComponent.create(d, amount));
        } else if (player.getSkills().getCurrentLevel(SkillCategories.HUNTING) >= d.rarity().huntingRequirement()) {
            int amount = click instanceof Click.LeftShift ? data.shardCount(d.id()) : 1;
            int used = data.syphon(d.id(), amount);
            if (used > 0) player.getSkills().increase(player, SkillCategories.HUNTING, used * syphonExperience(d));
        } else
            player.sendMessage("<c>You need Hunting Level {} to Syphon this Attribute!", d.rarity().huntingRequirement());
        refresh(ctx, ctx.session(State.class).state().sort(), ctx.session(State.class).state().query());
    }

    private void massSyphon(SkyBlockPlayer player, ViewContext ctx) {
        for (AttributeDefinition d : AttributeRegistry.values())
            if (player.getSkills().getCurrentLevel(SkillCategories.HUNTING) >= d.rarity().huntingRequirement())
                player.getHuntingData().syphon(d.id(), player.getHuntingData().shardCount(d.id()));
        refresh(ctx, ctx.session(State.class).state().sort(), ctx.session(State.class).state().query());
    }

    private double syphonExperience(AttributeDefinition d) {
        return switch (d.rarity()) {
            case COMMON -> 75;
            case UNCOMMON -> 150;
            case RARE -> 300;
            case EPIC -> 500;
            case LEGENDARY -> 1000;
        };
    }
}
