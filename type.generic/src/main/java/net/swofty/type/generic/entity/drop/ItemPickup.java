package net.swofty.type.generic.entity.drop;

import net.kyori.adventure.sound.Sound;
import net.minestom.server.collision.BoundingBox;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.inventory.TransactionOption;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.CollectItemPacket;
import net.minestom.server.sound.SoundEvent;

public final class ItemPickup {

    private ItemPickup() {
    }

    public static boolean isWithinRange(Player player, Entity item) {
        final BoundingBox playerBox = player.getBoundingBox();
        final BoundingBox itemBox = item.getBoundingBox();
        return ItemDropPhysics.withinPickupReach(
                player.getPosition(), playerBox.width(), playerBox.height(),
                item.getPosition(), itemBox.width(), itemBox.height());
    }

    public static int pickup(Player player, VanillaItemEntity item) {
        if (item.isRemoved() || !item.isPickable()) return 0;

        final ItemStack stack = item.getItemStack();
        if (stack.isAir()) return 0;

        final ItemStack remaining = player.getInventory().addItemStack(stack, TransactionOption.ALL);
        final int taken = stack.amount() - (remaining.isAir() ? 0 : remaining.amount());
        if (taken <= 0) return 0;

        sendCollectPacket(player, item, taken);
        playPickupSound(player);

        if (remaining.isAir()) {
            item.remove();
        } else {
            item.setItemStack(remaining);
        }
        return taken;
    }

    public static void sendCollectPacket(Player player, Entity item, int amount) {
        final CollectItemPacket packet = new CollectItemPacket(item.getEntityId(), player.getEntityId(), amount);
        item.sendPacketToViewers(packet);
        if (!item.getViewers().contains(player)) player.sendPacket(packet);
    }

    public static void playPickupSound(Player player) {
        playSound(player, SoundEvent.ENTITY_ITEM_PICKUP);
    }

    public static void playSound(Player player, SoundEvent soundEvent) {
        player.playSound(Sound.sound(soundEvent, Sound.Source.PLAYER,
                ItemDropPhysics.PICKUP_SOUND_VOLUME, ItemDropPhysics.pickupSoundPitch()));
    }
}
