package net.swofty.type.skyblockgeneric.experimentation;

import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointExperimentation;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.rngmeter.RNGMeterService;
import net.swofty.type.skyblockgeneric.levels.SkyBlockLevelCause;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public final class ExperimentationManager {
    private static final Map<UUID, GameSession> SESSIONS = new ConcurrentHashMap<>();
    private static final int MAX_CHARGES = 3;
    private static final int[] RENEWAL_LEVEL_COSTS = {50, 100, 200};
    private static final int[] RENEWAL_BIT_COSTS = {150, 300, 500};

    private ExperimentationManager() {
    }

    public static boolean canStart(SkyBlockPlayer player, ExperimentTier tier) {
        return canStart(player, ExperimentType.SUPERPAIRS, tier);
    }

    public static boolean canStart(SkyBlockPlayer player, ExperimentType type, ExperimentTier tier) {
        synchronized (player) {
            if (SESSIONS.containsKey(player.getUuid())) return false;
            if (pendingResult(player) != null) return false;
            ExperimentRules.Rule rule = rule(type, tier);
            DatapointExperimentation.PlayerExperimentation state = state(player);
            if (!rule.isUnlocked(player)) return false;
            if (type == ExperimentType.SUPERPAIRS) {
                return state.charges() > 0 && state.chronomatronCompleted() && state.ultrasequencerCompleted();
            }
            return !completed(type, state);
        }
    }

    public static Text requirementMessage(ExperimentTier tier) {
        return requirementMessage(ExperimentType.SUPERPAIRS, tier);
    }

    public static Text requirementMessage(ExperimentType type, ExperimentTier tier) {
        ExperimentRules.Rule rule = rule(type, tier);
        return Text.of("<c>You need Enchanting {} to play this experiment.",
                StringUtility.getAsRomanNumeral(rule.requiredEnchantingLevel()));
    }

    public static boolean start(SkyBlockPlayer player, ExperimentType type, ExperimentTier tier) {
        synchronized (player) {
            ExperimentRules.Rule rule = rule(type, tier);
            if (SESSIONS.containsKey(player.getUuid())) {
                player.sendMessage("<c>You already have an experiment in progress.");
                return false;
            }
            if (pendingResult(player) != null) {
                player.sendMessage("<c>Claim or decline your pending experiment result first.");
                return false;
            }
            if (!rule.isUnlocked(player)) {
                player.sendMessage(requirementMessage(type, tier));
                return false;
            }

            DatapointExperimentation.PlayerExperimentation current = state(player);
            if (type == ExperimentType.SUPERPAIRS) {
                if (!current.chronomatronCompleted() || !current.ultrasequencerCompleted()) {
                    player.sendMessage("<c>Complete both add-on experiments before playing Superpairs.");
                    return false;
                }
                if (current.charges() <= 0) {
                    player.sendMessage("<c>You have no Experimentation Table charges remaining. Renew your experiments first.");
                    return false;
                }

                GameSession session = new GameSession(player.getUuid(), type, tier);
                GameSession.SuperPairsState game = (GameSession.SuperPairsState) session.state();
                game.clicksRemaining(game.totalClicks() + rule.superpairsBonusClicks(current.superpairsBonusClicks()));
                game.board().addAll(createBoard(player, rule));
                return commitStart(player, session,
                        current.withCharges(current.charges() - 1, current.nextChargeRenewalAt())
                                .withBonusClicks(0), rule.startingCostLevels(), current);
            }

            if (completed(type, current)) {
                player.sendMessage("<c>You have already completed this add-on. Renew your experiments to play it again.");
                return false;
            }
            int attempts = attempts(type, current);
            int repeatCost = attempts == 0 ? 0 : rule.repeatAttemptCostLevels();

            GameSession session = new GameSession(player.getUuid(), type, tier);
            return commitStart(player, session, current.withAddOn(addOn(type), false, attempts + 1),
                    repeatCost, current);
        }
    }

    public static RenewalCost renewalCost(SkyBlockPlayer player) {
        synchronized (player) {
            DatapointExperimentation.PlayerExperimentation state = state(player);
            if (state.renewalCount() >= RENEWAL_LEVEL_COSTS.length) return null;
            return new RenewalCost(RENEWAL_LEVEL_COSTS[state.renewalCount()], RENEWAL_BIT_COSTS[state.renewalCount()]);
        }
    }

    public static boolean renew(SkyBlockPlayer player) {
        synchronized (player) {
            if (SESSIONS.containsKey(player.getUuid()) || pendingResult(player) != null) {
                player.sendMessage("<c>Finish your current experiment result before renewing.");
                return false;
            }
            DatapointExperimentation.PlayerExperimentation current = state(player);
            RenewalCost cost = renewalCost(player);
            if (cost == null) {
                player.sendMessage("<c>You have used all three renewals for today.");
                return false;
            }
            if (current.charges() > 0) {
                player.sendMessage("<c>Use all of your Experimentation Table charges before renewing your experiments.");
                return false;
            }
            if (!current.chronomatronCompleted() || !current.ultrasequencerCompleted()) {
                player.sendMessage("<c>Claim all three experiment results before renewing your experiments.");
                return false;
            }
            if (player.getLevel() < cost.levels()) {
                player.sendMessage("<c>You need " + cost.levels() + " XP levels to renew these experiments.");
                return false;
            }
            if (player.getBits() < cost.bits()) {
                player.sendMessage("<c>You need " + cost.bits() + " Bits to renew these experiments.");
                return false;
            }
            int previousLevel = player.getLevel();
            int previousBits = player.getBits();
            long now = System.currentTimeMillis();
            DatapointExperimentation.PlayerExperimentation renewed = current.resetForRenewal(MAX_CHARGES,
                    now + DatapointExperimentation.CHARGE_RENEWAL_PERIOD_MILLIS, current.renewalCount() + 1);
            try {
                player.setLevel(previousLevel - cost.levels());
                player.removeBits(cost.bits());
                setState(player, renewed);
            } catch (RuntimeException exception) {
                player.setLevel(previousLevel);
                player.setBits(previousBits);
                try {
                    setState(player, current);
                } catch (RuntimeException rollbackException) {
                    exception.addSuppressed(rollbackException);
                }
                throw exception;
            }
            player.sendMessage("<a>Your Experimentation Table experiments have been renewed.");
            return true;
        }
    }

    public static GameSession.ChronomatronState getChronomatronState(SkyBlockPlayer player) {
        synchronized (player) {
            GameSession session = SESSIONS.get(player.getUuid());
            return session != null && session.type() == ExperimentType.CHRONOMATRON
                    ? (GameSession.ChronomatronState) session.state() : null;
        }
    }

    public static GameSession.UltraSequencerState getUltraSequencerState(SkyBlockPlayer player) {
        synchronized (player) {
            GameSession session = SESSIONS.get(player.getUuid());
            return session != null && session.type() == ExperimentType.ULTRASEQUENCER
                    ? (GameSession.UltraSequencerState) session.state() : null;
        }
    }

    public static GameSession.SuperPairsState getSuperPairsState(SkyBlockPlayer player) {
        synchronized (player) {
            GameSession session = SESSIONS.get(player.getUuid());
            if (session == null || session.type() != ExperimentType.SUPERPAIRS) return null;
            GameSession.SuperPairsState state = (GameSession.SuperPairsState) session.state();
            expireMismatch(state);
            return state;
        }
    }

    public static DatapointExperimentation.PendingResult pendingResult(SkyBlockPlayer player) {
        synchronized (player) {
            return state(player).pendingResult();
        }
    }

    public static int charges(SkyBlockPlayer player) {
        synchronized (player) {
            return state(player).charges();
        }
    }

    public static boolean consumeMetaphysicalSerum(SkyBlockPlayer player) {
        synchronized (player) {
            DatapointExperimentation.PlayerExperimentation current = state(player);
            if (current.metaphysicalSerums() >= 3) {
                player.sendMessage("<b><l>YUCKY!</l></b><7> This serum seems to have lost it's flavor!");
                player.sendMessage("<8>(You've drank the max amount!)");
                return false;
            }

            int dose = current.metaphysicalSerums() + 1;
            player.getSkyBlockExperience().addExperience(SkyBlockLevelCause.getMetaphysicalSerumCause(dose));
            DatapointExperimentation.PlayerExperimentation updated = current.withMetaphysicalSerums(dose);
            switch (dose) {
                case 1 -> player.sendMessage(
                        "<b><l>DELICIOUS!</l></b><7> The wonderfully complex taste of the "
                                + "<5>Metaphysical Serum<7> has taken over all of your taste buds!");
                case 2 -> player.sendMessage(
                        "<b><l>AMAZING? </l></b><7>The <5>Metaphysical Serum<7> seems to have taken over "
                                + "<o>half your brain</o><7>, you should probably stop drinking more, seriously...");
                case 3 -> {
                    updated = updated.withBonusClicks(updated.superpairsBonusClicks() + 2);
                    player.sendMessage(
                            "<b><l>CONCERNING!</l></b><7> The <5>Metaphysical Serum<7> has caused "
                                    + "<c><o>irreversible damage</o></c><7> to your whole body- but hey, at least "
                                    + "you got some <b>SkyBlock XP</b><7> in return!");
                    player.sendMessage("<e>Bonus!</e><7> You now get an extra 2 clicks on Superpairs!");
                }
                default -> throw new IllegalStateException("Unexpected serum dose " + dose);
            }
            setState(player, updated);
            return true;
        }
    }

    public static boolean claimPending(SkyBlockPlayer player) {
        synchronized (player) {
            DatapointExperimentation.PlayerExperimentation current = state(player);
            DatapointExperimentation.PendingResult pending = current.pendingResult();
            if (pending == null) return false;
            PendingClaim claim = validatePending(pending);
            if (claim == null) {
                player.sendMessage("<c>This experiment result is invalid and needs staff attention.");
                return false;
            }
            ExperimentType type = claim.type();

            if (type == ExperimentType.SUPERPAIRS) {
                List<DatapointExperimentation.PendingReward> rewards = claim.rewards();
                for (DatapointExperimentation.PendingReward pendingReward : rewards) {
                    ExperimentReward.fromName(pendingReward.rewardId()).give(player, pendingReward.amount(),
                            pendingReward.rarityValue());
                }
                awardSkillXp(player, pending.xpAward());
                int rewardSkillXp = 0;
                for (DatapointExperimentation.PendingReward pendingReward : rewards) {
                    if (ExperimentReward.fromName(pendingReward.rewardId()).grantsSkillExperience()) {
                        rewardSkillXp += pendingReward.amount();
                    }
                }
                RNGMeterService.addProgress(player, ExperimentationRNGMeter.INSTANCE,
                        meterXpFromAwardedEnchantingXp(pending.xpAward() + rewardSkillXp));
                for (DatapointExperimentation.PendingReward pendingReward : rewards) {
                    RNGMeterService.selectedDropObtained(player, ExperimentationRNGMeter.INSTANCE,
                            ExperimentReward.fromName(pendingReward.rewardId()));
                }
                setState(player, current.withBonusClicks(current.superpairsBonusClicks() + pending.bonusClicks())
                        .withPendingResult(null));
                player.sendMessage("<a>You claimed your Superpairs rewards.");
                return true;
            }

            awardSkillXp(player, pending.xpAward());
            setState(player, current.withBonusClicks(current.superpairsBonusClicks() + pending.bonusClicks())
                    .withAddOn(addOn(type), true, attempts(type, current)).withPendingResult(null));
            player.sendMessage(Text.of("<a>You claimed your {} rewards.", type.displayName()));
            return true;
        }
    }

    public static boolean declinePending(SkyBlockPlayer player) {
        synchronized (player) {
            DatapointExperimentation.PlayerExperimentation current = state(player);
            DatapointExperimentation.PendingResult pending = current.pendingResult();
            if (pending == null) return false;
            PendingClaim claim = validatePending(pending);
            if (claim == null) {
                player.sendMessage("<c>This experiment result is invalid and needs staff attention.");
                return false;
            }
            if (claim.type() == ExperimentType.SUPERPAIRS) {
                player.sendMessage("<c>The main experiment result cannot be declined.");
                return false;
            }
            setState(player, current.withPendingResult(null));
            player.sendMessage("<e>You declined the add-on rewards. The next attempt costs additional XP levels.");
            return true;
        }
    }

    public static boolean startChronomatronRound(SkyBlockPlayer player) {
        synchronized (player) {
            GameSession session = SESSIONS.get(player.getUuid());
            GameSession.ChronomatronState state = getChronomatronState(player);
            if (state == null || state.phase() != GameSession.GamePhase.READY) return false;
            ExperimentRules.Rule rule = rule(ExperimentType.CHRONOMATRON, session.tier());
            if (state.sequence().size() >= rule.maximumScore()) return false;
            int length = Math.min(rule.maximumScore(), state.sequence().isEmpty() ? 3 : state.sequence().size() + 1);
            while (state.sequence().size() < length) {
                state.sequence().add(ThreadLocalRandom.current().nextInt(rule.colorCount()));
            }
            state.inputIndex(0);
            state.deadline(0);
            state.phase(GameSession.GamePhase.WATCHING);
            return true;
        }
    }

    public static void chronomatronSequenceShown(SkyBlockPlayer player) {
        synchronized (player) {
            GameSession session = SESSIONS.get(player.getUuid());
            GameSession.ChronomatronState state = getChronomatronState(player);
            if (session != null && state != null && state.phase() == GameSession.GamePhase.WATCHING) {
                state.phase(GameSession.GamePhase.PLAYING);
                state.deadline(System.currentTimeMillis()
                        + rule(ExperimentType.CHRONOMATRON, session.tier()).deadlineSeconds() * 1_000L);
            }
        }
    }

    public static ChronomatronInputResult inputChronomatron(SkyBlockPlayer player, int color) {
        synchronized (player) {
            GameSession session = SESSIONS.get(player.getUuid());
            if (session == null || session.type() != ExperimentType.CHRONOMATRON) {
                return new ChronomatronInputResult(false, "No active Chronomatron session.", false, false);
            }
            GameSession.ChronomatronState state = (GameSession.ChronomatronState) session.state();
            ExperimentRules.Rule rule = rule(ExperimentType.CHRONOMATRON, session.tier());
            if (state.phase() != GameSession.GamePhase.PLAYING) {
                return new ChronomatronInputResult(false, "The sequence is still being shown.", false, false);
            }
            if (color < 0 || color >= rule.colorCount()) {
                return new ChronomatronInputResult(false, "That color is not part of this experiment.", false, false);
            }
            long now = System.currentTimeMillis();
            if (state.deadline() > 0 && now >= state.deadline()) {
                state.phase(GameSession.GamePhase.COMPLETE);
                return new ChronomatronInputResult(true, null, false, false);
            }
            if (now - state.lastInput() < 150) {
                return new ChronomatronInputResult(false, "Please slow down.", false, false);
            }
            state.lastInput(now);
            if (state.sequence().get(state.inputIndex()) != color) {
                state.phase(GameSession.GamePhase.COMPLETE);
                return new ChronomatronInputResult(true, null, false, false);
            }
            state.inputIndex(state.inputIndex() + 1);
            if (state.inputIndex() < state.sequence().size()) {
                return new ChronomatronInputResult(true, null, true, false);
            }
            session.bestScore(state.sequence().size());
            if (state.sequence().size() >= rule.maximumScore()) state.phase(GameSession.GamePhase.COMPLETE);
            else state.phase(GameSession.GamePhase.READY);
            return new ChronomatronInputResult(true, null, true, true);
        }
    }

    public static ChronomatronFinishResult finishChronomatron(SkyBlockPlayer player) {
        synchronized (player) {
            GameSession session = SESSIONS.get(player.getUuid());
            if (session == null || session.type() != ExperimentType.CHRONOMATRON) {
                return new ChronomatronFinishResult(false, "No active Chronomatron session.", 0, 0, 0);
            }
            ExperimentRules.Rule rule = rule(ExperimentType.CHRONOMATRON, session.tier());
            int score = session.bestScore();
            int xp = Math.min(score, rule.xpCap()) * rule.xpPerStep();
            int serums = state(player).metaphysicalSerums();
            int bonus = rule.bonusClicksForScore(score, serums);
            storePending(player, new DatapointExperimentation.PendingResult(
                    ExperimentType.CHRONOMATRON.name(), session.tier().name(), score >= rule.maximumScore(), score,
                    xp, bonus, serums, List.of()));
            SESSIONS.remove(player.getUuid(), session);
            return new ChronomatronFinishResult(true, null, score, xp, bonus);
        }
    }

    public static boolean startUltraSequencerRound(SkyBlockPlayer player) {
        synchronized (player) {
            GameSession session = SESSIONS.get(player.getUuid());
            GameSession.UltraSequencerState state = getUltraSequencerState(player);
            if (state == null || state.phase() != GameSession.GamePhase.READY) return false;
            ExperimentRules.Rule rule = rule(ExperimentType.ULTRASEQUENCER, session.tier());
            if (state.sequence().size() >= rule.maximumScore()) return false;
            int length = Math.min(rule.maximumScore(), state.sequence().isEmpty() ? 3 : state.sequence().size() + 1);
            while (state.sequence().size() < length) {
                List<Integer> available = new ArrayList<>(state.boardNumbers());
                available.removeAll(state.sequence());
                state.sequence().add(available.get(ThreadLocalRandom.current().nextInt(available.size())));
            }
            state.inputIndex(0);
            state.deadline(0);
            state.phase(GameSession.GamePhase.WATCHING);
            return true;
        }
    }

    public static void ultraSequencerShown(SkyBlockPlayer player) {
        synchronized (player) {
            GameSession session = SESSIONS.get(player.getUuid());
            GameSession.UltraSequencerState state = getUltraSequencerState(player);
            if (session != null && state != null && state.phase() == GameSession.GamePhase.WATCHING) {
                state.phase(GameSession.GamePhase.PLAYING);
                state.deadline(System.currentTimeMillis()
                        + rule(ExperimentType.ULTRASEQUENCER, session.tier()).deadlineSeconds() * 1_000L);
            }
        }
    }

    public static UltraSequencerInputResult inputUltraSequencer(SkyBlockPlayer player, int number) {
        synchronized (player) {
            GameSession session = SESSIONS.get(player.getUuid());
            if (session == null || session.type() != ExperimentType.ULTRASEQUENCER) {
                return new UltraSequencerInputResult(false, "No active Ultrasequencer session.", false, false);
            }
            GameSession.UltraSequencerState state = (GameSession.UltraSequencerState) session.state();
            ExperimentRules.Rule rule = rule(ExperimentType.ULTRASEQUENCER, session.tier());
            if (state.phase() != GameSession.GamePhase.PLAYING) {
                return new UltraSequencerInputResult(false, "The sequence is still being shown.", false, false);
            }
            if (!state.boardNumbers().contains(number)) {
                return new UltraSequencerInputResult(false, "That number is not on the board.", false, false);
            }
            if (state.deadline() > 0 && System.currentTimeMillis() >= state.deadline()) {
                state.phase(GameSession.GamePhase.COMPLETE);
                return new UltraSequencerInputResult(true, null, false, false);
            }
            if (state.sequence().get(state.inputIndex()) != number) {
                state.phase(GameSession.GamePhase.COMPLETE);
                return new UltraSequencerInputResult(true, null, false, false);
            }
            state.inputIndex(state.inputIndex() + 1);
            if (state.inputIndex() < state.sequence().size()) {
                return new UltraSequencerInputResult(true, null, true, false);
            }
            session.bestScore(state.sequence().size());
            if (state.sequence().size() >= rule.maximumScore()) state.phase(GameSession.GamePhase.COMPLETE);
            else state.phase(GameSession.GamePhase.READY);
            return new UltraSequencerInputResult(true, null, true, true);
        }
    }

    public static UltraSequencerFinishResult finishUltraSequencer(SkyBlockPlayer player) {
        synchronized (player) {
            GameSession session = SESSIONS.get(player.getUuid());
            if (session == null || session.type() != ExperimentType.ULTRASEQUENCER) {
                return new UltraSequencerFinishResult(false, "No active Ultrasequencer session.", 0, 0, 0);
            }
            ExperimentRules.Rule rule = rule(ExperimentType.ULTRASEQUENCER, session.tier());
            int score = session.bestScore();
            int xp = Math.min(score, rule.xpCap()) * rule.xpPerStep();
            int serums = state(player).metaphysicalSerums();
            int bonus = rule.bonusClicksForScore(score, serums);
            storePending(player, new DatapointExperimentation.PendingResult(
                    ExperimentType.ULTRASEQUENCER.name(), session.tier().name(), score >= rule.maximumScore(), score,
                    xp, bonus, serums, List.of()));
            SESSIONS.remove(player.getUuid(), session);
            return new UltraSequencerFinishResult(true, null, score, xp, bonus);
        }
    }

    public static SuperPairsFlipResult flipSuperPair(SkyBlockPlayer player, int tile) {
        synchronized (player) {
            GameSession session = SESSIONS.get(player.getUuid());
            if (session == null || session.type() != ExperimentType.SUPERPAIRS) {
                return new SuperPairsFlipResult(false, "No active Superpairs session.", false, false, -1, -1);
            }
            GameSession.SuperPairsState state = (GameSession.SuperPairsState) session.state();
            expireMismatch(state);
            if (System.currentTimeMillis() >= state.deadline()) {
                return new SuperPairsFlipResult(false, "The Superpairs experiment has ended.", false, false, -1, -1);
            }
            if (state.clicksRemaining() <= 0) {
                return new SuperPairsFlipResult(false, "You have no clicks remaining.", false, false, -1, -1);
            }
            if (state.mismatchUntil() > System.currentTimeMillis()) {
                return new SuperPairsFlipResult(false, "Wait for the tiles to turn over.", false, false, -1, -1);
            }
            if (tile < 0 || tile >= state.board().size() || state.matchedTiles().contains(tile) || state.firstFlip() == tile) {
                return new SuperPairsFlipResult(false, "That tile cannot be flipped.", false, false, -1, -1);
            }
            if (state.nextClickFree()) state.nextClickFree(false);
            else state.clicksRemaining(state.clicksRemaining() - 1);

            SuperPairTile selected = state.board().get(tile);
            if (selected.isPowerUp()) {
                state.matchedTiles().add(tile);
                applyPowerUp(state, selected.item().powerUp(), session.tier());
                return new SuperPairsFlipResult(true, null, true,
                        state.pairsFound() == state.rewardPairCount(), tile, -1);
            }
            if (state.firstFlip() < 0) {
                state.firstFlip(tile);
                return new SuperPairsFlipResult(true, null, false, false, tile, -1);
            }

            int first = state.firstFlip();
            state.firstFlip(-1);
            boolean match = state.board().get(first).pairId().equals(selected.pairId());
            if (match) {
                state.matchedTiles().add(first);
                state.matchedTiles().add(tile);
                state.matchedPairs().add(selected.pairId());
                return new SuperPairsFlipResult(true, null, true,
                        state.pairsFound() == state.rewardPairCount(), first, tile);
            }
            state.mismatchFirst(first);
            state.mismatchSecond(tile);
            state.mismatchUntil(System.currentTimeMillis() + 750);
            return new SuperPairsFlipResult(true, null, false, false, first, tile);
        }
    }

    public static SuperPairsFinishResult finishSuperPairs(SkyBlockPlayer player) {
        synchronized (player) {
            GameSession session = SESSIONS.get(player.getUuid());
            if (session == null || session.type() != ExperimentType.SUPERPAIRS) {
                return new SuperPairsFinishResult(false, "No active Superpairs session.", 0, 0, 0);
            }
            GameSession.SuperPairsState state = (GameSession.SuperPairsState) session.state();
            ExperimentRules.Rule rule = rule(ExperimentType.SUPERPAIRS, session.tier());
            int pairs = state.pairsFound();
            boolean completed = pairs == state.rewardPairCount();
            int xp = pairs * rule.xpPerPair() + state.bonusXp() + (completed ? rule.completionXp() : 0);
            Set<String> rewardsAdded = new HashSet<>();
            List<DatapointExperimentation.PendingReward> rewards = new ArrayList<>();
            for (int tile : state.matchedTiles()) {
                SuperPairTile pair = state.board().get(tile);
                if (pair.isPowerUp() || !state.matchedPairs().contains(pair.pairId()) || !rewardsAdded.add(pair.pairId())) continue;
                rewards.add(new DatapointExperimentation.PendingReward(pair.reward().name(), pair.amount(),
                        pair.rewardRarity() == null ? null : pair.rewardRarity().name()));
            }
            int serums = state(player).metaphysicalSerums();
            storePending(player, new DatapointExperimentation.PendingResult(
                    ExperimentType.SUPERPAIRS.name(), session.tier().name(), completed, pairs, xp, 0, serums, rewards));
            SESSIONS.remove(player.getUuid(), session);
            return new SuperPairsFinishResult(true, null, pairs, xp, 0);
        }
    }

    public static void cancel(SkyBlockPlayer player, ExperimentType type) {
        synchronized (player) {
            SESSIONS.computeIfPresent(player.getUuid(), (uuid, session) -> session.type() == type ? null : session);
        }
    }

    public static double meterXpFromAwardedEnchantingXp(int awardedEnchantingXp) {
        if (awardedEnchantingXp < 0) throw new IllegalArgumentException("Awarded XP cannot be negative");
        return awardedEnchantingXp * 0.001D;
    }

    private static List<SuperPairTile> createBoard(SkyBlockPlayer player, ExperimentRules.Rule rule) {
        List<SuperPairTile> board = new ArrayList<>(rule.boardSize());
        List<SuperPairItem> availableRewards = new ArrayList<>(rule.rewardPool());
        ExperimentReward selectedReward = guaranteedSelectedReward(player, rule);
        for (int pair = 0; pair < rule.pairCount(); pair++) {
            SuperPairItem selectedItem = selectedReward == null ? null : itemForReward(selectedReward);
            if (selectedItem != null) availableRewards.remove(selectedItem);
            if (availableRewards.isEmpty()) availableRewards.addAll(rule.rewardPool());
            SuperPairItem item = pair == 0 && selectedReward != null
                    ? selectedItem : selectReward(player, availableRewards);
            availableRewards.remove(item);
            int amount = selectedReward != null && pair == 0
                    ? rewardAmount(selectedReward, rule.tier()) : rewardAmount(item, rule.tier());
            ExperimentReward reward = selectedReward != null && pair == 0 ? selectedReward : item.reward();
            SuperPairItem displayItem = selectedReward != null && pair == 0 ? itemForReward(selectedReward) : item;
            Rarity rewardRarity = reward == ExperimentReward.GUARDIAN_PET && selectedReward != null && pair == 0
                    ? Rarity.LEGENDARY : null;
            board.add(new SuperPairTile("pair-" + pair, reward, displayItem, amount, rewardRarity));
            board.add(new SuperPairTile("pair-" + pair, reward, displayItem, amount, rewardRarity));
        }
        for (int index = 0; index < rule.powerUps().size(); index++) {
            board.add(new SuperPairTile("powerup-" + index, null, rule.powerUps().get(index), 1));
        }
        Collections.shuffle(board);
        return List.copyOf(board);
    }

    private static SuperPairItem selectReward(SkyBlockPlayer player, List<SuperPairItem> rewards) {
        double multiplier = superpairsUltraRareBookMultiplier(player);
        double totalWeight = rewards.stream().mapToDouble(item ->
                item.reward().isUltraRareBook() ? multiplier : 1).sum();
        double selected = ThreadLocalRandom.current().nextDouble(totalWeight);
        for (SuperPairItem item : rewards) {
            selected -= item.reward().isUltraRareBook() ? multiplier : 1;
            if (selected < 0) return item;
        }
        return rewards.getLast();
    }

    private static double superpairsUltraRareBookMultiplier(SkyBlockPlayer player) {
        SkyBlockItem pet = player.getPetData().getEnabledPet();
        if (pet == null) return 1;
        double multiplier = 1;
        for (PetAbility ability : player.getPetData().getCachedAbilities(pet)) {
            multiplier *= Math.max(1, ability.getSuperpairsUltraRareBookMultiplier(player, pet));
        }
        return multiplier;
    }

    private static ExperimentReward guaranteedSelectedReward(SkyBlockPlayer player, ExperimentRules.Rule rule) {
        var meter = RNGMeterService.get(player, ExperimentationRNGMeter.INSTANCE);
        if (meter.selectedReward().isBlank()) return null;
        ExperimentReward reward = java.util.Arrays.stream(ExperimentReward.values())
                .filter(candidate -> candidate.id().equalsIgnoreCase(meter.selectedReward()))
                .findFirst().orElse(null);
        return reward != null && rule.canContain(reward) && meter.storedXp() >= reward.requiredXp() ? reward : null;
    }

    private static SuperPairItem itemForReward(ExperimentReward reward) {
        for (SuperPairItem item : SuperPairItem.values()) {
            if (item.reward() == reward) return item;
        }
        return null;
    }

    private static int rewardAmount(SuperPairItem item, ExperimentTier tier) {
        if (item == null) return 1;
        return rewardAmount(item.reward(), tier);
    }

    private static int rewardAmount(ExperimentReward reward, ExperimentTier tier) {
        if (reward == null) return 1;
        if (reward == ExperimentReward.EXPERIENCE) {
            int minimum = switch (tier) {
                case BEGINNER -> 3_500;
                case HIGH, GRAND, SUPREME -> 5_000;
                case TRANSCENDENT, METAPHYSICAL -> 25_000;
            };
            int maximum = switch (tier) {
                case BEGINNER -> 50_000;
                case HIGH -> 60_000;
                case GRAND -> 90_000;
                case SUPREME -> 120_000;
                case TRANSCENDENT -> 130_000;
                case METAPHYSICAL -> 150_000;
            };
            return ThreadLocalRandom.current().nextInt(minimum, maximum + 1);
        }
        if (reward == ExperimentReward.TITANIC_EXPERIENCE_BOTTLE) {
            return tier == ExperimentTier.TRANSCENDENT || tier == ExperimentTier.METAPHYSICAL
                    ? ThreadLocalRandom.current().nextInt(1, 3) : 1;
        }
        if (reward == ExperimentReward.GRAND_EXPERIENCE_BOTTLE) {
            return ThreadLocalRandom.current().nextInt(5, 21);
        }
        if (reward == ExperimentReward.EXPERIENCE_BOTTLE) {
            return ThreadLocalRandom.current().nextInt(10, 31);
        }
        return 1;
    }

    private static void applyPowerUp(GameSession.SuperPairsState state, SuperPairItem.PowerUp powerUp,
                                     ExperimentTier tier) {
        switch (powerUp) {
            case EXTRA_CLICK -> state.clicksRemaining(state.clicksRemaining() + 1);
            case EXPERIENCE -> state.bonusXp(state.bonusXp()
                    + Math.max(1, ExperimentRules.forExperiment(ExperimentType.SUPERPAIRS, tier).xpPerPair()) * 10);
            case EXTRA_CLICKS -> state.clicksRemaining(state.clicksRemaining() + 3);
            case NEXT_CLICK_FREE -> {
                state.clicksRemaining(state.clicksRemaining() + 1);
                state.nextClickFree(true);
            }
            case INSTANT_FIND -> {
                for (int first = 0; first < state.board().size(); first++) {
                    SuperPairTile firstTile = state.board().get(first);
                    if (firstTile.isPowerUp() || state.matchedTiles().contains(first)) continue;
                    for (int second = first + 1; second < state.board().size(); second++) {
                        SuperPairTile secondTile = state.board().get(second);
                        if (!secondTile.isPowerUp() && !state.matchedTiles().contains(second)
                                && firstTile.pairId().equals(secondTile.pairId())) {
                            state.matchedTiles().add(first);
                            state.matchedTiles().add(second);
                            state.matchedPairs().add(firstTile.pairId());
                            return;
                        }
                    }
                }
            }
        }
    }

    private static void expireMismatch(GameSession.SuperPairsState state) {
        if (state.mismatchUntil() != 0 && state.mismatchUntil() <= System.currentTimeMillis()) {
            state.mismatchFirst(-1);
            state.mismatchSecond(-1);
            state.mismatchUntil(0);
        }
    }

    private static ExperimentRules.Rule rule(ExperimentType type, ExperimentTier tier) {
        return ExperimentRules.forExperiment(type, tier);
    }

    private static boolean completed(ExperimentType type, DatapointExperimentation.PlayerExperimentation state) {
        return switch (type) {
            case CHRONOMATRON -> state.chronomatronCompleted();
            case ULTRASEQUENCER -> state.ultrasequencerCompleted();
            case SUPERPAIRS -> false;
        };
    }

    private static int attempts(ExperimentType type, DatapointExperimentation.PlayerExperimentation state) {
        return type == ExperimentType.CHRONOMATRON ? state.chronomatronAttempts() : state.ultrasequencerAttempts();
    }

    private static DatapointExperimentation.PlayerExperimentation.ExperimentAddOn addOn(ExperimentType type) {
        return type == ExperimentType.CHRONOMATRON
                ? DatapointExperimentation.PlayerExperimentation.ExperimentAddOn.CHRONOMATRON
                : DatapointExperimentation.PlayerExperimentation.ExperimentAddOn.ULTRASEQUENCER;
    }

    private static void storePending(SkyBlockPlayer player, DatapointExperimentation.PendingResult pending) {
        synchronized (player) {
            DatapointExperimentation.PlayerExperimentation current = state(player);
            if (current.pendingResult() != null) throw new IllegalStateException("Player already has a pending experiment result");
            setState(player, current.withPendingResult(pending));
        }
    }

    private static PendingClaim validatePending(DatapointExperimentation.PendingResult pending) {
        try {
            ExperimentType type = ExperimentType.fromName(pending.experimentType());
            ExperimentTier tier = ExperimentTier.fromName(pending.tier());
            ExperimentRules.Rule rule = rule(type, tier);
            if (pending.bonusClicks() != (type == ExperimentType.SUPERPAIRS
                    ? 0 : rule.bonusClicksForScore(pending.score(), pending.metaphysicalSerums()))) return null;

            if (type == ExperimentType.SUPERPAIRS) {
                int completionXp = pending.completed() ? rule.completionXp() : 0;
                int baseXp = pending.score() * rule.xpPerPair() + completionXp;
                int powerUpXp = (int) rule.powerUps().stream()
                        .filter(item -> item.powerUp() == SuperPairItem.PowerUp.EXPERIENCE)
                        .count() * rule.xpPerPair() * 10;
                if (pending.score() > rule.pairCount()
                        || pending.completed() != (pending.score() == rule.pairCount())
                        || pending.rewards().size() != pending.score()
                        || (pending.xpAward() != baseXp && pending.xpAward() != baseXp + powerUpXp)) return null;
                List<DatapointExperimentation.PendingReward> rewards = new ArrayList<>(pending.rewards().size());
                for (DatapointExperimentation.PendingReward pendingReward : pending.rewards()) {
                    ExperimentReward reward = ExperimentReward.fromName(pendingReward.rewardId());
                    if (!rule.canContain(reward) || !validRewardAmount(reward, pendingReward.amount(), tier)) return null;
                    if (pendingReward.rarityValue() != null && reward != ExperimentReward.GUARDIAN_PET) return null;
                    if (pendingReward.rarityValue() != null
                            && (pendingReward.rarityValue().ordinal() < Rarity.COMMON.ordinal()
                            || pendingReward.rarityValue().ordinal() > Rarity.LEGENDARY.ordinal())) return null;
                    rewards.add(pendingReward);
                }
                return new PendingClaim(type, tier, rewards);
            }

            int cappedScore = Math.min(pending.score(), rule.xpCap());
            if (pending.score() > rule.maximumScore() || pending.completed() != (pending.score() == rule.maximumScore())
                    || !pending.rewards().isEmpty()
                    || pending.xpAward() != cappedScore * rule.xpPerStep()) return null;
            return new PendingClaim(type, tier, List.of());
        } catch (RuntimeException exception) {
            return null;
        }
    }

    private static DatapointExperimentation.PlayerExperimentation state(SkyBlockPlayer player) {
        DatapointExperimentation datapoint = datapoint(player);
        DatapointExperimentation.PlayerExperimentation current = datapoint.getValue();
        long now = System.currentTimeMillis();
        if (now < current.nextChargeRenewalAt() || current.pendingResult() != null) return current;
        long periods = (now - current.nextChargeRenewalAt()) / DatapointExperimentation.CHARGE_RENEWAL_PERIOD_MILLIS + 1;
        int charges = Math.min(MAX_CHARGES, current.charges() + Math.toIntExact(Math.min(periods, MAX_CHARGES)));
        DatapointExperimentation.PlayerExperimentation renewed = new DatapointExperimentation.PlayerExperimentation(
                current.superpairsBonusClicks(), charges,
                current.nextChargeRenewalAt() + periods * DatapointExperimentation.CHARGE_RENEWAL_PERIOD_MILLIS,
                0, false, false, 0, 0, current.metaphysicalSerums(), current.pendingResult());
        datapoint.setValue(renewed);
        return renewed;
    }

    private static boolean validRewardAmount(ExperimentReward reward, int amount, ExperimentTier tier) {
        if (amount < 1) return false;
        if (reward == ExperimentReward.EXPERIENCE) {
            int minimum = switch (tier) {
                case BEGINNER -> 3_500;
                case HIGH, GRAND, SUPREME -> 5_000;
                case TRANSCENDENT, METAPHYSICAL -> 25_000;
            };
            int maximum = switch (tier) {
                case BEGINNER -> 50_000;
                case HIGH -> 60_000;
                case GRAND -> 90_000;
                case SUPREME -> 120_000;
                case TRANSCENDENT -> 130_000;
                case METAPHYSICAL -> 150_000;
            };
            return amount >= minimum && amount <= maximum;
        }
        if (reward == ExperimentReward.EXPERIENCE_BOTTLE) return tier == ExperimentTier.BEGINNER
                || tier == ExperimentTier.HIGH ? amount >= 10 && amount <= 30 : false;
        if (reward == ExperimentReward.GRAND_EXPERIENCE_BOTTLE) {
            return tier != ExperimentTier.BEGINNER && amount >= 5 && amount <= 20;
        }
        if (reward == ExperimentReward.TITANIC_EXPERIENCE_BOTTLE) {
            return tier == ExperimentTier.TRANSCENDENT || tier == ExperimentTier.METAPHYSICAL
                    ? amount <= 2 : amount == 1;
        }
        return amount == 1;
    }

    private static void setState(SkyBlockPlayer player, DatapointExperimentation.PlayerExperimentation state) {
        datapoint(player).setValue(state);
    }

    private static DatapointExperimentation datapoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.EXPERIMENTATION, DatapointExperimentation.class);
    }

    private static boolean commitStart(SkyBlockPlayer player, GameSession session,
                                       DatapointExperimentation.PlayerExperimentation nextState,
                                       int levelCost,
                                       DatapointExperimentation.PlayerExperimentation previousState) {
        int previousLevel = player.getLevel();
        boolean persisted = false;
        boolean committed = false;
        try {
            if (!payLevels(player, levelCost)) return false;
            setState(player, nextState);
            persisted = true;
            if (SESSIONS.putIfAbsent(player.getUuid(), session) != null) return false;
            committed = true;
            return true;
        } finally {
            if (!committed) {
                SESSIONS.remove(player.getUuid(), session);
                if (persisted) setState(player, previousState);
                if (player.getLevel() != previousLevel) player.setLevel(previousLevel);
            }
        }
    }

    private static void awardSkillXp(SkyBlockPlayer player, int xp) {
        if (xp > 0) player.getSkills().increase(player, SkillCategories.ENCHANTING, (double) xp);
    }

    private static boolean payLevels(SkyBlockPlayer player, int levels) {
        if (levels <= 0) return true;
        if (player.getLevel() < levels) {
            player.sendMessage("<c>You need " + levels + " XP levels to play this experiment.");
            return false;
        }
        player.setLevel(player.getLevel() - levels);
        return true;
    }

    public record RenewalCost(int levels, int bits) {
    }

    public record ChronomatronInputResult(boolean success, String errorMessage, boolean correct, boolean complete) {
    }

    public record ChronomatronFinishResult(boolean success, String errorMessage, int bestChain, int xpAward,
                                           int bonusClicksEarned) {
    }

    public record UltraSequencerInputResult(boolean success, String errorMessage, boolean correct, boolean complete) {
    }

    public record UltraSequencerFinishResult(boolean success, String errorMessage, int bestSeriesLength, int xpAward,
                                             int bonusClicksEarned) {
    }

    public record SuperPairsFlipResult(boolean success, String errorMessage, boolean match, boolean complete,
                                       int firstTile, int secondTile) {
    }

    public record SuperPairsFinishResult(boolean success, String errorMessage, int pairsFound, int xpAward,
                                         int bonusClicksEarned) {
    }

    private record PendingClaim(ExperimentType type, ExperimentTier tier,
                                List<DatapointExperimentation.PendingReward> rewards) {
    }
}
