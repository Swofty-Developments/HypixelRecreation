public class GUIGardenUpgrades extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Garden Upgrades", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 31);

        layout.slot(11, ItemStackCreator.getStack(
                "§aCrop Upgrades",
                Material.WHEAT,
                1,
                "§7Increase your §6☘ Farming Fortune",
                "§7for each crop by upgrading them!",
                "",
                "§eClick to view!"
        ));
        layout.slot(15, ItemStackCreator.getStack(
                "§aGreenhouse Upgrades",
                Material.WHITE_STAINED_GLASS,
                1,
                "§7Upgrade your Greenhouse to",
                "§7improve its growth and yield.",
                "",
                "§eClick to view!"
        ));
        layout.slot(30, ItemStackCreator.getStack(
                "§aGo Back",
                Material.ARROW,
                1,
                "§7To Desk"
        ));
    }
}
