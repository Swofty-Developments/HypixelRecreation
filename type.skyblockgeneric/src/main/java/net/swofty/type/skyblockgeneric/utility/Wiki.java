package net.swofty.type.skyblockgeneric.utility;

import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class Wiki {

    private static final String WIKI_BASE = "https://wiki.hypixel.net/";

    public static void wiki(SkyBlockPlayer player) {
        player.sendMessage("<click:url:'{}'><7>Click <e><l>HERE </l><7>to visit the <6>Official SkyBlock Wiki<7>!</click>", WIKI_BASE);
        player.getAchievementHandler().completeAchievement("skyblock.wow_thats_useful");
    }

    public static void wikiThis(SkyBlockPlayer player) {
        if (player.getItemInMainHand() == null || player.getItemInMainHand().isAir()) {
            player.sendMessage("<c>You must be holding an item to use this command!");
            return;
        }

        SkyBlockItem item = new SkyBlockItem(player.getItemInMainHand());
        String name = item.getDisplayName();

        if (name == null || name.isBlank()) {
            player.sendMessage("<c>This item does not have a valid name.");
            return;
        }

        String url = WIKI_BASE + name.replace(" ", "_");

        player.sendMessage("<7>Found Item: <a>{}", name);
        player.sendMessage("<7>Click <click:url:'{}'><e><l>HERE</l></click> to find it on the <6>Official SkyBlock Wiki<7>!", url);
        player.getAchievementHandler().completeAchievement("skyblock.wow_thats_useful");
    }

}
