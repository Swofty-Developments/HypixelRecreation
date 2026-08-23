package net.swofty.type.murdermysterygame.item.impl;

import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.ServerType;
import net.swofty.commons.ServiceType;
import net.swofty.commons.protocol.objects.orchestrator.ChooseGameProtocol;
import net.swofty.commons.protocol.objects.orchestrator.GetServerForMapProtocol;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.murdermysterygame.TypeMurderMysteryGameLoader;
import net.swofty.type.murdermysterygame.game.Game;
import net.swofty.type.murdermysterygame.user.MurderMysteryPlayer;
import net.swofty.type.murdermysterygame.item.SimpleInteractableItem;
import net.swofty.type.generic.gui.inventory.ItemStacks;

public class PlayAgainItem extends SimpleInteractableItem {
    private static final ProxyService PROXY_SERVICE = new ProxyService(ServiceType.ORCHESTRATOR);

    public PlayAgainItem() {
        super("play_again");
    }

    @Override
    public ItemStack getBlandItem() {
        return ItemStacks.item(Material.PAPER, """
                <a>Play Again <7>(Right Click)
                <7>Right-click to queue for another game!""").build();
    }

    @Override
    public void onItemUse(PlayerUseItemEvent event) {
        MurderMysteryPlayer player = (MurderMysteryPlayer) event.getPlayer();
        Game game = TypeMurderMysteryGameLoader.getPlayerGame(player);
        if (game != null) {
            String gameType = game.getGameType().toString();
            game.leave(player);

            // Queue for new game using ProxyService directly
            GetServerForMapProtocol.GetServerForMapMessage message =
                new GetServerForMapProtocol.GetServerForMapMessage(
                    ServerType.MURDER_MYSTERY_GAME,
                    null,
                    gameType,
                    1
                );

            PROXY_SERVICE.handleRequest(message).thenAccept(response -> {
                if (response instanceof GetServerForMapProtocol.GetServerForMapResponse resp) {
                    if (resp.server() != null) {
                        ChooseGameProtocol.ChooseGameMessage chooseMessage =
                            new ChooseGameProtocol.ChooseGameMessage(
                                player.getUuid(),
                                resp.server(),
                                resp.gameId()
                            );

                        PROXY_SERVICE.handleRequest(chooseMessage).thenRun(() -> {
                            player.asProxyPlayer().transferToWithIndication(resp.server().uuid());
                        });
                    }
                }
            });
        }
    }
}
