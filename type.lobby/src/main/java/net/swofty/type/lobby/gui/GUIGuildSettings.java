package net.swofty.type.lobby.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.guild.GuildData;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.HypixelSignGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.View;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.guild.GuildManager;

public class GUIGuildSettings implements View<GUIGuildSettings.GuildSettingsState> {

    @Override
    public ViewConfiguration<GuildSettingsState> configuration() {
        return new ViewConfiguration<>("Guild Settings", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<GuildSettingsState> layout, GuildSettingsState state, ViewContext ctx) {
        GuildData guild = state.guild();

        String currentTag = guild.getTag() != null ? guild.getTag() : "None";
        layout.slot(10, ItemStacks.item(Material.NAME_TAG, """
                <a>Guild Tag
                <7>Current: <6>{}
                <7>Changes the tag next to your guild
                <7>members' names.

                <e>Click to edit!""", currentTag), (click, viewCtx) -> new HypixelSignGUI(viewCtx.player())
            .open(new String[]{"Guild Tag", "Enter new tag"})
            .thenAccept(value -> {
                if (value == null || value.isBlank()) {
                    return;
                }
                GuildManager.changeSetting(viewCtx.player(), "tag", value.trim());
            }));

        layout.slot(11, ItemStacks.item(Material.RED_DYE, """
                <a>Guild Tag Color
                <7>Changes the color of the tag next to
                <7>your guild members' names.

                <e>Click to view available colors!"""), (click, viewCtx) -> viewCtx.push(new GUIGuildTagColor()));

        layout.slot(12, ItemStacks.item(Material.COMPARATOR, """
                <a>Guild Permissions
                <7>Modify your guild's ranks & their
                <7>permissions.

                <e>Click to edit!"""), (click, viewCtx) -> viewCtx.player().sendMessage("<c>Guild rank permission editor is not available yet."));

        Text finderStatus = guild.isListedInFinder() ? Text.of("<a>ON") : Text.of("<c>OFF");
        layout.slot(13, ItemStacks.item(Material.SUNFLOWER, """
                <a>Shown in Guild Finder
                <7>Whether or not players can find the
                <7>guild in Guild Finder and request to
                <7>join.
                <7>Currently {}

                <e>Click to toggle!""", finderStatus), (click, viewCtx) -> GuildManager.changeSetting(viewCtx.player(), "finder", "toggle"));

        layout.slot(14, ItemStacks.item(Material.COMPASS, """
                <a>Guild Games
                <7>Changes the Guild's list of games
                <7>used in the Guild Finder.

                <e>Click to pick games!"""), (click, viewCtx) -> viewCtx.player().sendMessage("<c>Guild games selector is not available yet."));

        String description = guild.getDescription() != null && !guild.getDescription().isEmpty()
            ? guild.getDescription() : "Not set";
        layout.slot(15, ItemStacks.item(Material.WRITABLE_BOOK, """
                <a>Guild Description
                <7>Current: <f>{}
                <7>Changes the Guild's description as
                <7>shown in the Guild Finder.

                <e>Click to edit!""", description), (click, viewCtx) -> new HypixelSignGUI(viewCtx.player())
            .open(new String[]{"Description", "Enter description"})
            .thenAccept(value -> {
                if (value == null || value.isBlank()) {
                    return;
                }
                GuildManager.changeSetting(viewCtx.player(), "description", value.trim());
            }));

        Text slowStatus = guild.isSlowChat() ? Text.of("<a>ON") : Text.of("<c>OFF");
        layout.slot(16, ItemStacks.item(Material.ORANGE_DYE, """
                <a>Personal Guild Settings
                <7>Slow Chat: {}

                <e>Click to toggle!""", slowStatus), (click, viewCtx) -> GuildManager.changeSetting(viewCtx.player(), "slow", "toggle"));

        layout.slot(31, ItemStacks.item(Material.ARROW, "<a>Go Back"),
            (click, viewCtx) -> viewCtx.navigator().pop());
    }

    public record GuildSettingsState(GuildData guild) {
    }
}
