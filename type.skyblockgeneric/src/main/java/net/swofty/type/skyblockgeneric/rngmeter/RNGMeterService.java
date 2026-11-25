package net.swofty.type.skyblockgeneric.rngmeter;

import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointRNGMeters;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Locale;
import java.util.Map;

public final class RNGMeterService {
    private RNGMeterService() {
    }

    public static RNGMeterState get(SkyBlockPlayer player, RNGMeterDefinition definition) {
        synchronized (player) {
            return data(player).getOrDefault(key(definition), defaultState());
        }
    }

    public static void select(SkyBlockPlayer player, RNGMeterDefinition definition, RNGMeterReward reward) {
        synchronized (player) {
            if (!definition.rewards().contains(reward)) {
                throw new IllegalArgumentException("Reward does not belong to " + definition.type());
            }
            RNGMeterState current = get(player, definition);
            set(player, key(definition), new RNGMeterState(reward.id(), current.storedXp()));
        }
    }

    public static ProgressResult addProgress(SkyBlockPlayer player, RNGMeterDefinition definition, double xp) {
        synchronized (player) {
            if (xp < 0) throw new IllegalArgumentException("RNG Meter XP cannot be negative");

            RNGMeterState current = get(player, definition);
            double progress = current.storedXp() + xp;
            if (current.selectedReward().isBlank()) {
                set(player, key(definition), new RNGMeterState("", progress));
                return new ProgressResult(progress, false, null);
            }
            RNGMeterReward reward = definition.reward(current.selectedReward());
            if (progress < reward.requiredXp()) {
                set(player, key(definition), new RNGMeterState(reward.id(), progress));
                return new ProgressResult(progress, false, reward);
            }

            if (current.storedXp() < reward.requiredXp()) {
                player.sendMessage(Text.of("<d><l>RNG METER! <f>Your {} RNG Meter is full "
                        + "and will guarantee your next drop!", definition.displayName()));
            }
            set(player, key(definition), new RNGMeterState(reward.id(), progress));
            return new ProgressResult(progress, true, reward);
        }
    }

    public static void reset(SkyBlockPlayer player, RNGMeterDefinition definition) {
        synchronized (player) {
            RNGMeterState current = get(player, definition);
            set(player, key(definition), new RNGMeterState("", current.storedXp()));
        }
    }

    public static boolean selectedDropObtained(SkyBlockPlayer player, RNGMeterDefinition definition,
                                               RNGMeterReward obtainedReward) {
        synchronized (player) {
            RNGMeterState current = get(player, definition);
            if (!current.selectedReward().equalsIgnoreCase(obtainedReward.id())) return false;

            RNGMeterReward selected = definition.reward(current.selectedReward());
            double remaining = Math.max(0, current.storedXp() - selected.requiredXp());
            set(player, key(definition), new RNGMeterState("", remaining));
            return true;
        }
    }

    public static boolean giveReward(SkyBlockPlayer player, RNGMeterDefinition definition,
                                     RNGMeterReward reward) {
        synchronized (player) {
            reward.give(player);
            return selectedDropObtained(player, definition, reward);
        }
    }

    public static double applyDropRate(RNGMeterState state, RNGMeterReward reward, double baseDropRate) {
        if (reward.requiredXp() <= 0) return baseDropRate;
        double completion = Math.min(state.storedXp(), reward.requiredXp()) / reward.requiredXp();
        return baseDropRate * (1 + 2 * completion);
    }

    public static double applyDropRate(SkyBlockPlayer player, RNGMeterDefinition definition,
                                       RNGMeterReward reward, double baseDropRate) {
        RNGMeterState state = get(player, definition);
        if (!state.selectedReward().equalsIgnoreCase(reward.id())) return baseDropRate;
        if (state.storedXp() >= reward.requiredXp()) return 1;
        return Math.min(1, applyDropRate(state, reward, baseDropRate));
    }

    private static Map<String, RNGMeterState> data(SkyBlockPlayer player) {
        return datapoint(player).getValue();
    }

    private static void set(SkyBlockPlayer player, String type, RNGMeterState state) {
        DatapointRNGMeters datapoint = datapoint(player);
        Map<String, RNGMeterState> meters = data(player);
        meters.put(type, state);
        datapoint.setValue(meters);
    }

    private static DatapointRNGMeters datapoint(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.RNG_METERS, DatapointRNGMeters.class);
    }

    private static RNGMeterState defaultState() {
        return new RNGMeterState("", 0);
    }

    private static String key(RNGMeterDefinition definition) {
        if (definition.id() == null || definition.id().isBlank()) {
            throw new IllegalArgumentException("RNG Meter id cannot be blank");
        }
        return definition.id().toUpperCase(Locale.ROOT);
    }

    public record ProgressResult(double storedXp, boolean completed, RNGMeterReward reward) {
    }
}
