package net.swofty.type.skyblockgeneric.item.handlers.bow;

import net.minestom.server.property.ServerProperties;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.swofty.type.skyblockgeneric.entity.ArrowEntityImpl;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.HashMap;
import java.util.Map;

public class BowRegistry {
    private static final Map<String, BowHandler> REGISTERED_HANDLERS = new HashMap<>();

    static {
        register("STANDARD_BOW_HANDLER", (player, item, power) -> {
            SkyBlockItem arrow = player.getAndConsumeArrow();
            if (arrow == null) {
                return;
            }

            ArrowEntityImpl arrowEntity = new ArrowEntityImpl(player, arrow);
            Vec arrowVelocity = calculateArrowVelocity(
                    player.getPosition().pitch(),
                    player.getPosition().yaw(),
                    power
            );

            // Add player's velocity to arrow (like vanilla)
            Vec playerVel = player.getVelocity();
            arrowVelocity = arrowVelocity.add(
                    playerVel.x(),
                    player.isOnGround() ? 0.0 : playerVel.y(),
                    playerVel.z()
            );

            arrowEntity.setVelocity(arrowVelocity);
            arrowEntity.setInstance(player.getInstance(), calculateArrowSpawnPosition(player));
        });
    }

    public static void register(String id, BowHandler handler) {
        REGISTERED_HANDLERS.put(id, handler);
    }

    public static BowHandler getHandler(String id) {
        return REGISTERED_HANDLERS.get(id);
    }

    /**
     * Calculates arrow velocity based on player view angles and bow power
     * @param playerPitch Player's pitch (up/down angle)
     * @param playerYaw Player's yaw (left/right angle)
     * @param power Bow power from 0.0 to 1.0
     * @return Velocity vector for the arrow
     */
    public static Vec calculateArrowVelocity(float playerPitch, float playerYaw, double power) {
        // Arrow speed scales with power (vanilla uses power * 3)
        double arrowSpeed = 3.0 * power;

        // Convert player's pitch and yaw to radians
        double pitchRadians = Math.toRadians(playerPitch);
        double yawRadians = Math.toRadians(playerYaw);

        // Calculate the arrow's velocity components
        double velocityX = -Math.sin(yawRadians) * Math.cos(pitchRadians) * arrowSpeed;
        double velocityY = -Math.sin(pitchRadians) * arrowSpeed;
        double velocityZ = Math.cos(yawRadians) * Math.cos(pitchRadians) * arrowSpeed;

        // Multiply by ticks per second to convert to Minestom velocity
        return new Vec(velocityX, velocityY, velocityZ).mul(ServerProperties.SERVER_TICKS_PER_SECOND.get());
    }

    private static Pos calculateArrowSpawnPosition(SkyBlockPlayer player) {
        // Get the player's eye position (slightly lower like vanilla)
        return player.getPosition().add(0, player.getEyeHeight() - 0.1, 0);
    }
}