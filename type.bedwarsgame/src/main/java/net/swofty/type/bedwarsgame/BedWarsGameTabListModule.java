package net.swofty.type.bedwarsgame;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.entity.PlayerSkin;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig;
import net.swofty.commons.party.FullParty;
import net.swofty.type.bedwarsgame.game.v2.BedWarsGame;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.generic.party.PartyManager;
import net.swofty.type.generic.tab.CustomTablistSkin;
import net.swofty.type.generic.tab.TablistModule;
import net.swofty.type.generic.tab.TablistSkin;
import net.swofty.type.generic.tab.TablistSkinRegistry;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.*;

public class BedWarsGameTabListModule extends TablistModule {

    @Override
    public List<TablistEntry> getEntries(HypixelPlayer p) {
        BedWarsPlayer player = (BedWarsPlayer) p;
        BedWarsGame game = player.getGame();
        if (game == null) {
            return List.of();
        }

        List<BedWarsPlayer> players = new ArrayList<>(game.getPlayers());
        Comparator<BedWarsPlayer> playerOrder = Comparator
                .comparing(BedWarsPlayer::getTeamKey, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(BedWarsPlayer::getUsername, String.CASE_INSENSITIVE_ORDER);
        if (game.getState().isWaiting()) {
            List<TablistEntry> entries = new ArrayList<>(players.size());
            for (BedWarsPlayer bedWarsPlayer : players.stream()
                    .sorted(playerOrder)
                    .toList()) {
                BedWarsMapsConfig.TeamKey teamKey = bedWarsPlayer.getTeamKey();
                boolean shouldObfuscate = bedWarsPlayer != player
                        && (teamKey == null || teamKey != player.getTeamKey());
                FullParty party = PartyManager.getPartyFromPlayer(player);
                if (shouldObfuscate && party != null) {
                    shouldObfuscate = !party.getParticipants().contains(bedWarsPlayer.getUuid());
                }

                TablistSkin skin;
                PlayerSkin playerSkin = bedWarsPlayer.getSkin();

                if (shouldObfuscate) {
                    skin = TablistSkinRegistry.GRAY;
                } else if (playerSkin == null) {
                    skin = TablistSkinRegistry.GRAY;
                } else {
                    skin = new CustomTablistSkin(playerSkin);
                }

                Component displayName = shouldObfuscate
                        ? Component.text(UUID.randomUUID().toString().replace("-", "").substring(0, new Random().nextInt(10) + 4), Style.style(TextDecoration.OBFUSCATED))
                        : bedWarsPlayer.getColouredName();
                entries.add(new TablistEntry(displayName, skin));
            }
            return entries;
        } else if (game.getState().isInProgress()) {
            List<TablistEntry> entries = new ArrayList<>(players.size());
            for (BedWarsPlayer bedWarsPlayer : players.stream()
                    .sorted(playerOrder)
                    .toList()) {
                Component displayName = bedWarsPlayer.getDisplayName();
                if (displayName == null) {
                    displayName = Component.text(bedWarsPlayer.getUsername());
                }
                TablistSkin skin;
                PlayerSkin playerSkin = bedWarsPlayer.getSkin();
                if (playerSkin == null) {
                    skin = TablistSkinRegistry.GRAY;
                } else {
                    skin = new CustomTablistSkin(playerSkin);
                }
                entries.add(new TablistEntry(displayName, skin));
            }
            return entries;
        }

        return List.of();
    }
}
