package net.swofty.type.skyblockgeneric.block;

import net.minestom.server.instance.block.Block;
import net.minestom.server.tag.Tag;
import net.swofty.type.skyblockgeneric.block.attribute.BlockAttribute;
import net.swofty.type.skyblockgeneric.block.attribute.BlockAttributeHandler;
import net.swofty.type.skyblockgeneric.block.attribute.attributes.BlockAttributeType;
import net.swofty.type.skyblockgeneric.block.impl.CustomSkyBlockBlock;
import net.swofty.type.skyblockgeneric.block.placement.states.state.Facing;
import org.tinylog.Logger;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class SkyBlockBlock {
    public List<BlockAttribute<?>> attributes = new ArrayList<>();
    public final Class<? extends CustomSkyBlockBlock> clazz;
    public CustomSkyBlockBlock instance = null;
    private final Facing facing;

    public SkyBlockBlock(BlockType blockType) {
        this(blockType, Facing.NORTH);
    }

    public SkyBlockBlock(BlockType type, Facing facing) {
        this.facing = facing;
        this.clazz = type.clazz;

        for (BlockAttribute attribute : BlockAttribute.getPossibleAttributes()) {
            attribute.setValue(attribute.getDefaultValue(clazz));
            attributes.add(attribute);
        }

        try {
            instance = clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            Logger.error(e, "Failed to instantiate CustomSkyBlockBlock of type: {}", clazz.getSimpleName());
        }

        BlockAttributeType typeAttribute = (BlockAttributeType) getAttribute("block_type");
        typeAttribute.setValue(type.name());
    }

    public SkyBlockBlock(Block block) {
        this(block, Facing.NORTH);
    }

    public SkyBlockBlock(Block block, Facing facing) {
        this.facing = facing;
        this.clazz = BlockType.valueOf(block.getTag(Tag.String("block_type"))).clazz;

        for (BlockAttribute attribute : BlockAttribute.getPossibleAttributes()) {
            attribute.setValue(attribute.loadFromString(block.getTag(Tag.String(attribute.getKey()))));
            attributes.add(attribute);
        }

        try {
            instance = clazz.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                 NoSuchMethodException e) {
            Logger.error(e, "Failed to instantiate CustomSkyBlockBlock from block: {}", clazz.getSimpleName());
        }
    }

    public BlockAttributeHandler getAttributeHandler() {
        return new BlockAttributeHandler(this);
    }

    public Block toBlock() {
        Block block = instance.getDisplayMaterial();

        for (BlockAttribute<?> attribute : attributes) {
            block = block.withTag(Tag.String(attribute.getKey()), attribute.saveIntoString());
        }

        if (block.properties().containsKey("facing")) {
            block = block.withProperty("facing", facing.getValue());
        }

        return block;
    }

    public Object getGenericInstance() {
        if (instance != null)
            return instance;

        try {
            instance = clazz.getDeclaredConstructor().newInstance();
            return instance;
        } catch (Exception _) {
        }
        return null;
    }

    public static boolean isSkyBlockBlock(Block block) {
        return block.hasTag(Tag.String("block_type"));
    }

    public Object getAttribute(String key) {
        return attributes.stream().filter(attribute -> attribute.getKey().equals(key)).findFirst().orElse(null);
    }
}
