package net.swofty.type.theend.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.function.Function;

public abstract class EndNpc extends HypixelNPC {
    protected EndNpc(String name, Pos position) {
        this(name, ignored -> position);
    }

    protected EndNpc(String name, Function<HypixelPlayer, Pos> position) {
        super(new EndNpcConfiguration(name, position));
    }

    protected EndNpc(String name, Pos position, Function<HypixelPlayer, Boolean> visible) {
        super(new EndNpcConfiguration(name, position, visible));
    }

    protected SkyBlockPlayer player(NPCInteractEvent event) {
        return (SkyBlockPlayer) event.player();
    }
}
