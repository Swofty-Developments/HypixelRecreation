package net.swofty.type.skyblockgeneric.item.components;

import net.swofty.commons.text.Text;
import lombok.Getter;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.fishing.rod.FishingPartCategory;

import java.util.Map;

@Getter
public class FishingRodPartComponent extends net.swofty.type.skyblockgeneric.item.SkyBlockItemComponent {
    private final String itemId;
    private final String displayName;
    private final FishingPartCategory category;
    private final int requiredFishingLevel;
    private final Map<String, Double> tagBonuses;
    private final boolean treasureOnly;
    private final boolean bayouTreasureToJunk;
    private final String materializedItemId;
    private final double materializedChance;
    private final double baitPreservationChance;
    private final double hotspotBuffMultiplier;
    private final String texture;

    public FishingRodPartComponent(
        String itemId,
        String displayName,
        FishingPartCategory category,
        int requiredFishingLevel,
        Map<String, Double> tagBonuses,
        boolean treasureOnly,
        boolean bayouTreasureToJunk,
        String materializedItemId,
        double materializedChance,
        double baitPreservationChance,
        double hotspotBuffMultiplier,
        String texture
    ) {
        this.itemId = itemId;
        ItemType type = ItemType.get(itemId);
        this.displayName = displayName == null || displayName.isBlank()
            ? (type == null ? itemId : type.getDisplayName())
            : displayName;
        this.category = category;
        this.requiredFishingLevel = requiredFishingLevel;
        this.tagBonuses = Map.copyOf(tagBonuses);
        this.treasureOnly = treasureOnly;
        this.bayouTreasureToJunk = bayouTreasureToJunk;
        this.materializedItemId = materializedItemId;
        this.materializedChance = materializedChance;
        this.baitPreservationChance = baitPreservationChance;
        this.hotspotBuffMultiplier = hotspotBuffMultiplier;
        this.texture = texture;

        addInheritedComponent(new CustomDisplayNameComponent(ignored -> Text.parseLenient(this.displayName)));
        if (texture != null && !texture.isBlank()) {
            addInheritedComponent(new SkullHeadComponent(ignored -> texture));
        }
        addInheritedComponent(new ExtraRarityComponent("ROD PART"));
    }
}
