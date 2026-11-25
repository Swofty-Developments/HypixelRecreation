package net.swofty.type.skyblockgeneric.item.set;

import lombok.Getter;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSet;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;
import net.swofty.type.skyblockgeneric.item.set.effects.*;
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
	ABYSSAL(ItemType.ABYSSAL_BOOTS, ItemType.ABYSSAL_LEGGINGS, ItemType.ABYSSAL_CHESTPLATE, ItemType.ABYSSAL_HELMET, AbyssalOneWithTheFishEffect.class, AbyssalDeepSeaDiverEffect.class),
	ANGLER(ItemType.ANGLER_BOOTS, ItemType.ANGLER_LEGGINGS, ItemType.ANGLER_CHESTPLATE, ItemType.ANGLER_HELMET, AnglerDepthChampionEffect.class, AnglerDeepnessWithinEffect.class),
	ARACHNE(ItemType.ARACHNE_BOOTS, ItemType.ARACHNE_LEGGINGS, ItemType.ARACHNE_CHESTPLATE, ItemType.ARACHNE_HELMET, ArachneFaithfulEffect.class),
	AURORA(ItemType.AURORA_BOOTS, ItemType.AURORA_LEGGINGS, ItemType.AURORA_CHESTPLATE, ItemType.AURORA_HELMET, AuroraArcaneEnergyEffect.class),
	BACKWATER(ItemType.BACKWATER_BOOTS, ItemType.BACKWATER_LEGGINGS, ItemType.BACKWATER_CHESTPLATE, ItemType.BACKWATER_HELMET, BackwaterSwampSoldierEffect.class),
	BAT_PERSON(ItemType.BAT_PERSON_BOOTS, ItemType.BAT_PERSON_LEGGINGS, ItemType.BAT_PERSON_CHESTPLATE, ItemType.BAT_PERSON_HELMET, BatPersonBatPowersActivateEffect.class, BatPersonBatPersonEffect.class),
	BERSERKER(ItemType.BERSERKER_BOOTS, ItemType.BERSERKER_LEGGINGS, ItemType.BERSERKER_CHESTPLATE, ItemType.BERSERKER_HELMET, BerserkerBerserkEffect.class),
	BIOHAZARD(null, ItemType.BIOHAZARD_BOOTS, ItemType.BIOHAZARD_LEGGINGS, ItemType.BIOHAZARD_SUIT, ItemType.BIOHAZARD_HELMET),
	BLAZE(ItemType.BLAZE_BOOTS, ItemType.BLAZE_LEGGINGS, ItemType.BLAZE_CHESTPLATE, ItemType.BLAZE_HELMET, BlazeBlazingAuraEffect.class),
	BRONZE_HUNTER(ItemType.BRONZE_HUNTER_BOOTS, ItemType.BRONZE_HUNTER_LEGGINGS, ItemType.BRONZE_HUNTER_CHESTPLATE, ItemType.BRONZE_HUNTER_HELMET, BronzeHunterStarFisherEffect.class, BronzeHunterOdgersBlessingEffect.class, BronzeHunterPeaceTreatyEffect.class),
	CACTUS(ItemType.CACTUS_BOOTS, ItemType.CACTUS_LEGGINGS, ItemType.CACTUS_CHESTPLATE, ItemType.CACTUS_HELMET, CactusDeflectEffect.class),
	CANOPY(null, ItemType.CANOPY_BOOTS, ItemType.CANOPY_LEGGINGS, ItemType.CANOPY_CHESTPLATE, ItemType.CANOPY_HELMET),
	CELESTE(null, ItemType.CELESTE_BOOTS, ItemType.CELESTE_LEGGINGS, ItemType.CELESTE_CHESTPLATE, ItemType.CELESTE_HELMET),
	CHALLENGER(ItemType.CHALLENGER_BOOTS, ItemType.CHALLENGER_LEGGINGS, ItemType.CHALLENGER_CHESTPLATE, ItemType.CHALLENGER_HELMET, ChallengerUnearthedEffect.class, ChallengerMythosNightEffect.class),
	CRIMSON(ItemType.CRIMSON_BOOTS, ItemType.CRIMSON_LEGGINGS, ItemType.CRIMSON_CHESTPLATE, ItemType.CRIMSON_HELMET, CrimsonDominusEffect.class),
	CROPIE(ItemType.CROPIE_BOOTS, ItemType.CROPIE_LEGGINGS, ItemType.CROPIE_CHESTPLATE, ItemType.CROPIE_HELMET, CropieSquashbuckleEffect.class, CropieFarmersGraceEffect.class),
	CRYSTAL(ItemType.CRYSTAL_BOOTS, ItemType.CRYSTAL_LEGGINGS, ItemType.CRYSTAL_CHESTPLATE, ItemType.CRYSTAL_HELMET, CrystalRefractionEffect.class),
	DIAMOND_HUNTER(ItemType.DIAMOND_HUNTER_BOOTS, ItemType.DIAMOND_HUNTER_LEGGINGS, ItemType.DIAMOND_HUNTER_CHESTPLATE, ItemType.DIAMOND_HUNTER_HELMET, DiamondHunterStarFisherEffect.class, DiamondHunterPeaceTreatyEffect.class, DiamondHunterOdgersBlessingEffect.class),
	DIVAN(null, ItemType.DIVAN_BOOTS, ItemType.DIVAN_LEGGINGS, ItemType.DIVAN_CHESTPLATE, ItemType.DIVAN_HELMET),
	DIVER(ItemType.DIVER_BOOTS, ItemType.DIVER_LEGGINGS, ItemType.DIVER_CHESTPLATE, ItemType.DIVER_HELMET, DiverOneWithTheFishEffect.class, DiverDeepSeaDiverEffect.class),
	EMBER(ItemType.EMBER_BOOTS, ItemType.EMBER_LEGGINGS, ItemType.EMBER_CHESTPLATE, ItemType.EMBER_HELMET, EmberNetherLordEffect.class),
	EMERALD(ItemType.EMERALD_ARMOR_BOOTS, ItemType.EMERALD_ARMOR_LEGGINGS, ItemType.EMERALD_ARMOR_CHESTPLATE, ItemType.EMERALD_ARMOR_HELMET, EmeraldTankEffect.class),
	FAIRY(ItemType.FAIRY_BOOTS, ItemType.FAIRY_LEGGINGS, ItemType.FAIRY_CHESTPLATE, ItemType.FAIRY_HELMET, FairyOutfitEffect.class),
	FARM(null, ItemType.FARM_ARMOR_BOOTS, ItemType.FARM_ARMOR_LEGGINGS, ItemType.FARM_ARMOR_CHESTPLATE, ItemType.FARM_ARMOR_HELMET),
	FARMSUIT(null, ItemType.FARM_SUIT_BOOTS, ItemType.FARM_SUIT_LEGGINGS, ItemType.FARM_SUIT_CHESTPLATE, ItemType.FARM_SUIT_HELMET),
	FERMENTO(ItemType.FERMENTO_BOOTS, ItemType.FERMENTO_LEGGINGS, ItemType.FERMENTO_CHESTPLATE, ItemType.FERMENTO_HELMET, FermentoFeastEffect.class, FermentoFarmersGraceEffect.class),
	FERVOR(ItemType.FERVOR_BOOTS, ItemType.FERVOR_LEGGINGS, ItemType.FERVOR_CHESTPLATE, ItemType.FERVOR_HELMET, FervorIntimidateEffect.class, FervorFervorEffect.class, FervorGroundPoundEffect.class),
	FIG(null, ItemType.FIG_BOOTS, ItemType.FIG_LEGGINGS, ItemType.FIG_CHESTPLATE, ItemType.FIG_HELMET),
	FINAL_DESTINATION(ItemType.FINAL_DESTINATION_BOOTS, ItemType.FINAL_DESTINATION_LEGGINGS, ItemType.FINAL_DESTINATION_CHESTPLATE, ItemType.FINAL_DESTINATION_HELMET, FinalDestinationVivaciousDarknessEffect.class),
	FLAME_BREAKER(null, ItemType.FLAME_BREAKER_BOOTS, ItemType.FLAME_BREAKER_LEGGINGS, ItemType.FLAME_BREAKER_CHESTPLATE, ItemType.FLAME_BREAKER_HELMET),
	FLAMEBREAKER(null, ItemType.FLAMEBREAKER_BOOTS, ItemType.FLAMEBREAKER_LEGGINGS, ItemType.FLAMEBREAKER_CHESTPLATE, ItemType.FLAMEBREAKER_HELMET),
	FROZEN_BLAZE(ItemType.FROZEN_BLAZE_BOOTS, ItemType.FROZEN_BLAZE_LEGGINGS, ItemType.FROZEN_BLAZE_CHESTPLATE, ItemType.FROZEN_BLAZE_HELMET, FrozenBlazeFrozenBlazingAuraEffect.class),
	GLACITE(ItemType.GLACITE_BOOTS, ItemType.GLACITE_LEGGINGS, ItemType.GLACITE_CHESTPLATE, ItemType.GLACITE_HELMET, GlaciteExpertMinerEffect.class),
	GLOSSY_MINERAL(ItemType.GLOSSY_MINERAL_BOOTS, ItemType.GLOSSY_MINERAL_LEGGINGS, ItemType.GLOSSY_MINERAL_CHESTPLATE, ItemType.GLOSSY_MINERAL_HELMET, GlossyMineralGlossyMineralworksEffect.class),
	GOBLIN(ItemType.GOBLIN_BOOTS, ItemType.GOBLIN_LEGGINGS, ItemType.GOBLIN_CHESTPLATE, ItemType.GOBLIN_HELMET, GoblinSmartMinerEffect.class),
	GOLD_HUNTER(ItemType.GOLD_HUNTER_BOOTS, ItemType.GOLD_HUNTER_LEGGINGS, ItemType.GOLD_HUNTER_CHESTPLATE, ItemType.GOLD_HUNTER_HELMET, GoldHunterStarFisherEffect.class, GoldHunterOdgersBlessingEffect.class, GoldHunterPeaceTreatyEffect.class),
	GOLEM(ItemType.GOLEM_ARMOR_BOOTS, ItemType.GOLEM_ARMOR_LEGGINGS, ItemType.GOLEM_ARMOR_CHESTPLATE, ItemType.GOLEM_ARMOR_HELMET, GolemAbsorptionEffect.class),
	GREAT_SPOOK(ItemType.GREAT_SPOOK_BOOTS, ItemType.GREAT_SPOOK_LEGGINGS, ItemType.GREAT_SPOOK_CHESTPLATE, ItemType.GREAT_SPOOK_HELMET, GreatSpookSpookyEffect.class),
	GROWTH(ItemType.GROWTH_BOOTS, ItemType.GROWTH_LEGGINGS, ItemType.GROWTH_CHESTPLATE, ItemType.GROWTH_HELMET, GrowthGrowthEffect.class),
	HARDENED_DIAMOND(null, ItemType.HARDENED_DIAMOND_BOOTS, ItemType.HARDENED_DIAMOND_LEGGINGS, ItemType.HARDENED_DIAMOND_CHESTPLATE, ItemType.HARDENED_DIAMOND_HELMET),
	HEAT(ItemType.HEAT_BOOTS, ItemType.HEAT_LEGGINGS, ItemType.HEAT_CHESTPLATE, ItemType.HEAT_HELMET, HeatHeatShieldEffect.class),
	HELIANTHUS(ItemType.HELIANTHUS_BOOTS, ItemType.HELIANTHUS_LEGGINGS, ItemType.HELIANTHUS_CHESTPLATE, ItemType.HELIANTHUS_HELMET, HelianthusFeastEffect.class, HelianthusFarmersGraceEffect.class),
	HOLLOW(ItemType.HOLLOW_BOOTS, ItemType.HOLLOW_LEGGINGS, ItemType.HOLLOW_CHESTPLATE, ItemType.HOLLOW_HELMET, HollowSpiritEffect.class),
	HOLY_DRAGON(ItemType.HOLY_DRAGON_BOOTS, ItemType.HOLY_DRAGON_LEGGINGS, ItemType.HOLY_DRAGON_CHESTPLATE, ItemType.HOLY_DRAGON_HELMET, HolyDragonHolyBloodEffect.class),
	MAGMA(ItemType.ARMOR_OF_MAGMA_BOOTS, ItemType.ARMOR_OF_MAGMA_LEGGINGS, ItemType.ARMOR_OF_MAGMA_CHESTPLATE, ItemType.ARMOR_OF_MAGMA_HELMET, MagmaAbsorbEffect.class),
	MAGMA_LORD(ItemType.MAGMA_LORD_BOOTS, ItemType.MAGMA_LORD_LEGGINGS, ItemType.MAGMA_LORD_CHESTPLATE, ItemType.MAGMA_LORD_HELMET, MagmaLordFireproofEffect.class, MagmaLordLordsBlessingEffect.class),
	MASTIFF(ItemType.MASTIFF_BOOTS, ItemType.MASTIFF_LEGGINGS, ItemType.MASTIFF_CHESTPLATE, ItemType.MASTIFF_HELMET, MastiffAbsoluteUnitEffect.class),
	MELON(ItemType.MELON_BOOTS, ItemType.MELON_LEGGINGS, ItemType.MELON_CHESTPLATE, ItemType.MELON_HELMET, MelonCropierCropsEffect.class, MelonFarmersGraceEffect.class),
	MERCENARY(ItemType.MERCENARY_BOOTS, ItemType.MERCENARY_LEGGINGS, ItemType.MERCENARY_CHESTPLATE, ItemType.MERCENARY_HELMET, MercenaryDeathTaxEffect.class),
	MINERAL(ItemType.MINERAL_BOOTS, ItemType.MINERAL_LEGGINGS, ItemType.MINERAL_CHESTPLATE, ItemType.MINERAL_HELMET, MineralMineralworksEffect.class),
	MONSTER_HUNTER(ItemType.SPIDER_BOOTS, ItemType.CREEPER_LEGGINGS, ItemType.GUARDIAN_CHESTPLATE, ItemType.SKELETON_HELMET, MonsterHunterMonsterHunterEffect.class),
	MONSTER_RAIDER(ItemType.TARANTULA_BOOTS, ItemType.CREEPER_LEGGINGS, ItemType.GUARDIAN_CHESTPLATE, ItemType.SKELETON_HELMET, MonsterRaiderMonsterRaiderEffect.class),
	MYTHOS(ItemType.MYTHOS_BOOTS, ItemType.MYTHOS_LEGGINGS, ItemType.MYTHOS_CHESTPLATE, ItemType.MYTHOS_HELMET, MythosUnearthedEffect.class, MythosMightEffect.class, MythosFamiliarityEffect.class),
	NUTCRACKER(ItemType.NUTCRACKER_BOOTS, ItemType.NUTCRACKER_LEGGINGS, ItemType.NUTCRACKER_CHESTPLATE, ItemType.NUTCRACKER_HELMET, NutcrackerColdThumbEffect.class),
	OLD_DRAGON(ItemType.OLD_DRAGON_BOOTS, ItemType.OLD_DRAGON_LEGGINGS, ItemType.OLD_DRAGON_CHESTPLATE, ItemType.OLD_DRAGON_HELMET, OldDragonOldBloodEffect.class),
	PACK(ItemType.BOOTS_OF_THE_PACK, ItemType.LEGGINGS_OF_THE_PACK, ItemType.CHESTPLATE_OF_THE_PACK, ItemType.HELMET_OF_THE_PACK, PackArmorOfThePackEffect.class),
	PERFECT(null, ItemType.PERFECT_BOOTS_1, ItemType.PERFECT_LEGGINGS_1, ItemType.PERFECT_CHESTPLATE_1, ItemType.PERFECT_HELMET_1),
	POWER_WITHER(ItemType.POWER_WITHER_BOOTS, ItemType.POWER_WITHER_LEGGINGS, ItemType.POWER_WITHER_CHESTPLATE, ItemType.POWER_WITHER_HELMET, PowerWitherWitherbornEffect.class),
	PRIMORDIAL(ItemType.PRIMORDIAL_BOOTS, ItemType.PRIMORDIAL_LEGGINGS, ItemType.PRIMORDIAL_CHESTPLATE, ItemType.PRIMORDIAL_HELMET, PrimordialOctodexterityEffect.class, PrimordialDoubleJumpEffect.class),
	PROTECTOR_DRAGON(ItemType.PROTECTOR_DRAGON_BOOTS, ItemType.PROTECTOR_DRAGON_LEGGINGS, ItemType.PROTECTOR_DRAGON_CHESTPLATE, ItemType.PROTECTOR_DRAGON_HELMET, ProtectorDragonProtectiveBloodEffect.class),
	RABBIT(ItemType.RABBIT_BOOTS, ItemType.RABBIT_LEGGINGS, ItemType.RABBIT_CHESTPLATE, ItemType.RABBIT_HELMET, RabbitSpringsneakEffect.class),
	RAMPART(null, ItemType.RAMPART_BOOTS, ItemType.RAMPART_LEGGINGS, ItemType.RAMPART_CHESTPLATE, ItemType.RAMPART_HELMET),
	REAPER(ItemType.REAPER_BOOTS, ItemType.REAPER_LEGGINGS, ItemType.REAPER_CHESTPLATE, null, ReaperZombieBulwarkEffect.class, ReaperTrollingTheReaperEffect.class, ReaperEnrageEffect.class),
	REKINDLED_EMBER(ItemType.REKINDLED_EMBER_BOOTS, ItemType.REKINDLED_EMBER_LEGGINGS, ItemType.REKINDLED_EMBER_CHESTPLATE, ItemType.REKINDLED_EMBER_HELMET, RekindledEmberRekindleEffect.class),
	RESISTANCE(ItemType.ARMOR_OF_THE_RESISTANCE_BOOTS, ItemType.ARMOR_OF_THE_RESISTANCE_LEGGINGS, ItemType.ARMOR_OF_THE_RESISTANCE_CHESTPLATE, ItemType.ARMOR_OF_THE_RESISTANCE_HELMET, ResistancePowerOfTheResistanceEffect.class),
	REVENANT(ItemType.REVENANT_BOOTS, ItemType.REVENANT_LEGGINGS, ItemType.REVENANT_CHESTPLATE, null, RevenantZombieBulwarkEffect.class, RevenantTrollingTheReaperEffect.class),
	ROSETTA(null, ItemType.ROSETTA_BOOTS, ItemType.ROSETTA_LEGGINGS, ItemType.ROSETTA_CHESTPLATE, ItemType.ROSETTA_HELMET),
	SALMON(ItemType.SALMON_BOOTS_NEW, ItemType.SALMON_LEGGINGS_NEW, ItemType.SALMON_CHESTPLATE_NEW, ItemType.SALMON_HELMET_NEW, SalmonWaterBurstEffect.class),
	SHARK_SCALE(ItemType.SHARK_SCALE_BOOTS, ItemType.SHARK_SCALE_LEGGINGS, ItemType.SHARK_SCALE_CHESTPLATE, ItemType.SHARK_SCALE_HELMET, SharkScaleAbsorbEffect.class, SharkScaleFestivalFisherEffect.class),
	SHIMMERING_LIGHT(ItemType.SHIMMERING_LIGHT_SLIPPERS, ItemType.SHIMMERING_LIGHT_TROUSERS, ItemType.SHIMMERING_LIGHT_TUNIC, ItemType.SHIMMERING_LIGHT_HOOD, ShimmeringLightShimmerEffect.class),
	SILVER_HUNTER(ItemType.SILVER_HUNTER_BOOTS, ItemType.SILVER_HUNTER_LEGGINGS, ItemType.SILVER_HUNTER_CHESTPLATE, ItemType.SILVER_HUNTER_HELMET, SilverHunterStarFisherEffect.class, SilverHunterOdgersBlessingEffect.class, SilverHunterPeaceTreatyEffect.class),
	SNORKELING(ItemType.SNORKELING_BOOTS, ItemType.SNORKELING_LEGGINGS, ItemType.SNORKELING_CHESTPLATE, ItemType.SNORKELING_HELMET, SnorkelingLongTubaEffect.class),
	SNOW_SUIT(ItemType.SNOW_SUIT_BOOTS, ItemType.SNOW_SUIT_LEGGINGS, ItemType.SNOW_SUIT_CHESTPLATE, ItemType.SNOW_SUIT_HELMET, SnowSuitColdThumbEffect.class, SnowSuitUnlimitedSnowballsEffect.class),
	SORROW(ItemType.SORROW_BOOTS, ItemType.SORROW_LEGGINGS, ItemType.SORROW_CHESTPLATE, ItemType.SORROW_HELMET, SorrowMistAuraEffect.class),
	SPEED_WITHER(ItemType.SPEED_WITHER_BOOTS, ItemType.SPEED_WITHER_LEGGINGS, ItemType.SPEED_WITHER_CHESTPLATE, ItemType.SPEED_WITHER_HELMET, SpeedWitherWitherbornEffect.class),
	SPONGE(ItemType.SPONGE_BOOTS, ItemType.SPONGE_LEGGINGS, ItemType.SPONGE_CHESTPLATE, ItemType.SPONGE_HELMET, SpongeAbsorbEffect.class),
	SPOOKY(ItemType.SPOOKY_BOOTS, ItemType.SPOOKY_LEGGINGS, ItemType.SPOOKY_CHESTPLATE, ItemType.SPOOKY_HELMET, SpookyCandyManEffect.class),
	SPROUT(null, ItemType.SPROUT_BOOTS, ItemType.SPROUT_LEGGINGS, ItemType.SPROUT_CHESTPLATE, ItemType.SPROUT_HELMET),
	SQUASH(ItemType.SQUASH_BOOTS, ItemType.SQUASH_LEGGINGS, ItemType.SQUASH_CHESTPLATE, ItemType.SQUASH_HELMET, SquashMentoFermentoEffect.class, SquashFarmersGraceEffect.class),
	SQUIRE(null, ItemType.SQUIRE_BOOTS, ItemType.SQUIRE_LEGGINGS, ItemType.SQUIRE_CHESTPLATE, ItemType.SQUIRE_HELMET),
	STARLIGHT(ItemType.STARLIGHT_BOOTS, ItemType.STARLIGHT_LEGGINGS, ItemType.STARLIGHT_CHESTPLATE, ItemType.STARLIGHT_HELMET, StarlightStarpowerEffect.class),
	STRONG_DRAGON(ItemType.STRONG_DRAGON_BOOTS, ItemType.STRONG_DRAGON_LEGGINGS, ItemType.STRONG_DRAGON_CHESTPLATE, ItemType.STRONG_DRAGON_HELMET, StrongDragonStrongBloodEffect.class),
	SUPERIOR_DRAGON(ItemType.SUPERIOR_DRAGON_BOOTS, ItemType.SUPERIOR_DRAGON_LEGGINGS, ItemType.SUPERIOR_DRAGON_CHESTPLATE, ItemType.SUPERIOR_DRAGON_HELMET, SuperiorDragonSuperiorBloodEffect.class),
	TANK_WITHER(ItemType.TANK_WITHER_BOOTS, ItemType.TANK_WITHER_LEGGINGS, ItemType.TANK_WITHER_CHESTPLATE, ItemType.TANK_WITHER_HELMET, TankWitherWitherbornEffect.class),
	TARANTULA(ItemType.TARANTULA_BOOTS, ItemType.TARANTULA_LEGGINGS, ItemType.TARANTULA_CHESTPLATE, ItemType.TARANTULA_HELMET, TarantulaOctodexterityEffect.class, TarantulaDoubleJumpEffect.class),
	TERROR(ItemType.TERROR_BOOTS, ItemType.TERROR_LEGGINGS, ItemType.TERROR_CHESTPLATE, ItemType.TERROR_HELMET, TerrorHydraStrikeEffect.class),
	THERMODYNAMIC(ItemType.THERMODYNAMIC_BOOTS, ItemType.THERMODYNAMIC_LEGGINGS, ItemType.THERMODYNAMIC_CHESTPLATE, ItemType.THERMODYNAMIC_HELMET, ThermodynamicNewtonsDemiseEffect.class),
	THUNDER(ItemType.THUNDER_BOOTS, ItemType.THUNDER_LEGGINGS, ItemType.THUNDER_CHESTPLATE, ItemType.THUNDER_HELMET, ThunderStaticChargeEffect.class),
	TROPHY_HUNTER(null, ItemType.SLUG_BOOTS, ItemType.MOOGMA_LEGGINGS, ItemType.FLAMING_CHESTPLATE, ItemType.TAURUS_HELMET),
	UNSTABLE_DRAGON(ItemType.UNSTABLE_DRAGON_BOOTS, ItemType.UNSTABLE_DRAGON_LEGGINGS, ItemType.UNSTABLE_DRAGON_CHESTPLATE, ItemType.UNSTABLE_DRAGON_HELMET, UnstableDragonUnstableBloodEffect.class),
	VELVET(null, ItemType.OXFORD_SHOES, ItemType.SATIN_TROUSERS, ItemType.CASHMERE_JACKET, ItemType.VELVET_TOP_HAT),
	WEREWOLF(ItemType.WEREWOLF_BOOTS, ItemType.WEREWOLF_LEGGINGS, ItemType.WEREWOLF_CHESTPLATE, ItemType.WEREWOLF_HELMET, WerewolfRegenerativeHowlEffect.class),
	WISE_DRAGON(ItemType.WISE_DRAGON_BOOTS, ItemType.WISE_DRAGON_LEGGINGS, ItemType.WISE_DRAGON_CHESTPLATE, ItemType.WISE_DRAGON_HELMET, WiseDragonWiseBloodEffect.class),
	WISE_WITHER(ItemType.WISE_WITHER_BOOTS, ItemType.WISE_WITHER_LEGGINGS, ItemType.WISE_WITHER_CHESTPLATE, ItemType.WISE_WITHER_HELMET, WiseWitherWitherbornEffect.class),
	WITHER(ItemType.WITHER_BOOTS, ItemType.WITHER_LEGGINGS, ItemType.WITHER_CHESTPLATE, ItemType.WITHER_HELMET, WitherWitherbornEffect.class),
	WYLD(ItemType.WYLD_BOOTS, ItemType.WYLD_LEGGINGS, ItemType.WYLD_CHESTPLATE, ItemType.WYLD_HELMET, WyldHelmetsAllTheWayDownEffect.class),
	YOG(ItemType.ARMOR_OF_YOG_BOOTS, ItemType.ARMOR_OF_YOG_LEGGINGS, ItemType.ARMOR_OF_YOG_CHESTPLATE, ItemType.ARMOR_OF_YOG_HELMET, YogAbsorbEffect.class),
	YOUNG_DRAGON(ItemType.YOUNG_DRAGON_BOOTS, ItemType.YOUNG_DRAGON_LEGGINGS, ItemType.YOUNG_DRAGON_CHESTPLATE, ItemType.YOUNG_DRAGON_HELMET, YoungDragonYoungBloodEffect.class),
	ZOMBIE(ItemType.ZOMBIE_BOOTS, ItemType.ZOMBIE_LEGGINGS, ItemType.ZOMBIE_CHESTPLATE, null, ZombieProjectileAbsorptionEffect.class),
	;

	private final Class<? extends ArmorSet> clazz;
	private final ItemType boots;
	private final ItemType leggings;
	private final ItemType chestplate;
	private final ItemType helmet;
	private final List<ArmorSetEffect> effects;

	@SafeVarargs
	ArmorSetRegistry(ItemType boots, ItemType legging, ItemType chestplate, ItemType helmet,
					 Class<? extends ArmorSetEffect>... effects) {
		this(null, boots, legging, chestplate, helmet, effects);
	}

	@SafeVarargs
	ArmorSetRegistry(Class<? extends ArmorSet> clazz, ItemType boots, ItemType legging,
					 ItemType chestplate, ItemType helmet, Class<? extends ArmorSetEffect>... effects) {
		this.clazz = clazz;
		this.boots = boots;
		this.leggings = legging;
		this.chestplate = chestplate;
		this.helmet = helmet;
		this.effects = instantiateEffects(effects);
	}

	private static List<ArmorSetEffect> instantiateEffects(Class<? extends ArmorSetEffect>[] effectClasses) {
		List<ArmorSetEffect> effects = new ArrayList<>(effectClasses.length);
		for (Class<? extends ArmorSetEffect> effectClass : effectClasses) {
			try {
				effects.add(effectClass.getDeclaredConstructor().newInstance());
			} catch (ReflectiveOperationException exception) {
				throw new IllegalStateException("Unable to create armor effect " + effectClass.getSimpleName(), exception);
			}
		}
		return List.copyOf(effects);
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
		if (clazz == null) return new RegisteredArmorSet(this, effects);
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

	public int getPieceCount() {
		return getPieceCount(this);
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

	public static List<ArmorSetRegistry> getArmorSets(@Nullable ItemType item) {
		if (item == null) return List.of();
		List<ArmorSetRegistry> armorSets = new ArrayList<>();
		for (ArmorSetRegistry armorSetRegistry : values()) {
			if (armorSetRegistry.getItemTypes().contains(item)) armorSets.add(armorSetRegistry);
		}
		return armorSets;
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

    public int getWornPieceCount(java.util.Set<ItemType> wornItems) {
        int count = 0;
        if (boots != null && wornItems.contains(boots)) count++;
        if (leggings != null && wornItems.contains(leggings)) count++;
        if (chestplate != null && wornItems.contains(chestplate)) count++;
        if (helmet != null && wornItems.contains(helmet)) count++;
        return count;
    }
}
