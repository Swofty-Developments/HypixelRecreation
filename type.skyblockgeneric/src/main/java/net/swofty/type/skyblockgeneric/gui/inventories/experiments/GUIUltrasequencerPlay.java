package net.swofty.type.skyblockgeneric.gui.inventories.experiments;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.*;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.experimentation.ExperimentTier;
import net.swofty.type.skyblockgeneric.experimentation.ExperimentRules;
import net.swofty.type.skyblockgeneric.experimentation.ExperimentType;
import net.swofty.type.skyblockgeneric.experimentation.ExperimentationManager;
import net.swofty.type.skyblockgeneric.experimentation.GameSession;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.Duration;
import java.util.List;

public final class GUIUltrasequencerPlay extends StatelessView {
    private static final Sound CLICK_SOUND = Sound.sound(
            Key.key("block.note_block.pling"), Sound.Source.PLAYER, 1f, 1.25f);
    private static final Sound ERROR_SOUND = Sound.sound(
            Key.key("block.note_block.bass"), Sound.Source.PLAYER, 1f, .6f);

    private final ExperimentTier tier;
    private int revealTicks;
    private int revealIndex;
    private int highlightedNumber = -1;
    private boolean sequencePlaying;
    private boolean gameOver;

    public GUIUltrasequencerPlay(ExperimentTier tier) {
        this.tier = tier;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Ultrasequencer · " + tier.displayName(), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void onOpen(DefaultState state, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        if (ExperimentationManager.getUltraSequencerState(player) == null
                && !ExperimentationManager.start(player, ExperimentType.ULTRASEQUENCER, tier)) {
            player.sendMessage("<c>Unable to start this experiment.");
            ctx.backOrClose();
            return;
        }
        ctx.session(DefaultState.class).refreshEvery(Duration.ofMillis(100));
        startRound(player);
    }

    @Override
    public void onClose(DefaultState state, ViewContext ctx, ViewSession.CloseReason reason) {
        if (!gameOver) {
            ExperimentationManager.cancel((SkyBlockPlayer) ctx.player(), ExperimentType.ULTRASEQUENCER);
        }
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.backOrClose(layout, 49, ctx);
        layout.slot(4, (s, c) -> timeItem((SkyBlockPlayer) c.player()));

        List<Integer> boardSlots = ExperimentRules.forExperiment(ExperimentType.ULTRASEQUENCER, tier).boardSlots();
        for (int index = 0; index < boardSlots.size(); index++) {
            int boardIndex = index;
            layout.slot(boardSlots.get(index),
                    (s, c) -> numberItem((SkyBlockPlayer) c.player(), boardIndex),
                    (click, viewCtx) -> {
                        GameSession.UltraSequencerState game = ExperimentationManager
                                .getUltraSequencerState((SkyBlockPlayer) viewCtx.player());
                        if (game != null && boardIndex < game.boardNumbers().size()) {
                            input((SkyBlockPlayer) viewCtx.player(), viewCtx, game.boardNumbers().get(boardIndex));
                        }
                    });
        }
    }

    @Override
    public void onRefresh(DefaultState state, ViewContext ctx) {
        if (gameOver) return;

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        GameSession.UltraSequencerState game = ExperimentationManager.getUltraSequencerState(player);
        if (game == null) return;

        if (sequencePlaying) {
            revealTicks++;
            if (revealTicks % 5 == 0 && revealIndex < game.sequence().size()) {
                highlightedNumber = game.sequence().get(revealIndex++);
                player.playSound(CLICK_SOUND);
            } else if (revealTicks % 5 == 3) {
                highlightedNumber = -1;
            }
            if (revealIndex >= game.sequence().size() && revealTicks % 5 == 3) {
                sequencePlaying = false;
                ExperimentationManager.ultraSequencerShown(player);
            }
        } else if (game.phase() == GameSession.GamePhase.PLAYING
                && game.deadline() > 0 && System.currentTimeMillis() >= game.deadline()) {
            showResults(player, ctx, false);
        }
    }

    private ItemStack.Builder numberItem(SkyBlockPlayer player, int boardIndex) {
        GameSession.UltraSequencerState game = ExperimentationManager.getUltraSequencerState(player);
        if (game == null || boardIndex >= game.boardNumbers().size()) {
            return ExperimentationGuiSupport.item(" ", Material.GRAY_STAINED_GLASS_PANE, 1);
        }
        int number = game.boardNumbers().get(boardIndex);
        if (sequencePlaying && highlightedNumber != number) {
            return ExperimentationGuiSupport.item(" ", Material.GRAY_STAINED_GLASS_PANE, 1,
                    "<7>Watch the sequence...");
        }
        if (!sequencePlaying && game.phase() == GameSession.GamePhase.PLAYING) {
            return ExperimentationGuiSupport.item(" ", Material.GRAY_STAINED_GLASS_PANE, 1,
                    "<7>Click the numbers in sequence.");
        }
        ItemStack.Builder item = ExperimentationGuiSupport.item("<f>" + number, Material.LIME_STAINED_GLASS_PANE, 1,
                "<7>Click the numbers in sequence.");
        return highlightedNumber == number ? ItemStacks.enchanted(item) : item;
    }

    private ItemStack.Builder timeItem(SkyBlockPlayer player) {
        GameSession.UltraSequencerState game = ExperimentationManager.getUltraSequencerState(player);
        if (game == null || game.deadline() <= 0) {
            return ExperimentationGuiSupport.item("<e>Watch the sequence", Material.CLOCK, 1,
                    "<7>Remember the numbers, then click them in order.");
        }
        int seconds = Math.max(0, (int) Math.ceil((game.deadline() - System.currentTimeMillis()) / 1_000d));
        return ExperimentationGuiSupport.item("<e>Time Left: <f>" + seconds, Material.CLOCK,
                Math.max(1, Math.min(64, seconds)),
                "<7>Remember the numbers, then click them in order.");
    }

    private void input(SkyBlockPlayer player, ViewContext ctx, int number) {
        if (gameOver || sequencePlaying) return;

        ExperimentationManager.UltraSequencerInputResult result =
                ExperimentationManager.inputUltraSequencer(player, number);
        if (!result.success()) {
            player.sendMessage("<c>" + result.errorMessage());
            return;
        }
        if (!result.correct()) {
            player.playSound(ERROR_SOUND);
            showResults(player, ctx, false);
            return;
        }

        player.playSound(CLICK_SOUND);
        if (result.complete() && !startRound(player)) {
            showResults(player, ctx, true);
            return;
        }
        ctx.session(DefaultState.class).refresh();
    }

    private boolean startRound(SkyBlockPlayer player) {
        if (!ExperimentationManager.startUltraSequencerRound(player)) return false;
        revealTicks = 0;
        revealIndex = 0;
        highlightedNumber = -1;
        sequencePlaying = true;
        return true;
    }

    private void showResults(SkyBlockPlayer player, ViewContext ctx, boolean completed) {
        if (gameOver) return;
        gameOver = true;

        ExperimentationManager.UltraSequencerFinishResult result =
                ExperimentationManager.finishUltraSequencer(player);
        if (result.success()) {
            ctx.replace(new GUIExperimentOver(ExperimentType.ULTRASEQUENCER, tier, completed,
                    completed ? "You completed the experiment." : "The sequence was broken.",
                    result.bestSeriesLength(), result.xpAward(), result.bonusClicksEarned()));
        } else {
            gameOver = false;
            player.sendMessage("<c>Unable to save your experiment result. Please try again.");
        }
    }

}
