package net.swofty.type.skyblockgeneric.skill;

import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.datapoints.DatapointDouble;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSkills;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Arrays;
import java.util.List;

public abstract class SkillCategory {
    public abstract Material getDisplayIcon();

    public abstract String getName();

    public abstract List<String> getDescription();

    public abstract SkillReward[] getRewards();

    public int getHighestLevel() {
        return getRewards().length;
    }

    public SkillReward getReward(int level) {
        return getRewards()[level - 1];
    }

    public int getLevel(double rawExperience) {
        int level = 0;
        double cumulative = 0;

        for (SkillReward reward : getRewards()) {
            cumulative += reward.requirement;
            if (rawExperience >= cumulative) {
                level++;
            } else {
                break;
            }
        }

        return level;
    }

    public record SkillReward(int level, int requirement, Reward... unlocks) {
        public List<String> getDisplay(List<String> lore) {
            lore.add(Text.of("<7>Level {:roman} Rewards:", level).serialize());

            Arrays.stream(unlocks).forEach(unlock -> {
                switch (unlock.type()) {
                    case XP -> lore.add("<7>  <8>+<b>" + ((XPReward) unlock).getXP() + " SkyBlock XP");
                    case COINS -> lore.add("<7>  <8>+<6>" + ((CoinReward) unlock).getCoins() + " <7>Coins");
                    case REGION_ACCESS -> {
                        RegionType region = ((RegionReward) unlock).getRegion();
                        lore.add(Text.of("<7>  <8>+<a>Access to <color:{}>{}",
                                region.getColor(), region.getName()).serialize());
                    }
                    case STATS_BASE -> {
                        ItemStatistic statistic = ((BaseStatisticReward) unlock).getStatistic();
                        lore.add(Text.of("<7>  <8>+<a>{:.1}{} <stat:'{}'>",
                                ((BaseStatisticReward) unlock).amountAdded(),
                                statistic.getSuffix(),
                                statistic.name()).serialize());
                    }
                    case STATS_ADDITIVE_PERCENTAGE -> {
                        ItemStatistic statistic = ((AdditivePercentageStatisticReward) unlock).getStatistic();
                        lore.add(Text.of("<7>  <8>+<a>{:.1}% <stat:'{}'>",
                                ((AdditivePercentageStatisticReward) unlock).amountAdded() * 100,
                                statistic.name()).serialize());
                    }
                    case RUNE -> lore.add("<7>  Access to Level <d>" + ((RuneReward) unlock).getRuneLevel() + " <7>Runes");
                }
            });

            return lore;
        }
    }

    public abstract static class Reward {
        public abstract UnlockType type();

        public abstract void onUnlock(SkyBlockPlayer player);

        public enum UnlockType {
            REGION_ACCESS,
            COINS,
            XP,
            STATS_BASE,
            STATS_ADDITIVE_PERCENTAGE,
            RUNE
        }
    }

    public abstract static class CoinReward extends Reward {
        @Override
        public UnlockType type() {
            return UnlockType.COINS;
        }

        @Override
        public void onUnlock(SkyBlockPlayer player) {
            DatapointDouble coins = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.COINS, DatapointDouble.class);
            coins.setValue(coins.getValue() + getCoins());
        }

        public abstract int getCoins();
    }

    public abstract static class XPReward extends Reward {
        @Override
        public UnlockType type() {
            return UnlockType.XP;
        }

        @Override
        public void onUnlock(SkyBlockPlayer player) {
            // TODO
        }

        public abstract int getXP();
    }

    public abstract static class BaseStatisticReward extends Reward {
        @Override
        public UnlockType type() {
            return UnlockType.STATS_BASE;
        }

        @Override
        public void onUnlock(SkyBlockPlayer player) {
            DatapointSkills.PlayerSkills skills = player.getSkills();
            ItemStatistics statistics = ItemStatistics.builder()
                    .withBase(getStatistic(), amountAdded())
                    .build();
            skills.setStatistics(ItemStatistics.add(skills.getSkillStatistics(), statistics));
        }

        public abstract ItemStatistic getStatistic();

        public abstract Double amountAdded();
    }

    public abstract static class AdditivePercentageStatisticReward extends Reward {
        @Override
        public UnlockType type() {
            return UnlockType.STATS_ADDITIVE_PERCENTAGE;
        }

        @Override
        public void onUnlock(SkyBlockPlayer player) {
            DatapointSkills.PlayerSkills skills = player.getSkills();
            ItemStatistics statistics = ItemStatistics.builder()
                .withAdditive(getStatistic(), amountAdded())
                    .build();
            skills.setStatistics(ItemStatistics.add(skills.getSkillStatistics(), statistics));
        }

        public abstract ItemStatistic getStatistic();

        public abstract Double amountAdded();
    }

    public abstract static class RegionReward extends Reward {
        @Override
        public UnlockType type() {
            return UnlockType.REGION_ACCESS;
        }

        @Override
        public void onUnlock(SkyBlockPlayer player) {
            // Handled by the region system
        }

        public abstract RegionType getRegion();
    }

    public abstract static class RuneReward extends Reward {
        @Override
        public UnlockType type() {
            return UnlockType.RUNE;
        }

        @Override
        public void onUnlock(SkyBlockPlayer player) {
            // Handled by the rune system
        }

        public abstract int getRuneLevel();
    }
}
