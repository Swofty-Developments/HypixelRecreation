package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities;

import net.minestom.server.entity.EntityType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.generic.entity.drop.ItemDrops;
import net.swofty.type.skyblockgeneric.entity.DroppedItemEntityImpl;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static net.swofty.commons.StringUtility.decimalify;

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
    public List<String> getDescription(SkyBlockItem pet) {
        Rarity rarity = pet.getAttributeHandler().getRarity();
        int level = pet.getAttributeHandler().getPetData().getAsLevel(rarity);
        double chance = CHANCE_PER_LEVEL.getForRarity(rarity) * level;

        return Arrays.asList(
                "<7>Chickens always drop an <f>Egg <7>when",
                "<7>killed. Grants a <a>" + decimalify(chance, 1) + "% <7>chance for",
                "<7>animals to drop an additional item."
        );
    }

    @Override
    public void onEvent(PetEvent event) {
        if (event instanceof PetEvent.Kill kill && kill.mob().getEntityType() == EntityType.CHICKEN) {
            dropItemForPlayer(
                    kill.player(),
                    new SkyBlockItem(ItemStack.of(Material.EGG)),
                    1,
                    kill.mob()
            );
        }
        if (event instanceof PetEvent.Kill kill && ANIMALS.contains(kill.mob().getEntityType())) {
            dropExtraLoot(kill);
        }
    }

    private void dropExtraLoot(PetEvent.Kill context) {
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
            dropItemForPlayer(context.player(), item, record.getAmount(), context.mob());
        }
    }

    private void dropItemForPlayer(SkyBlockPlayer player, SkyBlockItem item, int amount, SkyBlockMob mob) {
        ItemType type = item.getAttributeHandler().getPotentialType();
        if (type != null && player.canInsertItemIntoSacks(type, amount)) {
            player.getSackItems().increase(type, amount);
        } else if (player.getSkyBlockExperience().getLevel().asInt() >= 6) {
            player.addAndUpdateItem(item);
        } else {
            ItemDrops.dropAt(new DroppedItemEntityImpl(item, player),
                    mob.getInstance(), mob.getPosition().add(0, 0.5, 0));
        }
    }
}
