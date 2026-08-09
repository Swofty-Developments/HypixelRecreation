public class GUIJacobSFarmingContests extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Jacob's Farming Contests", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 31);

        layout.slot(11, ItemStackCreator.getStack(
                "§aWhat is this?",
                Material.OAK_SIGN,
                1,
                "§8Instructions",
                "",
                "§7Every 3 SkyBlock days, I hold a contest",
                "§7for 3 §efarming §7collections.",
                "",
                "§7The contests last 1 SkyBlock day.",
                "§8(20 minutes).",
                "",
                "§7The §etop §7players who increase",
                "§7their collection the most earn",
                "§aJacob's Tickets §7and §dunique",
                "§drewards§7!",
                "",
                "§bCo-ops do NOT pool their collection!",
                "",
                "§8You may participate in 1 collection",
                "§8contest per event!",
                "",
                "§8Minions do not count in contests!"
        ));
        layout.slot(13, ItemStackCreator.getStack(
                "§6Upcoming Contests",
                Material.CLOCK,
                1,
                "§8Schedule",
                "",
                "§eEarly Summer 23rd",
                "§e○ §7Cactus",
                "§e○ §7Moonflower",
                "§6☘ §7Wild Rose",
                "",
                "§eEarly Summer 26th",
                "§e○ §7Pumpkin",
                "§e○ §7Wheat",
                "§6☘ §7Sugar Cane",
                "",
                "§8View this info in your full",
                "§8SkyBlock calendar!"
        ));
        layout.slot(15, ItemStackCreator.getStack(
                "§bClaim your rewards!",
                Material.WHEAT,
                1,
                "§8Previous contests",
                "",
                "§7You've participated in §e11 §7previous",
                "§7contests.",
                "",
                "§7Medals inventory:",
                "§6§lGOLD §7medals: §60",
                "§f§lSILVER §7medals: §f0",
                "§c§lBRONZE §7medals: §c1",
                "",
                "§aYou have §f5 §aunclaimed awards!",
                "",
                "§eClick to view contests!"
        ));
    }
}
