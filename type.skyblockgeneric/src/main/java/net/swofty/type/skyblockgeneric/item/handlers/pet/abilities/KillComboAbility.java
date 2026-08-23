package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.AbilityRuntime;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.Arrays;
import java.util.List;

import static net.swofty.commons.StringUtility.commaify;

public final class KillComboAbility implements PetAbility {
    private static final RarityValue<Integer> MAGIC_FIND_5 = new RarityValue<>(1, 1, 2, 2, 3, 3, 0);
    private static final RarityValue<Integer> MAGIC_FIND_15 = new RarityValue<>(1, 1, 2, 2, 3, 3, 0);
    private static final RarityValue<Integer> MAGIC_FIND_25 = new RarityValue<>(1, 1, 2, 2, 3, 3, 0);

    private static final RarityValue<Integer> COINS_10 = new RarityValue<>(2, 4, 6, 8, 10, 10, 0);
    private static final RarityValue<Integer> COINS_30 = new RarityValue<>(2, 4, 6, 8, 10, 10, 0);

    private static final RarityValue<Integer> COMBAT_WISDOM_20 = new RarityValue<>(5, 7, 9, 12, 15, 15, 0);

    private static final double[] BASE_DURATIONS = {8.00, 6.00, 4.00, 3.00, 3.00, 2.00};
    private static final double[] DURATION_PER_LEVEL = {0.02, 0.02, 0.02, 0.02, 0.01, 0.01};
    private static final int[] THRESHOLDS = {5, 10, 15, 20, 25, 30};

    @Override
    public String getName() {
        return "Kill Combo";
    }

    @Override
    public List<String> getDescription(SkyBlockItem instance) {
        Rarity rarity = instance.getAttributeHandler().getRarity();
        int level = instance.getAttributeHandler().getPetData().getAsLevel(rarity);

        String d5 = commaify(BASE_DURATIONS[0] + level * DURATION_PER_LEVEL[0]);
        String d10 = commaify(BASE_DURATIONS[1] + level * DURATION_PER_LEVEL[1]);
        String d15 = commaify(BASE_DURATIONS[2] + level * DURATION_PER_LEVEL[2]);
        String d20 = commaify(BASE_DURATIONS[3] + level * DURATION_PER_LEVEL[3]);
        String d25 = commaify(BASE_DURATIONS[4] + level * DURATION_PER_LEVEL[4]);
        String d30 = commaify(BASE_DURATIONS[5] + level * DURATION_PER_LEVEL[5]);

        int mf5 = MAGIC_FIND_5.getForRarity(rarity);
        int mf15 = MAGIC_FIND_15.getForRarity(rarity);
        int mf25 = MAGIC_FIND_25.getForRarity(rarity);
        int coins10 = COINS_10.getForRarity(rarity);
        int coins30 = COINS_30.getForRarity(rarity);
        int wisdom20 = COMBAT_WISDOM_20.getForRarity(rarity);

        return Arrays.asList(
                "<7>Gain buffs for combo kills. Effects",
                "<7>stack as you increase your combo.",
                "",
                "<a>5 Combo <8>(lasts <a>" + d5 + "<8>s)",
                " <stat:magic_find:+" + mf5 + "%>",
                "<a>10 Combo <8>(lasts " + d10 + "s)",
                " <8>+<6>" + coins10 + " <7>coins per kill",
                "<a>15 Combo <8>(lasts " + d15 + "s)",
                " <stat:magic_find:+" + mf15 + "%>",
                "<a>20 Combo <8>(lasts " + d20 + "s)",
                " <stat:combat_wisdom:+" + wisdom20 + ">",
                "<a>25 Combo <8>(lasts " + d25 + "s)",
                " <stat:magic_find:+" + mf25 + "%>",
                "<a>30 Combo <8>(lasts " + d30 + "s)",
                " <8>+<6>" + coins30 + " <7>coins per kill"
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, SkyBlockItem pet) {
        return statisticsFor(player.getPetData().getAbilityRuntime(this), pet);
    }

    @Override
    public void onEvent(PetEvent event) {
        if (event instanceof PetEvent.Kill kill) {
            onKill(kill.player().getPetData().getAbilityRuntime(this), kill);
        }
    }

    private ItemStatistics statisticsFor(AbilityRuntime rt, SkyBlockItem pet) {
        int combo = rt.getStacks();
        if (combo <= 0) return ItemStatistics.empty();

        Rarity rarity = pet.getAttributeHandler().getRarity();
        int level = pet.getAttributeHandler().getPetData().getAsLevel(rarity);

        double activeDuration = 0;
        for (int i = 0; i < THRESHOLDS.length; i++) {
            if (combo >= THRESHOLDS[i]) {
                activeDuration = BASE_DURATIONS[i] + level * DURATION_PER_LEVEL[i];
            }
        }

        if (activeDuration <= 0
                || System.currentTimeMillis() - rt.getLastProc() > (long) (activeDuration * 1000)) {
            rt.setStacks(0);
            return ItemStatistics.empty();
        }

        double totalMf = 0;
        int totalWisdom = 0;

        if (combo >= 5) totalMf += MAGIC_FIND_5.getForRarity(rarity);
        if (combo >= 15) totalMf += MAGIC_FIND_15.getForRarity(rarity);
        if (combo >= 25) totalMf += MAGIC_FIND_25.getForRarity(rarity);
        if (combo >= 20) totalWisdom += COMBAT_WISDOM_20.getForRarity(rarity);

        return ItemStatistics.builder()
                .withAdditivePercentage(ItemStatistic.MAGIC_FIND, totalMf)
                .withBase(ItemStatistic.COMBAT_WISDOM, (double) totalWisdom)
                .build();
    }

    private void onKill(AbilityRuntime rt, PetEvent.Kill kill) {
        rt.setStacks(rt.getStacks() + 1);
        rt.setLastProc(System.currentTimeMillis());

        Rarity rarity = kill.pet().getAttributeHandler().getRarity();
        if (rt.getStacks() >= 10) {
            kill.player().addCoins(COINS_10.getForRarity(rarity));
        }
        if (rt.getStacks() >= 30) {
            kill.player().addCoins(COINS_30.getForRarity(rarity));
        }
    }
}
