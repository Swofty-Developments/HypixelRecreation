package net.swofty.type.skyblockgeneric.event.actions.custom.bestiary;

import net.swofty.commons.text.Text;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.bestiary.BestiaryData;
import net.swofty.type.skyblockgeneric.entity.mob.BestiaryMob;
import net.swofty.type.skyblockgeneric.event.custom.BestiaryUpdateEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

//TODO make messages clickable to open the right mob gui

public class ActionBestiaryLevelUp implements HypixelEventClass {

	private final BestiaryData bestiaryData = new BestiaryData();

	@PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
	public void run(BestiaryUpdateEvent event) {
		SkyBlockPlayer player = event.getPlayer();
		BestiaryMob mob = event.getBestiaryMob();
		int oldKills = event.getOldTotalValue();
		int newKills = event.getNewTotalValue();
		String mobName = mob.getDisplayName();

		int oldTier = bestiaryData.getCurrentBestiaryTier(mob, oldKills);
		int newTier = bestiaryData.getCurrentBestiaryTier(mob, newKills);

		if (newKills == 1) {
			player.sendMessage("  <3><l>BESTIARY FAMILY UNLOCKED </l><b>{}", mobName);
			return;
		}

		if (newTier == oldTier + 1) {
			Text transitionLine = oldTier == 0
					? Text.of("<center><b>{} {:roman}</center>", mobName, newTier)
					: Text.of("<center><b>{} <8>{:roman} ➡ <b>{:roman}</center>", mobName, oldTier, newTier);

			int magicFind = bestiaryData.getMagicFind(newTier);
			int strength = bestiaryData.getStrength(newTier);
			int coinBonus = bestiaryData.getExtraCoinPercentage(newTier);
			int xpBonus = bestiaryData.getExtraXpPercentage(newTier);

			List<Text> lines = new ArrayList<>();
			lines.add(Text.empty());
			lines.add(Text.of("<center><6><l>BESTIARY</center>"));
			lines.add(transitionLine);
			lines.add(Text.empty());
			lines.add(Text.of("<center><6><l>REWARDS</center>"));
			lines.add(Text.of("<center><8>+<a>{} {} <stat:magic_find></center>", magicFind, mobName));
			lines.add(Text.of("<center><8>+<a>{} {} <stat:strength></center>", strength, mobName));
			lines.add(Text.of("<center><8>+<6>{}% <a>{} <7>coins</center>", coinBonus, mobName));
			lines.add(Text.of("<center><8>+<a>{}% <7>chance for extra XP orbs</center>", xpBonus));
			lines.add(Text.of("<center><8>+<b>1 SkyBlock XP</center>"));
			lines.add(Text.empty());

			player.sendMessage("<3><l>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
			for (Text line : lines) {
				player.sendMessage(line);
			}
			player.sendMessage("<3><l>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");
		}
	}
}
