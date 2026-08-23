package net.swofty.type.hub.gui;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.ServerType;
import net.swofty.commons.UnderstandableProxyServer;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyInformation;
import net.swofty.proxyapi.ProxyPlayer;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.gui.inventory.HypixelPaginatedGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.RefreshingGUI;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.utility.PaginationList;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class GUIHubSelector extends HypixelPaginatedGUI<UnderstandableProxyServer> implements RefreshingGUI {
    private final ProxyInformation information;
    private List<UnderstandableProxyServer> servers;
    private int counter = 0;
    private boolean sending = false;

    public GUIHubSelector() {
        super(InventoryType.CHEST_6_ROW);

        information = new ProxyInformation();
    }

    @Override
    protected int[] getPaginatedSlots() {
        return new int[]{
                10, 11, 12, 13, 14, 15, 16,
                19, 20, 21, 22, 23, 24, 25,
                28, 29, 30, 31, 32, 33, 34,
                37, 38, 39, 40, 41, 42, 43
        };
    }

    @Override
    protected PaginationList<UnderstandableProxyServer> fillPaged(HypixelPlayer player, PaginationList<UnderstandableProxyServer> paged) {
        fill(ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE));
        servers = information.getServerInformation(ServerType.SKYBLOCK_HUB).join();
        paged.addAll(servers);

        set(GUIClickableItem.getCloseItem(49));
        set(new GUIClickableItem(50) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;

                if (sending) {
                    player.sendMessage("<c>We are currently trying to queue you into another server!");
                    return;
                }

                // Exclude current server
                List<UnderstandableProxyServer> serversToUse = servers.stream()
                        .filter(server -> !server.name().equals(HypixelConst.getServerName()))
                        .toList();

                if (serversToUse.isEmpty()) {
                    player.sendMessage("<c>No servers with enough players found!");
                    return;
                }

                sending = true;

                if (e.getClick() instanceof Click.Right) {
                    UnderstandableProxyServer smallestServer = serversToUse.stream()
                            .min(Comparator.comparingInt((UnderstandableProxyServer server) -> server.players().size()))
                           .orElseThrow();
                    ProxyPlayer proxyPlayer = new ProxyPlayer(player.getUuid());
                    proxyPlayer.sendMessage("<7>Request join for Hub {}...", smallestServer.name());
                    proxyPlayer.transferToWithIndication(smallestServer.uuid())
                            .orTimeout(3, TimeUnit.SECONDS)
                            .exceptionally(throwable -> {
                                if (throwable instanceof TimeoutException) {
                                    player.sendMessage("<c>Your transfer failed! The server took too long to respond.");
                                } else {
                                    player.sendMessage("<c>Your transfer failed! An error occurred.");
                                }
                                sending = false;
                                return null; // Return value for the CompletableFuture
                            });
                    return;
                }

                int randomIndex = (int) (Math.random() * serversToUse.size());
                UnderstandableProxyServer randomServer = serversToUse.get(randomIndex);
                ProxyPlayer proxyPlayer = new ProxyPlayer(player.getUuid());
                proxyPlayer.sendMessage("<7>Request join for Hub {}...", randomServer.name());
                proxyPlayer.transferToWithIndication(randomServer.uuid())
                        .orTimeout(3, TimeUnit.SECONDS)
                        .exceptionally(throwable -> {
                            if (throwable instanceof TimeoutException) {
                                player.sendMessage("<c>Your transfer failed! The server took too long to respond.");
                            } else {
                                player.sendMessage("<c>Your transfer failed! An error occurred.");
                            }
                            sending = false;
                            return null; // Return value for the CompletableFuture
                        });
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.COMPASS, 1, """
                        <a>Random Hub
                        <7>Have no strong feelings one way
                        <7>or the other?
                        <r>\s
                        <7>Hub Servers: <a>{}
                        <7>Current: <3>{} <7>({}/{})
                        <r>\s
                        <b>Right-Click for a small server!
                        <e>Click to join a random hub!""",
                        servers.size(), HypixelConst.getServerName(),
                        SkyBlockGenericLoader.getLoadedPlayers().size(), HypixelConst.getMaxPlayers());
            }
        });

        return paged;
    }

    @Override
    protected boolean shouldFilterFromSearch(String query, UnderstandableProxyServer server) {
        return false;
    }

    @Override
    protected void performSearch(HypixelPlayer player, String query, int page, int maxPage) {
    }

    @Override
    protected String getTitle(HypixelPlayer player, String query, int page, PaginationList<UnderstandableProxyServer> paged) {
        return "SkyBlock Hub Selector";
    }

    @Override
    protected GUIClickableItem createItemFor(UnderstandableProxyServer server, int slot, HypixelPlayer player) {
        boolean isThisServer = server.port() == HypixelConst.getPort();
        boolean isFull = server.players().size() >= server.maxPlayers();

        return new GUIClickableItem(slot) {
            private int counterAtThisMoment;

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                counter++;
                counterAtThisMoment = counter;
                return ItemStacks.item(
                        (isThisServer ? Material.RED_CONCRETE : Material.QUARTZ_BLOCK), 1,
                        Text.of((isThisServer ? "<c>" : "<a>") + "SkyBlock Hub #{}", counter),
                        List.of(
                                Text.of("<7>Players: {}/{}", server.players().size(), server.maxPlayers()),
                                Text.of("<8>Server: {}", server.name()),
                                Text.literal(" "),
                                Text.of(isThisServer ? "<c>Already connected!" :
                                        isFull ? "<c>Full!" : "<e>Click to connect!")
                        )
                );
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                if (isThisServer) {
                    player.sendMessage("<c>You are already on this server!");
                    player.closeInventory();
                    return;
                }

                if (isFull) {
                    player.sendMessage("<c>You cannot join this server because it is full!");
                    return;
                }

                if (sending) {
                    player.sendMessage("<c>We are currently trying to queue you into another server!");
                    return;
                }

                sending = true;
                ProxyPlayer proxyPlayer = new ProxyPlayer(player.getUuid());
                proxyPlayer.sendMessage("<7>Request join for Hub #{} ({})...", counterAtThisMoment, server.name());
                proxyPlayer.transferToWithIndication(server.uuid())
                        .orTimeout(3, TimeUnit.SECONDS)
                        .exceptionally(throwable -> {
                            if (throwable instanceof TimeoutException) {
                                player.sendMessage("<c>Your transfer failed! The server took too long to respond.");
                            } else {
                                player.sendMessage("<c>Your transfer failed! An error occurred.");
                            }
                            sending = false;
                            return null; // Return value for the CompletableFuture
                        });
            }
        };
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        e.setCancelled(true);
    }

    @Override
    public void refreshItems(HypixelPlayer player) {
        servers = information.getServerInformation(ServerType.SKYBLOCK_HUB).join();
        PaginationList<UnderstandableProxyServer> paged = fillPaged(player, new PaginationList<>(getPaginatedSlots().length));
        int page = 1;
        counter = 0;

        updatePagedItems(paged, page, player);
    }

    @Override
    public int refreshRate() {
        return 20;
    }
}
