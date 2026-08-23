package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.HypixelSignGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.*;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointLoadouts;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.loadout.LoadoutManager;
import net.swofty.type.skyblockgeneric.loadout.LoadoutManager.TreeType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUILoadoutEdit extends StatelessView {
    private static final int[] ARMOR_SLOTS = {11, 20, 29, 38};
    private static final int[] EQUIPMENT_SLOTS = {10, 19, 28, 37};
    private static final String[] ARMOR_NAMES = {"Helmet", "Chestplate", "Leggings", "Boots"};
    private static final String[] EQUIPMENT_NAMES = {"Necklace", "Cloak", "Belt", "Gloves/Bracelet"};
    private final int index;

    public GUILoadoutEdit(int index) {
        this.index = index;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return ViewConfiguration.withText((_, ctx) -> Text.literal(LoadoutManager.data((SkyBlockPlayer) ctx.player()).getLoadouts()[index].getName()), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        DatapointLoadouts.Loadout loadout = LoadoutManager.data(player).getLoadouts()[index];

        for (int component = 0; component < 4; component++) {
            int piece = component;
            layout.slot(ARMOR_SLOTS[component], (s, c) -> armorItem((SkyBlockPlayer) c.player(), piece),
                    (_, c) -> c.push(new GUIWardrobe(index)));
            layout.slot(EQUIPMENT_SLOTS[component], (s, c) -> equipmentItem((SkyBlockPlayer) c.player(), piece),
                    (_, c) -> c.push(new GUIEquipmentSets(index, piece)));
        }

        layout.slot(21, (s, c) -> petItem((SkyBlockPlayer) c.player()), (_, c) -> c.push(new GUILoadoutPetSelection(index)));
        layout.slot(23, treeItem(loadout, TreeType.HOTM), (_, c) -> c.push(new GUITreeSlots(TreeType.HOTM, index)));
        layout.slot(24, treeItem(loadout, TreeType.HOTF), (_, c) -> c.push(new GUITreeSlots(TreeType.HOTF, index)));
        layout.slot(32, ItemStacks.head("71e1f6162db42245639609f728a4e134ed7bd7de3c15a7792d219a6e2a9db", """
                <a>Power Stone
                <7>Select a Power Stone to use in this
                <7>loadout!

                <7>Current: <8>None

                <e>Left-click to change!"""));
        layout.slot(33, ItemStacks.item(Material.COMPARATOR, 1, """
                <a>Stats Tuning Slot
                <7>Select a Stats Tuning template slot
                <7>to use in this loadout!

                <7>Current: <8>None

                <e>Left-click to change!"""));
        layout.slot(48, ItemStacks.item(Material.ARROW, 1, """
                <a>Go Back
                <7>To Loadouts"""), (_, c) -> c.pop());
        layout.slot(50, ItemStacks.item(Material.LAVA_BUCKET, 1, """
                        <c>Clear
                        <7>Clear all settings in this loadout,
                        <7>restoring it back to default.

                        <e>Click to clear!"""),
                (_, c) -> clear((SkyBlockPlayer) c.player(), c));
        layout.slot(51, ItemStacks.item(Material.NAME_TAG, 1, """
                <a>Rename Loadout
                <7>Want to feel a more personal
                <7>connection with your loadout slot?
                <7>Give it a name!

                <7>Current Name: <a>{}

                <e>Click to rename!""", loadout.getName()), (_, c) -> rename((SkyBlockPlayer) c.player()));
    }

    private ItemStack.Builder armorItem(SkyBlockPlayer player, int component) {
        DatapointLoadouts.Loadout loadout = LoadoutManager.data(player).getLoadouts()[index];
        SkyBlockItem item = LoadoutManager.loadoutArmor(player, loadout, component);
        if (item != null && !item.isNA()) return PlayerItemUpdater.playerUpdate(player, item.getItemStack());
        return ItemStacks.item(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1, """
                <7>Empty {} Slot

                <7>No armor selected for this loadout.

                <e>Left-click to change!""", ARMOR_NAMES[component]);
    }

    private ItemStack.Builder equipmentItem(SkyBlockPlayer player, int component) {
        SkyBlockItem item = LoadoutManager.data(player).getLoadouts()[index].getEquipment()[component];
        if (item != null && !item.isNA()) return PlayerItemUpdater.playerUpdate(player, item.getItemStack());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<8>> {}", component == 3 ? "Gloves" : EQUIPMENT_NAMES[component]));
        if (component == 3) lore.add(Text.of("<8>> Bracelet"));
        lore.add(Text.empty());
        lore.add(Text.of("<7>No equipment selected for this"));
        lore.add(Text.of("<7>loadout."));
        lore.add(Text.empty());
        lore.add(Text.of("<e>Left-click to change!"));
        return ItemStacks.item(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1,
                Text.of("<7>Empty Equipment {} Slot", component + 1), lore);
    }

    private ItemStack.Builder petItem(SkyBlockPlayer player) {
        String petType = LoadoutManager.data(player).getLoadouts()[index].getPetType();
        if (petType != null) {
            try {
                SkyBlockItem pet = player.getPetData().getPet(net.swofty.commons.skyblock.item.ItemType.valueOf(petType));
                if (pet != null) return PlayerItemUpdater.playerUpdate(player, pet.getItemStack());
            } catch (IllegalArgumentException ignored) {
            }
        }
        return ItemStacks.item(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1, """
                <a>Pet
                <7>Select a pet to use in this loadout!

                <7>Current: <8>None

                <e>Left-click to change!""");
    }

    private ItemStack.Builder treeItem(DatapointLoadouts.Loadout loadout, TreeType tree) {
        boolean hotm = tree == TreeType.HOTM;
        int selected = hotm ? loadout.getHotmSlot() : loadout.getHotfSlot();
        String title = hotm ? "Heart of the Mountain" : "Heart of the Forest";
        String texture = hotm ? "86f06eaa3004aeed09b3d5b45d976de584e691c0e9cade133635de93d23b9edb"
                : "5ef539b165125cfa46b06ffb9659e7cf89084bbd3ede1b314edc8f443343d61c";
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of(hotm ? "<7>Select a Heart of the Mountain to" : "<7>Select a Heart of the Forest to use"));
        lore.add(Text.of(hotm ? "<7>use in this loadout!" : "<7>in this loadout!"));
        lore.add(Text.empty());
        lore.add(selected < 0
                ? Text.of("<7>Selected: <8>None")
                : Text.of("<7>Selected: <a>{} {}", title, selected + 1));
        lore.add(Text.empty());
        lore.add(Text.of("<c>Swapping trees has a 10m cooldown!"));
        lore.add(Text.empty());
        lore.add(Text.of("<e>Left-click to change!"));
        return ItemStacks.head(texture, Text.of("<a>{} Slot", title), lore);
    }

    private void clear(SkyBlockPlayer player, ViewContext ctx) {
        DatapointLoadouts.LoadoutsData data = LoadoutManager.data(player);
        data.getLoadouts()[index] = new DatapointLoadouts.Loadout("Loadout " + (index + 1));
        if (data.getEquipped() == index) data.setEquipped(-1);
        LoadoutManager.save(player);
        ctx.session(DefaultState.class).refresh();
    }

    private void rename(SkyBlockPlayer player) {
        DatapointLoadouts.Loadout loadout = LoadoutManager.data(player).getLoadouts()[index];
        new HypixelSignGUI(player).open(new String[]{loadout.getName(), ""}).thenAccept(name -> {
            if (name != null && !name.isBlank()) {
                loadout.setName(name.trim());
                LoadoutManager.save(player);
            }
            player.openView(new GUILoadoutEdit(index));
        });
    }
}
