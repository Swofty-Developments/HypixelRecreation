package net.swofty.type.skyblockgeneric.item.components;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.item.reforge.Reforge;
import net.swofty.commons.skyblock.item.reforge.ReforgeLoader;
import net.swofty.type.skyblockgeneric.item.SkyBlockItemComponent;
import net.swofty.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineHandler;
import net.swofty.type.skyblockgeneric.item.handlers.anvilcombine.AnvilCombineRegistry;

import java.util.Map;

/**
 * Makes a reforge stone applicable in the regular Anvil.
 */
public class ReforgeStoneComponent extends SkyBlockItemComponent {
    public ReforgeStoneComponent(String reforgeId, Map<Rarity, Integer> applicationCosts) {
        String handlerId = "REFORGE_STONE:" + reforgeId;

        AnvilCombineRegistry.register(handlerId, new AnvilCombineHandler(
                (upgradeItem, sacrificeItem) ->
                        upgradeItem.getAttributeHandler().setReforge(reforgeId),
                (player, upgradeItem, sacrificeItem) -> {
                    if (!upgradeItem.hasComponent(ReforgableComponent.class)) return false;
                    Reforge reforge = ReforgeLoader.getReforge(reforgeId);
                    return reforge != null && reforge.isApplicableTo(
                            upgradeItem.getComponent(ReforgableComponent.class).getReforgeType());
                },
                (upgradeItem, sacrificeItem, player) -> 0,
                (player, upgradeItem, sacrificeItem) -> {
                    int cost = applicationCosts.getOrDefault(
                            upgradeItem.getAttributeHandler().getRarity(), 0);
                    if (player.getCoins() < cost) {
                        player.sendMessage("<c>You don't have enough coins to apply this reforge!");
                        return false;
                    }
                    player.removeCoins(cost);
                    return true;
                }
        ));

        addInheritedComponent(new AnvilCombinableComponent(handlerId));
    }
}
