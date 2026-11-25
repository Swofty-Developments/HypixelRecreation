public class GUIConfigurePlots extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Configure Plots", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 49);

        layout.slot(2, ItemStackCreator.getStack(
                "§ePlot §7- §b21",
                Material.OAK_BUTTON,
                1,
                "§7Requirement",
                "§a✔ Garden Level 7",
                "",
                "§7Cost:",
                "§9Compost Bundle §8x8",
                "",
                "§7Rewards:",
                "§8+§a3 §6☘ Farming Fortune",
                "§8+§b5 SkyBlock XP",
                "",
                "§cYou need more Compost to unlock this!"
        ));
        layout.slot(3, ItemStackCreator.getStack(
                "§aPlot §7- §b13",
                Material.CARVED_PUMPKIN,
                1,
                "§7Preset: §aPumpkin",
                "§4§lൠ §cThis plot has §21 ൠ Pest§c!",
                "",
                "§eLeft-click to modify!",
                "§eRight-click to teleport to this plot!"
        ));
        layout.slot(4, ItemStackCreator.getStack(
                "§ePlot §7- §b9",
                Material.OAK_BUTTON,
                1,
                "§7Requirement",
                "§a✔ Garden Level 5",
                "",
                "§7Cost:",
                "§9Compost Bundle §8x1",
                "",
                "§7Rewards:",
                "§8+§a3 §6☘ Farming Fortune",
                "§8+§b5 SkyBlock XP",
                "",
                "§cYou need more Compost to unlock this!"
        ));
        layout.slot(5, ItemStackCreator.getStack(
                "§ePlot §7- §b14",
                Material.OAK_BUTTON,
                1,
                "§7Requirement",
                "§a✔ Garden Level 5",
                "",
                "§7Cost:",
                "§9Compost Bundle §8x1",
                "",
                "§7Rewards:",
                "§8+§a3 §6☘ Farming Fortune",
                "§8+§b5 SkyBlock XP",
                "",
                "§cYou need more Compost to unlock this!"
        ));
        layout.slot(6, ItemStackCreator.getStack(
                "§ePlot §7- §b22",
                Material.RED_STAINED_GLASS_PANE,
                1,
                "§7Requirement",
                "§a✔ Garden Level 7",
                "",
                "§7Cost:",
                "§9Compost Bundle §8x8",
                "",
                "§7Rewards:",
                "§8+§a3 §6☘ Farming Fortune",
                "§8+§b5 SkyBlock XP",
                "",
                "§cYou need to unlock an adjacent plot",
                "§cfirst!"
        ));
        layout.slot(11, ItemStackCreator.getStack(
                "§ePlot §7- §b15",
                Material.OAK_BUTTON,
                1,
                "§7Requirement",
                "§a✔ Garden Level 5",
                "",
                "§7Cost:",
                "§9Compost Bundle §8x1",
                "",
                "§7Rewards:",
                "§8+§a3 §6☘ Farming Fortune",
                "§8+§b5 SkyBlock XP",
                "",
                "§cYou need more Compost to unlock this!"
        ));
        layout.slot(12, ItemStackCreator.getStack(
                "§aPlot §7- §b5",
                Material.CARVED_PUMPKIN,
                1,
                "§7Preset: §aPumpkin",
                "",
                "§eLeft-click to modify!",
                "§eRight-click to teleport to this plot!"
        ));
        layout.slot(13, ItemStackCreator.getStack(
                "§aPlot §7- §b1",
                Material.SUGAR_CANE,
                1,
                "§7Preset: §aSugar Cane",
                "§4§lൠ §cThis plot has §21 ൠ Pest§c!",
                "",
                "§eLeft-click to modify!",
                "§eRight-click to teleport to this plot!"
        ));
        layout.slot(14, ItemStackCreator.getStack(
                "§aPlot §7- §b6",
                Material.ROSE_BUSH,
                1,
                "§7Preset: §aWild Rose",
                "",
                "§eLeft-click to modify!",
                "§eRight-click to teleport to this plot!"
        ));
        layout.slot(15, ItemStackCreator.getStack(
                "§ePlot §7- §b16",
                Material.OAK_BUTTON,
                1,
                "§7Requirement",
                "§a✔ Garden Level 5",
                "",
                "§7Cost:",
                "§9Compost Bundle §8x1",
                "",
                "§7Rewards:",
                "§8+§a3 §6☘ Farming Fortune",
                "§8+§b5 SkyBlock XP",
                "",
                "§cYou need more Compost to unlock this!"
        ));
        layout.slot(20, ItemStackCreator.getStack(
                "§ePlot §7- §b10",
                Material.OAK_BUTTON,
                1,
                "§7Requirement",
                "§a✔ Garden Level 5",
                "",
                "§7Cost:",
                "§9Compost Bundle §8x1",
                "",
                "§7Rewards:",
                "§8+§a3 §6☘ Farming Fortune",
                "§8+§b5 SkyBlock XP",
                "",
                "§cYou need more Compost to unlock this!"
        ));
        layout.slot(21, ItemStackCreator.getStack(
                "§aPlot §7- §b2",
                Material.CARVED_PUMPKIN,
                1,
                "§7Preset: §aPumpkin",
                "§4§lൠ §cThis plot has §23 ൠ Pests§c!",
                "",
                "§eLeft-click to modify!",
                "§eRight-click to teleport to this plot!"
        ));
        layout.slot(22, ItemStackCreator.getStack(
                "§aThe Barn",
                Material.LIGHT_BLUE_TERRACOTTA,
                1,
                "",
                "§eRight-click to teleport to this plot!"
        ));
        layout.slot(23, ItemStackCreator.getStack(
                "§aPlot §7- §b3",
                Material.WHITE_STAINED_GLASS,
                1,
                "§7Greenhouse Plot",
                "",
                "§eLeft-click to modify!",
                "§eRight-click to teleport to this plot!"
        ));
        layout.slot(24, ItemStackCreator.getStack(
                "§ePlot §7- §b11",
                Material.OAK_BUTTON,
                1,
                "§7Requirement",
                "§a✔ Garden Level 5",
                "",
                "§7Cost:",
                "§9Compost Bundle §8x1",
                "",
                "§7Rewards:",
                "§8+§a3 §6☘ Farming Fortune",
                "§8+§b5 SkyBlock XP",
                "",
                "§cYou need more Compost to unlock this!"
        ));
        layout.slot(29, ItemStackCreator.getStack(
                "§ePlot §7- §b17",
                Material.OAK_BUTTON,
                1,
                "§7Requirement",
                "§a✔ Garden Level 5",
                "",
                "§7Cost:",
                "§9Compost Bundle §8x1",
                "",
                "§7Rewards:",
                "§8+§a3 §6☘ Farming Fortune",
                "§8+§b5 SkyBlock XP",
                "",
                "§cYou need more Compost to unlock this!"
        ));
        layout.slot(30, ItemStackCreator.getStack(
                "§aPlot §7- §b7",
                Material.CARVED_PUMPKIN,
                1,
                "§7Preset: §aPumpkin",
                "",
                "§eLeft-click to modify!",
                "§eRight-click to teleport to this plot!"
        ));
        layout.slot(31, ItemStackCreator.getStack(
                "§aPlot §7- §b4",
                Material.POTATO,
                1,
                "§7Preset: §aPotato",
                "§4§lൠ §cThis plot has §21 ൠ Pest§c!",
                "",
                "§eLeft-click to modify!",
                "§eRight-click to teleport to this plot!"
        ));
        layout.slot(32, ItemStackCreator.getStack(
                "§aPlot §7- §b8",
                Material.RED_MUSHROOM,
                1,
                "§7Preset: §aMushroom",
                "",
                "§eLeft-click to modify!",
                "§eRight-click to teleport to this plot!"
        ));
        layout.slot(33, ItemStackCreator.getStack(
                "§ePlot §7- §b18",
                Material.OAK_BUTTON,
                1,
                "§7Requirement",
                "§a✔ Garden Level 5",
                "",
                "§7Cost:",
                "§9Compost Bundle §8x1",
                "",
                "§7Rewards:",
                "§8+§a3 §6☘ Farming Fortune",
                "§8+§b5 SkyBlock XP",
                "",
                "§cYou need more Compost to unlock this!"
        ));
        layout.slot(38, ItemStackCreator.getStack(
                "§ePlot §7- §b23",
                Material.OAK_BUTTON,
                1,
                "§7Requirement",
                "§a✔ Garden Level 7",
                "",
                "§7Cost:",
                "§9Compost Bundle §8x8",
                "",
                "§7Rewards:",
                "§8+§a3 §6☘ Farming Fortune",
                "§8+§b5 SkyBlock XP",
                "",
                "§cYou need more Compost to unlock this!"
        ));
        layout.slot(39, ItemStackCreator.getStack(
                "§aPlot §7- §b19",
                Material.CARVED_PUMPKIN,
                1,
                "§7Preset: §aPumpkin",
                "§4§lൠ §cThis plot has §21 ൠ Pest§c!",
                "",
                "§eLeft-click to modify!",
                "§eRight-click to teleport to this plot!"
        ));
        layout.slot(40, ItemStackCreator.getStack(
                "§aPlot §7- §b12",
                Material.POTATO,
                1,
                "§7Preset: §aPotato",
                "§4§lൠ §cThis plot has §21 ൠ Pest§c!",
                "",
                "§eLeft-click to modify!",
                "§eRight-click to teleport to this plot!"
        ));
        layout.slot(41, ItemStackCreator.getStack(
                "§ePlot §7- §b20",
                Material.OAK_BUTTON,
                1,
                "§7Requirement",
                "§a✔ Garden Level 5",
                "",
                "§7Cost:",
                "§9Compost Bundle §8x1",
                "",
                "§7Rewards:",
                "§8+§a3 §6☘ Farming Fortune",
                "§8+§b5 SkyBlock XP",
                "",
                "§cYou need more Compost to unlock this!"
        ));
        layout.slot(42, ItemStackCreator.getStack(
                "§ePlot §7- §b24",
                Material.RED_STAINED_GLASS_PANE,
                1,
                "§7Requirement",
                "§a✔ Garden Level 7",
                "",
                "§7Cost:",
                "§9Compost Bundle §8x8",
                "",
                "§7Rewards:",
                "§8+§a3 §6☘ Farming Fortune",
                "§8+§b5 SkyBlock XP",
                "",
                "§cYou need to unlock an adjacent plot",
                "§cfirst!"
        ));
        layout.slot(48, ItemStackCreator.getStack(
                "§aGo Back",
                Material.ARROW,
                1,
                "§7To Desk"
        ));
    }
}
