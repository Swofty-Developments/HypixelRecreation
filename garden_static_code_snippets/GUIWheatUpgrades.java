public class GUIWheatUpgrades extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Wheat Upgrades", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 49);

        layout.slot(4, ItemStackCreator.getStack(
                "§aWheat",
                Material.WHEAT,
                1,
                "§7Upgrade your §aWheat §7tier to increase",
                "§7your §6☘ Wheat Fortune§7.",
                "",
                "§7Current Tier: §e3§7/§a9",
                "§7Wheat Fortune: §6+15☘"
        ));
        layout.slot(18, ItemStackCreator.getStack(
                "§aWheat I",
                Material.LIME_STAINED_GLASS_PANE,
                1,
                "§6+5☘ Wheat Fortune",
                "§8+§b1 SkyBlock XP",
                "",
                "§a§lUNLOCKED"
        ));
        layout.slot(19, ItemStackCreator.getStack(
                "§aWheat II",
                Material.LIME_STAINED_GLASS_PANE,
                2,
                "§6+5☘ Wheat Fortune",
                "§8+§b1 SkyBlock XP",
                "",
                "§a§lUNLOCKED"
        ));
        layout.slot(20, ItemStackCreator.getStack(
                "§aWheat III",
                Material.LIME_STAINED_GLASS_PANE,
                3,
                "§6+5☘ Wheat Fortune",
                "§8+§b1 SkyBlock XP",
                "",
                "§a§lUNLOCKED"
        ));
        layout.slot(21, ItemStackCreator.getStack(
                "§eWheat IV",
                Material.YELLOW_STAINED_GLASS_PANE,
                4,
                "§6+5☘ Wheat Fortune",
                "§8+§b1 SkyBlock XP",
                "",
                "§7Costs",
                "§c50 Copper",
                "",
                "§eClick to unlock!"
        ));
        layout.slot(22, ItemStackCreator.getStack(
                "§cWheat V",
                Material.RED_STAINED_GLASS_PANE,
                5,
                "§6+5☘ Wheat Fortune",
                "§8+§b1 SkyBlock XP",
                "",
                "§7Costs",
                "§c100 Copper",
                "",
                "§cYou need to unlock the previous tier",
                "§cfirst!"
        ));
        layout.slot(23, ItemStackCreator.getStack(
                "§cWheat VI",
                Material.RED_STAINED_GLASS_PANE,
                6,
                "§6+5☘ Wheat Fortune",
                "§8+§b1 SkyBlock XP",
                "",
                "§7Costs",
                "§c500 Copper",
                "",
                "§cYou need to unlock the previous tier",
                "§cfirst!"
        ));
        layout.slot(24, ItemStackCreator.getStack(
                "§cWheat VII",
                Material.RED_STAINED_GLASS_PANE,
                7,
                "§6+5☘ Wheat Fortune",
                "§8+§b1 SkyBlock XP",
                "",
                "§7Costs",
                "§c1,000 Copper",
                "",
                "§cYou need to unlock the previous tier",
                "§cfirst!"
        ));
        layout.slot(25, ItemStackCreator.getStack(
                "§cWheat VIII",
                Material.RED_STAINED_GLASS_PANE,
                8,
                "§6+5☘ Wheat Fortune",
                "§8+§b1 SkyBlock XP",
                "",
                "§7Costs",
                "§c2,000 Copper",
                "",
                "§cYou need to unlock the previous tier",
                "§cfirst!"
        ));
        layout.slot(26, ItemStackCreator.getStack(
                "§cWheat IX",
                Material.RED_STAINED_GLASS_PANE,
                9,
                "§6+5☘ Wheat Fortune",
                "§8+§b1 SkyBlock XP",
                "",
                "§7Costs",
                "§c4,000 Copper",
                "",
                "§cYou need to unlock the previous tier",
                "§cfirst!"
        ));
        layout.slot(48, ItemStackCreator.getStack(
                "§aGo Back",
                Material.ARROW,
                1,
                "§7To Crop Upgrades"
        ));
    }
}
