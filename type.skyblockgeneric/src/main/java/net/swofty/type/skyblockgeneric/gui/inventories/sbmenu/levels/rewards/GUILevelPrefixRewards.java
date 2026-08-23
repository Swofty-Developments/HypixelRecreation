package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.levels.rewards;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.*;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSkyBlockExperience;
import net.swofty.type.skyblockgeneric.levels.SkyBlockLevelRequirement;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class GUILevelPrefixRewards extends StatelessView {
    private static final int[] SLOTS = new int[]{
            19, 20, 21, 22, 23, 24, 25,
            29, 30, 31, 32, 33
    };

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Prefix Rewards", InventoryType.CHEST_6_ROW);
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
            lore.add(Text.of("<7>New colors for your level prefix"));
            lore.add(Text.of("<7>shown in TAB and in chat!"));
            lore.add(Text.literal(" "));
            lore.add(Text.of("<7>Next Reward:"));

            Map.Entry<SkyBlockLevelRequirement, String> nextPrefix = experience.getLevel().getNextPrefixChange();
            if (nextPrefix == null) {
                lore.add(Text.of("<c>No more rewards!"));
            } else {
                lore.add(Text.parse(nextPrefix.getValue() + nextPrefix.getKey().getPrefixDisplay()));
                lore.add(Text.of("<8>at Level {}", nextPrefix.getKey().asInt()));
            }
            lore.add(Text.literal(" "));
            lore.addAll(GUILevelRewards.getAsDisplay(
                    player.getSkyBlockExperience().getLevel().getPreviousPrefixChanges().size(),
                    SkyBlockLevelRequirement.getAllPrefixChanges().size()
            ));

            return ItemStacks.item(Material.GRAY_DYE, 1, Text.of("<a>Prefix Color Rewards"), lore);
        });

        // Prefix items
        int index = 0;
        for (Map.Entry<SkyBlockLevelRequirement, String> entry : SkyBlockLevelRequirement.getAllPrefixChanges().entrySet()) {
            if (index >= SLOTS.length) break;
            SkyBlockLevelRequirement level = entry.getKey();
            int slot = SLOTS[index];

            layout.slot(slot, (s, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                boolean unlocked = player.getSkyBlockExperience().getLevel().asInt() >= level.asInt();

                Text name = Text.parse(level.getPrefix() + level.getPrefixDisplay());
                Text unlockLine = unlocked
                        ? Text.of("<a>You have unlocked this reward!")
                        : Text.of("<7>Levels left to unlock: <3>{}", level.asInt() - player.getSkyBlockExperience().getLevel().asInt());

                List<Text> lore = List.of(
                        Text.of("<8>Level {}", level.asInt()),
                        Text.literal(" "),
                        Text.of("<7>Preview: ").append(player.getFullDisplayName(level.getPrefix())),
                        Text.literal(" "),
                        unlockLine
                );

                return ItemStacks.item(level.getPrefixItem(), 1, name, lore);
            });

            index++;
        }
    }
}
