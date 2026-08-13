package net.swofty.type.skyblockgeneric.enchantment;

import lombok.Getter;
import lombok.SneakyThrows;
import net.swofty.commons.StringUtility;
import net.swofty.type.skyblockgeneric.enchantment.abstr.Ench;
import net.swofty.type.skyblockgeneric.enchantment.abstr.EnchFromTable;
import net.swofty.type.skyblockgeneric.enchantment.impl.*;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

@Getter
public enum EnchantmentType {
    SHARPNESS(EnchantmentSharpness.class),
    EFFICIENCY(EnchantmentEfficiency.class),
    SCAVENGER(EnchantmentScavenger.class),
    PROTECTION(EnchantmentProtection.class),
    CRITICAL(EnchantmentCritical.class),
    FIRST_STRIKE(EnchantmentFirstStrike.class),
    GROWTH(EnchantmentGrowth.class),
    LUCK(EnchantmentLuck.class),
    LOOTING(EnchantmentLooting.class),
    ENDER_SLAYER(EnchantmentEnderSlayer.class),
    SMITE(EnchantmentSmite.class),
    SILK_TOUCH(EnchantmentSilkTouch.class),
    SMELTING_TOUCH(EnchantmentSmeltingTouch.class),
    GIANT_KILLER(EnchantmentGiantKiller.class),
    EXECUTE(EnchantmentExecute.class),
    IMPALING(EnchantmentImpaling.class),
    CASTER(EnchantmentCaster.class),
    CHARM(EnchantmentCharm.class),
    ANGLER(EnchantmentAngler.class),
    BLESSING(EnchantmentBlessing.class),
    CORRUPTION(EnchantmentCorruption.class),
    EXPERTISE(EnchantmentExpertise.class),
    FLASH(EnchantmentFlash.class),
    FRAIL(EnchantmentFrail.class),
    LUCK_OF_THE_SEA(EnchantmentLuckOfTheSea.class),
    LURE(EnchantmentLure.class),
    MAGNET(EnchantmentMagnet.class),
    PISCARY(EnchantmentPiscary.class),
    QUICK_BITE(EnchantmentQuickBite.class),
    SPIKED_HOOK(EnchantmentSpikedHook.class),
    TABASCO(EnchantmentTabasco.class),
    BANE_OF_ARTHROPODS(EnchantmentBaneOfArthropods.class),
    CUBISM(EnchantmentCubism.class),
    GRAVITY(EnchantmentGravity.class),
    SMOLDERING(EnchantmentSmoldering.class),
    WOODSPLITTER(EnchantmentWoodsplitter.class),
    FORTUNE(EnchantmentFortune.class),
    CLEAVE(EnchantmentCleave.class),
    EXPERIENCE(EnchantmentExperience.class),
    FIRE_ASPECT(EnchantmentFireAspect.class),
    KNOCKBACK(EnchantmentKnockback.class),
    LETHALITY(EnchantmentLethality.class),
    LIFE_STEAL(EnchantmentLifeSteal.class),
    DRAIN(EnchantmentDrain.class),
    MANA_STEAL(EnchantmentManaSteal.class),
    PROSECUTE(EnchantmentProsecute.class),
    THUNDERBOLT(EnchantmentThunderbolt.class),
    THUNDERLORD(EnchantmentThunderlord.class),
    VAMPIRISM(EnchantmentVampirism.class),
    VENOMOUS(EnchantmentVenomous.class),
    VICIOUS(EnchantmentVicious.class),
    POWER(EnchantmentPower.class),
    FIRE_PROTECTION(EnchantmentFireProtection.class),
    TITAN_KILLER(EnchantmentTitanKiller.class),
    CHANCE(EnchantmentChance.class),
    PESTERMINATOR(EnchantmentPesterminator.class),
    SCUBA(EnchantmentScuba.class),
    BLAST_PROTECTION(EnchantmentBlastProtection.class),
    PROJECTILE_PROTECTION(EnchantmentProjectileProtection.class),
    RESPITE(EnchantmentRespite.class),
    SNIPE(EnchantmentSnipe.class),
    TRIPLE_STRIKE(EnchantmentTripleStrike.class),
    ;

    private final Class<? extends Ench> clazz;
    private final Ench ench;

    @SneakyThrows
    EnchantmentType(Class<? extends Ench> ench) {
        this.clazz = ench;

        this.ench = ench.getConstructor().newInstance();
    }

    public int getApplyCost(int level, SkyBlockPlayer player) {
        if (level < 1 || level > ench.getLevelsToApply(player).maximumLevel())
            throw new IllegalArgumentException("level cannot be less than 1 and more than " +
                    ench.getLevelsToApply(player).maximumLevel() + " for " + name());
        return ench.getLevelsToApply(player).get(level);
    }

    public String getDescription(int level, SkyBlockPlayer player) {
        if (level < 1 || level > ench.getLevelsToApply(player).maximumLevel())
            return ("level cannot be less than 1 and more than " +
                    ench.getLevelsToApply(player).maximumLevel() + " for " + name());
        return ench.getDescription(level);
    }

    public @Nullable EnchFromTable getEnchFromTable() {
        if (ench instanceof EnchFromTable)
            return (EnchFromTable) ench;
        return null;
    }

    public String getName() {
        return StringUtility.toNormalCase(this.name());
    }
}
