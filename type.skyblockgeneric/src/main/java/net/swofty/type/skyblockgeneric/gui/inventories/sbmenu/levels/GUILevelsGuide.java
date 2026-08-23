package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.levels;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.levels.LevelsGuide;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUILevelsGuide extends StatelessView {
    private final LevelsGuide guide;

    private static final int[] BORDER_SLOTS = {
            9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 26, 27, 35, 36, 44, 45, 46, 47, 51, 52, 53
    };

    private static final int[] TASK_SLOTS = {
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    public GUILevelsGuide(LevelsGuide guide) {
        this.guide = guide;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.withText(
            (state, ctx) -> Text.key("gui_sbmenu.levels.guide.title", StringUtility.toNormalCase(guide.name())),
                InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        // Border slots with colored glass
        for (int slot : BORDER_SLOTS) {
            layout.slot(slot, ItemStacks.named(guide.getGlassMaterial(), ""));
        }

        // Guide info
        layout.slot(50, (s, c) -> ItemStacks.item(Material.REDSTONE_TORCH, 1, Text.key("gui_sbmenu.levels.guide.info"),
                Text.keyLines("gui_sbmenu.levels.guide.info.lore")));

        // Task items
        LevelsGuide.TasksSet[] tasks = guide.getTasksSets().toArray(new LevelsGuide.TasksSet[0]);
        for (int i = 0; i < TASK_SLOTS.length && i < tasks.length; i++) {
            int slot = TASK_SLOTS[i];
            LevelsGuide.TasksSet task = tasks[i];

            layout.slot(slot, (s, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                List<Text> lore = new ArrayList<>();

                if (task.getCauses().size() > 1) {
                    lore.add(Text.key("gui_sbmenu.levels.guide.tasks", task.getCauses().size()));
                    lore.add(Text.empty());
                }

                task.getDisplay().apply(player).forEach(line -> lore.add(Text.parse(line)));

                return ItemStacks.lore(ItemStacks.copy(task.getMaterial().build()), lore);
            }, (click, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                player.openView(task.getGuiToOpen());
            });
        }

    }
}
