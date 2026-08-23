package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
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
import net.swofty.type.skyblockgeneric.loadout.LoadoutManager.TreeType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUILoadouts implements StatefulView<GUILoadouts.LoadoutsState> {
    private static final int[] LOADOUT_SLOTS = {14, 15, 16, 23, 24, 25, 32, 33, 34, 41, 42, 43};
    private static final String[] ARMOR_NAMES = {"Helmet", "Chestplate", "Leggings", "Boots"};
    private static final String[] EQUIPMENT_NAMES = {"Necklace", "Cloak", "Belt", "Gloves/Bracelet"};

    @Override
    public LoadoutsState initialState() {
        return new LoadoutsState(0);
    }

    @Override
    public ViewConfiguration<LoadoutsState> configuration() {
        return ViewConfiguration.withText((state, _) -> Text.of("({}/3) Loadouts", state.page + 1), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<LoadoutsState> layout, LoadoutsState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        DatapointLoadouts.LoadoutsData data = LoadoutManager.data(player);

        layout.slot(9, treeSummary(data, TreeType.HOTF), (_, c) -> c.push(new GUITreeSlots(TreeType.HOTF)));
        layout.slot(18, treeSummary(data, TreeType.HOTM), (_, c) -> c.push(new GUITreeSlots(TreeType.HOTM)));
        for (int component = 0; component < 4; component++) {
            int piece = component;
            layout.slot(10 + component * 9, (s, c) -> currentEquipment((SkyBlockPlayer) c.player(), piece),
                    (_, c) -> c.push(new GUISkyBlockProfile()));
            layout.slot(11 + component * 9, (s, c) -> currentArmor((SkyBlockPlayer) c.player(), piece),
                    (_, c) -> c.push(new GUIWardrobe()));
        }
        layout.slot(21, (s, c) -> currentPet((SkyBlockPlayer) c.player()), (_, c) -> c.push(new GUIPets()));
        layout.slot(27, powerStone());
        layout.slot(36, statsTuning());

        int start = state.page * LOADOUT_SLOTS.length;
        int unlocked = LoadoutManager.unlockedLoadouts(player);
        for (int offset = 0; offset < LOADOUT_SLOTS.length; offset++) {
            int index = start + offset;
            if (index < unlocked) {
                loadout(layout, LOADOUT_SLOTS[offset], index);
            } else {
                locked(layout, LOADOUT_SLOTS[offset], index + 1);
            }
        }

        if (state.page > 0) {
            layout.slot(17, ItemStacks.item(Material.ARROW, 1, """
                            <a>Previous Page
                            <e>Page {}""", state.page),
                    (_, c) -> c.session(LoadoutsState.class).update(s -> new LoadoutsState(s.page - 1)));
        }
        if (state.page < 2) {
            layout.slot(44, ItemStacks.item(Material.ARROW, 1, """
                            <a>Next Page
                            <e>Page {}""", state.page + 2),
                    (_, c) -> c.session(LoadoutsState.class).update(s -> new LoadoutsState(s.page + 1)));
        }
        layout.slot(48, ItemStacks.item(Material.ARROW, 1, """
                <a>Go Back
                <7>To SkyBlock Menu"""), (_, c) -> c.pop());
    }

    private static void loadout(ViewLayout<LoadoutsState> layout, int slot, int index) {
        layout.slot(slot,
                (s, c) -> icon((SkyBlockPlayer) c.player(), index),
                (click, c) -> {
                    if (click.click() instanceof Click.Right || click.click() instanceof Click.RightShift) {
                        c.push(new GUILoadoutEdit(index));
                    } else if (click.click() instanceof Click.Left || click.click() instanceof Click.LeftShift) {
                        LoadoutManager.equip((SkyBlockPlayer) c.player(), index);
                        c.session(LoadoutsState.class).refresh();
                    }
                });
    }

    private static void locked(ViewLayout<LoadoutsState> layout, int slot, int number) {
        layout.slot(slot, ItemStacks.item(Material.RED_DYE, 1, """
                <c>Loadout {} Locked
                <7>Unlock more slots from:
                <8>▶ <a>Account Upgrades <8>- <6>9 Slots

                <c>Unlock more slots from <d>Elizabeth <c>at
                <c>the <b>Community Center""", number));
    }

    static ItemStack.Builder icon(SkyBlockPlayer player, int index) {
        DatapointLoadouts.LoadoutsData data = LoadoutManager.data(player);
        DatapointLoadouts.Loadout loadout = data.getLoadouts()[index];
        boolean equipped = data.getEquipped() == index;
        List<Text> lore = new ArrayList<>();
        for (int i = 0; i < 4; i++)
            lore.add(Text.of("<7>{}: {}", ARMOR_NAMES[i], name(LoadoutManager.loadoutArmor(player, loadout, i), equipped)));
        lore.add(Text.empty());
        for (int i = 0; i < 4; i++) lore.add(Text.of("<7>{}: {}", EQUIPMENT_NAMES[i], name(loadout.getEquipment()[i], false)));
        lore.add(Text.empty());
        lore.add(Text.of("<7>Pet: {}", petName(player, loadout.getPetType())));
        lore.add(Text.of("<7>HOTM: {}", treeName(loadout.getHotmSlot(), data.getHotmNames())));
        lore.add(Text.of("<7>HOTF: {}", treeName(loadout.getHotfSlot(), data.getHotfNames())));
        lore.add(Text.of("<7>Power Stone: <8>None"));
        lore.add(Text.of("<7>Tuning Template Slot: <8>None"));
        lore.add(Text.empty());
        if (!equipped && !loadout.isEmpty()) lore.add(Text.of("<e>Left-click to equip!"));
        lore.add(Text.of("<e>Right-click to edit"));
        if (loadout.isEmpty()) {
            lore.add(Text.empty());
            lore.add(Text.of("<c>You must customize this loadout"));
            lore.add(Text.of("<c>before you can equip it!"));
        }
        SkyBlockItem helmet = LoadoutManager.loadoutArmor(player, loadout, 0);
        if (helmet == null || helmet.isNA()) {
            return ItemStacks.item(equipped ? Material.LIME_DYE : Material.GRAY_DYE, 1,
                    Text.of("<a>{}", loadout.getName()), lore);
        }
        return ItemStacks.lore(ItemStacks.name(PlayerItemUpdater.playerUpdate(player, helmet.getItemStack()),
                "<a>{}", loadout.getName()), lore);
    }

    static Text name(SkyBlockItem item, boolean equipped) {
        if (item == null || item.isNA()) return equipped ? Text.of("<8>Empty") : Text.of("<8>None");
        return Text.literal(item.getDisplayName());
    }

    private static Text petName(SkyBlockPlayer player, String type) {
        if (type == null) return Text.of("<8>None");
        try {
            SkyBlockItem pet = player.getPetData().getPet(net.swofty.commons.skyblock.item.ItemType.valueOf(type));
            return pet == null ? Text.of("<a>{}", type.replace('_', ' ')) : Text.literal(pet.getDisplayName());
        } catch (IllegalArgumentException ignored) {
            return Text.of("<a>{}", type.replace('_', ' '));
        }
    }

    private static Text treeName(int slot, String[] names) {
        return slot < 0 ? Text.of("<8>None") : Text.of("<a>{}", names[slot]);
    }

    private static ItemStack.Builder currentArmor(SkyBlockPlayer player, int component) {
        SkyBlockItem item = LoadoutManager.currentArmor(player, component);
        if (item != null) return PlayerItemUpdater.playerUpdate(player, item.getItemStack());
        return ItemStacks.item(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1, """
                <7>Empty {} Slot

                <e>Click to select!""", ARMOR_NAMES[component]);
    }

    private static ItemStack.Builder currentEquipment(SkyBlockPlayer player, int component) {
        SkyBlockItem item = LoadoutManager.currentEquipment(player, component);
        if (!item.isNA()) return PlayerItemUpdater.playerUpdate(player, item.getItemStack());
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of("<8>> {}", component == 3 ? "Gloves" : EQUIPMENT_NAMES[component]));
        if (component == 3) lore.add(Text.of("<8>> Bracelet"));
        lore.add(Text.empty());
        lore.add(Text.of("<e>Click to select!"));
        return ItemStacks.item(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1,
                Text.of("<7>Empty Equipment Slot"), lore);
    }

    private static ItemStack.Builder currentPet(SkyBlockPlayer player) {
        SkyBlockItem pet = player.getPetData().getEnabledPet();
        return pet == null
                ? ItemStacks.item(Material.LIGHT_GRAY_STAINED_GLASS_PANE, 1, """
                        <a>Pet
                        <7>Current: <8>None""")
                : PlayerItemUpdater.playerUpdate(player, pet.getItemStack());
    }

    private static ItemStack.Builder treeSummary(DatapointLoadouts.LoadoutsData data, TreeType tree) {
        boolean hotm = tree == TreeType.HOTM;
        int active = hotm ? data.getActiveHotmSlot() : data.getActiveHotfSlot();
        String[] names = hotm ? data.getHotmNames() : data.getHotfNames();
        String title = hotm ? "Heart of the Mountain" : "Heart of the Forest";
        String texture = hotm ? "86f06eaa3004aeed09b3d5b45d976de584e691c0e9cade133635de93d23b9edb"
                : "5ef539b165125cfa46b06ffb9659e7cf89084bbd3ede1b314edc8f443343d61c";
        return ItemStacks.head(texture, Text.of("<a>{} Slot", title), List.of(
                Text.of("<7>Quickly swap between saved trees."),
                Text.empty(),
                Text.of("<7>Current: <a>{}", names[active]),
                Text.empty(),
                Text.of("<c>Swapping trees has a 10m cooldown!"),
                Text.empty(),
                Text.of("<e>Click to view!")));
    }

    private static ItemStack.Builder powerStone() {
        return ItemStacks.item(Material.LAPIS_LAZULI, 1, """
                <a>Power Stone
                <7>Choose your selected Power Stone.

                <7>Current: <a>Inspired

                <7>Stats:
                <c>+31.02 Health
                <a>+22.15 Defense
                <c>+88.62 Strength
                <9>+17.72 Crit Chance
                <9>+66.46 Crit Damage
                <b>+299.09 Intelligence

                <e>Click to view!""");
    }

    private static ItemStack.Builder statsTuning() {
        return ItemStacks.item(Material.COMPARATOR, 1, """
                <a>Stats Tuning
                <7>Optimize your build to your liking by using
                <e>Tuning Points<7>.

                <7>Every <6>10 MP <7>grants <e>1 Tuning Point<7>.

                <7>Magical Power: <6>425
                <7>Tuning Points: <e>42

                <7>Your tuning:
                <b>+84 Intelligence

                <e>Click to view!""");
    }

    public record LoadoutsState(int page) {
    }
}
