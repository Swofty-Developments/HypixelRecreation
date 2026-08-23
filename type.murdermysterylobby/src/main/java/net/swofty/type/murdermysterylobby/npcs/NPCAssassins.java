package net.swofty.type.murdermysterylobby.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.murdermystery.MurderMysteryGameType;
import net.swofty.commons.ServerType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.utility.GameCountCache;
import net.swofty.type.murdermysterylobby.gui.GUIPlay;

public class NPCAssassins extends HypixelNPC {
    public NPCAssassins() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                int amountOnline = GameCountCache.getPlayerCount(
                        ServerType.MURDER_MYSTERY_GAME,
                        MurderMysteryGameType.ASSASSINS.name()
                );

                return new String[]{
                        "<e><l>CLICK TO PLAY",
                        "<b>Assassins",
                        Text.of("<e>{:,} Players", amountOnline).serialize()
                };
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "QoLDswhgZTkz/WDUHx338hVawnRCt1k/l9S7sOYit4u5GbeJoVHweKqnfvDjWywMJAPiio21LGCLUJJ9wcNVYCrI1PNSNC+5/9V08+u1gw73mxj58yanFyNMXXlXVQqXKyg3RLpboCIpyTDQ5+B72CBkseKqg6myLLfIlv1shbf1Y9doU51Nya7JBp9p1xSlgOAsoehqfhraG/ry515AmDkCODnix79yC4mfg48ayoWt5SKX4CLQDsXeOfFhzJWchr9R833zqfL6Z2cvNBGo3/PtxmG4JV3T9UiQOlObSKKrERAfyCvj4zkVtC+/vysLuWBqVYFknsLhy/alJdgN0dfvw8EFa+ctzlb4ehAoQsiuV+P6CkjmW96ao2HUsoamy0HSwrL62ff2w0t+NocIphPd7beR1wH7yAWhgQaIwiJPPe9GFSwLEAP5H0ewHn4anO0da/hf2+VPMj1EQdC3xJ7tu5g+Qq8lAYWnjtFN41DUxRvbRq1OLskxfSKA9bamxX9vYwc6PHN6al1krgHcSYZSVo6t/mOq94r4KwfnIUnUy0CCvnQFCuOSzenxeCMPwab6xrj/nSWHXkVuEMiNAGup9KoJqdzm2TxwytIae3VV2HuhDbhfc4j+QdIuBGrBBJRKbw4msHaCfHqLdjfmP+aewxlvPqwQ203Qpi7w0Is=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTcwNzIyNjg1MzE5NSwKICAicHJvZmlsZUlkIiA6ICJiYTBhMzYwZDM5ZDQ0N2JmOTNiZjBhMTU2ZDUzZTZjNiIsCiAgInByb2ZpbGVOYW1lIiA6ICJMZW9sb3oxMDAiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2Y1Yzc3YzVlMGVmNzdlOTQwMDFjYjM0MWFkMmY2NDFiZDFiMjU2N2E5YTQxYWEzM2E5NWRhM2Q3NzdmMzkzNCIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(24.5, 69, -2.5, 90, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return false;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        new GUIPlay(MurderMysteryGameType.ASSASSINS).open(e.player());
    }
}
