package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.StatefulView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointLoadouts;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.loadout.LoadoutManager;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class GUIEquipmentSets implements StatefulView<GUIEquipmentSets.EquipmentState> {
    private static final Material[] COLORS = {
            Material.RED_STAINED_GLASS_PANE, Material.ORANGE_STAINED_GLASS_PANE,
            Material.YELLOW_STAINED_GLASS_PANE, Material.LIME_STAINED_GLASS_PANE,
            Material.GREEN_STAINED_GLASS_PANE, Material.LIGHT_BLUE_STAINED_GLASS_PANE,
            Material.BLUE_STAINED_GLASS_PANE, Material.MAGENTA_STAINED_GLASS_PANE,
            Material.PURPLE_STAINED_GLASS_PANE
    };
    private static final String[] COMPONENTS = {"Necklace", "Cloak", "Belt", "Gloves/Bracelet"};
    private final int loadout;
    private final int component;

    public GUIEquipmentSets(int loadout, int component) {
        this.loadout = loadout;
        this.component = component;
    }

    @Override
    public EquipmentState initialState() {
        return new EquipmentState(0);
    }

    @Override
    public ViewConfiguration<EquipmentState> configuration() {
        return ViewConfiguration.withText((state, _) -> Text.of("({}/2) Equipment Sets", state.page + 1), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<EquipmentState> layout, EquipmentState state, ViewContext ctx) {
        Components.close(layout, 49);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        int start = state.page * 9;
        int unlocked = LoadoutManager.unlockedEquipmentSets(player);

        for (int column = 0; column < 9; column++) {
            int setIndex = start + column;
            boolean available = setIndex < unlocked;
            DatapointLoadouts.EquipmentSet set = LoadoutManager.data(player).getEquipmentSets()[setIndex];
            for (int row = 0; row < 4; row++) {
                int guiSlot = row * 9 + column;
                int equipmentComponent = row;
                if (!available) {
                    layout.slot(guiSlot, ItemStacks.item(Material.BLACK_STAINED_GLASS_PANE, 1, """
                            <7>Slot {}: <c>Locked
                            <7>Unlock this equipment set from
                            <d>Elizabeth <7>at the <b>Community Center<7>.""", setIndex + 1));
                } else {
                    layout.slot(guiSlot, (s, c) -> equipmentPiece((SkyBlockPlayer) c.player(), setIndex, equipmentComponent),
                            (_, c) -> editPiece((SkyBlockPlayer) c.player(), setIndex, equipmentComponent, c));
                }
            }
            if (available) {
                layout.slot(36 + column, ItemStacks.item(Material.GRAY_DYE, 1, """
                                Slot {}:<a> Ready
                                <7>This slot is ready to be selected.

                                <e>Click to equip to loadout!""", setIndex + 1),
                        (_, c) -> select((SkyBlockPlayer) c.player(), setIndex));
            } else {
                layout.slot(36 + column, ItemStacks.item(Material.RED_DYE, 1, """
                        <7>Slot {}: <c>Locked
                        <7>This equipment set is locked.

                        <c>Unlock more slots from <d>Elizabeth <c>at
                        <c>the <b>Community Center""", setIndex + 1));
            }
        }

        if (state.page > 0) {
            layout.slot(45, ItemStacks.item(Material.ARROW, 1, """
                            <a>Previous Page
                            <e>Page 1"""),
                    (_, c) -> c.session(EquipmentState.class).update(s -> new EquipmentState(0)));
        }
        if (state.page < 1) {
            layout.slot(53, ItemStacks.item(Material.ARROW, 1, """
                            <a>Next Page
                            <e>Page 2"""),
                    (_, c) -> c.session(EquipmentState.class).update(s -> new EquipmentState(1)));
        }
        layout.slot(48, ItemStacks.item(Material.ARROW, 1, """
                <a>Go Back
                <7>To Loadout {}""", loadout + 1), (_, c) -> c.pop());
        layout.slot(50, ItemStacks.item(Material.LAVA_BUCKET, 1, """
                        <c>Clear Selection
                        <7>Clears your current selection for
                        <7>this component of your loadout.

                        <e>Click to clear!"""),
                (_, c) -> clear((SkyBlockPlayer) c.player()));
    }

    private ItemStack.Builder equipmentPiece(SkyBlockPlayer player, int setIndex, int equipmentComponent) {
        SkyBlockItem item = LoadoutManager.data(player).getEquipmentSets()[setIndex].getPieces()[equipmentComponent];
        if (item != null && !item.isNA()) return PlayerItemUpdater.playerUpdate(player, item.getItemStack());
        List<Text> lore = switch (equipmentComponent) {
            case 0 -> List.of(Text.of("<7>Place a necklace here to add it to"), Text.of("<7>this set."));
            case 1 -> List.of(Text.of("<7>Place a cloak here to add it to this"), Text.of("<7>set."));
            case 2 -> List.of(Text.of("<7>Place a belt here to add it to this set."));
            default -> List.of(Text.of("<7>Place a pair of gloves or a bracelet"), Text.of("<7>here to add it to this set."));
        };
        return ItemStacks.item(COLORS[setIndex % 9], 1,
                Text.of("<a>Slot {} {}", setIndex + 1, COMPONENTS[equipmentComponent]), lore);
    }

    private void editPiece(SkyBlockPlayer player, int setIndex, int equipmentComponent, ViewContext ctx) {
        DatapointLoadouts.EquipmentSet set = LoadoutManager.data(player).getEquipmentSets()[setIndex];
        SkyBlockItem stored = set.getPieces()[equipmentComponent];
        ItemStack cursor = player.getInventory().getCursorItem();
        if (cursor.isAir()) {
            if (stored == null || stored.isNA()) return;
            player.getInventory().setCursorItem(stored.getItemStack());
            set.getPieces()[equipmentComponent] = null;
        } else {
            SkyBlockItem cursorItem = new SkyBlockItem(cursor);
            if (!LoadoutManager.acceptsEquipment(equipmentComponent, cursorItem)) {
                player.sendMessage("<c>That item does not fit in this equipment slot!");
                return;
            }
            set.getPieces()[equipmentComponent] = new SkyBlockItem(cursor.withAmount(1));
            player.getInventory().setCursorItem(stored == null || stored.isNA() ? ItemStack.AIR : stored.getItemStack());
            if (cursor.amount() > 1) player.addAndUpdateItem(cursor.withAmount(cursor.amount() - 1));
        }
        LoadoutManager.save(player);
        ctx.session(EquipmentState.class).refresh();
    }

    private void select(SkyBlockPlayer player, int setIndex) {
        SkyBlockItem selected = LoadoutManager.data(player).getEquipmentSets()[setIndex].getPieces()[component];
        LoadoutManager.data(player).getLoadouts()[loadout].getEquipment()[component] = copy(selected);
        LoadoutManager.save(player);
        player.openView(new GUILoadoutEdit(loadout));
    }

    private void clear(SkyBlockPlayer player) {
        LoadoutManager.data(player).getLoadouts()[loadout].getEquipment()[component] = null;
        LoadoutManager.save(player);
        player.openView(new GUILoadoutEdit(loadout));
    }

    private static SkyBlockItem copy(SkyBlockItem item) {
        return item == null || item.isNA() ? null : new SkyBlockItem(item.toUnderstandable());
    }

    public record EquipmentState(int page) {
    }
}
