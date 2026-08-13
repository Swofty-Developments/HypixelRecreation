package net.swofty.type.skyblockgeneric.loottable;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Optional;

public enum LootAnnouncement {
    NONE(Text.empty(), null),
    RARE(Text.of("<9><l>RARE DROP!"), Key.key("entity.experience_orb.pickup")),
    CRAZY_RARE(Text.of("<5><l>CRAZY RARE DROP!"), Key.key("entity.player.levelup")),
    INSANE(Text.of("<c><l>INSANE DROP!"), Key.key("ui.toast.challenge_complete")),
    FARMING_UNCOMMON(Text.of("<a><l>UNCOMMON DROP!"), Key.key("entity.experience_orb.pickup")),
    FARMING_RARE(Text.of("<9><l>RARE DROP!"), Key.key("entity.experience_orb.pickup")),
    FARMING_CRAZY_RARE(Text.of("<5><l>CRAZY RARE DROP!"), Key.key("entity.player.levelup")),
    FARMING_PRAY_TO_RNGESUS(Text.of("<d><l>PRAY TO RNGESUS DROP!"), Key.key("ui.toast.challenge_complete"));

    private final Text title;
    private final Key sound;

    LootAnnouncement(Text title, Key sound) {
        this.title = title;
        this.sound = sound;
    }

    public Text title() {
        return title;
    }

    public Optional<Sound> sound() {
        return Optional.ofNullable(sound).map(key -> Sound.sound(key, Sound.Source.PLAYER, 1, 1));
    }

    public void announce(SkyBlockPlayer player, Object rewardName) {
        if (this == NONE) return;
        player.sendMessage(Text.of("{} <f>{}", title, rewardName));
        sound().ifPresent(player::playSound);
    }
}
