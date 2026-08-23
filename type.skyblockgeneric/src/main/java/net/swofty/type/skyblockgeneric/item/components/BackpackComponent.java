package net.swofty.type.skyblockgeneric.item.components;

import lombok.Getter;
import net.swofty.type.skyblockgeneric.item.SkyBlockItemComponent;

public class BackpackComponent extends SkyBlockItemComponent {
    @Getter
    private final int rows;

    public BackpackComponent(int rows, String skullTexture) {
        this.rows = rows;
        addInheritedComponent(new SkullHeadComponent((item) -> skullTexture));
        addInheritedComponent(new InteractableComponent(
                (player, item) -> {
                    player.sendMessage("<c>Backpacks cannot be opened on their own.");
                    player.sendMessage("<c>Instead, use the <6>Storage <c>menu in your <a>SkyBlock Menu <c>to store backpacks.");
                },
                null,
                null
        ));
        addInheritedComponent(new TrackedUniqueComponent());
    }
}
