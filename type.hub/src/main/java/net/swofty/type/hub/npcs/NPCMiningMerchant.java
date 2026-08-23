package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.hub.gui.GUIShopMineMerchant;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCMiningMerchant extends HypixelNPC {
    public NPCMiningMerchant() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<6>Mining Merchant", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "M1H74ucEXCmd/ws7LGJSVaek4/4qsXn1Cj5HkuOg0Z7atseANP3NSQGoQF/vjuAOu8pMt/hE6oXfxX2XjsTysVpFxKWho+fVAObgCrkep9xaEZL7BvJ+zfFA+Gb3CAWPl4DtvjC0Jr7iWUo+sdlyWYb4HbipImwSUMVqPE0H0NC/aIlwiC19TlRNhS8WSP9IrlYQ1F50/rRRIgE3VfCyXQ85L7nk/ZjkKnNOmB/ekENSx9PqCbPSJV7nDjYpZAC9aT/f+kU2EuTuSptjomcWAcEgkBee2QZfnHkT79e41ezvEp4ZYfWqjJn2cQOTFNKbvk9Pl3jRUvXeTiDUkL15pWmJWhnhtW9sBzoP3JW7JXvUSQCeqVtNW9eQjeWhm0q2xV28KqTMYmwB3ZPh0lihJv79ae7u2Zretr8GrC2fScD/GJFjpz3JGIIWpiRogp/KY08y2KT07AzatrGjTJVlgTQCyJqIEIy1EmUoMq50i8EYyPR8FA7JvHKiiRheb/97vj0CwqRfA4nFMC5iLpMRJEjNXoeg7t/pau6Y6GA4lXJQcxourKsiTrbq+Mww3yery0Q3HNCDcgvbCOWCMdoLdL9FdC3POI9C+t7Nqh271lxY39NhT8LQ1ZQ+V57dqUq7EBwz/fq8VHpR8xb4JBdhPL+Ksb6IiAt6xmZ1inOJCJs=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1NTA2Nzg1NjA4ODUsInByb2ZpbGVJZCI6ImEyZjgzNDU5NWM4OTRhMjdhZGQzMDQ5NzE2Y2E5MTBjIiwicHJvZmlsZU5hbWUiOiJiUHVuY2giLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzYwODk5MDY1NjQ5MmQ3MjJhOGVhODk2YjgxNmQwMTkxMTM2OGIzODdhMmFjNGJjNzRmNzBhMGFlZGQ3ZWI3ZjgifX19";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(14.5, 63, -114, 90, 0);
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
        boolean hasSpokenBefore = player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_MINE_MERCHANT);

        if (!hasSpokenBefore) {
            setDialogue(player, "hello").thenRun(() -> {
                player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_MINE_MERCHANT, true);
            });
            return;
        }

        player.openView(new GUIShopMineMerchant());
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[] {
                DialogueSet.builder()
                        .key("hello").lines(
                                "My specialities are ores, stone, and mining equipment.",
                                "Click me again to open the Miner Shop!"
                        ).build(),
        };
    }
}
