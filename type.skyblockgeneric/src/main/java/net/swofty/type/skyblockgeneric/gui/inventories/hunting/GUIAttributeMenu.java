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
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GUIAttributeMenu extends StatefulPaginatedView<AttributeDefinition, GUIAttributeMenu.State> {
    public enum SkillFilter {ALL, COMBAT, FISHING, FARMING, FORAGING, MINING, TAMING, ENCHANTING, HUNTING, GLOBAL}

    public enum Sort {ID_ASCENDING, ID_DESCENDING, HIGHEST_LEVEL, LOWEST_LEVEL}

    public record State(List<AttributeDefinition> items, int page, SkillFilter filter, Sort sort, boolean advanced,
                        String query)
            implements PaginatedState<AttributeDefinition> {
        public PaginatedState<AttributeDefinition> withPage(int page) {
            return new State(items, page, filter, sort, advanced, query);
        }

        public PaginatedState<AttributeDefinition> withItems(List<AttributeDefinition> items) {
            return new State(items, page, filter, sort, advanced, query);
        }
    }

    public State initialState() {
        return new State(List.of(), 0, SkillFilter.ALL, Sort.ID_ASCENDING, false, "");
    }

    public void onOpen(State state, ViewContext ctx) {
        if (state.items().isEmpty()) refresh(ctx, state.filter(), state.sort(), state.advanced(), state.query());
    }

    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withText((s, c) -> Text.of("({}/{}) Attribute Menu", s.page() + 1,
                Math.max(1, (s.items().size() + DEFAULT_SLOTS.length - 1) / DEFAULT_SLOTS.length)), InventoryType.CHEST_6_ROW);
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

    protected ItemStack.Builder renderItem(AttributeDefinition item, int index, HypixelPlayer player) {
        return AttributeGUIItems.attribute(item, ((SkyBlockPlayer) player).getHuntingData(), true);
    }

    protected void onItemClick(ClickContext<State> click, ViewContext ctx, AttributeDefinition item, int index) {
        if (click.click() instanceof Click.Right || click.click() instanceof Click.RightShift) {
            ((SkyBlockPlayer) ctx.player()).getHuntingData().toggle(item.id());
            ctx.session(State.class).setState(ctx.session(State.class).state());
        } else ctx.push(new GUIAttributeDetails(item));
    }

    protected void layoutCustom(ViewLayout<State> layout, State state, ViewContext ctx) {
        DatapointHunting.HuntingData data = ((SkyBlockPlayer) ctx.player()).getHuntingData();
        int totalXp = AttributeRegistry.values().stream().mapToInt(d -> data.level(d.id())).sum();
        long maxed = AttributeRegistry.values().stream().filter(d -> data.level(d.id()) >= 10).count();
        int total = AttributeRegistry.values().size();
        layout.slot(4, ItemStacks.item(Material.LEAD, """
                <3>Attribute Menu
                <7>Syphon Shards to unlock and level
                <7>up your <a>Attributes<7>!
                <7>Each Attribute grants its own unique
                <d>power<7>, active at all times, no matter
                <7>where you are.
                <a>Attributes <7>can reach up to level <b>10<7>,
                <7>with shard costs scaling based on
                <7>their <d>rarity<7>.

                <7>Attributes Found: <e>{}<6>/<e>{}
                <7>Attributes Maxed: <e>{}<6>/<e>{}
                <7>Total XP: <b>{}<3>/<b>{}

                <e>Click to swap to the Hunting Box!""",
                data.uniqueAttributes(), total, maxed, total, totalXp, total * 10), (_, c) -> c.push(new GUIHuntingBox()));
        layout.slot(46, ItemStacks.item(Material.OAK_SIGN, """
                <a>Search Attributes
                <7>Search for specific attributes in
                <7>your Attribute Menu. You can enter
                <7>the Attribute Name, ID, Family, or
                <7>Attribute Category!

                <e>Click to search!"""), (_, c) -> new HypixelSignGUI(c.player()).open(new String[]{"Enter query", state.query()})
                .thenAccept(q -> {
                    if (q != null) refresh(c, state.filter(), state.sort(), state.advanced(), q);
                }));
        layout.slot(47, cycleItem("<a>Filter by SkyBlock Skill", Material.ENDER_EYE, SkillFilter.values(), state.filter()), (click, c) -> {
            int direction = isRight(click.click()) ? -1 : 1;
            SkillFilter next = SkillFilter.values()[Math.floorMod(state.filter().ordinal() + direction, SkillFilter.values().length)];
            refresh(c, next, state.sort(), state.advanced(), state.query());
        });
        layout.slot(48, ItemStacks.item(Material.ARROW, """
                <a>Go Back
                <7>To Hunting Box"""), (_, c) -> c.navigator().pop());
        layout.slot(50, cycleItem("<a>Sort", Material.HOPPER, Sort.values(), state.sort()), (click, c) -> {
            int direction = isRight(click.click()) ? -1 : 1;
            Sort next = Sort.values()[Math.floorMod(state.sort().ordinal() + direction, Sort.values().length)];
            refresh(c, state.filter(), next, state.advanced(), state.query());
        });
        layout.slot(51, ItemStacks.item(Material.ANVIL, """
                <a>Attribute Transfer
                <7>Do you still have old items, with
                <7>greyed out Attributes on them?

                <7>You can use this Anvil to transfer
                <7>those Attributes to the new system!

                <e>Click to view!"""));
        layout.slot(52, ItemStacks.item(Material.COMPARATOR, """
                <6>Advanced Mode
                <7>Advanced Mode lets you see every
                <7>attribute, and find out how to obtain
                <7>them.

                <7>Toggled: {}

                <e>Click to toggle!""", state.advanced() ? Text.of("<a>ON") : Text.of("<c>OFF")),
                (_, c) -> refresh(c, state.filter(), state.sort(), !state.advanced(), state.query()));
        Components.close(layout, 49);
    }

    private void refresh(ViewContext ctx, SkillFilter filter, Sort sort, boolean advanced, String query) {
        DatapointHunting.HuntingData data = ((SkyBlockPlayer) ctx.player()).getHuntingData();
        List<AttributeDefinition> result = new ArrayList<>(AttributeRegistry.values().stream()
                .filter(d -> advanced || data.level(d.id()) > 0)
                .filter(d -> filter == SkillFilter.ALL || d.skill().name().equals(filter.name()))
                .filter(d -> query.isBlank() || AttributeRegistry.search(query).contains(d)).toList());
        Comparator<AttributeDefinition> id = Comparator.comparingInt((AttributeDefinition d) -> d.rarity().ordinal()).thenComparingInt(AttributeDefinition::numericId);
        switch (sort) {
            case ID_ASCENDING -> result.sort(id);
            case ID_DESCENDING -> result.sort(id.reversed());
            case HIGHEST_LEVEL ->
                    result.sort(Comparator.comparingInt((AttributeDefinition d) -> data.level(d.id())).reversed().thenComparing(id));
            case LOWEST_LEVEL ->
                    result.sort(Comparator.comparingInt((AttributeDefinition d) -> data.level(d.id())).thenComparing(id));
        }
        ctx.session(State.class).setState(new State(result, 0, filter, sort, advanced, query));
    }

    private <E extends Enum<E>> ItemStack.Builder cycleItem(String name, Material material, E[] values, E selected) {
        List<Text> lore = new ArrayList<>(List.of(Text.empty()));
        for (int i = 0; i < values.length; i++) {
            if (i >= 8) {
                lore.add(Text.of("  <e>And {} more options", values.length - i));
                break;
            }
            lore.add(Text.of(values[i] == selected ? "<b>▶ {}" : "<7>   {}", values[i].name().replace('_', ' ')));
        }
        lore.add(Text.empty());
        lore.add(Text.of("<b>Right-click to go backwards!"));
        lore.add(Text.of("<e>Click to switch!"));
        return ItemStacks.item(material, 1, Text.of(name), lore);
    }

    private boolean isRight(Click click) {
        return click instanceof Click.Right || click instanceof Click.RightShift;
    }
}
