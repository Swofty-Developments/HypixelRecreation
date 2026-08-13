package net.swofty.type.skyblockgeneric.item.handlers.anvilcombine;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.PotatoType;
import net.swofty.commons.skyblock.item.attribute.attributes.ItemAttributeHotPotatoBookData;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.EnchantableComponent;
import net.swofty.type.skyblockgeneric.item.components.HotPotatoableComponent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class AnvilCombineRegistry {
    private static final Map<String, AnvilCombineHandler> REGISTERED_HANDLERS = new HashMap<>();

    static {
        register("HOT_POTATO_BOOK", new AnvilCombineHandler(
                (upgradeItem, sacrificeItem) -> {
                    HotPotatoableComponent hotPotatoable = upgradeItem.getComponent(HotPotatoableComponent.class);
                    PotatoType potatoType = hotPotatoable.getPotatoType();

                    var type = sacrificeItem.getAttributeHandler().getPotentialType();

                    ItemAttributeHotPotatoBookData.HotPotatoBookData upgradeData = upgradeItem.getAttributeHandler().getHotPotatoBookData();
                    upgradeData.addAmount(type, 1);
                    upgradeData.setPotatoType(potatoType);
                    upgradeItem.getAttributeHandler().setHotPotatoBookData(upgradeData);
                },
                (player, upgradeItem, sacrificeItem) -> {
                    if (upgradeItem.hasComponent(HotPotatoableComponent.class)) {
                        var type = sacrificeItem.getAttributeHandler().getPotentialType();

                        HotPotatoableComponent hotPotatoable = upgradeItem.getComponent(HotPotatoableComponent.class);
                        ItemAttributeHotPotatoBookData.HotPotatoBookData upgradeData = upgradeItem.getAttributeHandler().getHotPotatoBookData();
                        return hotPotatoable.canApply(type)
                                && hotPotatoable.canApply(type, upgradeData.getTotalAmount());
                    }
                    return false;
                },
                (SkyBlockItem upgradeItem, SkyBlockItem sacrificeItem, SkyBlockPlayer player) -> 0
        ));
        register("GOLDEN_BOUNTY", new AnvilCombineHandler(
                (upgradeItem, sacrificeItem) -> {
                    upgradeItem.getAttributeHandler().removeEnchantment(EnchantmentType.SCAVENGER);
                    upgradeItem.getAttributeHandler().addEnchantment(
                            new SkyBlockEnchantment(EnchantmentType.SCAVENGER, 6));
                },
                (player, upgradeItem, sacrificeItem) -> {
                    if (upgradeItem.getAttributeHandler().getPotentialType() == ItemType.TERMINATOR
                            || !upgradeItem.hasComponent(EnchantableComponent.class)) return false;
                    EnchantableComponent enchantable = upgradeItem.getComponent(EnchantableComponent.class);
                    boolean weapon = enchantable.getEnchantItemGroups().stream().anyMatch(Set.of(
                            EnchantItemGroups.SWORD, EnchantItemGroups.LONG_SWORD,
                            EnchantItemGroups.FISHING_WEAPON, EnchantItemGroups.GAUNTLET)::contains);
                    SkyBlockEnchantment scavenger = upgradeItem.getAttributeHandler()
                            .getEnchantment(EnchantmentType.SCAVENGER);
                    return weapon && scavenger != null && scavenger.level() == 5;
                },
                (SkyBlockItem upgradeItem, SkyBlockItem sacrificeItem, SkyBlockPlayer player) -> 0
        ));
        registerEnchantmentUpgrade("A_BEGINNERS_GUIDE_TO_PESTHUNTING", EnchantmentType.PESTERMINATOR,
                5, 6, Set.of(EnchantItemGroups.ARMOR));
        registerEnchantmentUpgrade("VIBRANT_CORAL", EnchantmentType.SCUBA,
                5, 6, Set.of(EnchantItemGroups.ARMOR));
        registerEnchantmentUpgrade("SEVERED_PINCER", EnchantmentType.FRAIL,
                6, 7, Set.of(EnchantItemGroups.FISHING_ROD));
        registerEnchantmentUpgrade("ENSNARED_SNAIL", EnchantmentType.BANE_OF_ARTHROPODS,
                6, 7, Set.of(EnchantItemGroups.SWORD, EnchantItemGroups.LONG_SWORD));
        registerEnchantmentUpgrade("SEVERED_HAND", EnchantmentType.SMITE,
                6, 7, weaponGroups());
        registerEnchantmentUpgrade("GOLD_BOTTLE_CAP", EnchantmentType.LUCK_OF_THE_SEA,
                6, 7, Set.of(EnchantItemGroups.FISHING_ROD));
        registerEnchantmentUpgrade("CHAIN_OF_THE_END_TIMES", EnchantmentType.CHARM,
                5, 6, Set.of(EnchantItemGroups.FISHING_ROD));
        registerEnchantmentUpgrade("FATEFUL_STINGER", EnchantmentType.VENOMOUS,
                6, 7, Set.of(EnchantItemGroups.SWORD, EnchantItemGroups.LONG_SWORD));
        registerEnchantmentUpgrade("OCTOPUS_TENDRIL", EnchantmentType.SPIKED_HOOK,
                6, 7, Set.of(EnchantItemGroups.FISHING_ROD));
        registerEnchantmentUpgrade("END_STONE_IDOL", EnchantmentType.ENDER_SLAYER,
                6, 7, weaponGroups());
        registerEnchantmentUpgrade("TROUBLED_BUBBLE", EnchantmentType.PISCARY,
                6, 7, Set.of(EnchantItemGroups.FISHING_ROD));
        register("ENCHANTED_BOOK", new AnvilCombineHandler(
                (upgradeItem, sacrificeItem) -> {
                    // Remove existing enchantments
                    List<SkyBlockEnchantment> enchantments = sacrificeItem.getAttributeHandler().getEnchantments().toList();
                    enchantments.forEach(enchantment -> upgradeItem.getAttributeHandler().removeEnchantment(enchantment.type()));

                    // Add new enchantments
                    enchantments.forEach(enchantment -> {
                        upgradeItem.getAttributeHandler().addEnchantment(new SkyBlockEnchantment(
                                enchantment.type(),
                                enchantment.level()));
                    });
                },
                ((player, upgradeItem, sacrificeItem) -> {
                    if (upgradeItem.hasComponent(EnchantableComponent.class)) {
                        EnchantableComponent enchantable = upgradeItem.getComponent(EnchantableComponent.class);
                        List<SkyBlockEnchantment> enchantments = sacrificeItem.getAttributeHandler().getEnchantments().toList();
                        Set<EnchantItemGroups> sourceTypes = enchantments.stream()
                                .flatMap(enchantment -> enchantment.type().getEnch().getGroups().stream()).collect(Collectors.toSet());

                        List<EnchantItemGroups> applicableTypes = enchantable.getEnchantItemGroups();
                        return sourceTypes.stream().anyMatch(applicableTypes::contains);
                    }
                    return false;
                }),
                (SkyBlockItem upgradeItem, SkyBlockItem sacrificeItem, SkyBlockPlayer player) -> {
                    List<SkyBlockEnchantment> enchantments = sacrificeItem.getAttributeHandler().getEnchantments().toList();
                    return enchantments.stream()
                            .mapToInt(enchant -> enchant.type().getApplyCost(enchant.level(), player))
                            .sum();
                }
        ));
    }

    private static void registerEnchantmentUpgrade(String id, EnchantmentType type,
                                                    int requiredLevel, int upgradedLevel,
                                                    Set<EnchantItemGroups> groups) {
        register(id, new AnvilCombineHandler(
                (upgradeItem, sacrificeItem) -> {
                    upgradeItem.getAttributeHandler().removeEnchantment(type);
                    upgradeItem.getAttributeHandler().addEnchantment(
                            new SkyBlockEnchantment(type, upgradedLevel));
                },
                (player, upgradeItem, sacrificeItem) -> {
                    if (!upgradeItem.hasComponent(EnchantableComponent.class)) return false;
                    EnchantableComponent enchantable = upgradeItem.getComponent(EnchantableComponent.class);
                    SkyBlockEnchantment enchantment = upgradeItem.getAttributeHandler().getEnchantment(type);
                    return groups.stream().anyMatch(enchantable.getEnchantItemGroups()::contains)
                            && enchantment != null && enchantment.level() == requiredLevel;
                },
                (SkyBlockItem upgradeItem, SkyBlockItem sacrificeItem, SkyBlockPlayer player) -> 0
        ));
    }

    private static Set<EnchantItemGroups> weaponGroups() {
        return Set.of(EnchantItemGroups.SWORD, EnchantItemGroups.LONG_SWORD,
                EnchantItemGroups.FISHING_WEAPON, EnchantItemGroups.GAUNTLET);
    }

    public static void register(String id, AnvilCombineHandler handler) {
        REGISTERED_HANDLERS.put(id, handler);
    }

    public static AnvilCombineHandler getHandler(String id) {
        return REGISTERED_HANDLERS.get(id);
    }
}
