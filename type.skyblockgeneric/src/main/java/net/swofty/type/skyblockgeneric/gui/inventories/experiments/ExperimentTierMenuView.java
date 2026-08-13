package net.swofty.type.skyblockgeneric.gui.inventories.experiments;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.experimentation.ExperimentTier;
import net.swofty.type.skyblockgeneric.experimentation.ExperimentType;
import net.swofty.type.skyblockgeneric.experimentation.ExperimentationManager;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.Arrays;

abstract class ExperimentTierMenuView extends StatelessView {
    private static final int[] TIER_SLOTS = {19, 20, 21, 22, 23, 24};
    private static final int[] BORDER_SLOTS = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26, 27,
            35, 36, 37, 38, 41, 42, 43, 44};

    protected abstract ExperimentType experimentType();

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(experimentType().displayName() + " ➜ Stakes", InventoryType.CHEST_5_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        layout.filler(Arrays.stream(BORDER_SLOTS).boxed().toList(),
                ExperimentationGuiSupport.item(" ", Material.PURPLE_STAINED_GLASS_PANE, 1));
        if (!Components.back(layout, 39, ctx)) Components.close(layout, 39);
        Components.close(layout, 40);

        ExperimentTier[] tiers = switch (experimentType()) {
            case SUPERPAIRS -> ExperimentTier.values();
            case CHRONOMATRON -> new ExperimentTier[]{ExperimentTier.HIGH, ExperimentTier.GRAND,
                    ExperimentTier.SUPREME, ExperimentTier.TRANSCENDENT, ExperimentTier.METAPHYSICAL};
            case ULTRASEQUENCER -> new ExperimentTier[]{ExperimentTier.SUPREME, ExperimentTier.TRANSCENDENT,
                    ExperimentTier.METAPHYSICAL};
        };
        int[] tierSlots = experimentType() == ExperimentType.ULTRASEQUENCER
                ? new int[]{21, 22, 23} : TIER_SLOTS;
        for (int i = 0; i < tiers.length; i++) {
            ExperimentTier tier = tiers[i];
            layout.slot(tierSlots[i], (s, c) -> ExperimentationGuiSupport.tierIcon(
                    experimentType(), tier, (SkyBlockPlayer) c.player()), (click, viewCtx) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) viewCtx.player();
                if (!ExperimentationManager.start(player, experimentType(), tier)) return;
                viewCtx.push(playView(tier));
            });
        }
    }

    protected abstract net.swofty.type.generic.gui.v2.View<DefaultState> playView(ExperimentTier tier);
}
