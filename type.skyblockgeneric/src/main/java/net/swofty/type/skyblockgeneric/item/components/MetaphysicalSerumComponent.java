package net.swofty.type.skyblockgeneric.item.components;

import net.swofty.type.skyblockgeneric.experimentation.ExperimentationManager;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.SkyBlockItemComponent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public final class MetaphysicalSerumComponent extends SkyBlockItemComponent {
    public MetaphysicalSerumComponent() {
        addInheritedComponent(new InteractableComponent(this::consume, null, null));
    }

    private void consume(SkyBlockPlayer player, SkyBlockItem item) {
        if (ExperimentationManager.consumeMetaphysicalSerum(player)) {
            player.takeItem(item.getAttributeHandler().getPotentialType(), 1);
        }
    }
}
