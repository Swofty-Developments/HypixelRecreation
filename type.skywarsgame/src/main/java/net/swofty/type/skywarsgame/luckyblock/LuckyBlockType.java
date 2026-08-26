package net.swofty.type.skywarsgame.luckyblock;

import lombok.Getter;
import net.kyori.adventure.text.format.TextColor;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;

import java.util.List;

@Getter
public enum LuckyBlockType {
    GUARDIAN(Block.CYAN_STAINED_GLASS, Material.CYAN_STAINED_GLASS, "Guardian", TextColor.color(0x55FFFF),
            "c7db2aeca61b7616888b91fbe215501c70fc72ee8165aa971c0312381d41a795"),
    WEAPONRY(Block.RED_STAINED_GLASS, Material.RED_STAINED_GLASS, "Weaponry", TextColor.color(0xFF5555),
            "93bcdf15334b178f998ead1bde6afa88a3d4a01c9dd2c8689c2dbae2e93c997"),
    WILD(Block.LIME_STAINED_GLASS, Material.LIME_STAINED_GLASS, "Wild", TextColor.color(0x55FF55),
            "3239aaf6a4fd43bffcbf0eb28c11d92e33e09c379f842771ea8962a9672ae344"),
    CRAZY(Block.YELLOW_STAINED_GLASS, Material.YELLOW_STAINED_GLASS, "Crazy", TextColor.color(0xFFFF55),
            "e0a443e0eca7f5d30622dd937f1e5ea2cdf15d10c27a199c68a7ce09c39f6b69"),
    INSANE(Block.PINK_STAINED_GLASS, Material.PINK_STAINED_GLASS, "Insane", TextColor.color(0xFF00AA),
            "5ee0a134d3f3b3cdf29fe3defbd8bc6d68111e614dd412ebbbb45a4379c5d1f1"),
    OP_RULE(Block.BLACK_STAINED_GLASS, Material.BLACK_STAINED_GLASS, "OP Rule", TextColor.color(0x00AAAA),
            "3725da82aa0ade5d52bd2024f4bc1d019fc030e9ec5e0ec158c7f9a6aa0c43ba");

    public static final Tag<String> LUCKY_BLOCK_TYPE_TAG = Tag.String("lucky_block_type");
    private final Block glassBlock;
    private final Material material;
    private final String displayName;
    private final TextColor color;
    private final String skullTexture;

    LuckyBlockType(Block glassBlock, Material material, String displayName, TextColor color, String skullTexture) {
        this.glassBlock = glassBlock;
        this.material = material;
        this.displayName = displayName;
        this.color = color;
        this.skullTexture = skullTexture;
    }

    public boolean matches(Block block) {
        return this.glassBlock.compare(block);
    }

    public static LuckyBlockType fromBlock(Block block) {
        for (LuckyBlockType type : values()) {
            if (type.matches(block)) {
                return type;
            }
        }
        return null;
    }

    public static boolean isLuckyBlock(Block block) {
        return fromBlock(block) != null;
    }

    public static LuckyBlockType fromName(String name) {
        for (LuckyBlockType type : values()) {
            if (type.name().equals(name)) {
                return type;
            }
        }
        return null;
    }

    public ItemStack createItemStack() {
        return ItemStackCreator.head(skullTexture, 1,
                Text.of("<color:{}>{} Lucky Block", color, displayName),
                List.of(
                        Text.of("<7>Place this block to receive"),
                        Text.of("<7>a random <color:{}>{} <7>reward!", color, displayName)
                )
        ).set(LUCKY_BLOCK_TYPE_TAG, this.name()).build();
    }
}
