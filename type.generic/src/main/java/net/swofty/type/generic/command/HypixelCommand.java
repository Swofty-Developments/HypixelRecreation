package net.swofty.type.generic.command;

import lombok.Getter;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.Command;
import net.swofty.type.generic.data.HypixelDataHandler;
import net.swofty.type.generic.data.datapoints.DatapointRank;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

public abstract class HypixelCommand {
    @Getter
    private final CommandParameters params;
    @Getter
    private final String name;
    @Getter
    private final MinestomCommand command;

    protected HypixelCommand() {
        this.params = this.getClass().getAnnotation(CommandParameters.class);

        if (params == null) {
            throw new RuntimeException("Command " + this.getClass().getSimpleName() + " does not have a CommandParameters annotation!");
        }

        List<String> labels = new ArrayList<>();
        if (params.labels() != null && !params.labels().trim().isEmpty()) {
            labels.addAll(Arrays.asList(params.labels().split(" ")));
        }

        this.name = labels.getFirst();

        if (this.name == null || this.name.isBlank()) {
            throw new RuntimeException("Command " + this.getClass().getSimpleName() + " does not have a name!");
        }

        // remove the command name, leaving only the aliases
        labels.removeFirst();

        if (labels.isEmpty()) {
            this.command = new MinestomCommand(this);
        } else {
            this.command = new MinestomCommand(this, labels.toArray(new String[0]));
        }
    }

    public abstract void registerUsage(MinestomCommand command);

    public boolean permissionCheck(CommandSender sender) {
        HypixelPlayer player = (HypixelPlayer) sender;
        Optional<HypixelDataHandler> dataHandler = player.getOptionalDataHandler();
        if (dataHandler.isEmpty()) {
            player.sendMessage("§cYour player data is unavailable right now. Please reconnect.");
            return false;
        }
        boolean passes = dataHandler.get().get(HypixelDataHandler.Data.RANK, DatapointRank.class).getValue().isEqualOrHigherThan(params.permission());

        if (!passes) {
            player.sendMessage("§cYou do not have permission to use this command.");
        }

        return passes;
    }

    public static class MinestomCommand extends Command {

        public MinestomCommand(HypixelCommand command) {
            super(command.getName());

            setDefaultExecutor((sender, _) -> sender.sendMessage("§cUsage: " + command.getParams().usage()));

            setCondition((commandSender, _) -> {
                if (commandSender instanceof ConsoleSender) {
                    return command.getParams().allowsConsole();
                }

                HypixelPlayer player = (HypixelPlayer) commandSender;
                Optional<HypixelDataHandler> dataHandler = player.getOptionalDataHandler();
                return dataHandler.map(hypixelDataHandler -> hypixelDataHandler.get(HypixelDataHandler.Data.RANK, DatapointRank.class).getValue().isEqualOrHigherThan(command.getParams().permission())).orElse(false);

            });

            command.registerUsage(this);
        }

        public MinestomCommand(HypixelCommand command, String... aliases) {
            super(command.getName(), aliases);

            setDefaultExecutor((sender, _) -> {
                sender.sendMessage("§cUsage: " + command.getParams().usage());
            });

            setCondition((commandSender, _) -> {
                if (commandSender instanceof ConsoleSender) {
                    return command.getParams().allowsConsole();
                }

                HypixelPlayer player = (HypixelPlayer) commandSender;
                Optional<HypixelDataHandler> dataHandler = player.getOptionalDataHandler();
                return dataHandler.map(hypixelDataHandler -> hypixelDataHandler.get(HypixelDataHandler.Data.RANK, DatapointRank.class).getValue().isEqualOrHigherThan(command.getParams().permission())).orElse(false);

            });

            command.registerUsage(this);
        }
    }
}
