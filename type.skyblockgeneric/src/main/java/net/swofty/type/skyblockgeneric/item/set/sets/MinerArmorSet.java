package net.swofty.type.skyblockgeneric.item.set.sets;

import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSet;
import net.swofty.type.skyblockgeneric.item.set.impl.MuseumableSet;
import net.swofty.type.skyblockgeneric.item.set.impl.SetEvents;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class MinerArmorSet implements ArmorSet, SetEvents, MuseumableSet {

	@Override
	public String getName() {
		return "Depth Coating";
	}

	@Override
	public ArrayList<String> getDescription() {
		return new ArrayList<>(List.of(
				"<f>Doubles this piece's <a>Defense <f>while on <a>Mining Islands<f>."
		));
	}

	@Override
	public void setPutOn(SkyBlockPlayer player) {
		// TODO: implement
	}

	@Override
	public void setTakeOff(SkyBlockPlayer player) {

	}
}
