package net.swofty.type.murdermysterylobby.gui;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.ServerType;
import net.swofty.commons.murdermystery.MurderMysteryGameType;
import net.swofty.commons.party.FullParty;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.party.PartyManager;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.utility.GameCountCache;
import net.swofty.type.lobby.LobbyOrchestratorConnector;

public class GUIPlayMurderMystery extends HypixelInventoryGUI {

    public GUIPlayMurderMystery() {
        super("Play Murder Mystery", InventoryType.CHEST_5_ROW);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        set(new GUIClickableItem(12) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                int playersInGame = GameCountCache.getPlayerCount(
                        ServerType.MURDER_MYSTERY_GAME,
                        MurderMysteryGameType.CLASSIC.name()
                );

                return ItemStacks.item(Material.CLOCK, """
                                <a>Murder Mystery (Classic)
                                <8>Solo
                                <8>{} Total Players

                                <7>The Classic Murder Mystery
                                <7>experience - take on the role of
                                <c>Murderer<7>, <9>Detective <7>or <a>Innocent<7>. The
                                <7>Murderer must try and kill without
                                <7>getting caught, while the others must
                                <7>try to figure out who they are!

                                <c>1 Murderer
                                <9>1 Detective
                                <a>10 Innocents

                                <a>{:,} currently playing!

                                <e>Click to play!""",
                        MurderMysteryGameType.CLASSIC.getMaxPlayers(), playersInGame);
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
                connector.sendToGame(ServerType.MURDER_MYSTERY_GAME, MurderMysteryGameType.CLASSIC.toString());
            }
        });
        set(new GUIClickableItem(14) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                int playersInGame = GameCountCache.getPlayerCount(
                        ServerType.MURDER_MYSTERY_GAME,
                        MurderMysteryGameType.DOUBLE_UP.name()
                );

                return ItemStacks.item(Material.CLOCK, """
                                <a>Murder Mystery (Double Up!)
                                <8>Teams
                                <8>{} Total Players

                                <7>The Classic Murder Mystery
                                <7>experience - take on the role of
                                <c>Murderer<7>, <9>Detective <7>or <a>Innocent<7>. The
                                <7>Murderers must try and kill without
                                <7>getting caught, while the others must
                                <7>try to figure out who they are!

                                <c>2 Murderers
                                <9>2 Detectives
                                <a>12 Innocents

                                <a>{:,} currently playing!

                                <e>Click to play!""",
                        MurderMysteryGameType.DOUBLE_UP.getMaxPlayers(), playersInGame);
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
                connector.sendToGame(ServerType.MURDER_MYSTERY_GAME, MurderMysteryGameType.DOUBLE_UP.toString());
            }
        });
        set(new GUIClickableItem(21) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.OAK_SIGN, """
                        <a>Map Selector (Classic)
                        <7>Pick which map you want to play from
                        <7>a list of available games.

                        <e>Click to browse!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIMapSelection(MurderMysteryGameType.CLASSIC).open(player);
            }
        });
        set(new GUIClickableItem(23) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.OAK_SIGN, """
                        <a>Map Selector (Double Up!)
                        <7>Pick which map you want to play from
                        <7>a list of available games.

                        <e>Click to browse!""");
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIMapSelection(MurderMysteryGameType.DOUBLE_UP).open(player);
            }
        });
        set(GUIClickableItem.getCloseItem(40));
        updateItemStacks(getInventory(), getPlayer());
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
        e.setCancelled(true);
    }
}
