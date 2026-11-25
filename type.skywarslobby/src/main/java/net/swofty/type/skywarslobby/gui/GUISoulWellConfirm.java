package net.swofty.type.skywarslobby.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.datapoints.DatapointLong;
import net.swofty.type.generic.data.datapoints.DatapointSoulWellUpgrades;
import net.swofty.type.generic.data.handlers.SkywarsDataHandler;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.v2.*;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skywarslobby.soulwell.SoulWellMessages;
import net.swofty.type.skywarslobby.soulwell.SoulWellUpgrade;

import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class GUISoulWellConfirm extends StatelessView {

    private final SoulWellUpgrade upgrade;
    private final SoulWellUpgrade.SoulWellUpgradeTier tier;
    private final int newLevel;

    public GUISoulWellConfirm(SoulWellUpgrade upgrade, SoulWellUpgrade.SoulWellUpgradeTier tier, int newLevel) {
        this.upgrade = upgrade;
        this.tier = tier;
        this.newLevel = newLevel;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Are you sure?", InventoryType.CHEST_3_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        layout.allowHotkey(false);
        Components.fill(layout);

        SkywarsDataHandler handler = SkywarsDataHandler.getUser(ctx.player());
        long coins = handler != null ? handler.get(SkywarsDataHandler.Data.COINS, DatapointLong.class).getValue() : 0;

        String formattedCost = NumberFormat.getNumberInstance(Locale.US).format(tier.cost());
        boolean canAfford = coins >= tier.cost();

        layout.slot(13, (_, __) -> {
            String colorCode = upgrade.color();
            return ItemStackCreator.item(upgrade.material(), 1,
                    Text.of("<color:{}>{} {:roman}", colorCode, upgrade.name(), newLevel),
                    List.of(
                            Text.of("<8>Permanent Upgrade"),
                            Text.empty(),
                            Text.of("<7>{}", upgrade.baseDescription()),
                            Text.empty(),
                            tier.getEffectChangeLine(),
                            Text.empty(),
                            Text.of("<7>Cost: <6>{}", formattedCost)
                    ));
        });

        layout.slot(11,
                (_, __) -> {
                    if (canAfford) {
                        return ItemStackCreator.item(Material.LIME_TERRACOTTA, 1, """
                                <a>Confirm
                                <7>Click to purchase {}
                                <7>for <6>{} Coins<7>.""",
                                Text.of("<color:{}>{}", upgrade.color(), upgrade.name()), formattedCost);
                    }
                    return ItemStackCreator.item(Material.GRAY_TERRACOTTA, 1, """
                            <c>Cannot Afford
                            <7>You need <6>{} Coins
                            <7>to purchase this upgrade.

                            <c>You don't have enough coins!""", formattedCost);
                },
                (_, c) -> {
                    SkywarsDataHandler dataHandler = SkywarsDataHandler.getUser(c.player());
                    if (dataHandler == null) return;

                    DatapointLong coinsDatapoint = dataHandler.get(SkywarsDataHandler.Data.COINS, DatapointLong.class);
                    long currentCoins = coinsDatapoint.getValue();
                    if (currentCoins < tier.cost()) {
                        c.player().sendMessage("<c>You don't have enough coins to purchase this upgrade!");
                        c.session(Object.class).refresh();
                        return;
                    }

                    coinsDatapoint.setValue(currentCoins - tier.cost());

                    DatapointSoulWellUpgrades upgradesDatapoint = dataHandler.get(
                            SkywarsDataHandler.Data.SOUL_WELL_UPGRADES, DatapointSoulWellUpgrades.class);
                    upgradesDatapoint.getValue().setUpgradeLevel(upgrade.id(), newLevel);

                    SoulWellMessages.sendPurchaseMessage(c.player(), upgrade, tier, newLevel);

                    c.replace(new GUISoulWell());
                }
        );

        layout.slot(15,
                (_, __) -> ItemStackCreator.item(Material.RED_TERRACOTTA, 1, """
                        <c>Cancel
                        <7>Click to go back."""),
                (_, c) -> c.replace(new GUISoulWell())
        );
    }
}
