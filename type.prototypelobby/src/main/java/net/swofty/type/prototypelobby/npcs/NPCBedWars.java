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

public class NPCBedWars extends HypixelNPC {
    private static List<UnderstandableProxyServer> cacheServers = new ArrayList<>();
    private static long lastCacheTime = 0;

    public NPCBedWars() {
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
                    if (server.type() != ServerType.BEDWARS_GAME &&
                            server.type() != ServerType.BEDWARS_LOBBY) return 0;
                    return server.players().size();
                }).reduce(0, Integer::sum);

                return new String[]{
                        Text.of("<e>{:,} Playing", amountOnline).serialize(),
                        "<b>BedWars",
                        "<e><l>CLICK"
                };
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "H2P72VRXpwSLVynYRGM292ZhXjoFXEzf3wuwGlz02kjpuN/xkz5/JDnMwtWKlGhnV50KLjl1qpbEm51RuM+SsB8gg30bn/jWQGAmYe+aNaNnzN79UHBvLTNDu3iaxyyZElJwvUFeI+20u3GL7SImUIu3OyUsp4dXL9C5/tPJeE8cShR+JjHM91+K2agFE7QrG+xqbT61kwMNRze+XlcP/LZCbt8dvdGToCA4w22C2s7FLn4aljun7Fix4M6/ibJzVFoLOtpevODXs2VkNx3/OeLLX3cKl/G24CCGNzLr4qhCyZLo7FLF9Jwq8Rjztm2xms+zayFRNylghNj1xR8N9/RSM+/QCany2g2H+lQbqkEXvvsgAdNE3MdPM06znMRwh3pdTx0noOQ0NJQLjRU98wY9UtWPyhfnGxNPHVlE1v5W5/j5n8GXsnuy1FSPv6qAM/YSVh0b8UiQ0j4zgC1BML3PwEuJEjlLwWD/2XL2VWUSfLwZBXxK1MJlyoe5ACKJz22kuXJWDsCweKRIAPeqxUszDzt8Yu25IMpvwqmme/Wme1gtX5EykPmx3Biu1F5k8C+MM972HzWBJWKlOjP223gV3l1+XgnDG34Z5U/thLMedNQEAohIUb0AJ9Fp2B/a8bG3ZkyyP8dXT7XKHjOYXFMIDK7RlDPXS+dfHwFC0ac=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYyMzY0OTQzMTY1OCwKICAicHJvZmlsZUlkIiA6ICI5YjBkNDI0OGI5NmQ0YTg0OWZlYjg0NzA4N2QxNmIxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJCZWRXYXJzIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2QxYmZiOTFlYTdlN2E4MjBiYjNkOWU4ZWVlODljMDM2MzRkZWE0NDg5YmM5ZjljNjdjNmMxOGE2YjY4NmRlMSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-9.5, 75, 7.5, -110, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return false;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        e.player().sendTo(ServerType.BEDWARS_LOBBY);
    }
}
