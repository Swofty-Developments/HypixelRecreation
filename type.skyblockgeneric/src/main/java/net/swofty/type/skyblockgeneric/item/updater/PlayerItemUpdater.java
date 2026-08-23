package net.swofty.type.skyblockgeneric.item.updater;

import net.minestom.server.color.Color;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.PotionContents;
import net.minestom.server.network.player.ResolvableProfile;
import net.minestom.server.potion.PotionType;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.commons.skyblock.item.attribute.attributes.ItemAttributeGemData;
import net.swofty.commons.skyblock.item.attribute.attributes.ItemAttributePotionData;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.item.ItemAttributeHandler;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skyblockgeneric.item.ItemLore;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.GemstoneComponent;
import net.swofty.type.skyblockgeneric.item.components.ItemModelComponent;
import net.swofty.type.skyblockgeneric.item.components.SkullHeadComponent;
import net.swofty.type.skyblockgeneric.item.components.TrackedUniqueComponent;
import net.swofty.type.skyblockgeneric.potion.PotionEffectType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class PlayerItemUpdater {
    public static ItemStack.Builder playerUpdate(SkyBlockPlayer player, ItemStack stack) {
        return playerUpdateFull(player, stack, false).getValue();
    }

    public static ItemStack.Builder playerUpdate(SkyBlockPlayer player, ItemStack stack, boolean isOwnedByPlayer) {
        return playerUpdateFull(player, stack, isOwnedByPlayer).getValue();
    }

    public static Map.Entry<SkyBlockItem, ItemStack.Builder> playerUpdateFull(SkyBlockPlayer player, ItemStack stack, boolean isOwnedByPlayer) {
        if (stack.hasTag(Tag.Boolean("uneditable")) && stack.getTag(Tag.Boolean("uneditable")))
            return Map.entry(new SkyBlockItem(stack), ItemStacks.copy(stack));

        if (!SkyBlockItem.isSkyBlockItem(stack) || stack.material().equals(Material.AIR)) {
            /*
             * Item is not SkyBlock item, so we just instance it here
             */
            SkyBlockItem item = new SkyBlockItem(stack.material());
            ItemStack.Builder itemAsBuilder = item.getItemStackBuilder();

            ItemLore lore = new ItemLore(stack);
            lore.updateLore(player);
            stack = lore.getStack();

            return Map.entry(item, itemAsBuilder
                            .set(DataComponents.LORE, stack.get(DataComponents.LORE))
                            .amount(stack.amount()));
        }

        /*
         * Check for value updates
         */
        SkyBlockItem item = new SkyBlockItem(stack);
        ItemAttributeHandler handler = item.getAttributeHandler();

        // Check for potion material override (splash/lingering)
        Material baseMaterial = item.getMaterial();
        ItemAttributePotionData.PotionData potionData = handler.getPotionData();
        if (potionData != null && isPotionMaterial(baseMaterial)) {
            baseMaterial = getPotionMaterial(potionData, baseMaterial);
        }

        ItemStack.Builder toReturn = ItemStack.builder(baseMaterial).amount(stack.amount());

        /*
         * Update SkyBlock Item Instance
         */

        // Update Rarity
        ItemType type = handler.getPotentialType();
        if (type != null) {
            if(!handler.isPet()) {
                handler.setRarity(type.rarity);
            }
        } else {
            handler.setRarity(Rarity.COMMON);
        }

        /*
         * Update Lore
         */
        ItemLore lore = new ItemLore(stack);
        lore.updateLore(player);
        stack = lore.getStack();

        if (handler.shouldBeEnchanted()) {
            toReturn.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
            toReturn = ItemStacks.clearAttributes(toReturn);
        }

        Color leatherColour = handler.getLeatherColour();
        if (leatherColour != null) {
            toReturn.set(DataComponents.DYED_COLOR, new Color(leatherColour.red(), leatherColour.green(), leatherColour.blue()));
        }

        if (item.hasComponent(TrackedUniqueComponent.class) && handler.getUniqueTrackedID() == null && isOwnedByPlayer) {
            UUID randomUUID = UUID.randomUUID();

            handler.setUniqueTrackedID(randomUUID.toString(), player);
            toReturn.set(Tag.String("unique-tracked-id"), randomUUID.toString());
        } else if (item.hasComponent(TrackedUniqueComponent.class) && handler.getUniqueTrackedID() != null && isOwnedByPlayer) {
            handler.setUniqueTrackedID(handler.getUniqueTrackedID(), player);
        }

        if (item.hasComponent(SkullHeadComponent.class)) {
            SkullHeadComponent skullHeadComponent = item.getComponent(SkullHeadComponent.class);

            JSONObject json = new JSONObject();
            json.put("isPublic", true);
            json.put("signatureRequired", false);
            json.put("textures", new JSONObject().put("SKIN", new JSONObject()
                .put("url", "https://textures.minecraft.net/texture/" + skullHeadComponent.getSkullTexture(item))
                    .put("metadata", new JSONObject().put("model", "slim"))));

            String texturesEncoded = Base64.getEncoder().encodeToString(json.toString().getBytes());

            toReturn.set(DataComponents.PROFILE, new ResolvableProfile(new PlayerSkin(texturesEncoded, null)));
        }

        if (item.hasComponent(ItemModelComponent.class)) {
            toReturn.set(DataComponents.ITEM_MODEL, item.getComponent(ItemModelComponent.class).getItemModel());
        }

        Rarity rarity = handler.isRecombobulated()
                ? handler.getRarity().upgrade()
                : handler.getRarity();
        toReturn.set(DataComponents.TOOLTIP_STYLE, rarity.getTooltipStyle());

        if (item.hasComponent(GemstoneComponent.class)) {
            GemstoneComponent gemstoneComponent = item.getComponent(GemstoneComponent.class);

            ItemAttributeGemData.GemData gemData = item.getAttributeHandler().getGemData();
            for (int index = 0; index < gemstoneComponent.getSlots().size(); index++) {
                if (gemData.hasGem(index)) continue;

                GemstoneComponent.GemstoneSlot slot = gemstoneComponent.getSlots().get(index);
                gemData.putGem(new ItemAttributeGemData.GemData.GemSlots(
                        index,
                        null,
                        slot.unlockPrice() == 0 && slot.itemRequirements().isEmpty()
                ));
            }
            item.getAttributeHandler().setGemData(gemData);
        }

        for (ItemAttribute attribute : ItemAttribute.getPossibleAttributes()) {
            toReturn.set(Tag.String(attribute.getKey()),
                    item.getAttribute(attribute.getKey()).saveIntoString());
        }

        // Apply potion contents for proper color display
        if (potionData != null && isPotionMaterial(baseMaterial)) {
            toReturn.set(DataComponents.POTION_CONTENTS, createPotionContents(potionData));
        }

        ItemStacks.clearAttributes(toReturn);
        return Map.entry(item,
                toReturn.amount(stack.amount())
                        .set(DataComponents.CUSTOM_NAME, stack.get(DataComponents.CUSTOM_NAME))
                        .set(DataComponents.LORE, stack.get(DataComponents.LORE)));
    }

    public static void updateLoop(Scheduler scheduler) {
        scheduler.submitTask(() -> {
            List<CompletableFuture<Void>> futures = new ArrayList<>();

            SkyBlockGenericLoader.getLoadedPlayers().forEach(player -> {
                futures.add(CompletableFuture.runAsync(() -> {
                    PlayerItemOrigin.OriginCache cache = PlayerItemOrigin.getFromCache(player.getUuid());

                    Arrays.stream(PlayerItemOrigin.values()).forEach(origin -> {
                        if (!origin.shouldBeLooped()) return;

                        ItemStack item = origin.getStack(player);
                        if (item == null || item.isAir()) {
                            cache.put(origin, new SkyBlockItem(Material.AIR));
                            return;
                        }

                        Map.Entry<SkyBlockItem, ItemStack.Builder> builder = playerUpdateFull(player, item, true);
                        cache.put(origin, builder.getKey());
                        origin.setStack(player, builder.getValue().build());
                    });

                    PlayerItemOrigin.setCache(player.getUuid(), cache);
                }));
            });

            // Wait for all futures to complete, do it async so they're at the same time
            CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

            return TaskSchedule.tick(10);
        });
    }

    /**
     * Check if a material is a potion type
     */
    private static boolean isPotionMaterial(Material material) {
        return material == Material.POTION ||
               material == Material.SPLASH_POTION ||
               material == Material.LINGERING_POTION;
    }

    /**
     * Get the correct potion material based on potion data
     */
    private static Material getPotionMaterial(ItemAttributePotionData.PotionData potionData, Material baseMaterial) {
        if (potionData.isSplash()) {
            return Material.SPLASH_POTION;
        }
        // Could add LINGERING support here in the future
        return baseMaterial;
    }

    /**
     * Create PotionContents component from potion data
     */
    private static PotionContents createPotionContents(ItemAttributePotionData.PotionData potionData) {
        PotionEffectType effectType = PotionEffectType.fromName(potionData.getEffectType());
        if (effectType == null) {
            // Default to water if unknown
            return new PotionContents(PotionType.WATER);
        }

        // Try to get vanilla potion type first
        PotionType vanillaType = effectType.getVanillaPotionType(potionData.getLevel(), potionData.isExtended());
        if (vanillaType != null) {
            // Use vanilla type - it has built-in colors
            return new PotionContents(vanillaType);
        }

        // SkyBlock-only effect - use custom color
        return new PotionContents(null, effectType.getPotionColor(), List.of(), null);
    }
}
