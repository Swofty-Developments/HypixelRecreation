public class GUIGreenhouseSkins extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Greenhouse Skins", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 49);

        layout.slot(10, ItemStackCreator.getStack(
                "§fDefault",
                Material.WHITE_STAINED_GLASS,
                1,
                "§7Select this skin for your Greenhouse!",
                "",
                "§f§lCOMMON COSMETIC",
                "",
                "§eClick to select!"
        ));
        layout.slot(48, ItemStackCreator.getStack(
                "§aGo Back",
                Material.ARROW,
                1,
                "§7To Garden Skins"
        ));
    }
}
