package net.swofty.type.skyblockgeneric.event.actions.player;

import net.minestom.server.entity.PlayerHand;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.block.BlockType;
import net.swofty.type.skyblockgeneric.block.SkyBlockBlock;
import net.swofty.type.skyblockgeneric.block.impl.EntityBackedBlock;

public final class ActionEntityBackedBlockInteract implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerEntityInteractEvent event) {
        if (event.getHand() != PlayerHand.MAIN) return;

        BlockType type = BlockType.getFromName(event.getTarget().getTag(EntityBackedBlock.BLOCK_TYPE_TAG));
        String blockId = event.getTarget().getTag(EntityBackedBlock.BLOCK_ID_TAG);
        if (type == null || blockId == null) return;

        SkyBlockBlock block = new SkyBlockBlock(type);
        if (block.getGenericInstance() instanceof EntityBackedBlock entityBackedBlock)
            entityBackedBlock.onEntityInteract(event, block, blockId);
    }
}
