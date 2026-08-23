package net.swofty.type.skywarslobby.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.skywars.SkyWarsLevelColor;
import net.swofty.commons.skywars.SkywarsLeaderboardPeriod;
import net.swofty.commons.skywars.SkywarsModeStats;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.achievement.AchievementCategory;
import net.swofty.type.generic.achievement.AchievementRegistry;
import net.swofty.type.generic.achievement.PlayerAchievementHandler;
import net.swofty.type.generic.data.datapoints.DatapointLong;
import net.swofty.type.generic.data.datapoints.DatapointSkywarsModeStats;
import net.swofty.type.generic.data.handlers.SkywarsDataHandler;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skywarslobby.gui.GUISkywarsStatistics;
import net.swofty.type.skywarslobby.level.SkywarsLevelRegistry;

import java.text.DecimalFormat;

public class NPCSkywarsStats extends HypixelNPC {
    private static final DecimalFormat NUMBER_FORMAT = new DecimalFormat("#,###");

    public NPCSkywarsStats() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                SkywarsDataHandler handler = SkywarsDataHandler.getUser(player);
                long totalWins = 0;
                long totalKills = 0;
                int level = 1;
                long experience = 0;

                if (handler != null) {
                    SkywarsModeStats stats = handler.get(SkywarsDataHandler.Data.MODE_STATS, DatapointSkywarsModeStats.class).getValue();
                    totalWins = stats.getTotalWins(SkywarsLeaderboardPeriod.LIFETIME);
                    totalKills = stats.getTotalKills(SkywarsLeaderboardPeriod.LIFETIME);
                    experience = handler.get(SkywarsDataHandler.Data.EXPERIENCE, DatapointLong.class).getValue();
                    level = SkywarsLevelRegistry.calculateLevel(experience);
                }

                PlayerAchievementHandler achievementHandler = new PlayerAchievementHandler(player);
                int playerAchievements = achievementHandler.getUnlockedCount(AchievementCategory.SKYWARS);
                int totalAchievements = AchievementRegistry.getByCategory(AchievementCategory.SKYWARS).size();

                return new String[]{
                        "<6><l>Your SkyWars Profile",
                        Text.of("<f>Your Level: {}", SkyWarsLevelColor.getLevelDisplay(level)).serialize(),
                        Text.of("<f>Progress: {}", SkywarsLevelRegistry.formatXPDisplay(experience)).serialize(),
                        Text.of("<f>Total Wins: <a>{}", NUMBER_FORMAT.format(totalWins)).serialize(),
                        Text.of("<f>Total Kills: <a>{}", NUMBER_FORMAT.format(totalKills)).serialize(),
                        Text.of("<f>Achievements: <e>{}<7>/<a>{}", playerAchievements, totalAchievements).serialize(),
                        "<e><l>CLICK FOR STATS",
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
                return new Pos(37.5, 64, -23.5, 90, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return false;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        new GUISkywarsStatistics().open(event.player());
    }
}
