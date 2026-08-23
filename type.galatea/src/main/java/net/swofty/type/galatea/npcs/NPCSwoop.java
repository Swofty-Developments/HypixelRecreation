package net.swofty.type.galatea.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCSwoop extends HypixelNPC {

    public NPCSwoop() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>Swoop", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "EF0QFzeMDwxRJH7yv7+yQWZeoSjoePOBDpiBbjR4C+pbaaVpvIdpkVPLCixbRTLbyhE5tms4to/91yehxsbsvbAJz5dr9K58XdcUp9nuzYBgxEmZv8z2wpYOUGPFOp7lQxyY1vH6UcrxrNdUy3d0h/zaPN2YcRgg7DCrtE6S1coNn7v+PB2z+k2QFQnPcJe7c8U4PIQIEsZoIaH4OYaw0d4pWfjNoEfblEjKfywyNpSJwHXaJh2XYUUmlOb8sb8wOnc774sSSMnReQL6x5DEWwK/18W9j/QOwvWduw8K29C6rQET9x4HbzMszj3utaZgt3tXNqUa0I2Kk3ikTkzG71nfWxnstNf52g0CQ0cSIJF1aDdbhGRhabgFKhNwDGJJr4aiKE5B4dRS1XGHtnE2Fp9VWrpN3EdBi+EfOEzcJlcC1luu8uqYDl4phPYXgLwR3wKFPeEouhERGRwhO+yRnyBS1MhHemUG8nCYykZxD2/l1ixOF7wBwGlrABpvtOA+xEYt5M1JLeaAYc4qNGv9Zbn17IH7mm4On2XHBCbXBzn9s3mv5b8G9xA7dIje79lu/C67vWJ0r/89LPAN+ZXkoj5xp6dqn7n0KFrQJk73VVre4Ry2makjZsg8hooiz1EbsepMzZHzb+PyHCqd3X1HCgVwQNdq96knTRuV8JjDjBY=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTcxOTMyODg5Mjk0OCwKICAicHJvZmlsZUlkIiA6ICIxODA1Y2E2MmM0ZDI0M2NiOWQxYmY4YmM5N2E1YjgyNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJSdWxsZWQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzc1YzU3ZGNjODVhNGM2MDM3ZjUyYWYwMGZkY2VmNDA0Zjc0NjI3NmMwZDMxYTIyYjJhNTUwYzZhZWQ5MzJlZSIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-579.5, 114, -23.5, 24, 0);
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
