package net.swofty.type.hub.npcs.villagers;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.VillagerProfession;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.VillagerConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class VillagerJerry extends HypixelNPC {

    public VillagerJerry() {
        super(new VillagerConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Jerry", "<e><l>CLICK"};
            }

            @Override
            public VillagerProfession profession() {
                return VillagerProfession.NITWIT;
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-33.5, 69, 7.5, -90, 0);
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        // what is this NPC even for?
        String message = switch ((int) (Math.random() * 3)) {
            case 1 ->  "!";
            case 2 -> "...";
            default -> "?";
        };
        sendNPCMessage(event.player(), "Jerry" + message);
    }
}
