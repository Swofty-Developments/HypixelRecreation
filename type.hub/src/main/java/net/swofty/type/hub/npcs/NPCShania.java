package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityType;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.AnimalConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCShania extends HypixelNPC {
    public NPCShania() {
        super(new AnimalConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Shania", "<e><l>CLICK"};
            }

            @Override
            public EntityType entityType() {
                return EntityType.COW;
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(59.5, 72, -144.5);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        sendNPCMessage(e.player(), "Moooooo!");
    }
}