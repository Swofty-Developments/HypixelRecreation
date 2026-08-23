package net.swofty.type.thefarmingislands.npcs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;

import net.swofty.type.generic.event.custom.NPCInteractEvent;

public class NPCJake extends HypixelNPC {
    public NPCJake() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Jake", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "pMvZ+g1p1HCldbVKd1w05q4HyluUjAeEJipZQe7omlFQdiF5PIoHo6sY7k1/dwPQwXgUGsytvxnFfIiWAflryAC5FrxLpTLhrghbuf0zgbNtrjhA6s67txvbp1jpAs7k4ydufiQ39Ob/Y7+Ojx8ZiPMb6UK5OrwPmUmyq1KxloEl6N2qzA1odf7sSXuVxrFlbPwT8p9rEsV8ogCUmzmTiPqkIAGUCWJ3ZbRDOFjOKbrD8Ff0/7qQ6g0BDrOLNPWeHl/eJHT8puCOktNIQs1lwYyNF9gllpkicFOQ9XqCJH8END6jyyF0wgfubFAFB1eoQKGRenmuY4ktJQT+D3yZlOzV20zbtGzUvVvkC7ooYyWFLY3L6m5NCEUNwU5XJbc1hK9PQH1zajY+v1Lp09DsVmiib4NjDsUvb6nF6BISH6w3J9ZNLlwWcoawXs5EqLQbpQxDXuNkD1q3FszvuCoRYOgmXSZqverQ+v5L18BnUjDMM1bUm5r/gH/hPp06Xc5jZmzhYsajqKwhCnsyYY8NS3PD3BIM96a3XhheEVoFZRvg7OHDQtQgzAydJE9bnJmFTFyThUR5ZH11OlSPmTMToKdOUaj+TbhEPGUXPolQ6gpHbcpRVD93RUbKqfPYIaQvgnPMiF6rWJlOHaYetFEmgIeAQGHzEG8KzTvKQChZQ14=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTU5NjA3ODI4NjQ3MywKICAicHJvZmlsZUlkIiA6ICJkODAwZDI4MDlmNTE0ZjkxODk4YTU4MWYzODE0Yzc5OSIsCiAgInByb2ZpbGVOYW1lIiA6ICJ0aGVCTFJ4eCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9kNTFjYzdjMWQxMWFhMGEwOGE3M2NiYzE4NDU0NzA1MWFlYmQwN2IzYmI1MGQ4NmYxMTg4ZmQ3YTdkYWQyZWQzIgogICAgfQogIH0KfQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(261.5, 184, -565.5, 90, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        e.player().notImplemented();
    }
}
