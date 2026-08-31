package net.swofty.type.skyblockgeneric.gui.inventories.experiments;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.v2.*;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.experimentation.ExperimentReward;
import net.swofty.type.skyblockgeneric.experimentation.ExperimentTier;
import net.swofty.type.skyblockgeneric.experimentation.ExperimentType;
import net.swofty.type.skyblockgeneric.experimentation.ExperimentationManager;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointExperimentation;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public final class GUIExperimentOver extends StatelessView {
    private final ExperimentType experiment;
    private final ExperimentTier tier;
    private final boolean completed;
    private final Text message;
    private final int score;
    private final int xp;
    private final int bonusClicks;

    public GUIExperimentOver(ExperimentType experiment, ExperimentTier tier, boolean completed, Text message,
                             int score, int xp, int bonusClicks) {
        this.experiment = experiment;
        this.tier = tier;
        this.completed = completed;
        this.message = message;
        this.score = score;
        this.xp = xp;
        this.bonusClicks = bonusClicks;
    }

    public static GUIExperimentOver fromPending(DatapointExperimentation.PendingResult pending) {
        ExperimentType experiment = ExperimentType.fromName(pending.experimentType());
        ExperimentTier tier = ExperimentTier.fromName(pending.tier());
        return new GUIExperimentOver(experiment, tier, pending.completed(),
                Text.literal(pending.completed() ? "You completed the experiment." : "The experiment ended."),
                pending.score(), pending.xpAward(), pending.bonusClicks());
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.of("{} Results", experiment.displayName()), InventoryType.CHEST_3_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.backOrClose(layout, 22, ctx);
        DatapointExperimentation.PendingResult pending = ExperimentationManager
                .pendingResult((SkyBlockPlayer) ctx.player());
        if (pending != null) {
            int slot = 2;
            for (DatapointExperimentation.PendingReward pendingReward : pending.rewards()) {
                if (slot > 6) break;
                layout.slot(slot++, (s, c) -> pendingRewardItem((SkyBlockPlayer) c.player(), pendingReward));
            }
        }
        layout.slot(13, ExperimentationGuiSupport.item(
                Text.of(completed ? "<a>Experiment Complete!" : "<c>Experiment Over"),
                completed ? Material.LIME_DYE : Material.RED_DYE,
                1,
                Text.of("<7>{} · {}", experiment.displayName(), tier.displayName()),
                Text.empty(),
                Text.of("<7>{}", message),
                Text.empty(),
                Text.of("<7>Best score: <e>{}", score),
                Text.of("<7>Enchanting XP: <b>+{}", xp),
                bonusClicks > 0 ? Text.of("<7>Superpairs clicks earned: <a>+{}", bonusClicks) : Text.empty()
        ));
        layout.slot(11, ExperimentationGuiSupport.item("<a>Claim Rewards", Material.LIME_DYE, 1,
                        "<7>Receive the XP, clicks, and items", "<7>shown in this result.", "", "<e>Click to claim!"),
                (_, viewCtx) -> {
                    if (ExperimentationManager.claimPending((SkyBlockPlayer) viewCtx.player())) viewCtx.backOrClose();
                });
        if (experiment == ExperimentType.SUPERPAIRS) {
            layout.slot(15, ExperimentationGuiSupport.item("<7>No Decline", Material.GRAY_DYE, 1,
                    "<7>Superpairs rewards must be claimed."));
        } else {
            layout.slot(15, ExperimentationGuiSupport.item("<c>Decline & Retry", Material.RED_DYE, 1,
                            "<7>Give up these rewards and retry", "<7>for the add-on cost.", "", "<e>Click to decline!"),
                    (_, viewCtx) -> {
                        if (ExperimentationManager.declinePending((SkyBlockPlayer) viewCtx.player())) viewCtx.backOrClose();
                    });
        }
    }

    private static ItemStack.Builder pendingRewardItem(SkyBlockPlayer player,
                                                       DatapointExperimentation.PendingReward pendingReward) {
        try {
            ExperimentReward reward = ExperimentReward.fromName(pendingReward.rewardId());
            if (reward == ExperimentReward.EXPERIENCE) {
                return ExperimentationGuiSupport.item(reward.displayName(), Material.EXPERIENCE_BOTTLE, 1,
                        "<7>Amount: <3>" + pendingReward.amount() + " Enchanting XP");
            }
            var item = reward.createItem(pendingReward.rarityValue());
            item.setAmount(pendingReward.amount());
            return PlayerItemUpdater.playerUpdate(player, item.getItemStack());
        } catch (RuntimeException exception) {
            return ExperimentationGuiSupport.item("<c>Invalid reward", Material.BARRIER, 1,
                    "<7>This result needs staff attention.");
        }
    }
}
