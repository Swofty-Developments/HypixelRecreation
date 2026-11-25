package net.swofty.type.skyblockgeneric.hunting;

import net.kyori.adventure.key.Key;
import net.swofty.commons.loot.LootTable;
import net.swofty.type.skyblockgeneric.entity.mob.BestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public final class CharmingService {
    private CharmingService() {
    }

    public static boolean tryCharm(SkyBlockPlayer player, SkyBlockMob mob) {
        AttributeDefinition definition = shardFor(mob);
        if (definition == null) return false;
        int huntingLevel = player.getSkills().getCurrentLevel(SkillCategories.HUNTING);
        double skillChance = huntingLevel * 0.0004D;
        double charmedChance = AttributeEffectService.value(player.getHuntingData(), AttributeId.parse("L11")) / 100D;
        if (LootTable.rollSingle(Key.key("skyblock", "hunting/charming"), definition,
                skillChance + charmedChance).isEmpty()) return false;
        int amount = AttributeEffectService.fortunateAmount(player, definition);
        player.getHuntingData().addShards(definition.id(), amount);
        player.getSkills().increase(player, SkillCategories.HUNTING, huntingExperience(definition));
        player.sendMessage("<d><l>CHARM! </l><a>Captured <e>{}x <a>{} <7>from <c>{}<7>!",
                amount, definition.shardName(), mob.getDisplayName());
        return true;
    }

    private static AttributeDefinition shardFor(SkyBlockMob mob) {
        if (!(mob instanceof BestiaryMob bestiaryMob)) return null;
        String mobId = bestiaryMob.getMobID();
        return AttributeRegistry.values().stream()
                .filter(definition -> definition.sources().stream()
                        .anyMatch(source -> source.mobIds().contains(mobId)))
                .findFirst().orElse(null);
    }

    private static double huntingExperience(AttributeDefinition definition) {
        return switch (definition.rarity()) {
            case COMMON -> 75;
            case UNCOMMON -> 150;
            case RARE -> 300;
            case EPIC -> 500;
            case LEGENDARY -> 1000;
        };
    }
}
