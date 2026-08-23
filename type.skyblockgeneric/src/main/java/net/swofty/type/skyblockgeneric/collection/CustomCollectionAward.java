package net.swofty.type.skyblockgeneric.collection;

import lombok.Getter;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;

import java.util.HashMap;
import java.util.Map;

@Getter
public enum CustomCollectionAward {
    // ENCHANTMENTS
    SCAVENGER_DISCOUNT("<9>Scavenger <7>Exp Discount <a>(-25%)"),
    PROTECTION_DISCOUNT("<9>Protection <7>Exp Discount <a>(-25%)"),
    HARVESTING_DISCOUNT("<9>Harvesting <7>Exp Discount <a>(-25%)"),
    FIRST_STRIKE_DISCOUNT("<9>First Strike <7>Exp Discount <a>(-25%)"),
    CRITICAL_DISCOUNT("<9>Critical <7>Exp Discount <a>(-25%)"),
    EFFICIENCY_DISCOUNT("<9>Efficiency <7>Exp Discount <a>(-25%)"),
    GROWTH_DISCOUNT("<9>Growth <7>Exp Discount <a>(-25%)"),
    LUCK_DISCOUNT("<9>Luck <7>Exp Discount <a>(-25%)"),
    LOOTING_DISCOUNT("<9>Looting <7>Exp Discount <a>(-25%)"),
    SHARPNESS_DISCOUNT("<9>Sharpness <7>Exp Discount <a>(-25%)"),
    SMITE_DISCOUNT("<9>Smite <7>Exp Discount <a>(-25%)"),
    ENDER_SLAYER_DISCOUNT("<9>Ender Slayer <7>Exp Discount <a>(-25%)"),
    GIANT_KILLER_DISCOUNT("<9>Giant Killer <7>Exp Discount <a>(-25%)"),
    EXECUTE_DISCOUNT("<9>Execute <7>Exp Discount <a>(-25%)"),
    IMPALING_DISCOUNT("<9>Impaling <7>Exp Discount <a>(-25%)"),
    BANE_OF_ARTHROPODS_DISCOUNT("<9>Bane of Arthropods <7>Exp Discount <a>(-25%)"),
    CUBISM_DISCOUNT("<9>Cubism <7>Exp Discount <a>(-25%)"),
    FORTUNE_DISCOUNT("<9>Fortune <7>Exp Discount <a>(-25%)"),
    CLEAVE_DISCOUNT("<9>Cleave <7>Exp Discount <a>(-25%)"),
    LIFE_STEAL_DISCOUNT("<9>Life Steal <7>Exp Discount <a>(-25%)"),
    PROSECUTE_DISCOUNT("<9>Prosecute <7>Exp Discount <a>(-25%)"),
    THUNDERBOLT_DISCOUNT("<9>Thunderbolt <7>Exp Discount <a>(-25%)"),
    EXPERIENCE_DISCOUNT("<9>Experience <7>Exp Discount <a>(-25%)"),
    FIRE_ASPECT_DISCOUNT("<9>Fire Aspect <7>Exp Discount <a>(-25%)"),
    KNOCKBACK_DISCOUNT("<9>Knockback <7>Exp Discount <a>(-25%)"),
    LETHALITY_DISCOUNT("<9>Lethality <7>Exp Discount <a>(-25%)"),
    THUNDERLORD_DISCOUNT("<9>Thunderlord <7>Exp Discount <a>(-25%)"),
    VAMPIRISM_DISCOUNT("<9>Vampirism <7>Exp Discount <a>(-25%)"),
    VENOMOUS_DISCOUNT("<9>Venomous <7>Exp Discount <a>(-25%)"),

    // BAGS
    QUIVER("<a>Quiver"),
    QUIVER_UPGRADE_1("<a>Large Quiver Update <7>(+9 slots)"),
    QUIVER_UPGRADE_2("<a>Giant Quiver Update <7>(+9 slots)"),
    ACCESSORY_BAG("<a>Small Accessory Bag"),
    ACCESSORY_BAG_UPGRADE_1("<a>Medium Accessory Bag Upgrade <7>(+6 slots)"),
    ACCESSORY_BAG_UPGRADE_2("<a>Large Accessory Bag Upgrade <7>(+6 slots)"),
    ACCESSORY_BAG_UPGRADE_3("<a>Greater Accessory Bag Upgrade <7>(+6 slots)"),
    ACCESSORY_BAG_UPGRADE_4("<a>Giant Accessory Bag Upgrade <7>(+6 slots)"),
    ACCESSORY_BAG_UPGRADE_5("<a>Massive Accessory Bag Upgrade <7>(+6 slots)"),
    ACCESSORY_BAG_UPGRADE_6("<a>Humongous Accessory Bag Upgrade <7>(+6 slots)"),
    ACCESSORY_BAG_UPGRADE_7("<a>Colossal Accessory Bag Upgrade <7>(+6 slots)"),
    ACCESSORY_BAG_UPGRADE_8("<a>Titanic Accessory Bag Upgrade <7>(+6 slots)"),
    ACCESSORY_BAG_UPGRADE_9("<a>Preposterous Accessory Bag Upgrade <7>(+6 slots)"),
    FISHING_BAG("<a>Small Fishing Bag"),
    FISHING_BAG_UPGRADE_1("<a>Medium Fishing Bag <7>(+9 slots)"),
    FISHING_BAG_UPGRADE_2("<a>Large Fishing Bag <7>(+9 slots)"),
    FISHING_BAG_UPGRADE_3("<a>Giant Fishing Bag <7>(+9 slots)"),
    FISHING_BAG_UPGRADE_4("<a>Massive Fishing Bag <7>(+9 slots)"),
    POTION_BAG("<a>Small Potion Bag"),
    POTION_BAG_UPGRADE_1("<a>Medium Potion Bag <7>(+9 slots)"),
    POTION_BAG_UPGRADE_2("<a>Large Potion Bag <7>(+9 slots)"),
    POTION_BAG_UPGRADE_3("<a>Giant Potion Bag <7>(+9 slots)"),
    POTION_BAG_UPGRADE_4("<a>Massive Potion Bag <7>(+9 slots)"),
    SACK_OF_SACKS("<a>Small Sack of Sacks"),
    SACK_OF_SACKS_UPGRADE_1("<a>Medium Sack of Sacks Upgrade <7>(+3 slots)"),
    SACK_OF_SACKS_UPGRADE_2("<a>Large Sack of Sacks Upgrade <7>(+3 slots)"),
    SACK_OF_SACKS_UPGRADE_3("<a>Greater Sack of Sacks Upgrade <7>(+3 slots)"),
    SACK_OF_SACKS_UPGRADE_4("<a>Giant Sack of Sacks Upgrade <7>(+3 slots)"),
    SACK_OF_SACKS_UPGRADE_5("<a>Massive Sack of Sacks Upgrade <7>(+3 slots)"),

    // FORAGING LEVEL CAP
    FIG_FORAGING_LEVEL_CAP("<a>Foraging Level Cap <7>(+1)"),
    MANGROVE_FORAGING_LEVEL_CAP("<a>Foraging Level Cap <7>(+1)"),
    ;

    public static final Map<CustomCollectionAward, Map.Entry<ItemType, Integer>> AWARD_CACHE = new HashMap<>();

    private final Text display;

    CustomCollectionAward(String display) {
        this.display = Text.of(display);
    }
}
