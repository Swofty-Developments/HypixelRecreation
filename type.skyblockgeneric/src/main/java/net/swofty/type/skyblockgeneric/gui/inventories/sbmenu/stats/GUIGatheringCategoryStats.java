package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.stats;

import lombok.Getter;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIMaterial;
import net.swofty.type.generic.gui.v2.*;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.statistics.PlayerStatistics;
import net.swofty.type.skyblockgeneric.user.statistics.StatisticModifier;
import net.swofty.type.skyblockgeneric.user.statistics.StatisticModifierType;
import net.swofty.type.skyblockgeneric.user.statistics.StatisticSourceType;

import java.util.*;

public class GUIGatheringCategoryStats extends StatelessView {
    private static final int[] DISPLAY_SLOTS = {
        10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29,
        30, 31, 32, 33, 34
    };

    private final Category category;
    private final ItemStatistic statistic;
    private final Mode mode;
    private final boolean showAll;
    private final boolean groupByCategory;
    private final boolean flattened;
    private final StatisticSourceType sourceFilter;
    private final StatisticModifierType modifierFilter;

    public GUIGatheringCategoryStats(Category category) {
        this(category, null, Mode.CATEGORY, true, false, false, null, null);
    }

    private GUIGatheringCategoryStats(Category category, ItemStatistic statistic, Mode mode,
                                      boolean showAll, boolean groupByCategory, boolean flattened,
                                      StatisticSourceType sourceFilter, StatisticModifierType modifierFilter) {
        this.category = category;
        this.statistic = statistic;
        this.mode = mode;
        this.showAll = showAll;
        this.groupByCategory = groupByCategory;
        this.flattened = flattened;
        this.sourceFilter = sourceFilter;
        this.modifierFilter = modifierFilter;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        Text title = switch (mode) {
            case CATEGORY -> Text.of("Your Stats Breakdown");
            case DETAIL -> Text.of("Stats ➜ {}", statistic.getDisplayName());
            case FLAT -> Text.of("{} ➜ Flat Bonuses", statistic.getDisplayName());
            case ADDITIVE -> Text.of("{} ➜ Additive Buffs", statistic.getDisplayName());
        };
        return new ViewConfiguration<>(title, InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        layout.filler(Layouts.border(0, 53));
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);

        // dumb way but it works for now
        switch (mode) {
            case CATEGORY -> categoryLayout(layout);
            case DETAIL -> detailLayout(layout, ctx);
            case FLAT, ADDITIVE -> breakdownLayout(layout);
        }
    }

    private void categoryLayout(ViewLayout<DefaultState> layout) {
        layout.slot(4, (s, c) -> category.createSummary((SkyBlockPlayer) c.player(), false));

        // TODO: using openView breaks back button
        layout.slot(50, toggleItem(), (_, c) -> c.player().openView(
            new GUIGatheringCategoryStats(category, null, Mode.CATEGORY, !showAll, false, false, null, null)));

        for (int index = 0; index < Math.min(category.statistics.size(), DISPLAY_SLOTS.length); index++) {
            int statisticIndex = index;
            layout.slot(DISPLAY_SLOTS[index], (s, c) -> {
                ItemStatistic stat = visibleStatistics((SkyBlockPlayer) c.player()).get(statisticIndex);
                return stat == null ? ItemStack.AIR.builder()
                    : createStatisticItem((SkyBlockPlayer) c.player(), stat, true);
            }, (_, c) -> {
                ItemStatistic stat = visibleStatistics((SkyBlockPlayer) c.player()).get(statisticIndex);
                if (stat != null) openStatistic((SkyBlockPlayer) c.player(), stat);
            });
        }
    }

    private void openStatistic(SkyBlockPlayer player, ItemStatistic stat) {
        ItemStatistics values = player.getStatistics().allStatistics();
        Mode target = values.getAdditive(stat) == 1D && values.getMultiplicative(stat) == 1D
            ? Mode.FLAT : Mode.DETAIL;
        player.openView(new GUIGatheringCategoryStats(category, stat, target, showAll, false, false, null, null));
    }

    private void detailLayout(ViewLayout<DefaultState> layout, ViewContext ctx) {
        layout.slot(4, (s, c) -> createStatisticItem((SkyBlockPlayer) c.player(), statistic, false));
        {
            SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
            ItemStatistics values = player.getStatistics().allStatistics();
            boolean additive = values.getAdditive(statistic) != 1D;
            boolean capped = statistic.getCap() != null;
            int flatSlot = capped ? 20 : 21;
            int additiveSlot = capped ? 22 : 23;

            layout.slot(flatSlot, (s, c) -> summaryItem((SkyBlockPlayer) c.player(), Mode.FLAT),
                (_, c) -> c.player().openView(copy(Mode.FLAT)));
            if (additive) {
                layout.slot(additiveSlot - 1, StatisticArrow.create(statistic),
                    (_, c) -> c.player().openView(copy(Mode.ADDITIVE)));
                layout.slot(additiveSlot, (s, c) -> summaryItem((SkyBlockPlayer) c.player(), Mode.ADDITIVE),
                    (_, c) -> c.player().openView(copy(Mode.ADDITIVE)));
            }
            if (capped) {
                layout.slot(23, StatisticArrow.create(statistic));
                layout.slot(24, capItem());
            }
        }
    }

    private void breakdownLayout(ViewLayout<DefaultState> layout) {
        for (int index = 0; index < DISPLAY_SLOTS.length; index++) {
            int sourceIndex = index;
            layout.slot(DISPLAY_SLOTS[index], (s, c) -> {
                List<ViewEntry> entries = visibleEntries((SkyBlockPlayer) c.player());
                if (sourceIndex >= entries.size()) return ItemStack.AIR.builder();
                return sourceItem(entries.get(sourceIndex));
            }, (_, c) -> {
                List<ViewEntry> entries = visibleEntries((SkyBlockPlayer) c.player());
                if (sourceIndex >= entries.size()) return;
                ViewEntry entry = entries.get(sourceIndex);
                if (!entry.grouped()) return;
                c.player().openView(new GUIGatheringCategoryStats(category, statistic, mode, showAll,
                    groupByCategory, flattened, entry.sourceFilter(), entry.modifierFilter()));
            });
        }
        layout.slot(50, optionItem("Group By Category", Material.NAME_TAG, groupByCategory),
            (_, c) -> c.player().openView(rootCopy(mode, !groupByCategory, flattened)));
        layout.slot(51, optionItem("Flatten Stats Menu",
                flattened ? Material.COBBLESTONE_SLAB : Material.COBBLESTONE, flattened),
            (_, c) -> c.player().openView(rootCopy(mode, groupByCategory, !flattened)));
    }

    private GUIGatheringCategoryStats copy(Mode next) {
        return copy(next, groupByCategory, flattened);
    }

    private GUIGatheringCategoryStats copy(Mode next, boolean grouped, boolean flat) {
        return new GUIGatheringCategoryStats(category, statistic, next, showAll, grouped, flat,
            sourceFilter, modifierFilter);
    }

    private GUIGatheringCategoryStats rootCopy(Mode next, boolean grouped, boolean flat) {
        return new GUIGatheringCategoryStats(category, statistic, next, showAll, grouped, flat, null, null);
    }

    private List<ItemStatistic> visibleStatistics(SkyBlockPlayer player) {
        List<ItemStatistic> visible = category.statistics.stream()
            .filter(stat -> showAll || overall(player, stat) != 0D).toList();
        return new java.util.AbstractList<>() {
            @Override
            public ItemStatistic get(int index) {
                return index < visible.size() ? visible.get(index) : null;
            }

            @Override
            public int size() {
                return Math.max(visible.size(), category.statistics.size());
            }
        };
    }

    private List<ViewEntry> visibleEntries(SkyBlockPlayer player) {
        if (flattened) return flattenedEntries(player);
        List<PlayerStatistics.StatisticSource> sources = player.getStatistics().statisticSources().stream()
            .filter(source -> sourceValue(source.statistics()) != 0D)
            .filter(source -> sourceFilter == null || source.sourceType() == sourceFilter)
            .toList();
        if (!groupByCategory || sourceFilter != null) {
            return sources.stream().map(ViewEntry::fromSource)
                .sorted(Comparator.comparingDouble(this::entryValue).reversed()).toList();
        }

        Map<StatisticSourceType, List<PlayerStatistics.StatisticSource>> grouped = new LinkedHashMap<>();
        for (PlayerStatistics.StatisticSource source : sources) {
            grouped.computeIfAbsent(source.sourceType(), ignored -> new ArrayList<>()).add(source);
        }
        return grouped.entrySet().stream().map(entry -> ViewEntry.fromSources(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparingDouble(this::entryValue).reversed()).toList();
    }

    private List<ViewEntry> flattenedEntries(SkyBlockPlayer player) {
        List<StatisticModifier> modifiers = player.getStatistics().statisticModifiers().stream()
            .filter(modifier -> sourceValue(modifier.statistics()) != 0D)
            .filter(modifier -> modifierFilter == null || modifier.modifierType() == modifierFilter)
            .toList();
        if (!groupByCategory || modifierFilter != null) {
            return modifiers.stream().map(ViewEntry::fromModifier)
                .sorted(Comparator.comparingDouble(this::entryValue).reversed()).toList();
        }

        Map<StatisticModifierType, List<StatisticModifier>> grouped = new LinkedHashMap<>();
        for (StatisticModifier modifier : modifiers) {
            grouped.computeIfAbsent(modifier.modifierType(), ignored -> new ArrayList<>()).add(modifier);
        }
        return grouped.entrySet().stream().map(entry -> ViewEntry.fromModifiers(entry.getKey(), entry.getValue()))
            .sorted(Comparator.comparingDouble(this::entryValue).reversed()).toList();
    }

    private double sourceValue(ItemStatistics statistics) {
        return mode == Mode.FLAT ? statistics.getBase(statistic)
            : (statistics.getAdditive(statistic) - 1D) * 100D;
    }

    private double entryValue(ViewEntry entry) {
        return sourceValue(entry.statistics());
    }

    private ItemStack.Builder summaryItem(SkyBlockPlayer player, Mode summaryMode) {
        ItemStatistics values = player.getStatistics().allStatistics();
        double value = summaryMode == Mode.FLAT ? values.getBase(statistic)
            : (values.getAdditive(statistic) - 1D) * 100D;
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<8>{} Stat", statistic.getDisplayName()));
        lore.add(Text.of(summaryMode == Mode.FLAT
            ? "<7>All flat bonuses are summed into" : "<7>These buffs are added and converted"));
        lore.add(Text.of(summaryMode == Mode.FLAT
            ? "<7>a base amount." : "<7>and then converted into the"));
        if (summaryMode == Mode.ADDITIVE) lore.add(Text.of("<7>(additive) multiplier."));
        lore.add(Text.empty());
        addSourcePreview(player, lore, summaryMode);
        lore.add(Text.empty());
        if (summaryMode == Mode.FLAT) {
            lore.add(Text.of("<7>Adds up to: <color:{}>+{:.2}<glyph:'{}'> {}",
                colour(statistic), value, symbol(statistic), statistic.getDisplayName()));
        } else {
            lore.add(Text.of("<7>Adds up to: <color:{}>+{:.2}%", colour(statistic), value));
        }
        if (summaryMode == Mode.ADDITIVE) {
            lore.add(Text.of("<7>As multiplier: <color:{}>{:.2}x", colour(statistic), 1D + value / 100D));
            lore.add(Text.of("<8>Multiplied with flat!"));
        }
        lore.add(Text.empty());
        lore.add(Text.of("<e>Click to dig deeper!"));
        return ItemStacks.item(summaryMode == Mode.FLAT ? Material.PAPER : Material.BOOK, 1,
            Text.of(summaryMode == Mode.FLAT
                    ? "<color:{}><stat:'{}'> Flat Bonuses" : "<color:{}><stat:'{}'> Additive Buffs",
                colour(statistic), statistic.name()),
            lore);
    }

    private void addSourcePreview(SkyBlockPlayer player, List<Text> lore, Mode summaryMode) {
        int shown = 0;
        for (PlayerStatistics.StatisticSource source : player.getStatistics().statisticSources()) {
            double value = summaryMode == Mode.FLAT ? source.statistics().getBase(statistic)
                : (source.statistics().getAdditive(statistic) - 1D) * 100D;
            if (value == 0D) continue;
            if (shown++ == 7) {
                lore.add(Text.of("  <8>And more..."));
                break;
            }
            if (summaryMode == Mode.FLAT) {
                lore.add(Text.of(" <color:{}>+{:.2}<glyph:'{}'> <f>{}",
                    colour(statistic), value, symbol(statistic), line(source.name())));
            } else {
                lore.add(Text.of(" <color:{}>+{:.2}% <f>{}", colour(statistic), value, line(source.name())));
            }
        }
    }

    private ItemStack.Builder sourceItem(ViewEntry source) {
        double value = entryValue(source);
        List<Text> lore = new ArrayList<>();
        lore.add(source.grouped() ? Text.of("<8>Grouped") : Text.of("<8>{}", line(source.categoryName())));
        lore.add(Text.empty());
        if (mode == Mode.FLAT) {
            lore.add(Text.of("<7>Value: <color:{}>+{:.2}<glyph:'{}'>",
                colour(statistic), value, symbol(statistic)));
        } else {
            lore.add(Text.of("<7>Value: <color:{}>+{:.2}%", colour(statistic), value));
        }
        lore.add(Text.empty());
        int shown = 0;
        if (source.children().size() > 1) {
            for (ViewEntry child : source.children()) {
                double childValue = entryValue(child);
                if (childValue == 0D) continue;
                if (shown++ == 7) {
                    lore.add(Text.of("  <8>And more..."));
                    break;
                }
                if (mode == Mode.FLAT) {
                    lore.add(Text.of(" <color:{}>+{:.2}<glyph:'{}'> <f>{}",
                        colour(statistic), childValue, symbol(statistic), line(child.name())));
                } else {
                    lore.add(Text.of(" <color:{}>+{:.2}% <f>{}", colour(statistic), childValue, line(child.name())));
                }
            }
            lore.add(Text.empty());
        }
        if (source.parentName() != null) {
            lore.add(Text.of("<7>Modifier flattened from:"));
            lore.add(Text.of("<9>{}", line(source.parentName())));
            lore.add(Text.empty());
        }
        source.description().forEach(entry -> lore.add(Text.parse(entry)));
        if (source.grouped()) {
            lore.add(Text.empty());
            lore.add(Text.of("<e>Click to dig even deeper!"));
        }
        Text name = Text.of(source.grouped()
                ? "<color:{}><stat:'{}'> Category: {}" : "<color:{}><stat:'{}'> {}",
            colour(statistic), statistic.name(), line(source.name()));
        GUIMaterial icon = source.texture() == null
            ? new GUIMaterial(source.material()) : new GUIMaterial(source.texture());
        return ItemStacks.of(icon, 1, name, lore);
    }

    private ItemStack.Builder capItem() {
        return ItemStacks.item(Material.LEATHER_HELMET, 1, """
                <color:{}><stat:'{}'> Cap</color>
                <8>{} Stat
                <7>There is a {} limit in SkyBlock!
                <7>Some magic may let you change it!

                <7>Value: <color:{}>{:.2}<glyph:'{}'>""",
            colour(statistic), statistic.name(), statistic.getDisplayName(),
            statistic.getDisplayName().toLowerCase(),
            colour(statistic), statistic.getCap(), symbol(statistic));
    }

    private record ViewEntry(String name, Material material, String texture, ItemStatistics statistics,
                             String categoryName, List<String> description, String parentName,
                             List<ViewEntry> children, boolean grouped,
                             StatisticSourceType sourceFilter, StatisticModifierType modifierFilter) {
        private static ViewEntry fromSource(PlayerStatistics.StatisticSource source) {
            List<ViewEntry> children = source.modifiers().stream().map(ViewEntry::fromModifier).toList();
            return new ViewEntry(source.name(), source.material(), source.texture(), source.statistics(),
                source.sourceType().getDisplayName(), source.sourceType().getDescription(), null,
                children, false, null, null);
        }

        private static ViewEntry fromModifier(StatisticModifier modifier) {
            return new ViewEntry(modifier.name(), modifier.material(), modifier.texture(), modifier.statistics(),
                modifier.modifierType().getDisplayName(), modifier.modifierType().getDescription(),
                modifier.parentName(), List.of(), false, null, null);
        }

        private static ViewEntry fromSources(StatisticSourceType type, List<PlayerStatistics.StatisticSource> sources) {
            ItemStatistics total = ItemStatistics.empty();
            List<ViewEntry> children = new ArrayList<>();
            for (PlayerStatistics.StatisticSource source : sources) {
                total = ItemStatistics.add(total, source.statistics());
                children.add(fromSource(source));
            }
            return new ViewEntry(type.getDisplayName(), type.getMaterial(), null, total,
                type.getDisplayName(), type.getDescription(), null, children, true, type, null);
        }

        private static ViewEntry fromModifiers(StatisticModifierType type, List<StatisticModifier> modifiers) {
            ItemStatistics total = ItemStatistics.empty();
            List<ViewEntry> children = new ArrayList<>();
            for (StatisticModifier modifier : modifiers) {
                total = ItemStatistics.add(total, modifier.statistics());
                children.add(fromModifier(modifier));
            }
            return new ViewEntry(type.getDisplayName(), type.getMaterial(), null, total,
                type.getDisplayName(), type.getDescription(), null, children, true, null, type);
        }
    }

    private ItemStack.Builder toggleItem() {
        return ItemStacks.item(Material.PUFFERFISH, 1, """
                <a>Toggle Show All Stats
                <7>Toggle whether you want to see
                <a>ALL <7>SkyBlock statistics, or just
                <7>the ones you have.

                <7>Show all stats: {}

                <e>Click to toggle!""",
            showAll ? Text.of("<a>Yes") : Text.of("<c>No"));
    }

    private static ItemStack.Builder optionItem(String name, Material material, boolean enabled) {
        return ItemStacks.item(material, 1, """
                <a>{}
                <7>{}

                <7>Enabled: {}

                <e>Click to {}""",
            name,
            name.startsWith("Group") ? "Groups modifiers from the same category."
                : "Breaks down modifiers for comparison.",
            enabled ? Text.of("<a>ON") : Text.of("<c>OFF"),
            enabled ? "disable!" : "enable!");
    }

    private static ItemStack.Builder createStatisticItem(
        SkyBlockPlayer player, ItemStatistic stat, boolean clickable) {
        ItemStatistics values = player.getStatistics().allStatistics();
        double value = values.getOverall(stat);
        List<Text> lore = new ArrayList<>();
        ItemStatistics.getDescription(stat).forEach(entry -> lore.add(Text.parse(entry)));
        lore.add(Text.empty());
        double base = values.getBase(stat);
        double additive = values.getAdditive(stat) - 1D;
        if (base != 0D) lore.add(Text.of("<7>Flat: <color:{}>+{:.2}<glyph:'{}'>",
            colour(stat), base, symbol(stat)));
        if (additive != 0D) lore.add(Text.of("<7>Additive: <color:{}>+{:.2}%", colour(stat), additive * 100D));
        if (stat.getCap() != null) lore.add(Text.of("<7>Stat Cap: <color:{}>{:.2}<glyph:'{}'> {}",
            colour(stat), stat.getCap(), symbol(stat), stat.getDisplayName()));
        if (base != 0D || additive != 0D) lore.add(Text.empty());
        if (stat.name().endsWith("_FORTUNE")) {
            lore.add(Text.of("<7>Bonus drops: <color:{}>+{}!", colour(stat), (int) (value / 100D)));
            lore.add(Text.of("<7>Chance for 1 more: <color:{}>{:.2}%", colour(stat), value % 100D));
            lore.add(Text.empty());
        }
        if (value == 0D) lore.add(Text.of("<8>You have none of this stat!"));
        if (clickable) lore.add(Text.of("<e>Click to view!"));
        GUIMaterial material = stat.getIconTexture() == null
            ? new GUIMaterial(stat.getIconMaterial()) : new GUIMaterial(stat.getIconTexture());
        return ItemStacks.of(material, 1,
            Text.of("<stat:'{}'> <f>{:.2}{}", stat.name(), value, stat.getSuffix()), lore);
    }

    private static double overall(SkyBlockPlayer player, ItemStatistic statistic) {
        return player.getStatistics().allStatistics().getOverall(statistic);
    }

    private static TextColor colour(ItemStatistic statistic) {
        return statistic.getDisplayColor();
    }

    private static String symbol(ItemStatistic statistic) {
        return statistic.getSymbol().name();
    }

    private static Text line(String raw) {
        return Text.parse(raw);
    }

    @Override
    public boolean onBottomClick(net.swofty.type.generic.gui.v2.context.ClickContext<DefaultState> click, ViewContext ctx) {
        return true;
    }

    private enum Mode {CATEGORY, DETAIL, FLAT, ADDITIVE}

    @Getter
    public enum Category {
        COMBAT("<c>Combat Stats", Material.STONE_SWORD, List.of("<7>Stats that influence damage dealt", "<7>and damage taken in combat."), ItemStatistic.COMBAT_STATS),
        MINING("<6>Mining Stats", Material.STONE_PICKAXE, List.of("<7>Stats that influence mining speed,", "<7>power, spread, and drops."), ItemStatistic.MINING_STATS),
        FARMING("<a>Farming Stats", Material.GOLDEN_HOE, List.of("<7>Stats that influence crop drops", "<7>and pest spawns."), ItemStatistic.FARMING_STATS),
        FORAGING("<2>Foraging Stats", Material.JUNGLE_SAPLING, List.of("<7>Stats that influence drops", "<7>received while foraging."), ItemStatistic.FORAGING_STATS),
        FISHING("<b>Fishing Stats", Material.FISHING_ROD, List.of("<7>Stats that influence what and", "<7>how quickly you catch fish."), ItemStatistic.FISHING_STATS),
        HUNTING("<e>Hunting Stats", Material.LEAD, List.of("<7>Stats that influence hunting speed", "<7>and shard drops."), ItemStatistic.HUNTING_STATS),
        WISDOM("<3>Wisdom Stats", Material.BOOK, List.of("<7>Increases the <3>XP</3> you gain", "<7>for your skills."), ItemStatistic.WISDOM_STATS),
        MISC("<d>Misc Stats", Material.CLOCK, List.of("<7>Augments various aspects", "<7>of your gameplay."), ItemStatistic.MISC_STATS);

        private final String title;
        private final Material material;
        private final List<String> description;
        private final List<ItemStatistic> statistics;

        Category(String title, Material material, List<String> description, List<ItemStatistic> statistics) {
            this.title = title;
            this.material = material;
            this.description = description;
            this.statistics = statistics;
        }

        public ItemStack.Builder createProfileSummary(SkyBlockPlayer player) {
            return createSummary(player, true);
        }

        private ItemStack.Builder createSummary(SkyBlockPlayer player, boolean clickable) {
            List<Text> lore = new ArrayList<>();
            description.forEach(entry -> lore.add(Text.of(entry)));
            lore.add(Text.empty());
            for (ItemStatistic stat : statistics) {
                lore.add(Text.of(" <stat:'{}'> <f>{:.2}{}", stat.name(), overall(player, stat), stat.getSuffix()));
            }
            if (clickable) {
                lore.add(Text.empty());
                lore.add(Text.of("<e>Click for details!"));
            }
            return ItemStacks.item(material, 1, Text.of(title), lore);
        }
    }
}
