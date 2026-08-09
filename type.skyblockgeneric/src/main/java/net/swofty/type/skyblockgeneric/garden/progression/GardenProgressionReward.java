package net.swofty.type.skyblockgeneric.garden.progression;

public record GardenProgressionReward(String type, String key, long amount) {
    public static GardenProgressionReward spokenNpc(String key) {
        return new GardenProgressionReward("SPOKEN_TO_NPC", key, 1L);
    }

    public static GardenProgressionReward flag(String key) {
        return new GardenProgressionReward("PROFILE_FLAG", key, 1L);
    }

    public static GardenProgressionReward donatedItem(String key) {
        return new GardenProgressionReward("ITEM_DONATED", key, 1L);
    }

    public static GardenProgressionReward exportedItem(String key, long amount) {
        return new GardenProgressionReward("ITEM_EXPORTED", key, amount);
    }

    public static GardenProgressionReward counter(String key, long amount) {
        return new GardenProgressionReward("PROFILE_COUNTER", key, amount);
    }
}
