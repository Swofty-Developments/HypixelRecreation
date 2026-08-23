package net.swofty.type.ravengardgeneric.entity;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.instance.Instance;
import net.swofty.type.generic.entity.InteractionEntity;
import net.swofty.type.generic.world.HypixelWorldLoader;

/**
 * The playtest notice board on the Nevermore's deck, captured from the live server: a fixed text
 * display with a wide, flat interaction volume in front of it. The live board offers a "CLICK
 * HERE" line whose click response has not been captured yet, so clicking does nothing here.
 */
public final class NevermoreNoticeBoard {
    private static final Pos POSITION = new Pos(33.0, 61.5, 74.05);
    private static final int LINE_WIDTH = 240;
    private static final float CLICK_WIDTH = 3.0f;
    private static final float CLICK_HEIGHT = 0.5f;

    private NevermoreNoticeBoard() {
    }

    public static void spawn(Instance instance) {
        LivingEntity display = new LivingEntity(EntityType.TEXT_DISPLAY);
        display.editEntityMeta(TextDisplayMeta.class, meta -> {
            meta.setText(text());
            meta.setLineWidth(LINE_WIDTH);
            meta.setBackgroundColor(0);
            meta.setShadow(true);
        });
        spawn(display, instance);

        spawn(new InteractionEntity(CLICK_WIDTH, CLICK_HEIGHT, (player, event) -> {
        }), instance);
    }

    private static void spawn(Entity entity, Instance instance) {
        entity.setNoGravity(true);
        entity.setHasPhysics(false);
        entity.updateViewableRule(HypixelWorldLoader.LOADED_ONLY);
        entity.setInstance(instance, POSITION);
    }

    private static Component text() {
        return Component.empty()
                .append(Component.text("Welcome to ", NamedTextColor.GOLD)
                        .append(Component.text("Ravengard").decorate(TextDecoration.BOLD)))
                .append(Component.text("!\n", NamedTextColor.GOLD))
                .append(Component.text("Still under development. Issues may arise.\n\n",
                        NamedTextColor.DARK_GRAY).decorate(TextDecoration.ITALIC))
                .append(bullet())
                .append(Component.text("PvPvE ", NamedTextColor.GREEN))
                .append(Component.text("extraction where ", NamedTextColor.GRAY))
                .append(Component.text("survival ", NamedTextColor.DARK_GREEN))
                .append(Component.text("means everything!\n", NamedTextColor.GRAY))
                .append(bullet())
                .append(Component.text("Enter the ", NamedTextColor.GRAY))
                .append(Component.text("dungeon ", NamedTextColor.RED))
                .append(Component.text("and ", NamedTextColor.GRAY))
                .append(Component.text("escape ", NamedTextColor.DARK_AQUA))
                .append(Component.text("with ", NamedTextColor.GRAY))
                .append(Component.text("loot", NamedTextColor.GOLD))
                .append(Component.text(".\n", NamedTextColor.GRAY))
                .append(bullet())
                .append(Component.text("Avoid the ", NamedTextColor.GRAY))
                .append(Component.text("death wall ", NamedTextColor.DARK_PURPLE))
                .append(Component.text("as it surrounds a random location.\n", NamedTextColor.GRAY))
                .append(bullet())
                .append(Component.text("If you ", NamedTextColor.GRAY))
                .append(Component.text("die ", NamedTextColor.RED))
                .append(Component.text("you will lose ", NamedTextColor.GRAY))
                .append(Component.text("everything ", NamedTextColor.DARK_RED))
                .append(Component.text("you are carrying.\n", NamedTextColor.GRAY))
                .append(bullet())
                .append(Component.text("Store items ", NamedTextColor.GRAY))
                .append(Component.text("in your ", NamedTextColor.GRAY))
                .append(Component.text("Lock Box", NamedTextColor.LIGHT_PURPLE))
                .append(Component.text(".\n", NamedTextColor.GRAY))
                .append(bullet())
                .append(Component.text("Defeat ", NamedTextColor.RED))
                .append(Component.text("enemies to ", NamedTextColor.GRAY))
                .append(Component.text("Level Up ", NamedTextColor.GOLD))
                .append(Component.text("and unlock abilities!\n\n", NamedTextColor.GRAY))
                .append(Component.text("Play test end date: ", NamedTextColor.GRAY))
                .append(Component.text("August 31st\n", NamedTextColor.GOLD))
                .append(Component.text("Have fun and good luck!\n\n", NamedTextColor.GRAY))
                .append(Component.text("CLICK HERE", NamedTextColor.YELLOW)
                        .decorate(TextDecoration.BOLD, TextDecoration.UNDERLINED))
                .append(Component.text(" to read more!", NamedTextColor.YELLOW))
                .append(Component.newline());
    }

    private static Component bullet() {
        return Component.text("> ", NamedTextColor.GOLD).decorate(TextDecoration.BOLD);
    }
}
