package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.Material;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.Layouts;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetSkinComponent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class GUIPetSkinVariants extends StatelessView {
    private static final int[] VARIANT_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34
    };

    private final SkyBlockItem pet;
    private final PetSkinComponent skin;

    public GUIPetSkinVariants(SkyBlockItem pet, PetSkinComponent skin) {
        this.pet = pet;
        this.skin = skin;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Skin Preview Swapper", InventoryType.CHEST_5_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        for (int slot : Layouts.border(0, 44)) {
            layout.slot(slot, ItemStacks.filler(Material.BLACK_STAINED_GLASS_PANE));
        }
        Components.close(layout, 40);

        List<PetSkinComponent.Variant> variants = skin.getVariants();
        for (int i = 0; i < variants.size() && i < VARIANT_SLOTS.length; i++) {
            PetSkinComponent.Variant variant = variants.get(i);
            layout.slot(
                    VARIANT_SLOTS[i],
                    ItemStacks.head(
                            variant.defaultTexture(),
                            Text.of(variant.color() + variant.name()),
                            Text.keyLines("gui_sbmenu.pet_skin_variants.variant.lore")
                    ),
                    (_, context) -> select((SkyBlockPlayer) context.player(), variant)
            );
        }
    }

    private void select(SkyBlockPlayer player, PetSkinComponent.Variant variant) {
        pet.getAttributeHandler().getPetData().setSkinVariant(variant.name());
        player.setItemInHand(pet);
        player.closeInventory();
        player.sendMessage(skin.formatSwapMessage(variant));
    }
}
