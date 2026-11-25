public class GUISkyMart extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("SkyMart", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 31);

        layout.slot(11, ItemStackCreator.getStack(
                "§aFarming Essentials",
                Material.GREEN_DYE,
                1,
                "§7All the farming supplies you could",
                "§7ever need!",
                "",
                "§eClick to view!"
        ));
        layout.slot(12, ItemStackCreator.getStack(
                "§aFarming Tools",
                Material.DIAMOND_HOE,
                1,
                "§7Purchase tools made specifically for",
                "§7each crop!",
                "",
                "§eClick to view!"
        ));
        layout.slot(13, ItemStackCreator.getStack(
                "§aBarn Skins",
                Material.OAK_PLANKS,
                1,
                "§7Spruce up your Barn!",
                "",
                "§eClick to view!"
        ));
        layout.slot(14, ItemStackCreator.getStack(
                "§aGreenhouse Skins",
                Material.WHITE_STAINED_GLASS,
                1,
                "§7Make your Greenhouse look fresh!",
                "",
                "§eClick to view!"
        ));
        layout.slot(15, ItemStackCreator.getStack(
                "§aPests",
                Material.CHEST_MINECART,
                1,
                "§7Got pests? We got you.",
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
