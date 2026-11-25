package net.swofty.type.skyblockgeneric.item;

import net.minestom.server.color.Color;
import net.swofty.commons.ServiceType;
import net.swofty.commons.protocol.objects.itemtracker.TrackedItemUpdateProtocol;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.item.attribute.attributes.*;
import net.swofty.commons.skyblock.item.reforge.Reforge;
import net.swofty.commons.skyblock.item.reforge.ReforgeLoader;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.skyblockgeneric.abiphone.AbiphoneNPC;
import net.swofty.type.skyblockgeneric.abiphone.AbiphoneRegistry;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import net.swofty.type.skyblockgeneric.item.components.*;
import net.swofty.type.skyblockgeneric.minion.MinionRegistry;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Stream;

public class ItemAttributeHandler {
    SkyBlockItem item;

    public ItemAttributeHandler(SkyBlockItem item) {
        this.item = item;
    }

    public String getTypeAsString() {
        return ((ItemAttributeType) item.getAttribute("item_type")).getValue();
    }

    public @Nullable String getAttributeShardId() {
        String value = ((ItemAttributeShardId) item.getAttribute("attribute_shard_id")).getValue();
        return value == null || value.equalsIgnoreCase("none") ? null : value;
    }

    public void setAttributeShardId(@Nullable String shardId) {
        item.getAttribute("attribute_shard_id").setValue(shardId == null ? "none" : shardId);
    }

    public boolean shouldBeEnchanted() {
        return item.hasComponent(EnchantedComponent.class);
    }

    public int getRuneLevel() {
        if (!(item.hasComponent(RuneableComponent.class))) {
            throw new RuntimeException("Item is not a rune item " + getTypeAsString());
        }
        return ((ItemAttributeRuneLevel) item.getAttribute("rune_level")).getValue();
    }

    public void setRuneLevel(int level) {
        if (!(item.hasComponent(RuneableComponent.class))) throw new RuntimeException("Item is not a rune item");
        ((ItemAttributeRuneLevel) item.getAttribute("rune_level")).setValue(level);
    }

    public ItemAttributeHotPotatoBookData.HotPotatoBookData getHotPotatoBookData() {
        return ((ItemAttributeHotPotatoBookData) item.getAttribute("hot_potato_book_data")).getValue();
    }

    public void setHotPotatoBookData(ItemAttributeHotPotatoBookData.HotPotatoBookData data) {
        item.getAttribute("hot_potato_book_data").setValue(data);
    }

    public ItemAttributeRuneInfusedWith.RuneData getRuneData() {
        return ((ItemAttributeRuneInfusedWith) item.getAttribute("rune_infused_with")).getValue();
    }

    public void setRuneData(ItemAttributeRuneInfusedWith.RuneData data) {
        ((ItemAttributeRuneInfusedWith) item.getAttribute("rune_infused_with")).setValue(data);
    }

    public @Nullable String getFishingHook() {
        String value = ((ItemAttributeFishingHook) item.getAttribute("fishing_hook")).getValue();
        return value == null || value.equalsIgnoreCase("none") ? null : value;
    }

    public void setFishingHook(@Nullable String hookId) {
        item.getAttribute("fishing_hook").setValue(hookId == null ? "none" : hookId);
    }

    public @Nullable String getFishingLine() {
        String value = ((ItemAttributeFishingLine) item.getAttribute("fishing_line")).getValue();
        return value == null || value.equalsIgnoreCase("none") ? null : value;
    }

    public void setFishingLine(@Nullable String lineId) {
        item.getAttribute("fishing_line").setValue(lineId == null ? "none" : lineId);
    }

    public @Nullable String getFishingSinker() {
        String value = ((ItemAttributeFishingSinker) item.getAttribute("fishing_sinker")).getValue();
        return value == null || value.equalsIgnoreCase("none") ? null : value;
    }

    public void setFishingSinker(@Nullable String sinkerId) {
        item.getAttribute("fishing_sinker").setValue(sinkerId == null ? "none" : sinkerId);
    }

    public long getFishingExpertiseKills() {
        return ((ItemAttributeFishingExpertiseKills) item.getAttribute("fishing_expertise_kills")).getValue();
    }

    public void setFishingExpertiseKills(long kills) {
        item.getAttribute("fishing_expertise_kills").setValue(kills);
    }

    public boolean isPet() {
        return item.hasComponent(PetComponent.class);
    }

    public ItemAttributePetData.PetData getPetData() {
        if (item.hasComponent(PetComponent.class)) {
            return ((ItemAttributePetData) item.getAttribute("pet_data")).getValue();
        } else {
            throw new RuntimeException("Item is not a pet");
        }
    }

    public Color getLeatherColour() {
        // Check for dynamically applied dye color first
        String dyeColor = getDyeColor();
        if (dyeColor != null) {
            return parseDyeColor(dyeColor);
        }
        // Fall back to component-defined color
        if (item.hasComponent(LeatherColorComponent.class)) {
            return item.getComponent(LeatherColorComponent.class).getColor();
        }
        return null;
    }

    public @Nullable String getDyeColor() {
        return ((ItemAttributeDyeColor) item.getAttribute("dye_color")).getValue();
    }

    public void setDyeColor(String hexColor) {
        item.getAttribute("dye_color").setValue(hexColor);
    }

    private Color parseHexColor(String hex) {
        hex = hex.replace("#", "");
        int r = Integer.parseInt(hex.substring(0, 2), 16);
        int g = Integer.parseInt(hex.substring(2, 4), 16);
        int b = Integer.parseInt(hex.substring(4, 6), 16);
        return new Color(r, g, b);
    }

    private Color parseDyeColor(String value) {
        if (!value.startsWith("animated:")) return parseHexColor(value);
        String[] parts = value.split(":");
        Color from = parseHexColor(parts[1]);
        Color to = parseHexColor(parts[2]);
        long period = Long.parseLong(parts[3]);
        double phase = (System.currentTimeMillis() % period) / (double) period;
        double progress = phase < .5 ? phase * 2 : (1 - phase) * 2;
        return new Color(
                (int) Math.round(from.red() + (to.red() - from.red()) * progress),
                (int) Math.round(from.green() + (to.green() - from.green()) * progress),
                (int) Math.round(from.blue() + (to.blue() - from.blue()) * progress));
    }

    public void setSoulBound(boolean coopAllowed) {
        item.getAttribute("soul_bound").setValue(
                new ItemAttributeSoulbound.SoulBoundData(coopAllowed)
        );
    }

    public ItemAttributeSoulbound.SoulBoundData getSoulBoundData() {
        ItemAttributeSoulbound.SoulBoundData potentialData = ((ItemAttributeSoulbound) item
                .getAttribute("soul_bound"))
                .getValue();
        if (potentialData != null) return potentialData;
        if (item.hasComponent(DefaultSoulboundComponent.class))
            return new ItemAttributeSoulbound.SoulBoundData(
                    item.getComponent(DefaultSoulboundComponent.class).isCoopAllowed());
        return null;
    }

    public @Nullable ItemAttributeGemData.GemData getGemData() {
        if (item.hasComponent(GemstoneComponent.class)) {
            return ((ItemAttributeGemData) item.getAttribute("gems")).getValue();
        } else {
            return null;
        }
    }

    public void setGemData(ItemAttributeGemData.GemData data) {
        if (item.hasComponent(GemstoneComponent.class)) {
            ((ItemAttributeGemData) item.getAttribute("gems")).setValue(data);
        } else {
            throw new RuntimeException("Item is not a gemstone item");
        }
    }

    public void setPetData(ItemAttributePetData.PetData data) {
        if (item.hasComponent(PetComponent.class)) {
            ((ItemAttributePetData) item.getAttribute("pet_data")).setValue(data);
        } else {
            throw new RuntimeException("Item is not a pet");
        }
    }

    public ItemAttributeBackpackData.BackpackData getBackpackData() {
        if (item.hasComponent(BackpackComponent.class)) {
            return ((ItemAttributeBackpackData) item.getAttribute("backpack_data")).getValue();
        } else {
            throw new RuntimeException("Item is not a backpack");
        }
    }

    public void setBackpackData(ItemAttributeBackpackData.BackpackData data) {
        if (item.hasComponent(BackpackComponent.class)) {
            ((ItemAttributeBackpackData) item.getAttribute("backpack_data")).setValue(data);
        } else {
            throw new RuntimeException("Item is not a backpack");
        }
    }

    public @Nullable ItemType getPotentialType() {
        try {
            return ItemType.valueOf(((ItemAttributeType) item.getAttribute("item_type")).getValue());
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public Rarity getRarity() {
        return ((ItemAttributeRarity) item.getAttribute("rarity")).getValue();
    }

    public @Nullable String getUniqueTrackedID() {
        String value = ((ItemAttributeUniqueTrackedID) item.getAttribute("unique-tracked-id")).getValue();
        if (value.equals("none")) return null;
        return value;
    }

    public void setUniqueTrackedID(String uniqueTrackedID, SkyBlockPlayer player) {
        item.getAttribute("unique-tracked-id").setValue(uniqueTrackedID);

        Thread.startVirtualThread(() -> {
            ProxyService itemTracker = new ProxyService(ServiceType.ITEM_TRACKER);
            if (!itemTracker.isOnline().join()) return;

            TrackedItemUpdateProtocol.TrackedItemUpdateMessage message =
                    new TrackedItemUpdateProtocol.TrackedItemUpdateMessage(
                            UUID.fromString(uniqueTrackedID),
                            player.getUuid(),
                            player.getProfiles().getCurrentlySelected(),
                            item.getAttributeHandler().getTypeAsString()
            );

            CompletableFuture<TrackedItemUpdateProtocol.TrackedItemUpdateResponse> future
                    = itemTracker.handleRequest(message);
        });
    }

    public void setRarity(Rarity rarity) {
        ((ItemAttributeRarity) item.getAttribute("rarity")).setValue(rarity);
    }

    public boolean hasEnchantment(EnchantmentType type) {
        return ((ItemAttributeEnchantments) item.getAttribute("enchantments")).getValue()
                .enchantments()
                .stream()
                .anyMatch(enchantment -> new SkyBlockEnchantment(enchantment).type() == type);
    }

    public void removeEnchantment(EnchantmentType type) {
        ((ItemAttributeEnchantments) item.getAttribute("enchantments")).getValue()
                .enchantments()
                .removeIf(enchantment -> new SkyBlockEnchantment(enchantment).type() == type);
    }

    public @Nullable Reforge getReforge() {
        String reforgeName = ((ItemAttributeReforge) item.getAttribute("reforge")).getValue();
        if (reforgeName == null || reforgeName.isEmpty()) {
            return null;
        }
        if (item.hasComponent(ReforgableComponent.class)) {
            return ReforgeLoader.getReforge(reforgeName,
                    item.getComponent(ReforgableComponent.class).getReforgeType());
        }
        return ReforgeLoader.getReforge(reforgeName);
    }

    public MinionRegistry getMinionType() {
        if (item.hasComponent(MinionComponent.class)) {
            return item.getComponent(MinionComponent.class).getMinionRegistry();
        } else {
            throw new RuntimeException("Item is not a minion");
        }
    }

    public ItemAttributeMinionData.MinionData getMinionData() {
        if (item.hasComponent(MinionComponent.class)) {
            return ((ItemAttributeMinionData) item.getAttribute("minion_tier")).getValue();
        } else {
            throw new RuntimeException("Item is not a minion");
        }
    }

    public void setMinionData(ItemAttributeMinionData.MinionData data) {
        item.getAttribute("minion_tier").setValue(data);
    }

    public void setReforge(Reforge reforge) throws IllegalArgumentException {
        if (!item.getAttributeHandler().getRarity().isCanReforge())
            throw new IllegalArgumentException("The rarity " + item.getAttributeHandler().getRarity().name() + " is not reforgable.");

        String reforgeName = (reforge != null) ? reforge.getId() : null;
        item.getAttribute("reforge").setValue(reforgeName);
    }

    public void setReforge(String reforgeName) throws IllegalArgumentException {
        if (!item.getAttributeHandler().getRarity().isCanReforge())
            throw new IllegalArgumentException("The rarity " + item.getAttributeHandler().getRarity().name() + " is not reforgable.");

        // Validate the reforge exists
        if (reforgeName != null && !reforgeName.isEmpty()) {
            Reforge reforge = ReforgeLoader.getReforge(reforgeName);
            if (reforge == null) {
                throw new IllegalArgumentException("Unknown reforge: " + reforgeName);
            }
        }

        item.getAttribute("reforge").setValue(reforgeName);
    }

    public @Nullable SkyBlockEnchantment getEnchantment(EnchantmentType type) {
        return ((ItemAttributeEnchantments) item.getAttribute("enchantments")).getValue()
                .enchantments()
                .stream()
                .filter(enchantment -> new SkyBlockEnchantment(enchantment).type() == type)
                .findFirst()
                .map(SkyBlockEnchantment::new)
                .orElse(null);
    }

    public Stream<SkyBlockEnchantment> getEnchantments() {
        return ((ItemAttributeEnchantments) item.getAttribute("enchantments")).getValue()
                .enchantments().stream()
                .map(SkyBlockEnchantment::new);
    }

    public void addEnchantment(SkyBlockEnchantment enchantment) {
        ((ItemAttributeEnchantments) item.getAttribute("enchantments")).getValue()
                .addEnchantment(enchantment.toUnderstandable());
    }

    /**
     * Gets the base statistics from the item's configuration.
     * This does NOT include extra dynamic statistics like Greed bonus.
     */
    public ItemStatistics getBaseStatistics() {
        return ((ItemAttributeStatistics) item.getAttribute("statistics")).getValue().clone();
    }

    /**
     * Gets the combined statistics (base + extra dynamic).
     * This is what should be used when calculating item stats for display/combat.
     */
    public ItemStatistics getStatistics() {
        ItemStatistics baseStats = getBaseStatistics();
        ItemStatistics extraStats = getExtraDynamicStatistics();
        return ItemStatistics.add(baseStats, extraStats);
    }

    /**
     * Gets extra dynamic statistics that are added on top of base stats.
     */
    public ItemStatistics getExtraDynamicStatistics() {
        return ((ItemAttributeExtraDynamicStatistics) item.getAttribute("extra_dynamic_statistics")).getValue();
    }

    public void setExtraDynamicStatistics(ItemStatistics statistics) {
        ((ItemAttributeExtraDynamicStatistics) item.getAttribute("extra_dynamic_statistics")).setValue(statistics);
    }

    public void setRecombobulated(boolean value) {
        ((ItemAttributeRecombobulated) item.getAttribute("recombobulated")).setValue(value);
    }

    public boolean isRecombobulated() {
        return ((ItemAttributeRecombobulated) item.getAttribute("recombobulated")).getValue();
    }

    public boolean isMithrilInfused() {
        if (item.hasComponent(MinionComponent.class)) {
            return ((ItemAttributeMithrilInfusion) item.getAttribute("mithril_infusion")).getValue();
        } else {
            throw new RuntimeException("Item is not a minion");
        }
    }

    public void setMithrilInfused(boolean value) {
        if (item.hasComponent(MinionComponent.class)) {
            ((ItemAttributeMithrilInfusion) item.getAttribute("mithril_infusion")).setValue(value);
        } else {
            throw new RuntimeException("Item is not a minion");
        }
    }

    public int getBreakingPower() {
        return ((ItemAttributeBreakingPower) item.getAttribute("breaking-power")).getValue();
    }

    public void setBreakingPower(int breakingPower) {
        ((ItemAttributeBreakingPower) item.getAttribute("breaking-power")).setValue(breakingPower);
    }

    public int getNewYearCakeYear() {
        return ((ItemAttributeNewYearCakeYear) item.getAttribute("new-year-cake-year")).getValue();
    }

    public void setNewYearCakeYear(int year) {
        ((ItemAttributeNewYearCakeYear) item.getAttribute("new-year-cake-year")).setValue(year);
    }

    public boolean isMiningTool() {
        return getBreakingPower() != 0;
    }

    public long getDarkAuctionPrice() {
        return ((ItemAttributeDarkAuctionPrice) item.getAttribute("dark_auction_price")).getValue();
    }

    public void setDarkAuctionPrice(long price) {
        ((ItemAttributeDarkAuctionPrice) item.getAttribute("dark_auction_price")).setValue(price);
    }

    public List<AbiphoneNPC> getAbiphoneNPCs() {
        return ((ItemAttributeAbiphoneContacts) item.getAttribute("abiphone_contacts")).getValue().stream()
                .map(AbiphoneRegistry::getFromId)
                .filter(Objects::nonNull)
                .toList();
    }

    public void setAbiphoneNPCs(List<AbiphoneNPC> npcs) {
        List<String> ids = npcs.stream()
                .map(AbiphoneNPC::getId)
                .toList();
        ((ItemAttributeAbiphoneContacts) item.getAttribute("abiphone_contacts")).setValue(ids);
    }

    public void addAbiphoneNPC(AbiphoneNPC npc) {
        ItemAttributeAbiphoneContacts attribute = (ItemAttributeAbiphoneContacts) item.getAttribute("abiphone_contacts");
        List<String> ids = new ArrayList<>(attribute.getValue());
        if (!ids.contains(npc.getId())) {
            ids.add(npc.getId());
            attribute.setValue(ids);
        }
    }

    public void removeAbiphoneNPC(AbiphoneNPC npc) {
        ItemAttributeAbiphoneContacts attribute = (ItemAttributeAbiphoneContacts) item.getAttribute("abiphone_contacts");
        List<String> ids = new ArrayList<>(attribute.getValue());
        if (ids.contains(npc.getId())) {
            ids.remove(npc.getId());
            attribute.setValue(ids);
        }
    }

    public boolean hasAbiphoneNPC(AbiphoneNPC npc) {
        ItemAttributeAbiphoneContacts attribute = (ItemAttributeAbiphoneContacts) item.getAttribute("abiphone_contacts");
        List<String> ids = attribute.getValue();
        return ids.contains(npc.getId());
    }

    public void setStoredPotential(int storedPotential) {
        ((ItemAttributeStoredPotential) item.getAttribute("stored_potential")).setValue(storedPotential);
    }

    public int getStoredPotential() {
        return ((ItemAttributeStoredPotential) item.getAttribute("stored_potential")).getValue();
    }

    public SkyBlockItem asSkyBlockItem() {
        return item;
    }

    public @Nullable ItemAttributePotionData.PotionData getPotionData() {
        return ((ItemAttributePotionData) item.getAttribute("potion_data")).getValue();
    }

    public void setPotionData(ItemAttributePotionData.PotionData data) {
        ((ItemAttributePotionData) item.getAttribute("potion_data")).setValue(data);
    }
}
