package net.swofty.pvp.player;

import net.minestom.server.property.ServerProperties;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.attribute.AttributeInstance;
import net.minestom.server.event.Event;
import net.minestom.server.event.EventNode;
import net.minestom.server.event.player.PlayerMoveEvent;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.potion.TimedPotion;

import java.util.function.Function;

public interface CombatPlayer {
    // Minestom methods
    TimedPotion getEffect(PotionEffect effect);
    AttributeInstance getAttribute(Attribute attribute);
    boolean isSprinting();
    Pos getPosition();
    
    /**
     * Does not guarantee anything, the implementation uses Minestom physics logic which does not take into account many edge cases.
     * It is also quite performance intensive, not suitable for calling often.
     * @param ticks the amount of ticks to test for
     * @return true if the player will likely be on the ground in the given amount of ticks
     */
    boolean isOnGroundAfterTicks(int ticks);
    
    default double getJumpVelocity() {
        return getAttribute(Attribute.JUMP_STRENGTH).getValue();
    }
    
    default double getJumpBoostVelocityModifier() {
        TimedPotion effect = getEffect(PotionEffect.JUMP_BOOST);
        return effect != null ?
                (0.1 * (effect.potion().amplifier() + 1)) : 0.0;
    }
    
    default void jump() {
        int tps = ServerProperties.SERVER_TICKS_PER_SECOND.get();
        double yVel = getJumpVelocity() + getJumpBoostVelocityModifier();
        setVelocityNoUpdate(velocity -> velocity.withY(Math.max(velocity.y(), yVel * tps)));
        if (isSprinting()) {
            double angle = getPosition().yaw() * (Math.PI / 180);
            setVelocityNoUpdate(velocity -> velocity.add(-Math.sin(angle) * 0.2 * tps, 0, Math.cos(angle) * 0.2 * tps));
        }
    }
    
    default void afterSprintAttack() {
        setVelocityNoUpdate(velocity -> velocity.mul(0.6, 1, 0.6));
    }
    
    void setVelocityNoUpdate(Function<Vec, Vec> function);
    
    void sendImmediateVelocityUpdate();
    
    static void init(EventNode<Event> node) {
        node.addListener(PlayerMoveEvent.class, event -> {
            Player player = event.getPlayer();
            if (player.isOnGround() && !event.isOnGround()
                    && event.getNewPosition().y() > player.getPosition().y()
                    && player instanceof CombatPlayer combatPlayer) {
                combatPlayer.jump();
            }
        });
    }
}
