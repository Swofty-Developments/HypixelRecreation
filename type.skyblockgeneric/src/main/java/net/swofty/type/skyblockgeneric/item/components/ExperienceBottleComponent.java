package net.swofty.type.skyblockgeneric.item.components;

import lombok.Getter;
import net.swofty.type.skyblockgeneric.item.SkyBlockItemComponent;

@Getter
public final class ExperienceBottleComponent extends SkyBlockItemComponent {
    private final int baseExperience;

    public ExperienceBottleComponent(int baseExperience) {
        this.baseExperience = baseExperience;
    }
}
