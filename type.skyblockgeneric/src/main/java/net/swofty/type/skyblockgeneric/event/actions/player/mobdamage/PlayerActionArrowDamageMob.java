package net.swofty.type.skyblockgeneric.event.actions.player.mobdamage;

import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.entity.ArrowEntityImpl;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.hunting.AttributeEffectService;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemOrigin;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.statistics.PlayerStatistics;
import net.swofty.type.skyblockgeneric.utility.AttackService;

import java.util.Map;

public class PlayerActionArrowDamageMob implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.ALL, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(ProjectileCollideWithEntityEvent event) {
        ArrowEntityImpl arrow;
        if (event.getEntity() instanceof ArrowEntityImpl arrowEntity)
            arrow = arrowEntity;
        else return;

        SkyBlockMob collidedWith;
        if (event.getTarget() instanceof SkyBlockMob mob)
            collidedWith = mob;
        else return;

        SkyBlockPlayer shooter;
        if (arrow.getShooter() instanceof SkyBlockPlayer player)
            shooter = player;
        else return;

        SkyBlockItem arrowItem = arrow.getArrowItem();
        ItemStatistics entityStats = mob.getStatistics();
        ItemStatistics playerStats = shooter.getStatistics().allStatistics(shooter, collidedWith);

        // Add the arrow's statistics to the player's statistics
        playerStats = ItemStatistics.add(playerStats, arrowItem.getAttributeHandler().getStatistics());

        Map.Entry<Double, Boolean> hit = PlayerStatistics.runPrimaryDamageFormula(playerStats, entityStats);
        double damage = hit.getKey() * AttributeEffectService.outgoingDamageMultiplier(shooter.getHuntingData(), collidedWith, true);
        boolean critical = hit.getValue();

        float damageAmount = (float) damage;
        if (!AttackService.applyHit(shooter, collidedWith, damageAmount, critical)) return;

        double ferocity = playerStats.getOverall(ItemStatistic.FEROCITY);
        AttackService.scheduleExtraHits(shooter, collidedWith, damageAmount, critical, ferocity);

        SkyBlockItem mainHandItem = PlayerItemOrigin.getFromCache(shooter.getUuid()).get(PlayerItemOrigin.MAIN_HAND);
        SkyBlockItem pet = shooter.getPetData().getEnabledPet();
        shooter.getPetData().dispatch(new PetEvent.RangedDamageDealt(shooter, pet, mob, mainHandItem, damageAmount));
    }
}
