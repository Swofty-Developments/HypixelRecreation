public class GUIShifty extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Shifty", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 49);

        layout.slot(13, ItemStackCreator.getStackHead(
                "§9Shifty",
                "6c01b213574f630616dd00b7a3bc61774fd7d467136476bd7b7a99ec25ee2757",
                1,
                "§9§lRARE",
                "",
                "§7Times Visited: §a10",
                "§7Offers Accepted: §a9"
        ));
        layout.slot(29, ItemStackCreator.getStack(
                "§aAccept Offer",
                Material.GREEN_TERRACOTTA,
                1,
                "§7Items Required:",
                " §9Enchanted Melon §8x8",
                "",
                "§7Rewards:",
                " §8+§35.6k §7Farming XP",
                " §8+§230 §7Garden Experience",
                " §8+§c68 Copper",
                "",
                "§cMissing items to accept!"
        ));
        layout.slot(33, ItemStackCreator.getStack(
                "§cRefuse Offer",
                Material.RED_TERRACOTTA,
                1,
                "§9Shifty §7will leave your §aGarden §7and",
                "§7maybe come back later.",
                "",
                "§eClick to refuse!"
        ));
    }
}
