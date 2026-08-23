package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.levels;

import net.minestom.server.inventory.InventoryType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.levels.SkyBlockLevelRequirement;
import net.swofty.type.skyblockgeneric.levels.SkyBlockLevelUnlock;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GUISkyBlockLevel extends StatelessView {
    private static final Map<Integer, List<Integer>> SLOTS_MAP = new HashMap<>(
            Map.of(
                    1, List.of(13),
                    2, List.of(12, 14),
                    3, List.of(11, 13, 15),
                    4, List.of(10, 12, 14, 16),
                    5, List.of(11, 12, 13, 14, 15)
            )
    );

    private final SkyBlockLevelRequirement levelRequirement;

    public GUISkyBlockLevel(SkyBlockLevelRequirement levelRequirement) {
        this.levelRequirement = levelRequirement;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.withText(
            (state, ctx) -> Text.key("gui_sbmenu.levels.level.title", levelRequirement.asInt()),
                InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 31);
        Components.back(layout, 30, ctx);

        List<SkyBlockLevelUnlock> unlocks = levelRequirement.getUnlocks();
        List<Integer> slots = SLOTS_MAP.get(unlocks.size());

        if (slots != null) {
            for (int i = 0; i < unlocks.size(); i++) {
                SkyBlockLevelUnlock unlock = unlocks.get(i);
                int slot = slots.get(i);

                layout.slot(slot, (s, c) -> {
                    SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                    return unlock.getItemDisplay(player, levelRequirement.asInt());
                });
            }
        }
    }
}
