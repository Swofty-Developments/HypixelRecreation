package net.swofty.type.skyblockgeneric.event.actions.player;

import net.minestom.server.event.entity.EntityAttackEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.block.BlockType;
import net.swofty.type.skyblockgeneric.block.SkyBlockBlock;
import net.swofty.type.skyblockgeneric.block.impl.EntityBackedBlock;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public final class ActionEntityBackedBlockDestroy implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.ENTITY, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(EntityAttackEvent event) {
        if (!(event.getEntity() instanceof SkyBlockPlayer player)) return;

        BlockType type = BlockType.getFromName(event.getTarget().getTag(EntityBackedBlock.BLOCK_TYPE_TAG));
        String blockId = event.getTarget().getTag(EntityBackedBlock.BLOCK_ID_TAG);
        if (type == null || blockId == null) return;

        SkyBlockBlock block = new SkyBlockBlock(type);
        if (block.getGenericInstance() instanceof EntityBackedBlock entityBackedBlock)
            entityBackedBlock.onEntityAttack(event, block, blockId);
    }
}
