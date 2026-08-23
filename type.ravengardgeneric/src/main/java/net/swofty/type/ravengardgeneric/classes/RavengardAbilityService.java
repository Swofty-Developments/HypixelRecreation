package net.swofty.type.ravengardgeneric.classes;

import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.ravengardgeneric.user.RavengardPlayer;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Runs the starter abilities. Heals work on the hud's 160 point scale over the forty point
 * vanilla pool, timed buffs sit in a registry the damage code reads, and Shadows is real
 * invisibility with the slow, cancelled by attacking or being hit.
 */
public final class RavengardAbilityService {
    /** Display health points per vanilla half heart pool: 160 shown over 40 vanilla. */
    private static final float DISPLAY_SCALE = 4f;

    private static final Map<UUID, Map<RavengardAbility, Long>> COOLDOWNS = new ConcurrentHashMap<>();
    private static final Map<UUID, Map<RavengardAbility, Long>> BUFFS = new ConcurrentHashMap<>();

    private RavengardAbilityService() {
    }

    public static void use(RavengardPlayer player, int slot) {
        RavengardClass playerClass = player.getRavengardClass();
        if (playerClass == null || player.isTutorial()) {
            return;
        }
        List<RavengardAbility> abilities = playerClass.defaultAbilities();
        if (slot >= abilities.size()) {
            return;
        }
        RavengardAbility ability = abilities.get(slot);

        long now = System.currentTimeMillis();
        long readyAt = COOLDOWNS.computeIfAbsent(player.getUuid(), ignored -> new ConcurrentHashMap<>())
                .getOrDefault(ability, 0L);
        if (readyAt > now) {
            player.sendMessage("<c>This ability is on cooldown for {}s!", (readyAt - now) / 1000 + 1);
            return;
        }
        COOLDOWNS.get(player.getUuid()).put(ability, now + ability.getCooldownSeconds() * 1000L);

        apply(player, ability);
        player.sendMessage("<a>You used <f>{}<a>!", ability.getDisplayName());
    }

    private static void apply(RavengardPlayer player, RavengardAbility ability) {
        switch (ability) {
            case RECOVERY, MED_KIT -> heal(player, 30);
            case COOL_OFF, HEAL_WOUNDS -> healOverTime(player, 35, 10);
            case SHADOWS -> shadows(player);
            default -> buff(player, ability, 10);
        }
    }

    /** Whether a timed buff from an ability is currently active, for the damage code to read. */
    public static boolean hasBuff(RavengardPlayer player, RavengardAbility ability) {
        Map<RavengardAbility, Long> buffs = BUFFS.get(player.getUuid());
        return buffs != null && buffs.getOrDefault(ability, 0L) > System.currentTimeMillis();
    }

    private static void buff(RavengardPlayer player, RavengardAbility ability, int seconds) {
        BUFFS.computeIfAbsent(player.getUuid(), ignored -> new ConcurrentHashMap<>())
                .put(ability, System.currentTimeMillis() + seconds * 1000L);
    }

    /** A consumable's heal on the display scale, instant or spread over the given seconds. */
    public static void consumeHeal(RavengardPlayer player, int displayAmount, int seconds) {
        if (seconds <= 0) {
            heal(player, displayAmount);
        } else {
            healOverTime(player, displayAmount, seconds);
        }
    }

    private static void heal(RavengardPlayer player, int displayAmount) {
        float max = (float) player.getAttributeValue(Attribute.MAX_HEALTH);
        player.setHealth(Math.min(max, player.getHealth() + displayAmount / DISPLAY_SCALE));
    }

    private static void healOverTime(RavengardPlayer player, int displayTotal, int seconds) {
        float perSecond = displayTotal / (float) seconds / DISPLAY_SCALE;
        for (int second = 1; second <= seconds; second++) {
            MinecraftServer.getSchedulerManager().buildTask(() -> {
                if (player.isOnline()) {
                    float max = (float) player.getAttributeValue(Attribute.MAX_HEALTH);
                    player.setHealth(Math.min(max, player.getHealth() + perSecond));
                }
            }).delay(TaskSchedule.tick(second * 20)).schedule();
        }
    }

    private static void shadows(RavengardPlayer player) {
        buff(player, RavengardAbility.SHADOWS, 25);
        player.setInvisible(true);
        var speed = player.getAttribute(Attribute.MOVEMENT_SPEED);
        double base = speed.getBaseValue();
        speed.setBaseValue(base * 0.5);
        MinecraftServer.getSchedulerManager().buildTask(() -> endShadows(player, base))
                .delay(TaskSchedule.tick(25 * 20)).schedule();
    }

    public static void breakShadows(RavengardPlayer player) {
        if (!hasBuff(player, RavengardAbility.SHADOWS)) {
            return;
        }
        BUFFS.get(player.getUuid()).remove(RavengardAbility.SHADOWS);
        endShadows(player, 0.1);
    }

    private static void endShadows(RavengardPlayer player, double baseSpeed) {
        if (!player.isOnline()) {
            return;
        }
        player.setInvisible(false);
        player.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(baseSpeed);
    }

    public static void forget(UUID uuid) {
        COOLDOWNS.remove(uuid);
        BUFFS.remove(uuid);
    }
}
