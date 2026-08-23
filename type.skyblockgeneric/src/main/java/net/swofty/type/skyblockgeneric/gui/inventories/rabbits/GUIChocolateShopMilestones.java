package net.swofty.type.skyblockgeneric.gui.inventories.rabbits;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.StatefulView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory;
import net.swofty.type.skyblockgeneric.rabbits.ChocolateFactoryHelper;
import net.swofty.type.skyblockgeneric.rabbits.ChocolateShopMilestone;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIChocolateShopMilestones implements StatefulView<GUIChocolateShopMilestones.State> {
    private static final int[] MILESTONE_SLOTS = {
            27, 18, 9, 0, 1, 2, 11, 20, 29, 30, 31, 22,
            13, 4, 5, 6, 15, 24, 33, 34, 35, 26, 17, 8
    };

    public record State() {
    }

    @Override
    public State initialState() {
        return new State();
    }

    @Override
    public ViewConfiguration<State> configuration() {
        return ViewConfiguration.withString((state, ctx) -> "Chocolate Shop Milestones", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.fill(layout);

        // Set milestone items
        for (ChocolateShopMilestone milestone : ChocolateShopMilestone.values()) {
            int slot = MILESTONE_SLOTS[milestone.getNumber() - 1];
            layout.slot(slot, (s, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                DatapointChocolateFactory.ChocolateFactoryData data = ChocolateFactoryHelper.getData(player);
                long totalSpent = data.getTotalChocolateSpent();

                if (milestone.isUnlocked(totalSpent)) {
                    return createUnlockedMilestoneItem(milestone);
                } else {
                    return createLockedMilestoneItem(milestone, totalSpent);
                }
            });
        }

        // Go Back button (slot 48)
        Components.back(layout, 48, ctx);

        // Close button (slot 49)
        Components.close(layout, 49);
    }

    private ItemStack.Builder createUnlockedMilestoneItem(ChocolateShopMilestone milestone) {
        List<Text> lore = new ArrayList<>();
        String formattedReq = formatRequirement(milestone.getRequiredSpent());

        lore.add(Text.of("<7>Spend <6>{} Chocolate <7>in the shop", formattedReq));
        lore.add(Text.of("<7>to unlock this special <a>Chocolate Rabbit<7>!"));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Milestone {} Reward", milestone.getRomanNumeral()));
        lore.add(Text.of("<color:{}>{}", milestone.getColorCode(), milestone.getRabbitName()));
        lore.add(Text.empty());
        addRewardLore(lore, milestone);
        lore.add(Text.empty());
        lore.add(Text.of("<a><l>UNLOCKED"));

        return ItemStacks.head(milestone.getTextureId(), 1, getMilestoneName(milestone), lore);
    }

    private ItemStack.Builder createLockedMilestoneItem(ChocolateShopMilestone milestone, long totalSpent) {
        List<Text> lore = new ArrayList<>();
        String formattedReq = formatRequirement(milestone.getRequiredSpent());
        String formattedCurrent = formatRequirement(totalSpent);
        double progress = milestone.getProgress(totalSpent);

        lore.add(Text.of("<7>Spend <6>{} Chocolate <7>in the shop", formattedReq));
        lore.add(Text.of("<7>to unlock this special <a>Chocolate Rabbit<7>!"));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Progress to Milestone {}: <b>{}%", milestone.getRomanNumeral(),
                String.format("%.0f", progress)));
        lore.add(createProgressBar(progress, formattedCurrent, formattedReq));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Milestone {} Reward", milestone.getRomanNumeral()));
        lore.add(Text.of("<color:{}>{}", milestone.getColorCode(), milestone.getRabbitName()));
        lore.add(Text.empty());
        addRewardLore(lore, milestone);
        lore.add(Text.empty());
        lore.add(Text.of("<c>Requires spending {} Chocolate!", formattedReq));

        return ItemStacks.item(milestone.getGlassPaneMaterial(), 1, getMilestoneName(milestone), lore);
    }

    private void addRewardLore(List<Text> lore, ChocolateShopMilestone milestone) {
        if (milestone.getChocolateBonus() > 0) {
            lore.add(Text.of("<7>Grants <6>+{} Chocolate <7>and <6>{}", milestone.getChocolateBonus(),
                    String.format("%.3fx", milestone.getMultiplierBonus())));
            lore.add(Text.of("<6>Chocolate <7>per second to your"));
            lore.add(Text.of("<6>Chocolate Factory<7>."));
            return;
        }

        lore.add(Text.of("<7>Grants <6>+{} Chocolate <7>per second",
                String.format("%.2fx", milestone.getMultiplierBonus())));
        lore.add(Text.of("<7>to your <6>Chocolate Factory<7>."));
    }

    private Text getMilestoneName(ChocolateShopMilestone milestone) {
        return Text.of("<color:{}>{} Shop Milestone", milestone.getColorCode(),
                StringUtility.commaifyAndTh(milestone.getNumber()));
    }

    private Text createProgressBar(double progress, String current, String required) {
        int filled = (int) (progress / 4);
        int empty = 25 - filled;

        return Text.of("<3><l><m>{}<f>{}<r> <b>{}<3>/<b>{}",
                " ".repeat(Math.max(0, filled)), " ".repeat(Math.max(0, empty)), current, required);
    }

    private String formatRequirement(long amount) {
        int magnitude = amount >= 1_000_000_000_000L ? 4
                : amount >= 1_000_000_000L ? 3
                  : amount >= 1_000_000L ? 2
                    : amount >= 1_000L ? 1
                      : 0;
        // pyramid

        return switch (magnitude) {
            case 4 -> formatScaled(amount, 1_000_000_000_000.0, "T");
            case 3 -> formatScaled(amount, 1_000_000_000.0, "B");
            case 2 -> formatScaled(amount, 1_000_000.0, "M");
            case 1 -> formatScaled(amount, 1_000.0, "k");
            default -> Long.toString(amount);
        };
    }

    private String formatScaled(long amount, double divisor, String suffix) {
        double value = amount / divisor;

        return value == (long) value
                ? String.format("%.0f%s", value, suffix)
                : String.format("%.1f%s", value, suffix);
    }

}
