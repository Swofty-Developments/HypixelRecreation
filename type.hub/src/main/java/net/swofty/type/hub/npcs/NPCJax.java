package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.hub.gui.GUIJax;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointArcheryPractice;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.stream.Stream;

public class NPCJax extends HypixelNPC {

    public NPCJax() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Jax", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "WkNbkv4F5IKaJsWVscT7vxmBlYqJZvsyobQvQKRbgsCxNbW+nN0B3lWLwnk2CNnlMkUvYZfqjCuiOKH/IRbP9IKTRxw2yU8eel8a7IWuQtB4UlNHLVfgjmbC5EPdBRKKF0QGJcBYLIeKOXKAoVS12RA9CsNl0k1PLRZ0+vHA+M208djgySS8K+DN/JluZchF5jx0io9KxyygLZToAsD5/DLB1pkiPhaqYX6COrBg3malhHBg2xwP3KSXHAmVWx2HUTByZoJOnsGy9GbGVBd7bFOKO3Pp8+3PqxBVg7vL2Hy69ZjqiwGtvzrkGc1P+RI92ZwGkXpe+vi8fmF+E2ZleOt0PpoYbGYRI0GehCVRiPMJo206Axsh385AHCvjNClGhO1vyBLqDonYkkQJQKvH7BbjVeQ0U5T8d6f8PibHwt0UWum8s/IG0w8Lglj5usMBNS0RdKPDSeBRIt2f0cMaLRvyI6KORMARQ8dsPs/JL8qkzvOjIBIzYWkLB6IQkW2f1IdVOCbb7rjk3lE1GdY1VKEnt5scznBXVIm40hWJvly4e1kQSV8mXlqPavSpHR7nsrhaMd/g2TZplwUPdRIE97ZqM0fHdpDds7rfL0Z+LoleKUGTxib3VBwILQ9///rfljL4f5fl94fUdFjLuMAm1x7jwxuOvq3jX76dAMiJlB0=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYyNTcyODU3NTQyNiwKICAicHJvZmlsZUlkIiA6ICI0NWY3YTJlNjE3ODE0YjJjODAwODM5MmRmN2IzNWY0ZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJfSnVzdERvSXQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMWRiODUwMzIxNDYxMDZiMTlmMGJlZDA2Y2FhMDNhYjk5OWYwODA3OTY3NjgwNGU2ZTBkZjJmZjFkMjhlMmNiMSIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-40.5, 69, -92.5, 35, 0);
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
        boolean hasSpokenBefore = player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_JAX);
        DatapointArcheryPractice.ArcheryPracticeData archeryData = player.getArcheryPracticeData();

        if (!hasSpokenBefore || archeryData.getTargetPracticeLevel() == DatapointArcheryPractice.TargetPracticeLevels.FIRST_LEVEL) {
            setDialogue(player, "introduction").thenRun(() -> {
                player.getArcheryPracticeData().incrementLevel(player);
            });
            return;
        }

        boolean hasHadDialogue = player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_REALLY_SPOKEN_TO_JAX);
        if (!hasHadDialogue) {
            setDialogue(player, "completed_level_one");
            return;
        }

        player.openView(new GUIJax());
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return Stream.of(
                DialogueSet.builder()
                        .key("introduction").line("Hello {}<f>! What brings you to my workshop? I forge the newest and most powerful arrows in all of SkyBlock!", player.getFullDisplayName())
                                .line("If you can prove to me you're a real archer I'll forge you arrows whenever you need them.")
                                .line("Ready to test your skills? Step on the pressure plate and all the targets will light up, if you can shoot them all in 25 seconds, I'll know you're the real deal.").build(),
                DialogueSet.builder()
                        .key("completed_level_one").lines(
                                "Wow, you can really shoot a bow!",
                                "Well, a deal's a deal! Come to me any time and I'll forge you whatever arrows you need for adventures.",
                                "I only have one condition... These are powerful arrows, they can't fall into the wrong hands so keep them in your quiver and don't share them with anyone."
                        ).build()
        ).toArray(DialogueSet[]::new);
    }
}
