package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.horse;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.HORSE, minimumRarity = Rarity.LEGENDARY)
public final class TrampleAbility implements PetAbility {
    private static final double BASE = 0.5;
    private static final double PER_LEVEL = 0.045;
    private static final double MIN_FALL_HEIGHT = 20;
    private static final double AOE_RADIUS = 3;

    @Override
    public String getName() {
        return "Trample";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String percent = decimalify(BASE + PER_LEVEL * level, 2);

        return List.of(
                "<7>After falling <a>20 <7>or more blocks, absorb",
                "<7>your fall damage and deal <a>" + percent + "% <7>of",
                "<7>your weapon's <c>Damage <7>for every block",
                "<7>fallen to mobs within <a>3 <7>blocks."
        );
    }

    @PetEventHandler
    public void onFallDamage(PetEvent.FallDamage event) {
        if (event.fallHeight() < MIN_FALL_HEIGHT) return;

        Rarity rarity = event.pet().getAttributeHandler().getRarity();
        int level = event.pet().getAttributeHandler().getPetData().getAsLevel(rarity);
        double percent = BASE + PER_LEVEL * level;

        double weaponDamage = new SkyBlockItem(event.player().getItemInMainHand())
                .getAttributeHandler().getStatistics().getBase(ItemStatistic.DAMAGE) - 5D;
        double damagePerBlock = weaponDamage * percent / 100 * event.fallHeight();

        event.damage(0);

        for (Entity entity : event.player().getInstance().getNearbyEntities(event.player().getPosition(), AOE_RADIUS)) {
            if (entity == event.player()) continue;
            if (!(entity instanceof SkyBlockMob mob)) continue;
            if (entity.getEntityType() == EntityType.PLAYER || entity.getEntityType() == EntityType.VILLAGER) continue;

            mob.damage(new Damage(DamageType.PLAYER_ATTACK, event.player(), event.player(),
                    event.player().getPosition(), (float) damagePerBlock));
        }
    }
}
