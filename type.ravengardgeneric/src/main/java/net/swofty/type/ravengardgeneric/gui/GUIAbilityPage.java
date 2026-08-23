package net.swofty.type.ravengardgeneric.gui;

import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.ravengardgeneric.classes.RavengardAbility;
import net.swofty.type.ravengardgeneric.classes.RavengardClass;
import net.swofty.type.ravengardgeneric.data.RavengardProfileStorage;
import net.swofty.type.ravengardgeneric.profile.RavengardProfile;
import net.swofty.type.ravengardgeneric.user.RavengardPlayer;

import java.util.List;

/**
 * The per-slot ability page, rebuilt from a 0.2 capture: the class's seven abilities laid out
 * down the panel, equipped ones on their own sprite and locked ones on the shared locked sprite,
 * with the ability points counter beside the back button.
 */
public class GUIAbilityPage extends RavengardView {
    private static final int PANEL_ICON = 0xE23D;
    private static final int[] ABILITY_SLOTS = {4, 11, 15, 22, 29, 33, 40};
    private static final int SLOT_POINTS = 44;

    private final int abilitySlot;

    public GUIAbilityPage(int abilitySlot) {
        this.abilitySlot = abilitySlot;
    }

    @Override
    protected String title() {
        return abilitySlot == 1 ? "First Ability" : "Second Ability";
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
        RavengardClass playerClass = player.getRavengardClass();
        if (playerClass == null) {
            return;
        }

        List<RavengardAbility> abilities = playerClass.getAbilities();
        List<RavengardAbility> equipped = playerClass.defaultAbilities();

        for (int index = 0; index < ABILITY_SLOTS.length && index < abilities.size(); index++) {
            RavengardAbility ability = abilities.get(index);
            int equippedSlot = equipped.indexOf(ability) + 1;

            RavengardItems.Builder button = equippedSlot > 0
                    ? RavengardItems.button(ability)
                            .label(Text.of("<a>{}", ability.getDisplayName()))
                            .lore("<a><l>EQUIPPED")
                    : RavengardItems.button(RavengardButton.ABILITY_LOCKED)
                            .label(Text.of("<c>{}", ability.getDisplayName()))
                            .lore("<c><l>LOCKED");

            button.blankLine()
                    .lore(ability.getWrappedDescription())
                    .blankLine()
                    .lore(Text.of("<7>Cooldown: <e>{}", ability.getCooldownText()))
                    .blankLine();

            if (equippedSlot > 0) {
                button.lore(Text.of("<c>Already equipped in <e>Slot {}<c>!", equippedSlot));
            } else {
                button.lore("<7>Cost: <e>1<b> Ability Point")
                        .blankLine()
                        .lore("<c>Not enough points to purchase this skill!");
            }

            place(layout, ABILITY_SLOTS[index], button);
        }

        RavengardProfile active = RavengardProfileStorage.byId(player.getSelectedProfile());
        int points = active == null ? 0 : active.getAbilityPoints();
        interactive(layout, SLOT_POINTS, RavengardItems.button(RavengardButton.LEVEL)
                .label(Text.of("<b>Ability Points <7>(<e>{}<7>)", points))
                .lore("<7>You can unlock new <a>abilities<7> by",
                        "<7>spending <b>Ability Points<7> in this menu.")
                .blankLine()
                .lore("<7>These points are rewarded for",
                        "<7>earning <e>experience<7> and <6>levelling up<7>",
                        "<7>your class.")
                .blankLine()
                .lore(Text.of("<e>Experience<7> is earned through:"),
                        Text.of("<7> <f>◦<7> Defeating mobs and bosses."),
                        Text.of("<7> <f>◦<7> Killing other players."),
                        Text.of("<7> <f>◦<7> Escaping through portals.")),
                (click, viewContext) -> click.player().playSound(
                        net.kyori.adventure.sound.Sound.sound()
                                .type(net.kyori.adventure.key.Key.key(
                                        "hypixel_ravengard", "ambience.town.raven"))
                                .volume(1.0f)
                                .pitch(1.0f)
                                .build()));

        backButton(layout);
    }
}
