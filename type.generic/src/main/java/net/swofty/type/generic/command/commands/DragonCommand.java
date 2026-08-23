package net.swofty.type.generic.command.commands;

import net.minestom.server.command.builder.arguments.ArgumentString;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.number.ArgumentDouble;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.entity.DragonEntity;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@CommandParameters(labels = "dragon", description = "Dragon management commands",
        usage = "/dragon <spawn|follow|stop|remove>",
        permission = Rank.STAFF,
        allowsConsole = false)
public class DragonCommand extends HypixelCommand {
    private static final Map<UUID, DragonEntity> playerDragons = new ConcurrentHashMap<>();

    @Override
    public void registerUsage(MinestomCommand command) {
        ArgumentString subcommand = ArgumentType.String("subcommand");
        ArgumentDouble speedArg = ArgumentType.Double("speed");

        command.addSyntax((sender, context) -> {
            showHelp((HypixelPlayer) sender);
        });

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            HypixelPlayer player = (HypixelPlayer) sender;
            String sub = context.get(subcommand);

            switch (sub.toLowerCase()) {
                case "spawn" -> spawnDragon(player, 0.8);
                case "follow" -> followPlayer(player, 0.8);
                case "idle" -> setIdle(player, 30, 0.6);
                case "stop" -> stopFollowing(player);
                case "remove" -> removeDragon(player);
                default -> showHelp(player);
            }
        }, subcommand);

        command.addSyntax((sender, context) -> {
            if (!permissionCheck(sender)) return;
            HypixelPlayer player = (HypixelPlayer) sender;
            String sub = context.get(subcommand);
            double value = context.get(speedArg);

            switch (sub.toLowerCase()) {
                case "spawn" -> spawnDragon(player, value);
                case "follow" -> followPlayer(player, value);
                case "idle" -> setIdle(player, value, 0.6);
                default -> showHelp(player);
            }
        }, subcommand, speedArg);
    }

    private void spawnDragon(HypixelPlayer player, double speed) {
        DragonEntity existing = playerDragons.get(player.getUuid());
        if (existing != null && !existing.isDead()) {
            player.sendMessage("<c>You already have a dragon spawned! Use /dragon remove first.");
            return;
        }

        DragonEntity dragon = new DragonEntity();
        dragon.setInstance(player.getInstance(), player.getPosition().add(0, 5, 0));
        dragon.addViewer(player);
        playerDragons.put(player.getUuid(), dragon);

        player.sendMessage("<a>Dragon spawned!");
    }

    private void followPlayer(HypixelPlayer player, double speed) {
        DragonEntity dragon = playerDragons.get(player.getUuid());
        if (dragon == null || dragon.isDead()) {
            player.sendMessage("<c>You don't have a dragon! Use /dragon spawn first.");
            return;
        }

        dragon.followPlayer(player, speed);
        player.sendMessage("<a>Dragon is now following you at speed {}!", speed);
    }

    private void setIdle(HypixelPlayer player, double distance, double speed) {
        DragonEntity dragon = playerDragons.get(player.getUuid());
        if (dragon == null || dragon.isDead()) {
            player.sendMessage("<c>You don't have a dragon! Use /dragon spawn first.");
            return;
        }

        dragon.setIdle(player.getPosition().add(0, 10, 0), distance, speed);
        player.sendMessage("<a>Dragon is now idling around your position with distance {}!", distance);
    }

    private void stopFollowing(HypixelPlayer player) {
        DragonEntity dragon = playerDragons.get(player.getUuid());
        if (dragon == null || dragon.isDead()) {
            player.sendMessage("<c>You don't have a dragon!");
            return;
        }

        dragon.clearTarget();
        player.sendMessage("<a>Dragon stopped.");
    }

    private void removeDragon(HypixelPlayer player) {
        DragonEntity dragon = playerDragons.remove(player.getUuid());
        if (dragon == null) {
            player.sendMessage("<c>You don't have a dragon!");
            return;
        }

        if (!dragon.isDead()) {
            dragon.remove();
        }
        player.sendMessage("<a>Dragon removed!");
    }

    private void showHelp(HypixelPlayer player) {
        player.sendMessage("<sep>");
        player.sendMessage("<6>Dragon Commands");
        player.sendMessage("<e>/dragon spawn [speed] <8>- <7>Spawn a dragon at your location");
        player.sendMessage("<e>/dragon follow [speed] <8>- <7>Make the dragon follow you");
        player.sendMessage("<e>/dragon idle [distance] <8>- <7>Make the dragon idle around your position");
        player.sendMessage("<e>/dragon stop <8>- <7>Stop the dragon");
        player.sendMessage("<e>/dragon remove <8>- <7>Remove your dragon");
        player.sendMessage("<sep>");
    }

    public static void cleanupDragon(UUID playerUuid) {
        DragonEntity dragon = playerDragons.remove(playerUuid);
        if (dragon != null && !dragon.isDead()) {
            dragon.remove();
        }
    }
}
