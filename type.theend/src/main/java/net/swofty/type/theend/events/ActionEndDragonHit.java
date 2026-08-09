package net.swofty.type.theend.events;

import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.custom.DragonHitEvent;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.entity.ArrowEntityImpl;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.theend.dragon.EndDragonManager;
import net.swofty.type.theend.dragon.EnderDragonEntity;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ActionEndDragonHit implements HypixelEventClass {
    private static final Map<UUID, Long> MELEE_COOLDOWN = new ConcurrentHashMap<>();

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(DragonHitEvent event) {
        if (!(event.getPlayer() instanceof SkyBlockPlayer player)) return;
        if (!(event.getDragon() instanceof EnderDragonEntity dragon)) return;
        if (!EndDragonManager.isCurrent(dragon)) return;
        EndDragonManager.recordDamage(player, event.getDamage());
    }

    @PhasedEvent(node = EventNodes.ALL, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(EntityAttackEvent event) {
        if (!(event.getEntity() instanceof SkyBlockPlayer player)) return;
        if (!(event.getTarget() instanceof EnderDragonEntity dragon) || !EndDragonManager.isCurrent(dragon)) return;
        long now = System.currentTimeMillis();
        if (now < MELEE_COOLDOWN.getOrDefault(player.getUuid(), 0L)) return;
        MELEE_COOLDOWN.put(player.getUuid(), now + 250);
        float damage = player.getStatistics()
                .runPrimaryDamageFormula(ItemStatistics.builder().build(), player, dragon).getKey().floatValue();
        damageDragon(player, dragon, DamageType.PLAYER_ATTACK, player, damage);
    }

    @PhasedEvent(node = EventNodes.ALL, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(ProjectileCollideWithEntityEvent event) {
        if (!(event.getEntity() instanceof ArrowEntityImpl arrow)) return;
        if (!(arrow.getShooter() instanceof SkyBlockPlayer player)) return;
        if (!(event.getTarget() instanceof EnderDragonEntity dragon) || !EndDragonManager.isCurrent(dragon)) return;
        ItemStatistics statistics = ItemStatistics.add(player.getStatistics().allStatistics(),
                arrow.getArrowItem().getAttributeHandler().getStatistics());
        float damage = player.getStatistics()
                .runPrimaryDamageFormula(statistics, ItemStatistics.builder().build()).getKey().floatValue();
        damageDragon(player, dragon, DamageType.ARROW, arrow, damage);
    }

    private void damageDragon(SkyBlockPlayer player, EnderDragonEntity dragon,
                              net.minestom.server.registry.RegistryKey<net.minestom.server.entity.damage.DamageType> type,
                              net.minestom.server.entity.Entity source, float damage) {
        dragon.damage(new Damage(type, source, player, player.getPosition(), damage));
    }
}
