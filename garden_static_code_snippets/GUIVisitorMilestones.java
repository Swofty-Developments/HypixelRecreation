public class GUIVisitorMilestones extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Visitor Milestones", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 49);

        layout.slot(21, ItemStackCreator.getStackHead(
                "§aUnique Visitors Served",
                "8d34f38c1bb106e11908ad3cc90162c18b863d678265c84a84a358903f8f7a1c",
                1,
                "§7Unlock unique visitors and complete",
                "§7their offer on your island to",
                "§7increase this milestone.",
                "",
                "§7Progress to Tier XII: §e0%",
                "§f§l§m                     §e0§6/§e10",
                "",
                "§7Rewards:",
                "  §8+§2360 §7Garden Experience",
                "  §8+§310,000 §7Farming Experience",
                "  §8+§b3 SkyBlock XP"
        ));
        layout.slot(23, ItemStackCreator.getStackHead(
                "§aOffers Accepted",
                "c765fee97bcfae7c136b6a6b8ca95381af964d6aebc08bfdd2350af78e2cf51a",
                1,
                "§7Accept offers from visitors to",
                "§7increase this milestone.",
                "",
                "§7Progress to Tier XIV: §e24%",
                "§2§l§m     §f§l§m                §e24§6/§e100",
                "",
                "§7Rewards:",
                "  §8+§2420 §7Garden Experience",
                "  §8+§330,000 §7Farming Experience",
                "  §8+§b3 SkyBlock XP"
        ));
        layout.slot(48, ItemStackCreator.getStack(
                "§aGo Back",
                Material.ARROW,
                1,
                "§7To Garden Milestones"
        ));
        layout.slot(50, ItemStackCreator.getStack(
                "§aVisitor's Logbook",
                Material.BOOK,
                1,
                "§7Browse and filter through all of the",
                "§7visitors on the Garden.",
                "",
                "§eClick to browse!"
        ));
    }
}
