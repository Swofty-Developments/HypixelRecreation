package net.swofty.type.skyblockgeneric.item.set;

import lombok.Getter;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSet;
import net.swofty.type.skyblockgeneric.item.set.sets.*;
import org.jetbrains.annotations.Nullable;

@Getter
public enum ArmorSetRegistry {
	LEAFLET(LeafletSet.class, ItemType.LEAFLET_BOOTS, ItemType.LEAFLET_LEGGINGS, ItemType.LEAFLET_CHESTPLATE, ItemType.LEAFLET_HELMET),
	MINER_OUTFIT(MinerOutfitSet.class, ItemType.MINERS_OUTFIT_BOOTS, ItemType.MINERS_OUTFIT_LEGGINGS, ItemType.MINERS_OUTFIT_CHESTPLATE, ItemType.MINERS_OUTFIT_HELMET),
	CHEAP_TUXEDO(CheapTuxedoSet.class, ItemType.CHEAP_TUXEDO_BOOTS, ItemType.CHEAP_TUXEDO_LEGGINGS, ItemType.CHEAP_TUXEDO_CHESTPLATE, null),
	FANCY_TUXEDO(FancyTuxedoSet.class, ItemType.FANCY_TUXEDO_BOOTS, ItemType.FANCY_TUXEDO_LEGGINGS, ItemType.FANCY_TUXEDO_CHESTPLATE, null),
	ELEGANT_TUXEDO(ElegantTuxedoSet.class, ItemType.ELEGANT_TUXEDO_BOOTS, ItemType.ELEGANT_TUXEDO_LEGGINGS, ItemType.ELEGANT_TUXEDO_CHESTPLATE, null),
	MUSHROOM(MushroomSet.class, ItemType.MUSHROOM_BOOTS, ItemType.MUSHROOM_LEGGINGS, ItemType.MUSHROOM_CHESTPLATE, ItemType.MUSHROOM_HELMET),
	LAPIS(LapisArmorSet.class, ItemType.LAPIS_ARMOR_BOOTS, ItemType.LAPIS_ARMOR_LEGGINGS, ItemType.LAPIS_ARMOR_CHESTPLATE, ItemType.LAPIS_ARMOR_HELMET),
    MINER(MinerArmorSet.class, ItemType.MINER_ARMOR_BOOTS, ItemType.MINER_ARMOR_LEGGINGS, ItemType.MINER_ARMOR_CHESTPLATE, ItemType.MINER_ARMOR_HELMET),
	PUMPKIN(PumpkinSet.class, ItemType.PUMPKIN_BOOTS, ItemType.PUMPKIN_LEGGINGS, ItemType.PUMPKIN_CHESTPLATE, ItemType.PUMPKIN_HELMET),
	ENDER(EnderArmorSet.class, ItemType.END_BOOTS, ItemType.END_LEGGINGS, ItemType.END_CHESTPLATE, ItemType.END_HELMET),
	PARK_ARMOR(null, ItemType.MELODY_SHOES, ItemType.CHARLIE_TROUSERS, ItemType.KELLY_TSHIRT, ItemType.MOLE_HAT),
	;

	private final Class<? extends ArmorSet> clazz;
	private final ItemType boots;
	private final ItemType leggings;
	private final ItemType chestplate;
	private final ItemType helmet;

	ArmorSetRegistry(Class<? extends ArmorSet> clazz, ItemType boots, ItemType legging,
					 ItemType chestplate, ItemType helmet) {
		this.clazz = clazz;
		this.boots = boots;
		this.leggings = legging;
		this.chestplate = chestplate;
		this.helmet = helmet;
	}

	public static @Nullable ArmorSetRegistry getArmorSet(Class<? extends ArmorSet> clazz) {
		for (ArmorSetRegistry armorSetRegistry : values()) {
			if (armorSetRegistry.getClazz() == clazz) {
				return armorSetRegistry;
			}
		}
		return null;
	}

	public static @Nullable ArmorSetRegistry getArmorSet(@Nullable ItemType item) {
		if (item == null) return null;
		for (ArmorSetRegistry armorSetRegistry : values()) {
			if (armorSetRegistry.getBoots() == item
					|| armorSetRegistry.getLeggings() == item
					|| armorSetRegistry.getChestplate() == item
					|| armorSetRegistry.getHelmet() == item) {
				return armorSetRegistry;
			}
		}
		return null;
	}

	public static int getPieceCount(ArmorSetRegistry armorSetRegistry) {
		int count = 0;
		if (armorSetRegistry.getBoots() != null) count++;
		if (armorSetRegistry.getLeggings() != null) count++;
		if (armorSetRegistry.getChestplate() != null) count++;
		if (armorSetRegistry.getHelmet() != null) count++;
		return count;
	}

	public static ArmorSetRegistry getArmorSet(ItemType boots, ItemType leggings, ItemType chestplate, ItemType helmet) {
		for (ArmorSetRegistry armorSetRegistry : values()) {
			if ((armorSetRegistry.getBoots() == null || armorSetRegistry.getBoots() == boots)
					&& (armorSetRegistry.getLeggings() == null || armorSetRegistry.getLeggings() == leggings)
					&& (armorSetRegistry.getChestplate() == null || armorSetRegistry.getChestplate() == chestplate)
					&& (armorSetRegistry.getHelmet() == null || armorSetRegistry.getHelmet() == helmet)) {
				return armorSetRegistry;
			}
		}
		return null;
	}

    public String getDisplayName() {
        return StringUtility.toNormalCase(name().replace("_", " "));
    }

    public int getWornPieceCount(ItemType boots, ItemType leggings, ItemType chestplate, ItemType helmet) {
        int count = 0;
        if (this.boots != null && this.boots == boots) count++;
        if (this.leggings != null && this.leggings == leggings) count++;
        if (this.chestplate != null && this.chestplate == chestplate) count++;
        if (this.helmet != null && this.helmet == helmet) count++;
        return count;
    }
}
