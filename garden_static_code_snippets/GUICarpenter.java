public class GUICarpenter extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Carpenter", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 49);

        layout.slot(13, ItemStackCreator.getStackHead(
                "§9Carpenter",
                "cc8d2a5af975c53cf081b531566148bae366141314b35aa5726e97cc97ef118b",
                1,
                "§9§lRARE",
                "",
                "§7Times Visited: §a4",
                "§7Offers Accepted: §a3"
        ));
        layout.slot(29, ItemStackCreator.getStack(
                "§aAccept Offer",
                Material.GREEN_TERRACOTTA,
                1,
                "§7Items Required:",
                " §9Enchanted Melon §8x7",
                "",
                "§7Rewards:",
                " §8+§34.7k §7Farming XP",
                " §8+§230 §7Garden Experience",
                " §8+§c58 Copper",
                "",
                "§cMissing items to accept!"
        ));
        layout.slot(33, ItemStackCreator.getStack(
                "§cRefuse Offer",
                Material.RED_TERRACOTTA,
                1,
                "§9Carpenter §7will leave your §aGarden",
                "§7and maybe come back later.",
                "",
                "§eClick to refuse!"
        ));
    }
}
