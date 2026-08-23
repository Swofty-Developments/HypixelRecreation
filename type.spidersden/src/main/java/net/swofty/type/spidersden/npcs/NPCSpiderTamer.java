package net.swofty.type.spidersden.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCSpiderTamer extends HypixelNPC {
    public NPCSpiderTamer() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{
                        "<c>Spider Tamer",
                        "<e><l>CLICK"
                };
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "iVtgkmTqpSiaC+wR3ShL3oE0gs/lhPJOWSxXxQzgBmAU+flgSepFAtIp7z1FKpV1DYXPUzPyvzx+OEhYDvi/cxEru545V0sZRSU/yBr5RyOL3+hYzh3htvw/w4XAZU2mlLmCiH2T354itZ8gw3j8Qz30HAhmFX7Z7HX5mi5RY+SJ73yJ281kzwkrybO+sOgu9Frzo+ToBgnA/g9gJmYmRNtMHCey0lxgqW+x97zVRmHwCe7fxVzltvrN+y/Ae6meX8xAsJT5Lb6kYbCpFKqMNsyxSC0UKM7w2wOmRYrw1dkxmWyhnhM5xETsAV5KEFgoklabVSfqUXzSDdsUAW93ENNatx/3KGayj2LncqwCTaLqqCcZq0DRteeM7Rb5VJEp2E7jLovr70PhWbbnRMU7aUuoea7/qz6v4fg+963rkczQaCWv9UbfTVITdtU2TqXEq+2Gm+H7z5txHRXwrm6S27g6S2wLmwDADH7f1a4P42thOfjP3A+h0FpZ+xzw2Sn1okFh5qE44JCzBoMy2E+ryZvesXsv80SCDecB85lAmr/z/rU6PG6ZQ+xkvd+FPaVgnJ/GI6Qt3MKuXyNYclWF2kku1nHDlO0BLIJByBKaDWX8A6hQRGEYoZ0XKpax1SOGol+ftvGVceqyOQfyA5+AmJpIseghSdmU4Mbp+rTCSgM=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTY2ODAwNDQ4MzAyMiwKICAicHJvZmlsZUlkIiA6ICJkYmQyYjU4N2VjMWY0ZTgxYTNkOGFlODM5OWJiMDFjMCIsCiAgInByb2ZpbGVOYW1lIiA6ICJjYXRzaW5zcGFjZWUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGVmNDBhMzg3NDJkMThkMzMwNDFmYWUwZTQzYmIzNDA1YzIxZTU3NmY2MzIxYmJlZTA2MzZlNzIxNGQ2ZWZmMiIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-298.5, 62, -194.5, 23, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {

    }
}
