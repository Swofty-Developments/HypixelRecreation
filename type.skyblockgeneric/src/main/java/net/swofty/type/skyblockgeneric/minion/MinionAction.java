package net.swofty.type.skyblockgeneric.minion;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.kyori.adventure.key.Key;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.swofty.commons.loot.LootEntry;
import net.swofty.commons.loot.LootPool;
import net.swofty.commons.loot.LootTable;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.MinionFuelComponent;
import net.swofty.type.skyblockgeneric.item.components.MinionShippingComponent;
import net.swofty.type.skyblockgeneric.item.components.SellableComponent;
import net.swofty.type.skyblockgeneric.item.crafting.FurnaceRecipeRegistry;
import net.swofty.type.skyblockgeneric.minion.extension.MinionExtensionData;
import net.swofty.type.skyblockgeneric.minion.extension.MinionExtensions;
import net.swofty.type.skyblockgeneric.minion.extension.extensions.MinionShippingExtension;

import java.util.List;

public abstract class MinionAction {

    public abstract @NonNull List<SkyBlockItem> onAction(MinionActionEvent event, IslandMinionData.IslandMinion minion, Instance island);
    public abstract boolean checkMaterials(IslandMinionData.IslandMinion minion, Instance island);

    @Getter
    @Setter
    public static class MinionActionEvent {
        private Pos toLook;
        private Runnable action;
    }

    public static void onMinionIteration(IslandMinionData.IslandMinion islandMinion,
                                             SkyBlockMinion minion,
                                             List<SkyBlockItem> items) {
        MinionExtensionData extensionData = islandMinion.getExtensionData();
        SkyBlockItem fuel = extensionData.getFuel();
        double outputMultiplier = fuel == null ? 1.0
                : fuel.getComponent(MinionFuelComponent.class).getOutputMultiplier();

        for (SkyBlockItem item : items) {
            if (extensionData.hasMinionUpgrade(ItemType.AUTO_SMELTER)) {
                item = FurnaceRecipeRegistry.smelt(item).orElse(item);
            }
            if (outputMultiplier > 1.0) {
                item.setAmount((int) Math.floor(item.getAmount() * outputMultiplier));
            }
            boolean hasAdded = islandMinion.addItem(item);
            double sellAmount = !item.hasComponent(SellableComponent.class) ? 0 :
                    item.getComponent(SellableComponent.class).getSellValue();

            if (!hasAdded && sellAmount > 0) {
                MinionShippingExtension shippingExtension = (MinionShippingExtension)
                        extensionData.getOfType(MinionShippingExtension.class);
                if (shippingExtension.getItemTypePassedIn() == null)
                    return;

                SkyBlockItem shippingItem = new SkyBlockItem(shippingExtension.getItemTypePassedIn());
                double percentage = shippingItem.getComponent(MinionShippingComponent.class).getPercentageOfOriginalPrice();
                double sellValue = sellAmount * item.getAmount() * (percentage / 100);
                shippingExtension.addCoins(sellValue, item.getAmount());

                extensionData.setData(MinionExtensions.SHIPPING_SLOT.getSlots()[0], shippingExtension);
            }
        }

        if (extensionData.hasMinionUpgrade(ItemType.DIAMOND_SPREADING)) {
            int baseDrops = items.stream().mapToInt(SkyBlockItem::getAmount).sum();
            int diamonds = new LootTable<Void, ItemType>(Key.key("skyblock", "minion/diamond_spreading"), List.of(
                    new LootPool<>(Key.key("skyblock", "diamonds"), LootPool.Mode.INDEPENDENT, baseDrops, 0,
                            List.of(new LootEntry<>(Key.key("skyblock", "diamond"), ItemType.DIAMOND, 0.1)))
            )).roll(null).size();
            if (diamonds > 0) islandMinion.addItem(new SkyBlockItem(ItemType.DIAMOND, diamonds));
        }
    }

}
