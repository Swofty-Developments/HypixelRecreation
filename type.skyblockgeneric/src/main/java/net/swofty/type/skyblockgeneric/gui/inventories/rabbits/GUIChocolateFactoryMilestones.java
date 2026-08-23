package net.swofty.type.skyblockgeneric.gui.inventories.rabbits;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.StatefulView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory;
import net.swofty.type.skyblockgeneric.rabbits.ChocolateFactoryHelper;
import net.swofty.type.skyblockgeneric.rabbits.ChocolateMilestone;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIChocolateFactoryMilestones implements StatefulView<GUIChocolateFactoryMilestones.State> {
    private static final int PROGRESS_BAR_SEGMENTS = 25;
    private static final double PERCENT_PER_SEGMENT = 100.0 / PROGRESS_BAR_SEGMENTS;

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
        return ViewConfiguration.withString((state, ctx) -> "Chocolate Factory Milestones", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<State> layout, State state, ViewContext ctx) {
        Components.fill(layout);

        // Set milestone items
        for (ChocolateMilestone milestone : ChocolateMilestone.values()) {
            int slot = MILESTONE_SLOTS[milestone.getNumber() - 1];
            layout.slot(slot, (s, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                DatapointChocolateFactory.ChocolateFactoryData data = ChocolateFactoryHelper.getData(player);
                long allTimeChocolate = data.getChocolateAllTime();

                if (milestone.isUnlocked(allTimeChocolate)) {
                    return createUnlockedMilestoneItem(milestone);
                } else {
                    return createLockedMilestoneItem(milestone, allTimeChocolate);
                }
            });
        }

        // Go Back button (slot 48)
        Components.back(layout, 48, ctx);

        // Close button (slot 49)
        Components.close(layout, 49);
    }

    private ItemStack.Builder createUnlockedMilestoneItem(ChocolateMilestone milestone) {
        List<Text> lore = new ArrayList<>();
        String formattedReq = formatRequirement(milestone.getRequiredChocolate());

        lore.add(Text.of("<7>Reach <6>{} Chocolate <7>all-time to", formattedReq));
        lore.add(Text.of("<7>unlock this special <a>Chocolate Rabbit<7>!"));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Milestone {} Reward", milestone.getRomanNumeral()));
        lore.add(Text.of("<color:{}>{}", milestone.getColorCode(), milestone.getRabbitName()));
        lore.add(Text.empty());
        addRewardLore(lore, milestone);
        lore.add(Text.empty());
        lore.add(Text.of("<a><l>UNLOCKED"));

        return ItemStacks.head(milestone.getTextureId(), 1, getMilestoneName(milestone), lore);
    }

    private ItemStack.Builder createLockedMilestoneItem(ChocolateMilestone milestone, long allTimeChocolate) {
        List<Text> lore = new ArrayList<>();
        String formattedReq = formatRequirement(milestone.getRequiredChocolate());
        String formattedCurrent = formatRequirement(allTimeChocolate);
        double progress = milestone.getProgress(allTimeChocolate);

        lore.add(Text.of("<7>Reach <6>{} Chocolate <7>all-time to", formattedReq));
        lore.add(Text.of("<7>unlock this special <a>Chocolate Rabbit<7>!"));
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
        lore.add(Text.of("<c>Requires {} all-time Chocolate!", formattedReq));

        return ItemStacks.item(milestone.getGlassPaneMaterial(), 1, getMilestoneName(milestone), lore);
    }

    private Text createProgressBar(double progress, String current, String required) {
        int filled = (int) (progress / PERCENT_PER_SEGMENT);
        int empty = PROGRESS_BAR_SEGMENTS - filled;

        return Text.of("<3><l><m>{}<f>{}<r> <b>{}<3>/<b>{}",
                " ".repeat(Math.max(0, filled)), " ".repeat(Math.max(0, empty)), current, required);
    }

    private String formatRequirement(long amount) {
        if (amount >= 1_000_000_000_000L) {
            double val = amount / 1_000_000_000_000.0;
            return val == (long) val ? String.format("%.0fT", val) : String.format("%.1fT", val);
        } else if (amount >= 1_000_000_000L) {
            double val = amount / 1_000_000_000.0;
            return val == (long) val ? String.format("%.0fB", val) : String.format("%.1fB", val);
        } else if (amount >= 1_000_000L) {
            double val = amount / 1_000_000.0;
            return val == (long) val ? String.format("%.0fM", val) : String.format("%.1fM", val);
        } else if (amount >= 1_000L) {
            double val = amount / 1_000.0;
            return val == (long) val ? String.format("%.0fk", val) : String.format("%.1fk", val);
        }
        return String.valueOf(amount);
    }

    private String getOrdinal(int number) {
        String[] suffixes = {"th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th"};
        if (number % 100 >= 11 && number % 100 <= 13) {
            return number + "th";
        }
        return number + suffixes[number % 10];
    }

    private Text getMilestoneName(ChocolateMilestone milestone) {
        return Text.of("<color:{}>{} Chocolate Milestone", milestone.getColorCode(),
                getOrdinal(milestone.getNumber()));
    }

    private void addRewardLore(List<Text> lore, ChocolateMilestone milestone) {
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
}
