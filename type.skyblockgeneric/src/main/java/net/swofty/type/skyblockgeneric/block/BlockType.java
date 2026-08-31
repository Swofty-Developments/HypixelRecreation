package net.swofty.type.skyblockgeneric.block;

import net.swofty.type.skyblockgeneric.block.blocks.BlockBrewingStand;
import net.swofty.type.skyblockgeneric.block.blocks.BlockChest;
import net.swofty.type.skyblockgeneric.block.blocks.BlockDecoration;
import net.swofty.type.skyblockgeneric.block.blocks.BlockExperimentationTable;
import net.swofty.type.skyblockgeneric.block.blocks.BlockEnchantmentTable;
import net.swofty.type.skyblockgeneric.block.impl.CustomSkyBlockBlock;

public enum BlockType {
    CHEST(BlockChest.class),
    DECORATION(BlockDecoration.class),
    BREWING_STAND(BlockBrewingStand.class),
    EXPERIMENTATION_TABLE(BlockExperimentationTable.class),
    ENCHANTMENT_TABLE(BlockEnchantmentTable.class),
    ;

    public final Class<? extends CustomSkyBlockBlock> clazz;

    BlockType(Class<? extends CustomSkyBlockBlock> clazz) {
        this.clazz = clazz;
    }

    public static BlockType getFromName(String name) {
        for (BlockType blockType : values()) {
            if (blockType.name().equalsIgnoreCase(name)) {
                return blockType;
            }
        }
        return null;
    }
}
