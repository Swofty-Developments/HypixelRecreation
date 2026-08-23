package net.swofty.type.skyblockgeneric.item.crafting.grid;

public record CraftingSlot(CraftingSlot.Region region, int index) {

    public enum Region {
        GRID,
        PLAYER,
        RESULT
    }

    public static final CraftingSlot RESULT = new CraftingSlot(Region.RESULT, 0);

    public static CraftingSlot grid(int index) {
        return new CraftingSlot(Region.GRID, index);
    }

    public static CraftingSlot player(int index) {
        return new CraftingSlot(Region.PLAYER, index);
    }
}
