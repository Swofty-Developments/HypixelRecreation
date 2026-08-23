package net.swofty.type.murdermysterygame.protection;

import net.minestom.server.MinecraftServer;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.murdermysterygame.user.MurderMysteryPlayer;

/**
 * Manages weakness effect on the murderer.
 * When a weakness splash potion hits the murderer, they become "weakened" for 60 seconds.
 * While weakened, the murderer's first knife hit is absorbed, but the second hit kills the victim.
 */
public class WeaknessProtectionManager {

    // Tags applied to the MURDERER when they are weakened
    public static final Tag<Boolean> MURDERER_WEAKENED = Tag.Boolean("murderer_weakened");
    public static final Tag<Boolean> FIRST_HIT_USED = Tag.Boolean("first_hit_used");
    public static final Tag<Long> WEAKNESS_EXPIRES = Tag.Long("weakness_expires");

    /**
     * Apply weakness to the murderer (60 seconds)
     * Called when murderer is hit by a weakness splash potion.
     */
    public static void applyWeaknessToMurderer(MurderMysteryPlayer murderer) {
        long expiresAt = System.currentTimeMillis() + 60000; // 60 seconds

        murderer.setTag(MURDERER_WEAKENED, true);
        murderer.setTag(FIRST_HIT_USED, false);
        murderer.setTag(WEAKNESS_EXPIRES, expiresAt);

        murderer.sendMessage("<d>You have been weakened for 60 seconds! Your first attack will be absorbed.");

        // Schedule weakness removal
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (isWeakened(murderer)) {
                removeWeakness(murderer);
                murderer.sendMessage("<7>Your weakness has worn off.");
            }
        }).delay(TaskSchedule.seconds(60)).schedule();
    }

    /**
     * Check if murderer is currently weakened
     */
    public static boolean isWeakened(MurderMysteryPlayer murderer) {
        Boolean weakened = murderer.getTag(MURDERER_WEAKENED);
        if (weakened == null || !weakened) return false;

        Long expires = murderer.getTag(WEAKNESS_EXPIRES);
        if (expires == null || System.currentTimeMillis() > expires) {
            removeWeakness(murderer);
            return false;
        }
        return true;
    }

    /**
     * Handle murderer attempting to attack while weakened.
     * @param attacker The murderer attempting to attack
     * @return true if the attack should proceed (kill), false if the attack is absorbed
     */
    public static boolean handleMurdererAttack(MurderMysteryPlayer attacker) {
        if (!isWeakened(attacker)) {
            return true; // Not weakened, attack proceeds normally
        }

        Boolean firstHitUsed = attacker.getTag(FIRST_HIT_USED);
        if (firstHitUsed != null && firstHitUsed) {
            // Second hit - attack proceeds, remove weakness
            removeWeakness(attacker);
            return true;
        }

        // First hit - absorbed, mark as used
        attacker.setTag(FIRST_HIT_USED, true);
        return false; // Attack absorbed
    }

    /**
     * Remove weakness from murderer
     */
    public static void removeWeakness(MurderMysteryPlayer murderer) {
        murderer.removeTag(MURDERER_WEAKENED);
        murderer.removeTag(FIRST_HIT_USED);
        murderer.removeTag(WEAKNESS_EXPIRES);
    }
}
