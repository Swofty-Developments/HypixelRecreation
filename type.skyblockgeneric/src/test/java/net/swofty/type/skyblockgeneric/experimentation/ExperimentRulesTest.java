package net.swofty.type.skyblockgeneric.experimentation;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

final class ExperimentRulesTest {
    @Test
    void superpairsRulesDescribeEachTierBoard() {
        assertSuperpairs(ExperimentTier.BEGINNER, 7, 2, 10, 25, 6);
        assertSuperpairs(ExperimentTier.HIGH, 5, 4, 12, 50, 9);
        assertSuperpairs(ExperimentTier.GRAND, 5, 4, 12, 75, 9);
        assertSuperpairs(ExperimentTier.SUPREME, 7, 4, 14, 100, 13);
        assertSuperpairs(ExperimentTier.TRANSCENDENT, 7, 4, 16, 200, 13);
        assertSuperpairs(ExperimentTier.METAPHYSICAL, 7, 4, 12, 350, 13);
    }

    @Test
    void addOnRulesUseTheirDocumentedGeometryAndLimits() {
        ExperimentRules.Rule supremeUltra = ExperimentRules.forExperiment(
                ExperimentType.ULTRASEQUENCER, ExperimentTier.SUPREME);
        assertEquals(25, supremeUltra.requiredEnchantingLevel());
        assertEquals(7 * 3, supremeUltra.boardSize());
        assertEquals(20, supremeUltra.maximumScore());

        ExperimentRules.Rule transcendentUltra = ExperimentRules.forExperiment(
                ExperimentType.ULTRASEQUENCER, ExperimentTier.TRANSCENDENT);
        assertEquals(7 * 4, transcendentUltra.boardSize());

        ExperimentRules.Rule metaphysicalUltra = ExperimentRules.forExperiment(
                ExperimentType.ULTRASEQUENCER, ExperimentTier.METAPHYSICAL);
        assertEquals(9 * 4, metaphysicalUltra.boardSize());

        ExperimentRules.Rule highChrono = ExperimentRules.forExperiment(
                ExperimentType.CHRONOMATRON, ExperimentTier.HIGH);
        ExperimentRules.Rule metaphysicalChrono = ExperimentRules.forExperiment(
                ExperimentType.CHRONOMATRON, ExperimentTier.METAPHYSICAL);
        assertEquals(49, highChrono.maximumScore());
        assertEquals(15, highChrono.xpCap());
        assertEquals(1, highChrono.bonusClicksForScore(5));
        assertEquals(2, highChrono.bonusClicksForScore(12));
        assertEquals(3, metaphysicalChrono.bonusClicksForScore(12));

        assertEquals(75_000, ExperimentRules.forExperiment(
                ExperimentType.SUPERPAIRS, ExperimentTier.BEGINNER).completionXp());
        assertEquals(200_000, ExperimentRules.forExperiment(
                ExperimentType.SUPERPAIRS, ExperimentTier.HIGH).completionXp());
        assertEquals(300_000, ExperimentRules.forExperiment(
                ExperimentType.SUPERPAIRS, ExperimentTier.GRAND).completionXp());
        assertEquals(400_000, ExperimentRules.forExperiment(
                ExperimentType.SUPERPAIRS, ExperimentTier.SUPREME).completionXp());
        assertEquals(500_000, ExperimentRules.forExperiment(
                ExperimentType.SUPERPAIRS, ExperimentTier.TRANSCENDENT).completionXp());
        assertEquals(600_000, ExperimentRules.forExperiment(
                ExperimentType.SUPERPAIRS, ExperimentTier.METAPHYSICAL).completionXp());
        assertEquals(2, ExperimentRules.forExperiment(
                ExperimentType.SUPERPAIRS, ExperimentTier.METAPHYSICAL).superpairsBonusClicks(1));
        assertEquals(List.of(SuperPairItem.EXTRA_CLICKS, SuperPairItem.EXTRA_CLICKS),
                ExperimentRules.forExperiment(ExperimentType.SUPERPAIRS, ExperimentTier.BEGINNER).powerUps());
    }

    @Test
    void ultrasequencerBoardNumbersAreUnique() {
        GameSession.UltraSequencerState state = new GameSession(
                UUID.randomUUID(), ExperimentType.ULTRASEQUENCER, ExperimentTier.METAPHYSICAL).state()
                instanceof GameSession.UltraSequencerState ultra ? ultra : null;

        assertNotNull(state);
        assertEquals(36, state.boardNumbers().size());
        assertEquals(36, new HashSet<>(state.boardNumbers()).size());
    }

    @Test
    void meterConversionUsesOneThousandthOfAwardedSkillXp() {
        assertEquals(1, ExperimentationManager.meterXpFromAwardedEnchantingXp(1_000));
        assertEquals(0.1, ExperimentationManager.meterXpFromAwardedEnchantingXp(100));
        assertThrows(IllegalArgumentException.class,
                () -> ExperimentationManager.meterXpFromAwardedEnchantingXp(-1));
    }

    @Test
    void meterEligibleRewardsFollowSuperpairsAvailability() {
        ExperimentRules.Rule high = ExperimentRules.forExperiment(ExperimentType.SUPERPAIRS, ExperimentTier.HIGH);
        ExperimentRules.Rule grand = ExperimentRules.forExperiment(ExperimentType.SUPERPAIRS, ExperimentTier.GRAND);
        ExperimentRules.Rule supreme = ExperimentRules.forExperiment(ExperimentType.SUPERPAIRS, ExperimentTier.SUPREME);

        assertFalse(high.canContain(ExperimentReward.LIFE_STEAL_IV));
        assertTrue(grand.canContain(ExperimentReward.METAPHYSICAL_SERUM));
        assertFalse(grand.canContain(ExperimentReward.A_BEGINNERS_GUIDE_TO_PESTHUNTING));
        assertTrue(supreme.canContain(ExperimentReward.A_BEGINNERS_GUIDE_TO_PESTHUNTING));
    }

    private static void assertSuperpairs(ExperimentTier tier, int width, int height, int clicks,
                                         int startingCost, int pairCount) {
        ExperimentRules.Rule rule = ExperimentRules.forExperiment(ExperimentType.SUPERPAIRS, tier);
        assertEquals(width, rule.boardWidth());
        assertEquals(height, rule.boardHeight());
        assertEquals(width * height, rule.boardSize());
        assertEquals(clicks, rule.baseClicks());
        assertEquals(startingCost, rule.startingCostLevels());
        assertEquals(pairCount, rule.pairCount());
        assertEquals(2, rule.powerUps().size());
        assertFalse(rule.rewardPool().stream().anyMatch(SuperPairItem::isPowerUp));
    }
}
