package net.swofty.type.skyblockgeneric.data.datapoints;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.swofty.commons.protocol.Serializer;
import net.swofty.commons.rates.GameRates;
import net.swofty.commons.rates.RateCategory;
import net.swofty.type.skyblockgeneric.data.SkyBlockDatapoint;
import org.json.JSONObject;

public class DatapointHOTM extends SkyBlockDatapoint<DatapointHOTM.PlayerHOTMData> {
	private static final Serializer<PlayerHOTMData> serializer = new Serializer<>() {
		@Override
		public String serialize(PlayerHOTMData value) {
			JSONObject json = new JSONObject();
			json.put("experience", value.experience);
			json.put("mithrilPowder", value.mithrilPowder);
			json.put("gemstonePowder", value.gemstonePowder);
			json.put("tokens", value.tokens);
			json.put("tokensSpent", value.tokensSpent);
			return json.toString();
		}

		@Override
		public PlayerHOTMData deserialize(String json) {
			if (json == null || json.isEmpty()) {
				return PlayerHOTMData.createDefault();
			}

			try {
				JSONObject obj = new JSONObject(json);
				PlayerHOTMData data = new PlayerHOTMData();
				data.experience = obj.optLong("experience", 0L);
				data.mithrilPowder = obj.optLong("mithrilPowder", 0L);
				data.gemstonePowder = obj.optLong("gemstonePowder", 0L);
				data.tokens = obj.optInt("tokens", 0);
				data.tokensSpent = obj.optInt("tokensSpent", 0);
				return data;
			} catch (Exception e) {
				return PlayerHOTMData.createDefault();
			}
		}

		@Override
		public PlayerHOTMData clone(PlayerHOTMData value) {
			PlayerHOTMData cloned = new PlayerHOTMData();
			cloned.experience = value.experience;
			cloned.mithrilPowder = value.mithrilPowder;
			cloned.gemstonePowder = value.gemstonePowder;
			cloned.tokens = value.tokens;
			cloned.tokensSpent = value.tokensSpent;
			return cloned;
		}
	};

	public DatapointHOTM(String key, PlayerHOTMData value) {
		super(key, value, serializer);
	}

	public DatapointHOTM(String key) {
		super(key, PlayerHOTMData.createDefault(), serializer);
	}

	@Getter
	@Setter
	@NoArgsConstructor
	@AllArgsConstructor
	public static class PlayerHOTMData {
		private long experience;
		private long mithrilPowder;
		private long gemstonePowder;
		private int tokens;
		private int tokensSpent;

		public static PlayerHOTMData createDefault() {
			PlayerHOTMData data = new PlayerHOTMData();
			data.experience = 0L;
			data.mithrilPowder = 0L;
			data.gemstonePowder = 0L;
			data.tokens = 1;
			data.tokensSpent = 0;
			return data;
		}

		public int getTier() {
			if (experience >= 1_247_000) return 10;
			if (experience >= 847_000) return 9;
			if (experience >= 557_000) return 8;
			if (experience >= 347_000) return 7;
			if (experience >= 197_000) return 6;
			if (experience >= 97_000) return 5;
			if (experience >= 37_000) return 4;
			if (experience >= 12_000) return 3;
			if (experience >= 3_000) return 2;
			return 1;
		}

		public long getNextTierXp() {
			int currentTier = getTier();
			return switch (currentTier) {
				case 1 -> 3_000;
				case 2 -> 12_000;
				case 3 -> 37_000;
				case 4 -> 97_000;
				case 5 -> 197_000;
				case 6 -> 347_000;
				case 7 -> 557_000;
				case 8 -> 847_000;
				case 9 -> 1_247_000;
				default -> -1; // maxed out
			};
		}

		public boolean isMaxed() {
			return getTier() >= 10;
		}

		public int addExperience(long amount) {
			int previousTier = getTier();
			experience += Math.round(amount * GameRates.multiplier(RateCategory.HOTM));
			int newTier = getTier();

			int levelUps = newTier - previousTier;
			if (levelUps > 0) {
				for (int tier = previousTier + 1; tier <= newTier; tier++) {
					tokens += getTokensForTier(tier);
				}
			}
			return levelUps;
		}

		private int getTokensForTier(int tier) {
			return switch (tier) {
				case 1 -> 1;
				case 2, 3, 4, 5, 6, 8, 9, 10 -> 2;
				case 7 -> 3;
				default -> 0;
			};
		}

		public void addMithrilPowder(long amount) {
			mithrilPowder += Math.round(amount * GameRates.multiplier(RateCategory.POWDER));
		}

		public void addGemstonePowder(long amount) {
			gemstonePowder += Math.round(amount * GameRates.multiplier(RateCategory.POWDER));
		}

		public int getAvailableTokens() {
			return tokens - tokensSpent;
		}

		public boolean spendToken() {
			if (getAvailableTokens() <= 0) return false;
			tokensSpent++;
			return true;
		}

		public int getProgressPercent() {
			if (isMaxed()) return 100;

			int currentTier = getTier();
			long currentTierXp = switch (currentTier) {
				case 1 -> 0;
				case 2 -> 3_000;
				case 3 -> 12_000;
				case 4 -> 37_000;
				case 5 -> 97_000;
				case 6 -> 197_000;
				case 7 -> 347_000;
				case 8 -> 557_000;
				case 9 -> 847_000;
				default -> 1_247_000;
			};
			long nextTierXp = getNextTierXp();
			if (nextTierXp == -1) return 100;

			long progressInTier = experience - currentTierXp;
			long xpNeededForTier = nextTierXp - currentTierXp;

			return (int) ((progressInTier * 100) / xpNeededForTier);
		}
	}
}

