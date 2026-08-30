package net.swofty.type.bedwarsgame.events;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.event.instance.InstanceBlockUpdateEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.potion.PotionEffect;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig.MapTeam;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig.TeamKey;
import net.swofty.type.bedwarsgame.TypeBedWarsGameLoader;
import net.swofty.type.bedwarsgame.game.BedWarsGame;
import net.swofty.type.bedwarsgame.item.impl.LuckyBlockItem;
import net.swofty.type.bedwarsgame.stats.BedWarsStatsRecorder;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.game.game.GameState;
import net.swofty.type.game.replay.dispatcher.BlockChangeDispatcher;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.PhasedEvent;

import java.util.Map;
import java.util.Objects;

public class ActionGameBreak implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.ALL, requireDataLoaded = false)
    public void globalBlockChange(InstanceBlockUpdateEvent event) {
        BedWarsGame game = TypeBedWarsGameLoader.getGameByInstance(event.getInstance());
        if (game == null || !game.getReplayManager().isRecording()) {
            return;
        }

        if (Boolean.TRUE.equals(event.getBlock().getTag(TypeBedWarsGameLoader.PLAYER_PLACED_TAG))) {
            return; // Player placements are recorded in ActionGamePlace
        }

        BlockChangeDispatcher blockDispatcher =
            game.getReplayManager().getBlockChangeDispatcher();
        if (blockDispatcher != null) {
            Block previousBlock = event.getInstance().getBlock(event.getBlockPosition());

            blockDispatcher.recordBlockChange(
                event.getBlockPosition().blockX(),
                event.getBlockPosition().blockY(),
                event.getBlockPosition().blockZ(),
                previousBlock.stateId(),
                event.getBlock().stateId()
            );
        }
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true)
    public void blockBreak(PlayerBlockBreakEvent event) {
        BedWarsPlayer player = (BedWarsPlayer) event.getPlayer();
        Block blockBeingBroken = event.getBlock();

        BedWarsGame game = player.getGame();
        if (game == null || game.getState() != GameState.IN_PROGRESS) {
            event.setCancelled(true);
            return;
        }

        boolean isTeamBedPart = false;
        TeamKey playerTeamKey = player.getTeamKey();
        Point brokenBlockPosition = event.getBlockPosition();

        if (LuckyBlockItem.handleBreak(player, brokenBlockPosition, blockBeingBroken)) {
            event.setCancelled(true);
            return;
        }

        // Check if it's a part of any team's bed first
        for (Map.Entry<TeamKey, MapTeam> entry : game.getMapEntry().getConfiguration().getTeams().entrySet()) {
            TeamKey teamKey = entry.getKey();
            MapTeam team = entry.getValue();
            BedWarsMapsConfig.TwoBlockPosition bedPos = team.getBed();
            if (bedPos == null || bedPos.feet() == null || bedPos.head() == null) continue;

            Point feetPoint = new Pos(bedPos.feet().x(), bedPos.feet().y(), bedPos.feet().z());
            Point headPoint = new Pos(bedPos.head().x(), bedPos.head().y(), bedPos.head().z());

            if (brokenBlockPosition.sameBlock(feetPoint) || brokenBlockPosition.sameBlock(headPoint)) {
                // This is team X's bed
                if (teamKey.equals(playerTeamKey)) {
                    if (player.allowsPersistentProgress())
                        player.getAchievementHandler().completeAchievement("bedwars.you_cant_do_that");
                    player.sendMessage("<c>You can't destroy your own bed!");
                    event.setCancelled(true);
                    return;
                }
                if (!game.isBedAlive(teamKey)) {
                    event.setCancelled(true);
                    return;
                }
                game.onBedDestroyed(teamKey, player);
                BedWarsStatsRecorder.recordBedBroken(player, game.getGameType());

                if (player.hasEffect(PotionEffect.INVISIBILITY) && player.isInvisible()) {
                    if (player.allowsPersistentProgress())
                        player.getAchievementHandler().completeAchievement("bedwars.sneaky_rusher"); // break an bed while invisible
                }
                event.setCancelled(true); // handled the bed destruction and block removal
                return;
            }
        }

        // If it's not a team bed part, then check if it's any other player-placed block
        if (!isTeamBedPart) {
            if (Boolean.TRUE.equals(blockBeingBroken.getTag(TypeBedWarsGameLoader.PLAYER_PLACED_TAG))) {
                new ItemEntity(ItemStack.of(Objects.requireNonNull(blockBeingBroken.material()))).setInstance(player.getInstance(), event.getBlockPosition());
                event.setCancelled(false);
            } else {
                // Not a team bed and not a player-placed block
                player.sendMessage("<c>You can only break blocks placed by players!");
                event.setCancelled(true);
            }
        }
    }
}
