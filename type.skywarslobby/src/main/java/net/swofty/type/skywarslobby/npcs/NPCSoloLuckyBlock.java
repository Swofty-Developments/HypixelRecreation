package net.swofty.type.skywarslobby.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.ServerType;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skywars.SkywarsGameType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.utility.GameCountCache;
import net.swofty.type.skywarslobby.gui.GUIPlaySkywars;

public class NPCSoloLuckyBlock extends HypixelNPC {
    private static final String TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTYyMjIzMzA2MTY0NSwKICAicHJvZmlsZUlkIiA6ICI4OGU0YWNiYTQwOTc0YWZkYmE0ZDM1YjdlYzdmNmJmYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJKb2FvMDkxNSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS80Y2Y4MzMzZTg3OWUyOTVmZGU5YjY1ZGJhODFlMzhkYzhkYTViNTM0Njc0YzAwZjE2NzUxMmYzYjU4ODJiMzQyIgogICAgfQogIH0KfQ==";
    private static final String SIGNATURE = "itJUn+fOzDrib+S7WsS3euHUcDTjoZyzRMyww5G66QawnxRKAlVygFkPY1ud3u0oMU4QAcBc2C9lu1mlya7XzJCjlJWUHh5WKeXyQCKCH8Pe+tXQI3DuGGWaAKPBaL/lX4S3Rx7cMxHmcJlNKmSSwLe9ZrPcU+g/11i1QzQfqgJLgyTpGIp7pLDo4FdaQXMPeanDHgyrK0nkS3SpJPzIqm07oCtxfLcwo9RQ9D/DB3vib7plbZRrrJ0CXfi5w+97FzoOfsxfXxrWQJj11Ob4IIi+RjhfA7oC4J0yq0NXsyM6jcuNz1iNUcpC8ySLu8fxAUJ09aVd1IBOaK4of06FhAjpHXxN4kRewadSAUOExIKDGuTntzhs/Wb2l5BFo79FWg3jp+tdw09sqOpcLft+/qjcG/YkNFpSlpnfIE8eG6BKtwCM/whWON5ettP6Lc2uf8/D10cziFPRQByNmjritHoJE0ARjYiU35Q297GyRHb2J6JxCE6LXYYg1JFKyBwivRRnLN7x+6at/zy0CwfOoOQXUzXQ/EahtsYyl8siLrLoIw5ShDiYv5AWhAfDuDPpLuCojTaZffUj/eCP03+AMsLHuPojOVWrFl9OHqE7YUtQdlxXWgVrD23XIy8tAYK+2/bqiY7/0Qyi60Wr8nQTQv4qk8d/EHb5Zeo82I8Xk64=";

    public NPCSoloLuckyBlock() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                int insanePlayers = GameCountCache.getPlayerCount(
                        ServerType.SKYWARS_GAME,
                        SkywarsGameType.SOLO_LUCKY_BLOCK.name()
                );

                String commaified = StringUtility.commaify(insanePlayers);

                return new String[]{
                        "<e><l>CLICK TO PLAY",
                        "<b>Lucky Block SkyWars <7>[Solo]",
                        Text.of("<e><l>{} Players", commaified).serialize(),
                };
            }

            @Override
            public String texture(HypixelPlayer player) {
                return TEXTURE;
            }

            @Override
            public String signature(HypixelPlayer player) {
                return SIGNATURE;
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(23.5, 64, -4.5, 90, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return false;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        new GUIPlaySkywars(SkywarsGameType.SOLO_LUCKY_BLOCK, false).open(event.player());
    }
}
