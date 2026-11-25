package net.swofty.type.skyblockgeneric.furniture;

public enum FurnitureLimitPool {
    FURNITURE(15),
    CENTURY_CAKE(16),
    EXPERIMENTATION_TABLE(1),
    FLOATING_CRYSTAL(1),
    GREAT_SPOOK_TREE(1),
    ISLAND_NPC(21),
    FAIRY_SOUL(1),
    POSTCARD(1),
    SHOWCASE_BLOCK(10),
    SOCIAL_DISPLAY(1),
    TRAINING_DUMMY(4),
    FARMING_CONTEST_DISPLAY(1),
    BINGO_DISPLAY(1);

    private final int limit;

    FurnitureLimitPool(int limit) {
        this.limit = limit;
    }

    public int limit() {
        return limit;
    }
}
