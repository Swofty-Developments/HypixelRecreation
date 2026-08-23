package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.storage;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.PaginatedView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointStorage;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIStorageIconSelection extends PaginatedView<Material, GUIStorageIconSelection.IconSelectionState> {
    private static final int[] PAGINATED_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private final int page;

    public GUIStorageIconSelection(int page) {
        this.page = page;
    }

    @Override
    public ViewConfiguration<IconSelectionState> configuration() {
        return ViewConfiguration.withText(
                (state, ctx) -> {
                    int totalPages = Math.max(1, (int) Math.ceil((double) getFilteredItems(state).size() / PAGINATED_SLOTS.length));
                    return Text.key("gui_sbmenu.storage.icon.title", state.page() + 1, totalPages);
                },
                InventoryType.CHEST_6_ROW
        );
    }

    public static IconSelectionState initialState() {
        List<Material> items = new ArrayList<>();
        items.add(Material.BARRIER);

        List<Material> vanilla = new ArrayList<>(Material.values().stream().toList());
        vanilla.removeIf((element) -> ItemType.isVanillaReplaced(element.name()));
        items.addAll(vanilla);

        return new IconSelectionState(items, 0);
    }

    @Override
    protected int[] getPaginatedSlots() {
        return PAGINATED_SLOTS;
    }

    @Override
    protected int getNextPageSlot() {
        return 53;
    }

    @Override
    protected int getPreviousPageSlot() {
        return 45;
    }

    @Override
    protected ItemStack.Builder renderItem(Material item, int index, HypixelPlayer player) {
        Text name = item == Material.BARRIER
                ? Text.key("gui_sbmenu.storage.icon.reset")
                : Text.literal(StringUtility.toNormalCase(item.name().replace("minecraft:", "")));
        return ItemStacks.item(item, 1, name, Text.keyLines("gui_sbmenu.storage.icon.lore"));
    }

    @Override
    protected void onItemClick(ClickContext<IconSelectionState> click, ViewContext ctx, Material item, int index) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        DatapointStorage.PlayerStorage storage = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.STORAGE, DatapointStorage.class).getValue();

        if (item == Material.BARRIER) {
            storage.setDisplay(page, Material.PURPLE_STAINED_GLASS_PANE);
        } else {
            storage.setDisplay(page, item);
        }

        player.openView(new GUIStorage());
    }

    @Override
    protected boolean shouldFilterFromSearch(IconSelectionState state, Material item) {
        return false;
    }

    @Override
    protected void layoutCustom(ViewLayout<IconSelectionState> layout, IconSelectionState state, ViewContext ctx) {
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);
    }

    public record IconSelectionState(List<Material> items, int page) implements PaginatedState<Material> {
        @Override
        public PaginatedState<Material> withPage(int page) {
            return new IconSelectionState(items, page);
        }

        @Override
        public PaginatedState<Material> withItems(List<Material> items) {
            return new IconSelectionState(items, page);
        }
    }
}
