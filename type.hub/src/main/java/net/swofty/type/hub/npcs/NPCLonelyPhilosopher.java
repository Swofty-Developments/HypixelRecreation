package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.data.HypixelDataHandler;
import net.swofty.type.generic.data.datapoints.DatapointRank;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.hub.gui.GUILonelyPhilosopher;

import java.util.stream.Stream;

import net.swofty.type.generic.event.custom.NPCInteractEvent;

public class NPCLonelyPhilosopher extends HypixelNPC {

    public NPCLonelyPhilosopher() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Lonely Philosopher", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "tKdLS3NJLZ66Hq4KK4jd0mjj+4BOeYpEkTK0+hS9JJfrFqbFdBypZKqnn5mMvujvoKfPUZ3LSH9s/HFfcnLsz33mvTStcv3HzwuiO68Dl7FTI3rye/Fho/en2zbihomGu7p8xb8+gEk6UYVgd9elaYkNt35jGKrlcOGePtPHVrG5LMM7qF9OYJtsA4zdR7h60zPg2SbSexhkbiKTM97xVPF+zya5PqSgwPMe1K/+s0SmIrd/6FMD7h+K0eavnCCq0dPweRYvc5vq3ji8a69h42Fg1p7U7vEuPdW0YH8wxD/dECx2w0VZqwWx4a14Gucnh87AlskOdrp2HZrdEAKU4i/rNPYrNIxzd//Ad899cuQ4fuk3NmnsrKpMvi4S9iPqkIYgkEHSbmm545r+Kcivhjj33eTTZr7ne9L+TAxPaxiqCjsP2hQQMS0jjtm6OUAALQRnq4625ZOZJ6TJGxXC8hSAJGv/PiqCpEsqc34gxhWVq8QgOq+2tX/A3TbmthxWp8BcoxyW9g25srCNZnmN0NyFVTjY9gTdWJwMfFOlzLSkHLksYB3QKYF4+HcTyyDIuQp12zKhYmFFAuz7WHcE4KOJ3QVHygmwPsnQMeqLvH9u6n9zBeC4KTK/AZvOH2BrkSfO6UUlnpv3jyu7ZCiz3PsFZVjn6DmnjOj1p88lan8=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTU4OTMzMzQ0ODA4OSwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWFkYTg3NTE3OTBjN2FkZTAzZDNhOGFjMzI1MDZhM2RlZDE4Y2JmYWU4MTI2ODlkMDNmNmYzNGM3YTk2MjYyOCIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-250.75, 130, 41.188, -51, 4);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        if (isInDialogue(e.player())) return;

        Rank rank = e.player().getDataHandler().get(HypixelDataHandler.Data.RANK, DatapointRank.class).getValue();
        if (rank.isEqualOrHigherThan(Rank.MVP_PLUS)) {
            setDialogue(e.player(), "open_shop").thenRun(() -> {
                new GUILonelyPhilosopher().open(e.player());
            });
            return;
        }

        setDialogue(e.player(), "hello");
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return Stream.of(
                DialogueSet.builder()
                        .key("hello").lines(
                                "<f>I'm sorry, I have nothing for you."
                        ).build(),
                DialogueSet.builder()
                        .key("open_shop").lines(
                                "<f>To fast travel or not to fast travel?"
                        ).build()
        ).toArray(DialogueSet[]::new);
    }
}
