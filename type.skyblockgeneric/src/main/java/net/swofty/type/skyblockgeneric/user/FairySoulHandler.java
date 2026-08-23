package net.swofty.type.skyblockgeneric.user;

import net.swofty.type.skyblockgeneric.user.fairysouls.FairySoul;
import net.swofty.type.skyblockgeneric.user.fairysouls.FairySoulZone;

public record FairySoulHandler(SkyBlockPlayer player) {

    public int getFound(FairySoulZone zone) {
        int amount = 0;
        for (Integer id : player.getFairySouls().getAllFairySouls()) {
            FairySoul soul = FairySoul.getFairySoul(id);
            if (soul != null && soul.getZone() == zone) {
                amount++;
            }
        }
        return amount;
    }

    public int getMax(FairySoulZone zone) {
        int amount = 0;
        for (FairySoul soul : FairySoul.getFairySouls()) {
            if (soul.getZone() == zone) {
                amount++;
            }
        }
        return amount;
    }

    public int getTotalFoundFairySouls() {
        return player.getFairySouls().getAllFairySouls().size();
    }
}
