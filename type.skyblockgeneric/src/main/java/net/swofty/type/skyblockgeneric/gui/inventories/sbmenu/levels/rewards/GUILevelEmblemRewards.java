package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.levels.rewards;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.*;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.levels.emblem.GUIEmblems;
import net.swofty.type.skyblockgeneric.levels.SkyBlockEmblems;
import net.swofty.type.skyblockgeneric.levels.causes.LevelCause;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUILevelEmblemRewards extends StatelessView {
    private static final int[] SLOTS = new int[]{
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43,
    };

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Emblem Rewards", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        // View Emblems button
        layout.slot(50, (s, c) -> ItemStacks.item(Material.NAME_TAG, 1, """
                        <a>Prefix Emblems
                        <7>Add some spice by having an emblem
                        <7>next to your name in chat and in tab!

                        <7>Emblems are unlocked through various
                        <7>activities such as leveling up
                        <7>or completing achievements!

                        <7>Emblems also show important data
                        <7>associated with them in chat!

                        <e>Click to view!"""),
                (click, c) -> c.player().openView(new GUIEmblems()));

        // Title item
        layout.slot(4, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            List<Text> lore = new ArrayList<>();
            lore.add(Text.of("<7>Emblems to show next to your name"));
            lore.add(Text.of("<7>that signify special achievements."));
            lore.add(Text.literal(" "));
            lore.add(Text.of("<7>Next Reward:"));

            List<SkyBlockEmblems.SkyBlockEmblem> levelEmblems = SkyBlockEmblems.getEmblemsWithLevelCause();
            SkyBlockEmblems.SkyBlockEmblem nextEmblem = null;
            for (SkyBlockEmblems.SkyBlockEmblem emblem : levelEmblems) {
                if (player.getSkyBlockExperience().hasExperienceFor(emblem.cause())) continue;
                nextEmblem = emblem;
                break;
            }

            if (nextEmblem == null) {
                lore.add(Text.of("<c>No more rewards!"));
            } else {
                lore.add(Text.of("<f>{} ", nextEmblem.displayName()).append(nextEmblem.emblem()));
                lore.add(Text.of("<8>at Level {}", ((LevelCause) nextEmblem.cause()).getLevel()));
            }

            lore.add(Text.literal(" "));
            lore.addAll(GUILevelRewards.getAsDisplay(
                    player.getSkyBlockExperience().getOfType(LevelCause.class).size(),
                    levelEmblems.size()
            ));

            return ItemStacks.item(Material.NAME_TAG, 1, Text.of("<a>Emblem Rewards"), lore);
        });

        // Emblem items
        int index = 0;
        for (SkyBlockEmblems.SkyBlockEmblem emblem : SkyBlockEmblems.getEmblemsWithLevelCause()) {
            if (index >= SLOTS.length) break;
            int slot = SLOTS[index];

            layout.slot(slot, (s, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                Text name = Text.of("{} ", emblem.displayName()).append(emblem.emblem());

                Text unlockLine = player.getSkyBlockExperience().hasExperienceFor(emblem.cause())
                        ? Text.of("<a>You have unlocked this reward!")
                        : Text.of("<7>Levels left to unlock: <3>{}",
                                ((LevelCause) emblem.cause()).getLevel() - player.getSkyBlockExperience().getLevel().asInt());

                List<Text> lore = List.of(
                        Text.of("<8>Level {}", ((LevelCause) emblem.cause()).getLevel()),
                        Text.literal(" "),
                        Text.of("<7>Preview: ").append(player.getFullDisplayName(emblem)),
                        Text.literal(" "),
                        unlockLine
                );

                return ItemStacks.item(Material.NAME_TAG, 1, name, lore);
            });
            index++;
        }
    }
}
