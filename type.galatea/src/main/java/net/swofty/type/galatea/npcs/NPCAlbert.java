package net.swofty.type.galatea.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCAlbert extends HypixelNPC {

    public NPCAlbert() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>Albert", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "bfmPL4+s1uE93bOGHyLlZahoF+cL/AOsDG3/IQEMwZYz+PxF4J93NOoS+cA2lRlWtKC6GFaR1wmrWPdT9viQlVSsSilFH4v7HNUoIq+s2ejhyo4R5L8cxMlfmXLrhHlE/7g8M+c1+o/G1pA9T/3o7AAS/T1W7I7PIa7/FMgUBg9xztITbFmzvFnbeDX0d39r4yOcTlFMLF/bqZJVfwlxvMhUoKfkEmJgFW3kBTM9arsUIrJmo1D8iwCV+Tggc1Elpad5XL/9ZoTjgqIIkBqkWPoC6gyPyC5ciyoJjxEi8k8YPAoM0f0CKzh8c7HaEp9lwQLkgN53Jh2dOIuRkzbUJc/3eui//4YihXfDkz4AUAMDURPQJAGxflJEiG/6ItcqVzFdewiaaucclRtBAqOkLHzxsb7IIbuTYOhlVSCJkvRUfqVUxte+ybSmIV0mBO/jOElRZuaKFdYe0q70ZZoYOATSUbMZ4KCWSBKDse5nmX5TC4yNOIP1BwAT7l60Zndk8WsnFm2hoUYXI/85KOXyqFVSfeA97GESyB5tXZUvGFX6F4EC0F+gxB1RKpuVdAjn6CePEZPiRDIz+JrqK43w3/pKWZYCbnaR1G63hHQaQWJe/Z7w/nrGLHEHXD+9d8lF9nxabrDaf+vJ9UAlD8U5PzoEFh5Zj/XmwQ0xaktVzyM=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTU5ODk4OTAzNjA5MSwKICAicHJvZmlsZUlkIiA6ICJkODAwZDI4MDlmNTE0ZjkxODk4YTU4MWYzODE0Yzc5OSIsCiAgInByb2ZpbGVOYW1lIiA6ICJ0aGVCTFJ4eCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS84NmU3YmNmYzA1NjkwOGNlMjY3MDYyMTQwYzUzNDM1MDNmYjA4YmRlODFkYTk5NjJhMWI2NjQ5YWE5NTkxMDk4IgogICAgfQogIH0KfQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-592.5, 113, 8.5, -180, 0);
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
