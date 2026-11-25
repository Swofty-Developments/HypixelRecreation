package net.swofty.type.skyblockgeneric.loottable;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.swofty.commons.text.Text;

public enum BestiaryDropRarity implements LootRarity {
    COMMON("common", "Common Loot", NamedTextColor.WHITE, false, LootAnnouncement.NONE),
    UNCOMMON("uncommon", "Uncommon Loot", NamedTextColor.GREEN, true, LootAnnouncement.NONE),
    RARE("rare", "Rare Loot", NamedTextColor.BLUE, true, LootAnnouncement.RARE),
    LEGENDARY("legendary", "Legendary Loot", NamedTextColor.GOLD, true, LootAnnouncement.CRAZY_RARE),
    RNGESUS("rngesus", "RNGesus Loot", NamedTextColor.LIGHT_PURPLE, true, LootAnnouncement.INSANE);

    private final Key key;
    private final Text displayName;
    private final TextColor color;
    private final boolean showsChance;
    private final LootAnnouncement announcement;

    BestiaryDropRarity(String key, String displayName, TextColor color, boolean showsChance,
                       LootAnnouncement announcement) {
        this.key = Key.key("skyblock", "bestiary/" + key);
        this.displayName = Text.of("<color:{}>{}", color.asHexString(), displayName);
        this.color = color;
        this.showsChance = showsChance;
        this.announcement = announcement;
    }

    public Key key() {
        return key;
    }

    public Text displayName() {
        return displayName;
    }

    public TextColor color() {
        return color;
    }

    public boolean showsChance() {
        return showsChance;
    }

    public LootAnnouncement announcement() {
        return announcement;
    }
}
