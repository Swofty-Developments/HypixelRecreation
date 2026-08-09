package net.swofty.type.garden.npc;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.swofty.type.garden.user.SkyBlockGarden;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public final class GardenNpcSupport {
    public static final Pos HIDDEN_POSITION = new Pos(0, -1000, 0);

    private GardenNpcSupport() {
    }

    public static boolean isVisibleOnGarden(SkyBlockPlayer player) {
        return player != null && player.isOnGarden();
    }

    public static Pos hiddenPosition() {
        return HIDDEN_POSITION;
    }

    public static Instance instanceFor(SkyBlockPlayer player) {
        if (player != null
            && player.getSkyBlockGarden() instanceof SkyBlockGarden garden
            && garden.getGardenInstance() != null) {
            return garden.getGardenInstance();
        }
        return HypixelConst.getInstanceContainer();
    }
}
