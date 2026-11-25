package net.swofty.type.skyblockgeneric.user.statistics;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.kyori.adventure.bossbar.BossBar;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.timer.ExecutionType;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.PotatoType;
import net.swofty.commons.skyblock.item.attribute.attributes.ItemAttributeHotPotatoBookData;
import net.swofty.commons.skyblock.item.attribute.attributes.ItemAttributeRuneInfusedWith;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.generic.data.datapoints.DatapointStringList;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.bestiary.BestiaryData;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointInventory;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSkills;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSkyBlockExperience;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import net.swofty.type.skyblockgeneric.enchantment.abstr.EventBasedEnchant;
import net.swofty.type.skyblockgeneric.enchantment.debuff.LethalityDebuff;
import net.swofty.type.skyblockgeneric.entity.mob.BestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import net.swofty.type.skyblockgeneric.event.value.events.RegenerationValueUpdateEvent;
import net.swofty.type.skyblockgeneric.gems.Gemstone;
import net.swofty.type.skyblockgeneric.hunting.AttributeEffectService;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.ConstantStatisticsComponent;
import net.swofty.type.skyblockgeneric.item.components.PetComponent;
import net.swofty.type.skyblockgeneric.item.components.SkullHeadComponent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.components.StandardItemComponent;
import net.swofty.type.skyblockgeneric.item.set.ArmorSetRegistry;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSet;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import net.swofty.type.skyblockgeneric.levels.unlocks.SkyBlockLevelStatisticUnlock;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.SkyBlockProgressMission;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.skill.SkillCategory;
import net.swofty.type.skyblockgeneric.user.SkyBlockActionBar;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.lang.reflect.Constructor;
import java.util.*;

public class PlayerStatistics {
    private static final Map<Player, BossBar> barCache = new HashMap<>();

    private final SkyBlockPlayer player;
    @Setter
    @Getter
    private double healthRegenerationPercentBonus;
    @Setter
    @Getter
    private double manaRegenerationPercentBonus;
    private ItemStatistics accessoryStatistics = ItemStatistics.builder().build();
    private final List<TemporaryStatistic> temporaryStatistics = Collections.synchronizedList(new ArrayList<>());
    private final List<TemporaryConditionalStatistic> temporaryConditionalStatistics = Collections.synchronizedList(new ArrayList<>());
    BestiaryData bestiaryData = new BestiaryData();

    public PlayerStatistics(SkyBlockPlayer player) {
        this.player = player;
    }

    public @Nullable SkyBlockItem getItemWithRune(ItemType runeType) {
        List<SkyBlockItem> piecesToCheck = getPossibleRuneItems();

        for (SkyBlockItem item : piecesToCheck) {
            if (item == null) continue;
            ItemAttributeRuneInfusedWith.RuneData runeData = item.getAttributeHandler().getRuneData();
            if (!runeData.hasRune()) continue;
            if (runeData.getRuneType() == runeType) continue;

            return item;
        }

        return null;
    }

    private List<SkyBlockItem> getPossibleRuneItems() {
        ArrayList<SkyBlockItem> piecesToCheck = new ArrayList<>();
        PlayerItemOrigin.OriginCache cache = PlayerItemOrigin.getFromCache(player.getUuid());

        piecesToCheck.add(getMainHandItem());
        piecesToCheck.add(cache.get(PlayerItemOrigin.HELMET));
        piecesToCheck.add(cache.get(PlayerItemOrigin.CHESTPLATE));
        piecesToCheck.add(cache.get(PlayerItemOrigin.LEGGINGS));
        piecesToCheck.add(cache.get(PlayerItemOrigin.BOOTS));

        return piecesToCheck;
    }

    public ItemStatistics allArmorStatistics(SkyBlockPlayer causer, LivingEntity enemy) {
        PlayerItemOrigin.OriginCache cache = PlayerItemOrigin.getFromCache(player.getUuid());
        ArrayList<SkyBlockItem> armorPieces = new ArrayList<>();

        armorPieces.add(cache.get(PlayerItemOrigin.HELMET));
        armorPieces.add(cache.get(PlayerItemOrigin.CHESTPLATE));
        armorPieces.add(cache.get(PlayerItemOrigin.LEGGINGS));
        armorPieces.add(cache.get(PlayerItemOrigin.BOOTS));

        ItemStatistics total = ItemStatistics.builder().build();
        for (SkyBlockItem item : armorPieces) {
            if (item.hasComponent(ConstantStatisticsComponent.class))
                continue;

            total = ItemStatistics.add(total, ItemStatistics.add(item.getAttributeHandler().getStatistics(), calculateExtraItemStatisticsToAdd(
                    item,
                    causer,
                    enemy
            )));
        }
        if (player.getArmorSet() != null) {
            ArmorSetRegistry armorSetRegistry = player.getArmorSet();
            try {
                Constructor<? extends ArmorSet> constructor = armorSetRegistry.getClazz().getConstructor();
                ArmorSet armorSet = constructor.newInstance();
                total = ItemStatistics.add(total, armorSet.getStatistics());
            } catch (Exception _) {
            }
        }
        return total;
    }

    // Returns a map of Level : Amount of that level
    public @NonNull Map<Integer, Integer> getAllOfEnchants(EnchantmentType enchantmentType,
                                                           boolean includeAccessories) {
        List<SkyBlockItem> toCheck = new ArrayList<>();
        PlayerItemOrigin.OriginCache cache = PlayerItemOrigin.getFromCache(player.getUuid());

        toCheck.add(getMainHandItem());
        toCheck.add(cache.get(PlayerItemOrigin.HELMET));
        toCheck.add(cache.get(PlayerItemOrigin.CHESTPLATE));
        toCheck.add(cache.get(PlayerItemOrigin.LEGGINGS));
        toCheck.add(cache.get(PlayerItemOrigin.BOOTS));

        if (includeAccessories) {
            toCheck.addAll(player.getAccessoryBag().getAllAccessories());
        }

        Map<Integer, Integer> enchantLevels = new HashMap<>();

        for (SkyBlockItem item : toCheck) {
            if (item == null) continue;

            for (SkyBlockEnchantment enchantment : item.getAttributeHandler().getEnchantments().toList()) {
                if (enchantment.type() != enchantmentType) continue;

                int level = enchantment.level();
                enchantLevels.merge(level, 1, Integer::sum);
            }
        }

        return enchantLevels;
    }

    public ItemStatistics mainHandStatistics(SkyBlockPlayer causer, LivingEntity enemy) {
        SkyBlockItem item = getMainHandItem();

        if (item.hasComponent(ConstantStatisticsComponent.class))
            return ItemStatistics.empty();
        if (item.hasComponent(StandardItemComponent.class)) {
            StandardItemComponent standardItem = item.getComponent(StandardItemComponent.class);
            if (standardItem.getType().isArmor())
                return ItemStatistics.empty();
        }

        return ItemStatistics.add(item.getAttributeHandler().getStatistics(),
                calculateExtraItemStatisticsToAdd(
                        item,
                        causer,
                        enemy
                ));
    }

    public ItemStatistics petStatistics(@Nullable LivingEntity entity) {
        ItemStatistics stats = ItemStatistics.empty();
        for (SkyBlockItem pet : player.getPetData().getActivePets()) {
            stats = ItemStatistics.add(stats, petStats(pet, entity));
        }
        return stats;
    }

    private ItemStatistics petStats(SkyBlockItem pet, @Nullable LivingEntity entity) {
        ItemStatistics stats = ItemStatistics.empty();
        if (player.getPetData().getEnabledPet() == pet) {
            PetComponent component = pet.getComponent(PetComponent.class);
            ItemStatistics baseStatistics = component.getBaseStatistics();
            ItemStatistics perLevelStatistics = component.getPerLevelStatistics(
                    pet.getAttributeHandler().getRarity()
            );
            int level = pet.getAttributeHandler().getPetData().getAsLevel(pet.getAttributeHandler().getRarity());
            stats = ItemStatistics.add(baseStatistics, ItemStatistics.multiply(perLevelStatistics, level));
        }
        for (PetAbility ability : player.getPetData().getAbilities(pet)) {
            stats = ItemStatistics.add(stats, ability.getStatistics(player, pet, entity));
        }
        return stats;
    }

    public ItemStatistics spareStatistics() {
        ItemStatistics spare = ItemStatistics.builder().build();

        int fairySouls = player.getFairySouls().getExchangedFairySouls().size();
        spare = ItemStatistics.add(spare, ItemStatistics.builder().withBase(ItemStatistic.HEALTH, (double) (fairySouls * 2)).build());

        DatapointSkills.PlayerSkills skills = player.getSkills();
        spare = ItemStatistics.add(spare, skills.getSkillStatistics());

        DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = player.getSkyBlockExperience();
        for (int i = 1; i <= experience.getLevel().asInt(); i++) {
            var requirement = net.swofty.type.skyblockgeneric.levels.SkyBlockLevelRequirement.getLevel(i);
            if (requirement == null) continue;
            List<SkyBlockLevelStatisticUnlock> unlocks = requirement.getStatisticUnlocks();
            for (SkyBlockLevelStatisticUnlock unlock : unlocks) {
                spare = ItemStatistics.add(spare, unlock.getStatistics());
            }
        }

        return spare;
    }

    public long getInvulnerabilityTime() {
        double bonusAttackSpeed = allStatistics().getOverall(ItemStatistic.BONUS_ATTACK_SPEED);
        return (long) (10 / (1 + (bonusAttackSpeed / 100)));
    }


    public ItemStatistics allStatistics() {
        return allStatistics(null, null);
    }

    public ItemStatistics allStatistics(SkyBlockPlayer causer, LivingEntity enemy) {
        ItemStatistics base = allNonPetStatistics(causer, enemy);
        return ItemStatistics.add(base, petStatistics(enemy));
    }

    public ItemStatistics allNonPetStatistics(SkyBlockPlayer causer, LivingEntity enemy) {
        ItemStatistics base = ItemStatistics.builder().build();
        if (enemy instanceof BestiaryMob bestiaryMob) base = ItemStatistics.add(base, getBestiaryStatistics(causer, bestiaryMob));
        base = ItemStatistics.add(base, allArmorStatistics(causer, enemy));
        base = ItemStatistics.add(base, equipmentStatistics(causer, enemy));
        base = ItemStatistics.add(base, mainHandStatistics(causer, enemy));
        base = ItemStatistics.add(base, spareStatistics());
        base = ItemStatistics.add(base, getTemporaryStatistics());
        base = ItemStatistics.add(base, accessoryStatistics);
        base = ItemStatistics.add(base, AttributeEffectService.statistics(player));
        base = ItemStatistics.add(base, ItemStatistic.getOfAllBaseValues());
        return base;
    }

    public List<StatisticSource> statisticSources() {
        Map<String, List<StatisticModifier>> grouped = new java.util.LinkedHashMap<>();
        for (StatisticModifier modifier : statisticModifiers()) {
            String key = modifier.sourceType().name() + "\0" +
                (modifier.parentName() == null ? modifier.name() : modifier.parentName());
            grouped.computeIfAbsent(key, ignored -> new ArrayList<>()).add(modifier);
        }

        List<StatisticSource> sources = new ArrayList<>();
        for (List<StatisticModifier> modifiers : grouped.values()) {
            StatisticModifier first = modifiers.getFirst();
            ItemStatistics total = ItemStatistics.empty();
            for (StatisticModifier modifier : modifiers) total = ItemStatistics.add(total, modifier.statistics());
            String name = first.parentName() == null ? first.name() : first.parentName();
            sources.add(new StatisticSource(name, first.material(), first.texture(), total,
                first.sourceType(), List.copyOf(modifiers)));
        }
        return sources;
    }

    public List<StatisticModifier> statisticModifiers() {
        List<StatisticModifier> modifiers = new ArrayList<>();
        PlayerItemOrigin.OriginCache cache = PlayerItemOrigin.getFromCache(player.getUuid());
        addItemModifiers(modifiers, cache.get(PlayerItemOrigin.HELMET), StatisticSourceType.ARMOR);
        addItemModifiers(modifiers, cache.get(PlayerItemOrigin.CHESTPLATE), StatisticSourceType.ARMOR);
        addItemModifiers(modifiers, cache.get(PlayerItemOrigin.LEGGINGS), StatisticSourceType.ARMOR);
        addItemModifiers(modifiers, cache.get(PlayerItemOrigin.BOOTS), StatisticSourceType.ARMOR);
        if (player.getArmorSet() != null) {
            try {
                ArmorSet armorSet = player.getArmorSet().getClazz().getConstructor().newInstance();
                addModifier(modifiers, player.getArmorSet().getDisplayName() + " Set Bonus",
                    armorSet.getStatistics(), StatisticSourceType.ARMOR,
                    StatisticModifierType.ABILITY_PASSIVE, null, Material.IRON_CHESTPLATE, null);
            } catch (ReflectiveOperationException ignored) {
            }
        }

        DatapointInventory inventory = player.getSkyblockDataHandler()
            .get(SkyBlockDataHandler.Data.INVENTORY, DatapointInventory.class);
        for (net.swofty.commons.skyblock.item.UnderstandableSkyBlockItem serialized : List.of(
            inventory.getValue().getNecklace(), inventory.getValue().getCloak(),
            inventory.getValue().getBelt(), inventory.getValue().getGloves())) {
            addItemModifiers(modifiers, new SkyBlockItem(serialized), StatisticSourceType.EQUIPMENT);
        }

        addItemModifiers(modifiers, getMainHandItem(), StatisticSourceType.HELD_ITEM);
        addProgressionModifiers(modifiers);
        addModifier(modifiers, "Attributes", AttributeEffectService.statistics(player),
                StatisticSourceType.ATTRIBUTE, StatisticModifierType.ATTRIBUTE, null, Material.PRISMARINE_SHARD, null);
        addTemporaryModifiers(modifiers);

        for (SkyBlockItem pet : player.getPetData().getActivePets()) {
            addModifier(modifiers, pet.getDisplayName(), petStats(pet, null),
                StatisticSourceType.PET, StatisticModifierType.BASIC, null, pet.getMaterial(), texture(pet));
        }

        Set<ItemType> usedAccessories = new HashSet<>();
        for (ItemStack stack : player.getInventory().getItemStacks()) {
            if (!SkyBlockItem.isSkyBlockItem(stack)) continue;
            SkyBlockItem accessory = new SkyBlockItem(stack);
            addAccessoryModifiers(modifiers, accessory, usedAccessories);
        }
        for (SkyBlockItem accessory : player.getAccessoryBag().getAllAccessories()) {
            addAccessoryModifiers(modifiers, accessory, usedAccessories);
        }
        modifiers.add(new StatisticModifier("Base Value", Material.NETHER_STAR, null,
            ItemStatistic.getOfAllBaseValues(), StatisticSourceType.INNATE, StatisticModifierType.INNATE, null));
        return modifiers;
    }

    /**
     * the updater skips the main hand while a menu is open, which causes
     * stat menus displaying wrong stats.
     */
    private SkyBlockItem getMainHandItem() {
        return new SkyBlockItem(player.getInventory().getItemStack(player.getHeldSlot()));
    }

    private void addAccessoryModifiers(List<StatisticModifier> modifiers, SkyBlockItem item, Set<ItemType> used) {
        if (item == null || !item.hasComponent(ConstantStatisticsComponent.class)) return;
        ItemType type = item.getAttributeHandler().getPotentialType();
        if (type != null && !used.add(type)) return;
        addItemModifiers(modifiers, item, StatisticSourceType.ACCESSORY_BAG);
    }

    private void addItemModifiers(List<StatisticModifier> modifiers, SkyBlockItem item, StatisticSourceType sourceType) {
        if (item == null || item.isAir()
            || (item.hasComponent(ConstantStatisticsComponent.class)
            && sourceType != StatisticSourceType.ACCESSORY_BAG)) return;
        String parent = item.getDisplayName();
        Material material = item.getMaterial();
        String texture = texture(item);
        addModifier(modifiers, "Base Value", item.getAttributeHandler().getBaseStatistics(),
            sourceType, StatisticModifierType.BASIC, parent, material, texture);
        addModifier(modifiers, parent + " Passive", item.getAttributeHandler().getExtraDynamicStatistics(),
            sourceType, StatisticModifierType.ABILITY_PASSIVE, parent, material, texture);

        if (item.getAttributeHandler().getReforge() != null) {
            addModifier(modifiers, item.getAttributeHandler().getReforge().getName() + " Reforge",
                getReforgeStatistics(item, ItemStatistics.empty()), sourceType,
                StatisticModifierType.REFORGE, parent, material, texture);
        }
        addModifier(modifiers, "Gemstones", getGemstoneStatistics(item, ItemStatistics.empty()),
            sourceType, StatisticModifierType.GEMSTONE, parent, material, texture);
        for (SkyBlockEnchantment enchantment : item.getAttributeHandler().getEnchantments().toList()) {
            String name = enchantment.type().getName() + " " + StringUtility.getAsRomanNumeral(enchantment.level());
            addModifier(modifiers, name, enchantment.type().getEnch().getStatistics(enchantment.level()),
                sourceType, StatisticModifierType.ENCHANTMENT, parent, material, texture);
        }
        addModifier(modifiers, "Hot Potato Books", getHotPotatoBookStatistics(item, ItemStatistics.empty()),
            sourceType, StatisticModifierType.HOT_POTATO_BOOK, parent, material, texture);
    }

    private void addProgressionModifiers(List<StatisticModifier> modifiers) {
        DatapointSkills.PlayerSkills skills = player.getSkills();
        ItemStatistics reconstructedSkills = ItemStatistics.empty();
        for (SkillCategories category : SkillCategories.values()) {
            ItemStatistics total = ItemStatistics.empty();
            SkillCategory skill = category.asCategory();
            int level = skills.getCurrentLevel(category);
            for (int current = 1; current <= level; current++) {
                for (SkillCategory.Reward reward : skill.getReward(current).unlocks()) {
                    if (reward instanceof SkillCategory.BaseStatisticReward base) {
                        total = total.addBase(base.getStatistic(), base.amountAdded());
                    } else if (reward instanceof SkillCategory.AdditivePercentageStatisticReward additive) {
                        total = total.addAdditive(additive.getStatistic(), additive.amountAdded());
                    }
                }
            }
            reconstructedSkills = ItemStatistics.add(reconstructedSkills, total);
            addModifier(modifiers, category + " Skill", total, StatisticSourceType.SKILL,
                StatisticModifierType.SKILL, null, skill.getDisplayIcon(), null);
        }
        addModifier(modifiers, "Skill Progression", skills.getSkillStatistics().sub(reconstructedSkills),
            StatisticSourceType.SKILL, StatisticModifierType.SKILL, null, Material.CRAFTING_TABLE, null);

        ItemStatistics levelStatistics = ItemStatistics.empty();
        DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = player.getSkyBlockExperience();
        for (int level = 1; level <= experience.getLevel().asInt(); level++) {
            var requirement = net.swofty.type.skyblockgeneric.levels.SkyBlockLevelRequirement.getLevel(level);
            if (requirement == null) continue;
            for (SkyBlockLevelStatisticUnlock unlock : requirement.getStatisticUnlocks()) {
                levelStatistics = ItemStatistics.add(levelStatistics, unlock.getStatistics());
            }
        }
        addModifier(modifiers, "SkyBlock Level", levelStatistics, StatisticSourceType.SKYBLOCK_LEVELS,
            StatisticModifierType.PROGRESSION, null, Material.NETHER_STAR, null);

        ItemStatistics fairySouls = ItemStatistics.builder().withBase(ItemStatistic.HEALTH,
            (double) player.getFairySouls().getExchangedFairySouls().size() * 2D).build();
        addModifier(modifiers, "Fairy Souls", fairySouls, StatisticSourceType.FAIRY_SOULS,
            StatisticModifierType.PROGRESSION, null, Material.PLAYER_HEAD, null);
    }

    private void addTemporaryModifiers(List<StatisticModifier> modifiers) {
        synchronized (temporaryStatistics) {
            temporaryStatistics.removeIf(stat -> stat.getExpiration() < System.currentTimeMillis());
            for (TemporaryStatistic temporary : temporaryStatistics) {
                String name = temporary.getDisplayName() == null ? "Temporary Buff" : temporary.getDisplayName();
                addModifier(modifiers, name, temporary.getStatistics(), StatisticSourceType.TEMPORARY_BUFF,
                    StatisticModifierType.BUFF, null, Material.POTION, null);
            }
            temporaryConditionalStatistics.removeIf(temporary -> !temporary.getExpiry().apply(player));
            for (TemporaryConditionalStatistic temporary : temporaryConditionalStatistics) {
                addModifier(modifiers, "Conditional Buff", temporary.getStatistics().apply(player),
                    StatisticSourceType.TEMPORARY_BUFF, StatisticModifierType.BUFF, null, Material.POTION, null);
            }
        }
    }

    private static void addModifier(List<StatisticModifier> modifiers, String name, ItemStatistics statistics,
                                    StatisticSourceType sourceType, StatisticModifierType modifierType,
                                    String parent, Material material, String texture) {
        if (statistics.toString().isEmpty()) return;
        modifiers.add(new StatisticModifier(name, material, texture, statistics, sourceType, modifierType, parent));
    }

    private static String texture(SkyBlockItem item) {
        return item.hasComponent(SkullHeadComponent.class)
            ? item.getComponent(SkullHeadComponent.class).getSkullTexture(item) : null;
    }

    public record StatisticSource(String name, Material material, String texture, ItemStatistics statistics,
                                  StatisticSourceType sourceType, List<StatisticModifier> modifiers) {
    }

    private ItemStatistics equipmentStatistics(SkyBlockPlayer causer, LivingEntity enemy) {
        DatapointInventory inventory = player.getSkyblockDataHandler()
            .get(SkyBlockDataHandler.Data.INVENTORY, DatapointInventory.class);
        List<net.swofty.commons.skyblock.item.UnderstandableSkyBlockItem> equipment = List.of(
            inventory.getValue().getNecklace(),
            inventory.getValue().getCloak(),
            inventory.getValue().getBelt(),
            inventory.getValue().getGloves()
        );

        ItemStatistics total = ItemStatistics.empty();
        for (net.swofty.commons.skyblock.item.UnderstandableSkyBlockItem serialized : equipment) {
            SkyBlockItem item = new SkyBlockItem(serialized);
            if (item.isAir()) continue;
            total = ItemStatistics.add(total, ItemStatistics.add(
                item.getAttributeHandler().getStatistics(),
                calculateExtraItemStatisticsToAdd(item, causer, enemy)
            ));
        }
        return total;
    }

    public void updateAccessoryStatistics() {
        List<ItemType> usedAccessories = new ArrayList<>();
        ItemStatistics total = ItemStatistics.builder().build();
        for (ItemStack itemStack : player.getInventory().getItemStacks()) {
            if (SkyBlockItem.isSkyBlockItem(itemStack)) {
                SkyBlockItem item = new SkyBlockItem(itemStack);
                if (!(item.hasComponent(ConstantStatisticsComponent.class))) continue;
                if (usedAccessories.contains(item.getAttributeHandler().getPotentialType())) continue;

                usedAccessories.add(item.getAttributeHandler().getPotentialType());
                total = ItemStatistics.add(total, ItemStatistics.add(item.getAttributeHandler().getStatistics(),
                        calculateExtraItemStatisticsToAdd(
                            item,
                            null,
                            null
                        )));
            }
        }
        for (SkyBlockItem item : player.getAccessoryBag().getAllAccessories()) {
            if (item == null) continue;
            if (!(item.hasComponent(ConstantStatisticsComponent.class))) continue;
            if (usedAccessories.contains(item.getAttributeHandler().getPotentialType())) continue;

            usedAccessories.add(item.getAttributeHandler().getPotentialType());
            total = ItemStatistics.add(total, ItemStatistics.add(item.getAttributeHandler().getStatistics(),
                    calculateExtraItemStatisticsToAdd(
                        item,
                        null,
                        null
            )));
        }
        accessoryStatistics = total;
    }

    private ItemStatistics calculateExtraItemStatisticsToAdd(SkyBlockItem item, SkyBlockPlayer causer, LivingEntity enemy) {
        ItemStatistics statistics = ItemStatistics.builder().build();
        statistics = getReforgeStatistics(item, statistics);
        statistics = getGemstoneStatistics(item, statistics);
        statistics = getEnchantStatistics(item, statistics, causer, enemy);
        statistics = getHotPotatoBookStatistics(item, statistics);

        return statistics;
    }

    private ItemStatistics getReforgeStatistics(SkyBlockItem item, ItemStatistics statistics) {
        if (item.getAttributeHandler().getReforge() != null) {
            statistics = item.getAttributeHandler().getReforge().getAfterCalculation(statistics,
                    item.getAttributeHandler().getRarity().ordinal() + 1);
        }
        return statistics;
    }

    private ItemStatistics getHotPotatoBookStatistics(SkyBlockItem item, ItemStatistics statistics) {
        ItemAttributeHotPotatoBookData.HotPotatoBookData hotPotatoBookData = item.getAttributeHandler().getHotPotatoBookData();
        if (hotPotatoBookData.hasAppliedItem()) {
            ItemStatistics.Builder toAdd = ItemStatistics.builder();
            PotatoType potatoType = hotPotatoBookData.getPotatoType();

            int appliedBooks = hotPotatoBookData.getTotalAmount();
            potatoType.stats.forEach((statistic, amount) ->
                    toAdd.withBase(statistic, amount * appliedBooks));
            statistics = ItemStatistics.add(statistics, toAdd.build());
        }
        return statistics;
    }

    private ItemStatistics getEnchantStatistics(SkyBlockItem item, ItemStatistics statistics,
                                                SkyBlockPlayer causer, LivingEntity enemy) {
        for (SkyBlockEnchantment enchantment : item.getAttributeHandler().getEnchantments().toList()) {
            ItemStatistics enchantmentStatistics = enchantment.type().getEnch().getStatistics(enchantment.level()).clone();
            statistics = ItemStatistics.add(statistics, enchantmentStatistics);

            if (causer != null && enemy != null) {
                if (enchantment.type().getEnch() instanceof EventBasedEnchant eventBasedStatistic) {
                    statistics = ItemStatistics.add(statistics, eventBasedStatistic.getStatisticsOnDamage(
                            causer,
                            enemy,
                            enchantment.level()
                    ));
                }
            }
        }
        return statistics;
    }

    private ItemStatistics getTemporaryStatistics() {
        ItemStatistics statistics = ItemStatistics.builder().build();

        synchronized (temporaryStatistics) {
            temporaryStatistics.removeIf(temporaryStatistic -> temporaryStatistic.getExpiration() < System.currentTimeMillis());
            for (TemporaryStatistic temporaryStatistic : temporaryStatistics) {
                statistics = ItemStatistics.add(statistics, temporaryStatistic.getStatistics());
            }

            temporaryConditionalStatistics.removeIf(temporaryStatistic -> !temporaryStatistic.getExpiry().apply(player));
            for (TemporaryConditionalStatistic temporaryStatistic : temporaryConditionalStatistics) {
                statistics = ItemStatistics.add(statistics, temporaryStatistic.getStatistics().apply(player));
            }

            return statistics;
        }
    }

    private ItemStatistics getGemstoneStatistics(SkyBlockItem item, ItemStatistics statistics) {
        for (ItemStatistic statistic : ItemStatistic.values()) {
            int extra = Gemstone.getExtraStatisticFromGemstone(statistic, item);
            if (extra != 0) {
                statistics = statistics.addBase(statistic, (double) extra);
            }
        }
        return statistics;
    }

    private ItemStatistics getBestiaryStatistics(SkyBlockPlayer causer, BestiaryMob enemy) {
        ItemStatistics statistics = ItemStatistics.builder().build();

        int kills = causer.getBestiaryData().getAmount(enemy);
        int tier = bestiaryData.getCurrentBestiaryTier(enemy, kills);
        double magicFind = bestiaryData.getTotalMagicFind(tier);
        double strength = bestiaryData.getTotalStrength(tier);

        if (magicFind > 0) statistics.addAdditive(ItemStatistic.MAGIC_FIND, magicFind);
        if (strength > 0) statistics.addAdditive(ItemStatistic.STRENGTH, strength);

        return statistics;
    }

    public Map.Entry<Double, Boolean> runPrimaryDamageFormula(ItemStatistics enemyStatistics, SkyBlockPlayer causer, LivingEntity enemy) {
        ItemStatistics all = allStatistics(causer, enemy);
        return runPrimaryDamageFormula(all, enemyStatistics, enemy);
    }

    public static Map.Entry<Double, Boolean> runPrimaryDamageFormula(ItemStatistics originStatistics, ItemStatistics enemyStatistics) {
        return runPrimaryDamageFormula(originStatistics, enemyStatistics, null);
    }

    private static Map.Entry<Double, Boolean> runPrimaryDamageFormula(ItemStatistics originStatistics, ItemStatistics enemyStatistics, LivingEntity enemy) {
        boolean isCrit = false;
        double critChance = originStatistics.getBase(ItemStatistic.CRITICAL_CHANCE);
        if (Math.random() <= (critChance / 100))
            isCrit = true;

        double baseDamage = originStatistics.getOverall(ItemStatistic.DAMAGE);
        double strength = originStatistics.getOverall(ItemStatistic.STRENGTH);
        double critDamage = originStatistics.getOverall(ItemStatistic.CRITICAL_DAMAGE);

        double strengthDamage = (1 + (strength / 100));
        double criticalDamage = isCrit ? 1 + (critDamage / 100) : 1;

        double damage = baseDamage * strengthDamage * criticalDamage;

        double enemyDefense = enemyStatistics.getOverall(ItemStatistic.DEFENSE);

        if (enemy instanceof SkyBlockMob) {
            double lethalityReduction = LethalityDebuff.getTotalDefenseReduction(enemy.getUuid());
            enemyDefense *= (1 - lethalityReduction);
        }

        if (enemyDefense > 0)
            damage = damage * (1 - (enemyDefense / (enemyDefense + 100)));
        return new AbstractMap.SimpleEntry<>(damage, isCrit);
    }

    public void boostManaRegeneration(double percent, int ticks) {
        manaRegenerationPercentBonus += percent;

        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            manaRegenerationPercentBonus -= percent;
        }, TaskSchedule.tick(ticks), TaskSchedule.stop());
    }

    public void boostStatistic(TemporaryStatistic temporaryStatistic) {
        temporaryStatistics.add(temporaryStatistic);
    }

    /**
     * Get all active temporary statistics that have display info (for tablist)
     */
    public List<TemporaryStatistic> getDisplayableActiveEffects() {
        synchronized (temporaryStatistics) {
            temporaryStatistics.removeIf(stat -> stat.getExpiration() < System.currentTimeMillis());
            return temporaryStatistics.stream()
                    .filter(TemporaryStatistic::hasDisplayInfo)
                    .toList();
        }
    }

    public void boostStatistic(TemporaryConditionalStatistic temporaryStatistic) {
        temporaryConditionalStatistics.add(temporaryStatistic);
    }

    public void boostHealthRegeneration(double percent, int ticks) {
        healthRegenerationPercentBonus += percent;

        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            healthRegenerationPercentBonus -= percent;
        }, TaskSchedule.tick(ticks), TaskSchedule.stop());
    }

    public static void run() {
        barLoop();
        healthLoop();
        manaLoop();
        missionLoop();
        statisticsLoop();
        experiencedStatisticsLoop();
        speedLoop();
    }

    public static void experiencedStatisticsLoop() {
        Scheduler scheduler = MinecraftServer.getSchedulerManager();
        scheduler.submitTask(() -> {
            SkyBlockGenericLoader.getLoadedPlayers().forEach(player -> {
                Thread.startVirtualThread(() -> {
                    List<String> experiencedStatistics = player.getSkyblockDataHandler().get(
                            SkyBlockDataHandler.Data.EXPERIENCED_STATISTICS, DatapointStringList.class
                    ).getValue();

                    ItemStatistics statistics = player.getStatistics().allStatistics();
                    for (ItemStatistic statistic : ItemStatistic.values()) {
                        if (experiencedStatistics.contains(statistic.name())) continue;

                        List<String> description = ItemStatistics.getDescription(statistic);

                        double experiencedValue = statistics.getOverall(statistic);
                        if (experiencedValue <= 0) continue;

                        experiencedStatistics.add(statistic.name());
                        player.getSkyblockDataHandler().get(
                                SkyBlockDataHandler.Data.EXPERIENCED_STATISTICS, DatapointStringList.class
                        ).setValue(experiencedStatistics);

                        player.sendMessage("<a><l>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                        player.sendMessage("<6><l>NEW STAT DISCOVERED! <r>{}", statistic.getCompleteDisplayName());
                        player.sendMessage(" ");
                        player.sendMessage(Text.parse(String.join(" ", description).replace("<7>", "")));
                        player.sendMessage(" ");
                        player.sendMessage("<hover:'<e>Click to view the {} Wiki page!'><click:url:'https://wiki.hypixel.net/{}'><e><l>CLICK HERE <r><e>to learn more on the Official SkyBlock Wiki!</click></hover>",
                            statistic.getDisplayName(),
                            StringUtility.toNormalCase(statistic.name()).replace(" ", "_"));
                        player.sendMessage("<a><l>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
                    }
                });
            });
            return TaskSchedule.seconds(10);
        });
    }

    public static void barLoop() {
        Scheduler scheduler = MinecraftServer.getSchedulerManager();
        scheduler.submitTask(() -> {
            SkyBlockGenericLoader.getLoadedPlayers().forEach(player -> {
                SkyBlockActionBar actionBar = SkyBlockActionBar.getFor(player);

                float absorption = player.getAdditionalHearts();
                int hearts = Math.round(player.getHealth() + absorption);
                int maxHearts = Math.round(player.getMaxHealth());
                actionBar.setDefaultDisplay(SkyBlockActionBar.BarSection.HEALTH, absorption > 0.0f
                        ? Text.of("<6>{}/{}❤", hearts, maxHearts)
                        : Text.of("<c>{}/{}❤", hearts, maxHearts));
                actionBar.setDefaultDisplay(SkyBlockActionBar.BarSection.DEFENSE,
                        player.getDefense() == 0 ? Text.empty()
                                : Text.of("<a>{}❈ Defense", Math.round(player.getDefense())));
                actionBar.setDefaultDisplay(SkyBlockActionBar.BarSection.MANA,
                        Text.of("<b>{}/{}✎ Mana", Math.round(player.getMana()), Math.round(player.getMaxMana())));

                player.sendActionBar(actionBar.render());
            });
            return TaskSchedule.tick(4);
        }, ExecutionType.TICK_END);
    }

    public static void statisticsLoop() {
        Scheduler scheduler = MinecraftServer.getSchedulerManager();
        scheduler.submitTask(() -> {
            SkyBlockGenericLoader.getLoadedPlayers().forEach(player -> {
                Thread.startVirtualThread(() -> {
                    player.getStatistics().updateAccessoryStatistics();
                });
            });
            return TaskSchedule.seconds(2);
        });
    }

    public static void speedLoop() {
        Scheduler scheduler = MinecraftServer.getSchedulerManager();
        scheduler.submitTask(() -> {
            SkyBlockGenericLoader.getLoadedPlayers().forEach(player -> {
                Thread.startVirtualThread(() -> {
                    double speed = player.getStatistics().allStatistics().getOverall(ItemStatistic.SPEED);
                    if (player.isSpeedManaged()) return; // if the Speed is currently managed by another source, don't override it
                    player.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue((float) (speed / 1000));
                });
            });
            return TaskSchedule.tick(4);
        });
    }

    public static void healthLoop() {
        Scheduler scheduler = MinecraftServer.getSchedulerManager();
        scheduler.submitTask(() -> {
            SkyBlockGenericLoader.getLoadedPlayers().forEach(player -> {
                if (player.getHealth() > player.getMaxHealth())
                    player.setHealth(player.getMaxHealth());

                if (player.getHealth() < player.getMaxHealth()) {
                    float healthToIncreaseBy = (float) (1.5 + ((int) player.getMaxHealth() * 0.01) +
                            ((1.5 + ((int) player.getMaxHealth() * 0.01)) * player.getStatistics().getHealthRegenerationPercentBonus()));

                    RegenerationValueUpdateEvent event = new RegenerationValueUpdateEvent(player, healthToIncreaseBy);
                    SkyBlockValueEvent.callValueUpdateEvent(event);
                    healthToIncreaseBy = (float) event.getValue();

                    player.setHealth(Math.min(player.getMaxHealth(), player.getHealth() + healthToIncreaseBy));
                }
            });
            return TaskSchedule.tick(30);
        }, ExecutionType.TICK_END);
    }

    public static void missionLoop() {
        Scheduler scheduler = MinecraftServer.getSchedulerManager();
        scheduler.submitTask(() -> {
            SkyBlockGenericLoader.getLoadedPlayers().forEach(player -> {
                if (player.getRegion() == null) return;
                RegionType region = player.getRegion().getType();
                List<MissionData.ActiveMission> activeMissions = player.getMissionData().getActiveMissions(region);

                if (activeMissions.isEmpty()) {
                    if (barCache.containsKey(player)) {
                        BossBar oldBar = barCache.get(player);
                        player.hideBossBar(oldBar);
                        barCache.remove(player);
                    }
                    return;
                }
                MissionData.ActiveMission activeMission = activeMissions.getFirst();
                BossBar bar;

                if (activeMission.isProgress()) {
                    int maxProgress = ((SkyBlockProgressMission) MissionData.getMissionClass(activeMission)).getMaxProgress();
                    float progress = (float) activeMission.getMissionProgress() / maxProgress;

                    bar = BossBar.bossBar(
                            Text.of("Objective: <e>{}  <7>(<e>{}<7>/<a>{}<7>)",
                                    MissionData.getMissionClass(activeMission).getName(),
                                    activeMission.getMissionProgress(), maxProgress),
                            progress,
                            BossBar.Color.YELLOW,
                            BossBar.Overlay.PROGRESS);
                } else {
                    bar = BossBar.bossBar(
                            Text.of("Objective: <e>{}", MissionData.getMissionClass(activeMission).getName()),
                            1f,
                            BossBar.Color.YELLOW,
                            BossBar.Overlay.NOTCHED_6);
                }

                if (barCache.containsKey(player)) {
                    BossBar oldBar = barCache.get(player);
                    if (oldBar.equals(bar)) return;
                    player.hideBossBar(oldBar);
                }
                player.showBossBar(bar);
                barCache.put(player, bar);
            });
            return TaskSchedule.tick(30);
        }, ExecutionType.TICK_END);
    }

    public static void manaLoop() {
        Scheduler scheduler = MinecraftServer.getSchedulerManager();
        scheduler.submitTask(() -> {
            SkyBlockGenericLoader.getLoadedPlayers().forEach(player -> {
                if (player.getMana() > player.getMaxMana())
                    player.setMana(player.getMaxMana());
                if (player.getMana() <= player.getMaxMana()) {
                    float manaPool = player.getMaxMana();
                    float manaRegeneration = manaPool / 50
                            + (int) ((manaPool / 50) * player.getStatistics().getManaRegenerationPercentBonus());
                    SkyBlockItem pet = player.getPetData().getEnabledPet();
                    PetEvent.ManaRegen manaRegenEvent = player.getPetData()
                            .dispatch(new PetEvent.ManaRegen(player, pet, manaRegeneration));
                    player.setMana((float) Math.min(manaPool, player.getMana() + manaRegenEvent.amount()));
                }
            });
            return TaskSchedule.seconds(1);
        }, ExecutionType.TICK_END);
    }
}
