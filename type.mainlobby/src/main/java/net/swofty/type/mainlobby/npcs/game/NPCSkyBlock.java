package net.swofty.type.mainlobby.npcs.game;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.ServerType;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCSkyBlock extends HypixelNPC {

    public NPCSkyBlock() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<e><l>0.25 <3>LOTUS ATOLL", "<b>SkyBlock", "<e>? Playing"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "nlSTknzGOCRA9nT4N8dQce9+2sRkCmpPBN/sKJqIYZxjbF+Uuejbuo32zSFqj1rdB720EA7jVplcZxMP6i9XscUAF83GWwCHX2N75AT1NRW3PeBg0gtrM/qQxKgaH4zOEquGZZ9l8NyV5k2rxDVZRG8YusxsP45WSUV4bnJyLpWeVXO/ZY5luX841BpseLQVTEHjkhYVbL32tauHM6DoB05VKh+5r8utv/OsV8oXQf37+5DMD530ftkriMt+QIlEcVogCk6BR9fvP7tA5Bxvuc6uDbVSkMZaXq62mHVbdRBmnh/nm2hWjrfwNrCPbGi3on5YY5qpnjY9t65Tjw4JsRqmpQXjrscIcfSir8sylisjZfi+kyrjoi4eGVGFVwS2HO1wpX303h3l/z2q9yCEwCHEjnnMx5CI3H8egTTQSLZC8pz60ZaFnyf+ZDwl9gs79wfYGDaqwi7rq7LFYeSrZctABb4oVs/DRuKs+4l8e0LOu90nHnMh7aseAT9YfSip+uTvGaybcpnG8kLcv4sm7J8n3EnhTtcU1ajS/dxZs9i/+p1bQ+wq+k1vwgAOXFLh02ISqUHVVYF9OlyZRiS+/84bPA6WS9SWz+ur6C49fCH44lCPaxxu1UJeHZXI2b1Luh8t7jtKj/gvtzj2GWHADLpcdw2M0IwrRH7ibew8OgE=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1NTk2NzYyNzYyNzYsInByb2ZpbGVJZCI6ImEyZjgzNDU5NWM4OTRhMjdhZGQzMDQ5NzE2Y2E5MTBjIiwicHJvZmlsZU5hbWUiOiJiUHVuY2giLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2NkNjU0NTMxODE2ZWM3MjAwYTZhNDYxZDdhMDNjMmRhZGYzMWY0NDlhNTkxYzg1ZjNiMzFjYjJhODNkZDczNjYifX19";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-25.5, 93, -1.5, 83, 0);
            }

            // TODO: holding skyblock player head
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        event.player().sendTo(ServerType.SKYBLOCK_ISLAND); // TODO: sends to last SkyBlock island.
    }
}
