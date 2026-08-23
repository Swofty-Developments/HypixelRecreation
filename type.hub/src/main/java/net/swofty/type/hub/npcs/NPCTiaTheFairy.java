package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.hub.gui.GUITiaTheFairy;

import java.util.stream.Stream;

public class NPCTiaTheFairy extends HypixelNPC {
    public NPCTiaTheFairy() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<d>Tia the Fairy", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "Jrf7HqW8Dj956V2RFd/8BPaiYICriEJBNJvz4bISRLMWPov15IhtVQlBj8aOF4ka5Q1oeI8Vj+Bm0BU+4JCNJ72Lr/pQR3ozU7uKxn6o+0N4+kyrYHbL9EfjYzyjHNmSJbAZPpNWl91EJvUb7u6LSrgdhfSHpED5uaXavi30TN7HHPdwo3NIx5A8ZdJyoCDB7k1bnO7z0IsrAv0+tLNgD/No47KVfw6kfWHwklz8RkkD7cbdjKojX3nqCvyYbrWqZbMbTNI0BqReQLB1mYBjDTQKBiGy09yJECJsn6jlszEcVY/c2VOmoCgi95aKNlLwUH864NDISnjyLgX/Z+VKgXPdzhf7IseplUr9B+2njvQucjm4rT5IT6GKHlfjTkLEnfaVrwvGI4N1NEyzG9ULSJaRdXnt5qO5UyiNBF9tXAeJ6+yPpmurbr/ZhcPPVErFogda8UKpZ0X9Q+3l+Ij6S+Z9mSNJ1kwROIQsmWDKNy3AhTytibtDzhY3WhsKO9SHseaZaaH2EnVYdwgSGOMG1DcE3u79gMfEzHYT9JZFDZPALrosXmCUC5MpbJz7L44+OSgQx4YHfZl/8iGdxOolXf0n8XmaVEPQ/NVpTm3n2QeYiK4OBOSdfa6BPtO/aZiFbOG5kqvJ+JEg222UZgEDtVdYKdFdc06d6KZ04WC0LRA=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTY4MzgyNjIyOTU3MywKICAicHJvZmlsZUlkIiA6ICI0YjBhOTI4NDY2NDY0NmRlYjY5NDJiZTIzZTlhMWQyZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJJc2FhY0FsdCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS83M2YwNzI5NDBhOTQyYjk1M2EzZjlkMTM5MWZmMzQyYjdkMTkwNzZmN2ExYTNlOTQ0MDQwZDY0MjBjN2Q0OTgwIgogICAgfQogIH0KfQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(119.5, 65, 147.5, 145, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        if (isInDialogue(e.player()))
            return;
        DatapointToggles.Toggles toggle = e.player().getToggles();

        if (!toggle.get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_TIA)) {
            setDialogue(e.player(), "hello");
            toggle.set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_TIA, true);
            return;
        }

        new GUITiaTheFairy().open(e.player());
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return Stream.of(
                DialogueSet.builder()
                        .key("hello").line("Welcome to the <d>Fairy Pond<f>, {}! I am Tia.", player.getColouredName())
                                .line("You may have noticed some strange orbs laying around the island.")
                                .line("They are the souls of my fallen sisters.")
                                .line("If you find any more during your travels, please bring them back to me!").build()
        ).toArray(DialogueSet[]::new);
    }
}
