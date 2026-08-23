package net.swofty.type.skyblockgeneric.item.updater;

import lombok.Getter;
import net.minestom.server.color.Color;
import net.minestom.server.component.DataComponents;
import net.minestom.server.entity.PlayerSkin;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.PotionContents;
import net.minestom.server.network.player.ResolvableProfile;
import net.minestom.server.potion.PotionType;
import net.minestom.server.tag.Tag;
import net.swofty.commons.skyblock.item.UnderstandableSkyBlockItem;
import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.commons.skyblock.item.attribute.attributes.ItemAttributeGemData;
import net.swofty.commons.skyblock.item.attribute.attributes.ItemAttributePotionData;
import net.swofty.commons.skyblock.item.attribute.attributes.ItemAttributeRarity;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skyblockgeneric.item.ItemLore;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.EnchantedComponent;
import net.swofty.type.skyblockgeneric.item.components.GemstoneComponent;
import net.swofty.type.skyblockgeneric.item.components.ItemModelComponent;
import net.swofty.type.skyblockgeneric.item.components.SkullHeadComponent;
import net.swofty.type.skyblockgeneric.item.components.TrackedUniqueComponent;
import net.swofty.type.skyblockgeneric.potion.PotionEffectType;
import org.json.JSONObject;

import java.util.Base64;
import java.util.List;
import java.util.UUID;

@Getter
public class NonPlayerItemUpdater {
    private final SkyBlockItem item;
    private final ItemStack.Builder stack;

    public NonPlayerItemUpdater(SkyBlockItem item) {
        this.item = item;
        this.stack = item.getItemStackBuilder();
    }

    public NonPlayerItemUpdater(UnderstandableSkyBlockItem item) {
        this.item = new SkyBlockItem(item);
        this.stack = this.item.getItemStackBuilder();
    }

    public NonPlayerItemUpdater(ItemStack item) {
        this.item = new SkyBlockItem(item);
        this.stack = ItemStacks.copy(item);
    }

    public NonPlayerItemUpdater(ItemStack.Builder item) {
        this(item.build());
    }

    public ItemStack.Builder getUpdatedItem() {
        if (stack.build().hasTag(Tag.Boolean("uneditable")) && stack.build().getTag(Tag.Boolean("uneditable"))) {
            return stack;
        }

        // Check for potion material override (splash/lingering)
        Material baseMaterial = item.getMaterial();
        ItemAttributePotionData.PotionData potionData = item.getAttributeHandler().getPotionData();
        if (potionData != null && isPotionMaterial(baseMaterial)) {
            baseMaterial = getPotionMaterial(potionData, baseMaterial);
        }

        ItemStack.Builder builder = ItemStack.builder(baseMaterial).amount(item.getAmount());
        // Copy tags from original builder
        for (ItemAttribute attribute : ItemAttribute.getPossibleAttributes()) {
            builder = builder.set(Tag.String(attribute.getKey()),
                    item.getAttribute(attribute.getKey()).saveIntoString());
        }
        builder = ItemStacks.clearAttributes(builder);

        ItemStack.Builder stack = updateItemLore(builder);

        if (item.hasComponent(EnchantedComponent.class))
            stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);

        if (item.hasComponent(TrackedUniqueComponent.class))
            stack.setTag(Tag.UUID("unique-tracked-id"), UUID.randomUUID());

        if (item.hasComponent(SkullHeadComponent.class)) {
            SkullHeadComponent component = item.getComponent(SkullHeadComponent.class);
            JSONObject json = new JSONObject();
            json.put("isPublic", true);
            json.put("signatureRequired", false);
            json.put("textures", new JSONObject().put("SKIN", new JSONObject()
                .put("url", "https://textures.minecraft.net/texture/" + component.getSkullTexture(item))
                    .put("metadata", new JSONObject().put("model", "slim"))));

            String texturesEncoded = Base64.getEncoder().encodeToString(json.toString().getBytes());

            stack.set(DataComponents.PROFILE, new ResolvableProfile(new PlayerSkin(texturesEncoded, null)));
        }

        if (item.hasComponent(ItemModelComponent.class)) {
            stack.set(DataComponents.ITEM_MODEL, item.getComponent(ItemModelComponent.class).getItemModel());
        }

        ItemAttributeRarity rarityAttribute = (ItemAttributeRarity) item.getAttribute("rarity");
        stack.set(DataComponents.TOOLTIP_STYLE, rarityAttribute.value.getTooltipStyle());

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

        Color leatherColour = item.getAttributeHandler().getLeatherColour();
        if (leatherColour != null) {
            stack.set(DataComponents.DYED_COLOR, leatherColour);
            stack = ItemStacks.clearAttributes(stack);
        }

        // Apply potion contents for proper color display
        if (potionData != null && isPotionMaterial(baseMaterial)) {
            stack.set(DataComponents.POTION_CONTENTS, createPotionContents(potionData));
        }

        for (ItemAttribute attribute : ItemAttribute.getPossibleAttributes()) {
            stack = stack.set(Tag.String(attribute.getKey()),
                    item.getAttribute(attribute.getKey()).saveIntoString());
        }

        ItemStacks.clearAttributes(stack);
        return stack;
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

    private static ItemStack.Builder updateItemLore(ItemStack.Builder stack) {
        ItemLore lore = new ItemLore(stack.build());
        lore.updateLore(null);

        return stack.set(DataComponents.LORE, lore.getStack().get(DataComponents.LORE))
                .set(DataComponents.CUSTOM_NAME, lore.getStack().get(DataComponents.CUSTOM_NAME));
    }
}
