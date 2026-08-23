package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.hub.gui.elizabeth.GUIBitsShop;
import net.swofty.type.skyblockgeneric.levels.SkyBlockLevelRequirement;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.stream.Stream;

public class NPCElizabeth extends HypixelNPC {

    public NPCElizabeth() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<e><l>COMMUNITY SHOP", "<d>Elizabeth" ,"<e><l>CLICK" };
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "B1nUeLBw7vTr3Xx5lRxwaaIeyjTCwhkV5Iq25jO6Iae0kpuss1JQr1fsmMokSRPoFesv0iIOSb84yxKhdxtTju8dUGuE1TitomTMVtr2tdZxO0hDxzk5UzPZAO9uJk6hfVtQA52IWWGo2gsc+9x/hOdGEuLWkHEKLMFyuPrmOGWginkgk6kRtYn45P7rqNReWW5elgf42lEofyEvg67G7R/TAhpMDMMMgR9u2PXz6Fl4QhSvO1loFURjJwEj2DxFxIBFJf54eYuWzWcItzh7L4WcphOaxpmYBjS76+SjjE5tcJIlovptGTiRyzXSWJ6OtnOKevUzrh/7S4M7ABHOUW18uYQvU3jYsF2v+QTXb+mt7xJYQ6Go/c0lFmwvfip+fdigDH/QnfApy9dWfUeQqsFP4DRtUnNi1evLewJ0+Blyz3NImOdoVF0ShArxKiEdLvd5aFQb3GQT83TR1CNe1gCJOS1tmz2HjiyCET5ZUafJc9zP45qpKYxsZ/U74hIAgod/Nnf2sgQiPG6GvAVMot8KuTQa5vS8o2Jt40kVP4ic0xbhAgHYHxWRkaEogpenGlS9NrlW8qFFpLBIyi8YLo3MS5SKTeeaVFsXKbPNtYl+Gs3VdwdCNsdLjkxBEph9Rck1CSyjC2qteGLx+usja4Raz/YvHkY07gaCnKLUy7k=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTU5MTc1MDY5MjI3NiwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTAxN2I2YWNmMjJkZWIxNjg2MjkzZDdjY2NmZmU0MDkwMWFhNWFhNGE2ZmE5MGY4ZTEyMTcxMTRlMWQzNzMwMiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-6.250, 79.000, 19.250, -145, 0);
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
        SkyBlockLevelRequirement lvl = player.getSkyBlockExperience().getLevel();
        if (lvl.asInt() >= 3) {
            new GUIBitsShop().open(player);
            return;
        }
        setDialogue(player, "hello");
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return Stream.of(
                DialogueSet.builder()
                        .key("hello").lines(
                                "<f>Hello! Welcome to <b>SkyBlock<f>!",
                                "<f>I have powerful items to offer, but only to experienced adventurers!",
                                "<f>Until then, I suggest leveling up to SkyBlock Level 3!"
                        ).build()
        ).toArray(DialogueSet[]::new);
    }
}
