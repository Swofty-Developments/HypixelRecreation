package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.black_cat;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.collection.CollectionCategories;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;
import java.util.Map;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.BLACK_CAT, minimumRarity = Rarity.MYTHIC, order = 3)
public final class LootingAbility implements PetAbility {
    private static final RarityValue<Double> PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.0, 0.15, 0.0);

    @Override
    public String getName() {
        return "Looting";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double value = PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Gain <c>" + decimalify(value, 2) + "% <7>more collection",
                "<7>items from monsters!"
        );
    }

    @PetEventHandler
    public void onKill(PetEvent.KilledMob kill) {
        Rarity rarity = kill.pet().getAttributeHandler().getRarity();
        int level = kill.pet().getAttributeHandler().getPetData().getAsLevel(rarity);
        double chance = PER_LEVEL.getForRarity(rarity) * level;
        if (Math.random() * 100 >= chance) return;

        SkyBlockLootTable lootTable = kill.mob().getLootTable();
        if (lootTable == null) return;

        Map<ItemType, SkyBlockLootTable.LootRecord> extraDrops = lootTable.runChances(kill.player());
        for (Map.Entry<ItemType, SkyBlockLootTable.LootRecord> entry : extraDrops.entrySet()) {
            SkyBlockLootTable.LootRecord record = entry.getValue();
            if (SkyBlockLootTable.LootRecord.isNone(record)) continue;
            if (CollectionCategories.getCategory(entry.getKey()) == null) continue;
            SkyBlockItem item = new SkyBlockItem(entry.getKey(), record.getAmount());
            kill.player().giveLoot(item, record.getAmount(), kill.mob().getPosition());
        }
    }
}
