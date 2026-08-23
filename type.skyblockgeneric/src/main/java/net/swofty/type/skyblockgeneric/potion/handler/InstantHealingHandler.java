package net.swofty.type.skyblockgeneric.potion.handler;

import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.potion.PotionEffectType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

/**
 * Handler for instant healing potions.
 * Heals players, damages undead mobs.
 */
public class InstantHealingHandler implements PotionEffectHandler {

    @Override
    public void applyToPlayer(SkyBlockPlayer player, int level, long durationMs) {
        double healAmount = PotionEffectType.HEALING.getInstantHealAmount(level);
        float currentHealth = player.getHealth();
        float maxHealth = player.getMaxHealth();
        player.setHealth(Math.min(maxHealth, currentHealth + (float) healAmount));
        player.sendMessage("<c>+{} ❤", (int) healAmount);
    }

    @Override
    public void applyToMob(SkyBlockMob mob, int level, long durationMs, double distance) {
        double healAmount = PotionEffectType.HEALING.getInstantHealAmount(level);
        double modifier = getDistanceModifier(distance);
        healAmount *= modifier;

        if (isUndeadMob(mob)) {
            // Healing damages undead
            mob.damage(new Damage(DamageType.MAGIC, null, null, null, (float) healAmount));
        } else {
            float maxHealth = (float) mob.getAttributeValue(Attribute.MAX_HEALTH);
            float newHealth = Math.min(maxHealth, mob.getHealth() + (float) healAmount);
            mob.setHealth(newHealth);
        }
    }

    @Override
    public boolean affectsMobs() {
        return true;
    }

    @Override
    public boolean affectsOtherPlayers() {
        return true;
    }

    private boolean isUndeadMob(SkyBlockMob mob) {
        String mobName = mob.getEntityType().name().toLowerCase();
        return mobName.contains("zombie") ||
               mobName.contains("skeleton") ||
               mobName.contains("wither") ||
               mobName.contains("phantom");
    }
}
