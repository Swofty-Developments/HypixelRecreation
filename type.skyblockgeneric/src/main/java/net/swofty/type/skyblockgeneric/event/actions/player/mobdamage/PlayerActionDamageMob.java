package net.swofty.type.skyblockgeneric.event.actions.player.mobdamage;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.generic.utility.MathUtility;
import net.swofty.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import net.swofty.type.skyblockgeneric.enchantment.abstr.DamageEventEnchant;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.event.value.SkyBlockValueEvent;
import net.swofty.type.skyblockgeneric.event.value.events.PlayerDamageMobValueUpdateEvent;
import net.swofty.type.skyblockgeneric.hunting.AttributeEffectService;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.components.ItemRequirementsComponent;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.AttackService;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerActionDamageMob implements HypixelEventClass {
    private static final Map<UUID, Long> COOLDOWN = new HashMap<>();

    @PhasedEvent(node = EventNodes.ALL, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(EntityAttackEvent event) {
        if (event.getTarget().getEntityType().equals(EntityType.PLAYER)) return;
        if (!event.getEntity().getEntityType().equals(EntityType.PLAYER)) return;
        SkyBlockPlayer player = (SkyBlockPlayer) event.getEntity();
        SkyBlockItem heldItem = new SkyBlockItem(player.getItemInMainHand());
        if (heldItem.hasComponent(ItemRequirementsComponent.class)
                && !heldItem.getComponent(ItemRequirementsComponent.class).ensureCanUse(player)) return;

        Entity targetEntity = event.getTarget();
        SkyBlockMob mob;
        if (event.getTarget() instanceof SkyBlockMob skyBlockMob)
            mob = skyBlockMob;
        else return;
        LivingEntity targetLivingEntity = (LivingEntity) targetEntity;

        ItemStatistics entityStats = mob.getStatistics();
        Map.Entry<Double, Boolean> hit = player.getStatistics().runPrimaryDamageFormula(entityStats, player, targetLivingEntity);

        double damage = hit.getKey() * AttributeEffectService.outgoingDamageMultiplier(player.getHuntingData(), mob, false);
        boolean critical = hit.getValue();

        PlayerDamageMobValueUpdateEvent valueEvent = new PlayerDamageMobValueUpdateEvent(
                (SkyBlockPlayer) event.getEntity(), (float) damage, mob);
        SkyBlockValueEvent.callValueUpdateEvent(valueEvent);

        if (COOLDOWN.containsKey(player.getUuid()) && System.currentTimeMillis() < COOLDOWN.get(player.getUuid()))
                return;
        COOLDOWN.put(player.getUuid(), System.currentTimeMillis() +
                MathUtility.ticksToMilliseconds(player.getStatistics().getInvulnerabilityTime()));

        float damageAmount = ((Number) valueEvent.getValue()).floatValue();
        if (!AttackService.applyHit(player, mob, damageAmount, critical)) return;

        player.getPetData().dispatch(new PetEvent.DamageMob(player, player.getPetData().getEnabledPet(), mob));

        double ferocity = player.getStatistics().allStatistics().getOverall(ItemStatistic.FEROCITY);
        if (player.getPosition().distance(mob.getPosition()) <= AttackService.MAX_MELEE_FEROCITY_DISTANCE)
            AttackService.scheduleExtraHits(player, mob, damageAmount, critical, ferocity);

        // Handle damage event enchantments
        SkyBlockItem mainHandItem = PlayerItemOrigin.getFromCache(player.getUuid()).get(PlayerItemOrigin.MAIN_HAND);
        double damageValue = damageAmount;

        for (SkyBlockEnchantment enchantment : mainHandItem.getAttributeHandler().getEnchantments().toList()) {
            if (enchantment.type().getEnch() instanceof DamageEventEnchant damageEventEnchant) {
                damageEventEnchant.onDamageDealt(player, targetLivingEntity, damageValue, enchantment.level());
            }
        }
    }
}
