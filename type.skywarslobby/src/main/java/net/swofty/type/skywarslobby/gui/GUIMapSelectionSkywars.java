package net.swofty.type.skywarslobby.gui;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.ServerType;
import net.swofty.commons.ServiceType;
import net.swofty.commons.protocol.objects.orchestrator.GetMapsProtocol;
import net.swofty.commons.skywars.SkywarsGameType;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.lobby.GameQueueValidator;
import net.swofty.type.lobby.LobbyOrchestratorConnector;

import java.util.ArrayList;
import java.util.List;

public class GUIMapSelectionSkywars extends HypixelInventoryGUI {
    private final SkywarsGameType gameType;
    private List<String> maps = new ArrayList<>();
    private boolean mapsLoaded = false;

    public GUIMapSelectionSkywars(SkywarsGameType gameType) {
        super(Text.of("Map Selection - {}", gameType.getDisplayName()), InventoryType.CHEST_4_ROW);
        this.gameType = gameType;
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        HypixelPlayer player = e.player();

        if (!mapsLoaded) {
            set(new GUIClickableItem(13) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer player) {
                    return ItemStackCreator.item(Material.CLOCK, 1, """
                            <e>Loading maps...
                            <7>Please wait while we fetch
                            <7>available maps for {}""", gameType.getDisplayName());
                }

                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer player) {
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
                new GetMapsProtocol.GetMapsMessage(ServerType.SKYWARS_GAME, gameType.name());

        orchestratorService.handleRequest(message)
                .thenAccept(response -> {
                    if (response instanceof GetMapsProtocol.GetMapsResponse mapsResponse) {
                        maps = mapsResponse.maps();
                        mapsLoaded = true;
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
                    return ItemStackCreator.item(Material.BARRIER, 1, """
                            <c>No maps available
                            <7>No maps are currently available
                            <7>for {}

                            <e>Click to go back""", gameType.getDisplayName());
                }

                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                    new GUIPlaySkywars(gameType, false).open(player);
                }
            });
            return;
        }

        // Back button
        set(new GUIClickableItem(31) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStackCreator.item(Material.ARROW, 1, """
                        <c>Back
                        <7>Go back to game selection""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIPlaySkywars(gameType, false).open(player);
            }
        });

        // Add map options
        int slot = 10;
        for (String map : maps) {
            if (slot > 25) break;

            final String mapName = map;

            set(new GUIClickableItem(slot) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer player) {
                    return ItemStackCreator.item(Material.FIREWORK_STAR, 1, """
                            <a>{}
                            <7>{}

                            <7>Available Games: <a>Unknown

                            <a>Click to Play""", mapName, gameType.getDisplayName());
                }

                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                    player.closeInventory();

                    if (!GameQueueValidator.canPlayerQueue(player, new GameQueueValidator.QueueRequirements(
                        "SkyWars",
                        gameType.getDisplayName(),
                        gameType.getTeamSize()
                    ))) {
                        return;
                    }

                    LobbyOrchestratorConnector connector = new LobbyOrchestratorConnector(player);
                    connector.sendToGame(ServerType.SKYWARS_GAME, gameType.name(), mapName);
                }
            });

            slot++;
            if (slot == 17) slot = 19; // Skip to next row
        }
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
    }
}
