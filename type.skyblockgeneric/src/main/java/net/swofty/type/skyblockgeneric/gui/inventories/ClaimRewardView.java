package net.swofty.type.skyblockgeneric.gui.inventories;

import net.minestom.server.inventory.InventoryType;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.*;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.function.BooleanSupplier;

public class ClaimRewardView implements View<ClaimRewardView.State> {

    public record State(SkyBlockItem rewardItem, BooleanSupplier canClaim, Runnable onClaim, boolean claimed) {
        public State(ItemType rewardItem, Runnable onClaim) {
            this(new SkyBlockItem(rewardItem), () -> true, onClaim, false);
        }

        public State(SkyBlockItem rewardItem, Runnable onClaim) {
            this(rewardItem, () -> true, onClaim, false);
        }

        public State(SkyBlockItem rewardItem, BooleanSupplier canClaim, Runnable onClaim) {
            this(rewardItem, canClaim, onClaim, false);
        }

        public State claim() {
            return new State(rewardItem, canClaim, onClaim, true);
        }
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return new ViewConfiguration<>("Claim Reward", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<ClaimRewardView.State> layout, ClaimRewardView.State state, ViewContext ctx) {
        Components.fill(layout);
        layout.slot(22,
                (_, _) -> ItemStacks.appendLore(
                        new NonPlayerItemUpdater(state.rewardItem()).getUpdatedItem(),
                        List.of(
                                Text.empty(),
                                Text.of("<e>Click to claim!")
                        )
                ),
                (s, viewContext) -> {
                    SkyBlockPlayer player = (SkyBlockPlayer) viewContext.player();
                    if (!s.state().canClaim().getAsBoolean()) {
                        viewContext.pop();
                        player.sendMessage("<c>You're unable to claim this reward at this time.");
                        return;
                    }
                    viewContext.session(State.class).update(State::claim);
                    SkyBlockItem item = state.rewardItem();
                    player.addAndUpdateItem(item);
                    player.sendMessage("<a>You claimed <f>{}<a>!", item.getDisplayName());
                    player.closeInventory();
                    state.onClaim().run();
                }
        );
        Components.close(layout, 49);
    }

    @Override
    public void onClose(State state, ViewContext ctx, ViewSession.CloseReason reason) {
        if (state.claimed() || !state.canClaim().getAsBoolean()) return;
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        state.claim();
        player.sendMessage("<a>You claimed <f>{}<a>!", state.rewardItem().getDisplayName());
        player.addAndUpdateItem(state.rewardItem());
        state.onClaim().run();
    }

}
