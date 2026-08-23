package net.swofty.type.skyblockgeneric.potion;

import net.swofty.commons.skyblock.item.attribute.attributes.ItemAttributePotionData;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSkills;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.potion.handler.PotionEffectHandler;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Service for applying potion effects to players and mobs.
 * Delegates to effect-specific handlers for actual application logic.
 */
public class PotionEffectService {

    /**
     * Apply a potion effect to a player who drinks it
     * @param player The player drinking the potion
     * @param potionData The potion's data
     */
    public static void applyToPlayer(SkyBlockPlayer player, ItemAttributePotionData.PotionData potionData) {
        if (potionData == null) return;

        PotionEffectType effectType = PotionEffectType.fromName(potionData.getEffectType());
        if (effectType == null) return;

        int level = potionData.getLevel();

        // Get Alchemy skill level for duration bonus
        DatapointSkills.PlayerSkills skills = player.getSkills();
        int alchemyLevel = skills.getCurrentLevel(SkillCategories.ALCHEMY);
        long durationMs = potionData.getFinalDurationMs(alchemyLevel);

        // Delegate to handler
        PotionEffectHandler handler = effectType.getHandler();
        handler.applyToPlayer(player, level, durationMs);

        // Send feedback message
        sendPotionMessage(player, effectType, level, durationMs);
    }

    /**
     * Apply a splash potion effect to a player
     * @param player The player affected
     * @param potionData The potion's data
     * @param alchemyLevel The Alchemy level of the player who threw the potion
     * @param distance Distance from splash center
     */
    public static void applySplashToPlayer(SkyBlockPlayer player, ItemAttributePotionData.PotionData potionData,
                                           int alchemyLevel, double distance) {
        if (potionData == null) return;

        PotionEffectType effectType = PotionEffectType.fromName(potionData.getEffectType());
        if (effectType == null) return;

        PotionEffectHandler handler = effectType.getHandler();

        // Check if this effect can affect other players
        if (!handler.affectsOtherPlayers()) return;

        int level = potionData.getLevel();
        long baseDurationMs = potionData.getFinalDurationMs(alchemyLevel);

        // Delegate to handler (handler handles distance modifier)
        handler.applySplashToPlayer(player, level, baseDurationMs, distance);
    }

    /**
     * Apply a splash potion effect to a mob
     * @param mob The mob affected
     * @param potionData The potion's data
     * @param alchemyLevel The Alchemy level of the player who threw the potion
     * @param distance Distance from splash center
     */
    public static void applySplashToMob(SkyBlockMob mob, ItemAttributePotionData.PotionData potionData,
                                        int alchemyLevel, double distance) {
        if (potionData == null || mob == null) return;

        PotionEffectType effectType = PotionEffectType.fromName(potionData.getEffectType());
        if (effectType == null) return;

        PotionEffectHandler handler = effectType.getHandler();

        // Check if this effect affects mobs
        if (!handler.affectsMobs()) return;

        int level = potionData.getLevel();
        long baseDurationMs = potionData.getFinalDurationMs(alchemyLevel);

        // Delegate to handler
        handler.applyToMob(mob, level, baseDurationMs, distance);
    }

    /**
     * Send feedback message to player about the potion they drank
     */
    private static void sendPotionMessage(SkyBlockPlayer player, PotionEffectType effectType, int level, long durationMs) {
        String effectName = effectType.getLevelDisplay(level);
        String colorToken = effectType.getCategory().getColorToken();

        if (effectType.getCategory() == PotionEffectCategory.INSTANT) {
            player.sendMessage("<color:'{}'>You drank a {} Potion!", colorToken, effectName);
        } else {
            int totalSeconds = (int) (durationMs / 1000);
            int minutes = totalSeconds / 60;
            int seconds = totalSeconds % 60;

            String durationStr;
            if (minutes > 0) {
                durationStr = minutes + ":" + String.format("%02d", seconds);
            } else {
                durationStr = seconds + "s";
            }

            player.sendMessage("<color:'{}'>You drank a {} Potion! <7>({})", colorToken, effectName, durationStr);
        }
    }
}
