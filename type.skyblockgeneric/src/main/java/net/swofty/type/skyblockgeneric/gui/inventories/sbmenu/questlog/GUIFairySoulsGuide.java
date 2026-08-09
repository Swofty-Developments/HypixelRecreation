package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.questlog;

import net.kyori.adventure.text.Component;
import net.minestom.server.inventory.InventoryType;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.i18n.I18n;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.fairysouls.FairySoulZone;

import java.util.Locale;

public class GUIFairySoulsGuide extends StatelessView {
    private static final int[] LOCATION_SLOTS = {
            11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30
    };

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.translatable("gui_sbmenu.questlog.fairy_souls_guide.title", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 53);
        Components.back(layout, 52, ctx);

        // Miscellaneous fairy souls
        layout.slot(10, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            Locale l = player.getLocale();
            int obtainedSouls = player.getFairySoulHandler().getFound(FairySoulZone.MISC_DUNGEONS) +
                    player.getFairySoulHandler().getFound(FairySoulZone.MISC_FISHING) +
                    player.getFairySoulHandler().getFound(FairySoulZone.MISC_GARDEN) +
                    player.getFairySoulHandler().getFound(FairySoulZone.MISC_PLACEABLE) +
                    player.getFairySoulHandler().getFound(FairySoulZone.MISC_GLACITE_MINESHAFTS);
            int totalSouls = player.getFairySoulHandler().getMax(FairySoulZone.MISC_DUNGEONS) +
                    player.getFairySoulHandler().getMax(FairySoulZone.MISC_FISHING) +
                    player.getFairySoulHandler().getMax(FairySoulZone.MISC_GARDEN) +
                    player.getFairySoulHandler().getMax(FairySoulZone.MISC_PLACEABLE) +
                    player.getFairySoulHandler().getMax(FairySoulZone.MISC_GLACITE_MINESHAFTS);
            return ItemStackCreator.getStackHead(I18n.string("gui_sbmenu.questlog.fairy_souls_guide.miscellaneous", l),
                    "126ec1ca185b47aad39f931db8b0a8500ded86a127a204886ed4b3783ad1775c", 1,
                I18n.string("gui_sbmenu.questlog.fairy_souls_guide.fairy_souls_count", l, Component.text(String.valueOf(obtainedSouls)), Component.text(String.valueOf(totalSouls))),
                I18n.string("gui_sbmenu.questlog.fairy_souls_guide.dungeons", l, Component.text(String.valueOf(player.getFairySoulHandler().getMax(FairySoulZone.MISC_DUNGEONS)))),
                I18n.string("gui_sbmenu.questlog.fairy_souls_guide.fishing", l, Component.text(String.valueOf(player.getFairySoulHandler().getMax(FairySoulZone.MISC_FISHING)))),
                I18n.string("gui_sbmenu.questlog.fairy_souls_guide.garden", l, Component.text(String.valueOf(player.getFairySoulHandler().getMax(FairySoulZone.MISC_GARDEN)))),
                I18n.string("gui_sbmenu.questlog.fairy_souls_guide.placeable", l, Component.text(String.valueOf(player.getFairySoulHandler().getMax(FairySoulZone.MISC_PLACEABLE)))),
                I18n.string("gui_sbmenu.questlog.fairy_souls_guide.glacite_mineshafts", l, Component.text(String.valueOf(player.getFairySoulHandler().getMax(FairySoulZone.MISC_GLACITE_MINESHAFTS)))));
        });

        FairySouls[] allFairySouls = FairySouls.values();
        for (int i = 0; i < LOCATION_SLOTS.length && i < allFairySouls.length; i++) {
            FairySouls fairySoul = allFairySouls[i];
            int slot = LOCATION_SLOTS[i];

            layout.slot(slot, (s, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                return ItemStackCreator.getStackHead("§d" + fairySoul.regionName, fairySoul.texture, 1,
                        "§7Fairy Souls: §e" + player.getFairySoulHandler().getFound(fairySoul.zone) +
                                "§7/§d" + player.getFairySoulHandler().getMax(fairySoul.zone));
            });
        }
    }

    private enum FairySouls {
        THE_END("The End", "7840b87d52271d2a755dedc82877e0ed3df67dcc42ea479ec146176b02779a5", FairySoulZone.THE_END),
        THE_FARMING_ISLAND("The Farming Islands", "4d3a6bd98ac1833c664c4909ff8d2dc62ce887bdcf3cc5b3848651ae5af6b", FairySoulZone.THE_FARMING_ISLANDS),
        DWARVEN_MINES("Dwarven Mines", "6b20b23c1aa2be0270f016b4c90d6ee6b8330a17cfef87869d6ad60b2ffbf3b5", FairySoulZone.DWARVEN_MINES),
        JERRYS_WORKSHOP("Jerry's Workshop", "6dd663136cafa11806fdbca6b596afd85166b4ec02142c8d5ac8941d89ab7", FairySoulZone.JERRYS_WORKSHOP),
        DEEP_CAVERNS("Deep Caverns", "569a1f114151b4521373f34bc14c2963a5011cdc25a6554c48c708cd96ebfc", FairySoulZone.DEEP_CAVERNS),
        CRIMSON_ISLAND("Crimson Isle", "c3687e25c632bce8aa61e0d64c24e694c3eea629ea944f4cf30dcfb4fbce071", FairySoulZone.CRIMSON_ISLE),
        DUNGEON_HUB("Dungeon Hub", "9b56895b9659896ad647f58599238af532d46db9c1b0389b8bbeb70999dab33d", FairySoulZone.DUNGEON_HUB),
        HUB("Hub", "686718d85e25b006f2c8f160f619b23c8fd6ae75ddf1c06308ec0f539d931703", FairySoulZone.HUB),
        SPIDERS_DEN("Spider's Den", "c754318a3376f470e481dfcd6c83a59aa690ad4b4dd7577fdad1c2ef08d8aee6", FairySoulZone.SPIDERS_DEN),
        THE_RIFT("The Rift", "f26192609d6c46ade73e807fc40dbc3a1a1afbb456ae165785b0fe834dd1cb57", FairySoulZone.THE_RIFT),
        GOLD_MINE("Gold Mine", "73bc965d579c3c6039f0a17eb7c2e6faf538c7a5de8e60ec7a719360d0a857a9", FairySoulZone.GOLD_MINE),
        THE_PARK("The Park", "a221f813dacee0fef8c59f76894dbb26415478d9ddfc44c2e708a6d3b7549b", FairySoulZone.THE_PARK),
        GALATEA("Galatea", "a211ac81698c229d8ef2fae89f62a6a961b30d8b82b97161863090e90bff02a5", FairySoulZone.GALATEA),
        BACKWATER_BAYOU("Backwater Bayou", "1c0cd33590f64d346d98cdd01606938742e715dda6737353306a44f81c8ba426", FairySoulZone.BACKWATER_BAYOU),
        LOTUS_ATOLL("Lotus Atoll", "1c0cd33590f64d346d98cdd01606938742e715dda6737353306a44f81c8ba426", FairySoulZone.LOTUS_ATOLL),
        SAFARI("Safari", "686718d85e25b006f2c8f160f619b23c8fd6ae75ddf1c06308ec0f539d931703", FairySoulZone.SAFARI);

        private final String regionName;
        private final String texture;
        private final FairySoulZone zone;

        FairySouls(String regionName, String texture, FairySoulZone zone) {
            this.regionName = regionName;
            this.texture = texture;
            this.zone = zone;
        }
    }
}
