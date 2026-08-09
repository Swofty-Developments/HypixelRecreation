package net.swofty.type.skyblockgeneric.item.set;

import lombok.Getter;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSet;
import net.swofty.type.skyblockgeneric.item.set.sets.*;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

@Getter
public enum ArmorSetRegistry {
	LEAFLET(LeafletSet.class, ItemType.LEAFLET_BOOTS, ItemType.LEAFLET_LEGGINGS, ItemType.LEAFLET_CHESTPLATE, ItemType.LEAFLET_HELMET),
	PROSPECTING(ProspectingSet.class, ItemType.MINER_OUTFIT_BOOTS, ItemType.MINER_OUTFIT_LEGGINGS, ItemType.MINER_OUTFIT_CHESTPLATE, ItemType.MINER_OUTFIT_HELMET),
	CHEAP_TUXEDO(CheapTuxedoSet.class, ItemType.CHEAP_TUXEDO_BOOTS, ItemType.CHEAP_TUXEDO_LEGGINGS, ItemType.CHEAP_TUXEDO_CHESTPLATE, null),
	FANCY_TUXEDO(FancyTuxedoSet.class, ItemType.FANCY_TUXEDO_BOOTS, ItemType.FANCY_TUXEDO_LEGGINGS, ItemType.FANCY_TUXEDO_CHESTPLATE, null),
	ELEGANT_TUXEDO(ElegantTuxedoSet.class, ItemType.ELEGANT_TUXEDO_BOOTS, ItemType.ELEGANT_TUXEDO_LEGGINGS, ItemType.ELEGANT_TUXEDO_CHESTPLATE, null),
	MUSHROOM(MushroomSet.class, ItemType.MUSHROOM_BOOTS, ItemType.MUSHROOM_LEGGINGS, ItemType.MUSHROOM_CHESTPLATE, ItemType.MUSHROOM_HELMET),
	LAPIS(LapisArmorSet.class, ItemType.LAPIS_ARMOR_BOOTS, ItemType.LAPIS_ARMOR_LEGGINGS, ItemType.LAPIS_ARMOR_CHESTPLATE, ItemType.LAPIS_ARMOR_HELMET),
	MINER(MinerArmorSet.class, ItemType.MINER_ARMOR_BOOTS, ItemType.MINER_ARMOR_LEGGINGS, ItemType.MINER_ARMOR_CHESTPLATE, ItemType.MINER_ARMOR_HELMET),
	SPEEDSTER(SpeedsterSet.class, ItemType.SPEEDSTER_BOOTS, ItemType.SPEEDSTER_LEGGINGS, ItemType.SPEEDSTER_CHESTPLATE, ItemType.SPEEDSTER_HELMET),
	PUMPKIN(PumpkinSet.class, ItemType.PUMPKIN_BOOTS, ItemType.PUMPKIN_LEGGINGS, ItemType.PUMPKIN_CHESTPLATE, ItemType.PUMPKIN_HELMET),
	ENDER(EnderArmorSet.class, ItemType.END_BOOTS, ItemType.END_LEGGINGS, ItemType.END_CHESTPLATE, ItemType.END_HELMET),
	PARK_ARMOR(null, ItemType.MELODY_SHOES, ItemType.CHARLIE_TROUSERS, ItemType.KELLY_TSHIRT, ItemType.MOLE_HAT),
	ABYSSAL(null, ItemType.ABYSSAL_BOOTS, ItemType.ABYSSAL_LEGGINGS, ItemType.ABYSSAL_CHESTPLATE, ItemType.ABYSSAL_HELMET),
	ANGLER(null, ItemType.ANGLER_BOOTS, ItemType.ANGLER_LEGGINGS, ItemType.ANGLER_CHESTPLATE, ItemType.ANGLER_HELMET),
	ARACHNE(null, ItemType.ARACHNE_BOOTS, ItemType.ARACHNE_LEGGINGS, ItemType.ARACHNE_CHESTPLATE, ItemType.ARACHNE_HELMET),
	AURORA(null, ItemType.AURORA_BOOTS, ItemType.AURORA_LEGGINGS, ItemType.AURORA_CHESTPLATE, ItemType.AURORA_HELMET),
	BACKWATER(null, ItemType.BACKWATER_BOOTS, ItemType.BACKWATER_LEGGINGS, ItemType.BACKWATER_CHESTPLATE, ItemType.BACKWATER_HELMET),
	BAT_PERSON(null, ItemType.BAT_PERSON_BOOTS, ItemType.BAT_PERSON_LEGGINGS, ItemType.BAT_PERSON_CHESTPLATE, ItemType.BAT_PERSON_HELMET),
	BERSERKER(null, ItemType.BERSERKER_BOOTS, ItemType.BERSERKER_LEGGINGS, ItemType.BERSERKER_CHESTPLATE, ItemType.BERSERKER_HELMET),
	BIOHAZARD(null, ItemType.BIOHAZARD_BOOTS, ItemType.BIOHAZARD_LEGGINGS, ItemType.BIOHAZARD_SUIT, ItemType.BIOHAZARD_HELMET),
	BLAZE(null, ItemType.BLAZE_BOOTS, ItemType.BLAZE_LEGGINGS, ItemType.BLAZE_CHESTPLATE, ItemType.BLAZE_HELMET),
	BRONZE_HUNTER(null, ItemType.BRONZE_HUNTER_BOOTS, ItemType.BRONZE_HUNTER_LEGGINGS, ItemType.BRONZE_HUNTER_CHESTPLATE, ItemType.BRONZE_HUNTER_HELMET),
	CACTUS(null, ItemType.CACTUS_BOOTS, ItemType.CACTUS_LEGGINGS, ItemType.CACTUS_CHESTPLATE, ItemType.CACTUS_HELMET),
	CANOPY(null, ItemType.CANOPY_BOOTS, ItemType.CANOPY_LEGGINGS, ItemType.CANOPY_CHESTPLATE, ItemType.CANOPY_HELMET),
	CELESTE(null, ItemType.CELESTE_BOOTS, ItemType.CELESTE_LEGGINGS, ItemType.CELESTE_CHESTPLATE, ItemType.CELESTE_HELMET),
	CHALLENGER(null, ItemType.CHALLENGER_BOOTS, ItemType.CHALLENGER_LEGGINGS, ItemType.CHALLENGER_CHESTPLATE, ItemType.CHALLENGER_HELMET),
	CRIMSON(null, ItemType.CRIMSON_BOOTS, ItemType.CRIMSON_LEGGINGS, ItemType.CRIMSON_CHESTPLATE, ItemType.CRIMSON_HELMET),
	CROPIE(null, ItemType.CROPIE_BOOTS, ItemType.CROPIE_LEGGINGS, ItemType.CROPIE_CHESTPLATE, ItemType.CROPIE_HELMET),
	CRYSTAL(null, ItemType.CRYSTAL_BOOTS, ItemType.CRYSTAL_LEGGINGS, ItemType.CRYSTAL_CHESTPLATE, ItemType.CRYSTAL_HELMET),
	DIAMOND_HUNTER(null, ItemType.DIAMOND_HUNTER_BOOTS, ItemType.DIAMOND_HUNTER_LEGGINGS, ItemType.DIAMOND_HUNTER_CHESTPLATE, ItemType.DIAMOND_HUNTER_HELMET),
	DIVAN(null, ItemType.DIVAN_BOOTS, ItemType.DIVAN_LEGGINGS, ItemType.DIVAN_CHESTPLATE, ItemType.DIVAN_HELMET),
	DIVER(null, ItemType.DIVER_BOOTS, ItemType.DIVER_LEGGINGS, ItemType.DIVER_CHESTPLATE, ItemType.DIVER_HELMET),
	EMBER(null, ItemType.EMBER_BOOTS, ItemType.EMBER_LEGGINGS, ItemType.EMBER_CHESTPLATE, ItemType.EMBER_HELMET),
	EMERALD(null, ItemType.EMERALD_ARMOR_BOOTS, ItemType.EMERALD_ARMOR_LEGGINGS, ItemType.EMERALD_ARMOR_CHESTPLATE, ItemType.EMERALD_ARMOR_HELMET),
	FAIRY(null, ItemType.FAIRY_BOOTS, ItemType.FAIRY_LEGGINGS, ItemType.FAIRY_CHESTPLATE, ItemType.FAIRY_HELMET),
	FARM(null, ItemType.FARM_ARMOR_BOOTS, ItemType.FARM_ARMOR_LEGGINGS, ItemType.FARM_ARMOR_CHESTPLATE, ItemType.FARM_ARMOR_HELMET),
	FARMSUIT(null, ItemType.FARM_SUIT_BOOTS, ItemType.FARM_SUIT_LEGGINGS, ItemType.FARM_SUIT_CHESTPLATE, ItemType.FARM_SUIT_HELMET),
	FERMENTO(null, ItemType.FERMENTO_BOOTS, ItemType.FERMENTO_LEGGINGS, ItemType.FERMENTO_CHESTPLATE, ItemType.FERMENTO_HELMET),
	FERVOR(null, ItemType.FERVOR_BOOTS, ItemType.FERVOR_LEGGINGS, ItemType.FERVOR_CHESTPLATE, ItemType.FERVOR_HELMET),
	FIG(null, ItemType.FIG_BOOTS, ItemType.FIG_LEGGINGS, ItemType.FIG_CHESTPLATE, ItemType.FIG_HELMET),
	FINAL_DESTINATION(null, ItemType.FINAL_DESTINATION_BOOTS, ItemType.FINAL_DESTINATION_LEGGINGS, ItemType.FINAL_DESTINATION_CHESTPLATE, ItemType.FINAL_DESTINATION_HELMET),
	FLAME_BREAKER(null, ItemType.FLAME_BREAKER_BOOTS, ItemType.FLAME_BREAKER_LEGGINGS, ItemType.FLAME_BREAKER_CHESTPLATE, ItemType.FLAME_BREAKER_HELMET),
	FLAMEBREAKER(null, ItemType.FLAMEBREAKER_BOOTS, ItemType.FLAMEBREAKER_LEGGINGS, ItemType.FLAMEBREAKER_CHESTPLATE, ItemType.FLAMEBREAKER_HELMET),
	FROZEN_BLAZE(null, ItemType.FROZEN_BLAZE_BOOTS, ItemType.FROZEN_BLAZE_LEGGINGS, ItemType.FROZEN_BLAZE_CHESTPLATE, ItemType.FROZEN_BLAZE_HELMET),
	GLACITE(null, ItemType.GLACITE_BOOTS, ItemType.GLACITE_LEGGINGS, ItemType.GLACITE_CHESTPLATE, ItemType.GLACITE_HELMET),
	GLOSSY_MINERAL(null, ItemType.GLOSSY_MINERAL_BOOTS, ItemType.GLOSSY_MINERAL_LEGGINGS, ItemType.GLOSSY_MINERAL_CHESTPLATE, ItemType.GLOSSY_MINERAL_HELMET),
	GOBLIN(null, ItemType.GOBLIN_BOOTS, ItemType.GOBLIN_LEGGINGS, ItemType.GOBLIN_CHESTPLATE, ItemType.GOBLIN_HELMET),
	GOLD_HUNTER(null, ItemType.GOLD_HUNTER_BOOTS, ItemType.GOLD_HUNTER_LEGGINGS, ItemType.GOLD_HUNTER_CHESTPLATE, ItemType.GOLD_HUNTER_HELMET),
	GOLEM(null, ItemType.GOLEM_ARMOR_BOOTS, ItemType.GOLEM_ARMOR_LEGGINGS, ItemType.GOLEM_ARMOR_CHESTPLATE, ItemType.GOLEM_ARMOR_HELMET),
	GREAT_SPOOK(null, ItemType.GREAT_SPOOK_BOOTS, ItemType.GREAT_SPOOK_LEGGINGS, ItemType.GREAT_SPOOK_CHESTPLATE, ItemType.GREAT_SPOOK_HELMET),
	GROWTH(null, ItemType.GROWTH_BOOTS, ItemType.GROWTH_LEGGINGS, ItemType.GROWTH_CHESTPLATE, ItemType.GROWTH_HELMET),
	HARDENED_DIAMOND(null, ItemType.HARDENED_DIAMOND_BOOTS, ItemType.HARDENED_DIAMOND_LEGGINGS, ItemType.HARDENED_DIAMOND_CHESTPLATE, ItemType.HARDENED_DIAMOND_HELMET),
	HEAT(null, ItemType.HEAT_BOOTS, ItemType.HEAT_LEGGINGS, ItemType.HEAT_CHESTPLATE, ItemType.HEAT_HELMET),
	HELIANTHUS(null, ItemType.HELIANTHUS_BOOTS, ItemType.HELIANTHUS_LEGGINGS, ItemType.HELIANTHUS_CHESTPLATE, ItemType.HELIANTHUS_HELMET),
	HOLLOW(null, ItemType.HOLLOW_BOOTS, ItemType.HOLLOW_LEGGINGS, ItemType.HOLLOW_CHESTPLATE, ItemType.HOLLOW_HELMET),
	HOLY_DRAGON(null, ItemType.HOLY_DRAGON_BOOTS, ItemType.HOLY_DRAGON_LEGGINGS, ItemType.HOLY_DRAGON_CHESTPLATE, ItemType.HOLY_DRAGON_HELMET),
	MAGMA(null, ItemType.ARMOR_OF_MAGMA_BOOTS, ItemType.ARMOR_OF_MAGMA_LEGGINGS, ItemType.ARMOR_OF_MAGMA_CHESTPLATE, ItemType.ARMOR_OF_MAGMA_HELMET),
	MAGMA_LORD(null, ItemType.MAGMA_LORD_BOOTS, ItemType.MAGMA_LORD_LEGGINGS, ItemType.MAGMA_LORD_CHESTPLATE, ItemType.MAGMA_LORD_HELMET),
	MASTIFF(null, ItemType.MASTIFF_BOOTS, ItemType.MASTIFF_LEGGINGS, ItemType.MASTIFF_CHESTPLATE, ItemType.MASTIFF_HELMET),
	MELON(null, ItemType.MELON_BOOTS, ItemType.MELON_LEGGINGS, ItemType.MELON_CHESTPLATE, ItemType.MELON_HELMET),
	MERCENARY(null, ItemType.MERCENARY_BOOTS, ItemType.MERCENARY_LEGGINGS, ItemType.MERCENARY_CHESTPLATE, ItemType.MERCENARY_HELMET),
	MINERAL(null, ItemType.MINERAL_BOOTS, ItemType.MINERAL_LEGGINGS, ItemType.MINERAL_CHESTPLATE, ItemType.MINERAL_HELMET),
	MONSTER_HUNTER(null, ItemType.SPIDER_BOOTS, ItemType.CREEPER_LEGGINGS, ItemType.GUARDIAN_CHESTPLATE, ItemType.SKELETON_HELMET),
	MYTHOS(null, ItemType.MYTHOS_BOOTS, ItemType.MYTHOS_LEGGINGS, ItemType.MYTHOS_CHESTPLATE, ItemType.MYTHOS_HELMET),
	NUTCRACKER(null, ItemType.NUTCRACKER_BOOTS, ItemType.NUTCRACKER_LEGGINGS, ItemType.NUTCRACKER_CHESTPLATE, ItemType.NUTCRACKER_HELMET),
	OLD_DRAGON(null, ItemType.OLD_DRAGON_BOOTS, ItemType.OLD_DRAGON_LEGGINGS, ItemType.OLD_DRAGON_CHESTPLATE, ItemType.OLD_DRAGON_HELMET),
	PACK(null, ItemType.BOOTS_OF_THE_PACK, ItemType.LEGGINGS_OF_THE_PACK, ItemType.CHESTPLATE_OF_THE_PACK, ItemType.HELMET_OF_THE_PACK),
	PERFECT(null, ItemType.PERFECT_BOOTS_1, ItemType.PERFECT_LEGGINGS_1, ItemType.PERFECT_CHESTPLATE_1, ItemType.PERFECT_HELMET_1),
	POWER_WITHER(null, ItemType.POWER_WITHER_BOOTS, ItemType.POWER_WITHER_LEGGINGS, ItemType.POWER_WITHER_CHESTPLATE, ItemType.POWER_WITHER_HELMET),
	PRIMORDIAL(null, ItemType.PRIMORDIAL_BOOTS, ItemType.PRIMORDIAL_LEGGINGS, ItemType.PRIMORDIAL_CHESTPLATE, ItemType.PRIMORDIAL_HELMET),
	PROTECTOR_DRAGON(null, ItemType.PROTECTOR_DRAGON_BOOTS, ItemType.PROTECTOR_DRAGON_LEGGINGS, ItemType.PROTECTOR_DRAGON_CHESTPLATE, ItemType.PROTECTOR_DRAGON_HELMET),
	RABBIT(null, ItemType.RABBIT_BOOTS, ItemType.RABBIT_LEGGINGS, ItemType.RABBIT_CHESTPLATE, ItemType.RABBIT_HELMET),
	RAMPART(null, ItemType.RAMPART_BOOTS, ItemType.RAMPART_LEGGINGS, ItemType.RAMPART_CHESTPLATE, ItemType.RAMPART_HELMET),
	REAPER(null, ItemType.REAPER_BOOTS, ItemType.REAPER_LEGGINGS, ItemType.REAPER_CHESTPLATE, null),
	REKINDLED_EMBER(null, ItemType.REKINDLED_EMBER_BOOTS, ItemType.REKINDLED_EMBER_LEGGINGS, ItemType.REKINDLED_EMBER_CHESTPLATE, ItemType.REKINDLED_EMBER_HELMET),
	RESISTANCE(null, ItemType.ARMOR_OF_THE_RESISTANCE_BOOTS, ItemType.ARMOR_OF_THE_RESISTANCE_LEGGINGS, ItemType.ARMOR_OF_THE_RESISTANCE_CHESTPLATE, ItemType.ARMOR_OF_THE_RESISTANCE_HELMET),
	REVENANT(null, ItemType.REVENANT_BOOTS, ItemType.REVENANT_LEGGINGS, ItemType.REVENANT_CHESTPLATE, null),
	ROSETTA(null, ItemType.ROSETTA_BOOTS, ItemType.ROSETTA_LEGGINGS, ItemType.ROSETTA_CHESTPLATE, ItemType.ROSETTA_HELMET),
	SALMON(null, ItemType.SALMON_BOOTS_NEW, ItemType.SALMON_LEGGINGS_NEW, ItemType.SALMON_CHESTPLATE_NEW, ItemType.SALMON_HELMET_NEW),
	SHARK_SCALE(null, ItemType.SHARK_SCALE_BOOTS, ItemType.SHARK_SCALE_LEGGINGS, ItemType.SHARK_SCALE_CHESTPLATE, ItemType.SHARK_SCALE_HELMET),
	SHIMMERING_LIGHT(null, ItemType.SHIMMERING_LIGHT_SLIPPERS, ItemType.SHIMMERING_LIGHT_TROUSERS, ItemType.SHIMMERING_LIGHT_TUNIC, ItemType.SHIMMERING_LIGHT_HOOD),
	SILVER_HUNTER(null, ItemType.SILVER_HUNTER_BOOTS, ItemType.SILVER_HUNTER_LEGGINGS, ItemType.SILVER_HUNTER_CHESTPLATE, ItemType.SILVER_HUNTER_HELMET),
	SNORKELING(null, ItemType.SNORKELING_BOOTS, ItemType.SNORKELING_LEGGINGS, ItemType.SNORKELING_CHESTPLATE, ItemType.SNORKELING_HELMET),
	SNOW_SUIT(null, ItemType.SNOW_SUIT_BOOTS, ItemType.SNOW_SUIT_LEGGINGS, ItemType.SNOW_SUIT_CHESTPLATE, ItemType.SNOW_SUIT_HELMET),
	SORROW(null, ItemType.SORROW_BOOTS, ItemType.SORROW_LEGGINGS, ItemType.SORROW_CHESTPLATE, ItemType.SORROW_HELMET),
	SPEED_WITHER(null, ItemType.SPEED_WITHER_BOOTS, ItemType.SPEED_WITHER_LEGGINGS, ItemType.SPEED_WITHER_CHESTPLATE, ItemType.SPEED_WITHER_HELMET),
	SPONGE(null, ItemType.SPONGE_BOOTS, ItemType.SPONGE_LEGGINGS, ItemType.SPONGE_CHESTPLATE, ItemType.SPONGE_HELMET),
	SPOOKY(null, ItemType.SPOOKY_BOOTS, ItemType.SPOOKY_LEGGINGS, ItemType.SPOOKY_CHESTPLATE, ItemType.SPOOKY_HELMET),
	SPROUT(null, ItemType.SPROUT_BOOTS, ItemType.SPROUT_LEGGINGS, ItemType.SPROUT_CHESTPLATE, ItemType.SPROUT_HELMET),
	SQUASH(null, ItemType.SQUASH_BOOTS, ItemType.SQUASH_LEGGINGS, ItemType.SQUASH_CHESTPLATE, ItemType.SQUASH_HELMET),
	SQUIRE(null, ItemType.SQUIRE_BOOTS, ItemType.SQUIRE_LEGGINGS, ItemType.SQUIRE_CHESTPLATE, ItemType.SQUIRE_HELMET),
	STARLIGHT(null, ItemType.STARLIGHT_BOOTS, ItemType.STARLIGHT_LEGGINGS, ItemType.STARLIGHT_CHESTPLATE, ItemType.STARLIGHT_HELMET),
	STRONG_DRAGON(null, ItemType.STRONG_DRAGON_BOOTS, ItemType.STRONG_DRAGON_LEGGINGS, ItemType.STRONG_DRAGON_CHESTPLATE, ItemType.STRONG_DRAGON_HELMET),
	SUPERIOR_DRAGON(null, ItemType.SUPERIOR_DRAGON_BOOTS, ItemType.SUPERIOR_DRAGON_LEGGINGS, ItemType.SUPERIOR_DRAGON_CHESTPLATE, ItemType.SUPERIOR_DRAGON_HELMET),
	TANK_WITHER(null, ItemType.TANK_WITHER_BOOTS, ItemType.TANK_WITHER_LEGGINGS, ItemType.TANK_WITHER_CHESTPLATE, ItemType.TANK_WITHER_HELMET),
	TARANTULA(null, ItemType.TARANTULA_BOOTS, ItemType.TARANTULA_LEGGINGS, ItemType.TARANTULA_CHESTPLATE, ItemType.TARANTULA_HELMET),
	TERROR(null, ItemType.TERROR_BOOTS, ItemType.TERROR_LEGGINGS, ItemType.TERROR_CHESTPLATE, ItemType.TERROR_HELMET),
	THERMODYNAMIC(null, ItemType.THERMODYNAMIC_BOOTS, ItemType.THERMODYNAMIC_LEGGINGS, ItemType.THERMODYNAMIC_CHESTPLATE, ItemType.THERMODYNAMIC_HELMET),
	THUNDER(null, ItemType.THUNDER_BOOTS, ItemType.THUNDER_LEGGINGS, ItemType.THUNDER_CHESTPLATE, ItemType.THUNDER_HELMET),
	TROPHY_HUNTER(null, ItemType.SLUG_BOOTS, ItemType.MOOGMA_LEGGINGS, ItemType.FLAMING_CHESTPLATE, ItemType.TAURUS_HELMET),
	UNSTABLE_DRAGON(null, ItemType.UNSTABLE_DRAGON_BOOTS, ItemType.UNSTABLE_DRAGON_LEGGINGS, ItemType.UNSTABLE_DRAGON_CHESTPLATE, ItemType.UNSTABLE_DRAGON_HELMET),
	VELVET(null, ItemType.OXFORD_SHOES, ItemType.SATIN_TROUSERS, ItemType.CASHMERE_JACKET, ItemType.VELVET_TOP_HAT),
	WEREWOLF(null, ItemType.WEREWOLF_BOOTS, ItemType.WEREWOLF_LEGGINGS, ItemType.WEREWOLF_CHESTPLATE, ItemType.WEREWOLF_HELMET),
	WISE_DRAGON(null, ItemType.WISE_DRAGON_BOOTS, ItemType.WISE_DRAGON_LEGGINGS, ItemType.WISE_DRAGON_CHESTPLATE, ItemType.WISE_DRAGON_HELMET),
	WISE_WITHER(null, ItemType.WISE_WITHER_BOOTS, ItemType.WISE_WITHER_LEGGINGS, ItemType.WISE_WITHER_CHESTPLATE, ItemType.WISE_WITHER_HELMET),
	WITHER(null, ItemType.WITHER_BOOTS, ItemType.WITHER_LEGGINGS, ItemType.WITHER_CHESTPLATE, ItemType.WITHER_HELMET),
	WYLD(null, ItemType.WYLD_BOOTS, ItemType.WYLD_LEGGINGS, ItemType.WYLD_CHESTPLATE, ItemType.WYLD_HELMET),
	YOG(null, ItemType.ARMOR_OF_YOG_BOOTS, ItemType.ARMOR_OF_YOG_LEGGINGS, ItemType.ARMOR_OF_YOG_CHESTPLATE, ItemType.ARMOR_OF_YOG_HELMET),
	YOUNG_DRAGON(null, ItemType.YOUNG_DRAGON_BOOTS, ItemType.YOUNG_DRAGON_LEGGINGS, ItemType.YOUNG_DRAGON_CHESTPLATE, ItemType.YOUNG_DRAGON_HELMET),
	ZOMBIE(null, ItemType.ZOMBIE_BOOTS, ItemType.ZOMBIE_LEGGINGS, ItemType.ZOMBIE_CHESTPLATE, null),
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
		if (clazz == null) return null;
		for (ArmorSetRegistry armorSetRegistry : values()) {
			if (armorSetRegistry.getClazz() == clazz) {
				return armorSetRegistry;
			}
		}
		return null;
	}

	public ArmorSet create() {
		if (clazz == null) return new ConfiguredArmorSet(this);
		try {
			return clazz.getDeclaredConstructor().newInstance();
		} catch (ReflectiveOperationException exception) {
			throw new IllegalStateException("Unable to create armor set " + name(), exception);
		}
	}

	public List<ItemType> getItemTypes() {
		List<ItemType> itemTypes = new ArrayList<>(4);
		if (helmet != null) itemTypes.add(helmet);
		if (chestplate != null) itemTypes.add(chestplate);
		if (leggings != null) itemTypes.add(leggings);
		if (boots != null) itemTypes.add(boots);
		return itemTypes;
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

	public static List<ArmorSetRegistry> getWornSets(ItemType boots, ItemType leggings, ItemType chestplate, ItemType helmet) {
		List<ArmorSetRegistry> wornSets = new ArrayList<>();
		for (ArmorSetRegistry armorSetRegistry : values()) {
			if (armorSetRegistry.getWornPieceCount(boots, leggings, chestplate, helmet) > 0) {
				wornSets.add(armorSetRegistry);
			}
		}
		return wornSets;
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
