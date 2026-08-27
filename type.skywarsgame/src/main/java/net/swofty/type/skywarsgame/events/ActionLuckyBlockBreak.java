package net.swofty.type.skywarsgame.events;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.instance.block.Block;
import net.swofty.commons.skywars.SkywarsGameType;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.game.game.GameState;
import net.swofty.type.skywarsgame.TypeSkywarsGameLoader;
import net.swofty.type.skywarsgame.game.SkywarsGame;
import net.swofty.type.skywarsgame.luckyblock.LuckyBlock;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

public class ActionLuckyBlockBreak implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onBlockBreak(PlayerBlockBreakEvent event) {
        if (!(event.getPlayer() instanceof SkywarsPlayer player)) return;

        SkywarsGame game = TypeSkywarsGameLoader.getPlayerGame(player);
        if (game == null) return;

        if (game.getState() != GameState.IN_PROGRESS) return;

        Pos blockPos = new Pos(event.getBlockPosition());

        if (game.getChestManager().isChestPosition(blockPos)) {
            event.setCancelled(true);
            return;
        }

        if (game.getGameType() != SkywarsGameType.SOLO_LUCKY_BLOCK) return;

        Block block = event.getBlock();
        LuckyBlock luckyBlockManager = game.getLuckyBlockManager();

        if (luckyBlockManager != null && luckyBlockManager.isLuckyBlockMaterial(block)) {
            event.setCancelled(true);
            luckyBlockManager.breakLuckyBlock(player, blockPos);
        }
    }
}
