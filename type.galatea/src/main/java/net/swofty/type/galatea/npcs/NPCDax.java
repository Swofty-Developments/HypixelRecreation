package net.swofty.type.galatea.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCDax extends HypixelNPC {

    public NPCDax() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>Dax", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "VaK3r8cw7g2fNUQp4bnrnGXhHJL4L2FK4x73+DZWBiDHvmd5qeZsWlr7KgjycmZhCAzB5RvlwsDmwJcjH9/MsdUXOSNoj5iHRxlv3vljxXz36jkRgkuvATAjRP3WNXeNSHI2JAZucpm7BtGHTh3ZZQuTd9Y4Suy53frEiv1Z5B0LKifxBDf89tETi+evKUE02cHrQm4evH2D32nAMdVY/PVIUqZc1V90WM25UysUyV/SX+Z5EhVwBmsQO4+0lMhEL723o7U6hAhkK7ut0vrrJ/Z0qLBMyiNxFQSs6scY1ucBT8uRwIMTD/cB6Y/hxQuvNGXbWE+4KchKzwZacNoRFmJTONbGLKeCklQC5pgWCgiVtCEfE2JeHq0hi0Q/Y67Z36Q07JDIP2OZ57FOD50jGkaj9MpXNrD9D3DQdDVY0CP+/r+u8FNrOk9MBn466q0VjCltyYnXTLJATD2Nr4DM9YnLWgBIx0rAhYYyUW5JUSZm0SEfE7Cowal2lceIUqmfDObzAfNFPZgPmGPE96TmrPPzolj3gj+ZM/r7ffc3+hlEc3QCWzucNV5k2MvX2LVB8P2B+8RWGIg2mw6UjsGleNQh5kEWJWKdWDD3VBmByTOMFVoN9bQjtWy38xGwGk+M/5z/SKPrmZm9UiuEfwuZNenJZz7ZbDOI0pOgSPF03XI=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTY2MDMxNjk4MDYwNiwKICAicHJvZmlsZUlkIiA6ICIzOTVkZTJlYjVjNjU0ZmRkOWQ2NDAwY2JhNmNmNjFhNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJzcGFyZXN0ZXZlIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzhkNWM3MzkzNTgyODNkZGIwZWQzMGRmMDdmNzQzOGUxODQyNGIwMTg2YjU5NWRmMjI2NmUwZGJkZTM0NjU1ZjIiCiAgICB9CiAgfQp9";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-705.5, 94, 33.5, 45, 0);
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
