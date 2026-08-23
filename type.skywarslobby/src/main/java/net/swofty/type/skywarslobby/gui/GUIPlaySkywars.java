package net.swofty.type.skywarslobby.gui;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.ServerType;
import net.swofty.commons.skywars.SkywarsGameType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.utility.GameCountCache;
import net.swofty.type.lobby.GameQueueValidator;
import net.swofty.type.lobby.LobbyOrchestratorConnector;

import java.util.ArrayList;
import java.util.List;

public class GUIPlaySkywars extends HypixelInventoryGUI {
    private final SkywarsGameType primaryType;
    private final boolean showBothSoloAndDoubles;

    public GUIPlaySkywars(SkywarsGameType primaryType, boolean showBothSoloAndDoubles) {
        super(Text.of("Play SkyWars - {}", primaryType.getDisplayName()), InventoryType.CHEST_4_ROW);
        this.primaryType = primaryType;
        this.showBothSoloAndDoubles = showBothSoloAndDoubles;
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        HypixelPlayer player = e.player();

        if (showBothSoloAndDoubles) {
            // Show both Solo and Doubles options for Normal mode
            SkywarsGameType soloType = getSoloVariant();
            SkywarsGameType doublesType = getDoublesVariant();

            // Solo button (slot 11)
            set(createPlayButton(11, soloType, player));

            // Doubles button (slot 15)
            if (doublesType != null) {
                set(createPlayButton(15, doublesType, player));
            }

            // Map selector for Solo (slot 12)
            set(createMapSelectorButton(12, soloType));

            // Map selector for Doubles (slot 16)
            if (doublesType != null) {
                set(createMapSelectorButton(16, doublesType));
            }
        } else {
            // Single mode display
            set(createPlayButton(12, primaryType, player));
            set(createMapSelectorButton(14, primaryType));
        }

        set(GUIClickableItem.getCloseItem(31));
        updateItemStacks(getInventory(), getPlayer());
    }

    private GUIClickableItem createPlayButton(int slot, SkywarsGameType type, HypixelPlayer player) {
        return new GUIClickableItem(slot) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                int playersInGame = GameCountCache.getPlayerCount(ServerType.SKYWARS_GAME, type.name());
                List<Text> loreDescription = new ArrayList<>(Text.of("<7>{}", getLoreForType(type)).lines());
                loreDescription.add(Text.of("<7>Players: <a>{}", playersInGame));
                loreDescription.add(Text.empty());
                loreDescription.add(Text.of("<e>Click to play!"));

                return ItemStacks.item(getMaterialForType(type), 1,
                        Text.of("<a>{}", type.getDisplayName()), loreDescription);
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                player.closeInventory();

                if (!GameQueueValidator.canPlayerQueue(player, new GameQueueValidator.QueueRequirements(
                    "SkyWars",
                    type.getDisplayName(),
                    type.getTeamSize()
                ))) {
                    return;
                }

                LobbyOrchestratorConnector connector = new LobbyOrchestratorConnector(player);
                connector.sendToGame(ServerType.SKYWARS_GAME, type.name());
            }
        };
    }

    private GUIClickableItem createMapSelectorButton(int slot, SkywarsGameType type) {
        return new GUIClickableItem(slot) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                return ItemStacks.item(Material.OAK_SIGN, 1, """
                        <a>Map Selector
                        <7>Pick which map you want to play from
                        <7>a list of available maps for {}.

                        <e>Click to browse!""", type.getDisplayName());
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                new GUIMapSelectionSkywars(type).open(player);
            }
        };
    }

    private SkywarsGameType getSoloVariant() {
        return switch (primaryType) {
            case SOLO_NORMAL, DOUBLES_NORMAL -> SkywarsGameType.SOLO_NORMAL;
            case SOLO_INSANE -> SkywarsGameType.SOLO_INSANE;
            case SOLO_LUCKY_BLOCK -> SkywarsGameType.SOLO_LUCKY_BLOCK;
        };
    }

    private SkywarsGameType getDoublesVariant() {
        return switch (primaryType) {
            case SOLO_NORMAL, DOUBLES_NORMAL -> SkywarsGameType.DOUBLES_NORMAL;
            default -> null; // Only Normal has doubles variant currently
        };
    }

    private String getLoreForType(SkywarsGameType type) {
        return switch (type) {
            case SOLO_NORMAL -> """
                    Classic SkyWars! Gather resources
                    from your island and center.
                    Be the last player standing!""";
            case SOLO_INSANE -> """
                    Insane mode with better loot!
                    Stronger gear, faster action.
                    Be the last player standing!""";
            case DOUBLES_NORMAL -> """
                    Team up with a partner!
                    Work together to be the
                    last team standing!""";
            case SOLO_LUCKY_BLOCK -> """
                    Break Lucky Blocks for random
                    rewards! Good luck or bad luck?
                    Be the last player standing!""";
        };
    }

    private Material getMaterialForType(SkywarsGameType type) {
        return switch (type) {
            case SOLO_NORMAL -> Material.GRASS_BLOCK;
            case SOLO_INSANE -> Material.DIAMOND_SWORD;
            case DOUBLES_NORMAL -> Material.PLAYER_HEAD;
            case SOLO_LUCKY_BLOCK -> Material.RED_STAINED_GLASS;
        };
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
    }
}
