package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.levels.rewards;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.*;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSkyBlockExperience;
import net.swofty.type.skyblockgeneric.levels.CustomLevelAward;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GUILevelFeatureRewards extends StatelessView {
    private static final int[] SLOTS = new int[]{
            19, 20, 21, 22, 23, 24, 25, 31
    };

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Feature Rewards", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        // Title item
        layout.slot(4, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            DatapointSkyBlockExperience.PlayerSkyBlockExperience experience = player.getSkyBlockExperience();
            List<Text> lore = new ArrayList<>();
            lore.add(Text.of("<7>Specific game features such as the"));
            lore.add(Text.of("<7>Bazaar or Community Shop."));
            lore.add(Text.literal(" "));
            lore.add(Text.of("<7>Next Reward:"));

            Map.Entry<Integer, List<CustomLevelAward>> nextAward = CustomLevelAward.getNextReward(experience.getLevel().asInt());
            if (nextAward == null) {
                lore.add(Text.of("<c>No more rewards!"));
            } else {
                nextAward.getValue().forEach(award -> lore.add(Text.of("<7>{}", award.getDisplay())));
                lore.add(Text.of("<8>at Level {}", nextAward.getKey()));
            }

            lore.add(Text.literal(" "));
            lore.addAll(GUILevelRewards.getAsDisplay(CustomLevelAward.getFromLevel(experience.getLevel().asInt()).size(),
                    CustomLevelAward.getTotalLevelAwards()));

            return ItemStacks.item(Material.NETHER_STAR, 1, Text.of("<a>Feature Rewards"), lore);
        });

        // Award items
        for (Map.Entry<CustomLevelAward, Integer> entry : CustomLevelAward.getAwards().entrySet()) {
            CustomLevelAward award = entry.getKey();
            Integer level = entry.getValue();
            int slot = SLOTS[award.ordinal()];

            layout.slot(slot, (s, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                boolean unlocked = player.getSkyBlockExperience().getLevel().asInt() >= level;

                ItemStack.Builder item = award.getItem();
                List<Text> lore = new ArrayList<>(List.of(
                        Text.of("<8>Level {}", level),
                        Text.literal(" ")
                ));

                if (unlocked) {
                    lore.add(Text.of("<a>You have unlocked this reward!"));
                } else {
                    lore.add(Text.of("<7>Levels left to Unlock: <3>{}", level - player.getSkyBlockExperience().getLevel().asInt()));
                }

                return ItemStacks.name(ItemStacks.lore(item, lore), award.getDisplay());
            });
        }
    }
}
