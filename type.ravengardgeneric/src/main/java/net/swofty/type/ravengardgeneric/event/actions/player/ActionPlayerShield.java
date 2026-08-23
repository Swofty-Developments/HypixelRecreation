package net.swofty.type.ravengardgeneric.event.actions.player;

import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.player.PlayerChangeHeldSlotEvent;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.generic.utility.ScheduleUtility;
import net.swofty.type.ravengardgeneric.item.RavengardItemType;
import net.swofty.type.ravengardgeneric.item.attribute.RavengardItemAttributeHandler;
import net.swofty.type.ravengardgeneric.item.components.StandardItemComponent;
import net.swofty.type.ravengardgeneric.user.RavengardPlayer;

/**
 * The 0.2 shield rules: the offhand only ever holds a shield the class can use, and two handed
 * weapons refuse to share hands with one -- switching to a halberd drops the shield back into
 * the inventory, as the playtest notes describe.
 */
public class ActionPlayerShield implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onInventoryClick(InventoryPreClickEvent event) {
        if (!(event.getPlayer() instanceof RavengardPlayer player)
                || event.getInventory() != player.getInventory()) {
            return;
        }

        if (event.getClick() instanceof Click.OffhandSwap(int slot)) {
            ItemStack incoming = player.getInventory().getItemStack(slot);
            if (!incoming.isAir() && !isUsableShield(incoming, player)) {
                event.setCancelled(true);
            }
            return;
        }

        if (event.getSlot() != PlayerInventoryUtils.OFFHAND_SLOT) {
            return;
        }
        ItemStack cursor = player.getInventory().getCursorItem();
        if (!cursor.isAir() && !isUsableShield(cursor, player)) {
            event.setCancelled(true);
        }
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onHeldChange(PlayerChangeHeldSlotEvent event) {
        if (!(event.getPlayer() instanceof RavengardPlayer player)) {
            return;
        }
        ScheduleUtility.nextTick(() -> unequipShieldIfTwoHanded(player));
    }

    private static void unequipShieldIfTwoHanded(RavengardPlayer player) {
        if (!player.isOnline()) {
            return;
        }
        StandardItemComponent held = standardOf(player.getItemInMainHand());
        if (held == null || !held.isTwoHanded()) {
            return;
        }
        ItemStack shield = player.getItemInOffHand();
        if (shield.isAir()) {
            return;
        }
        player.setItemInOffHand(ItemStack.AIR);
        player.getInventory().addItemStack(shield);
    }

    private static boolean isUsableShield(ItemStack stack, RavengardPlayer player) {
        RavengardItemType type = new RavengardItemAttributeHandler(stack).getType();
        StandardItemComponent standard = type == null ? null
                : type.component(StandardItemComponent.class);
        if (standard == null || !standard.isShield() || !type.usableBy(player.getRavengardClass())) {
            return false;
        }
        StandardItemComponent held = standardOf(player.getItemInMainHand());
        return held == null || !held.isTwoHanded();
    }

    private static StandardItemComponent standardOf(ItemStack stack) {
        RavengardItemType type = new RavengardItemAttributeHandler(stack).getType();
        return type == null ? null : type.component(StandardItemComponent.class);
    }
}
