package net.swofty.type.skyblockgeneric.item.components;

import lombok.Getter;
import net.swofty.type.skyblockgeneric.gui.inventories.GUIConsumeSoulflow;
import net.swofty.type.skyblockgeneric.item.SkyBlockItemComponent;

import java.util.List;

public class SoulflowComponent extends SkyBlockItemComponent {

	@Getter
	private final int amount;

	public SoulflowComponent(int amount) {
		addInheritedComponent(new LoreUpdateComponent(
				List.of(
						"<7>Hold and right-click to consume,",
						"<7>gaining <3>+" + amount + "⸎ Soulflow<7>."
				), false
		));
		addInheritedComponent(
				new InteractableComponent(GUIConsumeSoulflow::open, null, null)
		);
		this.amount = amount;
	}

}
