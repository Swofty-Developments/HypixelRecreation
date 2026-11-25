package net.swofty.type.skyblockgeneric.event.actions.player.mobdamage;

import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.damage.EntityDamage;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.slayer.SlayerBossMob;
import net.swofty.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import net.swofty.type.skyblockgeneric.event.value.events.PlayerDamagedByMobValueUpdateEvent;
import net.swofty.type.skyblockgeneric.hunting.AttributeEffectService;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.statistics.PlayerStatistics;
import net.swofty.type.skyblockgeneric.utility.DamageIndicator;

import java.util.Map;

public class PlayerActionDamagedAttacked implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.ENTITY, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(EntityAttackEvent event) {
        if (!event.getTarget().getEntityType().equals(EntityType.PLAYER)) return;

        SkyBlockPlayer player = (SkyBlockPlayer) event.getTarget();

        if (event.getEntity() instanceof SkyBlockMob mob) {
            damagePlayer(mob, player, true);
        }
    }

    public static void damagePlayer(SkyBlockMob mob, SkyBlockPlayer player, boolean melee) {
        if (mob.getLastAttack() + mob.damageCooldown() > System.currentTimeMillis()) return;
        mob.setLastAttack(System.currentTimeMillis());

        ItemStatistics mobStatistics = mob.getStatistics();
        ItemStatistics playerStatistics = player.getStatistics().allStatistics();

        Map.Entry<Double, Boolean> damageDealt =
                PlayerStatistics.runPrimaryDamageFormula(mobStatistics, playerStatistics);

        double baseDefense = playerStatistics.getOverall(ItemStatistic.DEFENSE);
        double resistance = AttributeEffectService.resistanceDefense(player.getHuntingData(), mob);
        double resistanceMultiplier = (100D + Math.max(0, baseDefense))
                / (100D + Math.max(0, baseDefense + resistance));
        PlayerDamagedByMobValueUpdateEvent valueEvent = new PlayerDamagedByMobValueUpdateEvent(
                player, (float) (damageDealt.getKey() * resistanceMultiplier), mob);
        SkyBlockValueEvent.callValueUpdateEvent(valueEvent);

        SkyBlockItem pet = player.getPetData().getEnabledPet();
        PetEvent.DamagedByMob damageEvent = player.getPetData()
                .dispatch(new PetEvent.DamagedByMob(player, pet, mob, (float) valueEvent.getValue()));
        float finalDamage = (float) damageEvent.damage();

        player.damage(new EntityDamage(mob, finalDamage));

        if (melee && mob instanceof SlayerBossMob slayerBoss) {
            slayerBoss.getAbility().onMeleeHit(slayerBoss, player);
        }

        new DamageIndicator()
                .damage(finalDamage)
                .pos(player.getPosition())
                .critical(damageDealt.getValue())
                .display(player.getInstance());
    }
}
