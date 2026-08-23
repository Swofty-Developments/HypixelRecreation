package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.hub.gui.GUIShopWoolWeaverVibrant;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCWoolWeaver extends HypixelNPC {

    public NPCWoolWeaver() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Wool Weaver", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "P2Gc0WgQ/6RuU3zP8YC5j6sfWtTlVPZOgpCPaW/ecSREesqHZ+9RCIISo34wlOUHRd985GRM6YpM2ZEwzBuJAIbqT6JYKB3/9As1M9fgCDTAqE60dKsTCflD9zNYW4FZFkvSt+OHTqBvfDVqMPtV7Kq3Rt3KZGt3bLsChRj1I1DLeR8plQPasWyYPzHj2AdTIKqLHTYqB8Mxq0EKlI+V5VUvo6urNkhm8ReFiA8aBXSbfWz98gVkDYp1GRY3z75DH6jn5n/xkGbRbeWPv6b3x0hZYoEyb1Na0JGoVy8RCxiDI6L7ibxK4TOdZqdvhVAdVqWgLgEzeocmb1uAKp5WQEoCQ1rClwjI1gIeLo03ByMg6ddFu756U2yTI6t8L7HW1jKhfmOykM1IvABjL1/b+lQ/I/3XMM6A61YKuh/Eh91WNd2gwuubYl6YyIlvOskAkEQ6iqm8yxnJQ1lziy343bC7lEK7QaJ/XdDJvA8MQN8qCxc53ZnnQb4FZgPf6IslE6gR2rolb3n96skPVlbfwd99hY5jwvptN1WF0l5ouMKEgk+Lku5fp4jKSs1iAr0Q5vJ4TXDhdEevsHZdf0na3opClk5+MseEP1g+gn5Vc/4c7IAe2KJyrp6F7PF4NDGwci5NSWhLGAMQaZyhrcTXRuVnhDaxgF+bLwsQOUagC7c=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTU5MTQ0MTQ2NzIxNCwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjRlNTg3YzA4YjM0MDNkNzYxY2RhZmU1NGY0ZDUzMGNiZTE0YjRjNWMyYTAxZTg3ZWExOGFmMTFkMGUwNGMyMCIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-17.5, 71, -57.5, -135, 0);
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
        boolean hasSpokenBefore = player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_WOOL_WEAVER);

        if (!hasSpokenBefore) {
            setDialogue(player, "hello").thenRun(() -> {
                player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_WOOL_WEAVER, true);
            });
            return;
        }

        player.openView(new GUIShopWoolWeaverVibrant());
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[] {
                DialogueSet.builder()
                        .key("hello").lines(
                                "If wool shrinks when you wash it...",
                                "...why don't sheep get smaller when it rains?"
                        ).build(),
        };
    }
}
