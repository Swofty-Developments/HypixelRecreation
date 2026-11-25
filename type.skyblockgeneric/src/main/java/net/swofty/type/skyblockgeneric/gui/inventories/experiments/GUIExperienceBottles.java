package net.swofty.type.skyblockgeneric.gui.inventories.experiments;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.ExperienceBottleComponent;

import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public final class GUIExperienceBottles extends StatelessView {
    private static final Bottle[] BOTTLES = {
            new Bottle(11, Text.of("<f>Experience Bottle"), ItemType.EXPERIENCE_BOTTLE, 8),
            new Bottle(12, Text.of("<a>Grand Experience Bottle"), ItemType.GRAND_EXP_BOTTLE, 1_500),
            new Bottle(14, Text.of("<9>Titanic Experience Bottle"), ItemType.TITANIC_EXP_BOTTLE, 250_000),
            new Bottle(15, Text.of("<5>Colossal Experience Bottle"), ItemType.COLOSSAL_EXP_BOTTLE, 500_000)
    };

    private final Map<ItemType, Double> bazaarPrices = new ConcurrentHashMap<>();

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.literal("Bottles of Enchanting"), InventoryType.CHEST_4_ROW);
    }

    @Override
    public void onOpen(DefaultState state, ViewContext ctx) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        for (Bottle bottle : BOTTLES) {
            player.getBazaarConnector().getItemStatistics(bottle.itemType()).thenAccept(statistics -> {
                bazaarPrices.put(bottle.itemType(), statistics.bestAsk() > 0
                        ? statistics.bestAsk() * 1.04 : -1D);
                ctx.session(DefaultState.class).refresh();
            }).exceptionally(exception -> {
                bazaarPrices.put(bottle.itemType(), -1D);
                ctx.session(DefaultState.class).refresh();
                return null;
            });
        }
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        for (Bottle bottle : BOTTLES) {
            layout.slot(bottle.slot(), (s, c) -> bottleItem(bottle, (SkyBlockPlayer) c.player()),
                    (click, c) -> buy((SkyBlockPlayer) c.player(), bottle));
        }
        if (!Components.back(layout, 30, ctx)) Components.close(layout, 30);
        Components.close(layout, 31);
    }

    private ItemStack.Builder bottleItem(Bottle bottle, SkyBlockPlayer player) {
        long experience = Math.round(bottle.baseExperience() * (1 + player.getSkills()
                .getCurrentLevel(net.swofty.type.skyblockgeneric.skill.SkillCategories.ENCHANTING) * .05));
        int currentLevel = player.getLevel();
        int appliedLevel = levelForExperience(player.getExperience() + experience);
        Double price = bazaarPrices.get(bottle.itemType());
        Text priceText = price == null ? Text.of("<e>Checking...")
                : price < 0 ? Text.of("<c>No Bazaar offers")
                : Text.of("<6>{} Coins", StringUtility.commaify(price));
        return ItemStacks.item(Material.EXPERIENCE_BOTTLE, 1, bottle.name(), List.of(
                Text.of("<7>Grants <3>{} <7>experience orbs. Buying", StringUtility.commaify(experience)),
                Text.of("<7>this directly will instantly consume it."),
                Text.empty(),
                Text.of("<7>Your Exp Level: <3>{}", currentLevel),
                Text.of("<7>Level When Applied: <3>{}", appliedLevel),
                Text.empty(),
                Text.of("<7>Cost"),
                Text.of("<7>Bazaar Price"),
                priceText,
                Text.empty(),
                Text.of("<e>Click to consume or buy!")
        ));
    }

    private void buy(SkyBlockPlayer player, Bottle bottle) {
        List<SkyBlockItem> consumed = player.takeItem(bottle.itemType(), 1);
        if (consumed != null && !consumed.isEmpty()) {
            long experience = experienceFrom(bottle, consumed.getFirst(), player);
            player.addExperience(experience);
            player.sendMessage(Text.of("<a>You consumed {}<a> and gained <3>{} experience<a>.",
                    bottle.name(), StringUtility.commaify(experience)));
            return;
        }

        Double price = bazaarPrices.get(bottle.itemType());
        if (price == null) {
            player.sendMessage("<c>The Bazaar price is still loading. Try again in a moment.");
            return;
        }
        if (price < 0) {
            player.sendMessage("<c>There are no Bazaar offers for that bottle right now.");
            return;
        }
        if (price > player.getCoins()) {
            player.sendMessage("<c>You don't have enough coins for that bottle.");
            return;
        }
        if (player.maxItemFit(bottle.itemType()) < 1) {
            player.sendMessage("<c>You don't have enough inventory space for that bottle.");
            return;
        }

        player.getBazaarConnector().instantBuy(bottle.itemType(), 1).thenAccept(result -> {
            player.sendMessage(Text.of(result.success() ? "<a>{}" : "<c>{}", result.message()));
            if (result.success()) player.closeInventory();
        });
    }

    private static long experienceFrom(Bottle bottle, SkyBlockItem item, SkyBlockPlayer player) {
        int baseExperience = item.hasComponent(ExperienceBottleComponent.class)
                ? item.getComponent(ExperienceBottleComponent.class).getBaseExperience()
                : Math.toIntExact(bottle.baseExperience());
        int level = player.getSkills().getCurrentLevel(net.swofty.type.skyblockgeneric.skill.SkillCategories.ENCHANTING);
        return Math.round(baseExperience * (1 + level * .05));
    }

    private static int levelForExperience(long value) {
        if (value <= 352) return (int) (Math.sqrt(value + 9) - 3);
        if (value <= 1_507) return (int) (8.1 + Math.sqrt((2.0 / 5.0) * (value - 195.975)));
        return (int) (18.0555 + Math.sqrt((2.0 / 9.0) * (value - 752.9861)));
    }

    private record Bottle(int slot, Text name, ItemType itemType, long baseExperience) {
    }
}
