public class GUICropMilestones extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Crop Milestones", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 49);

        layout.slot(11, ItemStackCreator.getStack(
                "§aWheat XVIII",
                Material.WHEAT,
                1,
                "§7Harvest §fWheat §7on your Garden to",
                "§7increase your §fWheat §7tier!",
                "",
                "§7Total: §a469,212",
                "",
                "§7Progress to Tier XIX: §e86.2%",
                "§2§l§m                  §f§l§m   §e150,802§6/§e175k",
                "",
                "§7Rewards:",
                "  §8+§2190 §7Garden Experience",
                "  §8+§350,000 §7Farming Experience",
                "  §8+§b1 SkyBlock XP"
        ));
        layout.slot(12, ItemStackCreator.getStack(
                "§aCarrot XVII",
                Material.CARROT,
                1,
                "§7Harvest §fCarrot §7on your Garden to",
                "§7increase your §fCarrot §7tier!",
                "",
                "§7Total: §a1,039,360",
                "",
                "§7Progress to Tier XVIII: §e84.2%",
                "§2§l§m                 §f§l§m    §e294,860§6/§e350k",
                "",
                "§7Rewards:",
                "  §8+§2180 §7Garden Experience",
                "  §8+§350,000 §7Farming Experience",
                "  §8+§b1 SkyBlock XP"
        ));
        layout.slot(13, ItemStackCreator.getStack(
                "§aPotato XXIII",
                Material.POTATO,
                1,
                "§7Harvest §fPotato §7on your Garden to",
                "§7increase your §fPotato §7tier!",
                "",
                "§7Total: §a8,149,777",
                "",
                "§7Progress to Tier XXIV: §e59.3%",
                "§2§l§m            §f§l§m         §e1,305,277§6/§e2.2M",
                "",
                "§7Progress to Tier XLVI: §e12.3%",
                "§2§l§m   §f§l§m                  §e8,149,777§6/§e66.2M",
                "",
                "§7Rewards:",
                "  §8+§2240 §7Garden Experience",
                "  §8+§350,000 §7Farming Experience",
                "  §8+§b1 SkyBlock XP"
        ));
        layout.slot(14, ItemStackCreator.getStack(
                "§aPumpkin XXV",
                Material.CARVED_PUMPKIN,
                1,
                "§7Harvest §fPumpkin §7on your Garden to",
                "§7increase your §fPumpkin §7tier!",
                "",
                "§7Total: §a3,624,486",
                "",
                "§7Progress to Tier XXVI: §e25.8%",
                "§2§l§m      §f§l§m               §e206,076§6/§e800k",
                "",
                "§7Progress to Tier XLVI: §e17.9%",
                "§2§l§m    §f§l§m                 §e3,624,486§6/§e20.2M",
                "",
                "§7Rewards:",
                "  §8+§2260 §7Garden Experience",
                "  §8+§350,000 §7Farming Experience",
                "  §8+§b1 SkyBlock XP"
        ));
        layout.slot(15, ItemStackCreator.getStack(
                "§aSugar Cane XVII",
                Material.SUGAR_CANE,
                1,
                "§7Harvest §fSugar Cane §7on your Garden",
                "§7to increase your §fSugar Cane §7tier!",
                "",
                "§7Total: §a556,707",
                "",
                "§7Progress to Tier XVIII: §e59.9%",
                "§2§l§m            §f§l§m         §e119,887§6/§e200k",
                "",
                "§7Rewards:",
                "  §8+§2180 §7Garden Experience",
                "  §8+§350,000 §7Farming Experience",
                "  §8+§b1 SkyBlock XP"
        ));
        layout.slot(20, ItemStackCreator.getStack(
                "§aMelon Slice XVI",
                Material.MELON_SLICE,
                1,
                "§7Harvest §fMelon Slice §7on your Garden",
                "§7to increase your §fMelon Slice §7tier!",
                "",
                "§7Total: §a844,640",
                "",
                "§7Progress to Tier XVII: §e34%",
                "§2§l§m       §f§l§m              §e127,590§6/§e375k",
                "",
                "§7Rewards:",
                "  §8+§2170 §7Garden Experience",
                "  §8+§350,000 §7Farming Experience",
                "  §8+§b1 SkyBlock XP"
        ));
        layout.slot(21, ItemStackCreator.getStack(
                "§aCactus XVIII",
                Material.CACTUS,
                1,
                "§7Harvest §fCactus §7on your Garden to",
                "§7increase your §fCactus §7tier!",
                "",
                "§7Total: §a678,335",
                "",
                "§7Progress to Tier XIX: §e11.9%",
                "§2§l§m   §f§l§m                  §e41,515§6/§e350k",
                "",
                "§7Rewards:",
                "  §8+§2190 §7Garden Experience",
                "  §8+§350,000 §7Farming Experience",
                "  §8+§b1 SkyBlock XP"
        ));
        layout.slot(22, ItemStackCreator.getStack(
                "§aCocoa Beans XVI",
                Material.COCOA_BEANS,
                1,
                "§7Harvest §fCocoa Beans §7on your",
                "§7Garden to increase your §fCocoa",
                "§fBeans §7tier!",
                "",
                "§7Total: §a485,440",
                "",
                "§7Progress to Tier XVII: §e35%",
                "§2§l§m       §f§l§m              §e69,950§6/§e200k",
                "",
                "§7Rewards:",
                "  §8+§2170 §7Garden Experience",
                "  §8+§350,000 §7Farming Experience",
                "  §8+§b1 SkyBlock XP"
        ));
        layout.slot(23, ItemStackCreator.getStack(
                "§aMushroom XXI",
                Material.RED_MUSHROOM,
                1,
                "§7Harvest §fMushroom §7on your Garden",
                "§7to increase your §fMushroom §7tier!",
                "",
                "§7Total: §a1,242,256",
                "",
                "§7Progress to Tier XXII: §e43.5%",
                "§2§l§m         §f§l§m            §e173,846§6/§e400k",
                "",
                "§7Progress to Tier XLVI: §e6.1%",
                "§2§l§m  §f§l§m                   §e1,242,256§6/§e20.2M",
                "",
                "§7Rewards:",
                "  §8+§2220 §7Garden Experience",
                "  §8+§350,000 §7Farming Experience",
                "  §8+§b1 SkyBlock XP"
        ));
        layout.slot(24, ItemStackCreator.getStack(
                "§aNether Wart XVII",
                Material.NETHER_WART,
                1,
                "§7Harvest §fNether Wart §7on your Garden",
                "§7to increase your §fNether Wart §7tier!",
                "",
                "§7Total: §a649,280",
                "",
                "§7Progress to Tier XVIII: §e11.3%",
                "§2§l§m   §f§l§m                  §e33,790§6/§e300k",
                "",
                "§7Rewards:",
                "  §8+§2180 §7Garden Experience",
                "  §8+§350,000 §7Farming Experience",
                "  §8+§b1 SkyBlock XP"
        ));
        layout.slot(30, ItemStackCreator.getStack(
                "§aMoonflower XVI",
                Material.BLUE_ORCHID,
                1,
                "§7Harvest §fMoonflower §7on your Garden",
                "§7to increase your §fMoonflower §7tier!",
                "",
                "§7Total: §a198,560",
                "",
                "§7Progress to Tier XVII: §e73.1%",
                "§2§l§m               §f§l§m      §e54,800§6/§e75k",
                "",
                "§7Rewards:",
                "  §8+§2170 §7Garden Experience",
                "  §8+§350,000 §7Farming Experience",
                "  §8+§b1 SkyBlock XP"
        ));
        layout.slot(31, ItemStackCreator.getStack(
                "§aSunflower XVII",
                Material.SUNFLOWER,
                1,
                "§7Harvest §fSunflower §7on your Garden",
                "§7to increase your §fSunflower §7tier!",
                "",
                "§7Total: §a253,440",
                "",
                "§7Progress to Tier XVIII: §e35%",
                "§2§l§m        §f§l§m             §e35,030§6/§e100k",
                "",
                "§7Rewards:",
                "  §8+§2180 §7Garden Experience",
                "  §8+§350,000 §7Farming Experience",
                "  §8+§b1 SkyBlock XP"
        ));
        layout.slot(32, ItemStackCreator.getStack(
                "§aWild Rose XVII",
                Material.ROSE_BUSH,
                1,
                "§7Harvest §fWild Rose §7on your Garden to",
                "§7increase your §fWild Rose §7tier!",
                "",
                "§7Total: §a633,416",
                "",
                "§7Progress to Tier XVIII: §e98.3%",
                "§2§l§m                     §e196,596§6/§e200k",
                "",
                "§7Rewards:",
                "  §8+§2180 §7Garden Experience",
                "  §8+§350,000 §7Farming Experience",
                "  §8+§b1 SkyBlock XP"
        ));
        layout.slot(48, ItemStackCreator.getStack(
                "§aGo Back",
                Material.ARROW,
                1,
                "§7To Garden Milestones"
        ));
    }
}
