package net.swofty.type.ravengardgeneric.gui;

import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.ravengardgeneric.classes.RavengardClass;
import net.swofty.type.ravengardgeneric.classes.RavengardSelection;
import net.swofty.type.ravengardgeneric.user.RavengardPlayer;

public class GUISelectClass extends RavengardView {
    private static final int PANEL_ICON = 0xF238;

    /**
     * Origin slot and captured offset per class, taken verbatim from the 0.2 capture because
     * Hypixel authors them per menu. The menu holds two statue rows: the classes launch trio on
     * top, then Assassin, the new Sorcerer and a coming-soon placeholder below.
     */
    private enum Option {
        KNIGHT(RavengardClass.KNIGHT, RavengardButton.STATUE_KNIGHT, 2, 0x4D1C00),
        WARRIOR(RavengardClass.WARRIOR, RavengardButton.STATUE_WARRIOR, 4, 0x721C00),
        HUNTER(RavengardClass.HUNTER, RavengardButton.STATUE_HUNTER, 6, 0x961C00),
        ASSASSIN(RavengardClass.ASSASSIN, RavengardButton.STATUE_ASSASSIN, 29, 0x4E5300),
        SORCERER(RavengardClass.SORCERER, RavengardButton.STATUE_SORCERER, 31, 0x735300);

        private final RavengardClass value;
        private final RavengardButton statue;
        private final int slot;
        private final int offset;

        Option(RavengardClass value, RavengardButton statue, int slot, int offset) {
            this.value = value;
            this.statue = statue;
            this.slot = slot;
            this.offset = offset;
        }
    }

    @Override
    protected String title() {
        return "Select Class";
    }

    @Override
    protected int panelIcon() {
        return PANEL_ICON;
    }

    private static final int SLOT_UNAVAILABLE = 33;
    private static final int OFFSET_UNAVAILABLE = 0x965300;

    @Override
    protected void content(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        for (Option option : Option.values()) {
            RavengardItems.Builder button = RavengardItems.button(option.statue)
                    .footprint(1, 2)
                    .hoverColor(option.offset)
                    .label(Text.of("<a>{}", option.value.getDisplayName()))
                    .lore(option.value.selectLore())
                    .blankLine()
                    .lore("<e>Click to select!");

            interactive(layout, option.slot, button, (click, viewContext) -> {
                if (click.player() instanceof RavengardPlayer player) {
                    RavengardSelection.choose(player, option.value);
                }
            });
        }

        place(layout, SLOT_UNAVAILABLE, RavengardItems.button(RavengardButton.STATUE_DEFAULT)
                .footprint(1, 2)
                .hoverColor(OFFSET_UNAVAILABLE)
                .font(RavengardFont.ILLAGERALT)
                .label("<c>Unavailable")
                .lore("<7>This class is currently unavailable.")
                .lore("<7>")
                .lore("<7><c>Coming Soon."));
    }
}
