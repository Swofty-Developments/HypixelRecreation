package net.swofty.type.skyblockgeneric.item.components;

import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.SkyBlockItemComponent;

import java.util.function.Function;

public class CustomDisplayNameComponent extends SkyBlockItemComponent {
    private final Function<SkyBlockItem, Text> displayNameProvider;

    public CustomDisplayNameComponent(Function<SkyBlockItem, Text> displayNameProvider) {
        this.displayNameProvider = displayNameProvider;
    }

    public Text getDisplayName(SkyBlockItem item) {
        return displayNameProvider.apply(item);
    }
}
