package net.swofty.type.galatea.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCSalvageOldAttributes extends HypixelNPC {

    public NPCSalvageOldAttributes() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>Salvage Old Attributes", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "pGs2HGEDHYEsu9tULhPveqhCYAZ5mbmYik/hVuKDplZSWovebnjMlDHmgIHLtXv7j/Ks8enpS7JmorA3PsgVqGiueUCJMUFr5LUiVDmaJoCVieHxropPn+huhwzh3e5gsj9XBPFStHQAO2xm7FYZbyvQ3q9pFcSPkONHFi67ui6VSLY9In2iVCaNg9B7cRm0G43SsraaTeaA8dO0BWFyj7lEyBHj4S/SYfbPIyFyEjbaMaIqsvZAJARS03YhtiPnGeWFQTlD0TyBmnxqAHDmAJMm/fL0zd+smPCeEMZ7Jli4g+VkusOGHLDpGCJ1JmTh5v9EZt+gh1DGuDZetprFK1gdIkCpgKrzfDuFD19PwJZ6qtTD5lWil6i8JbtIAsv2khygcO/9K3oVOLAaAZ5dJYKHB63Gf0H9WKuJQuQKieHRdz1urZ9URJmZ7gMTs+AfgbAZR28f1R9Bsge2WHAM2CIp18AYZjMixdOROyrk15UqP3ahVqOA2/UFnUs84Sv9akN9ypbZljFpjCSRWlTB0FOFKZ5/s/dlWtWg3RMmBshbn5RnGtcRI9k8xzW8W5IBOHq9lMj6sippaT0W07b6LNnIb9NpVRy/dMOeujOaqVKhbpSSiJ8G3KMy1MwDYQ9/QlDlfbT5n2It0JaxpPfwPQxR4wVkPp+moGPJhQsRtC0=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTU5MTM2MTI2MzY0OCwKICAicHJvZmlsZUlkIiA6ICIxOTI1MjFiNGVmZGI0MjVjODkzMWYwMmE4NDk2ZTExYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJTZXJpYWxpemFibGUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWFjNjZhZjc3ZTU4YmE5Yjg5MTc1ODMxZjk2ZDEwMGYwNTYyYzIzMjZiYjJkZDQzMTg0OTQyYWRhZmJmODI1YSIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-594.5, 114, -17.5, 0, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        event.player().notImplemented();
    }
}
