package net.swofty.type.generic.command;

import lombok.Getter;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.command.builder.ArgumentCallback;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.command.builder.CommandSyntax;
import net.minestom.server.command.builder.arguments.Argument;
import net.minestom.server.command.builder.condition.CommandCondition;
import net.minestom.server.command.builder.suggestion.Suggestion;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.HypixelDataHandler;
import net.swofty.type.generic.data.datapoints.DatapointRank;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

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

        if (labels.isEmpty()) {
            throw new RuntimeException("Command " + this.getClass().getSimpleName() + " does not have a name!");
        }

        this.name = labels.getFirst();

        if (this.name.isBlank()) {
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

    public static SuggestionEntry suggestion(String value, Text tooltip) {
        return new SuggestionEntry(value, tooltip.asComponent());
    }

    public static SuggestionEntry suggestion(String value, String tooltipMarkup, Object... arguments) {
        return suggestion(value, Text.of(tooltipMarkup, arguments));
    }

    public static void suggest(Suggestion target, String value, Text tooltip) {
        target.addEntry(suggestion(value, tooltip));
    }

    public static void suggest(Suggestion target, String value, String tooltipMarkup, Object... arguments) {
        target.addEntry(suggestion(value, tooltipMarkup, arguments));
    }

    public boolean permissionCheck(CommandSender sender) {
        return permissionCheck((HypixelPlayer) sender, params.permission());
    }

    public boolean permissionCheck(HypixelPlayer player, Rank rank) {
        HypixelDataHandler dataHandler = player.getDataHandler();
        boolean passes = dataHandler.get(HypixelDataHandler.Data.RANK, DatapointRank.class).getValue().isEqualOrHigherThan(rank);

        if (!passes) {
            player.sendMessage("<c>You do not have permission to use this command.");
        }

        return passes;
    }

    /**
     * Minestom {@link Command} that applies the markup contract to whichever sender an executor receives.
     *
     * Executors registered through this class are handed a {@link ConsoleMarkupSender} when the console runs
     * the command, so a bare {@code sender.sendMessage("<c>...")} renders for the console exactly as the
     * {@link HypixelPlayer} override renders it for players. Conditions are deliberately left unwrapped so the
     * {@code instanceof ConsoleSender} check keeps seeing the real console sender, and players are never
     * wrapped so that casts inside executors keep working.
     */
    public static class MinestomCommand extends Command {

        public MinestomCommand(HypixelCommand command) {
            super(command.getName());

            install(command);
        }

        public MinestomCommand(HypixelCommand command, String... aliases) {
            super(command.getName(), aliases);

            install(command);
        }

        @Override
        public void setDefaultExecutor(CommandExecutor executor) {
            super.setDefaultExecutor(markupAware(executor));
        }

        @Override
        public Collection<CommandSyntax> addSyntax(CommandExecutor executor, Argument<?>... arguments) {
            return super.addSyntax(markupAware(executor), arguments);
        }

        @Override
        public Collection<CommandSyntax> addSyntax(CommandExecutor executor, String format) {
            return super.addSyntax(markupAware(executor), format);
        }

        @Override
        public Collection<CommandSyntax> addConditionalSyntax(CommandCondition condition, CommandExecutor executor,
                                                              Argument<?>... arguments) {
            return super.addConditionalSyntax(condition, markupAware(executor), arguments);
        }

        @Override
        public void setArgumentCallback(ArgumentCallback callback, Argument<?> argument) {
            super.setArgumentCallback((sender, exception) ->
                    callback.apply(ConsoleMarkupSender.wrap(sender), exception), argument);
        }

        private void install(HypixelCommand command) {
            setDefaultExecutor((sender, _) ->
                    sender.sendMessage(Text.of("<c>Usage: {}", command.getParams().usage())));

            setCondition((commandSender, _) -> {
                if (commandSender instanceof ConsoleSender) {
                    return command.getParams().allowsConsole();
                }

                HypixelPlayer player = (HypixelPlayer) commandSender;
                HypixelDataHandler dataHandler = player.getDataHandler();

                return dataHandler.get(HypixelDataHandler.Data.RANK, DatapointRank.class).getValue()
                        .isEqualOrHigherThan(command.getParams().permission());
            });

            command.registerUsage(this);
        }

        private static CommandExecutor markupAware(CommandExecutor executor) {
            return (sender, context) -> executor.apply(ConsoleMarkupSender.wrap(sender), context);
        }
    }
}
