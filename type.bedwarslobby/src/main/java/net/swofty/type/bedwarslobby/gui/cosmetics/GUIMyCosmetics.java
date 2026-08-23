package net.swofty.type.bedwarslobby.gui.cosmetics;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.bedwars.BedwarsLevelUtil;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.collectibles.CollectibleCategory;
import net.swofty.type.generic.collectibles.CollectibleDefinition;
import net.swofty.type.generic.collectibles.bedwars.BedWarsCollectibleCatalog;
import net.swofty.type.generic.collectibles.bedwars.BedWarsCollectibleStateService;
import net.swofty.type.generic.collectibles.bedwars.prestige.BedWarsPrestigeRenderer;
import net.swofty.type.generic.data.datapoints.DatapointLeaderboardLong;
import net.swofty.type.generic.data.handlers.BedWarsDataHandler;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.List;
import java.util.Optional;

public class GUIMyCosmetics extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("My Cosmetics", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        BedWarsCollectibleCatalog.initialize();

        layout.slot(26, ItemStacks.item(Material.ARROW, """
                <a>Projectile Trails\s
                <7>Change your projectile particle trail
                <7>effect.

                <7>Unlocked: <a>0/0 <8>(NaN%)
                <7>Currently Selected:
                <a>None

                <e>Click to view!"""));
        layout.slot(32, ItemStacks.head("73480592266dd7f53681efeee3188af531eea53da4af583a67617deeb4f473", """
                <a>Victory Dances\s
                <7>Celebrate by gloating and showing
                <7>off to other players whenever you
                <7>win!

                <7>Unlocked: <a>0/0 <8>(NaN%)
                <7>Currently Selected:
                <a>None

                <e>Click to view!"""));
        layout.slot(16, ItemStacks.item(Material.IRON_SWORD, """
                <a>Final Kill Effects\s
                <7>A selection of various effects to
                <7>choose from that will trigger
                <7>whenever you final kill an enemy!

                <7>Unlocked: <a>0/0 <8>(NaN%)
                <7>Currently Selected:
                <a>None

                <e>Click to view!"""));
        layout.slot(30, ItemStacks.item(Material.FILLED_MAP, """
                <a>Sprays\s
                <7>Select a spray to show off all over
                <7>the place! Spray slots can be found
                <7>on every spawn island and some
                <7>center islands.

                <7>Unlocked: <a>0/0 <8>(NaN%)
                <7>Currently Selected:
                <a>None

                <e>Click to view!"""));
        layout.slot(20, ItemStacks.head("d55b1aa95fdb777179a4bb9c92f116d787eddc97b9b8c1666256eedf2d6b35", """
                <a>Island Toppers\s
                <7>Select an Island Topper to decorate
                <7>your island with! In Doubles and
                <7>Teams Modes a random player's
                <7>choice from each team is chosen.

                <7>Unlocked: <a>0/0 <8>(NaN%)
                <7>Currently Selected:
                <a>None

                <e>Click to view!"""));
        layout.slot(12, ItemStacks.head("b371e4e1cf6a1a36fdae27137fd9b8748e6169299925f9af2be301e54298c73", """
                <a>Death Cries\s
                <7>Let others know just how salty your
                <7>tears are every time you die with
                <7>these death cries!

                <7>Unlocked: <a>0/0 <8>(NaN%)
                <7>Currently Selected:
                <a>None

                <e>Click to view!"""));
        layout.slot(28, (_, c) -> {
            CosmeticSummary summary = summarize(c.player(), CollectibleCategory.SHOPKEEPER_SKINS);
            return ItemStacks.head("822d8e751c8f2fd4c8942c44bdb2f5ca4d8ae8e575ed3eb34c18a86e93b", """
                    <a>Shopkeeper Skins\s
                    <7>Select from various Shopkeeper
                    <7>skins, which will replace how the
                    <7>Shopkeepers look in-game! In
                    <7>Doubles and Teams Modes a random
                    <7>player's choice from each team is
                    <7>chosen.

                    <7>Unlocked: <a>{}/{} <8>({}%)
                    <7>Currently Selected:
                    <r>{}

                    <e>Click to view!""",
                summary.unlocked(), summary.total(), summary.percent(), summary.selectedDisplay());
        }, (_, context) -> context.push(new GUIShopkeeperSkins()));
        layout.slot(22, ItemStacks.item(Material.OAK_SIGN, """
                <a>Kill Messages\s
                <7>Select a Kill Message package to
                <7>replace chat messages when you kill
                <7>players, Teams and break Beds!

                <7>Unlocked: <a>0/0 <8>(NaN%)
                <7>Currently Selected:
                <a>None

                <e>Click to view!"""));
        layout.slot(24, (_, c) -> {
            BedWarsDataHandler dataHandler = BedWarsDataHandler.getUser(c.player());
            int level = dataHandler == null
                ? 0
                : BedwarsLevelUtil.calculateLevel(
                dataHandler.get(BedWarsDataHandler.Data.EXPERIENCE, DatapointLeaderboardLong.class).getValue());
            return ItemStacks.item(Material.NAME_TAG, """
                    <a>Prestige Customizer
                    <7>Choose the colors, star, and
                    <7>brackets of your prestige!

                    <7>Currently Selected:
                    <r>{}

                    <e>Click to view!""",
                BedWarsPrestigeRenderer.renderBrackets(c.player(), level));
        }, (_, context) -> context.push(new GUIPrestigeCustomizer()));
        layout.slot(18, ItemStacks.item(Material.DIAMOND, """
                <a>Glyphs\s
                <7>Select a Glyph image which will
                <7>appear when picking up diamonds and
                <7>emeralds!

                <7>Unlocked: <a>0/0 <8>(NaN%)
                <7>Currently Selected:
                <a>None

                <e>Click to view!"""));
        layout.slot(10, ItemStacks.item(Material.RED_BED, """
                <a>Bed Destroys\s
                <7>Select from various Bed Destroy
                <7>effects, which will occur when you
                <7>break a bed!

                <7>Unlocked: <a>0/0 <8>(NaN%)
                <7>Currently Selected:
                <a>None

                <e>Click to view!"""));
        layout.slot(34, (_, c) -> {
            CosmeticSummary summary = summarize(c.player(), CollectibleCategory.WOOD_SKINS);
            return ItemStacks.item(Material.DARK_OAK_PLANKS, """
                    <a>Wood Skins\s
                    <7>Change the Skin of Wood in-game.

                    <7>Unlocked: <a>{}/{} <8>({}%)
                    <7>Currently Selected:
                    <r>{}

                    <e>Click to view!""",
                summary.unlocked(), summary.total(), summary.percent(), summary.selectedDisplay());
        }, (_, context) -> context.push(new GUIWoodSkins()));
        layout.slot(14, ItemStacks.item(Material.ARMOR_STAND, """
                <a>Figurines\s
                <7>Choose which of your figurines is
                <7>showcased at your base in games!

                <7>Unlocked: <a>0/0 <8>(NaN%)
                <7>Currently Selected:
                <a>None

                <e>Click to view!"""));
        Components.backOrClose(layout, 48, ctx);
        layout.slot(49, (_, c) -> {
            BedWarsDataHandler dataHandler = BedWarsDataHandler.getUser(c.player());
            long tokens = dataHandler == null
                ? 0L
                : dataHandler.get(BedWarsDataHandler.Data.TOKENS, DatapointLeaderboardLong.class).getValue();
            return ItemStacks.item(Material.EMERALD, """
                    <7>Total Tokens: <2>{:,}
                    <6>https://store.hypixel.net""", tokens);
        });
        layout.slot(50, ItemStacks.item(Material.COMPASS, """
                <a>Search
                <7>Use this feature to easily find a
                <7>specific cosmetic item."""));
    }

    private static CosmeticSummary summarize(HypixelPlayer player, CollectibleCategory category) {
        List<CollectibleDefinition> definitions = BedWarsCollectibleCatalog.getCategoryItems(category);
        int total = definitions.size();
        int unlocked = (int) definitions.stream()
            .filter(definition -> BedWarsCollectibleStateService.checkSelectable(player, definition).selectable())
            .count();

        int percent = total == 0 ? 0 : (int) Math.round((unlocked * 100.0) / total);
        Text selectedDisplay = resolveSelectedDisplay(player, category);
        return new CosmeticSummary(unlocked, total, percent, selectedDisplay);
    }

    private static Text resolveSelectedDisplay(HypixelPlayer player, CollectibleCategory category) {
        String selectedId = BedWarsCollectibleStateService.getSelectedId(player, category);
        if (BedWarsCollectibleStateService.RANDOM_SELECTION_ID.equals(selectedId)) {
            return Text.of("<a>Random");
        }
        if (BedWarsCollectibleStateService.RANDOM_FAVORITE_SELECTION_ID.equals(selectedId)) {
            return Text.of("<a>Random Favorite");
        }

        Optional<CollectibleDefinition> selected = BedWarsCollectibleStateService.resolveSelected(player, category);
        return selected.map(collectibleDefinition -> Text.of("<a>{}", collectibleDefinition.name()))
            .orElse(Text.of("<c>None"));
    }

    private record CosmeticSummary(int unlocked, int total, int percent, Text selectedDisplay) {
    }
}
