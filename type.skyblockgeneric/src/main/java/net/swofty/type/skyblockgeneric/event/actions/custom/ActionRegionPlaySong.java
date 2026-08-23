package net.swofty.type.skyblockgeneric.event.actions.custom;

import net.swofty.commons.Songs;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.SkyBlockTypeLoader;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.event.custom.PlayerRegionChangeEvent;
import net.swofty.type.skyblockgeneric.noteblock.SkyBlockSong;
import net.swofty.type.skyblockgeneric.noteblock.SkyBlockSongsHandler;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.user.SkyBlockActionBar;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class ActionRegionPlaySong implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = false, isAsync = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerRegionChangeEvent event) {
        SkyBlockPlayer player = event.getPlayer();
        SkyBlockSongsHandler songHandler = player.getSongHandler();
        if (!SkyBlockSongsHandler.isEnabled) return;

        RegionType to = event.getTo();
        RegionType from = event.getFrom();
        if (to == null || (from != null && to.equals(from))) return;

        Songs songToPlay = null;
        List<Songs> regionSongs = to.getSongs();
        if (regionSongs != null && !regionSongs.isEmpty()) {
            songToPlay = regionSongs.get((int) (Math.random() * regionSongs.size()));
        } else if (HypixelConst.getTypeLoader() instanceof SkyBlockTypeLoader skyBlockTypeLoader) {
            songToPlay = skyBlockTypeLoader.getIslandSong();
        }

        SkyBlockSongsHandler.PlayerSong activeSong = songHandler.getPlayerSong();
        if (songToPlay == null) {
            if (activeSong != null) songHandler.stopPlayerSong();
            return;
        }

        if (activeSong != null && activeSong.song().getSong() == songToPlay) {
            return;
        }

        songHandler.setPlayerSong(new SkyBlockSong(songToPlay));

        SkyBlockActionBar.getFor(player).addReplacement(
            SkyBlockActionBar.BarSection.MANA,
            Text.of("<2>{}", StringUtility.toNormalCase(songToPlay.name())),
            20,
            2
        );
    }
}
