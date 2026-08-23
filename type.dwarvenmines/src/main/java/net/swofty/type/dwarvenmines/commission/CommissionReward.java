package net.swofty.type.dwarvenmines.commission;

import net.swofty.commons.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public record CommissionReward(int hotmXp, int miningXp, int mithrilPowder, boolean isDailyBonus) {
	public static CommissionReward calculate(int hotmTier, boolean isHotmMaxed, boolean isDailyBonus) {
		int hotmXp;
		int miningXp;
		int mithrilPowder;

		if (!isHotmMaxed) {
			if (hotmTier == 1) {
				hotmXp = 100;
			} else if (hotmTier == 2) {
				hotmXp = 200;
			} else {
				hotmXp = 400;
			}
		} else {
			hotmXp = 0;
		}

		miningXp = ThreadLocalRandom.current().nextInt(5_000, 10_001);
		if (!isHotmMaxed) {
			mithrilPowder = ThreadLocalRandom.current().nextInt(100, 201);
		} else {
			mithrilPowder = 500;
		}

		int dailyBonusHotmXp = 0;
		int dailyBonusPowder = 0;
		if (isDailyBonus) {
			if (!isHotmMaxed) {
				dailyBonusHotmXp = 900;
			} else {
				dailyBonusPowder = 2000;
			}
		}

		return new CommissionReward(
				hotmXp + dailyBonusHotmXp,
				miningXp,
				mithrilPowder + dailyBonusPowder,
				isDailyBonus
		);
	}

	public static List<Text> getRewardLore(int hotmTier, boolean isHotmMaxed, boolean isDailyBonus) {
		List<Text> lore = new ArrayList<>();

		if (!isHotmMaxed) {
			int baseHotmXp;
			if (hotmTier == 1) {
				baseHotmXp = 100;
			} else if (hotmTier == 2) {
				baseHotmXp = 200;
			} else {
				baseHotmXp = 400;
			}

			if (isDailyBonus) {
				lore.add(Text.of("<7>- <5>+{} Heart of the Mountain XP <e>(Daily Bonus)", baseHotmXp + 900));
			} else {
				lore.add(Text.of("<7>- <5>+{} Heart of the Mountain XP", baseHotmXp));
			}
		}

		if (!isHotmMaxed) {
			if (isDailyBonus) {
				lore.add(Text.of("<7>- <2>+100 Mithril Powder"));
			} else {
				lore.add(Text.of("<7>- <2>+100 Mithril Powder"));
			}
		} else {
			lore.add(Text.of("<7>- <2>+500 Mithril Powder"));
		}

		lore.add(Text.of("<7>- <3>+7.5k Mining XP"));

		return lore;
	}
}

