package net.swofty.type.garden.gui;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.type.garden.config.GardenConfigRegistry;
import net.swofty.type.garden.config.GardenPlotDefinition;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.garden.GardenData;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Map;

public class GUIConfigurePlots extends StatelessView {
    private static final int[] PLOT_SLOTS = {
        2, 3, 4, 5, 6,
        11, 12, 13, 14, 15,
        20, 21, 22, 23, 24,
        29, 30, 31, 32, 33,
        38, 39, 40, 41, 42
    };

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Configure Plots", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 49);

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        GardenData.GardenCoreData core = GardenGuiSupport.core(player);
        List<GardenPlotDefinition> plots = GardenGuiSupport.lockedPlotsFirst(player);
        for (int i = 0; i < Math.min(PLOT_SLOTS.length, plots.size()); i++) {
            GardenPlotDefinition plot = plots.get(i);
            int slot = PLOT_SLOTS[i];
            layout.slot(slot, buildPlotItem(player, core, plot));
        }
    }

    private net.minestom.server.item.ItemStack.Builder buildPlotItem(SkyBlockPlayer player, GardenData.GardenCoreData core, GardenPlotDefinition plot) {
        boolean unlocked = plot.defaultUnlocked() || core.getUnlockedPlots().contains(plot.id());
        Material material = unlocked ? plotMaterial(core, plot) : Material.OAK_BUTTON;
        String plotNumber = plot.id().replaceAll("\\D+", "");
        String displayNumber = plotNumber.isBlank() ? plot.id().toUpperCase() : plotNumber;

        if ("central".equalsIgnoreCase(plot.id())) {
            return ItemStackCreator.getStack(
                "§aThe Barn",
                Material.LIGHT_BLUE_TERRACOTTA,
                1,
                "",
                "§eRight-click to teleport to this plot!"
            );
        }

        if (!unlocked) {
            long copperLikeCost = plotCost(plot);
            return ItemStackCreator.getStack(
                "§ePlot §7- §b" + displayNumber,
                material,
                1,
                "§7Requirement",
                "§a✔ Garden Level " + plot.gardenLevel(),
                "",
                "§7Cost:",
                costText(copperLikeCost),
                "",
                "§7Rewards:",
                "§8+§a3 §6☘ Farming Fortune",
                "§8+§b5 SkyBlock XP",
                "",
                "§cYou need more Compost to unlock this!"
            );
        }

        String preset = StringUtility.toNormalCase(core.getPlotPresets().getOrDefault(plot.id(), plot.displayName()));
        double cleaned = core.getCleanedPlots().getOrDefault(plot.id(), 0D);
        return ItemStackCreator.getStack(
            "§aPlot §7- §b" + displayNumber,
            material,
            1,
            "§7Preset: §a" + preset,
            cleaned >= 70D ? "§aCleaned: §e" + String.format("%.1f", cleaned) + "%" : "§7Cleaned: §e" + String.format("%.1f", cleaned) + "%",
            "",
            "§eLeft-click to modify!",
            "§eRight-click to teleport to this plot!"
        );
    }

    private Material plotMaterial(GardenData.GardenCoreData core, GardenPlotDefinition plot) {
        String preset = core.getPlotPresets().getOrDefault(plot.id(), "").toUpperCase();
        return switch (preset) {
            case "SUGAR_CANE" -> Material.SUGAR_CANE;
            case "POTATO" -> Material.POTATO;
            case "MUSHROOM" -> Material.RED_MUSHROOM;
            case "WILD_ROSE" -> Material.ROSE_BUSH;
            case "GREENHOUSE" -> Material.WHITE_STAINED_GLASS;
            default -> Material.CARVED_PUMPKIN;
        };
    }

    private long plotCost(GardenPlotDefinition plot) {
        Map<String, Object> config = GardenConfigRegistry.getConfig("plots.yml");
        for (Map<String, Object> rawPlot : GardenConfigRegistry.getMapList(config, "plots")) {
            if (plot.id().equalsIgnoreCase(GardenConfigRegistry.getString(rawPlot, "id", ""))) {
                return GardenConfigRegistry.getLong(rawPlot, "cost", 0L);
            }
        }
        return 0L;
    }

    private String costText(long cost) {
        if (cost <= 0L) {
            return "§aFree";
        }
        if (cost >= 1_000L) {
            return "§9Compost Bundle §8x" + (cost / 1_000L);
        }
        return "§aCompost §8x" + cost;
    }
}
