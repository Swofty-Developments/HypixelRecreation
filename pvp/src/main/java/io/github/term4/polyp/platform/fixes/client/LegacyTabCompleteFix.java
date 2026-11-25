package io.github.term4.polyp.platform.fixes.client;

import net.minestom.server.MinecraftServer;
import net.minestom.server.listener.TabCompleteListener;
import net.minestom.server.network.packet.client.play.ClientTabCompletePacket;
import net.minestom.server.network.packet.server.play.TabCompletePacket;

import java.util.Arrays;
import java.util.List;

/**
 * Command-name tab completion for legacy clients: answers a request whose cursor is still in the command name (no
 * space yet). A 1.8 client asks the server to complete command names (modern clients do it from the command tree);
 * Minestom's {@code TabCompleteListener} only suggests argument values, so names never complete - vanilla answers
 * because Brigadier's literal nodes self-suggest. Replaces the {@link ClientTabCompletePacket} listener server-wide;
 * anything it doesn't answer delegates to the stock {@link TabCompleteListener}.
 *
 * <p><b>Temporary:</b> removable once upstream branch {@code fix/tab-complete-command-names} is on the pinned dependency.
 */
public final class LegacyTabCompleteFix {

    // rebuilt on command-count change; requests are per keystroke
    private static volatile String[] names = new String[0];
    private static volatile int commandCount = -1;

    private LegacyTabCompleteFix() {}

    public static void install() {
        MinecraftServer.getPacketListenerManager().setPlayListener(ClientTabCompletePacket.class, (packet, player) -> {
            final String text = packet.text();
            final String prefix = text.startsWith("/") ? text.substring(1) : text;
            final List<TabCompletePacket.Match> matches = prefix.indexOf(' ') >= 0 ? List.of()
                    : Arrays.stream(commandNames())
                    .filter(name -> name.regionMatches(true, 0, prefix, 0, prefix.length()))
                    .map(name -> new TabCompletePacket.Match(name, null))
                    .toList();
            if (matches.isEmpty()) {
                TabCompleteListener.listener(packet, player); // argument (or unknown) completion - Minestom's
                return;
            }
            player.sendPacket(new TabCompletePacket(packet.transactionId(), 1, prefix.length(), matches));
        });
    }

    private static String[] commandNames() {
        var commands = MinecraftServer.getCommandManager().getCommands();
        if (commands.size() != commandCount) {
            names = commands.stream().flatMap(c -> Arrays.stream(c.getNames()))
                    .distinct().sorted().toArray(String[]::new);
            commandCount = commands.size();
        }
        return names;
    }
}
