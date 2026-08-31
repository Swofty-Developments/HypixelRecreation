package net.swofty.type.skyblockgeneric.gui.inventories.experiments;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class GUIChronomatronPlay extends StatelessView {
    private static final Material[] COLOR_MATERIALS = {
            Material.RED_STAINED_GLASS,
            Material.BLUE_STAINED_GLASS,
            Material.LIME_STAINED_GLASS,
            Material.YELLOW_STAINED_GLASS,
            Material.WHITE_STAINED_GLASS,
            Material.PURPLE_STAINED_GLASS,
            Material.ORANGE_STAINED_GLASS,
            Material.PINK_STAINED_GLASS,
            Material.LIGHT_BLUE_STAINED_GLASS,
            Material.CYAN_STAINED_GLASS
    };
    private final ExperimentTier tier;
    private final Map<Integer, Integer> slotColors = new ConcurrentHashMap<>();
    private int revealTicks;
    private int revealIndex;
    private int highlightedColor = -1;
    private boolean sequencePlaying;
    private boolean gameOver;

    public GUIChronomatronPlay(ExperimentTier tier) {
        this.tier = tier;
        ExperimentRules.Rule rule = ExperimentRules.forExperiment(ExperimentType.CHRONOMATRON, tier);
        for (int color = 0; color < rule.colorCount(); color++) {
            int colorIndex = color;
            rule.slotsForColor(colorIndex).forEach(slot -> slotColors.put(slot, colorIndex));
        }
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.of("Chronomatron · {}", tier.displayName()), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void onOpen(DefaultState state, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        if (ExperimentationManager.getChronomatronState(player) == null
                && !ExperimentationManager.start(player, ExperimentType.CHRONOMATRON, tier)) {
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
            ExperimentationManager.cancel((SkyBlockPlayer) ctx.player(), ExperimentType.CHRONOMATRON);
        }
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.backOrClose(layout, 49, ctx);
        layout.slot(4, (s, c) -> timeItem((SkyBlockPlayer) c.player()));

        slotColors.forEach((slot, color) -> layout.slot(slot, (s, c) -> colorItem(color), (click, viewCtx) -> {
            if (gameOver || sequencePlaying) return;
            SkyBlockPlayer player = (SkyBlockPlayer) viewCtx.player();
            ExperimentationManager.ChronomatronInputResult result = ExperimentationManager.inputChronomatron(player, color);
            if (!result.success()) {
                player.sendMessage(Text.of("<c>{}", result.errorMessage()));
                return;
            }
            if (!result.correct()) {
                player.playSound(Sound.sound(Key.key("block.note_block.bass"), Sound.Source.PLAYER, 1f, .5f));
                showResults(player, viewCtx, false);
                return;
            }
            player.playSound(Sound.sound(Key.key("block.note_block.pling"), Sound.Source.PLAYER, 1f, 1f));
            if (result.complete() && !startRound(player)) {
                showResults(player, viewCtx, true);
                return;
            }
            viewCtx.session(DefaultState.class).refresh();
        }));
    }

    @Override
    public void onRefresh(DefaultState state, ViewContext ctx) {
        if (gameOver) return;
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        GameSession.ChronomatronState game = ExperimentationManager.getChronomatronState(player);
        if (game == null) return;

        if (sequencePlaying) {
            revealTicks++;
            if (revealTicks % 5 == 0 && revealIndex < game.sequence().size()) {
                highlightedColor = game.sequence().get(revealIndex++);
                player.playSound(Sound.sound(Key.key("block.note_block.pling"), Sound.Source.PLAYER, 1f,
                        .8f + (revealIndex * .05f)));
            } else if (revealTicks % 5 == 3) {
                highlightedColor = -1;
            }
            if (revealIndex >= game.sequence().size() && revealTicks % 5 == 3) {
                sequencePlaying = false;
                ExperimentationManager.chronomatronSequenceShown(player);
            }
        } else if (game.phase() == GameSession.GamePhase.PLAYING
                && game.deadline() > 0 && System.currentTimeMillis() >= game.deadline()) {
            showResults(player, ctx, false);
        }
    }

    private ItemStack.Builder colorItem(int color) {
        ItemStack.Builder item = ExperimentationGuiSupport.item("<f>Color " + (color + 1), COLOR_MATERIALS[color], 1);
        return highlightedColor == color ? ItemStacks.enchanted(item) : item;
    }

    private ItemStack.Builder timeItem(SkyBlockPlayer player) {
        GameSession.ChronomatronState game = ExperimentationManager.getChronomatronState(player);
        if (game == null || game.deadline() <= 0) {
            return ExperimentationGuiSupport.item("<e>Watch the sequence", Material.CLOCK, 1,
                    "<7>Repeat the displayed sequence before time runs out.");
        }
        int seconds = Math.max(0, (int) Math.ceil((game.deadline() - System.currentTimeMillis()) / 1_000d));
        return ExperimentationGuiSupport.item("<e>Time Left: <f>" + seconds, Material.CLOCK,
                Math.max(1, Math.min(64, seconds)),
                "<7>Repeat the displayed sequence before time runs out.");
    }

    private boolean startRound(SkyBlockPlayer player) {
        if (!ExperimentationManager.startChronomatronRound(player)) return false;
        revealTicks = 0;
        revealIndex = 0;
        highlightedColor = -1;
        sequencePlaying = true;
        return true;
    }

    private void showResults(SkyBlockPlayer player, ViewContext ctx, boolean completed) {
        if (gameOver) return;
        gameOver = true;
        ExperimentationManager.ChronomatronFinishResult result = ExperimentationManager.finishChronomatron(player);
        if (result.success()) {
            ctx.replace(new GUIExperimentOver(ExperimentType.CHRONOMATRON, tier, completed,
                    Text.literal(completed ? "You completed the experiment." : "The sequence was broken."),
                    result.bestChain(), result.xpAward(), result.bonusClicksEarned()));
        } else {
            gameOver = false;
            player.sendMessage("<c>Unable to save your experiment result. Please try again.");
        }
    }

}
