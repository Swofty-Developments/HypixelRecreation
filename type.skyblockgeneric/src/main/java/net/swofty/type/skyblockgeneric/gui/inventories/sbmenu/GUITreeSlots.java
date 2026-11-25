package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.HypixelSignGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.*;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointLoadouts;
import net.swofty.type.skyblockgeneric.loadout.LoadoutManager;
import net.swofty.type.skyblockgeneric.skilltree.SkillTreeType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUITreeSlots extends StatelessView {
    private static final List<Text> HOTM_TREE_ONE = List.of(
            Text.empty(),
            Text.of("<f>███████"),
            Text.of("<l> <r><0> <r><f>█<r><f><l> <r><0> <r><f>█<r><f><l> <r><0> <r><f>█<r><f><l> <r><0> "),
            Text.of("<f>███████"),
            Text.of("<l> <r><0> <r><f>█<r><f><l> <r><0> <r><f>█<r><f><l> <r><0> <r><f>█<r><f><l> <r><0> "),
            Text.of("<f>█████<r><a>█<r><f>█"),
            Text.of("<l> <r><0> <r><f>█<r><f><l> <r><0> <r><a>█<r><a><l> <r><0> <r><b>█<r><b><l> <r><0> "),
            Text.of("<f>███<r><a>█<r><f>█<r><a>█<r><f>█"),
            Text.of("<l> <r><0> <r><f>█<r><f><l> <r><0> <r><a>█<r><a><l> <r><0> <r><a>█<r><a><l> <r><0> "),
            Text.of("<l> <r><0> <r><f>█<r><b>█<r><a>███<r><a><l> <r><0> "),
            Text.of("<l> <r><0> <r><0><l> <r><0> <r><0><l> <r><0> <r><a>█<r><a><l> <r><0> <r><0><l> <r><0> <r><0><l> <r><0> "));

    private static final List<Text> HOTM_EMPTY = List.of(
            Text.empty(),
            Text.of("<f>███████"),
            Text.of("<l> <r><0> <r><f>█<r><f><l> <r><0> <r><f>█<r><f><l> <r><0> <r><f>█<r><f><l> <r><0> "),
            Text.of("<f>███████"),
            Text.of("<l> <r><0> <r><f>█<r><f><l> <r><0> <r><f>█<r><f><l> <r><0> <r><f>█<r><f><l> <r><0> "),
            Text.of("<f>███████"),
            Text.of("<l> <r><0> <r><f>█<r><f><l> <r><0> <r><a>█<r><a><l> <r><0> <r><f>█<r><f><l> <r><0> "),
            Text.of("<f>███████"),
            Text.of("<l> <r><0> <r><f>█<r><f><l> <r><0> <r><f>█<r><f><l> <r><0> <r><f>█<r><f><l> <r><0> "),
            Text.of("<l> <r><0> <r><f>█████<r><f><l> <r><0> "),
            Text.of("<l> <r><0> <r><0><l> <r><0> <r><0><l> <r><0> <r><f>█<r><f><l> <r><0> <r><0><l> <r><0> <r><0><l> <r><0> "));

    private static final List<Text> HOTF_TREE_ONE = List.of(
            Text.empty(),
            Text.of("<l> <r><0> <r><f>█<r><f><l> <r><0> <r><f>█<r><f><l> <r><0> <r><f>█<r><f><l> <r><0> "),
            Text.of("<6>█<r><e>███<r><f>███"),
            Text.of("<l> <r><0> <r><f>█<r><f><l> <r><0> <r><6>█<r><6><l> <r><0> <r><f>█<r><f><l> <r><0> "),
            Text.of("<f>███<r><e>█<r><f>█<r><6>█<r><f>█"),
            Text.of("<l> <r><0> <r><f>█<r><f><l> <r><0> <r><6>█<r><6><l> <r><0> <r><e>█<r><e><l> <r><0> "),
            Text.of("<l> <r><0> <r><f>██<r><6>█<r><e>██<r><e><l> <r><0> "),
            Text.of("<l> <r><0> <r><0><l> <r><0> <r><0><l> <r><0> <r><6>█<r><6><l> <r><0> <r><0><l> <r><0> <r><0><l> <r><0> "));

    private static final List<Text> HOTF_EMPTY = List.of(
            Text.empty(),
            Text.of("<l> <r><0> <r><f>█<r><f><l> <r><0> <r><f>█<r><f><l> <r><0> <r><f>█<r><f><l> <r><0> "),
            Text.of("<f>███████"),
            Text.of("<l> <r><0> <r><f>█<r><f><l> <r><0> <r><6>█<r><6><l> <r><0> <r><f>█<r><f><l> <r><0> "),
            Text.of("<f>███████"),
            Text.of("<l> <r><0> <r><f>█<r><f><l> <r><0> <r><f>█<r><f><l> <r><0> <r><f>█<r><f><l> <r><0> "),
            Text.of("<l> <r><0> <r><f>█████<r><f><l> <r><0> "),
            Text.of("<l> <r><0> <r><0><l> <r><0> <r><0><l> <r><0> <r><f>█<r><f><l> <r><0> <r><0><l> <r><0> <r><0><l> <r><0> "));

    private final SkillTreeType tree;
    private final Integer loadout;

    public GUITreeSlots(SkillTreeType tree) {
        this(tree, null);
    }

    public GUITreeSlots(SkillTreeType tree, Integer loadout) {
        this.tree = tree;
        this.loadout = loadout;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.of("{} Slot", title()), InventoryType.CHEST_4_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 31);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        DatapointLoadouts.LoadoutsData data = LoadoutManager.data(player);
        int selected = loadout == null
                ? (tree == SkillTreeType.HOTM ? data.getActiveHotmSlot() : data.getActiveHotfSlot())
                : selected(data.getLoadouts()[loadout]);
        String[] names = tree == SkillTreeType.HOTM ? data.getHotmNames() : data.getHotfNames();

        for (int slot = 0; slot < DatapointLoadouts.TREE_SLOT_COUNT; slot++) {
            int treeSlot = slot;
            boolean unlocked = slot < 2;
            boolean active = slot == selected;
            List<Text> lore = new ArrayList<>(diagram(slot));
            lore.add(Text.empty());
            if (!unlocked) {
                lore.add(Text.of("<c>Unlock more {} <c>Slots from", tree.name()));
                lore.add(Text.of("<d>Elizabeth <c>at the <b>Community Center<c>!"));
                lore.add(Text.empty());
                lore.add(Text.of("<c><l>LOCKED"));
            } else if (loadout != null) {
                lore.add(Text.of("<e>Click to select!"));
            } else if (active) {
                lore.add(Text.of("<a><l>SELECTED"));
                lore.add(Text.empty());
                lore.add(Text.of("<e>Right-click to rename!"));
            } else {
                lore.add(Text.of("<e>Left-click to select!"));
                lore.add(Text.of("<e>Right-click to rename!"));
            }
            Material material = !unlocked ? Material.RED_DYE : active && loadout == null ? Material.LIME_DYE : Material.GRAY_DYE;
            layout.slot(11 + slot, ItemStacks.item(material, 1,
                            Text.of(active ? "<a>{}" : "<c>{}", names[slot]), lore),
                    (click, c) -> handleClick((SkyBlockPlayer) c.player(), treeSlot, unlocked, click.click(), c));
        }

        layout.slot(30, ItemStacks.item(Material.ARROW, 1, """
                <a>Go Back
                <7>To {}""", loadout == null ? "Loadouts" : LoadoutManager.data(player).getLoadouts()[loadout].getName()), (_, c) -> c.pop());
        if (loadout != null) {
            layout.slot(32, ItemStacks.item(Material.LAVA_BUCKET, 1, """
                            <c>Clear Selection
                            <7>Clears your current selection for
                            <7>this component of your loadout.

                            <e>Click to clear!"""),
                    (_, c) -> clear((SkyBlockPlayer) c.player()));
        }
    }

    private void handleClick(SkyBlockPlayer player, int slot, boolean unlocked, Click click, ViewContext ctx) {
        if (!unlocked) return;
        if (loadout != null) {
            DatapointLoadouts.Loadout selected = LoadoutManager.data(player).getLoadouts()[loadout];
            if (tree == SkillTreeType.HOTM) selected.setHotmSlot(slot);
            else selected.setHotfSlot(slot);
            LoadoutManager.save(player);
            player.openView(new GUILoadoutEdit(loadout));
            return;
        }
        if (click instanceof Click.Right || click instanceof Click.RightShift) {
            rename(player, slot);
            return;
        }
        if (click instanceof Click.Left || click instanceof Click.LeftShift) {
            if (LoadoutManager.switchTree(player, tree, slot)) ctx.session(DefaultState.class).refresh();
        }
    }

    private void clear(SkyBlockPlayer player) {
        DatapointLoadouts.Loadout selected = LoadoutManager.data(player).getLoadouts()[loadout];
        if (tree == SkillTreeType.HOTM) selected.setHotmSlot(-1);
        else selected.setHotfSlot(-1);
        LoadoutManager.save(player);
        player.openView(new GUILoadoutEdit(loadout));
    }

    private void rename(SkyBlockPlayer player, int slot) {
        DatapointLoadouts.LoadoutsData data = LoadoutManager.data(player);
        String[] names = tree == SkillTreeType.HOTM ? data.getHotmNames() : data.getHotfNames();
        new HypixelSignGUI(player).open(new String[]{names[slot], ""}).thenAccept(name -> {
            if (name != null && !name.isBlank()) {
                names[slot] = name.trim();
                LoadoutManager.save(player);
            }
            player.openView(new GUITreeSlots(tree));
        });
    }

    private int selected(DatapointLoadouts.Loadout loadout) {
        return tree == SkillTreeType.HOTM ? loadout.getHotmSlot() : loadout.getHotfSlot();
    }

    private List<Text> diagram(int slot) {
        if (tree == SkillTreeType.HOTM) return slot == 0 ? HOTM_TREE_ONE : HOTM_EMPTY;
        return slot == 0 ? HOTF_TREE_ONE : HOTF_EMPTY;
    }

    private String title() {
        return tree == SkillTreeType.HOTM ? "Heart of the Mountain" : "Heart of the Forest";
    }
}
