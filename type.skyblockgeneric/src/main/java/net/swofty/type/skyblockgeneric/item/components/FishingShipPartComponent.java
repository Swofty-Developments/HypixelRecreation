package net.swofty.type.skyblockgeneric.item.components;

import net.swofty.commons.text.Text;
import lombok.Getter;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.fishing.rod.FishingShipPartSlot;

@Getter
public class FishingShipPartComponent extends net.swofty.type.skyblockgeneric.item.SkyBlockItemComponent {
    private final String itemId;
    private final String displayName;
    private final FishingShipPartSlot slot;
    private final String texture;

    public FishingShipPartComponent(String itemId, String displayName, FishingShipPartSlot slot, String texture) {
        this.itemId = itemId;
        ItemType type = ItemType.get(itemId);
        this.displayName = displayName == null || displayName.isBlank()
            ? (type == null ? itemId : type.getDisplayName())
            : displayName;
        this.slot = slot;
        this.texture = texture;

        addInheritedComponent(new CustomDisplayNameComponent(ignored -> Text.parseLenient(this.displayName)));
        if (texture != null && !texture.isBlank()) {
            addInheritedComponent(new SkullHeadComponent(ignored -> texture));
        }
    }
}
