package net.swofty.type.skyblockgeneric.entity.mob.impl;

import net.minestom.server.entity.EntityType;
import net.swofty.type.generic.gui.inventory.item.GUIMaterial;
import net.swofty.type.skyblockgeneric.entity.mob.MobType;
import net.swofty.type.skyblockgeneric.loottable.OtherLoot;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.region.RegionType;

import java.util.List;
import java.util.Objects;

public record MobDefinition(
        String displayName,
        String mobId,
        EntityType entityType,
        int level,
        double health,
        double damage,
        double speed,
        OtherLoot otherLoot,
        List<MobType> mobTypes,
        SkyBlockLootTable lootTable,
        GUIMaterial guiMaterial,
        int maxBestiaryTier,
        int bestiaryBracket,
        RegionType targetRegion,
        boolean attacks,
        boolean targetsPlayers
) {
    public MobDefinition {
        Objects.requireNonNull(displayName, "displayName");
        Objects.requireNonNull(mobId, "mobId");
        Objects.requireNonNull(entityType, "entityType");
        Objects.requireNonNull(otherLoot, "otherLoot");
        Objects.requireNonNull(guiMaterial, "guiMaterial");
        mobTypes = List.copyOf(mobTypes);
        if (displayName.isBlank() || mobId.isBlank() || mobTypes.isEmpty()) {
            throw new IllegalArgumentException("Mob name, id, and types must be present");
        }
        if (level < 1 || health <= 0 || damage < 0 || speed < 0) {
            throw new IllegalArgumentException("Mob stats must be non-negative and health/level must be positive");
        }
        if (maxBestiaryTier < 1 || bestiaryBracket < 1) {
            throw new IllegalArgumentException("Bestiary tier and bracket must be positive");
        }
        if (targetsPlayers && !attacks) {
            throw new IllegalArgumentException("A mob cannot target players without an attack goal");
        }
        if (targetsPlayers && targetRegion == null) {
            throw new IllegalArgumentException("Player-targeting mobs require a target region");
        }
    }

    public static Builder builder(String mobId, String displayName, EntityType entityType) {
        return new Builder(mobId, displayName, entityType);
    }

    public static final class Builder {
        private final String mobId;
        private final String displayName;
        private final EntityType entityType;
        private int level = 1;
        private double health = 100;
        private double damage;
        private double speed = 100;
        private OtherLoot otherLoot = new OtherLoot(0, 0, 0);
        private List<MobType> mobTypes = List.of();
        private SkyBlockLootTable lootTable;
        private GUIMaterial guiMaterial;
        private int maxBestiaryTier = 5;
        private int bestiaryBracket = 1;
        private RegionType targetRegion;
        private boolean attacks;
        private boolean targetsPlayers;

        private Builder(String mobId, String displayName, EntityType entityType) {
            this.mobId = mobId;
            this.displayName = displayName;
            this.entityType = entityType;
        }

        public Builder level(int value) { level = value; return this; }
        public Builder stats(double health, double damage, double speed) { this.health = health; this.damage = damage; this.speed = speed; return this; }
        public Builder rewards(long skillXp, int coins, int xpOrbs) { otherLoot = new OtherLoot(skillXp, coins, xpOrbs); return this; }
        public Builder types(MobType... values) { mobTypes = List.of(values); return this; }
        public Builder loot(SkyBlockLootTable value) { lootTable = value; return this; }
        public Builder gui(GUIMaterial value) { guiMaterial = value; return this; }
        public Builder bestiary(int maxTier, int bracket) { maxBestiaryTier = maxTier; bestiaryBracket = bracket; return this; }
        public Builder behaviour(RegionType region, boolean attacks, boolean targetsPlayers) { targetRegion = region; this.attacks = attacks; this.targetsPlayers = targetsPlayers; return this; }

        public MobDefinition build() {
            return new MobDefinition(displayName, mobId, entityType, level, health, damage, speed, otherLoot,
                    mobTypes, lootTable, guiMaterial, maxBestiaryTier, bestiaryBracket, targetRegion, attacks,
                    targetsPlayers);
        }
    }
}
