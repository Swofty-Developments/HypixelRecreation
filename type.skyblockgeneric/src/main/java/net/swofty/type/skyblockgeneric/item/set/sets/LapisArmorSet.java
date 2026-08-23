package net.swofty.type.skyblockgeneric.item.set.sets;

import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSet;
import net.swofty.type.skyblockgeneric.item.set.impl.MuseumableSet;
import net.swofty.type.skyblockgeneric.item.set.impl.SetEvents;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class LapisArmorSet implements ArmorSet, SetEvents, MuseumableSet {

	@Override
	public String getName() {
		return "Magnetic";
	}

	@Override
	public ArrayList<String> getDescription() {
		return new ArrayList<>(List.of(
				"<f>Earn <a>50% <f>more Exp when mining"
		));
	}

	@Override
	public void setPutOn(SkyBlockPlayer player) {
		// TODO: Implement Exp boost when mining
	}

	@Override
	public void setTakeOff(SkyBlockPlayer player) {

	}
}
