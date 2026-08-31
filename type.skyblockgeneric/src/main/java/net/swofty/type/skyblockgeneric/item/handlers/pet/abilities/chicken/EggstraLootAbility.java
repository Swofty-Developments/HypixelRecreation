package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.chicken;

import net.minestom.server.entity.EntityType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.CHICKEN, minimumRarity = Rarity.RARE)
public final class EggstraLootAbility implements PetAbility {
    private static final RarityValue<Double> CHANCE_PER_LEVEL = new RarityValue<>(0.0, 0.0, 0.8, 1.0, 1.0, 1.0, 0.0);
    private static final Set<EntityType> ANIMALS = Set.of(
            EntityType.CHICKEN, EntityType.COW, EntityType.SHEEP,
            EntityType.PIG, EntityType.RABBIT
    );

    @Override
    public String getName() {
        return "Eggstra Loot";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double chance = CHANCE_PER_LEVEL.getForRarity(rarity) * level;

        return Arrays.asList(
                "<7>Chickens always drop an <f>Egg <7>when",
                "<7>killed. Grants a <a>" + decimalify(chance, 1) + "% <7>chance for",
                "<7>animals to drop an additional item."
        );
    }

    @PetEventHandler
    public void onChickenKill(PetEvent.KilledMob kill) {
        if (kill.mob().getEntityType() != EntityType.CHICKEN) return;
        kill.player().giveLoot(new SkyBlockItem(ItemStack.of(Material.EGG)), 1, kill.mob().getPosition());
    }

    @PetEventHandler
    public void onAnimalKill(PetEvent.KilledMob kill) {
        if (!ANIMALS.contains(kill.mob().getEntityType())) return;
        dropExtraLoot(kill);
    }

    private void dropExtraLoot(PetEvent.KilledMob context) {
        Rarity rarity = context.pet().getAttributeHandler().getRarity();
        int level = context.pet().getAttributeHandler().getPetData().getAsLevel(rarity);
        double chance = CHANCE_PER_LEVEL.getForRarity(rarity) * level;
        if (Math.random() * 100 >= chance) return;

        SkyBlockLootTable lootTable = context.mob().getLootTable();
        if (lootTable == null) return;

        Map<ItemType, SkyBlockLootTable.LootRecord> extraDrops = lootTable.runChances(context.player());
        for (Map.Entry<ItemType, SkyBlockLootTable.LootRecord> entry : extraDrops.entrySet()) {
            SkyBlockLootTable.LootRecord record = entry.getValue();
            if (SkyBlockLootTable.LootRecord.isNone(record)) continue;
            SkyBlockItem item = new SkyBlockItem(entry.getKey(), record.getAmount());
            context.player().giveLoot(item, record.getAmount(), context.mob().getPosition());
        }
    }
}
