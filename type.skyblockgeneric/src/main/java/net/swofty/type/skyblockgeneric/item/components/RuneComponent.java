package net.swofty.type.skyblockgeneric.item.components;

import lombok.Getter;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.item.SkyBlockItemComponent;

import java.util.List;

public class RuneComponent extends SkyBlockItemComponent {
    @Getter
    private final int requiredLevel;
    @Getter
    private final String color;
    @Getter
    private final String skullTexture;
    @Getter
    private final RuneableComponent.RuneApplicableTo applicableTo;

    public RuneComponent(int requiredLevel, String color, String applicableTo, String skullTexture) {
        this.requiredLevel = requiredLevel;
        this.color = color;
        this.applicableTo = RuneableComponent.RuneApplicableTo.valueOf(applicableTo);
        this.skullTexture = skullTexture;

        addInheritedComponent(new ExtraRarityComponent("COSMETIC"));
        addInheritedComponent(new TrackedUniqueComponent());
        addInheritedComponent(new ExtraUnderNameComponent(getExtraDisplay()));
        addInheritedComponent(new SkullHeadComponent((item) -> skullTexture));
        addInheritedComponent(new LoreUpdateComponent(List.of(
                "<7>Apply this rune to weapons or",
                "<7>fuse two together at the Runic",
                "<7>Pedestal!"
        ), false));
    }

    private List<String> getExtraDisplay() {
        return List.of(
                "<8>Requires level " + requiredLevel,
                StringUtility.toNormalCase(applicableTo.name())
        );
    }

    public String getDisplayName(ItemType type, int level) {
        return getColor() + "◆ " + StringUtility.toNormalCase(type.toString())
                + " " + StringUtility.getAsRomanNumeral(level);
    }
}
