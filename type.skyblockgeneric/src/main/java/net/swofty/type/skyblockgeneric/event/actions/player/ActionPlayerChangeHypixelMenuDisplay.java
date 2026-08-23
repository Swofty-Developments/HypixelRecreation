package net.swofty.type.skyblockgeneric.event.actions.player;

import net.minestom.server.component.DataComponents;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skyblockgeneric.collection.CustomCollectionAward;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointQuiver;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.ArrowComponent;
import net.swofty.type.skyblockgeneric.item.components.QuiverDisplayComponent;
import net.swofty.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class ActionPlayerChangeHypixelMenuDisplay implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerChangeHeldSlotEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        runCheck(player);
    }

    public static void runCheck(SkyBlockPlayer player) {
        SkyBlockItem switchedTo = new SkyBlockItem(player.getItemInMainHand());
        if (switchedTo.isNA() || switchedTo.toConfigurableItem() == null) {
            setMainMenu(player);
            return;
        }

        // Check if item shows quiver
        if (switchedTo.hasComponent(QuiverDisplayComponent.class)) {
            DatapointQuiver.PlayerQuiver quiver = player.getQuiver();
            QuiverDisplayComponent quiverDisplay = switchedTo.getComponent(QuiverDisplayComponent.class);

            // If the bow should not be drawn back then also replace all arrows in inventory with a feather
            if (!quiverDisplay.isShouldBeArrow()) {
                for (int index = 0; index < player.getInventory().getSize(); index++) {
                    SkyBlockItem item = new SkyBlockItem(player.getInventory().getItemStack(index));
                    if (item.hasComponent(ArrowComponent.class)) {
                        player.getInventory().setItemStack(index, ItemStacks.name(ItemStack.builder(Material.FEATHER)
                                .set(DataComponents.CUSTOM_DATA, item.getItemStack().get(DataComponents.CUSTOM_DATA)),
                                "<c>Switch your held item for this item!")
                                .amount(item.getAmount()).build());
                    }
                }
            } else {
                setMainMenu(player);
            }

            if (!player.hasCustomCollectionAward(CustomCollectionAward.QUIVER)) return;

            ItemStack.Builder builder;
            if (player.getQuiver().isEmpty()) {
                builder = ItemStacks.item(Material.FEATHER, 1, """
                        <8>Empty Quiver
                        <7>This item is in your inventory
                        <7>because you are currently holding a
                        <7>Bow

                        <c>Quiver is empty

                        <7>Switch away from your Bow to see
                        <7>the item that was here before.""");
            } else {
                SkyBlockItem item = quiver.getFirstItemInQuiver();
                int arrowCount = quiver.getAmountOfArrows(item.getAttributeHandler().getPotentialType());
                builder = ItemStacks.item(quiverDisplay.isShouldBeArrow() ? Material.ARROW : Material.FEATHER,
                        Math.min(64, arrowCount), """
                        <8>Quiver {}
                        <7>This item is in your inventory
                        <7>because you are currently holding a
                        <7>Bow

                        <7>Active Arrow: {} <7>(<e>{}<7>)

                        <7>Switch away from your Bow to see
                        <7>the item that was here before.""",
                        StringUtility.stripColor(item.getDisplayName()),
                        item.getDisplayName(),
                        arrowCount);
            }

            player.getInventory().setItemStack(8, ItemStacks.notEditable(builder).build());
            return;
        }

        setMainMenu(player);
    }

    public static void setMainMenu(SkyBlockPlayer player) {
        for (int index = 0; index < player.getInventory().getSize(); index++) {
            SkyBlockItem item = new SkyBlockItem(player.getInventory().getItemStack(index));
            if (item.hasComponent(ArrowComponent.class)) {
                player.getInventory().setItemStack(index,
                        PlayerItemUpdater.playerUpdate(player, item.getItemStack())
                                .build());
            }
        }

        player.getInventory().setItemStack(8,
                new NonPlayerItemUpdater(new SkyBlockItem(ItemType.SKYBLOCK_MENU).getItemStack())
                        .getUpdatedItem().build());
    }
}
