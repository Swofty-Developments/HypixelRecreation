package net.swofty.type.skyblockgeneric.item.components;

import net.swofty.commons.StringUtility;
import net.swofty.type.skyblockgeneric.gui.inventories.abiphone.AbiphoneView;
import net.swofty.type.skyblockgeneric.item.SkyBlockItemComponent;

import java.util.ArrayList;
import java.util.List;

public class AbiphoneComponent extends SkyBlockItemComponent {

	public AbiphoneComponent(int maxContacts, int maxDiscs, List<AbiphoneFeature> features) {
		List<String> lore = new ArrayList<>(List.of(
				"<7>A device that can be used to contact",
				"<7>people! Click NPCs to add them to ",
				"<7>your contacts!",
				" ",
				"<7>Features:",
				" <7>Maximum Contacts: <b>" + maxContacts
		));

		if (maxDiscs > 0) {
			lore.add(" <7>Maximum Music Discs: <b>" + maxDiscs);
		}

		if (!features.isEmpty()) {
			for (AbiphoneFeature feature : features) {
				lore.add(" " + feature.colorCode + "<l>" + StringUtility.toNormalCase(feature.name()));
			}
		}

		lore.add(" ");
		lore.add("<e>Right-click to open!");

		addInheritedComponent(new LoreUpdateComponent(lore, false));
		addInheritedComponent(new InteractableComponent(
				(player, item) -> player.openView(new AbiphoneView(), new AbiphoneView.State(item)),
				null,
				null
		));
		addInheritedComponent(new TrackedUniqueComponent());
	}

	public enum AbiphoneFeature {
		CONTACTS_DIRECTORY("<a>"),
		DO_NOT_DISTURB_MODE("<c>"),
		RINGTONES("<6>"),
		TIC_TAC_TOE("<5>"),
		SNAKE("<5>"),
		SPEED_DIAL("<b>");

		public final String colorCode;

		AbiphoneFeature(String colorCode) {
			this.colorCode = colorCode;
		}
	}

}
