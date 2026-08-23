package net.swofty.type.murdermysterylobby.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.murdermystery.MurderMysteryLeaderboardPeriod;
import net.swofty.commons.murdermystery.MurderMysteryModeStats;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.achievement.AchievementCategory;
import net.swofty.type.generic.achievement.AchievementRegistry;
import net.swofty.type.generic.achievement.PlayerAchievementHandler;
import net.swofty.type.generic.data.datapoints.DatapointMurderMysteryModeStats;
import net.swofty.type.generic.data.handlers.MurderMysteryDataHandler;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.murdermysterylobby.gui.GUIMurderMysteryStatistics;

public class NPCStats extends HypixelNPC {

    public NPCStats() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                // Get actual achievement count from player data
                PlayerAchievementHandler achievementHandler = new PlayerAchievementHandler(player);
                int playerAchievements = achievementHandler.getUnlockedCount(AchievementCategory.MURDER_MYSTERY);
                int totalAchievements = AchievementRegistry.getByCategory(AchievementCategory.MURDER_MYSTERY).size();

                // Get actual wins from player data
                long totalWins = 0;
                MurderMysteryDataHandler handler = MurderMysteryDataHandler.getUser(player);
                if (handler != null) {
                    MurderMysteryModeStats stats = handler.get(MurderMysteryDataHandler.Data.MODE_STATS, DatapointMurderMysteryModeStats.class).getValue();
                    totalWins = stats.getTotalWins(MurderMysteryLeaderboardPeriod.LIFETIME);
                }

                return new String[]{
                        "<6><l>Your Murder Mystery Profile",
                        Text.of("<f>Achievements: <e>{}<7>/<a>{}", playerAchievements, totalAchievements).serialize(),
                        Text.of("<f>Total Wins: <a>{:,}", totalWins).serialize(),
                        "<e><l>CLICK FOR STATS"
                };
            }

            @Override
            public String texture(HypixelPlayer player) {
                return player.getPlayerSkin().textures();
            }

            @Override
            public String signature(HypixelPlayer player) {
                return player.getPlayerSkin().signature();
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(3.5, 68, 10.5, -130, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return false;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        new GUIMurderMysteryStatistics().open(event.player());
    }
}
