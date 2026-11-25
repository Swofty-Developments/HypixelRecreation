public class GUIVisitorSLogbook extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Visitor's Logbook", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 49);

        layout.slot(4, ItemStackCreator.getStackHead(
                "§aLogbook",
                "8d34f38c1bb106e11908ad3cc90162c18b863d678265c84a84a358903f8f7a1c",
                1,
                "§7Various NPCs will visit your island",
                "§7and queue at your barn stand to",
                "§7make offers.",
                "",
                "§7Harvesting crops will reduce the time",
                "§7until the next visitor appears.",
                "",
                "§7Next Visitor: §c§lQueue Full!",
                "",
                "§7New Visitors come every §a10 minutes§7.",
                "§7Upgrades in §c10 §7more unique visitors",
                "§7served!"
        ));
        layout.slot(10, ItemStackCreator.getStack(
                "§aRyu",
                Material.VILLAGER_SPAWN_EGG,
                1,
                "§a§lUNCOMMON",
                "",
                "§7Times Visited: §a18",
                "§7Offers Accepted: §a15",
                "§7Unlocked by: §bHieta"
        ));
        layout.slot(11, ItemStackCreator.getStack(
                "§aLibrarian",
                Material.VILLAGER_SPAWN_EGG,
                1,
                "§a§lUNCOMMON",
                "",
                "§7Times Visited: §a17",
                "§7Offers Accepted: §a15",
                "§7Unlocked by: §bArikSquad"
        ));
        layout.slot(12, ItemStackCreator.getStack(
                "§aOdawa",
                Material.GRAY_DYE,
                1,
                "§7Requirement:",
                "§c✖ Talk to Odawa"
        ));
        layout.slot(13, ItemStackCreator.getStack(
                "§aAndrew",
                Material.VILLAGER_SPAWN_EGG,
                1,
                "§a§lUNCOMMON",
                "",
                "§7Times Visited: §a15",
                "§7Offers Accepted: §a12",
                "§7Unlocked by: §bHieta"
        ));
        layout.slot(14, ItemStackCreator.getStack(
                "§aDuke",
                Material.VILLAGER_SPAWN_EGG,
                1,
                "§a§lUNCOMMON",
                "",
                "§7Times Visited: §a14",
                "§7Offers Accepted: §a11",
                "§7Unlocked by: §bHieta"
        ));
        layout.slot(15, ItemStackCreator.getStackHead(
                "§aJacob",
                "7b8bb1b48f4babc67ce39547208fdbed722ca598cdf30681e367c6247cab1918",
                1,
                "§a§lUNCOMMON",
                "",
                "§7Times Visited: §a14",
                "§7Offers Accepted: §a13",
                "§7Unlocked by: §bHieta"
        ));
        layout.slot(16, ItemStackCreator.getStack(
                "§aFriendly Hiker",
                Material.GRAY_DYE,
                1,
                "§7Requirement:",
                "§c✖ Talk to Friendly Hiker"
        ));
        layout.slot(19, ItemStackCreator.getStackHead(
                "§9Gwendolyn",
                "5c34351606a99f62cab9e7c51283864753d2e67948b1f126baf70c12917233f0",
                1,
                "§9§lRARE",
                "",
                "§7Times Visited: §a12",
                "§7Offers Accepted: §a11",
                "§7Unlocked by: §bHieta"
        ));
        layout.slot(20, ItemStackCreator.getStack(
                "§aLeo",
                Material.VILLAGER_SPAWN_EGG,
                1,
                "§a§lUNCOMMON",
                "",
                "§7Times Visited: §a12",
                "§7Offers Accepted: §a12",
                "§7Unlocked by: §bHieta"
        ));
        layout.slot(21, ItemStackCreator.getStack(
                "§aLynn",
                Material.VILLAGER_SPAWN_EGG,
                1,
                "§a§lUNCOMMON",
                "",
                "§7Times Visited: §a12",
                "§7Offers Accepted: §a10",
                "§7Unlocked by: §bHieta"
        ));
        layout.slot(22, ItemStackCreator.getStackHead(
                "§aOringo",
                "e49fe595c6a08adec8b9dab0986853271c6b87d897d7318b1badad2c34bd5a0e",
                1,
                "§a§lUNCOMMON",
                "",
                "§7Times Visited: §a12",
                "§7Offers Accepted: §a11",
                "§7Unlocked by: §bHieta"
        ));
        layout.slot(23, ItemStackCreator.getStackHead(
                "§aWeaponsmith",
                "af3f7a69c9a7ad95e90f212da304990c8be36e93197db874c2b0fe08106e323b",
                1,
                "§a§lUNCOMMON",
                "",
                "§7Times Visited: §a12",
                "§7Offers Accepted: §a9",
                "§7Unlocked by: §bHieta"
        ));
        layout.slot(24, ItemStackCreator.getStack(
                "§aArthur",
                Material.GRAY_DYE,
                1,
                "§7Requirement:",
                "§c✖ Talk to Arthur"
        ));
        layout.slot(25, ItemStackCreator.getStackHead(
                "§aEmissary Fraiser",
                "9a43322511f4bba710bd78c98c90e83ac92a2da0f05a0a6951089bace0cf203a",
                1,
                "§a§lUNCOMMON",
                "",
                "§7Times Visited: §a11",
                "§7Offers Accepted: §a11",
                "§7Unlocked by: §bHieta"
        ));
        layout.slot(28, ItemStackCreator.getStack(
                "§aFarmhand",
                Material.VILLAGER_SPAWN_EGG,
                1,
                "§a§lUNCOMMON",
                "",
                "§7Times Visited: §a11",
                "§7Offers Accepted: §a8",
                "§7Unlocked by: §bHieta"
        ));
        layout.slot(29, ItemStackCreator.getStackHead(
                "§aTarwen",
                "213cf0ca79a3611b8e05fe9e264fb2bf8d27e464dc12dc6e95dd0ae0c335a561",
                1,
                "§a§lUNCOMMON",
                "",
                "§7Times Visited: §a11",
                "§7Offers Accepted: §a8",
                "§7Unlocked by: §bArikSquad"
        ));
        layout.slot(30, ItemStackCreator.getStackHead(
                "§aBanker Broadjaw",
                "e59f8c4dfd1c583a8a54fa5cc7f8eb6c24d269af27c99fd9f2b2bbf40e70fed2",
                1,
                "§a§lUNCOMMON",
                "",
                "§7Times Visited: §a10",
                "§7Offers Accepted: §a7",
                "§7Unlocked by: §bArikSquad"
        ));
        layout.slot(31, ItemStackCreator.getStackHead(
                "§aGuy",
                "e9e23be7f04556fa33351a4c771a3f05f4a6d27de413a36d0230c61f71698799",
                1,
                "§a§lUNCOMMON",
                "",
                "§7Times Visited: §a10",
                "§7Offers Accepted: §a9",
                "§7Unlocked by: §bHieta"
        ));
        layout.slot(32, ItemStackCreator.getStack(
                "§aLiam",
                Material.VILLAGER_SPAWN_EGG,
                1,
                "§a§lUNCOMMON",
                "",
                "§7Times Visited: §a10",
                "§7Offers Accepted: §a9",
                "§7Unlocked by: §bHieta"
        ));
        layout.slot(33, ItemStackCreator.getStackHead(
                "§aRhys",
                "6b20b23c1aa2be0270f016b4c90d6ee6b8330a17cfef87869d6ad60b2ffbf3b5",
                1,
                "§a§lUNCOMMON",
                "",
                "§7Times Visited: §a10",
                "§7Offers Accepted: §a6",
                "§7Unlocked by: §bArikSquad"
        ));
        layout.slot(34, ItemStackCreator.getStack(
                "§9Shifty",
                Material.GRAY_DYE,
                1,
                "§7Requirement:",
                "§c✖ Talk to Shifty"
        ));
        layout.slot(37, ItemStackCreator.getStackHead(
                "§aTerry",
                "9c3b72242f38b15c1312fac84e7cc73329236d20218e0ab78d08e2ac47806a48",
                1,
                "§a§lUNCOMMON",
                "",
                "§7Times Visited: §a10",
                "§7Offers Accepted: §a8",
                "§7Unlocked by: §bArikSquad"
        ));
        layout.slot(38, ItemStackCreator.getStackHead(
                "§aEmissary Ceanna",
                "e78c25ac8d793a73af31e5da3e8bf2d63a6c0026fde9a9682fc9caf615540c79",
                1,
                "§a§lUNCOMMON",
                "",
                "§7Times Visited: §a9",
                "§7Offers Accepted: §a7",
                "§7Unlocked by: §bHieta"
        ));
        layout.slot(39, ItemStackCreator.getStack(
                "§aJack",
                Material.VILLAGER_SPAWN_EGG,
                1,
                "§a§lUNCOMMON",
                "",
                "§7Times Visited: §a9",
                "§7Offers Accepted: §a9",
                "§7Unlocked by: §bHieta"
        ));
        layout.slot(40, ItemStackCreator.getStackHead(
                "§aJotraeline Greatforge",
                "85362b61f298dec2df765457d6b417e917d2309ebc8f691f0855489682ddec59",
                1,
                "§a§lUNCOMMON",
                "",
                "§7Times Visited: §a9",
                "§7Offers Accepted: §a9",
                "§7Unlocked by: §bHieta"
        ));
        layout.slot(41, ItemStackCreator.getStackHead(
                "§aShaggy",
                "258f88dcbd2af110c77cffaa00eab7a499c00133e61575599d9e06e61b8a24a6",
                1,
                "§a§lUNCOMMON",
                "",
                "§7Times Visited: §a9",
                "§7Offers Accepted: §a9",
                "§7Unlocked by: §bHieta"
        ));
        layout.slot(42, ItemStackCreator.getStackHead(
                "§aTrevor",
                "6102f82148461ced1f7b62e326eb2db3a94a33cba81d4281452af4d8aeca4991",
                1,
                "§a§lUNCOMMON",
                "",
                "§7Times Visited: §a9",
                "§7Offers Accepted: §a9",
                "§7Unlocked by: §bHieta"
        ));
        layout.slot(43, ItemStackCreator.getStackHead(
                "§aAdventurer",
                "8d34f38c1bb106e11908ad3cc90162c18b863d678265c84a84a358903f8f7a1c",
                1,
                "§a§lUNCOMMON",
                "",
                "§7Times Visited: §a8",
                "§7Offers Accepted: §a7",
                "§7Unlocked by: §bHieta"
        ));
        layout.slot(47, ItemStackCreator.getStack(
                "§aUnlocked",
                Material.EMERALD_BLOCK,
                1,
                "",
                "§8▶ Show All",
                "§7  Unlocked",
                "§7  Not Unlocked",
                "",
                "§bRight-click to go backwards!",
                "§eClick to switch!"
        ));
        layout.slot(48, ItemStackCreator.getStack(
                "§aSort",
                Material.HOPPER,
                1,
                "",
                "§b▶ Visits",
                "§7   Offers Accepted",
                "§7   Rarity",
                "§7   Alphabetically",
                "",
                "§bRight-click to go backwards!",
                "§eClick to switch sort!"
        ));
        layout.slot(50, ItemStackCreator.getStack(
                "§aRarity",
                Material.ENDER_EYE,
                1,
                "",
                "§8▶ No filter",
                "§7  Uncommon",
                "§7  Rare",
                "§7  Legendary",
                "§7  Mythic",
                "§7  Special",
                "",
                "§bRight-click to go backwards!",
                "§eClick to switch rarity!"
        ));
        layout.slot(51, ItemStackCreator.getStack(
                "§aVisited",
                Material.WHITE_CARPET,
                1,
                "",
                "§8▶ Show All",
                "§7  Visited",
                "§7  Never Visited",
                "",
                "§bRight-click to go backwards!",
                "§eClick to switch!"
        ));
        layout.slot(52, ItemStackCreator.getStack(
                "§aOffers Accepted",
                Material.GOLD_INGOT,
                1,
                "",
                "§8▶ Show All",
                "§7  Completed At Least 1 Offer",
                "§7  Never Completed An Offer",
                "",
                "§bRight-click to go backwards!",
                "§eClick to switch!"
        ));
        layout.slot(53, ItemStackCreator.getStack(
                "§aNext Page",
                Material.ARROW,
                1,
                "§ePage 2"
        ));
    }
}
