package net.swofty.type.bedwarslobby.gui;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
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

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class GUILobbySelector extends HypixelPaginatedGUI<UnderstandableProxyServer> implements RefreshingGUI {
    private final ProxyInformation information;
    private List<UnderstandableProxyServer> servers;
    private int counter = 0;
    private boolean sending = false;

    public GUILobbySelector() {
        super(InventoryType.CHEST_2_ROW);

        information = new ProxyInformation();
    }

    @Override
    protected int[] getPaginatedSlots() {
        return new int[]{
                0, 1, 2, 3, 4, 5, 6, 7, 8,
                9, 10, 11, 12, 13, 14, 15, 16, 17
        };
    }

    @Override
    protected PaginationList<UnderstandableProxyServer> fillPaged(HypixelPlayer player, PaginationList<UnderstandableProxyServer> paged) {
        servers = information.getServerInformation(ServerType.BEDWARS_LOBBY).join();
        paged.addAll(servers);
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
        return "Bed Wars Lobby Selector";
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
                        (isThisServer ? Material.RED_CONCRETE  : Material.QUARTZ_BLOCK), 1,
                        Text.of((isThisServer ? "<c>" : "<a>") + "Bed Wars Lobby #{}", counter),
                        List.of(
                                Text.of("<7>Players: {}/{}", server.players().size(), server.maxPlayers()),
                                Text.literal(" "),
                                Text.of(isThisServer ? "<c>Already connected!" :
                                        isFull ? "<c>Full!" : "<e>Click to connect!")
                        )
                );
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                if (isThisServer) {
                    p.sendMessage("<c>You are already on this server!");
                    p.closeInventory();
                    return;
                }

                if (isFull) {
                    p.sendMessage("<c>You cannot join this server because it is full!");
                    return;
                }

                if (sending) {
                    p.sendMessage("<c>We are currently trying to queue you into another server!");
                    return;
                }

                sending = true;
                ProxyPlayer proxyPlayer = new ProxyPlayer(p.getUuid());
                proxyPlayer.sendMessage("<7>Request join for Hub #{} ({})...", counterAtThisMoment, server.name());
                proxyPlayer.transferToWithIndication(server.uuid())
                        .orTimeout(3, TimeUnit.SECONDS)
                        .exceptionally(throwable -> {
                            if (throwable instanceof TimeoutException) {
                                p.sendMessage("<c>Your transfer failed! The server took too long to respond.");
                            } else {
                                p.sendMessage("<c>Your transfer failed! An error occurred.");
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
        servers = information.getServerInformation(ServerType.BEDWARS_LOBBY).join();
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
