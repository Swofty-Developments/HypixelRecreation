package net.swofty.type.skyblockgeneric.loottable;

import lombok.NonNull;
import net.kyori.adventure.key.Key;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public abstract class PityLootTable extends SkyBlockLootTable {
    protected PityLootTable(Key key) {
        super(key);
    }

    public abstract @NonNull PityDefinition getPityDefinition();

    public abstract boolean isPityReward(LootRecord record);

    @Override
    protected double adjustChance(SkyBlockPlayer player, LootRecord record, double chance) {
        if (isPityReward(record) && PityService.guaranteesNext(player, getPityDefinition())) return 1;
        return chance;
    }

    @Override
    protected void afterRoll(SkyBlockPlayer player, List<LootRecord> records) {
        PityService.recordAttempt(player, getPityDefinition(), records.stream().anyMatch(this::isPityReward));
    }
}
