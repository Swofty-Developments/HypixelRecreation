package net.swofty.type.skyblockgeneric.gui.inventories.experiments;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.skyblockgeneric.rngmeter.RNGMeterDefinition;
import net.swofty.type.skyblockgeneric.rngmeter.RNGMeterReward;
import net.swofty.type.skyblockgeneric.rngmeter.RNGMeterService;
import net.swofty.type.skyblockgeneric.rngmeter.RNGMeterState;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class RNGMeterGuiSupport {
    private RNGMeterGuiSupport() {
    }

    public static ItemStack.Builder meterItem(RNGMeterDefinition definition, SkyBlockPlayer player) {
        RNGMeterState meter = RNGMeterService.get(player, definition);
        RNGMeterReward selected = selectedReward(definition, meter);
        List<String> lore = new ArrayList<>(List.of(
                "<7>Your <d>" + definition.displayName() + " RNG",
                "<d>Meter <7>fills with <3>" + definition.progressName() + " <7>every",
                "<7>time you complete your <d>" + definition.activityName() + "<7>!",
                "<7>",
                "<7>Your selected <d>RNG Drop <7>will be",
                "<7>guaranteed to appear in your next",
                "<d>" + definition.activityName() + " <7>once your RNG Meter is",
                "<7>full!",
                "",
                "<7>Selected Drop",
                selected == null ? "<c>None" : selected.displayName(),
                "",
                "<7>Progress: <d>" + progressPercent(meter, selected) + "<5>%",
                progressBar(meter, selected),
                "",
                "<e>Click to view!"
        ));
        return icon(definition, Text.of("<d>" + definition.displayName() + " RNG Meter"),
                lore.stream().map(Text::of).toList());
    }

    public static ItemStack.Builder icon(RNGMeterDefinition definition, Text name, List<Text> lore) {
        if (definition.iconTexture() == null) return ItemStacks.item(Material.MAGENTA_DYE, 1, name, lore);
        return ItemStacks.head(definition.iconTexture(), 1, name, lore);
    }

    public static RNGMeterReward selectedReward(RNGMeterDefinition definition, RNGMeterState state) {
        if (state.selectedReward().isBlank()) return null;
        return definition.rewards().stream()
                .filter(reward -> reward.id().equalsIgnoreCase(state.selectedReward()))
                .findFirst()
                .orElse(null);
    }

    public static String progressPercent(RNGMeterState state, RNGMeterReward reward) {
        if (reward == null || reward.requiredXp() <= 0) return "0.0";
        return StringUtility.decimalify(Math.min(100, state.storedXp() * 100d / reward.requiredXp()), 1);
    }

    public static String progressBar(RNGMeterState state, RNGMeterReward reward) {
        double progress = reward == null || reward.requiredXp() <= 0
                ? 0 : Math.min(1, state.storedXp() / reward.requiredXp());
        int filled = (int) Math.round(progress * 23);
        return "<d><m>" + " ".repeat(filled) + "<f><m>" + " ".repeat(23 - filled)
                + " <d>" + number(state.storedXp()) + "<5>/" + shortNumber(reward == null ? 0 : reward.requiredXp());
    }

    public static List<Text> rewardLore(RNGMeterDefinition definition, RNGMeterState state, RNGMeterReward reward) {
        double chance = state.selectedReward().equalsIgnoreCase(reward.id())
                ? RNGMeterService.applyDropRate(state, reward, reward.loot().baseChancePercent())
                : reward.loot().baseChancePercent();
        String chanceText = String.format(Locale.ROOT, "%.4f", chance);
        List<Text> lore = new ArrayList<>(List.of(
                Text.of("<7>Odds: {} <7>({}%)", reward.loot().rarity().displayName(), chanceText),
                Text.empty()
        ));

        if (state.selectedReward().equalsIgnoreCase(reward.id())) {
            lore.add(Text.of("<7>Progress: <d>{}<5>%", progressPercent(state, reward)));
            lore.add(Text.of(progressBar(state, reward)));
            lore.add(Text.empty());
            lore.add(Text.of("<7>Filling the meter increases the drop"));
            lore.add(Text.of("<7>chance of this item. Reaching <a>100%"));
            lore.add(Text.of("<7>will guarantee it to drop!"));
            lore.add(Text.empty());
            lore.add(Text.of("<a><l>SELECTED"));
        } else {
            lore.add(Text.of("<7>" + definition.rewardProgressName() + ": <d>{}<5>/<d>{}",
                    number(state.storedXp()), number(reward.requiredXp())));
            lore.add(Text.empty());
            lore.add(Text.of("<e>Click to select!"));
        }
        return lore;
    }

    private static String number(double value) {
        return StringUtility.commaify(Math.max(0, Math.round(value)));
    }

    private static String shortNumber(double value) {
        if (value >= 1_000_000) return compact(value / 1_000_000, "m");
        if (value >= 1_000) return compact(value / 1_000, "k");
        return number(value);
    }

    private static String compact(double value, String suffix) {
        String number = String.format(Locale.ROOT, "%.1f", value);
        if (number.endsWith(".0")) number = number.substring(0, number.length() - 2);
        return number + suffix;
    }
}
