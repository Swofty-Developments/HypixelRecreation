package net.swofty.type.skyblockgeneric.block.blocks;

import lombok.NonNull;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.instance.block.Block;
import net.swofty.type.skyblockgeneric.block.SkyBlockBlock;
import net.swofty.type.skyblockgeneric.block.impl.BlockInteractable;
import net.swofty.type.skyblockgeneric.block.impl.CustomSkyBlockBlock;
import net.swofty.type.skyblockgeneric.gui.inventories.GUIEnchantmentTable;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public final class BlockEnchantmentTable implements CustomSkyBlockBlock, BlockInteractable {
    @Override
    public @NonNull Block getDisplayMaterial() {
        return Block.ENCHANTING_TABLE;
    }

    @Override
    public @NonNull Boolean shouldPlace(SkyBlockPlayer player) {
        return true;
    }

    @Override
    public @NonNull Boolean shouldDestroy(SkyBlockPlayer player) {
        return true;
    }

    @Override
    public void onInteract(PlayerBlockInteractEvent event, SkyBlockBlock block) {
        event.setBlockingItemUse(true);
        new GUIEnchantmentTable(event.getInstance(), event.getBlockPosition().asPos())
                .open((SkyBlockPlayer) event.getPlayer());
    }
}
