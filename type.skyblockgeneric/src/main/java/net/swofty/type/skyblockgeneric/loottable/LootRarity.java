package net.swofty.type.skyblockgeneric.loottable;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.format.TextColor;
import net.swofty.commons.text.Text;

public interface LootRarity {
    Key key();

    Text displayName();

    TextColor color();
}
