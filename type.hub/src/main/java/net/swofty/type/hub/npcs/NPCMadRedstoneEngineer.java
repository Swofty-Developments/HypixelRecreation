package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.hub.gui.GUIShopMadRedstoneEngineer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCMadRedstoneEngineer extends HypixelNPC {

    public NPCMadRedstoneEngineer() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Mad Redstone Engineer", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "voTLQRDa5VvAd3n8vk9A0q2SdvmMmsWnIOLM9LFsWwebfAfZskwvaotuJQ6cv1PEfPc62pmZXPjd73R+BNZOaibVBcFqnFSZ4NBDoj8H8VVSSjz79iRIqjUTXAbw7yxZPQ+icMlaQ2EMgUVHB9z6NP6ly6uptpOJEMNRXqwf+rqPjUQbjA1SvRWY7njcz5zqExbvTX05gPU2Z5mC4ocHm3iWTjWeHwf0zEcYBYurOZ8RU8FR65rIDQv9SZ4Qx+DfffUtN892SRU2cdYXtXCizUyEe7g1vt7c+yZ0Es+1voJkgz4UUA9/0irZJ3FqCF+wL9sjG7zg5cog8jRrMynVYWM6ODcf9Q2KvaO7ka66HuFboJdo2e9MDoGNcOdWPV9nWJR05znu0hQCD2sc7qTnU72CLQjXgasPlF45JkIcEeqZ0i85uBHk1lhuJVvWFfFGlFfQ7KcxDiIw/Bd2QZS34BaWmJYmYPh65xiHngN6JQoQllscll72Rf4/9rU1JE9rJDdGd42DPjk2LLx8/eJBc/HyogZ2H3Egopna1IVK2lUGoTVA0FRjIe4PATwmM5BWj8xX2XK8V2PKchL0MH/4+v3sCPqSBKAW3GyvXvw2TOrt9W4/mxNhDvA6SurxbrOqJ1hQSwtLf4G/K8XsH4HN2rre9qUnl5X3S6cwoqMrX5c=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYwMjY0ODI3Mzg3OCwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWMzNjM5NGQ3NzRhZGU1OWMyNTYxODQ5NGQwNTE1ZDkwZjJiZDJmMmI0NDcyYmEzYzAzOGQ3NTA5NTNmOWE0MyIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-8.5, 71, -55.5, -180, 0);
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
        boolean hasSpokenBefore = player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_MAD_REDSTONE_ENGINEER);

        if (!hasSpokenBefore) {
            setDialogue(player, "hello").thenRun(() -> {
                player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_MAD_REDSTONE_ENGINEER, true);
            });
            return;
        }

        player.openView(new GUIShopMadRedstoneEngineer());
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[] {
                DialogueSet.builder()
                        .key("hello").lines(
                                "Every problem in life can be solved with a little redstone."
                        ).build(),
        };
    }

}
