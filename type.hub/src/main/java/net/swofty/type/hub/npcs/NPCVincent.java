package net.swofty.type.hub.npcs;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.NPCOption;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCVincent extends HypixelNPC {

    public NPCVincent() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>Vincent", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "l9pYvAwAs6liRRBKySCecGDU8zERatdYPFOpx2s/kc9daZ67hwUe6MIHojxyu0+dtyhwAPf18pBEvR58HvZjpD1OWfkDQtPrVJscyzAiJdZVfze7kxsUHOIA9I9x6Dd3mUSukyZLDpmH6nNVetKn2W82nwOPBxT8wXpVIYizZGSGFrl6AXW1UUiWr6sJI0+7yGhph5q+QtKFGrfp+FNd7IwahSOAuk41xfdu4cze5CLk35QMWvKW3V2ItX7KOkJv1kooAnzYXWfMoiM0FT+liY+OOWUI8eAgPngCMmQN7+W6W7gu5Aea2TM2mDKufG/52H7CiagrSDKk/jlxlP9/1QeR09uchAjbOibSFxU1OFqGmcIyfUN1cq19WV0C5YQd+6AbyC3Hblde+QzjsyNLe7R3KvMpB5zlYWyb84uoRi2ogWuifE6rswoxBWJgtUCMBf/MKu5XzW6eoOpwNGLt5qHtr9QyxeqPp9XbBgn6syfAlTwqHx7utf+gu3SSBZbNe6cf2OE/ngxsPxDri3Tt3M9NTdS485Ja4Ic9JZkpz6I7GsaG+3nOClRWX16eseGTDgXGpcJ3OUZz25obuHWEQScqq7oI1XH8KnVENPhzy8VZ4vhZVUZBPgjnsVcE7K5q9Trbk75C1zJAaplM+XrQs2Cr1Ig2HnUn3Vy2ixWkRGY=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTcxNzYyMDA2OTUyOSwKICAicHJvZmlsZUlkIiA6ICIwMzQ2N2E0Yzc5ZGU0ZGM5YTQ4NzU5MGY5NmEwODFmMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJTaGFrYVlhbWkiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWJhM2M0ZjU4MmZjODc0OWUzZDhlMzVlZjFlNjMyODBjNDc1MmU0YWM4M2FhYjY5ZjY3NGRhYzI0YTM3NTgwMyIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(79.5, 74, 53.5, 0, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;
        if (player.getSkyBlockExperience().getLevel().asInt() < 10) {
            setDialogue(player, "low-level");
            return;
        }
        setDialogue(player, "hello").thenRun(() -> NPCOption.sendOption(player, "vincent-dyes", true,
                java.util.List.of(
                        NPCOption.Option.builder()
                                .key("yes")
                                .color(NamedTextColor.GREEN)
                                .bold(false)
                                .name("Yes...")
                                .action(ignored -> setDialogue(player, "yes"))
                                .build(),
                        NPCOption.Option.builder()
                                .key("no")
                                .color(NamedTextColor.RED)
                                .bold(false)
                                .name("No, what are they?")
                                .action(ignored -> setDialogue(player, "no"))
                                .build()
                )));
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder()
                        .key("low-level").lines(
                                "Hmm. I don't know if you've been here long enough to appreciate my artistry.",
                                "Maybe come back when you have SkyBlock Level 10!"
                        ).build(),
                DialogueSet.builder()
                        .key("hello").lines(
                                "Hello there. My name is Vincent.",
                                "I'm an artist. I believe you may be familiar with others, such as Marco and Pablo.",
                                "Let me ask you a question - have you ever heard of d y e s?"
                        ).build(),
                DialogueSet.builder()
                        .key("yes").lines(
                                "Very good, then I don't need to explain what they're used for.",
                                "However, I should probably give you some tips on how to get them...",
                                "As a painter, I prefer to use d y e s as part of my work.",
                                "Dyes drop from a variety of different parts of SkyBlock and are extremely rare, so most artists just go with the flow and work with what they have.",
                                "However, I feel like I'm... different.",
                                "Depending on what I'm painting, I need different types of dyes, but I always find exactly what I need!",
                                "It's almost as if the dyes I use become more common for everyone...",
                                "Nah, that would be CRAZY.",
                                "Anyways, if you ever want to see what I'm currently painting, come speak to me again!"
                        ).build(),
                DialogueSet.builder()
                        .key("no").lines(
                                "For someone like you, d y e s can be used to turn any kind of armor a certain color.",
                                "You can apply them in an anvil alongside any piece of armor, and it will recolor it!",
                                "Though it's worth noting that this will consume the dye, meaning you won't get it back afterwards.",
                                "They don't call it a consumable for nothing!",
                                "As a painter, I prefer to use d y e s as part of my work.",
                                "Dyes drop from a variety of different parts of SkyBlock and are extremely rare, so most artists just go with the flow and work with what they have.",
                                "However, I feel like I'm... different.",
                                "Depending on what I'm painting, I need different types of dyes, but I always find exactly what I need!",
                                "It's almost as if the dyes I use become more common for everyone...",
                                "Nah, that would be CRAZY.",
                                "Anyways, if you ever want to see what I'm currently painting, come speak to me again!"
                        ).build()
        };
    }
}
