package net.swofty.type.ravengardgeneric.gui;

import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.commons.ServerType;
import net.swofty.commons.text.Text;
import net.swofty.type.ravengardgeneric.classes.RavengardAbility;
import net.swofty.type.ravengardgeneric.classes.RavengardClass;
import net.swofty.type.ravengardgeneric.user.RavengardPlayer;

import java.util.List;

public class GUIRavengardMenu extends RavengardView {
    // origins taken from the captured menu
    private static final int SLOT_FIGHT = 3;
    private static final int SLOT_STATUE = 18;
    private static final int SLOT_LOCKBOX = 20;
    private static final int SLOT_BAG = 22;
    private static final int SLOT_TROPHY = 24;
    private static final int SLOT_ABILITY_ONE = 26;
    private static final int SLOT_QUILL = 38;
    private static final int SLOT_BOOK = 39;
    private static final int SLOT_CANDLE = 42;
    private static final int SLOT_ABILITY_TWO = 44;

    @Override
    protected String title() {
        return "Main Menu";
    }

    @Override
    protected void content(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        RavengardClass playerClass = ctx.player() instanceof RavengardPlayer player
                ? player.getRavengardClass()
                : null;

        for (int slot : RavengardButton.TEXT_FIGHT.coveredSlots(SLOT_FIGHT)) {
            layout.autoUpdating(slot,
                    (state2, ctx2) -> fightButton(ctx2).origin(SLOT_FIGHT).toBuilder(),
                    (click, viewCtx) -> {
                        if (!(viewCtx.player() instanceof RavengardPlayer player)) {
                            return;
                        }
                        if (net.swofty.type.ravengardgeneric.queue.RavengardQueue.isQueued(player)) {
                            net.swofty.type.ravengardgeneric.queue.RavengardQueue.leave(player);
                        } else {
                            player.closeInventory();
                            net.swofty.type.ravengardgeneric.queue.RavengardQueue.join(player);
                        }
                    },
                    java.time.Duration.ofSeconds(1));
        }

        RavengardButton statue = playerClass == null ? null : RavengardButton.statueFor(playerClass);
        if (statue != null) {
            RavengardItems.Builder profile = RavengardItems.button(statue)
                    .label(Text.of("Profiles<7> - <f>{}", playerClass.getDisplayName()))
                    .blankLine()
                    .lore(playerClass.selectLore())
                    .blankLine();

            int[] stats = playerClass.baseStats();
            if (stats != null) {
                profile.lore(Text.of("<7>Stats:"),
                                Text.of("<f> ◦ <7>Health: <c>{} ❤", stats[0]),
                                Text.of("<f> ◦ <7>Protection: <b>{} ⛊", stats[1]),
                                Text.of("<f> ◦ <7>Damage: <4>{} ⚔", stats[2]))
                        .blankLine();
            }

            net.swofty.type.ravengardgeneric.profile.RavengardProfile active =
                    ctx.player() instanceof RavengardPlayer player
                            ? net.swofty.type.ravengardgeneric.data.RavengardProfileStorage
                                    .byId(player.getSelectedProfile())
                            : null;
            if (active != null) {
                profile.lore(Text.of("<7>Progression:"),
                                Text.of("<f> ◦ <7>Level: <6>{}", active.getLevel()),
                                Text.of("<f> ◦ <7>Experience: <e>{}<7>/<e>{}",
                                        active.getExperience(), active.experienceForNextLevel()))
                        .blankLine();
            }

            interactive(layout, SLOT_STATUE, profile.lore("<e>Click to change profile!"),
                    (click, viewCtx) -> net.swofty.type.generic.gui.v2.ViewNavigator
                            .get(viewCtx.player()).push(new GUIProfiles()));
        }

        interactive(layout, SLOT_LOCKBOX, RavengardItems.button(RavengardButton.CHEST)
                        .label("Lock Box")
                        .lore("<7>Safely store your items here!")
                        .blankLine()
                        .lore("<e>Click to open!"),
                (click, viewCtx) -> net.swofty.type.generic.gui.v2.ViewNavigator
                        .get(viewCtx.player()).push(new GUILockBox()));

        // Unreleased features. Hypixel renders their whole tooltip in the Illager rune font so the
        // placeholder text (alphabet runs, lorem ipsum) is unreadable in game; kept verbatim.
        place(layout, SLOT_BAG, RavengardItems.button(RavengardButton.BAG)
                .font(RavengardFont.ILLAGERALT)
                .label("Abcdefgh")
                .lore("<7>Lorem ipsum dolor sit amet!")
                .blankLine()
                .lore("<c>Coming Soon!"));

        place(layout, SLOT_TROPHY, RavengardItems.button(RavengardButton.TROPHY)
                .font(RavengardFont.ILLAGERALT)
                .label("Ijklmno")
                .lore("<7>Consectetur adipiscing elit.")
                .blankLine()
                .lore("<c>Coming Soon!"));

        place(layout, SLOT_QUILL, RavengardItems.button(RavengardButton.QUILL)
                .font(RavengardFont.ILLAGERALT)
                .label("Pqrstuv")
                .lore("<7>Sed do eiusmod tempor incididunt!")
                .blankLine()
                .lore("<c>Coming Soon!"));

        place(layout, SLOT_BOOK, RavengardItems.button(RavengardButton.BOOK)
                .font(RavengardFont.ILLAGERALT)
                .label("Pqrstu")
                .lore("<7>Sed do eiusmod tempor incididunt!")
                .blankLine()
                .lore("<c>Coming Soon!"));

        place(layout, SLOT_CANDLE, RavengardItems.button(RavengardButton.CANDLE)
                .font(RavengardFont.ILLAGERALT)
                .label("Vwxyz")
                .lore("<7>Ut labore et dolore magna aliqua!")
                .blankLine()
                .lore("<c>Coming Soon!"));

        if (playerClass == null) {
            return;
        }

        List<RavengardAbility> abilities = playerClass.defaultAbilities();
        int[] slots = {SLOT_ABILITY_ONE, SLOT_ABILITY_TWO};
        for (int index = 0; index < slots.length && index < abilities.size(); index++) {
            RavengardAbility ability = abilities.get(index);
            final int page = index + 1;
            RavengardItems.Builder button = RavengardItems.button(ability)
                    .label(Text.of((page == 1 ? "First" : "Second") + " Ability<7> - <f>{}",
                            ability.getDisplayName()))
                    .lore(ability.getWrappedDescription())
                    .blankLine()
                    .lore("<e>Click to change!")
                    .origin(slots[index]);
            interactive(layout, slots[index], button, (click, viewCtx) ->
                    net.swofty.type.generic.gui.v2.ViewNavigator.get(viewCtx.player())
                            .push(new GUIAbilityPage(page)));
        }
    }

    private static RavengardItems.Builder fightButton(ViewContext ctx) {
        boolean queued = ctx.player() instanceof RavengardPlayer player
                && net.swofty.type.ravengardgeneric.queue.RavengardQueue.isQueued(player);
        if (queued) {
            return RavengardItems.button(RavengardButton.TEXT_LEAVE)
                    .label("Leave Queue!")
                    .lore("<7>Decided you don't want to fight?")
                    .blankLine()
                    .lore("<e>Click to leave the queue!");
        }
        return RavengardItems.button(RavengardButton.TEXT_FIGHT)
                .label("Join the Fight!")
                .lore("<7>Jump into the action and fight",
                        "<7>against other players and monsters!")
                .blankLine()
                .lore("<e>Click to join!");
    }

    @Override
    public void onOpen(DefaultState state, ViewContext ctx) {
        if (((RavengardPlayer) ctx.player()).isTutorial()) {
            ctx.player().sendMessage("<c>You must select a class to open this menu!");
            ctx.backOrClose();
            return;
        }
        super.onOpen(state, ctx);
    }
}
