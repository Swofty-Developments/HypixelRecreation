package net.swofty.type.skyblockgeneric.user.statistics;

import lombok.Getter;
import net.minestom.server.item.Material;

import java.util.List;

@Getter
public enum StatisticSourceType {
    ARMOR("Armor", Material.IRON_CHESTPLATE, List.of("<8>Stats on your equipped helmet,", "<8>chestplate, leggings and boots.")),
    EQUIPMENT("Equipment", Material.CHEST, List.of("<8>Stats on your equipped necklace,", "<8>cloak, belt and handwear.")),
    HELD_ITEM("Held Item", Material.DIAMOND_SWORD, List.of("<8>Stats on the item you are holding.")),
    ACCESSORY_BAG("Accessory Bag", Material.REDSTONE, List.of("<8>Stats on the accessories in your", "<8>accessory bag.")),
    PET("Pet", Material.BONE, List.of("<8>Stats from your active pet.")),
    SKILL("Skill", Material.CRAFTING_TABLE, List.of("<8>Level up your skills to gain", "<8>permanent stats.")),
    SKYBLOCK_LEVELS("SkyBlock Levels", Material.NETHER_STAR, List.of("<8>Stats from leveling up your SkyBlock", "<8>level.")),
    INNATE("Innate", Material.NETHER_STAR, List.of("<8>This is the base value of this stat", "<8>which everybody starts with.")),
    TEMPORARY_BUFF("Temporary Buff", Material.POTION, List.of("<8>Stats from an active buff.")),
    FAIRY_SOULS("Fairy Souls", Material.PLAYER_HEAD, List.of("<8>Stats from exchanged Fairy Souls.")),
    BESTIARY("Bestiary", Material.WRITABLE_BOOK, List.of("<8>Stats from your progress in the", "<8>Bestiary.")),
    SLAYER("Slayer", Material.ROTTEN_FLESH, List.of("<8>Stats from slayer LVLs.")),
    THAUMATURGIST_POWER("Thaumaturgist Power", Material.LAPIS_LAZULI, List.of("<8>Stats from your selected accessory", "<8>bag power.")),
    TUNING("Tuning", Material.COMPARATOR, List.of("<8>Stats from your tuning points.")),
    ATTRIBUTE("Attribute", Material.DIAMOND, List.of("<8>Stats from your equipped Attributes.")),
    SHIP_CREWMATE("Ship Crewmate", Material.OAK_BOAT, List.of("<8>Stats from crewmates on your Ship.")),
    HEART_OF_THE_MOUNTAIN("Heart of the Mountain", Material.EMERALD, List.of("<8>Stats from Heart of the Mountain perks.")),
    HEART_OF_THE_FOREST("Heart of the Forest", Material.JUNGLE_SAPLING, List.of("<8>Stats from Heart of the Forest perks.")),
    OTHER("Other", Material.PAPER, List.of("<8>Another source of this statistic."));

    private final String displayName;
    private final Material material;
    private final List<String> description;

    StatisticSourceType(String displayName, Material material, List<String> description) {
        this.displayName = displayName;
        this.material = material;
        this.description = description;
    }
}
