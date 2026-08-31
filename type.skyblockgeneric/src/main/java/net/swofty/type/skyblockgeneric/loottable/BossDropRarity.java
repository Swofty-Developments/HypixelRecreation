package net.swofty.type.skyblockgeneric.loottable;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.swofty.commons.text.Text;

public enum BossDropRarity implements LootRarity {
    GUARANTEED("guaranteed", "Guaranteed", NamedTextColor.GREEN, LootAnnouncement.NONE),
    COMMON("common", "Common", NamedTextColor.WHITE, LootAnnouncement.NONE),
    OCCASIONAL("occasional", "Occasional", NamedTextColor.GREEN, LootAnnouncement.NONE),
    RARE("rare", "Rare", NamedTextColor.AQUA, LootAnnouncement.RARE),
    EXTRAORDINARY("extraordinary", "Extraordinary", NamedTextColor.DARK_PURPLE, LootAnnouncement.RARE),
    PRAY_RNGESUS("pray_rngesus", "Pray RNGesus", NamedTextColor.LIGHT_PURPLE, LootAnnouncement.CRAZY_RARE),
    RNGESUS_INCARNATE("rngesus_incarnate", "RNGesus Incarnate", NamedTextColor.RED, LootAnnouncement.INSANE);

    private final Key key;
    private final Text displayName;
    private final TextColor color;
    private final LootAnnouncement announcement;

    BossDropRarity(String key, String displayName, TextColor color, LootAnnouncement announcement) {
        this.key = Key.key("skyblock", "boss/" + key);
        this.displayName = Text.of("<color:{}>{}", color.asHexString(), displayName);
        this.color = color;
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

    public LootAnnouncement announcement() {
        return announcement;
    }
}
