package net.swofty.velocity.text;

import com.velocitypowered.api.proxy.Player;
import net.kyori.adventure.text.Component;
import net.swofty.commons.text.Text;

public final class ProxyText {

    private ProxyText() {
    }

    public static Component render(String markup, Object... arguments) {
        return Text.of(markup, arguments).asComponent();
    }

    public static void disconnect(Player player, String markup, Object... arguments) {
        player.disconnect(render(markup, arguments));
    }

    public static void disconnect(Player player, Text reason) {
        player.disconnect(reason.asComponent());
    }
}
