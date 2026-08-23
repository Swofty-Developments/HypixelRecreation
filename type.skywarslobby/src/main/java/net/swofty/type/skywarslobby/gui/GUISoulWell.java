package net.swofty.type.skywarslobby.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.datapoints.DatapointLong;
import net.swofty.type.generic.data.datapoints.DatapointSoulWellUpgrades;
import net.swofty.type.generic.data.handlers.SkywarsDataHandler;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.*;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skywarslobby.soulwell.SoulWellUpgrade;
import net.swofty.type.skywarslobby.soulwell.SoulWellUpgradeRegistry;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class GUISoulWell extends StatelessView {
    private static final int BASE_ROLL_COST = 2;

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Soul Well", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        layout.allowHotkey(false);
        Components.fill(layout);

        SkywarsDataHandler handler = SkywarsDataHandler.getUser(ctx.player());
        long coins = handler != null ? handler.get(SkywarsDataHandler.Data.COINS, DatapointLong.class).getValue() : 0;
        DatapointSoulWellUpgrades.SoulWellUpgrades playerUpgrades = handler != null
                ? handler.get(SkywarsDataHandler.Data.SOUL_WELL_UPGRADES, DatapointSoulWellUpgrades.class).getValue()
                : DatapointSoulWellUpgrades.SoulWellUpgrades.empty();

        int wheelCount = getWheelCount(handler);
        int rollCost = wheelCount * BASE_ROLL_COST;

        // Roll Soul Well button (slot 12)
        layout.slot(12,
                (_, _) -> ItemStacks.item(Material.END_PORTAL_FRAME, 1, """
                        <a>Roll Soul Well
                        <7>Rolls for a random kit, perk, or coin
                        <7>bonus.

                        <7>Cost: <b>{} Souls

                        <e>Click to roll!""", rollCost),
                (_, c) -> {
                    if (handler == null) return;
                    long currentSouls = handler.get(SkywarsDataHandler.Data.SOULS, DatapointLong.class).getValue();
                    if (currentSouls < rollCost) {
                        c.player().sendMessage("<c>You don't have enough souls!");
                        return;
                    }

                    handler.get(SkywarsDataHandler.Data.SOULS, DatapointLong.class).setValue(currentSouls - rollCost);

                    c.push(new GUISoulWellRolling(wheelCount));
                }
        );

        // Soul Well Wheels setting (slot 14)
        layout.slot(14,
                (_, _) -> {
                    int wheels = getWheelCount(handler);
                    int cost = wheels * BASE_ROLL_COST;

                    List<Text> lore = new ArrayList<>();
                    lore.add(Text.of("<8>Setting"));
                    lore.add(Text.empty());
                    lore.add(Text.of("<7>Change the number of wheels your"));
                    lore.add(Text.of("<b>Soul Well <7>will spin each roll. <8>(max 5)"));
                    lore.add(Text.empty());
                    lore.add(Text.of("<7># of Wheels: <a>{} <8>({} Souls)", wheels, cost));
                    lore.add(Text.empty());
                    if (wheels < 5) lore.add(Text.of("<e>Left-click to increase!"));
                    if (wheels > 1) lore.add(Text.of("<e>Right-click to decrease!"));

                    return ItemStacks.item(Material.ENCHANTING_TABLE, 1,
                            Text.of("<6>Soul Well Wheels"), lore);
                },
                (click, c) -> {
                    if (handler == null) return;
                    int currentWheels = getWheelCount(handler);

                    if (click.click() instanceof Click.Left && currentWheels < 5) {
                        setWheelCount(handler, currentWheels + 1);
                    } else if (click.click() instanceof Click.Right && currentWheels > 1) {
                        setWheelCount(handler, currentWheels - 1);
                    }

                    c.session(Object.class).refresh();
                }
        );

        // Upgrade items
        layoutUpgradeItem(layout, 28, "xezbeth_luck", playerUpgrades, coins);
        layoutUpgradeItem(layout, 30, "harvesting_season", playerUpgrades, coins);
        layoutUpgradeItem(layout, 32, "angel_of_death", playerUpgrades, coins);

        // Head Collection placeholder (slot 34)
        layout.slot(34, (_, _) -> ItemStacks.item(Material.CHEST, 1, """
                <c>Head Collection
                <7>View your collection of <c>Heads</c>.

                <7>Players drop their <c>Heads</c> when killed
                <7>in <5>Corrupted Games</5>!

                <7>Total Heads: <a>0

                <8>Coming soon..."""));

        // Total Coins display (slot 50)
        layout.slot(50, (_, _) -> {
            String formattedCoins = NumberFormat.getNumberInstance(Locale.US).format(coins);
            return ItemStacks.item(Material.EMERALD, 1, """
                    <7>Total Coins: <6>{}
                    <6>https://store.hypixel.net""", formattedCoins);
        });

        // Close button (slot 49)
        Components.close(layout, 49);
    }

    private void layoutUpgradeItem(ViewLayout<DefaultState> layout,
                                   int slot,
                                   String upgradeId,
                                   DatapointSoulWellUpgrades.SoulWellUpgrades playerUpgrades,
                                   long coins) {
        SoulWellUpgrade upgrade = SoulWellUpgradeRegistry.getUpgrade(upgradeId);
        if (upgrade == null) return;

        int currentLevel = playerUpgrades.getUpgradeLevel(upgradeId);
        boolean isMaxed = upgrade.isMaxed(currentLevel);
        SoulWellUpgrade.SoulWellUpgradeTier nextTier = upgrade.getNextTier(currentLevel);

        layout.slot(slot,
                (_, _) -> {
                    List<Text> lore = new ArrayList<>();
                    lore.add(Text.of("<8>Permanent Upgrade"));
                    lore.add(Text.empty());
                    lore.add(Text.of("<7>{}", upgrade.baseDescription()));
                    lore.add(Text.empty());

                    String colorCode = upgrade.color();

                    if (isMaxed) {
                        SoulWellUpgrade.SoulWellUpgradeTier currentTier = upgrade.getTier(currentLevel);
                        if (currentTier != null) {
                            lore.add(Text.of("<7>Current: <color:{}>{} <7>{}",
                                    colorCode, currentTier.newEffect(), currentTier.effectDescription()));
                        }
                        lore.add(Text.empty());
                        lore.add(Text.of("<a>MAXED OUT!"));

                        return ItemStacks.item(upgrade.material(), 1,
                                Text.of("<color:{}>{} {:roman}", colorCode, upgrade.name(), currentLevel), lore);
                    }

                    if (nextTier != null) {
                        lore.add(nextTier.getEffectChangeLine());
                        lore.add(Text.empty());

                        String formattedCost = NumberFormat.getNumberInstance(Locale.US).format(nextTier.cost());
                        boolean canAfford = coins >= nextTier.cost();

                        lore.add(Text.of("<7>Cost: <6>{}", formattedCost));
                        lore.add(Text.empty());

                        if (canAfford) lore.add(Text.of("<e>Click to purchase!"));
                        else lore.add(Text.of("<c>You can't afford this!"));

                        Text displayName;
                        if (currentLevel == 0) {
                            displayName = Text.of("<color:{}>{}", colorCode, upgrade.name());
                        } else {
                            displayName = Text.of("<color:{}>{} {:roman} <l>→ </l>{:roman}",
                                    colorCode, upgrade.name(), currentLevel, currentLevel + 1);
                        }

                        return ItemStacks.item(upgrade.material(), 1, displayName, lore);
                    }

                    return ItemStacks.item(upgrade.material(), 1,
                            Text.of("<7>{}", upgrade.name()), lore);
                },
                (_, c) -> {
                    if (isMaxed) {
                        c.player().sendMessage("<c>This upgrade is already maxed out!");
                        return;
                    }
                    if (nextTier == null) {
                        c.player().sendMessage("<c>No upgrade tier found!");
                        return;
                    }

                    c.push(new GUISoulWellConfirm(upgrade, nextTier, currentLevel + 1));
                }
        );
    }

    private int getWheelCount(SkywarsDataHandler handler) {
        if (handler == null) return 3;
        DatapointSoulWellUpgrades.SoulWellUpgrades upgrades = handler.get(
                SkywarsDataHandler.Data.SOUL_WELL_UPGRADES, DatapointSoulWellUpgrades.class).getValue();
        return upgrades.getWheelCount();
    }

    private void setWheelCount(SkywarsDataHandler handler, int count) {
        if (handler == null) return;
        DatapointSoulWellUpgrades.SoulWellUpgrades upgrades = handler.get(
                SkywarsDataHandler.Data.SOUL_WELL_UPGRADES, DatapointSoulWellUpgrades.class).getValue();
        upgrades.setWheelCount(count);
    }
}
