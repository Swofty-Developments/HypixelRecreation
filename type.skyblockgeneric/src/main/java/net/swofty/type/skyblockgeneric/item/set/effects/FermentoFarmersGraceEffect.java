package net.swofty.type.skyblockgeneric.item.set.effects;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetBonusType;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetContext;
import net.swofty.type.skyblockgeneric.item.set.impl.ArmorSetEffect;

import java.util.List;
import java.util.Set;

public final class FermentoFarmersGraceEffect extends ArmorSetEffect {
    @Override
    public String getName() { return "Farmer's Grace"; }

    @Override
    public ArmorSetBonusType getType() { return ArmorSetBonusType.ABILITY; }

    @Override
    public List<String> getDescription(ArmorSetContext context) {
        return List.of("Grants immunity to trampling crops.");
    }

    @Override
    public int getRequiredPieces(ArmorSetContext context) { return 1; }
    @Override
    public Set<ItemType> getRequiredItems() { return Set.of(ItemType.FERMENTO_BOOTS); }
}
