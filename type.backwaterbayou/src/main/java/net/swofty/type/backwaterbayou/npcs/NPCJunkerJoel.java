package net.swofty.type.backwaterbayou.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.backwaterbayou.gui.GUIJunkerJoel;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.stream.Stream;

public class NPCJunkerJoel extends HypixelNPC {

    public NPCJunkerJoel() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{
                    "<2>Junker Joel",
                    "<e><l>CLICK"
                };
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "cuTJRxZ5c0tPjwo+PK4Wuzp8Gymj87qIm+p9QQHLfgQWj75RkXAigGjcbWcIpBol4eP9xQ1KpZwuCfoIToRc9Y1HMMNAzdm459KJfSkhiHWMXggJBbW8CFvdKVIhQHMVKBCA0cgVDQE/ozNScwS63TXSSIE7iyHMAbFxoOw6hZMd9oQuaujnFt2lYda0yLmOSq1CKR+xUbLOsDqOPtQRaKw7hdYL99QzX8rMu7CAdPJkbnvb8ki/r/hkzqj0HikoBM02XZycdArMHq9MQPT6M7XKgRur/1AfMkaIFMTTGDVcIMQiWBY10I7UGui/hWm2jnIBjJrI/bvN5VRBeTxeoSaFSxQfZ1YJpLfuGjBY4XitD1W7UL11Z6BTI63VVIdW6L7vYm/4pRek/AAEfwa/cmEQelM44pvj3Yk/9/RWoZ0J+Sb/v6VX96apDmHk30pRDSx0gWrBXf9DGW56VFtiklR2edZI7OgGtfNcu83S2fmVZoxYO5zCEejbNj9tI7W2eY8Qmoqwo7J6OlfJWc3Ng/ucHBqlVR/T3sxnsyOE24BjvqPM349eyas079dzGc8PlYyvMj6RXtjX/Jz9clFdAXw94zENzvUltPNxcJgj9Xt41/9L7oEcEn9okM5kCqzfJXIAvZQmiZSv3rxYNg6TEoCY1r+eVRwRJMDTgefOpcs=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTc0MDE1NjgzNjkxNCwKICAicHJvZmlsZUlkIiA6ICI2NDU4Mjc0MjEyNDg0MDY0YTRkMDBlNDdjZWM4ZjcyZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJUaDNtMXMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWE0NTAzZTdmMTYxOTg1ZDAyNzMxYTMwNjczNWFlOTRlNDA3Yzc5MGY1ZWI0N2I2MzExOTA3N2RkODgzNDc2YiIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(6.5, 72, 4.5, 135, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        HypixelPlayer player = event.player();
        if (isInDialogue(player)) {
            return;
        }

        setDialogue(player, "intro").thenRun(() -> player.openView(new GUIJunkerJoel()));
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return Stream.of(
            DialogueSet.builder()
                .key("intro").lines(
                    "People dump all sorts of junk in these waters, and I make sure none of it goes to waste.",
                    "Bring me your scraps and I'll trade you something useful.",
                    "If you're planning to fish up a mountain of junk, you'll want a <9>Junk Sinker<f>."
                ).build()
        ).toArray(DialogueSet[]::new);
    }
}
