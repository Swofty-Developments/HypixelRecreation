package net.swofty.type.ravengardgeneric.music;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.generic.HypixelGenericLoader;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The lobby soundtrack: every player on the lobby server hears the pack's day or night theme on
 * loop, on the docked Nevermore and in Ravenport alike. The client cannot loop a served track, so
 * each play is rescheduled after the track's measured length, picking day or night again from the
 * world clock at that moment.
 */
public final class RavengardMusic {
    private static final long DAY_TRACK_MILLIS = 244_900;
    private static final long NIGHT_TRACK_MILLIS = 240_000;
    private static final Sound DAY = track("music.day");
    private static final Sound NIGHT = track("music.night");

    private static final Map<UUID, Long> NEXT_START = new ConcurrentHashMap<>();

    private RavengardMusic() {
    }

    public static void start() {
        MinecraftServer.getSchedulerManager()
                .buildTask(RavengardMusic::tick)
                .repeat(TaskSchedule.seconds(1))
                .schedule();
    }

    private static void tick() {
        long now = System.currentTimeMillis();
        Set<UUID> online = new HashSet<>();
        for (HypixelPlayer player : HypixelGenericLoader.getLoadedPlayers()) {
            online.add(player.getUuid());
            long next = NEXT_START.getOrDefault(player.getUuid(), 0L);
            if (now < next) {
                continue;
            }
            boolean night = isNight(player);
            player.playSound(night ? NIGHT : DAY, Sound.Emitter.self());
            NEXT_START.put(player.getUuid(), now + (night ? NIGHT_TRACK_MILLIS : DAY_TRACK_MILLIS));
        }
        NEXT_START.keySet().retainAll(online);
    }

    private static boolean isNight(HypixelPlayer player) {
        if (player.getInstance() == null) {
            return false;
        }
        long time = Math.floorMod(player.getInstance().getTime(), 24000L);
        return time >= 13000 && time < 23000;
    }

    private static Sound track(String key) {
        return Sound.sound()
                .type(Key.key("hypixel_ravengard", key))
                .source(Sound.Source.MUSIC)
                .volume(1.0f)
                .pitch(1.0f)
                .build();
    }
}
