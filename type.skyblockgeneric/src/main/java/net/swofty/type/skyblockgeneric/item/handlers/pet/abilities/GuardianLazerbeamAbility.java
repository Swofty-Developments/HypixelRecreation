package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.AbilityRuntime;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.AttackService;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

public final class GuardianLazerbeamAbility implements PetAbility {
    private static final long COOLDOWN_MILLIS = 3_000;
    private static final RarityValue<Double> DAMAGE_MULTIPLIER = new RarityValue<>(
            0.02, 0.06, 0.10, 0.15, 0.20, 1.20, 0.0);

    @Override
    public String getName() {
        return "Lazerbeam";
    }

    @Override
    public List<String> getDescription(SkyBlockItem instance) {
        Rarity rarity = instance.getAttributeHandler().getRarity();
        int level = instance.getAttributeHandler().getPetData().getAsLevel(rarity);
        double multiplier = DAMAGE_MULTIPLIER.getForRarity(rarity) * level;
        return List.of("<7>Zaps your enemies for <b>" + decimalify(multiplier, 2)
                + "x <7>your <stat:intelligence><7>every <a>3s<7>.");
    }

    @Override
    public void onEvent(PetEvent event) {
        if (!(event instanceof PetEvent.DamageMob damageMob)) return;
        SkyBlockPlayer player = damageMob.player();
        if (player.getRegion() != null && player.getRegion().getType() == RegionType.PRIVATE_ISLAND) return;

        AbilityRuntime runtime = player.getPetData().getAbilityRuntime(this);
        long now = System.currentTimeMillis();
        if (now - runtime.getLastProc() < COOLDOWN_MILLIS) return;

        SkyBlockItem pet = damageMob.pet();
        Rarity rarity = pet.getAttributeHandler().getRarity();
        int level = pet.getAttributeHandler().getPetData().getAsLevel(rarity);
        double intelligence = Math.max(0,
                player.getStatistics().allStatistics().getOverall(ItemStatistic.INTELLIGENCE));
        double damage = DAMAGE_MULTIPLIER.getForRarity(rarity) * level * intelligence;
        if (damage <= 0 || !AttackService.applyHit(player, damageMob.mob(), (float) damage, false)) return;
        runtime.setLastProc(now);
    }
}
