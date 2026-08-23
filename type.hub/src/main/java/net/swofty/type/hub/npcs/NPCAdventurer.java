package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.hub.gui.GUIShopAdventurer;

public class NPCAdventurer extends HypixelNPC {
    public NPCAdventurer() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Adventurer", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "FaHd91h2NJ5HnDgFbKaEDQDSd/mLzTDavHEo54LEBIwH3aJ4dzPigXp/UO19LjtHUpHHoC/6vOEzJSFhdR9SfOIzNCbWA7RoVFgkoNyIAYpdpf4M4ErtmzrhZiuWrKV4vTebAMaptKWtYY5LkuI0wHDG8SrhPL2rEbxVVDPjw3k0PRS7R1DaTAnmf8OqMHeWyJmWZ7KMAbOsQUORFvlUlPpc+DDurhXCw+iruIGCcwTo+t+RLyaGgfCwZoJYQTqkwmHIh2/rrzSG5Nn5k7Tou2+JY11ebfL7tu+DUlNFVGZ3HHbY7/p2rgPg9uNYxAb4JYeqZ8VmOSPDqqBjkI66h7LPzA7JdR7MwdddlAXiHN0aqwfsoOZ5P8l/XQrJbxg/2JmkF6MCPiD7781fQWKUmNe2BhdXKE1/rzlOMlpj4USfWg9JuLGWLFM5C33i8keJunmpcxUTK8ufU0xuiZDMNiZxdIOyq4eN/zcNbwj2XELIiQ7q1WPUC+CSY1AHWMapNpol460wQZoJP1vyFvv+gHrv64ugQaHXKOGmtKlHRlwoKtTuU9A61kd4DjfcIO0PMC9ct8LgWYO+uRZggVm830YGb+h0BoCF5pbVCiUk3Sth3y6w+joMzJtONLTj7fN87p4kkohIY/Ybtbc9wHHyKw561iFC6HXXemFlBdKaVpg=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1NTkxMzU4MzE3MzksInByb2ZpbGVJZCI6ImIwZDRiMjhiYzFkNzQ4ODlhZjBlODY2MWNlZTk2YWFiIiwicHJvZmlsZU5hbWUiOiJ4RmFpaUxlUiIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGQzNGYzOGMxYmIxMDZlMTE5MDhhZDNjYzkwMTYyYzE4Yjg2M2Q2NzgyNjVjODRhODRhMzU4OTAzZjhmN2ExYyJ9fX0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-49.5, 69, -66.5, -180, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        HypixelPlayer player = e.player();
        if (isInDialogue(player)) return;
        boolean hasSpokenBefore = player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_ADVENTURER);

        if (!hasSpokenBefore) {
            setDialogue(player, "hello").thenRun(() -> {
                player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_ADVENTURER, true);
            });
            return;
        }

        player.openView(new GUIShopAdventurer());
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[] {
            DialogueSet.ofTranslation("hello", "npcs_hub.adventurer.dialogue.hello")
        };
    }
}
