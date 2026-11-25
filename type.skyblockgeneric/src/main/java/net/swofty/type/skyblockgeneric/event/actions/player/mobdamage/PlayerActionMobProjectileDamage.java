package net.swofty.type.skyblockgeneric.event.actions.player.mobdamage;

import net.minestom.server.entity.EntityProjectile;
import net.minestom.server.event.entity.projectile.ProjectileCollideWithEntityEvent;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class PlayerActionMobProjectileDamage implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.ENTITY, requireDataLoaded = false, phase = EventPhase.GAMEPLAY)
    public void run(ProjectileCollideWithEntityEvent event) {
        if (!(event.getEntity() instanceof EntityProjectile projectile)
                || !(projectile.getShooter() instanceof SkyBlockMob mob)
                || !(event.getTarget() instanceof SkyBlockPlayer player)) {
            return;
        }

        PlayerActionDamagedAttacked.damagePlayer(mob, player, false);
    }
}
