package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.stream.Stream;

public class NPCFisherwomanEnid extends HypixelNPC {

    public NPCFisherwomanEnid() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<9>Fisherwoman Enid", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "I+sHx+MZakXIPUh9jbCMzbknhSJScGyDnbjKVxBUqss4/uvOyy+O0lqa3xOLKMPScFtOguogAc61eQyESNZYbtFE5oRHmNmBntPe09FQLJydxeC2Kre/V8s9oxHQ8+aSa4a6UfnhsSEbM5Xe9gEipy+SJZNuEtm1LEK/Nkwcxqn3MzXLPLUcYKGOs8bmH+umqM9c/CILh/paKOBZw0hkzz9bpx3JENPF0NEdRopAqdxE3EzNDg/o90jpbqKjYXSE0NXt1PPmJjC1tn/lqWkU+n+rIAI975X5ViE5cHNOkn+CecweVawZ9eq3hgVMNV9y5jN1utN38nmNUijZcz46rvsB27JjfsCk5tgFDcXjbFB3aMgqmzYwpC8/UALCiAi/GVWPyig1odm+8fvPPPmVD/yEesIcEL4SSRDxWv2CJocm/mQfU4cQ86AcCKPbKaxy5DRJrpm6d9x7ewhVgIwKgrVMR3SGNTlfTdm2UZTtyZZmDhbOAjvGA0b2GNpi47ahdBGqIvHKjpx02CUF21V9MND1zpo2DoAirnNHz5moUp3YpeV4p70vXB0RfwlZq3Caqwe8O7gxvPVYdR4ucg5SG6vHZ6FVMpRD3aqFwTdSa92JufJi7PL8ZDX1Ew9DQFNKL5N1ttp6Z33i7XqcD/lCLvTIBN9xESuTjousXJfD79w=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTc0MTEwMTMwMzExNSwKICAicHJvZmlsZUlkIiA6ICIzN2JhNjRkYzkxOTg0OGI4YjZhNDdiYTg0ZDgwNDM3MCIsCiAgInByb2ZpbGVOYW1lIiA6ICJTb3lLb3NhIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2IzNWNiMzhiODZjNGViYjYzN2ZjMDJkMzdmNzNkZmJmZWIxN2Y0NjI2N2Q4MTliMzgyOGFkZDQ2OWQ5M2UzOWEiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(41.500, 70.000, -22.250, 0, 0);
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
        if (isInDialogue(player)) {
            return;
        }

        if (!player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_FISHERWOMAN_ENID)) {
            setDialogue(player, "first-interaction").thenRun(() ->
                player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_FISHERWOMAN_ENID, true));
            return;
        }

        if (!player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_CAUGHT_FIRST_FISH)) {
            setDialogue(player, "first-interaction");
            return;
        }

        if (player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_UNLOCKED_SHIP)) {
            setDialogue(player, Math.random() < 0.5D ? "after-gerald" : "idle-" + (1 + (int) (Math.random() * 3)));
            return;
        }

        setDialogue(player, "after-catching-fish");
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return Stream.of(
            DialogueSet.builder()
                .key("first-interaction").lines(
                    "To fish, cast your rod into the water and wait for a fish to bite!"
                ).build(),
            DialogueSet.builder()
                .key("after-catching-fish").lines(
                    "This part of the hub is a popular area for fishing because of the Fishing Outpost!",
                    "If you follow the river that flows under the bridge downstream, you'll find your way to it.",
                    "And tell Gerald I won't be home tonight! I'm tired of him making salmon."
                ).build(),
            DialogueSet.builder()
                .key("after-gerald").lines(
                    "If you like fishin', you'll love the Backwater Bayou!",
                    "You should go there sometime soon!"
                ).build(),
            DialogueSet.builder()
                .key("idle-1").lines(
                    "Using Bait is important to get better results!",
                    "My favorite kind of bait is Dark Bait because I prefer fishing at night.",
                    "Angler Angus knows a lot more about Bait than I do. I think he's fishing somewhere along this river if you're interested in learning more."
                ).build(),
            DialogueSet.builder()
                .key("idle-2").lines(
                    "I know that Gavin uses /scg instead of looking in his Fishing Skill menu.",
                    "He thinks he's being sneaky, but everyone knows about it.",
                    "Little does he know, I use it too!"
                ).build(),
            DialogueSet.builder()
                .key("idle-3").lines(
                    "The Backwater Bayou has all sorts of wild Sea Creatures native to the area.",
                    "I've heard stories of Frog Man, but I've never quite worked out if he's a frog or a man.",
                    "Perhaps he is both, perhaps neither."
                ).build()
        ).toArray(DialogueSet[]::new);
    }
}
