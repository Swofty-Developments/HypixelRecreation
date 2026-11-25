package net.swofty.type.skyblockgeneric.event.actions.custom.loot;

import net.swofty.commons.rates.GameRates;
import net.swofty.commons.rates.RateCategory;
import net.swofty.type.skyblockgeneric.bestiary.BestiaryData;
import net.swofty.type.skyblockgeneric.entity.mob.BestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import net.swofty.type.skyblockgeneric.enchantment.impl.EnchantmentExperience;
import net.swofty.type.skyblockgeneric.event.custom.PlayerKilledSkyBlockMobEvent;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class VanillaExperienceHandler implements HypixelEventClass {
    BestiaryData bestiaryData = new BestiaryData();

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerKilledSkyBlockMobEvent event) {

        SkyBlockPlayer player = event.getPlayer();
        SkyBlockMob mob = event.getKilledMob();

        if (mob == null) return;

        long xpOrbsDropped;

        // Base XP Orbs (additive)
        long baseXpOrbs = mob.getOtherLoot().getXpOrbAmount();

        // Bestiary Boost (multiplicative)
        float bestiaryBoost = 1;
        if (mob instanceof BestiaryMob bestiaryMob) {
            int tier = bestiaryData.getCurrentBestiaryTier(bestiaryMob, player.getBestiaryData().getAmount(bestiaryMob));
            bestiaryBoost += (float) bestiaryData.getTotalExtraXPPercentage(tier) / 100;
        }

        // Experience enchantment (multiplicative)
        float experienceEnchBoost = 1;
        SkyBlockItem mainHandItem = PlayerItemOrigin.getFromCache(player.getUuid()).get(PlayerItemOrigin.MAIN_HAND);
        SkyBlockEnchantment experienceEnchantment = mainHandItem.getAttributeHandler().getEnchantment(EnchantmentType.EXPERIENCE);
        if (experienceEnchantment != null) {
            if (EnchantmentExperience.shouldDoubleExperience(experienceEnchantment.level())) {
                experienceEnchBoost *= 2;
            }
        }

        xpOrbsDropped = Math.round(baseXpOrbs * bestiaryBoost * experienceEnchBoost * GameRates.multiplier(RateCategory.VANILLA));

        player.addExperience(xpOrbsDropped);
    }
}
