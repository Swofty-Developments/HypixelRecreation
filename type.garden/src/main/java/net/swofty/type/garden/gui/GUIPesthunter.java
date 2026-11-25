package net.swofty.type.garden.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.garden.GardenData;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class GUIPesthunter extends StatelessView {
    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Pesthunter", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.backOrClose(layout, 31, ctx);

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        GardenData.GardenPestsData pests = GardenGuiSupport.pests(player);
        int activePests = pests.getActivePests().size();
        int penalty = net.swofty.type.garden.GardenServices.pests().getFortunePenaltyPercent(activePests, 0);

        layout.slot(11, ItemStackCreator.getStack(
            "§aPest Storage",
            Material.HOPPER_MINECART,
            1,
            "§7Stored pests: §2" + pests.getStoredPests(),
            "§7Active pests: §2" + activePests,
            "",
            "§7Offline pest timer: §e" + pests.getOfflineAccumulatorMs() / 1000 + "s"
        ));

        layout.slot(13, ItemStackCreator.getStack(
            "§ePesthunter Bonus",
            Material.CLOCK,
            1,
            "§7Current infestation penalty:",
            penalty == 0 ? "§aNo Farming Fortune penalty" : "§c-" + penalty + "% Farming Fortune",
            "",
            pests.getRepellentEndsAt() > System.currentTimeMillis()
                ? "§7Repellent active"
                : "§7Repellent inactive"
        ));

        layout.slot(15, ItemStackCreator.getStack(
            "§4§lൠ §cGarden Infestation",
            Material.RED_DYE,
            1,
            "§7For every §2+100ൠ Bonus Pest Chance",
            "§7you have, you can spawn §a1 §7extra pest",
            "§7before the penalty begins.",
            "",
            "§7Current pests: §2" + activePests,
            "§7Penalty: " + (penalty == 0 ? "§aSAFE" : "§c-" + penalty + "% ☘")
        ));
    }
}
