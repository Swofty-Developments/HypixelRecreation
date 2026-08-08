package net.swofty.type.bedwarsgame.replay;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig.TeamKey;
import net.swofty.type.bedwarsgame.death.BedWarsDeathResult;
import net.swofty.type.bedwarsgame.death.BedWarsDeathType;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;

public final class BedWarsReplayMessages {
    private BedWarsReplayMessages() {
    }

    public static Component chat(BedWarsPlayer player, Component message, boolean shout) {
        Component prefix = shout ? Component.text("[SHOUT] ", NamedTextColor.GOLD) : Component.empty();
        return prefix.append(playerName(player)).append(Component.text(": ", NamedTextColor.GRAY)).append(message);
    }

    public static Component bedDestroyed(TeamKey team, BedWarsPlayer destroyer) {
        Component destroyerName = destroyer == null ? Component.text("Unknown", NamedTextColor.GRAY) : playerName(destroyer);
        return Component.text("BED DESTRUCTION > ", NamedTextColor.WHITE).decorate(TextDecoration.BOLD)
                .append(Component.text(team.getName() + " Bed", TextColor.color(team.rgb())).decoration(TextDecoration.BOLD, false))
                .append(Component.text(" has been destroyed by ", NamedTextColor.GRAY).decoration(TextDecoration.BOLD, false))
                .append(destroyerName)
                .append(Component.text("!", NamedTextColor.GRAY));
    }

    public static Component kill(BedWarsPlayer killer, BedWarsPlayer victim, BedWarsDeathType type, boolean finalKill) {
        Component victimName = playerName(victim);
        Component killerName = killer == null ? null : playerName(killer);
        Component message = switch (type) {
            case GENERIC -> victimName.append(Component.text(" died.", NamedTextColor.GRAY));
            case GENERIC_ASSISTED -> killerName == null
                    ? victimName.append(Component.text(" died.", NamedTextColor.GRAY))
                    : victimName.append(Component.text(" was killed by ", NamedTextColor.GRAY)).append(killerName).append(Component.text(".", NamedTextColor.GRAY));
            case VOID -> victimName.append(Component.text(" fell into the void.", NamedTextColor.GRAY));
            case VOID_ASSISTED -> killerName == null
                    ? victimName.append(Component.text(" fell into the void.", NamedTextColor.GRAY))
                    : victimName.append(Component.text(" was knocked into the void by ", NamedTextColor.GRAY)).append(killerName).append(Component.text(".", NamedTextColor.GRAY));
            case BOW -> killerName == null
                    ? victimName.append(Component.text(" died.", NamedTextColor.GRAY))
                    : victimName.append(Component.text(" was shot by ", NamedTextColor.GRAY)).append(killerName).append(Component.text(".", NamedTextColor.GRAY));
            case ENTITY -> killerName == null
                    ? victimName.append(Component.text(" died.", NamedTextColor.GRAY))
                    : victimName.append(Component.text(" was slain by ", NamedTextColor.GRAY)).append(killerName)
                    .append(Component.text("'s entity.", NamedTextColor.GRAY));
        };
        return finalKill ? message.append(Component.text(" FINAL KILL!", NamedTextColor.AQUA, TextDecoration.BOLD)) : message;
    }

    public static Component death(BedWarsDeathResult result) {
        BedWarsPlayer victim = result.victim();
        BedWarsPlayer credited = result.getKillCreditPlayer();
        Component victimName = playerName(victim);
        Component killerName = credited == null ? Component.text("Unknown", NamedTextColor.GRAY) : playerName(credited);
        Component message = switch (result.deathType()) {
            case VOID -> victimName.append(Component.text(" fell into the void.", NamedTextColor.GRAY));
            case VOID_ASSISTED ->
                    victimName.append(Component.text(" was knocked into the void by ", NamedTextColor.GRAY))
                            .append(killerName).append(Component.text(".", NamedTextColor.GRAY));
            case GENERIC -> victimName.append(Component.text(" died.", NamedTextColor.GRAY));
            case GENERIC_ASSISTED -> victimName.append(Component.text(" was killed by ", NamedTextColor.GRAY))
                    .append(killerName).append(Component.text(".", NamedTextColor.GRAY));
            case BOW -> victimName.append(Component.text(" was shot by ", NamedTextColor.GRAY))
                    .append(killerName).append(Component.text(".", NamedTextColor.GRAY));
            case ENTITY -> {
                String entityName = result.attackerEntity() == null ? "an entity" : result.attackerEntity().getEntityType().name();
                yield victimName.append(Component.text(" was slain by ", NamedTextColor.GRAY)).append(killerName)
                        .append(Component.text("'s " + entityName + ".", NamedTextColor.GRAY));
            }
        };
        return result.isFinalKill() ? message.append(Component.text(" FINAL KILL!", NamedTextColor.AQUA, TextDecoration.BOLD)) : message;
    }

    public static Component teamEliminated(TeamKey team) {
        return Component.text("TEAM ELIMINATED > ", NamedTextColor.WHITE).decorate(TextDecoration.BOLD)
                .append(Component.text(team.getName(), TextColor.color(team.rgb())))
                .append(Component.text(" has been eliminated!", NamedTextColor.GRAY).decoration(TextDecoration.BOLD, false));
    }

    private static Component playerName(BedWarsPlayer player) {
        return player.getTeamKey() == null ? Component.text(player.getUsername(), NamedTextColor.GRAY)
                : Component.text(player.getUsername(), TextColor.color(player.getTeamKey().rgb()));
    }
}
