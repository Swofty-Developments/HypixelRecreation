package net.swofty.type.hub.npcs.villagers;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.VillagerProfession;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.VillagerConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class VillagerMathsEnjoyer extends HypixelNPC {

    public VillagerMathsEnjoyer() {
        super(new VillagerConfiguration(){
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<f>Maths Enjoyer", "<e><l>CLICK"};
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-72.5, 70, -62, 0, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }

            @Override
            public VillagerProfession profession() {
                return VillagerProfession.LIBRARIAN;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        if (isInDialogue(e.player())) return;
        setDialogue(e.player(), "hello");
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
                DialogueSet.builder()
                        .key("hello").lines(
                                "<f>Hey if you really want to know...",
                                "<f>The formula to <6>Magical Power <f>is...",
                                "<d>Stats Mult. <f>= <b>29.97<e>(<a>ln(<b>0.0019<6>MP<a>+1)<e>)^<b>1.2",
                                "<f>Have fun with that!"
                        ).build()
        };
    }
}
