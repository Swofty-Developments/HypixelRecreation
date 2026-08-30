package net.swofty.type.skywarsgame.events;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skywars.SkywarsGameType;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.game.game.GameState;
import net.swofty.type.skywarsgame.TypeSkywarsGameLoader;
import net.swofty.type.skywarsgame.game.SkywarsGame;
import net.swofty.type.skywarsgame.luckyblock.LuckyBlock;
import net.swofty.type.skywarsgame.luckyblock.LuckyBlockType;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

public class ActionLuckyBlockPlace implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void onBlockPlace(PlayerBlockPlaceEvent event) {
        if (!(event.getPlayer() instanceof SkywarsPlayer player)) return;

        ItemStack itemInHand = player.getItemInMainHand();
        if (itemInHand.material() != Material.PLAYER_HEAD) return;

        String luckyBlockTypeName = itemInHand.getTag(LuckyBlockType.LUCKY_BLOCK_TYPE_TAG);
        if (luckyBlockTypeName == null) return;

        LuckyBlockType type = LuckyBlockType.fromName(luckyBlockTypeName);
        if (type == null) return;

        event.setCancelled(true);

        SkywarsGame game = TypeSkywarsGameLoader.getPlayerGame(player);
        if (game == null) return;

        if (game.getGameType() != SkywarsGameType.SOLO_LUCKY_BLOCK) return;

        if (game.getState() != GameState.IN_PROGRESS) return;

        LuckyBlock luckyBlockManager = game.getLuckyBlockManager();
        if (luckyBlockManager == null) return;

        Pos blockPos = new Pos(event.getBlockPosition());
        luckyBlockManager.placeLuckyBlock(blockPos, type);

        ItemStack newStack = itemInHand.withAmount(itemInHand.amount() - 1);
        player.setItemInMainHand(newStack);
    }
}
