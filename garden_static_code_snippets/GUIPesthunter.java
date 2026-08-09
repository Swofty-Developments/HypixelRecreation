public class GUIPesthunter extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Pesthunter", InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 31);

        layout.slot(11, ItemStackCreator.getStack(
                "§aEmpty Vacuum Bag",
                Material.HOPPER_MINECART,
                1,
                "§7Empty your §6Vacuum Bag §7to receive",
                "§7bonus §6☘ Farming Fortune §7while on",
                "§7your §aGarden §7over the next §a30m§7!",
                "",
                "§7Vacuum Bag: §20 ൠ Pests",
                "",
                "§cYou don't have any Pests in your",
                "§cVacuum Bag!"
        ));
        layout.slot(13, ItemStackCreator.getStack(
                "§ePesthunter Bonus",
                Material.CLOCK,
                1,
                "§7Exchange §2ൠ Pests §7from your",
                "§6Vacuum Bag §7with the §6Pesthunter §7to",
                "§7gain temporary §6☘ Farming Fortune",
                "§7while on your §aGarden§7.",
                "",
                "§c§lINACTIVE"
        ));
        layout.slot(15, ItemStackCreator.getStack(
                "§6Pesthunter's Wares",
                Material.OAK_SIGN,
                1,
                "§7Using §2ൠ Pests§7 as currency,",
                "§7purchase a variety of bits 'n bobs",
                "§7that §6Phillip§7 has held onto over the",
                "§7years!",
                "",
                "§7Vacuum Bag: §20 ൠ Pests",
                "",
                "§eClick to view!"
        ));
        layout.slot(32, ItemStackCreator.getStack(
                "§4§lൠ §cGarden Infestation",
                Material.RED_DYE,
                1,
                "§7Your §6☘ Farming Fortune §7decreases",
                "§7as more §2ൠ Pests §7spawn in §aThe",
                "§aGarden§7!",
                "",
                "§7For every §2+100ൠ Bonus Pest Chance",
                "§7you have, you can spawn §a1 §7extra §2ൠ",
                "§2Pest §7before you start losing fortune!",
                "",
                "§7Get rid of §2ൠ Pests §7to restore it!",
                "",
                " §8ൠ §70-3 Pests §8§lSAFE",
                " §8ൠ §74 Pests §8-5% ☘",
                " §8ൠ §75 Pests §8-15% ☘",
                " §8ൠ §76 Pests §8-30% ☘",
                " §8ൠ §77 Pests §8-50% ☘",
                " §4ൠ §c8 Pests §4-75% ☘"
        ));
    }
}
