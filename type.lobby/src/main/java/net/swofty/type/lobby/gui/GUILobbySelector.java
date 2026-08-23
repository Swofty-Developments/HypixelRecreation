package net.swofty.type.lobby.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.ServerType;
import net.swofty.commons.UnderstandableProxyServer;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.StatefulView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.lobby.LobbyServerOrder;
import net.swofty.type.lobby.ServerInfoCache;

import java.util.List;

public class GUILobbySelector implements StatefulView<GUILobbySelector.State> {
    private final ServerType lobbyType;
    private final String lobbyName;

    public GUILobbySelector(ServerType lobbyType, String lobbyName) {
        this.lobbyType = lobbyType;
        this.lobbyName = lobbyName;
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>(Text.literal(lobbyName + " Selector"), InventoryType.CHEST_2_ROW);
    }

    @Override
    public State initialState() {
        return new State(null, null);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        if (state.error() != null) {
            layout.slot(4, ItemStacks.item(Material.BARRIER, 1, """
                    <c>Failed to load lobbies
                    <7>{}""", state.error()));
            return;
        }
        if (state.lobbies() == null) {
            layout.slot(4, ItemStacks.item(Material.CLOCK, """
                    <e>Loading lobbies...
                    <7>Please wait..."""));
            load(ctx);
            return;
        }
        if (state.lobbies().isEmpty()) {
            layout.slot(4, ItemStacks.item(Material.BARRIER, """
                    <c>No lobbies available
                    <7>No lobbies are currently online."""));
            return;
        }

        for (int i = 0; i < state.lobbies().size() && i < 18; i++) {
            UnderstandableProxyServer lobby = state.lobbies().get(i);
            boolean current = lobby.uuid().equals(HypixelConst.getServerUUID());
            int number = i + 1;
            layout.slot(i, ItemStacks.item(
                current ? Material.RED_TERRACOTTA : Material.QUARTZ_BLOCK,
                number,
                Text.of((current ? "<c>" : "<a>") + "{} #{}", lobbyName, number),
                List.of(
                    Text.of("<7>Players: {}/{}", lobby.players().size(), lobby.maxPlayers()),
                    Text.empty(),
                    current ? Text.of("<c>Already connected!") : Text.of("<e>Click to connect!")
                )
            ), (_, c) -> connect(c, lobby, number, current));
        }
    }

    private void load(ViewContext ctx) {
        ServerInfoCache.getServersByType(lobbyType)
            .thenApply(LobbyServerOrder::sortBySelectorOrder)
            .thenAccept(lobbies -> ctx.session(State.class).update(_ -> new State(lobbies, null)))
            .exceptionally(error -> {
                ctx.session(State.class).update(_ -> new State(List.of(), error.getMessage()));
                return null;
            });
    }

    private void connect(ViewContext ctx, UnderstandableProxyServer lobby, int number, boolean current) {
        if (current) {
            ctx.player().sendMessage("<c>You are already connected to this lobby!");
            return;
        }
        ctx.player().closeInventory();
        ctx.player().sendMessage("<a>Sending you to {} #{}...", lobbyName, number);
        ctx.player().asProxyPlayer().transferToWithIndication(lobby.uuid());
    }

    public record State(List<UnderstandableProxyServer> lobbies, String error) {
    }
}
