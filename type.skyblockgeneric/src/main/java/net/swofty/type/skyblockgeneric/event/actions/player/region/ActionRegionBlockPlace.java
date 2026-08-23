package net.swofty.type.skyblockgeneric.event.actions.player.region;

import net.minestom.server.coordinate.Point;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.tag.Tag;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.generic.utility.MathUtility;

public class ActionRegionBlockPlace implements HypixelEventClass {
    private static final int ISLAND_SIZE = 161;
    private static final Tag<Boolean> PLAYER_PLACED_TAG = Tag.Boolean("player_placed");

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(PlayerBlockPlaceEvent event) {
        final SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();

        if (player.isBypassBuild()) {
            return;
        }

        if (!HypixelConst.isIslandServer()) {
            event.setCancelled(true);
            return;
        }

        int islandSizePlus = (int) Math.floor((double) ISLAND_SIZE/2);
        int islandSizeMinus = -islandSizePlus;
        Point position = event.getBlockPosition();
        int x = position.blockX();
        int z = position.blockZ();

        if (x > islandSizePlus || x < islandSizeMinus || z > islandSizePlus || z < islandSizeMinus) {
            event.setCancelled(true);
            player.sendMessage("<c>You can't build any further in this direction!");
            return;
        }

        // Solve weird placement block issues
        MathUtility.delay(() -> {
            //player.getChunk().sendChunk(player);
        }, 5);

        event.setBlock(event.getBlock().withTag(PLAYER_PLACED_TAG, true));
    }
}

