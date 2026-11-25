package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.baby_yeti;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.BABY_YETI, minimumRarity = Rarity.COMMON, order = 0)
public final class YetiFuryAbility implements PetAbility {
    private static final RarityValue<Double> DAMAGE_PER_LEVEL =
            new RarityValue<>(0.5, 0.75, 0.75, 1.0, 1.0, 1.5, 0.0);
    private static final RarityValue<Double> COOLDOWN_PER_LEVEL =
            new RarityValue<>(0.2, 0.35, 0.35, 0.5, 0.5, 0.75, 0.0);

    @Override
    public String getName() {
        return "Yeti Fury";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double damage = DAMAGE_PER_LEVEL.getForRarity(rarity) * level;
        double cooldown = COOLDOWN_PER_LEVEL.getForRarity(rarity) * level;

        return List.of(
                "<7>Buffs the <6>Yeti Sword <7>by <a>" + decimalify(damage, 1) + " <c><stat:damage>",
                "<7>and <b><stat:intelligence> <7>and",
                "<7>reduces its cooldown by <a>" + decimalify(cooldown, 2) + "%<7>."
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        if (!isHoldingYetiSword(player)) return ItemStatistics.empty();

        double buff = DAMAGE_PER_LEVEL.getForRarity(rarity) * level;

        return ItemStatistics.builder()
                .withBase(ItemStatistic.DAMAGE, buff)
                .withBase(ItemStatistic.INTELLIGENCE, buff)
                .build();
    }

    @PetEventHandler
    public void onAbilityCooldown(PetEvent.AbilityCooldown event) {
        if (!isYetiSword(event.item())) return;

        Rarity rarity = event.pet().getAttributeHandler().getRarity();
        int level = event.pet().getAttributeHandler().getPetData().getAsLevel(rarity);
        double reduction = COOLDOWN_PER_LEVEL.getForRarity(rarity) * level;

        event.cooldown(event.cooldown() * (1 - reduction / 100));
    }

    private static boolean isHoldingYetiSword(SkyBlockPlayer player) {
        if (player.getItemInMainHand().isAir()) return false;
        return isYetiSword(new SkyBlockItem(player.getItemInMainHand()));
    }

    private static boolean isYetiSword(SkyBlockItem item) {
        return item.getItemType() == ItemType.YETI_SWORD
                || item.getItemType() == ItemType.STARRED_YETI_SWORD;
    }
}
