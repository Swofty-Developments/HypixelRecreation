package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.hub.gui.GUIShopWeaponsmith;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCWeaponsmith extends HypixelNPC {
    public NPCWeaponsmith() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Weaponsmith", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "Sw5WHUwuYX2OxF/U1BHPpFdAJzwzzyzZ78bxQkdCQL1Jyd9RsIiQWKJSeTHsnhScpHxXEBtWKQ71YOci6HzwLiYZOh/M6KJ3kY+RtlRdh8EUEVj1BCoo8xvwAt28Piiqke3uDs9dWEvVBzs1PR9qxvMaarw3DT8sTgxIBU/xMmt41uDiCOgn6M/rL3waFsnPdN+v2tTHsl1aNz+hMLn4NuIbBprE90X0tsT/qlQzCBPHuwUV8elGb9xAfjJ0eRSlH1jxcfEJdb+YsPKwfqQ6btwkCOK2hdfm9/S6/Fd2KJsaDhH5twfykFyD1PEn4sCAlAisibKiOADQcg6lYN7d2eE95RVYhlqHTsb6g0aBUSk8Gj8OOABbBB0sEhGPmzTKJOAoyu9ZWgfzAty/ipGooyL/gWlGByTmWnmXf0ek6TUtkPpmLNd3Ik364+GDI+C8H14Dltc7axQXh0GsFmepUL2t/fz0fDtZlqfhwz3ei7q7fJ8S20l2O4y/GKDCItqMTkY80rOEQuysln13pV32z/8oZwl7a1rGSvSjLQwC+cpbhA8tATTVu/ovUd5Aev+PUb5vqrh4DmF4Br5cRxLCDHj6Q2CG5glXVTULhbmMLQE0YLVFvnik2xGNnlKIJ9GEdsat+PBHR7ToHasGr4JiS7n2uQykxTntgpqB9QNS4fI=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1NTA2Nzg0NTQ3MTYsInByb2ZpbGVJZCI6ImEyZjgzNDU5NWM4OTRhMjdhZGQzMDQ5NzE2Y2E5MTBjIiwicHJvZmlsZU5hbWUiOiJiUHVuY2giLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2FmM2Y3YTY5YzlhN2FkOTVlOTBmMjEyZGEzMDQ5OTBjOGJlMzZlOTMxOTdkYjg3NGMyYjBmZTA4MTA2ZTMyM2IifX19";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-50.5, 69, -85.5, 45, 0);
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
        boolean hasSpokenBefore = player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_WEAPONSMITH);

        if (!hasSpokenBefore) {
            setDialogue(player, "hello").thenRun(() -> {
                player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_WEAPONSMITH, true);
            });
            return;
        }

        player.openView(new GUIShopWeaponsmith());
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[] {
                DialogueSet.builder()
                        .key("hello").lines(
                                "You'll need some strong weapons to survive out in the wild! Lucky for you, I've got some!",
                                "Click me again to open the Weaponsmith Shop!"
                        ).build(),
        };
    }
}
