package net.swofty.type.skyblockgeneric.gems;

import lombok.Getter;
import lombok.SneakyThrows;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.attribute.attributes.ItemAttributeGemData;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.GemstoneComponent;
import net.swofty.type.skyblockgeneric.item.components.GemstoneImplComponent;

import java.util.List;

@Getter
public enum Gemstone {
    RUBY(List.of(ItemType.ROUGH_RUBY_GEM, ItemType.FLAWED_RUBY_GEM, ItemType.FINE_RUBY_GEM, ItemType.FLAWLESS_RUBY_GEM, ItemType.PERFECT_RUBY_GEM), NamedTextColor.RED, ItemStatistic.HEALTH),
    AMETHYST(List.of(ItemType.ROUGH_AMETHYST_GEM, ItemType.FLAWED_AMETHYST_GEM, ItemType.FINE_AMETHYST_GEM, ItemType.FLAWLESS_AMETHYST_GEM, ItemType.PERFECT_AMETHYST_GEM), NamedTextColor.DARK_PURPLE, ItemStatistic.DEFENSE),
    JADE(List.of(ItemType.ROUGH_JADE_GEM, ItemType.FLAWED_JADE_GEM, ItemType.FINE_JADE_GEM, ItemType.FLAWLESS_JADE_GEM, ItemType.PERFECT_JADE_GEM), NamedTextColor.GREEN, null),
    SAPPHIRE(List.of(ItemType.ROUGH_SAPPHIRE_GEM, ItemType.FLAWED_SAPPHIRE_GEM, ItemType.FINE_SAPPHIRE_GEM, ItemType.FLAWLESS_SAPPHIRE_GEM, ItemType.PERFECT_SAPPHIRE_GEM), NamedTextColor.DARK_AQUA, ItemStatistic.INTELLIGENCE),
    AMBER(List.of(ItemType.ROUGH_AMBER_GEM, ItemType.FLAWED_AMBER_GEM, ItemType.FINE_AMBER_GEM, ItemType.FLAWLESS_AMBER_GEM, ItemType.PERFECT_AMBER_GEM), NamedTextColor.GOLD, ItemStatistic.MINING_SPEED),
    TOPAZ(List.of(ItemType.ROUGH_TOPAZ_GEM, ItemType.FLAWED_TOPAZ_GEM, ItemType.FINE_TOPAZ_GEM, ItemType.FLAWLESS_TOPAZ_GEM, ItemType.PERFECT_TOPAZ_GEM), NamedTextColor.YELLOW, null),
    JASPER(List.of(ItemType.ROUGH_JASPER_GEM, ItemType.FLAWED_JASPER_GEM, ItemType.FINE_JASPER_GEM, ItemType.FLAWLESS_JASPER_GEM, ItemType.PERFECT_JASPER_GEM), NamedTextColor.LIGHT_PURPLE, ItemStatistic.STRENGTH),
    OPAL(List.of(ItemType.ROUGH_OPAL_GEM, ItemType.FLAWED_OPAL_GEM, ItemType.FINE_OPAL_GEM, ItemType.FLAWLESS_OPAL_GEM, ItemType.PERFECT_OPAL_GEM), NamedTextColor.WHITE, ItemStatistic.TRUE_DEFENSE),
    AQUAMARINE(List.of(ItemType.ROUGH_AQUAMARINE_GEM, ItemType.FLAWED_AQUAMARINE_GEM, ItemType.FINE_AQUAMARINE_GEM, ItemType.FLAWLESS_AQUAMARINE_GEM, ItemType.PERFECT_AQUAMARINE_GEM), NamedTextColor.AQUA, ItemStatistic.FISHING_SPEED),
    CITRINE(List.of(ItemType.ROUGH_CITRINE_GEM, ItemType.FLAWED_CITRINE_GEM, ItemType.FINE_CITRINE_GEM, ItemType.FLAWLESS_CITRINE_GEM, ItemType.PERFECT_CITRINE_GEM), NamedTextColor.DARK_RED, ItemStatistic.FORAGING_FORTUNE),
    PERIDOT(List.of(ItemType.ROUGH_PERIDOT_GEM, ItemType.FLAWED_PERIDOT_GEM, ItemType.FINE_PERIDOT_GEM, ItemType.FLAWLESS_PERIDOT_GEM, ItemType.PERFECT_PERIDOT_GEM), NamedTextColor.GREEN, ItemStatistic.FARMING_FORTUNE),
    ONYX(List.of(ItemType.ROUGH_ONYX_GEM, ItemType.FLAWED_ONYX_GEM, ItemType.FINE_ONYX_GEM, ItemType.FLAWLESS_ONYX_GEM, ItemType.PERFECT_ONYX_GEM), NamedTextColor.DARK_GRAY, ItemStatistic.CRITICAL_DAMAGE),
    ;

    public final List<ItemType> item;
    public final ItemStatistic correspondingStatistic;
    public final TextColor color;

    Gemstone(List<ItemType> item, TextColor color, ItemStatistic correspondingStatistic) {
        this.item = item;
        this.color = color;
        this.correspondingStatistic = correspondingStatistic;
    }

    @SneakyThrows
    public static Integer getExtraStatisticFromGemstone(ItemStatistic statistic, SkyBlockItem item) {
        ItemAttributeGemData.GemData gemData;
        if (item.hasComponent(GemstoneComponent.class))
            gemData = item.getAttributeHandler().getGemData();
        else return 0;

        int toReturn = 0;
        for (ItemAttributeGemData.GemData.GemSlots slot : gemData.slots) {
            ItemType gem = slot.filledWith;
            if (gem == null) continue;

            GemstoneImplComponent impl = new SkyBlockItem(gem).getComponent(GemstoneImplComponent.class);
            GemRarity gemRarity = impl.getGemRarity();
            Gemstone gemstone = impl.getGemstone();
            ItemStatistic correspondingStatistic = gemstone.getCorrespondingStatistic();

            if (correspondingStatistic != statistic) continue;
            toReturn += GemStats.getFromGemstoneAndRarity(gemstone, gemRarity)
                    .getFromRarity(item.getAttributeHandler().getRarity());
        }

        return toReturn;
    }

    public static Gemstone getFromItemType(ItemType item) {
        for (Gemstone gemstone : Gemstone.values()) {
            if (gemstone.getItem().contains(item)) {
                return gemstone;
            }
        }
        return null;
    }

    public enum Slots {
        //NORMAL
        RUBY("Ruby", "❤", NamedTextColor.RED, Material.RED_STAINED_GLASS_PANE, List.of(Gemstone.RUBY)),
        AMETHYST("Amethyst", "❈", NamedTextColor.DARK_PURPLE, Material.PURPLE_STAINED_GLASS_PANE, List.of(Gemstone.AMETHYST)),
        JADE("Jade", "☘", NamedTextColor.GREEN, Material.LIME_STAINED_GLASS_PANE, List.of(Gemstone.JADE)),
        SAPPHIRE("Sapphire", "✎", NamedTextColor.BLUE, Material.LIGHT_BLUE_STAINED_GLASS_PANE, List.of(Gemstone.SAPPHIRE)),
        AMBER("Amber", "⸕", NamedTextColor.GOLD, Material.ORANGE_STAINED_GLASS_PANE, List.of(Gemstone.AMBER)),
        TOPAZ("Topaz", "✧", NamedTextColor.YELLOW, Material.YELLOW_STAINED_GLASS_PANE, List.of(Gemstone.TOPAZ)),
        JASPER("Jasper", "❁", NamedTextColor.LIGHT_PURPLE, Material.PINK_STAINED_GLASS_PANE, List.of(Gemstone.JASPER)),
        OPAL("Opal", "❂", NamedTextColor.WHITE, Material.WHITE_STAINED_GLASS_PANE, List.of(Gemstone.OPAL)),
        AQUAMARINE("Aquamarine", "☂", NamedTextColor.AQUA, Material.LIGHT_BLUE_STAINED_GLASS_PANE, List.of(Gemstone.AQUAMARINE)),
        CITRINE("Citrine", "☘", NamedTextColor.DARK_RED, Material.RED_STAINED_GLASS_PANE, List.of(Gemstone.CITRINE)),
        PERIDOT("Peridot", "☘", NamedTextColor.DARK_GREEN, Material.GREEN_STAINED_GLASS_PANE, List.of(Gemstone.PERIDOT)),
        ONYX("Onyx", "☠", NamedTextColor.DARK_GRAY, Material.BLACK_STAINED_GLASS_PANE, List.of(Gemstone.ONYX)),

        //SPECIAL
        COMBAT("Combat", "⚔", NamedTextColor.DARK_RED, Material.RED_STAINED_GLASS_PANE, List.of(Gemstone.RUBY, Gemstone.AMETHYST, Gemstone.SAPPHIRE, Gemstone.JASPER, Gemstone.ONYX, Gemstone.OPAL)),
        DEFENSIVE("Defensive", "☤", NamedTextColor.GREEN, Material.LIME_STAINED_GLASS_PANE, List.of(Gemstone.RUBY, Gemstone.AMETHYST, Gemstone.OPAL)),
        OFFENSIVE("Offensive", "☠", NamedTextColor.DARK_BLUE, Material.BLUE_STAINED_GLASS_PANE, List.of(Gemstone.SAPPHIRE, Gemstone.JASPER)),
        MINING("Mining", "✦", NamedTextColor.DARK_PURPLE, Material.PURPLE_STAINED_GLASS_PANE, List.of(Gemstone.JADE, Gemstone.AMBER, Gemstone.TOPAZ)),
        CHISEL("Chisel", "❥", NamedTextColor.GOLD, Material.ORANGE_STAINED_GLASS_PANE, List.of(Gemstone.CITRINE, Gemstone.AQUAMARINE, Gemstone.ONYX, Gemstone.PERIDOT)),
        UNIVERSAL("Universal", "❂", NamedTextColor.WHITE, Material.WHITE_STAINED_GLASS_PANE, List.of(Gemstone.values())),
        ;

        @Getter
        public final String name;
        @Getter
        public final String symbol;
        @Getter
        public final TextColor color;
        @Getter
        public final Material paneColor;
        @Getter
        public final List<Gemstone> validGemstones;

        Slots(String name, String symbol, TextColor color, Material paneColor, List<Gemstone> validGemstones) {
            this.name = name;
            this.symbol = symbol;
            this.color = color;
            this.paneColor = paneColor;
            this.validGemstones = validGemstones;
        }

        public static Slots getFromGemstone(Gemstone gemstone) {
            for (Slots slots : Slots.values()) {
                if (slots.getValidGemstones().contains(gemstone) && slots.getValidGemstones().size() == 1) {
                    return slots;
                }
            }
            return null;
        }
    }
}
