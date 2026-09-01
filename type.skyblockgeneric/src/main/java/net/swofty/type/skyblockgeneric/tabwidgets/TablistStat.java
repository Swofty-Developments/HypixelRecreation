package net.swofty.type.skyblockgeneric.tabwidgets;

import net.minestom.server.item.Material;

public enum TablistStat {
    HEALTH("health", "Health", Material.GOLDEN_APPLE), DEFENSE("defense", "Defense", Material.IRON_CHESTPLATE), TRUE_DEFENSE("true_defense", "True Defense", Material.BONE_MEAL),
    STRENGTH("strength", "Strength", Material.BLAZE_POWDER), CRIT_CHANCE("crit_chance", "Crit Chance", Material.PLAYER_HEAD), CRIT_DAMAGE("crit_damage", "Crit Damage", Material.PLAYER_HEAD),
    ATTACK_SPEED("attack_speed", "Attack Speed", Material.GOLDEN_AXE), FEROCITY("ferocity", "Ferocity", Material.RED_DYE), SWING_RANGE("swing_range", "Swing Range", Material.STONE_SWORD),
    INTELLIGENCE("intelligence", "Intelligence", Material.ENCHANTED_BOOK), ABILITY_DAMAGE("ability_damage", "Ability Damage", Material.BEACON), HEALTH_REGEN("health_regen", "Health Regen", Material.POTION),
    PULL("pull", "Pull", Material.COBWEB), VITALITY("vitality", "Vitality", Material.GLISTERING_MELON_SLICE), MENDING("mending", "Mending", Material.GHAST_TEAR),
    SPEED("speed", "Speed", Material.SUGAR), MAGIC_FIND("magic_find", "Magic Find", Material.STICK), PET_LUCK("pet_luck", "Pet Luck", Material.PLAYER_HEAD),
    FISHING_SPEED("fishing_speed", "Fishing Speed", Material.FISHING_ROD), SEA_CREATURE_CHANCE("sea_creature_chance", "Sea Creature Chance", Material.PRISMARINE_CRYSTALS), DOUBLE_HOOK_CHANCE("double_hook_chance", "Double Hook Chance", Material.COOKED_COD);
    public final String id, display;
    public final Material material;

    TablistStat(String id, String display, Material material) {
        this.id = id;
        this.display = display;
        this.material = material;
    }
}
