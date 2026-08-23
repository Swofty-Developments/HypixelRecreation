package net.swofty.type.skywarslobby.hologram;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.swofty.type.generic.entity.hologram.PlayerHolograms;
import net.swofty.type.generic.user.HypixelPlayer;
import org.tinylog.Logger;

public class SoulWellHologramManager {
    private static final Pos SOUL_WELL_POS = new Pos(33.5, 68, 0.5);
    private static boolean initialized = false;

    public static void initialize(Instance instance) {
        if (initialized) return;
        initialized = true;
        Logger.info("SoulWellHologramManager initialized");
    }

    public static void spawnHologramForPlayer(HypixelPlayer player) {
        String[] lines = new String[]{
                "<b>Soul Well",
                "<e><l>RIGHT CLICK"
        };

        PlayerHolograms.ExternalPlayerHologram externalHologram = PlayerHolograms.ExternalPlayerHologram.builder()
                .pos(SOUL_WELL_POS)
                .text(lines)
                .player(player)
                .instance(player.getInstance())
                .spacing(0.3)
                .displayFunction(p -> new String[]{
                        "<b>Soul Well",
                        "<e><l>RIGHT CLICK"
                })
                .build();

        PlayerHolograms.removeExternalPlayerHologramsAt(player, SOUL_WELL_POS);
        PlayerHolograms.addExternalPlayerHologram(externalHologram);
    }

    public static void removeHologramForPlayer(HypixelPlayer player) {
        PlayerHolograms.removeExternalPlayerHologramsAt(player, SOUL_WELL_POS);
    }

    public static Pos getSoulWellPosition() {
        return SOUL_WELL_POS;
    }

    public static void shutdown() {
        initialized = false;
    }
}