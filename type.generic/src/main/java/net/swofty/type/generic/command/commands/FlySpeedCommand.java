package net.swofty.type.generic.command.commands;

import lombok.Getter;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentNumber;
import net.minestom.server.entity.attribute.Attribute;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;

@CommandParameters(labels = "speed flyspeed",
        description = "Sets a players speed",
        usage = "/speed <speed>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class FlySpeedCommand extends HypixelCommand {
    private static final float MIN_SPEED = 0.0f;
    private static final float MAX_SPEED = 10.0f;

    public enum SpeedType {
        FLY("flying"),
        WALK("walking");

        @Getter
        private final String displayName;

        SpeedType(String displayName) {
            this.displayName = displayName;
        }
    }

    /**
     * Calculates the real movement speed from user input
     *
     * @param userSpeed The user input speed (0-10)
     * @param isFly     Whether this is for flying speed
     * @return The calculated movement speed value
     */
    private float getRealMoveSpeed(final float userSpeed, final boolean isFly) {
        final float defaultSpeed = isFly ? 0.1f : 0.2f;
        float maxSpeed = 5f;
        if (userSpeed < 1f) {
            return defaultSpeed * userSpeed;
        } else {
            final float ratio = ((userSpeed - 1) / 9) * (maxSpeed - defaultSpeed);
            return ratio + defaultSpeed;
        }
    }


    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentNumber<Float> speedArg = ArgumentType.Float("speed")
                .min(0f)
                .max(10f);

        var typeArg = ArgumentType.Enum("type", SpeedType.class)
                .setDefaultValue(SpeedType.FLY);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            final float userSpeed = context.get(speedArg);
            final SpeedType speedType = context.get(typeArg);
            final boolean isFly = speedType == SpeedType.FLY;
            HypixelPlayer player = (HypixelPlayer) sender;
            final float realSpeed = getRealMoveSpeed(userSpeed, isFly);
            if (isFly) {
                player.setFlyingSpeed(realSpeed);
            } else {
                player.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(realSpeed);
            }
            player.sendMessage("<a>Set your {} speed to <e>{}<a>.", speedType.getDisplayName(), userSpeed);
        }, speedArg, typeArg);
    }
}
