public class GUIGardenSkins extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Garden Skins", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 31);

        layout.slot(11, ItemStackCreator.getStack(
                "§aBarn Skins",
                Material.OAK_PLANKS,
                1,
                "§7View and select different skins for",
                "§7your Barn.",
                "",
                "§eClick to view!"
        ));
        layout.slot(15, ItemStackCreator.getStack(
                "§aGreenhouse Skins",
                Material.WHITE_STAINED_GLASS,
                1,
                "§7View and select different skins for",
                "§7your Greenhouse.",
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
