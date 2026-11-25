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
import net.swofty.type.skyblockgeneric.experimentation.*;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.time.Duration;
import java.util.List;

public final class GUISuperPairsPlay extends StatelessView {
    private static final Sound FLIP_SOUND = Sound.sound(
            Key.key("block.note_block.bit"), Sound.Source.PLAYER, 1f, 1.15f);
    private static final Sound MATCH_SOUND = Sound.sound(
            Key.key("entity.player.levelup"), Sound.Source.PLAYER, 1f, 1.25f);
    private static final Sound MISS_SOUND = Sound.sound(
            Key.key("block.note_block.bass"), Sound.Source.PLAYER, 1f, .6f);

    private final ExperimentTier tier;
    private boolean gameOver;

    public GUISuperPairsPlay(ExperimentTier tier) {
        this.tier = tier;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.of("Superpairs · {}", tier.displayName()), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void onOpen(DefaultState state, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        if (ExperimentationManager.getSuperPairsState(player) == null
                && !ExperimentationManager.start(player, ExperimentType.SUPERPAIRS, tier)) {
            player.sendMessage("<c>Unable to start this experiment.");
            ctx.backOrClose();
            return;
        }
        ctx.session(DefaultState.class).refreshEvery(Duration.ofMillis(100));
    }

    @Override
    public void onClose(DefaultState state, ViewContext ctx, ViewSession.CloseReason reason) {
        if (!gameOver) {
            ExperimentationManager.cancel((SkyBlockPlayer) ctx.player(), ExperimentType.SUPERPAIRS);
        }
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.backOrClose(layout, 49, ctx);

        layout.slot(4, (s, c) -> clicksItem((SkyBlockPlayer) c.player()));
        layout.slot(6, (s, c) -> scoreItem((SkyBlockPlayer) c.player()));

        List<Integer> boardSlots = ExperimentRules.forExperiment(ExperimentType.SUPERPAIRS, tier).boardSlots();
        for (int index = 0; index < boardSlots.size(); index++) {
            int tile = index;
            layout.slot(boardSlots.get(index),
                    (s, c) -> tileItem((SkyBlockPlayer) c.player(), tile),
                    (click, viewCtx) -> flip((SkyBlockPlayer) viewCtx.player(), viewCtx, tile));
        }
    }

    @Override
    public void onRefresh(DefaultState state, ViewContext ctx) {
        if (gameOver) return;

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        GameSession.SuperPairsState game = ExperimentationManager.getSuperPairsState(player);
        if (game == null) return;

        if (game.pairsFound() == game.rewardPairCount()
                || System.currentTimeMillis() >= game.deadline()
                || (game.clicksRemaining() <= 0 && game.mismatchUntil() <= System.currentTimeMillis())) {
            showResults(player, ctx, game.pairsFound() == game.rewardPairCount());
        }
    }

    private void flip(SkyBlockPlayer player, ViewContext ctx, int tile) {
        if (gameOver) return;

        ExperimentationManager.SuperPairsFlipResult result =
                ExperimentationManager.flipSuperPair(player, tile);
        if (!result.success()) {
            if (result.errorMessage() != null) player.sendMessage(Text.of("<c>{}", result.errorMessage()));
            return;
        }

        if (result.complete()) {
            player.playSound(MATCH_SOUND);
            showResults(player, ctx, true);
            return;
        }

        Sound sound = result.match() ? MATCH_SOUND : result.secondTile() >= 0 ? MISS_SOUND : FLIP_SOUND;
        player.playSound(sound);
        ctx.session(DefaultState.class).refresh();
    }

    private ItemStack.Builder tileItem(SkyBlockPlayer player, int tile) {
        GameSession.SuperPairsState game = ExperimentationManager.getSuperPairsState(player);
        if (game == null || game.board().size() <= tile) {
            return ExperimentationGuiSupport.item(" ", Material.GRAY_STAINED_GLASS_PANE, 1);
        }

        boolean visible = game.matchedTiles().contains(tile)
                || game.firstFlip() == tile
                || game.mismatchFirst() == tile
                || game.mismatchSecond() == tile;
        if (!visible) {
            return ExperimentationGuiSupport.item("<7>Click to reveal", Material.GRAY_STAINED_GLASS_PANE, 1);
        }

        SuperPairTile tileData = game.board().get(tile);
        if (tileData.isPowerUp()) {
            return ExperimentationGuiSupport.item(tileData.item().displayNameOrPowerUp(), tileData.item().material(), 1,
                    "<7>Power-ups take effect when revealed.");
        }
        if (tileData.reward() == ExperimentReward.EXPERIENCE) {
            return ExperimentationGuiSupport.item(tileData.reward().displayName(), Material.EXPERIENCE_BOTTLE, 1,
                    "<7>Grants <3>" + tileData.amount() + " Enchanting XP<7>.");
        }
        var item = tileData.createItem();
        item.setAmount(tileData.amount());
        return PlayerItemUpdater.playerUpdate(player, item.getItemStack());
    }

    private ItemStack.Builder clicksItem(SkyBlockPlayer player) {
        GameSession.SuperPairsState game = ExperimentationManager.getSuperPairsState(player);
        int clicks = game == null ? 0 : game.clicksRemaining();
        return ExperimentationGuiSupport.item(
                "<e>Clicks Left: <f>" + clicks,
                Material.CLOCK,
                Math.max(1, Math.min(64, clicks)),
                "<7>Each tile flip uses one click.",
                "<7>Match every pair before you run out.");
    }

    private ItemStack.Builder scoreItem(SkyBlockPlayer player) {
        GameSession.SuperPairsState game = ExperimentationManager.getSuperPairsState(player);
        int pairs = game == null ? 0 : game.pairsFound();
        int total = ExperimentRules.forExperiment(ExperimentType.SUPERPAIRS, tier).pairCount();
        return ExperimentationGuiSupport.item("<a>Pairs Found: <f>" + pairs + "/" + total, Material.CHEST, 1,
                "<7>Find matching items to earn Enchanting XP.");
    }

    private void showResults(SkyBlockPlayer player, ViewContext ctx, boolean completed) {
        if (gameOver) return;
        gameOver = true;

        ExperimentationManager.SuperPairsFinishResult result = ExperimentationManager.finishSuperPairs(player);
        if (result.success()) {
            ctx.replace(new GUIExperimentOver(ExperimentType.SUPERPAIRS, tier, completed,
                    Text.literal(completed ? "You found every pair." : "You ran out of clicks."),
                    result.pairsFound(), result.xpAward(), result.bonusClicksEarned()));
        } else {
            gameOver = false;
            player.sendMessage("<c>Unable to save your experiment result. Please try again.");
        }
    }
}
