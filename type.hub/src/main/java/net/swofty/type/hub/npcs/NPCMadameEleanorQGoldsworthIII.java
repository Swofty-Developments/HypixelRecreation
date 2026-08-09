package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.gui.inventories.museum.GUIMuseumAppraisal;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.stream.Stream;

public class NPCMadameEleanorQGoldsworthIII extends HypixelNPC {

    public NPCMadameEleanorQGoldsworthIII() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"§6Madame Eleanor Q. Goldsworth III", "§e§lCLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "VnhMZ2a55oTztZzmxmmkoTQ8vmKUFyh1QUFBLHeVGB6AsaFeWhef+NLUtftoWAf64m78I+OkmIQcexSHtCLZm6xSGitEcx8i/TRx5X25ZsV+jHKd2jJQMMA/BFrnnfawvV6KZ7zqbc7m/5Uv4aOHZxcYt/EBJyNiDBpqhEGDX/Ulo34Ti87JTHp5lT4qG1pmND9FQw4T/1JNdJTI+Wlxw2Ux9k2tRloDAUtyb9YDwYjMn27Ua49eYtVv3tFawuibXFtIj0u/Ni88PF25zAU2kE+1i3dtmj6htQ/Nzgc8gaFB/cETHD997D4/DkpPPnCPL1sd8iO63ncma5aFxvaBwAh97bGIwNUrsJJy2AtYlluD3PHrIIukKCuN+v37+Tn8KM9AbYVVfpJ6Z1Xot+s3BClWuzo4+sAvfBER6QiOvCYuSBjlGxCagSEIaBkxc6YIFhVs5Wa1ijpPcebB+HROePr2lQNkRFjiA4QYDprZJLm6HeGpXAhHtKAEb/D965sYe1EY3zDSPB33ZdO/Yq0u5oq+jyzAIurmS/oHWbMJ8VPWF39jUzc7ykSosEUDwOt1N6bO67pOB7Axjkt/45zJiYgAiU9XOEAsfkeSQcHKeJve3yKKizs0j6nPXGsF9z+mYwVokYLFbisanum6GtXHmhCNNJEkHbX6cgOid5V0I4U=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYzNTE2OTIwNTU3NywKICAicHJvZmlsZUlkIiA6ICI5MThhMDI5NTU5ZGQ0Y2U2YjE2ZjdhNWQ1M2VmYjQxMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJCZWV2ZWxvcGVyIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2I3ZTBmYzhkZjkwNjdkMzkzYzk3N2YxZjM0MDI1NjM0MjIzZGIxNDVlOTQwM2RjMzJjMzExODg5ZTMzNjU5NjUiCiAgICB9CiAgfQp9";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(33.500, 73.000, 11.500, 90, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        if (isInDialogue(player)) return;

        if (!player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_CURATOR)) {
            setGardenDialogue(player, "pre-curator");
            return;
        }
        if (!player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_MADAME_ELEANOR)) {
            setGardenDialogue(player, "post-curator").thenRun(() -> {
                player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_MADAME_ELEANOR, true);
            });
            return;
        }

        new GUIMuseumAppraisal().open(player);
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return Stream.of(
                DialogueSet.builder()
                        .key("pre-curator").lines(new String[]{
                                "The §bCurator §fhas advised me to direct you back to him.",
                                "Please hear what he has to say before coming to me"
                        }).build(),
                DialogueSet.builder()
                        .key("post-curator").lines(new String[]{
                                "Don't trust what the §bCurator §ftold you, I am in no way his assistant.",
                                "I serve someone much more important.",
                                "I offer a §6Museum Appraisal Service§f, which allows you to determine the value of your Museum.",
                                "For a one-time fee, I will appraise your Museum's worth each time you modify your Museum.",
                                "You may even find yourself atop the §6Top Valued Museums §fin the §9Museum Browser§f!",
                                "Additionally, reaching certain valuation thresholds will allow you to purchase §6bank upgrades§f.",
                                "In the future, reaching these thresholds will reward even more!"
                        }).build()
        ).toArray(DialogueSet[]::new);
    }

    private java.util.concurrent.CompletableFuture<Void> setGardenDialogue(HypixelPlayer player, String key) {
        return setDialogue(player, key).thenRun(() -> {
            if (player instanceof net.swofty.type.skyblockgeneric.user.SkyBlockPlayer skyBlockPlayer) {
                net.swofty.type.skyblockgeneric.garden.progression.GardenProgressionSupport.apply(
                    skyBlockPlayer,
                    net.swofty.type.skyblockgeneric.garden.progression.GardenProgressionReward.spokenNpc("MADAME_ELEANOR_Q_GOLDSWORTH_III")
                );
            }
        });
    }
}
