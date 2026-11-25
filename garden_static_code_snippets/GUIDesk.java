public class GUIDesk extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Desk", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 49);

        layout.slot(4, ItemStackCreator.getStack(
                "§aGarden Level XIII",
                Material.SUNFLOWER,
                1,
                "§7Earn Garden experience by",
                "§7accepting visitors' offers and",
                "§7unlocking new milestones!",
                "",
                "§7Progress to Level XIV: §e71.2%",
                "§2§l§m               §f§l§m      §e7,125§6/§e10k",
                "",
                "§7Level XIV Rewards:",
                "  §8+§b1 §eVisitor",
                "  §aTier VIII §7Crop Upgrades",
                "  §8+§b10 SkyBlock XP",
                "  §8+§210 Crop Growth",
                "",
                "§7You currently have §2130 Crop Growth§7!",
                "",
                "§eClick to view!"
        ));
        layout.slot(19, ItemStackCreator.getStack(
                "§aConfigure Plots",
                Material.GRASS_BLOCK,
                1,
                "§7Unlock access to new plots or modify",
                "§7plots that you have already unlocked!",
                "",
                "§eClick to view!"
        ));
        layout.slot(21, ItemStackCreator.getStack(
                "§aGarden Upgrades",
                Material.GLISTERING_MELON_SLICE,
                1,
                "§7Upgrade various aspects of your",
                "§7garden to increase yield,",
                "§7experience, and more.",
                "",
                "§eClick to view!"
        ));
        layout.slot(23, ItemStackCreator.getStack(
                "§aSkyMart",
                Material.EMERALD,
                1,
                "§7Browse the wide variety of products",
                "§7SkyMart has to offer. We are not",
                "§7responsible for any injuries,",
                "§7accidents, headaches, paper-cuts or",
                "§7sudden outburst of tears. SkyMart",
                "§7wishes you happy shopping.",
                "",
                "§eClick to view!"
        ));
        layout.slot(25, ItemStackCreator.getStack(
                "§aGarden Milestones",
                Material.GOLD_BLOCK,
                1,
                "§7Achieve milestones on your Garden",
                "§7to earn Garden XP and Farming XP.",
                "",
                "§eClick to view!"
        ));
        layout.slot(31, ItemStackCreator.getStack(
                "§aGarden Skins",
                Material.BEACON,
                1,
                "§7View and select different skins for",
                "§7your Garden!",
                "",
                "§eClick to view!"
        ));
        layout.slot(50, ItemStackCreator.getStack(
                "§aGarden Time",
                Material.CLOCK,
                1,
                "§7Modifies your Garden time.",
                "",
                "§cClick on a Day Saver in your",
                "§cinventory to unlock! (0/2)"
        ));
    }
}
