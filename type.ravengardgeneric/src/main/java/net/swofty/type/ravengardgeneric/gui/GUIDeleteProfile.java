package net.swofty.type.ravengardgeneric.gui;

import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.ViewNavigator;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.ravengardgeneric.profile.RavengardProfiles;
import net.swofty.type.ravengardgeneric.user.RavengardPlayer;

import java.util.UUID;

public class GUIDeleteProfile extends RavengardView {
    private static final int PANEL_ICON = 0xF001;
    private static final int SLOT_CONFIRM = 28;
    private static final int SLOT_CANCEL = 32;

    private final UUID profileId;

    public GUIDeleteProfile(UUID profileId) {
        this.profileId = profileId;
    }

    @Override
    protected String title() {
        return "Delete profile?";
    }

    @Override
    protected int panelIcon() {
        return PANEL_ICON;
    }

    @Override
    protected void content(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        interactive(layout, SLOT_CONFIRM, RavengardItems.button(RavengardButton.TEXT_CONFIRM)
                        .label("<a>Confirm")
                        .blankLine()
                        .lore("<e>Click to confirm this change!"),
                (click, viewContext) -> {
                    if (viewContext.player() instanceof RavengardPlayer player) {
                        player.closeInventory();
                        RavengardProfiles.delete(player, profileId);
                    }
                });

        interactive(layout, SLOT_CANCEL, RavengardItems.button(RavengardButton.TEXT_CANCEL)
                        .label("<c>Cancel")
                        .blankLine()
                        .lore("<e>Click to cancel this change!"),
                (click, viewContext) -> viewContext.backOrClose());

        backButton(layout);
    }
}
