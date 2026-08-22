package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.skeleton_horse;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.SKELETON_HORSE, minimumRarity = Rarity.LEGENDARY, order = 1)
public final class StampedeAbility implements PetAbility {
    private static final int MAX_STACKS = 20;
    private static final long DURATION_MILLIS = 5_000L;
    private static final double ATTACK_SPEED_PER_LEVEL = 0.01;
    private static final double STRENGTH_PER_LEVEL = 0.1;

    private int stacks;
    private long lastProc;

    @Override
    public String getName() {
        return "Stampede";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String attackSpeed = decimalify(ATTACK_SPEED_PER_LEVEL * level, 2);
        String strength = decimalify(STRENGTH_PER_LEVEL * level, 2);

        return List.of(
                "<7>Mob kills grant a stack of <e>+" + attackSpeed + " <stat:bonus_attack_speed>",
                "<7>and <c>+" + strength + " <stat:strength> <7>for <a>5s<7>.",
                "<8>(Max " + MAX_STACKS + " stacks)"
        );
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        if (stacks <= 0) return ItemStatistics.empty();
        if (System.currentTimeMillis() - lastProc > DURATION_MILLIS) {
            stacks = 0;
            return ItemStatistics.empty();
        }

        return ItemStatistics.builder()
                .withBase(ItemStatistic.BONUS_ATTACK_SPEED, ATTACK_SPEED_PER_LEVEL * level * stacks)
                .withBase(ItemStatistic.STRENGTH, STRENGTH_PER_LEVEL * level * stacks)
                .build();
    }

    @PetEventHandler
    public void onKilledMob(PetEvent.KilledMob event) {
        stacks = Math.min(stacks + 1, MAX_STACKS);
        lastProc = System.currentTimeMillis();
    }
}
