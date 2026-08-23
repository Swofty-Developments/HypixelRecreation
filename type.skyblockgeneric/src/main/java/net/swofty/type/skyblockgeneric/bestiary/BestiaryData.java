package net.swofty.type.skyblockgeneric.bestiary;

import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.entity.mob.BestiaryMob;

import java.util.List;

public class BestiaryData {
    private final static int[] BRACKET_1 = new int[]{
            20, 40, 60, 100, 200, 400, 800, 1400, 2000, 3000,
            6000, 12000, 20000, 30000, 40000, 50000, 60000, 72000,
            86000, 100000, 200000, 400000, 600000, 800000, 1000000
    };

    private final static int[] BRACKET_2 = new int[]{
            5, 10, 15, 25, 50, 100, 200, 350, 500, 750,
            1500, 3000, 5000, 7500, 10000, 12500, 15000, 18000,
            21500, 25000, 50000, 100000, 150000, 200000, 250000
    };

    private final static int[] BRACKET_3 = new int[]{
            4, 8, 12, 16, 20, 40, 80, 140, 200, 300,
            600, 1200, 2000, 3000, 4000, 5000, 6000, 7200,
            8600, 10000, 20000, 40000, 60000, 80000, 100000
    };

    private final static int[] BRACKET_4 = new int[]{
            2, 4, 6, 10, 15, 20, 25, 35, 50, 75,
            150, 300, 500, 750, 1000, 1350, 1650, 2000,
            2500, 3000, 5000, 10000, 15000, 20000, 25000
    };

    private final static int[] BRACKET_5 = new int[]{
            1, 2, 3, 5, 7, 10, 15, 20, 25, 30,
            60, 120, 200, 300, 400, 500, 600, 720,
            860, 1000, 2000, 4000, 6000, 8000, 10000
    };

    private final static int[] BRACKET_6 = new int[]{
            1, 2, 3, 5, 7, 9, 14, 17, 21, 25,
            50, 80, 125, 175, 250, 325, 425, 525,
            625, 750, 1500, 3000, 4500, 6000, 7500
    };

    private final static int[] BRACKET_7 = new int[]{
            1, 2, 3, 5, 7, 9, 11, 14, 17, 20,
            30, 40, 55, 75, 100, 150, 200, 275,
            375, 500, 1000, 1500, 2000, 2500, 3000
    };

    private final List<int[]> brackets = List.of(BRACKET_1, BRACKET_2, BRACKET_3, BRACKET_4, BRACKET_5, BRACKET_6, BRACKET_7);

    private enum BestiaryTierRewards {
        TIER_1(2, 2, 2, 50),
        TIER_2(4, 4, 4, 100),
        TIER_3(6, 6, 6, 150),
        TIER_4(8, 8, 8, 200),
        TIER_5(10, 10, 10, 250),
        TIER_6(13, 13, 12, 300),
        TIER_7(16, 16, 14, 350),
        TIER_8(19, 19, 16, 400),
        TIER_9(22, 22, 18, 450),
        TIER_10(25, 25, 20, 500),
        TIER_11(29, 29, 22, 550),
        TIER_12(33, 33, 24, 600),
        TIER_13(37, 37, 26, 650),
        TIER_14(41, 41, 28, 700),
        TIER_15(45, 45, 30, 750),
        TIER_16(50, 50, 32, 800),
        TIER_17(55, 55, 34, 850),
        TIER_18(60, 60, 36, 900),
        TIER_19(65, 65, 38, 950),
        TIER_20(70, 70, 40, 1000),
        TIER_21(76, 76, 42, 1050),
        TIER_22(82, 82, 44, 1100),
        TIER_23(88, 88, 46, 1150),
        TIER_24(94, 94, 48, 1200),
        TIER_25(100, 100, 50, 1250);

        public final int magicFind;
        public final int strength;
        public final int extraCoinPercentage;
        public final int extraXpPercentage;

        BestiaryTierRewards(int magicFind, int strength, int extraCoinPercentage, int extraXpPercentage) {
            this.magicFind = magicFind;
            this.strength = strength;
            this.extraCoinPercentage = extraCoinPercentage;
            this.extraXpPercentage = extraXpPercentage;
        }
    }

    public int getTotalKillsForMaxTier(BestiaryMob mob) {
        int bracket = mob.getBestiaryBracket();
        int maxTier = mob.getMaxBestiaryTier();

        if (bracket < 1 || bracket > brackets.size()) {
            throw new IllegalArgumentException("Invalid bestiary bracket: " + bracket);
        }

        int[] tierKills = brackets.get(bracket - 1);
        if (maxTier <= 0 || maxTier > tierKills.length) {
            return 0;
        }

        return tierKills[maxTier - 1];
    }

    public int getKillsToNextTier(BestiaryMob mob, int kills) {
        int bracket = mob.getBestiaryBracket();
        int tier = getCurrentBestiaryTier(mob, kills);

        int[] tierKills = brackets.get(bracket - 1);
        if (tier >= tierKills.length) return 0;

        int previous = (tier == 0) ? 0 : tierKills[tier - 1];

        if (kills >= tierKills[tier]) {
            return 0;
        }

        return kills - previous;
    }

    public int getTotalKillsForNextTier(int bracket, int currentTier) {

        if (bracket < 1 || bracket > brackets.size()) {
            throw new IllegalArgumentException("Invalid bestiary bracket: " + bracket);
        }

        int[] tierKills = brackets.get(bracket - 1);
        if (currentTier >= tierKills.length) {
            return 0;
        }

        int previousKills = (currentTier == 0) ? 0 : tierKills[currentTier - 1];
        return tierKills[currentTier] - previousKills;
    }

    public int getCurrentBestiaryTier(BestiaryMob bestiaryMob, int kills) {
        int bracket = bestiaryMob.getBestiaryBracket();
        int maxTier = bestiaryMob.getMaxBestiaryTier();

        if (bracket < 1 || bracket > brackets.size()) {
            throw new IllegalArgumentException("Invalid bestiary bracket.");
        }

        int[] bracketKills = brackets.get(bracket - 1);

        for (int tier = bracketKills.length - 1; tier >= 0; tier--) {
            if (kills >= bracketKills[tier]) {
                return Math.min(tier + 1, maxTier);
            }
        }

        return 0;
    }

    public int getMagicFind(int tier) {
        if (tier < 1 || tier > BestiaryTierRewards.values().length) return 0;
        if (tier == 1) return BestiaryTierRewards.TIER_1.magicFind;
        return BestiaryTierRewards.values()[tier - 1].magicFind - BestiaryTierRewards.values()[tier - 2].magicFind;
    }

    public int getStrength(int tier) {
        if (tier < 1 || tier > BestiaryTierRewards.values().length) return 0;
        if (tier == 1) return BestiaryTierRewards.TIER_1.strength;
        return BestiaryTierRewards.values()[tier - 1].strength - BestiaryTierRewards.values()[tier - 2].strength;
    }

    public int getExtraCoinPercentage(int tier) {
        if (tier < 1 || tier > BestiaryTierRewards.values().length) return 0;
        if (tier == 1) return BestiaryTierRewards.TIER_1.extraCoinPercentage;
        return BestiaryTierRewards.values()[tier - 1].extraCoinPercentage - BestiaryTierRewards.values()[tier - 2].extraCoinPercentage;
    }

    public int getExtraXpPercentage(int tier) {
        if (tier < 1 || tier > BestiaryTierRewards.values().length) return 0;
        if (tier == 1) return BestiaryTierRewards.TIER_1.extraXpPercentage;
        return BestiaryTierRewards.values()[tier - 1].extraXpPercentage - BestiaryTierRewards.values()[tier - 2].extraXpPercentage;
    }

    public int getTotalMagicFind(int tier) {
        if (tier < 1 || tier > BestiaryTierRewards.values().length + 1) {
            return 0;
        }

        return BestiaryTierRewards.values()[tier - 1].magicFind;
    }

    public int getTotalStrength(int tier) {
        if (tier < 1 || tier > BestiaryTierRewards.values().length + 1) {
            return 0;
        }

        return BestiaryTierRewards.values()[tier - 1].strength;
    }

    public int getTotalExtraCoinPercentage(int tier) {
        if (tier < 1 || tier > BestiaryTierRewards.values().length + 1) {
            return 0;
        }

        return BestiaryTierRewards.values()[tier - 1].extraCoinPercentage;
    }

    public int getTotalExtraXPPercentage(int tier) {
        if (tier < 1 || tier > BestiaryTierRewards.values().length + 1) {
            return 0;
        }

        return BestiaryTierRewards.values()[tier - 1].extraXpPercentage;
    }

    public List<String> getTotalBonuses(List<String> lore, String name, int tier) {
        lore.add("<a>" + name + " Bonuses");
        lore.add(Text.of("<8>+<stat:magic_find:{}>", getTotalMagicFind(tier)).serialize());
        lore.add(Text.of("<8>+<stat:strength:{}>", getTotalStrength(tier)).serialize());
        lore.add("<8>+<6>" + getTotalExtraCoinPercentage(tier) + "% <7>coin gain");

        int totalXp = getTotalExtraXPPercentage(tier);
        int percent = totalXp % 100;
        int extra = totalXp / 100;

        if (totalXp >= 100) lore.add("<8>+<a>100% <7>chance for <a>+" + extra + " <7>XP orbs");
        if (percent > 0) lore.add("<8>+<a>50% <7>chance for <a>+" + (extra + 1) + " <7>XP orbs");

        return lore;
    }

    public List<String> getNextBonuses(List<String> lore, String name, int tier) {
        lore.add(Text.of("<7>Tier {:roman} Rewards", tier).serialize());
        lore.add(Text.of("  <8>+<stat:magic_find:{}>", getMagicFind(tier)).serialize());
        lore.add(Text.of("  <8>+<stat:strength:{}>", getStrength(tier)).serialize());
        lore.add("  <8>+<6>" + getExtraCoinPercentage(tier) + "% <7>coin gain");

        int totalXp = getExtraXpPercentage(tier);
        int percent = totalXp % 100;
        int extra = totalXp / 100;

        if (totalXp >= 100) lore.add("  <8>+<a>100% <7>chance for <a>+" + extra + " <7>XP orbs");
        if (percent > 0) lore.add("  <8>+<a>50% <7>chance for <a>+" + (extra + 1) + " <7>XP orbs");

        return lore;
    }
}