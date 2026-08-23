package net.swofty.type.lobby.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;

public class GUIViewPlayer extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.withText((_, ctx) -> Text.literal(ctx.player().getUsername()), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        layout.slot(0, ItemStacks.head(
            ctx.player().getPlayerSkin(),
            ctx.player().getFullDisplayName(),
            Text.of("""
                    <7>Hypixel Level: <6>177
                    <7>Achievement Points: <e>8,505
                    <7>Guild: <b>NONE""").lines()
        ));
        layout.slot(22, ItemStacks.head(
            "3685a0be743e9067de95cd8c6d1ba21ab21d37371b3d597211bb75e43279",
            """
                    <a>Social Media
                    <7>Click to view Player's Social Media
                    <7>links."""
        ));
        layout.slot(23, ItemStacks.item(
            Material.PAPER,
            "<a>Report {}",
            ctx.player().getUsername()
        ));
        layout.slot(29, ItemStacks.head(
            "84e1c42f11383b9dc8e67f2846fa311b16320f2c2ec7e175538dbff1dd94bb7",
            """
                    <a>Gift a Rank
                    <7>Gift a rank to {}.

                    <e>Click to gift!""",
            ctx.player().getFullDisplayName()
        ));
        layout.slot(30, ItemStacks.item(
            Material.DIAMOND,
            """
                    <a>Invite to Party
                    <7>Click here to invite {} to a party.""",
            ctx.player().getUsername()
        ));
        layout.slot(31, ItemStacks.item(
            Material.WRITABLE_BOOK,
            """
                    <a>Add Friend
                    <7>Click here to send {} a friend
                    <7>request.""",
            ctx.player().getUsername()
        ));
        layout.slot(32, ItemStacks.item(
            Material.HOPPER,
            """
                    <a>Block Player
                    <7>Click here to add {} to your
                    <7>block list.""",
            ctx.player().getUsername()
        ));
        layout.slot(33, ItemStacks.item(
            Material.IRON_SWORD,
            "<a>Send Duel Request"
        ));
        layout.slot(39, ItemStacks.item(
            Material.GRAY_STAINED_GLASS_PANE,
            """
                    <a>Invite to guild
                    <7>You can't invite {} to your guild
                    <7>because they are already in a guild.""",
            ctx.player().getUsername()
        ));
        layout.slot(40, ItemStacks.item(
            Material.GRAY_STAINED_GLASS_PANE,
            """
                    <a>Promote Party Role
                    <7>You must be in a party to use this."""
        ));
        layout.slot(41, ItemStacks.item(
            Material.GRAY_STAINED_GLASS_PANE,
            """
                    <a>Promote to higher guild rank
                    <7>You can't use this because {}
                    <7>isn't in your guild.""",
            ctx.player().getUsername()
        ));
        layout.slot(49, ItemStacks.item(
            Material.ARROW,
            "<a>Close"
        ));
    }
}
