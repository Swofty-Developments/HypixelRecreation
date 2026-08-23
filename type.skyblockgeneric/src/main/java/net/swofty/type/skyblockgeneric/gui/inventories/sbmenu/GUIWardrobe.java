package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.*;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointLoadouts;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointWardrobe;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.loadout.LoadoutManager;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.wardrobe.WardrobeService;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class GUIWardrobe implements StatefulView<GUIWardrobe.WardrobeState> {
    private final Integer selectingLoadout;

    public GUIWardrobe() {
        this.selectingLoadout = null;
    }

    public GUIWardrobe(int selectingLoadout) {
        this.selectingLoadout = selectingLoadout;
    }
    private static final Material[] EMPTY = {
        Material.RED_STAINED_GLASS_PANE, Material.ORANGE_STAINED_GLASS_PANE,
        Material.YELLOW_STAINED_GLASS_PANE, Material.LIME_STAINED_GLASS_PANE,
        Material.GREEN_STAINED_GLASS_PANE, Material.LIGHT_BLUE_STAINED_GLASS_PANE,
        Material.BLUE_STAINED_GLASS_PANE, Material.MAGENTA_STAINED_GLASS_PANE,
        Material.PURPLE_STAINED_GLASS_PANE
    };

    @Override
    public WardrobeState initialState() {
        return new WardrobeState(0);
    }

    @Override
    public ViewConfiguration<WardrobeState> configuration() {
        return ViewConfiguration.withText((state, _) -> Text.of("Wardrobe ({}/3)", state.page + 1), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<WardrobeState> layout, WardrobeState state, ViewContext ctx) {
        Components.fill(layout);

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        DatapointWardrobe.WardrobeData data = data(player);
        int selectedForLoadout = selectingLoadout == null ? -1
                : LoadoutManager.data(player).getLoadouts()[selectingLoadout].getArmorSet();
        int start = state.page * 9;

        for (int column = 0; column < 9; column++) {
            int setIndex = start + column;
            boolean unlocked = WardrobeService.isUnlocked(setIndex, player.getRank(), data);
            DatapointWardrobe.ArmorSet set = data.getSets()[setIndex];
            for (int piece = 0; piece < 4; piece++) {
                int guiSlot = piece * 9 + column;
                if (!unlocked) {
                    layout.slot(guiSlot, ItemStacks.item(Material.BLACK_STAINED_GLASS_PANE, 1, """
                            <7>Slot {}: <c>Locked

                            <7>Unlock more slots from:
                            <8>▶ <a>Account Upgrades <8>- <6>9 Slots

                            <c>Unlock more slots from <d>Elizabeth <c>at
                            <c>the <b>Community Center""", setIndex + 1));
                    continue;
                }
                SkyBlockItem item = set.getPieces()[piece];
                if (data.getEquippedSlot() == setIndex) {
                    int pieceIndex = piece;
                    layout.slot(guiSlot, (s, c) -> {
                            SkyBlockItem worn = ((SkyBlockPlayer) c.player()).getArmor()[pieceIndex];
                            return worn == null || worn.isNA()
                                ? ItemStack.AIR.builder()
                                : PlayerItemUpdater.playerUpdate((SkyBlockPlayer) c.player(), worn.getItemStack());
                        },
                        (_, c) -> c.player().sendMessage("<c>You cannot modify your equipped armor set!"));
                } else if (selectingLoadout != null && selectedForLoadout == setIndex) {
                    if (item == null || item.isNA()) {
                        layout.slot(guiSlot, emptyPiece(setIndex, column, piece));
                    } else {
                        layout.slot(guiSlot,
                                (s, c) -> PlayerItemUpdater.playerUpdate((SkyBlockPlayer) c.player(), item.getItemStack()),
                                (_, c) -> c.player().sendMessage("<c>You cannot modify the armor set selected for this loadout!"));
                    }
                } else if (item == null || item.isNA()) {
                    int pieceIndex = piece;
                    layout.slot(guiSlot, emptyPiece(setIndex, column, piece),
                        (_, c) -> placeStoredPiece((SkyBlockPlayer) c.player(), setIndex, pieceIndex, c));
                } else {
                    int pieceIndex = piece;
                    layout.slot(guiSlot,
                        (s, c) -> PlayerItemUpdater.playerUpdate((SkyBlockPlayer) c.player(), item.getItemStack()),
                        (click, c) -> handleStoredPiece(
                            (SkyBlockPlayer) c.player(), setIndex, pieceIndex, click.click(), c));
                }
            }

            int controlSlot = 36 + column;
            if (!unlocked) {
                layout.slot(controlSlot, ItemStacks.item(Material.RED_DYE, 1, """
                        <7>Slot {}: <c>Locked
                        <7>This wardrobe slot is locked and
                        <7>cannot be used.

                        <7>Unlock more slots from:
                        <8>▶ <a>Account Upgrades <8>- <6>9 Slots

                        <c>Unlock more slots from <d>Elizabeth <c>at
                        <c>the <b>Community Center""", setIndex + 1));
            } else {
                layout.slot(controlSlot, (s, c) -> selectingLoadout != null && selectedForLoadout == setIndex
                        ? selectedControl(setIndex) : control(setIndex, set, data), (_, c) -> {
                    if (selectingLoadout != null) {
                        if (selectedForLoadout == setIndex) {
                            c.player().sendMessage("<c>This armor set is already selected for the loadout!");
                            return;
                        }
                        selectForLoadout((SkyBlockPlayer) c.player(), setIndex);
                        return;
                    }
                    savePage((SkyBlockPlayer) c.player(), c, state.page);
                    toggle((SkyBlockPlayer) c.player(), setIndex);
                    c.session(WardrobeState.class).refresh();
                });
            }
        }

        if (state.page > 0) {
            layout.slot(45, ItemStacks.item(Material.ARROW, 1, """
                    <a>Previous Page
                    <e>Page {}""", state.page),
                (_, c) -> {
                    savePage((SkyBlockPlayer) c.player(), c, state.page);
                    c.session(WardrobeState.class).update(s -> new WardrobeState(s.page - 1));
                });
        }
        Components.back(layout, 48, ctx);
        if (selectingLoadout != null) {
            layout.slot(50, ItemStacks.item(Material.LAVA_BUCKET, 1, """
                            <c>Clear Selection
                            <7>Clears your current selection for
                            <7>this component of your loadout.

                            <e>Click to clear!"""),
                    (_, c) -> clearLoadoutArmor((SkyBlockPlayer) c.player()));
        }
        Components.close(layout, 49);
        if (state.page < 2) {
            layout.slot(53, ItemStacks.item(Material.ARROW, 1, """
                    <a>Next Page
                    <e>Page {}""", state.page + 2),
                (_, c) -> {
                    savePage((SkyBlockPlayer) c.player(), c, state.page);
                    c.session(WardrobeState.class).update(s -> new WardrobeState(s.page + 1));
                });
        }
        layout.allowHotkey(true);
    }

    @Override
    public boolean onBottomClick(ClickContext<WardrobeState> click, ViewContext ctx) {
        if (!(click.click() instanceof Click.LeftShift) && !(click.click() instanceof Click.RightShift)) {
            return true;
        }

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        ItemStack clickedStack = player.getInventory().getItemStack(click.slot());
        SkyBlockItem clickedItem = new SkyBlockItem(clickedStack);

        if (clickedItem.isNA()) {
            return false;
        }

        int piece = armorPiece(clickedItem);
        if (piece == -1) {
            player.sendMessage("<c>Only armor can be placed in the Wardrobe!");
            return false;
        }

        DatapointWardrobe.WardrobeData data = data(player);
        int targetSet = findClosestAvailableSet(player, data, click.state().page, piece);

        if (targetSet == -1) {
            player.sendMessage("<c>There are no available Wardrobe slots for that item!");
            return false;
        }

        ItemStack one = clickedStack.withAmount(1);
        SkyBlockItem stored = new SkyBlockItem(one);

        data.getSets()[targetSet].getPieces()[piece] = stored;

        player.getInventory().setItemStack(click.slot(),
            clickedStack.amount() == 1 ? ItemStack.AIR : clickedStack.withAmount(clickedStack.amount() - 1)
        );

        int pageStart = click.state().page * 9;
        if (targetSet >= pageStart && targetSet < pageStart + 9) {
            int column = targetSet - pageStart;
            int guiSlot = piece * 9 + column;

            ctx.inventory().setItemStack(guiSlot,
                PlayerItemUpdater.playerUpdate(player, one).build()
            );
        }

        save(player);
        ctx.session(WardrobeState.class).refresh();
        return false;
    }

    private int findClosestAvailableSet(SkyBlockPlayer player, DatapointWardrobe.WardrobeData data,
                                        int page, int piece) {
        int pageStart = page * 9;
        for (int distance = 0; distance < data.getSets().length; distance++) {
            int setIndex = (pageStart + distance) % data.getSets().length;
            if (!WardrobeService.isUnlocked(setIndex, player.getRank(), data)
                    || data.getEquippedSlot() == setIndex || isSelectedForLoadout(player, setIndex)) {
                continue;
            }

            SkyBlockItem stored = data.getSets()[setIndex].getPieces()[piece];
            if (stored == null || stored.isNA()) {
                return setIndex;
            }
        }
        return -1;
    }

    private int armorPiece(SkyBlockItem item) {
        for (int piece = 0; piece < 4; piece++) {
            if (WardrobeService.accepts(piece, item)) {
                return piece;
            }
        }
        return -1;
    }

    @Override
    public void onClose(WardrobeState state, ViewContext ctx, ViewSession.CloseReason reason) {
        savePage((SkyBlockPlayer) ctx.player(), ctx, state.page);
    }

    private void savePage(SkyBlockPlayer player, ViewContext ctx, int page) {
        DatapointWardrobe.WardrobeData data = data(player);
        for (int column = 0; column < 9; column++) {
            int setIndex = page * 9 + column;
            if (!WardrobeService.isUnlocked(setIndex, player.getRank(), data)) continue;
            for (int piece = 0; piece < 4; piece++) {
                SkyBlockItem item = new SkyBlockItem(ctx.inventory().getItemStack(piece * 9 + column));
                if (data.getEquippedSlot() == setIndex) {
                    setArmorPiece(player, piece, item);
                } else if (!isPanel(item)) {
                    data.getSets()[setIndex].getPieces()[piece] = item.isNA() ? null : item;
                }
            }
        }
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.WARDROBE, DatapointWardrobe.class).setValue(data);
    }

    private void toggle(SkyBlockPlayer player, int slot) {
        DatapointWardrobe.WardrobeData data = data(player);
        DatapointWardrobe.ArmorSet target = data.getSets()[slot];
        if (data.getEquippedSlot() != slot && target.isEmpty()) {
            player.sendMessage("<c>You cannot equip an empty wardrobe slot!");
            return;
        }
        SkyBlockItem[] worn = player.getArmor();
        if (data.getEquippedSlot() == slot) {
            target.setPieces(worn);
            setArmor(player, new SkyBlockItem[4]);
            data.setEquippedSlot(-1);
        } else {
            SkyBlockItem[] stored = target.getPieces().clone();
            boolean storedComplete = target.isComplete();

            target.setPieces(worn);
            setArmor(player, stored);
            data.setEquippedSlot(slot);

            if (storedComplete && target.getFirstWorn() == 0) {
                target.setFirstWorn(System.currentTimeMillis());
            }
        }
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.WARDROBE, DatapointWardrobe.class).setValue(data);
    }

    private void selectForLoadout(SkyBlockPlayer player, int slot) {
        DatapointWardrobe.WardrobeData wardrobe = data(player);
        DatapointWardrobe.ArmorSet source = wardrobe.getSets()[slot];
        if (source.isEmpty() && wardrobe.getEquippedSlot() != slot) {
            player.sendMessage("<c>You cannot select an empty wardrobe slot!");
            return;
        }
        SkyBlockItem[] armor = new SkyBlockItem[4];
        for (int i = 0; i < armor.length; i++) {
            SkyBlockItem piece = wardrobe.getEquippedSlot() == slot ? player.getArmor()[i] : source.getPieces()[i];
            armor[i] = piece == null || piece.isNA() ? null : new SkyBlockItem(piece.toUnderstandable());
        }
        DatapointLoadouts.Loadout loadout = LoadoutManager.data(player).getLoadouts()[selectingLoadout];
        loadout.setArmor(armor);
        loadout.setArmorSet(slot);
        LoadoutManager.save(player);
        player.openView(new GUILoadoutEdit(selectingLoadout));
    }

    private void clearLoadoutArmor(SkyBlockPlayer player) {
        DatapointLoadouts.Loadout loadout = LoadoutManager.data(player).getLoadouts()[selectingLoadout];
        loadout.setArmor(new SkyBlockItem[4]);
        loadout.setArmorSet(-1);
        LoadoutManager.save(player);
        player.openView(new GUILoadoutEdit(selectingLoadout));
    }

    private void setArmor(SkyBlockPlayer player, SkyBlockItem[] pieces) {
        player.setHelmet(stack(pieces[0]));
        player.setChestplate(stack(pieces[1]));
        player.setLeggings(stack(pieces[2]));
        player.setBoots(stack(pieces[3]));
    }

    private void handleStoredPiece(SkyBlockPlayer player, int setIndex, int piece, Click click, ViewContext ctx) {
        DatapointWardrobe.WardrobeData data = data(player);
        SkyBlockItem stored = data.getSets()[setIndex].getPieces()[piece];
        if (stored == null || stored.isNA()) {
            ctx.session(WardrobeState.class).refresh();
            return;
        }

        if (click instanceof Click.LeftShift || click instanceof Click.RightShift) {
            player.addAndUpdateItem(stored);
            data.getSets()[setIndex].getPieces()[piece] = null;
            save(player);
            ctx.session(WardrobeState.class).refresh();
            return;
        }

        if (!(click instanceof Click.Left) && !(click instanceof Click.Right)) {
            return;
        }

        ItemStack cursor = player.getInventory().getCursorItem();
        if (cursor.isAir()) {
            player.getInventory().setCursorItem(stored.getItemStack());
            data.getSets()[setIndex].getPieces()[piece] = null;
            save(player);
            ctx.session(WardrobeState.class).refresh();
            return;
        }

        SkyBlockItem cursorItem = new SkyBlockItem(cursor);
        if (!WardrobeService.accepts(piece, cursorItem)) {
            player.sendMessage("<c>That item does not fit in this Wardrobe slot!");
            return;
        }

        data.getSets()[setIndex].getPieces()[piece] = new SkyBlockItem(cursor.withAmount(1));
        if (cursor.amount() == 1) {
            player.getInventory().setCursorItem(stored.getItemStack());
        } else {
            player.getInventory().setCursorItem(cursor.withAmount(cursor.amount() - 1));
            player.addAndUpdateItem(stored);
        }

        save(player);
        ctx.session(WardrobeState.class).refresh();
    }

    private void placeStoredPiece(SkyBlockPlayer player, int setIndex, int piece, ViewContext ctx) {
        ItemStack cursor = player.getInventory().getCursorItem();
        SkyBlockItem item = new SkyBlockItem(cursor);
        if (item.isNA()) return;

        if (!WardrobeService.accepts(piece, item)) {
            player.sendMessage("<c>That item does not fit in this Wardrobe slot!");
            return;
        }

        ItemStack one = cursor.withAmount(1);
        SkyBlockItem stored = new SkyBlockItem(one);

        data(player).getSets()[setIndex].getPieces()[piece] = stored;

        int guiSlot = piece * 9 + (setIndex % 9);

        ctx.inventory().setItemStack(guiSlot,
            PlayerItemUpdater.playerUpdate(player, one).build()
        );

        int remaining = cursor.amount() - 1;
        player.getInventory().setCursorItem(
            remaining <= 0 ? ItemStack.AIR : cursor.withAmount(remaining)
        );

        save(player);
        ctx.session(WardrobeState.class).refresh();
    }

    private void setArmorPiece(SkyBlockPlayer player, int piece, SkyBlockItem item) {
        ItemStack stack = stack(item);
        switch (piece) {
            case 0 -> player.setHelmet(stack);
            case 1 -> player.setChestplate(stack);
            case 2 -> player.setLeggings(stack);
            case 3 -> player.setBoots(stack);
            default -> throw new IllegalArgumentException("Invalid armor piece: " + piece);
        }
    }

    private boolean isPanel(SkyBlockItem item) {
        if (item == null || item.isNA()) return false;
        for (Material material : EMPTY) {
            if (item.getMaterial() == material) return true;
        }
        return false;
    }

    private void save(SkyBlockPlayer player) {
        player.getSkyblockDataHandler()
            .get(SkyBlockDataHandler.Data.WARDROBE, DatapointWardrobe.class)
            .setValue(data(player));
    }

    private ItemStack stack(SkyBlockItem item) {
        return item == null || item.isNA() ? ItemStack.AIR : item.getItemStack();
    }

    private ItemStack.Builder control(int index, DatapointWardrobe.ArmorSet set, DatapointWardrobe.WardrobeData data) {
        if (data.getEquippedSlot() == index) {
            return ItemStacks.item(Material.LIME_DYE, 1, """
                    <7>Slot {}: <a>Equipped
                    <7>This wardrobe slot contains your
                    <7>current armor set.

                    <e>Click to unequip this armor set""", index + 1);
        }
        if (set.isEmpty()) {
            return ItemStacks.item(Material.GRAY_DYE, 1, """
                    <7>Slot {}: <c>Empty
                    <7>This wardrobe slot contains no
                    <7>armor""", index + 1);
        }
        if (set.getFirstWorn() > 0) {
            return ItemStacks.item(Material.PINK_DYE, 1, """
                    <7>Slot {}: <a>Ready
                    <7>This wardrobe slot is ready to be
                    <7>equipped.

                    <b>Full Set First Worn
                    <7>{}

                    <e>Click to equip this armor set""",
                    index + 1, new SimpleDateFormat("MMM d, yyyy").format(new Date(set.getFirstWorn())));
        }
        return ItemStacks.item(Material.PINK_DYE, 1, """
                <7>Slot {}: <a>Ready
                <7>This wardrobe slot is ready to be
                <7>equipped.

                <e>Click to equip this armor set""", index + 1);
    }

    private ItemStack.Builder selectedControl(int index) {
        return ItemStacks.item(Material.LIME_DYE, 1, """
                <7>Slot {}: <a>Selected
                <7>This armor set is selected for the
                <7>loadout you are editing.

                <c>Change the loadout selection before
                <c>modifying this armor set.""", index + 1);
    }

    private ItemStack.Builder emptyPiece(int setIndex, int column, int piece) {
        return ItemStacks.item(EMPTY[column], 1,
                Text.of("<a>Slot {} {}", setIndex + 1, pieceName(piece)),
                List.of(
                        Text.of(piece < 2 ? "<7>Place a {} here to add it to the" : "<7>Place a pair of {} here to add",
                                pieceName(piece).toLowerCase()),
                        Text.of(piece < 2 ? "<7>armor set" : "<7>them to the armor set")));
    }

    private boolean isSelectedForLoadout(SkyBlockPlayer player, int setIndex) {
        return selectingLoadout != null
                && LoadoutManager.data(player).getLoadouts()[selectingLoadout].getArmorSet() == setIndex;
    }

    private DatapointWardrobe.WardrobeData data(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.WARDROBE, DatapointWardrobe.class).getValue();
    }

    private static String pieceName(int piece) {
        return switch (piece) {
            case 0 -> "Helmet";
            case 1 -> "Chestplate";
            case 2 -> "Leggings";
            default -> "Boots";
        };
    }

    public record WardrobeState(int page) {
    }
}
