package net.swofty.type.skyblockgeneric.block.impl;

import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.tag.Tag;
import net.swofty.type.skyblockgeneric.block.SkyBlockBlock;

public interface EntityBackedBlock {
    Tag<String> BLOCK_TYPE_TAG = Tag.String("entity_backed_block_type");
    Tag<String> BLOCK_ID_TAG = Tag.String("entity_backed_block_id");

    void onEntityInteract(PlayerEntityInteractEvent event, SkyBlockBlock block, String blockId);

    void onEntityAttack(EntityAttackEvent event, SkyBlockBlock block, String blockId);
}
