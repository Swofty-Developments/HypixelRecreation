package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.turtle;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.TURTLE, minimumRarity = Rarity.LEGENDARY, order = 3)
public final class TurtleShellAbility implements PetAbility {
    private static final double HP_THRESHOLD = 0.40;
    private static final int HITS_FOR_VITALITY = 10;
    private static final RarityValue<Double> DAMAGE_REDUCTION_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.25, 0.0, 0.0);
    private static final double VITALITY_BASE = 5.0;
    private static final double VITALITY_PER_LEVEL = 0.05;

    private int hitsTaken;

    @Override
    public String getName() {
        return "Turtle Shell";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        double reduction = DAMAGE_REDUCTION_PER_LEVEL.getForRarity(rarity) * level;
        double vitality = VITALITY_BASE + VITALITY_PER_LEVEL * level;

        return List.of(
                "<7>When under <c>40% <7>maximum HP, you take",
                "<a>" + decimalify(reduction, 1) + "% <7>less damage. Gain",
                "<4>+" + decimalify(vitality, 1) + " <stat:vitality> <7>after",
                "<7>taking <a>10 <7>hits."
        );
    }

    @PetEventHandler
    public void onDamaged(PetEvent.Damaged event) {
        hitsTaken++;

        SkyBlockPlayer player = event.player();
        if (player.getHealth() / player.getMaxHealth() >= HP_THRESHOLD) return;

        Rarity rarity = event.pet().getAttributeHandler().getRarity();
        int level = event.pet().getAttributeHandler().getPetData().getAsLevel(rarity);
        double reduction = DAMAGE_REDUCTION_PER_LEVEL.getForRarity(rarity) * level;

        event.damage(event.damage() * (1 - reduction / 100));
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        if (hitsTaken < HITS_FOR_VITALITY) return ItemStatistics.empty();

        return ItemStatistics.builder()
                .withBase(ItemStatistic.VITALITY, VITALITY_BASE + VITALITY_PER_LEVEL * level)
                .build();
    }
}
