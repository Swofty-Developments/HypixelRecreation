package net.swofty.type.skyblockgeneric.data.datapoints;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import net.swofty.commons.protocol.Serializer;
import net.swofty.type.skyblockgeneric.data.SkyBlockDatapoint;
import net.swofty.type.skyblockgeneric.skilltree.SkillTreeState;
import net.swofty.type.skyblockgeneric.skilltree.TreePowder;
import org.json.JSONArray;
import org.json.JSONObject;

public class DatapointHOTM extends SkyBlockDatapoint<DatapointHOTM.PlayerHOTMData> {
	private static final Serializer<PlayerHOTMData> serializer = new Serializer<>() {
		@Override
		public String serialize(PlayerHOTMData value) {
			JSONObject json = new JSONObject();
			json.put("experience", value.experience);
			json.put("mithrilPowder", value.mithrilPowder);
			json.put("gemstonePowder", value.gemstonePowder);
			json.put("glacitePowder", value.glacitePowder);
			json.put("tokens", value.tokens);
			json.put("tokensSpent", value.tokensSpent);
			JSONArray treeStates = new JSONArray();
			SkillTreeState[] states = value.treeStates == null ? PlayerHOTMData.createTreeStates() : value.treeStates;
			for (SkillTreeState treeState : states) {
				if (treeState == null) treeState = new SkillTreeState();
				JSONObject state = new JSONObject()
						.put("levels", new JSONObject(treeState.getLevels()));
				state.put("disabledNodes", new JSONArray(treeState.getDisabledNodes()));
				if (treeState.getSelectedAbility() == null) {
					state.put("selectedAbility", JSONObject.NULL);
				} else {
					state.put("selectedAbility", treeState.getSelectedAbility());
				}
				treeStates.put(state);
			}
			json.put("treeStates", treeStates);
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
				data.glacitePowder = obj.optLong("glacitePowder", 0L);
				data.tokens = obj.optInt("tokens", 0);
				data.tokensSpent = obj.optInt("tokensSpent", 0);
				JSONArray treeStates = obj.optJSONArray("treeStates");
				if (treeStates != null) {
					for (int i = 0; i < Math.min(data.treeStates.length, treeStates.length()); i++) {
						JSONObject state = treeStates.optJSONObject(i);
						if (state == null) continue;
						JSONObject levels = state.optJSONObject("levels");
						if (levels != null) {
							for (String key : levels.keySet()) {
								data.treeStates[i].setLevel(key, levels.optInt(key, 0));
							}
						}
						JSONArray disabledNodes = state.optJSONArray("disabledNodes");
						if (disabledNodes != null) {
							for (int j = 0; j < disabledNodes.length(); j++) {
								data.treeStates[i].getDisabledNodes().add(disabledNodes.optString(j));
							}
						}
						if (!state.isNull("selectedAbility")) {
							data.treeStates[i].setSelectedAbility(state.optString("selectedAbility", null));
						}
					}
				}
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
			cloned.glacitePowder = value.glacitePowder;
			cloned.tokens = value.tokens;
			cloned.tokensSpent = value.tokensSpent;
			SkillTreeState[] sourceStates = value.treeStates == null ? PlayerHOTMData.createTreeStates() : value.treeStates;
			cloned.treeStates = new SkillTreeState[sourceStates.length];
			for (int i = 0; i < sourceStates.length; i++) {
				SkillTreeState source = sourceStates[i] == null ? new SkillTreeState() : sourceStates[i];
				SkillTreeState target = new SkillTreeState();
				target.setLevels(new java.util.HashMap<>(source.getLevels()));
				target.setDisabledNodes(new java.util.HashSet<>(source.getDisabledNodes()));
				target.setSelectedAbility(source.getSelectedAbility());
				cloned.treeStates[i] = target;
			}
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
		private long glacitePowder;
		private int tokens;
		private int tokensSpent;
		private SkillTreeState[] treeStates = createTreeStates();

		public PlayerHOTMData(long experience, long mithrilPowder, long gemstonePowder, int tokens, int tokensSpent) {
			this.experience = experience;
			this.mithrilPowder = mithrilPowder;
			this.gemstonePowder = gemstonePowder;
			this.tokens = tokens;
			this.tokensSpent = tokensSpent;
		}

		public static PlayerHOTMData createDefault() {
			PlayerHOTMData data = new PlayerHOTMData();
			data.experience = 0L;
			data.mithrilPowder = 0L;
			data.gemstonePowder = 0L;
			data.glacitePowder = 0L;
			data.tokens = 1;
			data.tokensSpent = 0;
			return data;
		}

		private static SkillTreeState[] createTreeStates() {
			SkillTreeState[] states = new SkillTreeState[5];
			for (int i = 0; i < states.length; i++) states[i] = new SkillTreeState();
			return states;
		}

		public SkillTreeState getTreeState(int slot) {
			if (treeStates == null || treeStates.length != 5) treeStates = createTreeStates();
			return treeStates[Math.clamp(slot, 0, treeStates.length - 1)];
		}

		public long getPowder(TreePowder powder) {
			return switch (powder) {
				case MITHRIL -> mithrilPowder;
				case GEMSTONE -> gemstonePowder;
				case GLACITE -> glacitePowder;
			};
		}

		public void addPowder(TreePowder powder, long amount) {
			switch (powder) {
				case MITHRIL -> mithrilPowder += amount;
				case GEMSTONE -> gemstonePowder += amount;
				case GLACITE -> glacitePowder += amount;
			}
		}

		public boolean spendPowder(TreePowder powder, long amount) {
			if (amount < 0 || getPowder(powder) < amount) return false;
			addPowder(powder, -amount);
			return true;
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
			experience += amount;
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
			mithrilPowder += amount;
		}

		public void addGemstonePowder(long amount) {
			gemstonePowder += amount;
		}

		public int getAvailableTokens() {
			return Math.max(0, tokens - tokensSpent);
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
