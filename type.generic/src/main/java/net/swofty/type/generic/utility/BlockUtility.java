package net.swofty.type.generic.utility;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.block.Block;
import org.json.JSONObject;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BlockUtility {


    public static Block applyTexture(Block block, String texture) {
        JSONObject json = new JSONObject();
        json.put("isPublic", true);
        json.put("signatureRequired", false);
        json.put("textures", new JSONObject().put("SKIN",
                new JSONObject().put("url", "http://textures.minecraft.net/texture/" + texture).put("metadata", new JSONObject().put("model", "slim"))));

        /*

        String texturesEncoded = Base64.getEncoder().encodeToString(json.toString().getBytes());
        block = block.withTag(ExtraItemTags.SKULL_OWNER, new ExtraItemTags.SkullOwner(null, "25",
                new PlayerSkin(texturesEncoded, null)));*/
        return block;
    }

    /**
     * Returns an ASCII arrow showing the direction relative to where the player is currently looking.
     * The arrow shows which way to turn/move from the current view direction to reach the target.
     *
     * @param from the starting position (with current yaw/pitch)
     * @param to   the target position
     * @return ASCII arrow character showing relative direction from current view
     */
    public static String getArrow(Pos from, Pos to) {
        if (from.samePoint(to)) {
            return "•";
        }

        // Get the yaw needed to look toward the target
        Pos lookingAt = from.withLookAt(to);
        float targetYaw = lookingAt.yaw();

        float currentYaw = from.yaw();
        float yawDifference = targetYaw - currentYaw;

        // Normalize the difference to -180 to 180 range
        while (yawDifference > 180) yawDifference -= 360;
        while (yawDifference < -180) yawDifference += 360;

        // Convert to 0-360 range for easier mapping
        float relativeAngle = yawDifference < 0 ? yawDifference + 360 : yawDifference;

        // Map to 8 directions based on relative angle
        // Add 22.5° offset so ranges are centered on cardinal directions
        relativeAngle += 22.5f;
        if (relativeAngle >= 360) {
            relativeAngle -= 360;
        }

        int direction = (int) (relativeAngle / 45.0f);

        String color = from.distance(to) > 20 ? "<e><l>" : "<a><l>";
        return switch (direction) {
            case 0 -> color + "⬆";  // Straight ahead
            case 1 -> color + "⬈";  // Forward-right
            case 2 -> color + "➡";  // Right
            case 3 -> color + "⬊";  // Back-right
            case 4 -> color + "⬇";  // Behind
            case 5 -> color + "⬋";  // Back-left
            case 6 -> color + "⬅";  // Left
            case 7 -> color + "⬉";  // Forward-left
            default -> throw new IllegalArgumentException("Invalid direction: " + direction);
        };
    }
}
