package net.swofty.type.generic.command;

import net.kyori.adventure.identity.Identity;
import net.kyori.adventure.pointer.Pointers;
import net.kyori.adventure.text.Component;
import net.minestom.server.command.CommandSender;
import net.minestom.server.command.ConsoleSender;
import net.minestom.server.tag.TagHandler;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.text.HypixelTextRenderer;
import net.swofty.type.generic.text.RenderContext;
import org.jetbrains.annotations.NotNull;

/**
 * Console side of the {@code sendMessage(String)} contract.
 *
 * {@link net.swofty.type.generic.user.HypixelPlayer} converts markup at its own boundary, but Minestom's
 * {@link ConsoleSender} treats a raw string as literal text, so the same call site would print {@code <c>}
 * tags to the log. The command framework hands executors this delegate instead of the bare console sender,
 * which parses the markup and lets the console sender log the resulting component.
 *
 * Players are handed through untouched so that {@code instanceof} checks and casts inside executors keep
 * working; only the console is ever wrapped.
 */
public final class ConsoleMarkupSender implements CommandSender {

    private final ConsoleSender console;

    private ConsoleMarkupSender(ConsoleSender console) {
        this.console = console;
    }

    public static CommandSender wrap(CommandSender sender) {
        return sender instanceof ConsoleSender console ? new ConsoleMarkupSender(console) : sender;
    }

    @Override
    public void sendMessage(@NotNull String markup) {
        sendMessage(Text.read(markup));
    }

    @Override
    public void sendMessage(@NotNull Component message) {
        console.sendMessage(HypixelTextRenderer.render(message, RenderContext.ofServer()));
    }

    @Override
    public @NotNull TagHandler tagHandler() {
        return console.tagHandler();
    }

    @Override
    public @NotNull Identity identity() {
        return console.identity();
    }

    @Override
    public @NotNull Pointers pointers() {
        return console.pointers();
    }
}
