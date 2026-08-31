package net.swofty.type.game.replay.event;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.swofty.commons.replay.protocol.ReplayDataReader;
import net.swofty.commons.replay.protocol.ReplayDataWriter;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ReplayComponentEventTest {
    @Test
    void preservesStyledAdventureComponent() throws Exception {
        Component component = Component.translatable("chat.type.text")
                .color(NamedTextColor.GOLD)
                .decorate(TextDecoration.BOLD)
                .hoverEvent(HoverEvent.showText(Component.text("hover", NamedTextColor.AQUA)))
                .clickEvent(ClickEvent.runCommand("/replay"))
                .append(Component.text(" child", NamedTextColor.RED));
        ReplayComponentEvent event = new ReplayComponentEvent(ReplayComponentEvent.Kind.CHAT, component);
        ReplayDataWriter writer = new ReplayDataWriter();
        event.write(writer);

        ReplayComponentEvent decoded;
        try (ReplayDataReader reader = new ReplayDataReader(writer.toByteArray())) {
            decoded = ReplayComponentEvent.read(reader);
        }

        assertEquals(event, decoded);
    }
}
