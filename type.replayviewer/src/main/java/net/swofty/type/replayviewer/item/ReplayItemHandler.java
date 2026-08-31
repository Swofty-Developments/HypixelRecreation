package net.swofty.type.replayviewer.item;

import net.minestom.server.component.DataComponents;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.item.PlayerFinishItemUseEvent;
import net.minestom.server.event.player.PlayerHandAnimationEvent;
import net.minestom.server.event.player.PlayerUseItemEvent;
import net.minestom.server.event.player.PlayerUseItemOnBlockEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.component.CustomData;
import net.minestom.server.tag.Tag;
import net.swofty.type.replayviewer.item.impl.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ReplayItemHandler {
    private final List<ReplayItem> items = new ArrayList<>(
        List.of(
            new PlaybackControlItem(),
            new TeleporterItem(),
            new ForwardItem(),
            new SlowerItem(),
            new MoreItem(),
            new BackwardItem(),
            new FasterItem()
        )
    );

    public void add(ReplayItem item) {
        items.add(item);
    }

    public Optional<ReplayItem> getItem(String name) {
        return items.stream()
            .filter(p -> p.getId().equalsIgnoreCase(name))
            .findFirst();
    }

    public void onItemFinishUse(PlayerFinishItemUseEvent event) {
        for (ReplayItem item : items) {
            ItemStack itemStack = event.getItemStack();
            if (isItem(item, itemStack)) {
                item.onItemFinishUse(event);
            }
        }
    }

    public void onItemUseOnBlock(PlayerUseItemOnBlockEvent event) {
        for (ReplayItem item : items) {
            ItemStack itemStack = event.getItemStack();
            if (isItem(item, itemStack)) {
                item.onItemUseOnBlock(event);
                item.onItemInteract(event);
            }
        }
    }

    public void onHandAnimation(PlayerHandAnimationEvent event) {
        for (ReplayItem item : items) {
            ItemStack itemStack = event.getPlayer().getItemInHand(event.getHand());
            if (isItem(item, itemStack)) {
                item.onHandAnimation(event);
            }
        }
    }

    public void onItemUse(PlayerUseItemEvent event) {
        for (ReplayItem item : items) {
            ItemStack itemStack = event.getItemStack();
            if (isItem(item, itemStack)) {
                item.onItemUse(event);
                item.onItemInteract(event);
            }
        }
    }

    public void onItemDrop(ItemDropEvent event) {
        for (ReplayItem item : items) {
            ItemStack itemStack = event.getItemStack();
            if (isItem(item, itemStack)) {
                item.onItemDrop(event);
            }
        }
    }

    public boolean onInventoryClick(InventoryPreClickEvent event) {
        for (ReplayItem item : items) {
            if (isItem(item, event.getClickedItem())) {
                item.onInventoryClick(event);
                return true;
            }
        }
        return false;
    }

    private boolean isItem(ReplayItem item, ItemStack itemStack) {
        CustomData data = itemStack.get(DataComponents.CUSTOM_DATA);
        if (data == null) return false;
        String id = data.getTag(Tag.String("item"));
        return id != null && id.equalsIgnoreCase(item.getId());
    }
}
