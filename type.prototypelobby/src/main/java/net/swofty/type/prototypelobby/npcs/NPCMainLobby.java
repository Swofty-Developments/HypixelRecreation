package net.swofty.type.prototypelobby.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.ServerType;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCMainLobby extends HypixelNPC {

    public NPCMainLobby() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<e><l>CLICK", "<b>Main Lobby"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "gNz/WCm8osVPD29ZLfjhHcJuTg0Zcw+ZgjAV6nfkXuPkfokUaji69UiYN38RROd2J5rU5CcUAVC/kyhyy2CznjPz++6LhMVMFZMHIXStat1s4IC98u9fV1WE9Ut9zoTE/FP6lu4v8zYQVUTDdxEXRM4zJtJci5h3HaHpBp4nStL/a/TfVfeqQ0zlaViJXCuCGWLcna1scqYlegvP0hSb3/0Xcvtz+Z2g8YnivTF+V3cCLQ/3WpKTwvYIQ1zdTPFB48+tST5oJOOJ2H15wydk2C7qtA58KNGOUFU8jTGJ2ARVDSf5RmWU1/01MoD/B2b/hAZkY0GzWVcLzLJj2TDSEmbTmY2IZn771KFTQB9v6ZgZr3amwyfiL74m/aGj68plS1Ce83ZZr/b5rHSt1IFuE3ViXyH+rouxiEtjVcZRSiVs4d7batsMQuyMuOe0spQTvmMTYruyzzNE2g6Za+VVP07o3ofuQjVUJuzTM3vO1hGzYeXoMG1nAE2MouT/TZLUgFG+9UFtSWc4hODCYI9XJQZ7YXgdb/v4tG8VNi7/qAOiKdjTaRHKIwUmbAGrIjVwnuWsiGWuO1BBXO9xCEMQNz2VnGI0LQ5ZLXBKPZUa13lkxOtUlHAiGUDIUIKdZmCTxGx1KzzhjO6AKp62gU9SVokSmZ23sAxrssaM7TABEek=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE0MzQ2MjE0NDczNTgsInByb2ZpbGVJZCI6ImY3Yzc3ZDk5OWYxNTRhNjZhODdkYzRhNTFlZjMwZDE5IiwicHJvZmlsZU5hbWUiOiJoeXBpeGVsIiwiaXNQdWJsaWMiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8zZmFhYmU5ZjViNTZhZmNmZjQ3MTcwYTA5OTcwODdmMTQ2YTkxYjJmNDcxYTI3OGQxZWEzYmM4NzRkZTUxNTYifSwiQ0FQRSI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2MzYWY3ZmI4MjEyNTQ2NjQ1NThmMjgzNjExNThjYTczMzAzYzlhODVlOTZlNTI1MTEwMjk1OGQ3ZWQ2MGM0YTMifX19";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(8.5, 75, 4.5, -143, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        event.player().sendTo(ServerType.MAIN_LOBBY);
    }
}
