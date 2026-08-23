package net.swofty.type.ravengardgeneric.gui;

import net.minestom.server.inventory.click.Click;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.ViewNavigator;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.ravengardgeneric.classes.RavengardClass;
import net.swofty.type.ravengardgeneric.profile.RavengardProfile;
import net.swofty.type.ravengardgeneric.profile.RavengardProfiles;
import net.swofty.type.ravengardgeneric.user.RavengardPlayer;

import java.util.List;
import java.util.UUID;

public class GUIProfiles extends RavengardView {
    private static final int PANEL_ICON = 0xE238;
    /** The five profile columns of the captured menu, statues spanning rows two to four. */
    private static final int[] COLUMN_ORIGINS = {18, 20, 22, 24, 26};

    @Override
    protected String title() {
        return "Profiles";
    }

    @Override
    protected int panelIcon() {
        return PANEL_ICON;
    }

    @Override
    protected void content(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        if (!(ctx.player() instanceof RavengardPlayer player)) {
            return;
        }

        List<RavengardProfile> profiles = RavengardProfiles.list(player);
        UUID selected = player.getSelectedProfile();

        for (int column = 0; column < COLUMN_ORIGINS.length; column++) {
            int origin = COLUMN_ORIGINS[column];
            if (column < profiles.size()) {
                placeProfile(layout, origin, profiles.get(column), selected);
            } else {
                placeCreate(layout, origin);
            }
        }

        backButton(layout);
    }

    private void placeProfile(ViewLayout<DefaultState> layout, int origin, RavengardProfile profile,
                              UUID selected) {
        RavengardClass profileClass = profile.getProfileClass();
        RavengardButton statue = profileClass == null
                ? RavengardButton.STATUE_KNIGHT
                : RavengardButton.statueFor(profileClass);
        boolean isSelected = profile.getId().equals(selected);

        RavengardItems.Builder button = RavengardItems.button(statue)
                .label(Text.literal(profileClass == null ? "Unclaimed" : profileClass.getDisplayName()))
                .lore(Text.of("<8>{}", profile.getId()))
                .blankLine()
                .lore(Text.of("<7>Class: <e>{}", profileClass == null ? "None" : profileClass.getDisplayName()),
                        Text.of("<7>Playtime: <e>{}", profile.playtimeText()),
                        Text.of("<7>Crowns: <e>{}", profile.getCrowns()),
                        Text.of("<7>Ability Points<e>: {}", profile.getAbilityPoints()),
                        Text.of("<7>Level: <e>{}", profile.getLevel()),
                        Text.of("<7>Experience: <e>{}/{}", profile.getExperience(), profile.experienceForNextLevel()))
                .blankLine()
                .origin(origin);

        if (isSelected) {
            button.lore("<a>Selected!");
        } else {
            button.lore("<e>Left-Click to select!", "<c>Right-Click to delete!");
        }

        interactive(layout, origin, button, (click, viewContext) -> {
                if (!(viewContext.player() instanceof RavengardPlayer target)) {
                    return;
                }
                if (click.click() instanceof Click.Right || click.click() instanceof Click.RightShift) {
                    ViewNavigator.get(target).push(new GUIDeleteProfile(profile.getId()));
                    return;
                }
                if (!isSelected) {
                    target.closeInventory();
                    RavengardProfiles.select(target, profile.getId());
                }
            });
    }

    private void placeCreate(ViewLayout<DefaultState> layout, int origin) {
        RavengardItems.Builder button = RavengardItems.button(RavengardButton.ADD)
                .label("<a>Create Profile")
                .blankLine()
                .lore("<e>Click to create!")
                .origin(origin);

        interactive(layout, origin, button, (click, viewContext) -> {
            if (viewContext.player() instanceof RavengardPlayer target) {
                target.closeInventory();
                RavengardProfiles.create(target);
            }
        });
    }
}
