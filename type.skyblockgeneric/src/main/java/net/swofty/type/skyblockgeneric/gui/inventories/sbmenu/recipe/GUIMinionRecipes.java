package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.recipe;

import net.minestom.server.inventory.InventoryType;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.attribute.attributes.ItemAttributeMinionData;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.minion.MinionRegistry;
import net.swofty.type.skyblockgeneric.minion.SkyBlockMinion;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GUIMinionRecipes extends StatelessView {
    private static final Map<Integer, int[]> SLOTS = new HashMap<>(Map.of(
            10, new int[]{11, 12, 13, 14, 15, 20, 21, 22, 23, 24},
            11, new int[]{11, 12, 13, 14, 15, 21, 22, 23, 30, 31, 32}
    ));

    private final MinionRegistry minionRegistry;

    public GUIMinionRecipes(MinionRegistry minionRegistry) {
        this.minionRegistry = minionRegistry;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.key("gui_sbmenu.recipe.minion.title",
                StringUtility.toNormalCase(minionRegistry.toString())), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        List<SkyBlockMinion.MinionTier> craftableMinionTiers = new ArrayList<>();
        for (SkyBlockMinion.MinionTier minionTier : minionRegistry.asSkyBlockMinion().getTiers()) {
            if (minionTier.craftable()) craftableMinionTiers.add(minionTier);
        }

        int[] slots = SLOTS.getOrDefault(craftableMinionTiers.size(), new int[]{});
        for (int i = 0; i < craftableMinionTiers.size() && i < slots.length; i++) {
            SkyBlockMinion.MinionTier minionTier = craftableMinionTiers.get(i);
            int slot = slots[i];

            SkyBlockItem minion = new SkyBlockItem(minionRegistry.getItemType());
            minion.getAttributeHandler().setMinionData(new ItemAttributeMinionData.MinionData(minionTier.tier(), 0));

            layout.slot(slot, (s, c) -> ItemStacks.head(minionTier.texture(),
                            Text.literal(minion.getDisplayName()),
                            minion.getLore().stream().map(Text::literal).toList()),
                    (click, c) -> c.player().openView(new GUIRecipe(minion, minionTier.tier() - 1)));
        }
    }
}
