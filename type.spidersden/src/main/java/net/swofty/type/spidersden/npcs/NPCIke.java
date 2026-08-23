package net.swofty.type.spidersden.npcs;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.NPCOption;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.spidersden.gui.GUICollectedMobTypes;

import java.util.List;

public class NPCIke extends HypixelNPC {
    public NPCIke() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{
                        "<6>Ike",
                        "<e><l>CLICK"
                };
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "d4g+ZRuYzKehekkNskqtDdQZnqF3vPFhUD37hUSEkwgWytaY2YCRSKM/1ZiBFh5Ufv7ILgQdBRLaumFju88v7RnkWMzEjfWJQnipTNQlYKFpUb8M7ucYMy6Vl+H5kFd0PBqkWdn8btz7BOqbPXPjwHYJjZgQ9+w28M1p+GQXd3TWm1NCqtUS+ufmNmu4yB9guf59ZQYx806UVT0z8KcelMFGj+RXljGfEh8Wo4mlVb237aofoc+htkh8R66G/GJiLJCNIO/YchB2iqDE7VcLR/UPkrJmS/m7alntdpJA03GdBfkyIi+deYQXmqQ+Kl7Dic2QhmfxJcte94vPbVwpMuVdTlEnTpm/CzAqStp9AGJfMm934zL8g2FvVvaFtOUUlSpnULpETuaozcCBxlrcX0uuEOKDVEStTAk4hVjeg+5aGwg2yT1AGkistUnfEly3WeVp2cHmMOBkKi8y8QlV4QprnTOpl9uehiMWP5iSdf+MFVidj2FRamKaZ8h7uuu+hOCk12Ga17VyKw2AT+CaRKlxDBpZnGV1rP/rIMDA/lLgD38W6HQnxkyiPzLMmKb2mslye70I7xQS9s5JYWsARPbp+aw5E6kKvUIonW6Z+gWabkYIs6zCod9aLBZcs7/Pwx3xRtJGj0dsbDlGlxXBV2iV2ikAaRFEdO6+dqA7RVo=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTc0NTg0NTcyNjY3NywKICAicHJvZmlsZUlkIiA6ICI2YWVmMjM3Y2RhNDY0Y2QzYTdiZDcxYjg3YzFlMDEwNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJLYWlqdW5va2kiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTAxMzAyNzFjODE2ZTM2YWIxN2Y4ZWY1ZWYwNjMwNGM4Y2JkYTk1MjMwNzg4Mjg1NDEyYmY1MzQ2NmIwNzAzNCIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-260.5, 94.5, -284.5, 90, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        if (dialogue().isInDialogue(player)) return;

        if (!player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_IKE)) {
            setDialogue(player, "initial-hello").thenRun(() -> {
                NPCOption.sendOption(player, "ike-intro", true, List.of(
                        new NPCOption.Option(
                                "yes",
                                NamedTextColor.GREEN,
                                false,
                                "Yes",
                                (_) -> {
                                    setDialogue(player, "continuation-1").thenRun(() -> {
                                        NPCOption.sendOption(player, "ike-what-kind-of-game", true, List.of(
                                                new NPCOption.Option(
                                                        "what-kind-of-game",
                                                        NamedTextColor.GREEN,
                                                        false,
                                                        "What kind of game?",
                                                        (_) -> {
                                                            setDialogue(player, "continuation-2").thenRun(() -> {
                                                                player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_IKE, true);
                                                            });
                                                        }
                                                )
                                        ));
                                    });
                                }
                        ),
                        new NPCOption.Option(
                                "no",
                                NamedTextColor.RED,
                                false,
                                "No",
                                (p) -> setDialogue(player, "answering-no")
                        )
                ));
            });
            return;
        }

        setDialogue(player, "idle-" + (1 + (int)(Math.random() * 2)));
        new GUICollectedMobTypes().open(player);
    }

    //TODO handle haveing to talk to michael first before anything is done here

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[] {
                DialogueSet.builder()
                        .key("initial-hello").lines(
                                "You heard what Michael said about Mob Types?"
                        ).build(),
                DialogueSet.builder()
                        .key("answering-no").lines(
                                "Then go talk to him, bro."
                        ).build(),
                DialogueSet.builder()
                        .key("continuation-1").lines(
                                "Not to flex, but I've seen enough films to know about every mob type in existence.",
                                "I'm bored, so let's play a game, just us two."
                        ).build(),
                DialogueSet.builder()
                        .key("continuation-2").lines(
                                "You go out and slay some mobs and, every time you kill a mob with a new mob type, I'll add it to my book!",
                                "I'll give you a reward each time you kill a new mob type, so get busy!",
                                "Talk to me again if you want to check your progress!"
                        ).build(),
                DialogueSet.builder()
                        .key("idle-1").lines(
                                "'Sup champ! How's that Mob Type collection coming along? Let's take a look!"
                        ).build(),
                DialogueSet.builder()
                        .key("idle-2").lines(
                                "Dude, I'm so bored out here. Mind showing me what you've been up to?"
                        ).build(),
                DialogueSet.builder()
                        .key("close").lines(
                                "Get back out there, there's always more to explore!"
                        ).build(),
        };
    }
}
