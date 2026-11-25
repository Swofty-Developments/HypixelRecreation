package net.swofty.type.skyblockgeneric.item.set.impl;

import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Set;

public abstract class ArmorSetEffect {
    public abstract String getName();

    public abstract ArmorSetBonusType getType();

    public abstract List<String> getDescription(ArmorSetContext context);

    public int getRequiredPieces(ArmorSetContext context) {
        return context.registry().getPieceCount();
    }

    public Set<ItemType> getRequiredItems() {
        return Set.of();
    }

    public boolean isRelevantTo(ItemType itemType) {
        return getRequiredItems().isEmpty() || getRequiredItems().contains(itemType);
    }

    public boolean isActive(ArmorSetContext context) {
        return getRequiredItems().isEmpty()
                ? context.wornPieces() >= getRequiredPieces(context)
                : context.wornItems().containsAll(getRequiredItems());
    }

    public ItemStatistics getStatistics(ArmorSetContext context) {
        return ItemStatistics.empty();
    }

    public float modifyIncomingDamage(ArmorSetContext context, SkyBlockMob mob, float damage) {
        return damage;
    }

    public float modifyOutgoingDamage(ArmorSetContext context, SkyBlockMob mob, float damage) {
        return damage;
    }

    public int modifyManaCost(ArmorSetContext context, int manaCost) {
        return manaCost;
    }

    public void onMobKill(ArmorSetContext context, SkyBlockMob mob) {
    }

    public void onEquip(SkyBlockPlayer player) {
    }

    public void onUnequip(SkyBlockPlayer player) {
    }

    protected ItemStatistics baseStatistic(ItemStatistic statistic, double value) {
        return ItemStatistics.builder().withBase(statistic, value).build();
    }

    protected String statistic(ItemStatistic statistic, double value) {
        String signedValue = value > 0 ? "+" + StringUtility.decimalify(value, 1) : StringUtility.decimalify(value, 1);
        return "<sbstat:" + statistic.name().toLowerCase() + ":" + signedValue + ">";
    }
}
