package net.swofty.type.mainlobby.npcs.game;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.ServerType;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCMurderMystery extends HypixelNPC {

    public NPCMurderMystery() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<e><l>CLICK TO PLAY", "<b>Murder Mystery", "<e>? Playing"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "c1hU1jMYKOGHJakNgneUnUYtA0bLsVki0qnJGh9f7kbGWgPmQ2wtDr44/qPwjiIMALlMK0WgAhEVG68fMgZvHSLb/BPMIy1aU9aj2cInIbqELxjyGUjl0Fj8TF/eTBPRlUMPtQF0PNsSzmxS++omk9dwhUEla9HEVDSy6QQxnDxB3f5kO2FphgeMOpLEdhrMLx41n+Nv5yViAwysIP55pBZxHj2kQxyQMtGMR7tfCEhyR6cVLhVxP553L7PMbmL7KgQx5tLeSW4w3m5q4eQIj+ZrVRF3scbNIo7V9TdGiAW7ufTfZUftX0ffsPc1/6tpHoC4eiy0FZR10JYhKuzyRRvBOnRtqbPWTIuPdFaBs6ZHn13hbQoyKBjrHnDAMJai84F3QLcTV2sjKT49x6tL03mbRN8WT4l9H3n/RcR3ja/XbN/kDWWpD3IOHaW0y6XhA42mRGoMjMrro0Mh1yzXd2FlfRzvnHv9BbCqFqtKWyNpkcHaZ2SfP+0ltNoesvEMqbq4XLj+O/vrdgCJi1TbeDdvTg4fK4TvKRW9C5kp85H18rAmJhedbPfkw+6yWDLoTrFSNbjcx+/j34+GLXs/bic3Sn6NNJhFh5kkDPTVlcmOu37Ya+x/97PReA1+T22ssFWaSTQys6ZPOyvJhK2NrdLCkiHPmvrgK0F/LrKKv5g=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTY0Njg3NDAxMDExMSwKICAicHJvZmlsZUlkIiA6ICI5ZDIyZGRhOTVmZGI0MjFmOGZhNjAzNTI1YThkZmE4ZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJTYWZlRHJpZnQ0OCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9mMGVlODdhNmJjNjZkM2VjZGExYjRjNzUyNGEwY2U0NGQ3MjY3MWM0MGQ5ZWRkOThjOGI2YjBjZmEzMWFiZTFjIgogICAgfQogIH0KfQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-29.5, 93, -17.5, 39, 0);
            }

            // TODO: holding bow
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        event.player().sendTo(ServerType.MURDER_MYSTERY_LOBBY);
    }
}
