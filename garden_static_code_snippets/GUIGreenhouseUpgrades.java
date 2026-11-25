public class GUIGreenhouseUpgrades extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Greenhouse Upgrades", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 31);

        layout.slot(11, ItemStackCreator.getStack(
                "§aGrowth Speed",
                Material.WHEAT_SEEDS,
                1,
                "§7Upgrade your §aGrowth Speed §7tier to",
                "§7increase your §bGreenhouse Growth",
                "§bSpeed§7.",
                "",
                "§7Current Tier: §e4§7/§a9",
                "§7Growth Speed: §b20%",
                "",
                "§eClick to view!"
        ));
        layout.slot(13, ItemStackCreator.getStack(
                "§aPlant Yield",
                Material.FLOWER_POT,
                1,
                "§7Upgrade your §aPlant Yield §7tier to",
                "§7increase your §eGreenhouse Plant",
                "§eYield§7.",
                "",
                "§7Current Tier: §e2§7/§a9",
                "§7Plant Yield: §e4%",
                "",
                "§eClick to view!"
        ));
        layout.slot(15, ItemStackCreator.getStack(
                "§cPlot Limit",
                Material.GRAY_DYE,
                1,
                "§7Unlock all Greenhouse crop slots to",
                "§7use this!"
        ));
        layout.slot(30, ItemStackCreator.getStack(
                "§aGo Back",
                Material.ARROW,
                1,
                "§7To Garden Upgrades"
        ));
        layout.slot(32, ItemStackCreator.getStack(
                "§6Crop Slots",
                Material.COARSE_DIRT,
                1,
                "§7The Greenhouse has a specific",
                "§7amount of area for crops to grow in.",
                "§7The grid below shows where crops",
                "§7can be planted and grow.",
                "",
                "§7To unlock a slot, right-click it with an",
                "§5Ethereal Vine §7in your hand.",
                "",
                "§7Your Slots:",
                " §c██████████",
                " §c██████████",
                " §c██████████",
                " §c████§a██§c████",
                " §c███§a████§c███",
                " §c███§a████§c███",
                " §c████§a██§c████",
                " §c██████████",
                " §c██████████",
                " §c██████████"
        ));
    }
}
