public class GUIAnita extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Anita", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 49);

        layout.slot(10, ItemStackCreator.getStack(
                "§aInfiniDirt™ Wand",
                Material.STICK,
                1,
                "§6Ability: Place Dirt  §e§lRIGHT CLICK",
                "§7Place a dirt block.",
                "§7Costs §61 coin§7.",
                "",
                "§7Can be used within the Builder's Wand!",
                "",
                "§a§lUNCOMMON",
                "",
                "§7Cost",
                "§aJacob's Ticket",
                "",
                "§eClick to trade!"
        ));
        layout.slot(11, ItemStackCreator.getStack(
                "§9Prismapump §8x4",
                Material.DARK_PRISMARINE,
                4,
                "§6Ability: Pipeline  §e§lRIGHT CLICK",
                "§7When used, pumps water in the direction the",
                "§7player was facing.",
                "",
                "§9§lRARE",
                "",
                "§7Cost",
                "§cBronze medal",
                "§aJacob's Ticket §8x2",
                "",
                "§eClick to trade!"
        ));
        layout.slot(12, ItemStackCreator.getStack(
                "§aHoe of Great Tilling",
                Material.STONE_HOE,
                1,
                "§7Tills a §93x3 §7area of farmland at a time.",
                "",
                "§8This item can be reforged!",
                "§a§lUNCOMMON HOE",
                "",
                "§7Cost",
                "§cBronze medal",
                "§aJacob's Ticket §8x5",
                "",
                "§eClick to trade!"
        ));
        layout.slot(13, ItemStackCreator.getStack(
                "§9Hoe of Greater Tilling",
                Material.DIAMOND_HOE,
                1,
                "§7Tills a §95x5 §7area of farmland at a time.",
                "",
                "§8This item can be reforged!",
                "§9§lRARE HOE",
                "",
                "§7Cost",
                "§fSilver medal",
                "§aJacob's Ticket §8x10",
                "",
                "§eClick to trade!"
        ));
        layout.slot(14, ItemStackCreator.getStack(
                "§5Hoe of Greatest Tilling",
                Material.DIAMOND_HOE,
                1,
                "§7Tills a row of farmland. The perfect",
                "§7accessory to the §5Basket of Seeds§7!",
                "",
                "§8This item can be reforged!",
                "§5§lEPIC HOE",
                "",
                "§7Cost",
                "§fSilver medal§8 x2",
                "§aJacob's Ticket §8x20",
                "",
                "§eClick to trade!"
        ));
        layout.slot(15, ItemStackCreator.getStackHead(
                "§5Basket of Seeds",
                "7a6bf916e28ccb80b4ebfacf98686ad6af7c4fb257e57a8cb78c71d19dccb2",
                1,
                "",
                "§6Ability: Seed Storage  §e§lLEFT CLICK",
                "§7Place seeds in the basket.",
                "",
                "§6Ability: Farmer's Delight  §e§lRIGHT CLICK",
                "§7Automatically seed a row of farmland.",
                "",
                "§5§lEPIC",
                "",
                "§7Cost",
                "§fSilver medal§8 x2",
                "§aJacob's Ticket §8x30",
                "",
                "§eClick to trade!"
        ));
        layout.slot(16, ItemStackCreator.getStackHead(
                "§9Nether Wart Pouch",
                "85e24b8c2d20f9e23bca52d94fbd820aa0744591acb0359183312e43a287e5b0",
                1,
                "",
                "§6Ability: Nether Wart Storage  §e§lLEFT CLICK",
                "§7Place nether wart in the pouch.",
                "",
                "§6Ability: Alchemist's Bliss  §e§lRIGHT CLICK",
                "§7Automatically plant nether wart on a",
                "§7row of soul sand.",
                "",
                "§9§lRARE",
                "",
                "§7Cost",
                "§fSilver medal§8 x2",
                "§aJacob's Ticket §8x30",
                "",
                "§eClick to trade!"
        ));
        layout.slot(19, ItemStackCreator.getStack(
                "§aEnchanted Book",
                Material.ENCHANTED_BOOK,
                1,
                "§9Delicate V",
                "§7Avoids breaking stems and baby",
                "§7crops.",
                "",
                "§7Applicable on: §9Axe§7, §9Hoe",
                "§7Apply Cost: §350 Exp Levels",
                "",
                "§e▲ §7Delicate cannot be combined!",
                "§7Use this on an item in an Anvil to",
                "§7apply it!",
                "",
                "§a§lUNCOMMON",
                "",
                "§7Cost",
                "§fSilver medal§8 x2",
                "§aJacob's Ticket §8x32",
                "",
                "§eClick to trade!"
        ));
        layout.slot(20, ItemStackCreator.getStack(
                "§e+1 Farming Level Cap",
                Material.HAY_BLOCK,
                1,
                "§7Raise your Farming Skill level cap",
                "§7by one, up to §f+10§7.",
                "",
                "§7Your cap: §eFarming L",
                "",
                "§7Requirement",
                "§c✖ §7Win §6§lGOLD §7in §e1 §7collection! (§c0§7/§e1§7)",
                "",
                "§7Cost",
                "§6Gold medal",
                "",
                "§eClick to trade!"
        ));
        layout.slot(21, ItemStackCreator.getStack(
                "§eExtra Farming Fortune",
                Material.WHEAT,
                1,
                "§7Permanently gain §6+4☘ Farming",
                "§6Fortune §7per tier.",
                "",
                "§7Cost",
                "§6Gold medal",
                "",
                "§eClick to trade!"
        ));
        layout.slot(22, ItemStackCreator.getStack(
                "§ePersonal Bests",
                Material.PAPER,
                1,
                "§7Beating your §6Personal Best §7is great!",
                "§7But what if it also gave you §6☘",
                "§6Farming Fortune§7?!",
                "",
                "§7Cost",
                "§6Gold medal§8 x2",
                "§aJacob's Ticket §8x64",
                "",
                "§eClick to trade!"
        ));
        layout.slot(23, ItemStackCreator.getStackHead(
                "§fAnita's Talisman",
                "bb8ec57b37fdf093fe66efe2ac070a8f5181949970a4458d56ec9701eded8cff",
                1,
                "§7Grants a random §6+5☘ Farming",
                "§6Fortune §7stat during §eJacob's Farming",
                "§eContest§7.",
                "",
                "§8Works while in Accessory Bag!",
                "§f§lCOMMON ACCESSORY",
                "",
                "§7Cost",
                "§cBronze medal§8 x2",
                "§aJacob's Ticket §8x32",
                "",
                "§eClick to trade!"
        ));
        layout.slot(24, ItemStackCreator.getStackHead(
                "§aAnita's Ring",
                "59a1035bc6f00fddc8e0291c38319408babc84ae10648cb5258ed8b55d60e0c3",
                1,
                "§7Grants a random §6+15☘ Farming",
                "§6Fortune §7stat during §eJacob's Farming",
                "§eContest§7.",
                "",
                "§8Works while in Accessory Bag!",
                "§a§lUNCOMMON ACCESSORY",
                "",
                "§7Cost",
                "§fSilver medal§8 x2",
                "§aJacob's Ticket §8x64",
                "§fAnita's Talisman",
                "",
                "§eClick to trade!"
        ));
        layout.slot(25, ItemStackCreator.getStackHead(
                "§9Anita's Artifact",
                "8feabdadd5f593771fa23c94fc6816091917371fd5a1c74723e716210d6e6efb",
                1,
                "§7Grants a random §6+25☘ Farming",
                "§6Fortune §7stat during §eJacob's Farming",
                "§eContest§7.",
                "",
                "§8Works while in Accessory Bag!",
                "§9§lRARE ACCESSORY",
                "",
                "§7Cost",
                "§6Gold medal§8 x2",
                "§aJacob's Ticket §8x100",
                "§aAnita's Ring",
                "",
                "§eClick to trade!"
        ));
        layout.slot(28, ItemStackCreator.getStackHead(
                "§9Mechamind Chip",
                "560aa469cc6b667dbcbfdc63e827b7c05ca7726af8a178a4aa2e8ffa2690e843",
                1,
                "§8Garden Chip",
                "",
                "§6Ability: Mechamind ",
                "§7Grants §a1.5% §7more §eFarming Tool",
                "§7Experience per §9Chip §7Level!",
                "",
                "§7Redeem Chips to increase their rarity!",
                "",
                "§eRight-Click to redeem!",
                "§eShift Right-Click to redeem all!",
                "",
                "§9§lRARE GARDEN CHIP",
                "",
                "§7Cost",
                "§6Gold medal",
                "",
                "§eClick to trade!"
        ));
        layout.slot(29, ItemStackCreator.getStackHead(
                "§9Overdrive Chip",
                "115f8b48be9a2c3539af2b7c4026fb27a3dc3580cba74cc1f3ec8beda9a91af5",
                1,
                "§8Garden Chip",
                "",
                "§6Ability: Overdrive ",
                "§7Grants §6+5☘ Crop Fortune §7for the",
                "§7active crop during §eJacob's Farming",
                "§eContest §7per §9Chip §7Level!",
                "",
                "§7Redeem Chips to increase their rarity!",
                "",
                "§eRight-Click to redeem!",
                "§eShift Right-Click to redeem all!",
                "",
                "§9§lRARE GARDEN CHIP",
                "",
                "§7Cost",
                "§6Gold medal§8 x2",
                "",
                "§eClick to trade!"
        ));
        layout.slot(48, ItemStackCreator.getStack(
                "§aPersonal Bests",
                Material.PAPER,
                1,
                "§7The higher your §6Personal Bests §7are",
                "§7from participating in §eJacob's Farming",
                "§eContest§7, the more §6☘ Farming Fortune",
                "§7you'll obtain for that crop!",
                "",
                "§cPurchase the perk in Anita's Shop to",
                "§cunlock this menu!"
        ));
        layout.slot(50, ItemStackCreator.getStack(
                "§aMedal Trades",
                Material.POWERED_RAIL,
                1,
                "§7Convert and sell your medals.",
                "",
                "§eClick to open!"
        ));
        layout.slot(51, ItemStackCreator.getStack(
                "§eUnique Brackets Reached",
                Material.GOLD_INGOT,
                1,
                "§c◌§f◌§6◌ §cWheat",
                "§c◌§f◌§6◌ §cCarrot",
                "§c●§f◌§6◌ §cPotato",
                "§c◌§f◌§6◌ §cPumpkin",
                "§c◌§f◌§6◌ §cMelon Slice",
                "§c◌§f◌§6◌ §cMushroom",
                "§c◌§f◌§6◌ §cCactus",
                "§c◌§f◌§6◌ §cSugar Cane",
                "§c◌§f◌§6◌ §cNether Wart",
                "§c◌§f◌§6◌ §cCocoa Beans",
                "§c◌§f◌§6◌ §cSunflower",
                "§c◌§f◌§6◌ §cMoonflower",
                "§c◌§f◌§6◌ §cWild Rose"
        ));
    }
}
