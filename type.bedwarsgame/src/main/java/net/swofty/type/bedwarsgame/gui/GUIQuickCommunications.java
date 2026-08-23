package net.swofty.type.bedwarsgame.gui;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.bedwars.BedWarsGameType;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig.TeamKey;
import net.swofty.commons.text.Text;
import net.swofty.type.bedwarsgame.game.v2.BedWarsGame;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public class GUIQuickCommunications extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Quick Communications", InventoryType.CHEST_5_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        addSendButton(
            layout,
            10,
            "<a>Hello ( ﾟ◡ﾟ)/!",
            Material.BOOK,
            "<a>Hello ( ﾟ◡ﾟ)/!"
        );
        addSendButton(
            layout,
            11,
            "<a>I'm coming back to base!",
            Material.BOOK,
            "<a>I'm coming back to base!"
        );
        addSendButton(
            layout,
            12,
            "<a>I'm defending!",
            Material.IRON_BARS,
            "<a>I'm defending!"
        );
        addSelectButton(
            layout,
            13,
            "<a>I'm attacking!",
            Material.IRON_SWORD,
            () -> GUISelectAnOption.forTeamCommunication("I'm attacking"),
            "<7>You will be able to select the Team.",
            "",
            "<e>Click to select!"
        );
        addSelectButton(
            layout,
            14,
            "<a>I'm collecting resources!",
            Material.DIAMOND,
            () -> GUISelectAnOption.forResourceCommunication("I'm collecting"),
            "<7>You will be able to select the",
            "<7>Resource.",
            "",
            "<e>Click to select!"
        );
        addSelectButton(
            layout,
            15,
            "<a>I have resources!",
            Material.CHEST,
            () -> GUISelectAnOption.forResourceCommunication("I have"),
            "<7>You will be able to select the",
            "<7>Resource.",
            "",
            "<e>Click to select!"
        );
        addSendButton(
            layout,
            20,
            "<a>Thank You!",
            Material.BOOK,
            "<a>Thank You!"
        );
        addSendButton(
            layout,
            21,
            "<a>Get back to base!",
            Material.BOOK,
            "<a>Get back to base!"
        );
        addSendButton(
            layout,
            22,
            "<a>Please defend!",
            Material.IRON_BARS,
            "<a>Please defend!"
        );
        addSelectButton(
            layout,
            23,
            "<a>Let's attack!",
            Material.IRON_SWORD,
            () -> GUISelectAnOption.forTeamCommunication("Let's attack"),
            "<7>You will be able to select the Team.",
            "",
            "<e>Click to select!"
        );
        addSelectButton(
            layout,
            24,
            "<a>We need resources!",
            Material.DIAMOND,
            () -> GUISelectAnOption.forResourceCommunication("We need"),
            "<7>You will be able to select the",
            "<7>Resource.",
            "",
            "<e>Click to select!"
        );
        addSendButton(
            layout,
            25,
            "<a>Player incoming!!",
            Material.FEATHER,
            "<a>Player incoming!!"
        );

        Components.back(layout, 40, ctx);
    }

    private void addSendButton(ViewLayout<DefaultState> layout,
                               int slot,
                               String title,
                               Material icon,
                               String message) {
        layout.slot(slot, ItemStacks.item(icon, 1, Text.of(title), List.of(
            Text.empty(),
            Text.of("<e>Click to send!")
        )), (click, context) -> {
            if (!(click.player() instanceof BedWarsPlayer player)) {
                return;
            }

            sendTeamQuickMessage(player, Text.of(message));
            playClickSound(player);
            player.closeInventory();
        });
    }

    private void addSelectButton(ViewLayout<DefaultState> layout,
                                 int slot,
                                 String title,
                                 Material icon,
                                 Supplier<GUISelectAnOption> selectViewSupplier,
                                 String... lore) {
        layout.slot(slot, ItemStacks.item(icon, 1, Text.of(title),
            Stream.of(lore).map(Text::of).toList()), (click, context) -> {
            playClickSound(click.player());
            context.push(selectViewSupplier.get());
        });
    }

    static void sendTeamQuickMessage(BedWarsPlayer player, Text message) {
        BedWarsGame game = player.getGame();
        if (game == null) {
            return;
        }

        TeamKey teamKey = resolveTeamKey(player);
        List<BedWarsPlayer> receivers;
        if (game.getGameType() == BedWarsGameType.ONE_EIGHT || teamKey == null) {
            receivers = new ArrayList<>(game.getPlayers());
        } else {
            receivers = game.getPlayersOnTeam(teamKey);
        }

        Text formatted = Text.of("<a><l>TEAM > <r>{}<f>: {}",
            player.getFullDisplayName(), message);
        receivers.forEach(receiver -> receiver.sendMessage(formatted));
    }

    static TeamKey resolveTeamKey(BedWarsPlayer player) {
        String teamName = player.getTeamName();
        if (teamName == null) {
            return null;
        }

        try {
            return TeamKey.valueOf(teamName);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    static void playClickSound(HypixelPlayer player) {
        player.playSound(Sound.sound(Key.key("minecraft:ui.button.click"), Sound.Source.MASTER, 1.0f, 1.0f));
    }

    static void playBuySound(HypixelPlayer player) {
        player.playSound(Sound.sound(Key.key("minecraft:entity.experience_orb.pickup"), Sound.Source.MASTER, 1.0f, 1.0f));
    }
}
