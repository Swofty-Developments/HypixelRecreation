package net.swofty.type.skyblockgeneric.gui.inventories.centauri;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.type.generic.gui.HypixelSignGUI;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.View;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public final class GUICentauriCoinGenerator implements View<GUICentauriCoinGenerator.State> {
    private static final double[] AMOUNTS = {1_000, 10_000, 100_000, 1_000_000, 10_000_000, 100_000_000, 1_000_000_000};
    private static final int[] SLOTS = {11, 13, 15, 19, 21, 23, 25};
    private static final Material[] MATERIALS = {Material.GOLD_NUGGET, Material.GOLD_INGOT, Material.GOLD_BLOCK,
            Material.DIAMOND, Material.DIAMOND_BLOCK, Material.EMERALD, Material.EMERALD_BLOCK};

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withString((state, ctx) -> "Coin Generator", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.fill(layout);
        for (int i = 0; i < AMOUNTS.length; i++) {
            double amount = AMOUNTS[i];
            Material material = MATERIALS[i];
            layout.slot(SLOTS[i], (s, c) -> GUICentauri.item("§aGenerate " + StringUtility.shortenNumber(amount) + " Coins", material,
                    " ", "§7Generates a pre-defined amount of", "§7coins that are immediately deposited",
                    "§7into your purse free of charge.", " ", "§eClick to generate!"),
                    (click, c) -> award((SkyBlockPlayer) c.player(), amount));
        }
        layout.slot(31, (s, c) -> GUICentauri.item("§aCustom Amount", Material.OAK_SIGN,
                "§7Creates a custom order of coins", "§7that will magically appear into your",
                "§7purse free of charge.", " ", "§eClick to generate!"), (click, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            new HypixelSignGUI(player).open(new String[]{"Enter amount", ""}).thenAccept(input -> {
                if (input == null) return;
                try {
                    double amount = Double.parseDouble(input.replace(",", ""));
                    if (!Double.isFinite(amount) || amount < 0) throw new NumberFormatException();
                    award(player, amount);
                } catch (NumberFormatException ignored) {
                    player.sendMessage("§cPlease enter a valid positive number.");
                }
            });
        });
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
    }

    private static void award(SkyBlockPlayer player, double amount) {
        player.addCoins(amount);
        player.sendMessage("§aGenerated §6" + StringUtility.commaify(amount) + " coins§a!");
    }

    public record State() {}
}
