package net.swofty.type.prototypelobby.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.ServerType;
import net.swofty.commons.UnderstandableProxyServer;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyInformation;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.ArrayList;
import java.util.List;

import net.swofty.type.generic.event.custom.NPCInteractEvent;

public class NPCSkyBlock extends HypixelNPC {
    private static List<UnderstandableProxyServer> cacheServers = new ArrayList<>();
    private static long lastCacheTime = 0;

    public NPCSkyBlock() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                if (System.currentTimeMillis() - lastCacheTime > 5000) {
                    ProxyInformation information = new ProxyInformation();
                    List<UnderstandableProxyServer> servers = information.getAllServersInformation().join();
                    cacheServers.clear();
                    cacheServers.addAll(servers);
                    lastCacheTime = System.currentTimeMillis();
                }

                int amountOnline = cacheServers.stream().map((server) -> {
                    if (!server.type().isSkyBlock()) return 0;
                    return server.players().size();
                }).reduce(0, Integer::sum);

                return new String[]{
                        Text.of("<e>{:,} Playing", amountOnline).serialize(),
                        "<b>SkyBlock <7>[v0.23.3]",
                        "<e><l>CLICK"
                };
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "XlI+W3ye+8a1kOQLxIgXUmI4vdZ5A9kJ8MyVbDMy/BOb9iN/L/PPwmwl9iT7gYKFaWd+/6m+9byDZLtrTkJGkse/GT+j9ubLeVw41eAlGT5esttrxG2BA9FGDRBkFIsAyptCoUg+81xcLTT1y3KKeLOshpbNel4ZTH89OGljA5j1LhVO6oM6ZOowYa4RsFPnuu5A0VKlukRG9zZmB3zExJljb50PuRhhavsNSOHEVSO0zEzBYI09EmXt7G+zKf/QXvhuQJiEOuYVL2Fmzhye3hbD5s/tLcQuCqz7k/yxnTxTkPpBi5vd+7T2gaNrWHEir8bzqwoFqP03xOKAC2ia1w6/bvobuqso3zGRH3veAAHKyh12xmWJcpywo2026b4Pwog1dHV3jcesRl6GisnZFXaHyTgMg4T3B0LCQkWsZoE41USDAhVC0+E8+lxtJYTZEyUtWM3MMJNXWROjQRNh4e35Jwipqk9YLmfdXoDYj6YytMTO4YGwmlEgqeFbpcjnSia6CF7vs4jemWoqoYKGdih5Id4PIBOJd55Ij47HS1WOt7apQHEtFRWPMW/URr0fHUCYELpVAPEhOh6jqfnwFJZJhZh/p/MyXP5kaVjWiXD5CmTl0Ujo5U5fHmmTbMrU78suUX6pSibZDSfYs9bIYaZLD7BYbbTW4xPYPqykiEI=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYwNzk2NDk0MTg5MiwKICAicHJvZmlsZUlkIiA6ICJhNzdkNmQ2YmFjOWE0NzY3YTFhNzU1NjYxOTllYmY5MiIsCiAgInByb2ZpbGVOYW1lIiA6ICIwOEJFRDUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYzYxMmE3YjE3NzMwNzk5M2RmYTRmYzc2OWI3ZGEyYWQwOWQ3ODQ5Y2M0NDhiMzQ0ZWY1YWRhNzJlY2E3NWQ1NSIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-9.5, 75, -6.5, -70, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return false;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        e.player().sendTo(ServerType.SKYBLOCK_ISLAND);
    }
}
