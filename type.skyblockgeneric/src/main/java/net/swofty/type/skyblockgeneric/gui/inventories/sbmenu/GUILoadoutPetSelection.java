package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.*;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.loadout.LoadoutManager;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUILoadoutPetSelection extends StatelessView {
    private static final int[] SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};
    private final int loadout;

    public GUILoadoutPetSelection(int loadout) {
        this.loadout = loadout;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Select Pet", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        List<SkyBlockItem> pets = new ArrayList<>(player.getPetData().getPetsMap().keySet());
        for (int i = 0; i < Math.min(SLOTS.length, pets.size()); i++) {
            SkyBlockItem pet = pets.get(i);
            layout.slot(SLOTS[i], (s, c) -> PlayerItemUpdater.playerUpdate((SkyBlockPlayer) c.player(), pet.getItemStack()),
                    (_, c) -> select((SkyBlockPlayer) c.player(), pet));
        }
        layout.slot(48, ItemStacks.item(Material.ARROW, 1, """
                <a>Go Back
                <7>To Loadout {}""", loadout + 1), (_, c) -> c.pop());
        layout.slot(50, ItemStacks.item(Material.LAVA_BUCKET, 1, """
                <c>Clear Selection
                <7>Clear this loadout's pet.

                <e>Click to clear!"""), (_, c) -> clear((SkyBlockPlayer) c.player()));
    }

    private void select(SkyBlockPlayer player, SkyBlockItem pet) {
        LoadoutManager.data(player).getLoadouts()[loadout].setPetType(pet.getAttributeHandler().getPotentialType().name());
        LoadoutManager.save(player);
        player.openView(new GUILoadoutEdit(loadout));
    }

    private void clear(SkyBlockPlayer player) {
        LoadoutManager.data(player).getLoadouts()[loadout].setPetType(null);
        LoadoutManager.save(player);
        player.openView(new GUILoadoutEdit(loadout));
    }
}
