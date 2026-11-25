package net.swofty.type.generic.command.commands;

import net.minestom.server.command.CommandSender;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.swofty.commons.rates.GameRates;
import net.swofty.commons.rates.RateCategory;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.rates.RatesSync;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;

import java.util.Arrays;

@CommandParameters(labels = "rates droprates xprates",
        description = "View or change drop and XP rate multipliers on every server",
        usage = "/rates [category multiplier | reset [category]]",
        permission = Rank.STAFF,
        allowsConsole = true)
public class RatesCommand extends HypixelCommand {

    @Override
    public void registerUsage(MinestomCommand command) {
        String[] keys = Arrays.stream(RateCategory.values()).map(RateCategory::getKey).toArray(String[]::new);
        var categoryArg = ArgumentType.Word("category").from(keys);
        var valueArg = ArgumentType.Double("multiplier").min(GameRates.MIN_VALUE).max(GameRates.MAX_VALUE);
        var resetArg = ArgumentType.Literal("reset");

        command.setDefaultExecutor((sender, _) -> {
            if (lacksPermission(sender)) return;
            list(sender);
        });

        command.addSyntax((sender, context) -> {
            if (lacksPermission(sender)) return;
            RateCategory category = RateCategory.fromKey(context.get(categoryArg)).orElseThrow();
            double value = context.get(valueArg);
            GameRates.set(category, value);
            persist(sender, "<a>Set <e>{}<a> to <e>{}x<a>.", category.getKey(), format(GameRates.get(category)));
        }, categoryArg, valueArg);

        command.addSyntax((sender, _) -> {
            if (lacksPermission(sender)) return;
            GameRates.resetAll();
            persist(sender, "<a>Reset every rate to its default.");
        }, resetArg);

        command.addSyntax((sender, context) -> {
            if (lacksPermission(sender)) return;
            RateCategory category = RateCategory.fromKey(context.get(categoryArg)).orElseThrow();
            GameRates.reset(category);
            persist(sender, "<a>Reset <e>{}<a> to <e>{}x<a>.", category.getKey(), format(GameRates.get(category)));
        }, resetArg, categoryArg);
    }

    private boolean lacksPermission(CommandSender sender) {
        return sender instanceof HypixelPlayer && !permissionCheck(sender);
    }

    private void persist(CommandSender sender, String message, Object... arguments) {
        RatesSync.save().whenComplete((_, error) -> {
            if (error != null) {
                sender.sendMessage(Text.of("<c>Failed to save rates: {}", error.getMessage()));
                return;
            }
            sender.sendMessage(Text.of(message, arguments));
            sender.sendMessage(Text.of("<7>Applies to every server within a few seconds."));
        });
    }

    private void list(CommandSender sender) {
        sender.sendMessage(Text.of("<6><l>Rate multipliers"));
        sender.sendMessage(Text.of("<7>Effective = category x global. Yellow values were changed with /rates."));
        for (RateCategory.Kind kind : RateCategory.Kind.values()) {
            for (RateCategory category : RateCategory.values()) {
                if (category.getKind() != kind) continue;
                String color = GameRates.isOverridden(category) ? "e" : "7";
                String value = format(GameRates.get(category));
                if (category.isGlobal()) {
                    sender.sendMessage(Text.of("<b>" + category.getKey() + " <" + color + ">" + value + "x"));
                } else {
                    sender.sendMessage(Text.of("<7> " + category.getKey() + " <" + color + ">" + value
                            + "x <8>= " + format(GameRates.multiplier(category)) + "x"));
                }
            }
        }
        sender.sendMessage(Text.of("<7>Usage: {}", getParams().usage()));
    }

    private static String format(double value) {
        return value == Math.floor(value) && !Double.isInfinite(value)
                ? String.valueOf((long) value)
                : String.format("%.3f", value).replaceAll("0+$", "");
    }
}
