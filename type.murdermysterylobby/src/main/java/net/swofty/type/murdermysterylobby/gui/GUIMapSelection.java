package net.swofty.type.murdermysterylobby.gui;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.murdermystery.MurderMysteryGameType;
import net.swofty.commons.ServerType;
import net.swofty.commons.ServiceType;
import net.swofty.commons.protocol.objects.orchestrator.GetMapsProtocol;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.lobby.LobbyOrchestratorConnector;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.utility.GameCountCache;
import net.swofty.type.generic.party.PartyManager;
import net.swofty.commons.party.FullParty;

import java.util.ArrayList;
import java.util.List;

public class GUIMapSelection extends HypixelInventoryGUI {

    private final MurderMysteryGameType gameType;
    private List<String> maps = new ArrayList<>();
    private boolean mapsLoaded = false;

    public GUIMapSelection(MurderMysteryGameType gameType) {
        super(Text.of("Map Selection - {}", gameType.getDisplayName()), InventoryType.CHEST_4_ROW);
        this.gameType = gameType;
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        HypixelPlayer player = e.player();

        if (!mapsLoaded) {
            // Show loading message
            set(new GUIClickableItem(13) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer player) {
                    return ItemStacks.item(Material.CLOCK, """
                            <e>Loading maps...
                            <7>Please wait while we fetch
                            <7>available maps for {}""", gameType.getDisplayName());
                }

                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                    // No action while loading
                }
            });

            loadMaps(player);
        } else {
            populateMaps(player);
        }

        updateItemStacks(getInventory(), getPlayer());
    }

    private void loadMaps(HypixelPlayer player) {
        ProxyService orchestratorService = new ProxyService(ServiceType.ORCHESTRATOR);

        GetMapsProtocol.GetMapsMessage message =
                new GetMapsProtocol.GetMapsMessage(ServerType.MURDER_MYSTERY_GAME, gameType.toString());

        orchestratorService.handleRequest(message)
                .thenAccept(response -> {
                    if (response instanceof GetMapsProtocol.GetMapsResponse mapsResponse) {
                        maps = mapsResponse.maps();
                        mapsLoaded = true;

                        // Refresh the GUI with the loaded maps
                        populateMaps(player);
                        updateItemStacks(getInventory(), player);
                    }
                })
                .exceptionally(throwable -> {
                    throwable.printStackTrace();
                    player.sendMessage("<c>Failed to load maps: {}", throwable.getMessage());
                    player.closeInventory();
                    return null;
                });
    }

    private void populateMaps(HypixelPlayer player) {
        if (maps.isEmpty()) {
            set(new GUIClickableItem(13) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer player) {
                    return ItemStacks.item(Material.BARRIER, """
                            <c>No maps available
                            <7>No maps are currently available
                            <7>for {}

                            <e>Click to go back""", gameType.getDisplayName());
                }

                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                    new GUIPlay(gameType).open(player);
                }
            });
            return;
        }

        // Add back button
        set(new GUIClickableItem(31) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.ARROW, """
                        <c>Back
                        <7>Go back to game selection""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIPlay(gameType).open(player);
            }
        });

        // Add map options
        int slot = 10;
        for (String map : maps) {
            if (slot > 25) break; // Max capacity reached

            final String mapName = map;

            set(new GUIClickableItem(slot) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer player) {
                    int gameCount = GameCountCache.getGameCount(
                            ServerType.MURDER_MYSTERY_GAME,
                            gameType.toString(),
                            mapName
                    );
                    return ItemStacks.item(Material.PAPER, """
                            <a>{}
                            <7>{}

                            <7>Available Games: <a>{}

                            <e>Click to Play!""", mapName, gameType.getDisplayName(), gameCount);
                }

                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                    player.closeInventory();

                    if (LobbyOrchestratorConnector.isSearching(player.getUuid())) {
                        player.sendMessage("<c>You are already searching for a game!");
                        return;
                    }

                    // Party check - non-leaders cannot queue
                    if (PartyManager.isInParty(player)) {
                        FullParty party = PartyManager.getPartyFromPlayer(player);
                        if (party != null && !party.getLeader().getUuid().equals(player.getUuid())) {
                            player.sendMessage("<c>You are in a party! Ask your leader to start the game, or /p leave");
                            return;
                        }
                    }

                    LobbyOrchestratorConnector connector = new LobbyOrchestratorConnector(player);
                    connector.sendToGame(ServerType.MURDER_MYSTERY_GAME, gameType.toString(), mapName);
                }
            });

            if (slot > 16) slot = 18; // Move to the next row
            slot++;
        }
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        // No-op
    }
}
