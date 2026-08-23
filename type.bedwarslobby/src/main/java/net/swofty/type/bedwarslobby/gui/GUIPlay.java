package net.swofty.type.bedwarslobby.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.ServerType;
import net.swofty.commons.ServiceType;
import net.swofty.commons.bedwars.BedWarsGameType;
import net.swofty.commons.protocol.objects.orchestrator.ChooseGameProtocol;
import net.swofty.commons.protocol.objects.orchestrator.RejoinGameProtocol;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.lobby.GameQueueValidator;
import net.swofty.type.lobby.LobbyOrchestratorConnector;

import java.util.ArrayList;
import java.util.List;

public class GUIPlay extends StatelessView {
    private static final ProxyService ORCHESTRATOR = new ProxyService(ServiceType.ORCHESTRATOR);

    private final BedWarsGameType type;

    public GUIPlay(BedWarsGameType type) {
        if (type.isDream()) {
            throw new IllegalArgumentException("Dream types should not be used in this GUI!");
        }
        this.type = type;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Play Bed Wars", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        layout.allowHotkey(false);

        int playSlot = type == BedWarsGameType.TWO_FOUR ? 13 : 12;
        layout.slot(playSlot, (s, viewCtx) -> {
            List<Text> lore = new ArrayList<>();
            lore.add(Text.of("<7>Play a game of Bed Wars {}", type.getDisplayName()));
            lore.add(Text.empty());
            for (String line : type.getDescription()) {
                lore.add(Text.of("<7>{}", line));
            }
            lore.add(Text.empty());
            lore.add(Text.of("<e>Click to play!"));
            return ItemStacks.item(Material.RED_BED, 1, Text.of("<a>Bed Wars {}", type.getDisplayName()), lore);
        }, (_, viewCtx) -> {
            var player = viewCtx.player();
            player.closeInventory();

            if (!GameQueueValidator.canPlayerQueue(player, new GameQueueValidator.QueueRequirements(
                "Bed Wars",
                type.getQueueModeDisplayName(),
                type.getTeamSize()
            ))) {
                return;
            }

            LobbyOrchestratorConnector connector = new LobbyOrchestratorConnector(player);
            connector.sendToGame(ServerType.BEDWARS_GAME, type.toString());
        });


        if (type != BedWarsGameType.TWO_FOUR) {
            layout.slot(14,
                (_, _) -> ItemStacks.item(Material.OAK_SIGN, 1, Text.of("<a>Map Selector {}", type.getDisplayName()), List.of(
                    Text.of("<7>Pick which map you want to play from"),
                    Text.of("<7>a list of available maps."),
                    Text.empty(),
                    Text.of("<e>Click to browse!")
                )),
                (_, viewCtx) -> viewCtx.push(new GUIMapSelection(type))
            );
        }

        layout.slot(35,
            (s, viewCtx) -> ItemStacks.item(Material.ENDER_PEARL, """
                <c>Click here to rejoin!
                <7>Click here to join your Bed Wars
                <7>game if you have been disconnected
                <7>from it."""),
            (_, viewCtx) -> {
                var player = viewCtx.player();
                player.closeInventory();

                RejoinGameProtocol.RejoinGameRequest request =
                    new RejoinGameProtocol.RejoinGameRequest(player.getUuid());

                ORCHESTRATOR.handleRequest(request).thenAccept(response -> {
                    if (!(response instanceof RejoinGameProtocol.RejoinGameResponse resp)) {
                        player.sendMessage("<c>Failed to check for active games. Please try again.");
                        return;
                    }

                    if (!resp.hasActiveGame() || resp.server() == null) {
                        player.sendMessage("<c>You don't have an active game to rejoin!");
                        return;
                    }

                    player.sendMessage("<a>Rejoining your game...");

                    ChooseGameProtocol.ChooseGameMessage chooseMsg =
                        new ChooseGameProtocol.ChooseGameMessage(
                            player.getUuid(),
                            resp.server(),
                            resp.gameId()
                        );
                    ORCHESTRATOR.handleRequest(chooseMsg);

                    player.asProxyPlayer().transferToWithIndication(resp.server().uuid());
                    player.getAchievementHandler().completeAchievement("bedwars.rejoining_the_dream");
                }).exceptionally(throwable -> {
                    player.sendMessage("<c>Failed to rejoin: {}", throwable.getMessage());
                    return null;
                });
            }
        );

        Components.backOrClose(layout, 31, ctx);
    }
}
