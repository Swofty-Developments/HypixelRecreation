package net.swofty.type.island.npcs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.coordinate.Pos;
import net.swofty.commons.ServerType;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.NPCOption;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.island.gui.GUISam;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class NPCSam extends HypixelNPC {
    private static final String OPTION_ID = "sam";
    private static final String TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTY2MTI0NzcyMjk1MiwKICAicHJvZmlsZUlkIiA6ICJjYmFkZmRmNTRkZTM0N2UwODQ3MjUyMDIyYTFkNGRkZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJvRml3aSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS85NWRjZmM4YjMyMDAzMjZiOGY2MWQ4MDYxNWM1YjUzNjk5N2ExMTg1NzBhNDdmMWQzNTMwNjMyOTY5MjFmZTEzIgogICAgfQogIH0KfQ==";
    private static final String SIGNATURE = "BgdB1A6pFJnqpQ5hCaEhFcEt4kZgvELFPg5Sx1bCXsUM956OmkAOJ3vqsyTAVcfhV/US7sKxDqwSh8JIV0tTV8fsC28cjur2f83cTw6ehlBKoERMRzwMV+ppLs6YjWIncTbst0D5CZqLpDs4ZhuR6u5uaDtbC716FJWFng+4YYn4XAapwiOqufUtmTdNjBuN1gmAJXtMhdcG5pNrm3SYO6KqVzo1a5eHcdXIkGp0mHFulAYZNy9S9RHuaEX1YNWWjyCTI+KQyv/mNfV/OfJtLH5e/BwPvL+Dcp7f+4wPeu3U2exiGpXPe2VOFRqFnaGP2URagVMj5BjVeuuiaY519YEGbYbR0SyG6HPSXVOM8A5NeVqGJ6FCcCg1ejfWNpIDS9mchIxgbufIzFOK1rD1E/JX051/IRAUrxW9hx8hne8X3/gOy84D6wS0j/KDjR7X7U1gZyAsTb1HqYdcNard1PNSLsdathh71APASTH0EZVm2JCh8exx7tNCKSW4jB9uvw0FugAdDUBKjWs1qO2Zrd7/IBePfW1uvoEU7TUnXRMKu0OB8yug7Y7CCE/jM1zMDnWRhn1QuF94+se6R2swmDJM8NA/exsDmzFMXi+ndw+bOfJYdyzM+s3H6AgvOnmOtgrZBIkLYSMlvSMigxl2Vd/9rRdVn4wFyv4/Dd1aPho=";

    public NPCSam() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"§bSam", "§e§lCLICK"};
            }

            @Override
            public String chatName(HypixelPlayer player) {
                return "§bSam";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                if (!(player instanceof SkyBlockPlayer skyBlockPlayer)
                    || skyBlockPlayer.getSkyBlockIsland() == null
                    || skyBlockPlayer.getSkyBlockIsland().getSamPosition() == null) {
                    return new Pos(8.5, 100, 41.5, 180, 0);
                }
                return skyBlockPlayer.getSkyBlockIsland().getSamPosition();
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }

            @Override
            public boolean visible(HypixelPlayer player) {
                return player instanceof SkyBlockPlayer skyBlockPlayer
                    && HypixelConst.isIslandServer()
                    && skyBlockPlayer.isOnIsland()
                    && skyBlockPlayer.getSkyBlockIsland() != null
                    && skyBlockPlayer.getSkyBlockIsland().getIslandInstance() != null
                    && skyBlockPlayer.getSkyBlockExperience().getLevel().getLevel() >= 5;
            }

            @Override
            public String texture(HypixelPlayer player) {
                return TEXTURE;
            }

            @Override
            public String signature(HypixelPlayer player) {
                return SIGNATURE;
            }

            @Override
            public net.minestom.server.instance.Instance instance(HypixelPlayer player) {
                return player instanceof SkyBlockPlayer skyBlockPlayer && skyBlockPlayer.getSkyBlockIsland() != null
                    ? skyBlockPlayer.getSkyBlockIsland().getIslandInstance()
                    : HypixelConst.getEmptyInstance();
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) {
            return;
        }

        DatapointToggles.Toggles toggles = player.getToggles();
        if (toggles.get(DatapointToggles.Toggles.ToggleType.HAS_ACCEPTED_SAM_GARDEN)) {
            new GUISam().open(player);
            return;
        }

        if (!toggles.get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_SAM)) {
            sendStrangerMessage(player, "What a pleasant surprise! I see you have quite an impressive island.");
            setDialogue(player, "first-interaction").thenRun(() -> sendGardenQuestion(player));
            return;
        }

        setDialogue(player, "after-no").thenRun(() -> sendGardenQuestion(player));
    }

    private void sendGardenQuestion(SkyBlockPlayer player) {
        NPCOption.sendOption(player, OPTION_ID, "Help her out?", List.of(
            new NPCOption.Option("yes", NamedTextColor.GREEN, true, "YES", this::acceptGarden),
            new NPCOption.Option("no", NamedTextColor.RED, true, "NO", this::declineGarden)
        ));
    }

    private void acceptGarden(HypixelPlayer hypixelPlayer) {
        SkyBlockPlayer player = (SkyBlockPlayer) hypixelPlayer;
        player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_SAM, true);
        player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_ACCEPTED_SAM_GARDEN, true);
        setDialogue(player, "yes").thenRun(() -> player.asProxyPlayer()
            .transferToWithIndication(ServerType.SKYBLOCK_GARDEN)
            .exceptionally(error -> {
                player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_ACCEPTED_SAM_GARDEN, false);
                setDialogue(player, "garden-closed");
                return null;
            }));
    }

    private void declineGarden(HypixelPlayer hypixelPlayer) {
        SkyBlockPlayer player = (SkyBlockPlayer) hypixelPlayer;
        player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_SAM, true);
        setDialogue(player, "no");
    }

    private void sendStrangerMessage(SkyBlockPlayer player, String message) {
        player.sendMessage(Component.text("[NPC] ", NamedTextColor.YELLOW)
            .append(Component.text("Stranger", NamedTextColor.BLUE))
            .append(Component.text(": ", NamedTextColor.WHITE))
            .append(LegacyComponentSerializer.legacySection().deserialize(message)));
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
            DialogueSet.builder().key("first-interaction").lines(new String[]{
                "Excuse my manners, my name is Sam and I live next door... or should I rather say next \"island\".",
                "You seem to have quite a talent for growing crops... which is something I couldn't really say about myself...",
                "You see, I've been having quite the struggle with my farm that I inherited from my father.",
                "He was a very skillful farmer... not like me... I have two left thumbs when it comes to farming.",
                "I was wondering if maybe you could... help me out... with my farm."
            }).build(),
            DialogueSet.builder().key("after-no").lines(new String[]{
                "Could you help me out with my farm?"
            }).build(),
            DialogueSet.builder().key("yes").lines(new String[]{
                "Wow!! Thank you so much. I'll bring you right there."
            }).build(),
            DialogueSet.builder().key("no").lines(new String[]{
                "Oh... ok then.."
            }).build(),
            DialogueSet.builder().key("garden-closed").lines(new String[]{
                "Hmmm, it seems we can't go to The Garden right now. Let's try again later..."
            }).build()
        };
    }
}
