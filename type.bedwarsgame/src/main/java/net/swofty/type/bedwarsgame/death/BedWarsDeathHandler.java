package net.swofty.type.bedwarsgame.death;

import net.kyori.adventure.text.Component;
import net.minestom.server.item.Material;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig.TeamKey;
import net.swofty.type.bedwarsgame.game.v2.BedWarsGame;
import net.swofty.type.bedwarsgame.replay.BedWarsReplayMessages;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public final class BedWarsDeathHandler {
    private BedWarsDeathHandler() {
    }

    public static BedWarsDeathResult calculateDeath(@NotNull BedWarsPlayer victim, @NotNull BedWarsGame game, boolean isVoidKill) {
        TeamKey teamKey = victim.getTeamKey();
        boolean isFinalKill = teamKey != null && !game.isBedAlive(teamKey);

        BedWarsPlayer recentAttacker = BedWarsCombatTracker.getRecentAttacker(victim);
        Material lastAttackerWeapon = BedWarsCombatTracker.getLastAttackerWeapon(victim);

        BedWarsDeathResult.Builder builder = BedWarsDeathResult.builder()
            .victim(victim)
            .isFinalKill(isFinalKill)
            .weaponUsed(lastAttackerWeapon);

        if (isVoidKill) {
            if (recentAttacker != null) {
                return builder
                    .deathType(BedWarsDeathType.VOID_ASSISTED)
                    .assistPlayer(recentAttacker)
                    .build();
            }
            return builder
                .deathType(BedWarsDeathType.VOID)
                .build();
        }

        if (recentAttacker != null) {
            BedWarsDeathType type = isRangedWeapon(lastAttackerWeapon) ? BedWarsDeathType.BOW : BedWarsDeathType.GENERIC_ASSISTED;
            return builder
                .deathType(type)
                .killer(recentAttacker)
                .build();
        }

        return builder
            .deathType(BedWarsDeathType.GENERIC)
            .build();
    }

    private static boolean isRangedWeapon(@Nullable Material weapon) {
        if (weapon == null) return false;
        return weapon == Material.BOW
            || weapon == Material.CROSSBOW
            || weapon == Material.TRIDENT;
    }

    public static Component createDeathMessage(@NotNull BedWarsDeathResult result) {
        return BedWarsReplayMessages.death(result);
    }
}
