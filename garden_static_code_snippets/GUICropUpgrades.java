public class GUICropUpgrades extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Crop Upgrades", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 49);

        layout.slot(11, ItemStackCreator.getStack(
                "§aWheat",
                Material.WHEAT,
                1,
                "§7Upgrade your §aWheat §7tier to increase",
                "§7your §6☘ Wheat Fortune§7.",
                "",
                "§7Current Tier: §e3§7/§a9",
                "§7Wheat Fortune: §6+15☘",
                "",
                "§eClick to view!"
        ));
        layout.slot(12, ItemStackCreator.getStack(
                "§aCarrot",
                Material.CARROT,
                1,
                "§7Upgrade your §aCarrot §7tier to",
                "§7increase your §6☘ Carrot Fortune§7.",
                "",
                "§7Current Tier: §e1§7/§a9",
                "§7Carrot Fortune: §6+5☘",
                "",
                "§eClick to view!"
        ));
        layout.slot(13, ItemStackCreator.getStack(
                "§aPotato",
                Material.POTATO,
                1,
                "§7Upgrade your §aPotato §7tier to",
                "§7increase your §6☘ Potato Fortune§7.",
                "",
                "§7Current Tier: §e6§7/§a9",
                "§7Potato Fortune: §6+30☘",
                "",
                "§eClick to view!"
        ));
        layout.slot(14, ItemStackCreator.getStack(
                "§aPumpkin",
                Material.CARVED_PUMPKIN,
                1,
                "§7Upgrade your §aPumpkin §7tier to",
                "§7increase your §6☘ Pumpkin Fortune§7.",
                "",
                "§7Current Tier: §e6§7/§a9",
                "§7Pumpkin Fortune: §6+30☘",
                "",
                "§eClick to view!"
        ));
        layout.slot(15, ItemStackCreator.getStack(
                "§aSugar Cane",
                Material.SUGAR_CANE,
                1,
                "§7Upgrade your §aSugar Cane §7tier to",
                "§7increase your §6☘ Sugar Cane",
                "§6Fortune§7.",
                "",
                "§7Current Tier: §e5§7/§a9",
                "§7Sugar Cane Fortune: §6+25☘",
                "",
                "§eClick to view!"
        ));
        layout.slot(20, ItemStackCreator.getStack(
                "§aMelon Slice",
                Material.MELON_SLICE,
                1,
                "§7Upgrade your §aMelon Slice §7tier to",
                "§7increase your §6☘ Melon Slice Fortune§7.",
                "",
                "§7Current Tier: §e3§7/§a9",
                "§7Melon Slice Fortune: §6+15☘",
                "",
                "§eClick to view!"
        ));
        layout.slot(21, ItemStackCreator.getStack(
                "§aCactus",
                Material.CACTUS,
                1,
                "§7Upgrade your §aCactus §7tier to",
                "§7increase your §6☘ Cactus Fortune§7.",
                "",
                "§7Current Tier: §e2§7/§a9",
                "§7Cactus Fortune: §6+10☘",
                "",
                "§eClick to view!"
        ));
        layout.slot(22, ItemStackCreator.getStack(
                "§aCocoa Beans",
                Material.COCOA_BEANS,
                1,
                "§7Upgrade your §aCocoa Beans §7tier to",
                "§7increase your §6☘ Cocoa Beans",
                "§6Fortune§7.",
                "",
                "§7Current Tier: §e3§7/§a9",
                "§7Cocoa Beans Fortune: §6+15☘",
                "",
                "§eClick to view!"
        ));
        layout.slot(23, ItemStackCreator.getStack(
                "§aMushroom",
                Material.RED_MUSHROOM,
                1,
                "§7Upgrade your §aMushroom §7tier to",
                "§7increase your §6☘ Mushroom Fortune§7.",
                "",
                "§7Current Tier: §e2§7/§a9",
                "§7Mushroom Fortune: §6+10☘",
                "",
                "§eClick to view!"
        ));
        layout.slot(24, ItemStackCreator.getStack(
                "§aNether Wart",
                Material.NETHER_WART,
                1,
                "§7Upgrade your §aNether Wart §7tier to",
                "§7increase your §6☘ Nether Wart",
                "§6Fortune§7.",
                "",
                "§7Current Tier: §e2§7/§a9",
                "§7Nether Wart Fortune: §6+10☘",
                "",
                "§eClick to view!"
        ));
        layout.slot(30, ItemStackCreator.getStack(
                "§aSunflower",
                Material.SUNFLOWER,
                1,
                "§7Upgrade your §aSunflower §7tier to",
                "§7increase your §6☘ Sunflower Fortune§7.",
                "",
                "§7Current Tier: §e3§7/§a9",
                "§7Sunflower Fortune: §6+15☘",
                "",
                "§eClick to view!"
        ));
        layout.slot(31, ItemStackCreator.getStack(
                "§aMoonflower",
                Material.BLUE_ORCHID,
                1,
                "§7Upgrade your §aMoonflower §7tier to",
                "§7increase your §6☘ Moonflower Fortune§7.",
                "",
                "§7Current Tier: §e3§7/§a9",
                "§7Moonflower Fortune: §6+15☘",
                "",
                "§eClick to view!"
        ));
        layout.slot(32, ItemStackCreator.getStack(
                "§aWild Rose",
                Material.ROSE_BUSH,
                1,
                "§7Upgrade your §aWild Rose §7tier to",
                "§7increase your §6☘ Wild Rose Fortune§7.",
                "",
                "§7Current Tier: §e5§7/§a9",
                "§7Wild Rose Fortune: §6+25☘",
                "",
                "§eClick to view!"
        ));
        layout.slot(48, ItemStackCreator.getStack(
                "§aGo Back",
                Material.ARROW,
                1,
                "§7To Garden Upgrades"
        ));
    }
}
