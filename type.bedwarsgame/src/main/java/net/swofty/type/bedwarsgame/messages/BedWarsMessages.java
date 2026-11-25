package net.swofty.type.bedwarsgame.messages;

import net.kyori.adventure.text.format.TextColor;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig.TeamKey;
import net.swofty.commons.text.Text;
import net.swofty.type.bedwarsgame.death.BedWarsDeathResult;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;

public final class BedWarsMessages {
    private BedWarsMessages() {
    }

    public static Text chat(BedWarsPlayer player, Text message, boolean shout) {
        Text prefix = shout ? Text.of("<6>[SHOUT] ") : Text.empty();
        return prefix.append(Text.of("{}<7>: {}", playerName(player), message));
    }

    public static Text bedDestroyed(TeamKey team, BedWarsPlayer destroyer) {
        Text destroyerName = destroyer == null ? Text.of("<7>Unknown") : playerName(destroyer);
        Text bedName = Text.of("<color:{}>{} Bed", teamColor(team), team.getName());
        return Text.of("<f><l>BED DESTRUCTION > <r>{}<7> has been destroyed by {}<7>!", bedName, destroyerName);
    }

    public static Text death(BedWarsDeathResult result) {
        Text victimName = playerName(result.victim());
        BedWarsPlayer creditedPlayer = result.getKillCreditPlayer();
        Text killerName = creditedPlayer == null
                ? Text.of("<7>Unknown")
                : playerName(creditedPlayer);
        Text message = switch (result.deathType()) {
            case VOID -> Text.of("{}<7> fell into the void.", victimName);
            case VOID_ASSISTED -> Text.of("{}<7> was knocked into the void by {}<7>.", victimName, killerName);
            case GENERIC -> Text.of("{}<7> died.", victimName);
            case GENERIC_ASSISTED -> Text.of("{}<7> was killed by {}<7>.", victimName, killerName);
            case BOW -> Text.of("{}<7> was shot by {}<7>.", victimName, killerName);
            case ENTITY -> {
                String entityName = result.attackerEntity() == null
                        ? "an entity"
                        : result.attackerEntity().getEntityType().name();
                yield Text.of("{}<7> was slain by {}<7>'s {}<7>.", victimName, killerName, entityName);
            }
        };
        return result.isFinalKill() ? message.append(" <b><l>FINAL KILL!") : message;
    }

    public static Text teamEliminated(TeamKey team) {
        Text teamName = Text.of("<color:{}>{}", teamColor(team), team.getName());
        return Text.of("<f><l>TEAM ELIMINATED > {}<r><7> has been eliminated!", teamName);
    }

    private static Text playerName(BedWarsPlayer player) {
        TeamKey team = player.getTeamKey();
        return team == null
                ? Text.of("<7>{}", player.getUsername())
                : Text.of("<color:{}>{}", teamColor(team), player.getUsername());
    }

    private static TextColor teamColor(TeamKey team) {
        return TextColor.color(team.rgb());
    }
}
