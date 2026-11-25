public class GUIGardenLevels extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Garden Levels", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 49);

        layout.slot(0, ItemStackCreator.getStack(
                "§aGarden Levels",
                Material.SUNFLOWER,
                1,
                "§7Earn Garden experience by",
                "§7accepting visitors' offers and",
                "§7unlocking new milestones!",
                "",
                "§7Progress to Level XIV: §e71.2%",
                "§2§l§m               §f§l§m      §e7,125§6/§e10k",
                "",
                "§8Increase your Garden Level to",
                "§8unlock new visitors, crops and more!"
        ));
        layout.slot(2, ItemStackCreator.getStack(
                "§aGarden Level VIII",
                Material.LIME_STAINED_GLASS_PANE,
                8,
                "§7Rewards:",
                "  §8+§b9 §eVisitors",
                "  §aCocoa Beans §7Crop",
                "  §6Moth Pest",
                "  §aTier IV §7Crop Upgrades",
                "  §8+§b10 SkyBlock XP",
                "  §8+§210 Crop Growth",
                "",
                "§a§lUNLOCKED"
        ));
        layout.slot(3, ItemStackCreator.getStack(
                "§aGarden Level IX",
                Material.LIME_STAINED_GLASS_PANE,
                9,
                "§7Rewards:",
                "  §8+§b8 §eVisitors",
                "  §aMushroom §7Crop",
                "  §6Slug Pest",
                "  §aRed Barn Skin",
                "  §8+§b10 SkyBlock XP",
                "  §8+§210 Crop Growth",
                "",
                "§a§lUNLOCKED"
        ));
        layout.slot(4, ItemStackCreator.getStack(
                "§aGarden Level X",
                Material.DIAMOND_HOE,
                1,
                "§7Rewards:",
                "  §8+§b9 §eVisitors",
                "  §aNether Wart §7Crop",
                "  §6Beetle Pest",
                "  §aTier V §7Crop Upgrades",
                "  §8+§b10 SkyBlock XP",
                "  §8+§210 Crop Growth",
                "",
                "§a§lUNLOCKED"
        ));
        layout.slot(9, ItemStackCreator.getStack(
                "§aGarden Level I",
                Material.LIME_STAINED_GLASS_PANE,
                1,
                "§7Rewards:",
                "  §8+§b46 §eVisitors",
                "  §aWheat §7Crop",
                "",
                "§a§lUNLOCKED"
        ));
        layout.slot(11, ItemStackCreator.getStack(
                "§aGarden Level VII",
                Material.LIME_STAINED_GLASS_PANE,
                7,
                "§7Rewards:",
                "  §8+§b10 §eVisitors",
                "  §aCactus §7Crop",
                "  §6Mite Pest",
                "  §8+§b10 SkyBlock XP",
                "  §8+§210 Crop Growth",
                "",
                "§a§lUNLOCKED"
        ));
        layout.slot(13, ItemStackCreator.getStack(
                "§aGarden Level XI",
                Material.LIME_STAINED_GLASS_PANE,
                11,
                "§7Rewards:",
                "  §8+§b3 §eVisitors",
                "  §aSunflower §7Crop",
                "  §aMoonflower §7Crop",
                "  §6Firefly Pest",
                "  §6Dragonfly Pest",
                "  §9Cabin Barn Skin",
                "  §8+§b10 SkyBlock XP",
                "  §8+§210 Crop Growth",
                "",
                "§a§lUNLOCKED"
        ));
        layout.slot(18, ItemStackCreator.getStack(
                "§aGarden Level II",
                Material.LIME_STAINED_GLASS_PANE,
                2,
                "§7Rewards:",
                "  §8+§b9 §eVisitors",
                "  §aCarrot §7Crop",
                "  §aTier I §7Crop Upgrades",
                "  §8+§b10 SkyBlock XP",
                "  §8+§210 Crop Growth",
                "",
                "§a§lUNLOCKED"
        ));
        layout.slot(20, ItemStackCreator.getStack(
                "§aGarden Level VI",
                Material.LIME_STAINED_GLASS_PANE,
                6,
                "§7Rewards:",
                "  §8+§b6 §eVisitors",
                "  §aMelon Slice §7Crop",
                "  §6Earthworm Pest",
                "  §aTier III §7Crop Upgrades",
                "  §aSunny Barn Skin",
                "  §8+§b10 SkyBlock XP",
                "  §8+§210 Crop Growth",
                "",
                "§a§lUNLOCKED"
        ));
        layout.slot(22, ItemStackCreator.getStack(
                "§aGarden Level XII",
                Material.LIME_STAINED_GLASS_PANE,
                12,
                "§7Rewards:",
                "  §8+§b9 §eVisitors",
                "  §aWild Rose §7Crop",
                "  §6Praying Mantis Pest",
                "  §aTier VI §7Crop Upgrades",
                "  §8+§b10 SkyBlock XP",
                "  §8+§210 Crop Growth",
                "",
                "§a§lUNLOCKED"
        ));
        layout.slot(27, ItemStackCreator.getStack(
                "§aGarden Level III",
                Material.LIME_STAINED_GLASS_PANE,
                3,
                "§7Rewards:",
                "  §8+§b13 §eVisitors",
                "  §aPotato §7Crop",
                "  §aMedieval Barn Skin",
                "  §8+§b10 SkyBlock XP",
                "  §8+§210 Crop Growth",
                "",
                "§a§lUNLOCKED"
        ));
        layout.slot(28, ItemStackCreator.getStack(
                "§aGarden Level IV",
                Material.LIME_STAINED_GLASS_PANE,
                4,
                "§7Rewards:",
                "  §8+§b5 §eVisitors",
                "  §aPumpkin §7Crop",
                "  §aTier II §7Crop Upgrades",
                "  §8+§7Gearing Up Quest",
                "  §8+§b10 SkyBlock XP",
                "  §8+§210 Crop Growth",
                "",
                "§a§lUNLOCKED"
        ));
        layout.slot(29, ItemStackCreator.getStack(
                "§aGarden Level V",
                Material.DIAMOND_HOE,
                1,
                "§7Rewards:",
                "  §8+§b5 §eVisitors",
                "  §aSugar Cane §7Crop",
                "  §6Fly Pest",
                "  §6Cricket Pest",
                "  §6Locust Pest",
                "  §6Rat Pest",
                "  §6Field Mouse Pest",
                "  §6Mosquito Pest",
                "  §8+§b10 SkyBlock XP",
                "  §8+§210 Crop Growth",
                "",
                "§a§lUNLOCKED"
        ));
        layout.slot(31, ItemStackCreator.getStack(
                "§aGarden Level XIII",
                Material.LIME_STAINED_GLASS_PANE,
                13,
                "§7Rewards:",
                "  §8+§b2 §eVisitors",
                "  §aTier VII §7Crop Upgrades",
                "  §8+§b10 SkyBlock XP",
                "  §8+§210 Crop Growth",
                "",
                "§a§lUNLOCKED"
        ));
        layout.slot(32, ItemStackCreator.getStack(
                "§eGarden Level XIV",
                Material.YELLOW_STAINED_GLASS_PANE,
                14,
                "§7Rewards:",
                "  §8+§b1 §eVisitor",
                "  §aTier VIII §7Crop Upgrades",
                "  §8+§b10 SkyBlock XP",
                "  §8+§210 Crop Growth",
                "",
                "§7Progress to Level XIV: §e71.2%",
                "§2§l§m               §f§l§m      §e7,125§6/§e10k"
        ));
        layout.slot(33, ItemStackCreator.getStack(
                "§cGarden Level XV",
                Material.DIAMOND_HOE,
                1,
                "§7Rewards:",
                "  §8+§b2 §eVisitors",
                "  §aTier IX §7Crop Upgrades",
                "  §5Mansion Heights Barn Skin",
                "  §8+§b10 SkyBlock XP",
                "  §8+§210 Crop Growth"
        ));
        layout.slot(48, ItemStackCreator.getStack(
                "§aGo Back",
                Material.ARROW,
                1,
                "§7To Desk"
        ));
    }
}
