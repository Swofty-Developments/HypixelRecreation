public class GUIManageChips extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Manage Chips", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 49);

        layout.slot(4, ItemStackCreator.getStackHead(
                "§aManage Chips",
                "2a4a77840449437d21f9d5f047f518413cb2f69e3ecbfb99386649c997ca1e91",
                1,
                "§7Upgrade your §9Chips §7to gain powerful",
                "§7buffs!",
                "",
                "§2✿ Sowdust",
                "§7Sowdust is dropped from harvesting",
                "§7crops in §aThe Garden §7and is used to",
                "§7upgrade the Chips you've unlocked!",
                "",
                "§7Sowdust: §2198,605"
        ));
        layout.slot(20, ItemStackCreator.getStack(
                "§cVermin Vaporizer Chip",
                Material.GRAY_DYE,
                1,
                "§7Level 0§8/10",
                "",
                "§6Ability: Vermin Vaporizer ",
                "§7Grants §2+3ൠ Bonus Pest Chance§7!",
                "",
                "§7Rarely dropped by the §2Dragonfly",
                "§2Pest§7.",
                "",
                "§c§lLOCKED"
        ));
        layout.slot(21, ItemStackCreator.getStack(
                "§cSynthesis Chip",
                Material.GRAY_DYE,
                1,
                "§7Level 0§8/10",
                "",
                "§6Ability: Synthesis ",
                "§7Analyzing a mutation in the §eCrop",
                "§eAnalyzer §7gives §a1% §7more §cCopper§7!",
                "",
                "§7Rarely drops from the §aGreenhouse§7.",
                "",
                "§c§lLOCKED"
        ));
        layout.slot(22, ItemStackCreator.getStack(
                "§cSowledge Chip",
                Material.GRAY_DYE,
                1,
                "§7Level 0§8/10",
                "",
                "§6Ability: Sowledge ",
                "§7Grants §3+1☯ Farming Wisdom§7!",
                "",
                "§7Unlocked from §aSkyMart§7.",
                "",
                "§c§lLOCKED"
        ));
        layout.slot(23, ItemStackCreator.getStack(
                "§cMechamind Chip",
                Material.GRAY_DYE,
                1,
                "§7Level 0§8/10",
                "",
                "§6Ability: Mechamind ",
                "§7Grants §a1.5% §7more §eFarming Tool",
                "§7Experience!",
                "",
                "§7Purchased from §bAnita's Shop§7.",
                "",
                "§c§lLOCKED"
        ));
        layout.slot(24, ItemStackCreator.getStack(
                "§cHypercharge Chip",
                Material.GRAY_DYE,
                1,
                "§7Level 0§8/10",
                "",
                "§6Ability: Hypercharge ",
                "§7Temporary §6☘ Farming Fortune §7buffs",
                "§7are §a3% §7stronger!",
                "",
                "§7Rarely gets rewarded from",
                "§7completing §bVisitor Offers§7.",
                "",
                "§c§lLOCKED"
        ));
        layout.slot(29, ItemStackCreator.getStack(
                "§cEvergreen Chip",
                Material.GRAY_DYE,
                1,
                "§7Level 0§8/10",
                "",
                "§6Ability: Evergreen ",
                "§7Gain §a2% §7more base crops when",
                "§7harvesting in the §aGreenhouse§7!",
                "",
                "§7Rarely drops from the §aGreenhouse§7.",
                "",
                "§c§lLOCKED"
        ));
        layout.slot(30, ItemStackCreator.getStack(
                "§cOverdrive Chip",
                Material.GRAY_DYE,
                1,
                "§7Level 0§8/10",
                "",
                "§6Ability: Overdrive ",
                "§7Grants §6+5☘ Crop Fortune §7for the",
                "§7active crop during §eJacob's Farming",
                "§eContest§7!",
                "",
                "§7Purchased from §bAnita's Shop§7.",
                "",
                "§c§lLOCKED"
        ));
        layout.slot(31, ItemStackCreator.getStackHead(
                "§9Cropshot Chip",
                "e85b6f92f03867f835d3179a42557b4c4bdd3545c9a8e9285bdd32dab464d08f",
                1,
                "§7Level 2§8/10",
                "",
                "§6Ability: Cropshot ",
                "§7Grants §6+6☘ Farming Fortune§7!",
                "",
                "§a§l=====[ UPGRADE ]=====",
                "§6Ability: Cropshot ",
                "§7Grants §6+9☘ Farming Fortune§7!",
                "",
                "§7Cost",
                "§2200,000 Sowdust",
                "",
                "§7Redeem §a4 §7more §9Cropshot Chips §7to",
                "§7upgrade this chip to §5§lEPIC§7!",
                "",
                "§9§lRARE GARDEN CHIP",
                "",
                "§eClick to level up!"
        ));
        layout.slot(32, ItemStackCreator.getStack(
                "§cQuickdraw Chip",
                Material.GRAY_DYE,
                1,
                "§7Level 0§8/10",
                "",
                "§6Ability: Quickdraw ",
                "§7Decreases the time for §bVisitors §7to",
                "§7appear when harvesting crops by",
                "§a1.5%§7!",
                "",
                "§7Rarely gets rewarded from",
                "§7completing §bVisitor Offers§7.",
                "",
                "§c§lLOCKED"
        ));
        layout.slot(33, ItemStackCreator.getStack(
                "§cRarefinder Chip",
                Material.GRAY_DYE,
                1,
                "§7Level 0§8/10",
                "",
                "§6Ability: Rarefinder ",
                "§7Increases the chance to find rare",
                "§7items when breaking crops by §a2%§7!",
                "",
                "§7Rarely drops from farming §eCrops§7.",
                "",
                "§c§lLOCKED"
        ));
        layout.slot(50, ItemStackCreator.getStack(
                "§aChip Information",
                Material.REDSTONE_TORCH,
                1,
                "§7Chips can be upgraded using §2Sowdust§7.",
                "§7To increase a Chip's §9maximum level§7,",
                "§7you'll need to upgrade its rarity by",
                "§ausing Chip items§7.",
                "",
                "§7• §9§lRARE §7Chips max at §aLevel 10",
                "§7• §5§lEPIC §7Chips max at §aLevel 15",
                "§7• §6§lLEGENDARY §7Chips max at §aLevel 20",
                "",
                "§7Increasing a Chip's rarity also",
                "§7increases the scaling of its perks!"
        ));
        layout.slot(53, ItemStackCreator.getStackHead(
                "§cReset Farming Chips",
                "7c8489c03357d6d6abd9f4a3bd8824eb0f2841685ade95ff987ebe15b2e65fad",
                1,
                "§7Resets all §9Chip §7levels back to 1 so",
                "§7you can reallocate your §2Sowdust§7.",
                "",
                "§7You will be reimbursed with:",
                " §8- §2100,000 Sowdust",
                "",
                "§eClick to reset!"
        ));
    }
}
