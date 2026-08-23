package net.swofty.type.skyblockgeneric.gui.inventories.fishing;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.fishing.item.FishingItemSupport;
import net.swofty.type.skyblockgeneric.fishing.ship.FishingShipService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class GUIFishingShip extends HypixelInventoryGUI {

    public GUIFishingShip() {
        super("\\{Fishing Ship}", InventoryType.CHEST_5_ROW);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        fill(FILLER_ITEM);
        set(new GUIItem(4) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                SkyBlockPlayer skyBlockPlayer = (SkyBlockPlayer) player;
                var state = FishingShipService.getState(skyBlockPlayer);
                return ItemStacks.item(Material.OAK_BOAT, """
                        <6>\\{Fishing Ship}
                        <7>Your <6>Ship</6> will help you travel to
                        <7>different <9>fishing islands</9> in SkyBlock.

                        <7>For now, it can only get you to the
                        <2>Backwater Bayou<7>.

                        <7>Helm: <f>{}
                        <7>Engine: <f>{}
                        <7>Hull: <f>{}""",
                    resolvePartName(state.getHelm(), "Cracked Ship Helm"),
                    resolvePartName(state.getEngine(), "Missing Engine"),
                    resolvePartName(state.getHull(), "Rusty Ship Hull"));
            }
        });
        set(new GUIItem(21) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                SkyBlockPlayer skyBlockPlayer = (SkyBlockPlayer) player;
                return buildShipPartStack(
                    FishingShipService.getState(skyBlockPlayer).getHelm(),
                    "Cracked Ship Helm",
                    "d8d4a54d1fcf47b2efc99ba4cc772250aee5c2f26ed1a19052213e0f3323ca1d",
                    """
                        <7>A cracked ship helm, incapable of
                        <7>changing its heading which appears
                        <7>due east.""");
            }
        });
        set(new GUIItem(22) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                SkyBlockPlayer skyBlockPlayer = (SkyBlockPlayer) player;
                return buildShipPartStack(
                    FishingShipService.getState(skyBlockPlayer).getEngine(),
                    "Missing Engine",
                    "53e84793917c890f7f8a2c4078a29e8ba939790498727af9342c2b6f6ac43c9c",
                    """
                        <7>This ship still needs an engine before
                        <7>it can get you anywhere.""");
            }
        });
        set(new GUIItem(23) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                SkyBlockPlayer skyBlockPlayer = (SkyBlockPlayer) player;
                return buildShipPartStack(
                    FishingShipService.getState(skyBlockPlayer).getHull(),
                    "Rusty Ship Hull",
                    "f42d53ca6e7d80a99a699c2036dcf6e233394feb9f46fb2ff9d9a819690894a9",
                    """
                        <7>A hull rusted and dilapidated beyond
                        <7>repair. It's a miracle the ship
                        <7>remains afloat.""");
            }
        });
        set(new GUIClickableItem(31) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                SkyBlockPlayer skyBlockPlayer = (SkyBlockPlayer) player;
                boolean unlocked = FishingShipService.getState(skyBlockPlayer).hasDestination("BACKWATER_BAYOU");
                return ItemStacks.item(Material.COMPASS, """
                        {}
                        <7>Choose where to set sail next.

                        {}""",
                    unlocked ? Text.of("<a>Open Navigator") : Text.of("<c>Navigator Locked"),
                    unlocked ? Text.of("<e>Click to browse destinations!") : Text.of("<c>Install an engine first."));
            }

            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer player) {
                SkyBlockPlayer skyBlockPlayer = (SkyBlockPlayer) player;
                if (!FishingShipService.getState(skyBlockPlayer).hasDestination("BACKWATER_BAYOU")) {
                    player.sendMessage("<c>Your ship cannot travel anywhere yet.");
                    return;
                }
                skyBlockPlayer.openView(new GUINavigator());
            }
        });
        set(new GUIItem(44) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer player) {
                SkyBlockPlayer skyBlockPlayer = (SkyBlockPlayer) player;
                return ItemStacks.item(Material.NAME_TAG, """
                        <a>Rename Ship
                        <7>You may be going on long voyages
                        <7>with your <6>Ship<7>, best to give it a name!

                        <7>Current Name: <6>{}

                        <7>Renaming is not implemented yet.""",
                    FishingShipService.getState(skyBlockPlayer).getShipName());
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

    private static String resolvePartName(String itemId, String fallback) {
        var definition = FishingItemSupport.getShipPart(itemId);
        return definition == null ? fallback : definition.getDisplayName();
    }

    private static ItemStack.Builder buildShipPartStack(String itemId, String fallbackName, String fallbackTexture,
                                                        String fallbackLore) {
        var definition = FishingItemSupport.getShipPart(itemId);
        String name = definition == null ? fallbackName : definition.getDisplayName();
        String texture = definition == null || definition.getTexture() == null ? fallbackTexture : definition.getTexture();
        return ItemStacks.head(texture, Text.of("<f>{}", name), Text.of(fallbackLore).lines());
    }
}
