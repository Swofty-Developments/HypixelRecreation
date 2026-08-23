package net.swofty.type.skyblockgeneric.string;


import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.function.BiFunction;

public enum PlayerTemplates {
    // Basic Information
    NAME((player, input) -> Text.literal(player.getUsername())),
    UUID((player, input) -> Text.literal(player.getUuid().toString())),
    DISPLAY_NAME((player, input) -> player.getFullDisplayName()),
    SHORTENED_NAME((player, input) -> Text.literal(player.getShortenedDisplayName())),

    // Currency & Resources
    COINS((player, input) -> Text.literal(String.format("%.2f", player.getCoins()))),
    BITS((player, input) -> Text.literal(player.getBits().toString())),
    GEMS((player, input) -> Text.literal(player.getGems().toString())),

    // Statistics & Levels
    HEALTH((player, input) -> Text.literal(String.format("%.1f", player.getHealth()))),
    MAX_HEALTH((player, input) -> Text.literal(String.format("%.1f", player.getMaxHealth()))),
    MANA((player, input) -> Text.literal(String.format("%.1f", player.getMana()))),
    MAX_MANA((player, input) -> Text.literal(String.format("%.1f", player.getMaxMana()))),
    DEFENSE((player, input) -> Text.literal(String.format("%.1f", player.getDefense()))),
    MINING_SPEED((player, input) -> Text.literal(String.format("%.1f", player.getMiningSpeed()))),

    // Collections & Progress
    COLLECTION((player, input) -> {
        String collectionType = input.split(":")[1];
        return Text.literal(player.getCollection().get(ItemType.valueOf(collectionType)).toString());
    }),
    FAIRY_SOULS((player, input) -> Text.literal(String.valueOf(player.getFairySouls().getAllFairySouls().size()))),

    // Skills
    SKILL_LEVEL((player, input) -> {
        String skillName = input.split(":")[1];
        SkillCategories category = SkillCategories.valueOf(skillName);
        return Text.literal(String.valueOf(player.getSkills().getCurrentLevel(category)));
    }),
    SKILL_XP((player, input) -> {
        String skillName = input.split(":")[1];
        SkillCategories category = SkillCategories.valueOf(skillName);
        return Text.literal(String.format("%.2f", player.getSkills().getRaw(category)));
    }),

    // SkyBlock Experience
    SKYBLOCK_LEVEL((player, input) -> Text.literal(String.valueOf(player.getSkyBlockExperience().getLevel().asInt()))),
    SKYBLOCK_XP((player, input) -> Text.literal(String.format("%.2f", player.getSkyBlockExperience().getTotalXP()))),

    // Equipment & Inventory
    ARMOR_SET((player, input) -> {
        var armorSet = player.getArmorSet();
        return Text.literal(armorSet != null ? armorSet.name() : "None");
    }),
    EMPTY_SLOTS((player, input) -> Text.literal(String.valueOf(player.getAmountOfEmptySlots()))),

    // Region & Location
    REGION((player, input) -> {
        var region = player.getRegion();
        return Text.literal(region != null ? region.getName() : "None");
    }),

    // Magical Power & Special Stats
    MAGICAL_POWER((player, input) -> Text.literal(String.valueOf(player.getMagicalPower()))),
    RUNE_LEVEL((player, input) -> Text.literal(String.valueOf(player.getRuneLevel()))),

    // Toggles & Settings
    TOGGLE((player, input) -> {
        String toggleName = input.split(":")[1];
        DatapointToggles.Toggles.ToggleType toggle = DatapointToggles.Toggles.ToggleType.valueOf(toggleName);
        return Text.literal(String.valueOf(player.getToggles().get(toggle)));
    }),

    // Coop Status
    IS_COOP((player, input) -> Text.literal(String.valueOf(player.isCoop()))),
    COOP_MEMBERS((player, input) -> {
        if (!player.isCoop()) return Text.literal("0");
        return Text.literal(String.valueOf(player.getCoop().members().size()));
    }),
    ;

    private final BiFunction<SkyBlockPlayer, String, Text> processor;

    PlayerTemplates(BiFunction<SkyBlockPlayer, String, Text> processor) {
        this.processor = processor;
    }

    public Text process(SkyBlockPlayer player, String input) {
        return processor.apply(player, input);
    }
}
