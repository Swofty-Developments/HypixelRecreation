package net.swofty.type.skyblockgeneric.gui.inventories.centauri;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.HypixelSignGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.View;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

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
            layout.slot(SLOTS[i], (s, c) -> ItemStacks.item(material, 1, Text.of("<a>Generate {} Coins", StringUtility.shortenNumber(amount)), List.of(
                    Text.literal(" "), Text.of("<7>Generates a pre-defined amount of"), Text.of("<7>coins that are immediately deposited"),
                    Text.of("<7>into your purse free of charge."), Text.literal(" "), Text.of("<e>Click to generate!"))),
                    (click, c) -> award((SkyBlockPlayer) c.player(), amount));
        }
        layout.slot(31, (s, c) -> GUICentauri.item("<a>Custom Amount", Material.OAK_SIGN,
                "<7>Creates a custom order of coins", "<7>that will magically appear into your",
                "<7>purse free of charge.", " ", "<e>Click to generate!"), (click, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            new HypixelSignGUI(player).open(new String[]{"Enter amount", ""}).thenAccept(input -> {
                if (input == null) return;
                try {
                    double amount = Double.parseDouble(input.replace(",", ""));
                    if (!Double.isFinite(amount) || amount < 0) throw new NumberFormatException();
                    award(player, amount);
                } catch (NumberFormatException ignored) {
                    player.sendMessage("<c>Please enter a valid positive number.");
                }
            });
        });
        Components.back(layout, 48, ctx);
        Components.close(layout, 49);
    }

    private static void award(SkyBlockPlayer player, double amount) {
        player.addCoins(amount);
        player.sendMessage("<a>Generated <6>{:,} coins<a>!", amount);
    }

    public record State() {}
}
