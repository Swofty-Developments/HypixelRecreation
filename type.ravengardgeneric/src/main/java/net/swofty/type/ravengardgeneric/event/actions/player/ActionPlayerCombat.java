package net.swofty.type.ravengardgeneric.event.actions.player;

import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.item.ItemStack;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.ravengardgeneric.entity.mob.RavengardMob;
import net.swofty.type.ravengardgeneric.item.RavengardItemType;
import net.swofty.type.ravengardgeneric.item.attribute.RavengardItemAttributeHandler;
import net.swofty.type.ravengardgeneric.item.statistics.RavengardItemStatistic;
import net.swofty.type.ravengardgeneric.user.RavengardPlayer;

public class ActionPlayerCombat implements HypixelEventClass {
    private static final double DISPLAY_DAMAGE_SCALE = 4.0;
    private static final float FIST_DAMAGE = 1f;

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void onAttack(EntityAttackEvent event) {
        if (!(event.getEntity() instanceof RavengardPlayer player)
                || !(event.getTarget() instanceof RavengardMob mob)) {
            return;
        }

        ItemStack held = player.getItemInMainHand();
        RavengardItemAttributeHandler handler = new RavengardItemAttributeHandler(held);
        RavengardItemType type = handler.getType();
        if (type != null && !type.usableBy(player.getRavengardClass())) {
            player.sendMessage("<c>Your class cannot use this item!");
            return;
        }

        float damage = FIST_DAMAGE;
        if (type != null) {
            double weapon = type.statistic(RavengardItemStatistic.DAMAGE) * handler.getStatBoost();
            if (weapon > 0) {
                damage = (float) Math.max(FIST_DAMAGE, weapon / DISPLAY_DAMAGE_SCALE);
            }
        }
        mob.damage(DamageType.PLAYER_ATTACK, damage);
    }
}
