public class GUIGardenMilestones extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Garden Milestones", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 49);

        layout.slot(21, ItemStackCreator.getStack(
                "§aCrop Milestones",
                Material.WHEAT,
                1,
                "§7View progress and rewards for",
                "§7crop milestones on your garden!",
                "",
                "§eClick to view!"
        ));
        layout.slot(23, ItemStackCreator.getStackHead(
                "§aVisitor Milestones",
                "8d34f38c1bb106e11908ad3cc90162c18b863d678265c84a84a358903f8f7a1c",
                1,
                "§7View progress and rewards for",
                "§7visitor milestones on your garden!",
                "",
                "§eClick to view!"
        ));
        layout.slot(48, ItemStackCreator.getStack(
                "§aGo Back",
                Material.ARROW,
                1,
                "§7To Desk"
        ));
    }
}
