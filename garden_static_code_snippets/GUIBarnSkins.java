public class GUIBarnSkins extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Barn Skins", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 49);

        layout.slot(10, ItemStackCreator.getStack(
                "§fDefault",
                Material.DARK_OAK_PLANKS,
                1,
                "§7Select this skin for your Barn!",
                "",
                "§f§lCOMMON COSMETIC",
                "",
                "§eClick to select!"
        ));
        layout.slot(11, ItemStackCreator.getStack(
                "§aMedieval",
                Material.SPRUCE_LOG,
                1,
                "§7Select this skin for your Barn!",
                "",
                "§a§lUNCOMMON COSMETIC",
                "",
                "§eClick to select!"
        ));
        layout.slot(12, ItemStackCreator.getStack(
                "§aRed",
                Material.QUARTZ_BLOCK,
                1,
                "§7Select this skin for your Barn!",
                "",
                "§a§lUNCOMMON COSMETIC",
                "",
                "§eClick to select!"
        ));
        layout.slot(13, ItemStackCreator.getStack(
                "§aSunny",
                Material.RED_SANDSTONE,
                1,
                "§7Select this skin for your Barn!",
                "",
                "§a§lUNCOMMON COSMETIC",
                "",
                "§eClick to select!"
        ));
        layout.slot(14, ItemStackCreator.getStack(
                "§9Cabin",
                Material.LIGHT_BLUE_TERRACOTTA,
                1,
                "§7Select this skin for your Barn!",
                "",
                "§9§lRARE COSMETIC",
                "",
                "§aSELECTED"
        ));
        layout.slot(15, ItemStackCreator.getStackHead(
                "§6Chocolate Factory",
                "af90da40c557af4ac01d39b6733e204c74ae9fee8c2bc40be1fd4f28f837d52",
                1,
                "§7Select this skin for your Barn!",
                "",
                "§6§lLEGENDARY COSMETIC",
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
