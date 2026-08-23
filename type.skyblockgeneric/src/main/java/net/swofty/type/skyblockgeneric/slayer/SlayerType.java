package net.swofty.type.skyblockgeneric.slayer;

import net.minestom.server.entity.EntityType;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;

public enum SlayerType {
    REVENANT_HORROR("Zombie", "Revenant Horror", "<c>", EntityType.ZOMBIE, Material.ROTTEN_FLESH),
    TARANTULA_BROODFATHER("Spider", "Tarantula Broodfather", "<5>", EntityType.SPIDER, Material.SPIDER_EYE),
    SVEN_PACKMASTER("Wolf", "Sven Packmaster", "<f>", EntityType.WOLF, Material.BONE),
    VOIDGLOOM_SERAPH("Enderman", "Voidgloom Seraph", "<5>", EntityType.ENDERMAN, Material.ENDER_PEARL),
    RIFTSTALKER_BLOODFIEND("Vampire", "Riftstalker Bloodfiend", "<4>", EntityType.ZOMBIE, Material.RED_DYE),
    INFERNO_DEMONLORD("Blaze", "Inferno Demonlord", "<6>", EntityType.BLAZE, Material.BLAZE_ROD);

    private final String categoryName;
    private final String displayName;
    private final String colourTag;
    private final EntityType entityType;
    private final Material menuMaterial;

    SlayerType(String categoryName, String displayName, String colourTag, EntityType entityType, Material menuMaterial) {
        this.categoryName = categoryName;
        this.displayName = displayName;
        this.colourTag = colourTag;
        this.entityType = entityType;
        this.menuMaterial = menuMaterial;
    }

    public String categoryName() {
        return categoryName;
    }

    public String displayName() {
        return displayName;
    }

    public Text coloredName() {
        return Text.of(colourTag + "{}", displayName);
    }

    public EntityType entityType() {
        return entityType;
    }

    public Material menuMaterial() {
        return menuMaterial;
    }
}
