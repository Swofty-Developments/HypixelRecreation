public class GUIComposter extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Composter", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 49);

        layout.slot(1, ItemStackCreator.getStack(
                "§eOrganic Matter",
                Material.RED_STAINED_GLASS_PANE,
                1,
                "§2§l§m  §f§l§m                   §e3,876.5§6/§e40k"
        ));
        layout.slot(7, ItemStackCreator.getStack(
                "§2Fuel",
                Material.RED_STAINED_GLASS_PANE,
                1,
                "§2§l§m                §f§l§m     §e79,000§6/§e100k"
        ));
        layout.slot(10, ItemStackCreator.getStack(
                "§eOrganic Matter",
                Material.RED_STAINED_GLASS_PANE,
                1,
                "§2§l§m  §f§l§m                   §e3,876.5§6/§e40k"
        ));
        layout.slot(13, ItemStackCreator.getStack(
                "§eCollect Compost",
                Material.RED_TERRACOTTA,
                1,
                "§7Compost Available: §a0"
        ));
        layout.slot(16, ItemStackCreator.getStack(
                "§2Fuel",
                Material.YELLOW_STAINED_GLASS_PANE,
                1,
                "§2§l§m                §f§l§m     §e79,000§6/§e100k"
        ));
        layout.slot(19, ItemStackCreator.getStack(
                "§eOrganic Matter",
                Material.RED_STAINED_GLASS_PANE,
                1,
                "§2§l§m  §f§l§m                   §e3,876.5§6/§e40k"
        ));
        layout.slot(22, ItemStackCreator.getStack(
                "§aComposter Upgrades",
                Material.HOPPER,
                1,
                "§7Upgrade your composter to increase",
                "§7your compost production.",
                "",
                "§eClick to view upgrades!"
        ));
        layout.slot(25, ItemStackCreator.getStack(
                "§2Fuel",
                Material.LIME_STAINED_GLASS_PANE,
                1,
                "§2§l§m                §f§l§m     §e79,000§6/§e100k"
        ));
        layout.slot(28, ItemStackCreator.getStack(
                "§eOrganic Matter",
                Material.RED_STAINED_GLASS_PANE,
                1,
                "§2§l§m  §f§l§m                   §e3,876.5§6/§e40k"
        ));
        layout.slot(34, ItemStackCreator.getStack(
                "§2Fuel",
                Material.LIME_STAINED_GLASS_PANE,
                1,
                "§2§l§m                §f§l§m     §e79,000§6/§e100k"
        ));
        layout.slot(37, ItemStackCreator.getStack(
                "§eOrganic Matter",
                Material.YELLOW_STAINED_GLASS_PANE,
                1,
                "§2§l§m  §f§l§m                   §e3,876.5§6/§e40k"
        ));
        layout.slot(39, ItemStackCreator.getStack(
                "§aInsert Crops from Sacks",
                Material.CAULDRON,
                1,
                "§7Grab as many crops that will fit into",
                "§7the composter from your sacks.",
                "",
                "§7In your sacks: §e33.9k Organic Matter",
                "",
                "§eLeft-click to grab from sacks!"
        ));
        layout.slot(41, ItemStackCreator.getStack(
                "§aInsert Fuel from Sacks",
                Material.CAULDRON,
                1,
                "§7Grab as much fuel that will fit into",
                "§7the composter from your sacks.",
                "",
                "§7In your sacks:",
                "  §a5§7x §aOil Barrel §7worth §250k Fuel",
                "",
                "§7Totalling §250k Fuel§7.",
                "",
                "§eLeft-click to grab from sacks!"
        ));
        layout.slot(43, ItemStackCreator.getStack(
                "§2Fuel",
                Material.LIME_STAINED_GLASS_PANE,
                1,
                "§2§l§m                §f§l§m     §e79,000§6/§e100k"
        ));
        layout.slot(46, ItemStackCreator.getStack(
                "§eCrop Meter",
                Material.POTATO,
                1,
                "§2§l§m  §f§l§m                   §e3,876.5§6/§e40k",
                "",
                "§7Fill your composter with §acrops§7, like",
                "§fWheat §7or §aEnchanted Potato§7, to turn",
                "§7them into §eOrganic Matter§7. Organic",
                "§7Matter is used to make §6Compost§7.",
                "",
                "§7The composter must have §b4,000",
                "§7organic matter stored to start",
                "§7making compost."
        ));
        layout.slot(48, ItemStackCreator.getStackHead(
                "§aInsert Crops from Inventory",
                "ef835b8941fe319931749b87fe8e84c5d1f4a271b5fbce5e700a60004d881f79",
                1,
                "§7Grab as many crops that will fit into",
                "§7the composter from your inventory.",
                "",
                "§7In your inventory: §cNo Organic Matter",
                "",
                "§cNo crops in your inventory!"
        ));
        layout.slot(50, ItemStackCreator.getStack(
                "§aInsert Fuel from Inventory",
                Material.GREEN_DYE,
                1,
                "§7Grab as much fuel that will fit into",
                "§7the composter from your inventory.",
                "",
                "§7In your inventory: §cNo Fuel",
                "",
                "§cNo fuel in your inventory!"
        ));
        layout.slot(52, ItemStackCreator.getStackHead(
                "§2Fuel Meter",
                "d5d2750595477ecc13869580b12ffc3b13fc2b3ac3e5035ecfc9aafa036722a2",
                1,
                "§2§l§m                §f§l§m     §e79,000§6/§e100k",
                "",
                "§7Fill your composter with §2machine fuel§7,",
                "§7like §9Biofuel§7 to power the composter",
                "§7to turn Organic Matter into §6Compost§7.",
                "",
                "§7The composter must have §22,000♢",
                "§2Fuel §7stored to start making compost."
        ));
    }
}
