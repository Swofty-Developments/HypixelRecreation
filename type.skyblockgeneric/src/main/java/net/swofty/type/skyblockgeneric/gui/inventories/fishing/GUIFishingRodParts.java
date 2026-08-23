package net.swofty.type.skyblockgeneric.gui.inventories.fishing;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.ViewSession;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.fishing.item.FishingItemSupport;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class GUIFishingRodParts extends StatelessView {

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>("Fishing Rod Parts", InventoryType.CHEST_6_ROW);
    }

    @Override
    public void onClose(DefaultState state, ViewContext ctx, ViewSession.CloseReason reason) {
        ItemStack rodItem = ctx.inventory().getItemStack(21);
        if (!rodItem.isAir()) {
            SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
            player.addAndUpdateItem(new SkyBlockItem(rodItem));
            ctx.inventory().setItemStack(21, ItemStack.AIR);
        }
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.close(layout, 49);

        layout.editable(21, (_, _) -> ItemStacks.named(
            Material.FISHING_ROD,
            "<7>Place your <a>Fishing Rod <7>here!"
        ), (_, oldItem, newItem, _) -> {
            if (newItem.isAir()) {
                return;
            }
            SkyBlockItem rod = new SkyBlockItem(newItem);
            ItemType type = rod.getAttributeHandler().getPotentialType();
            var metadata = type == null ? null : FishingItemSupport.getRodMetadata(type.name());
            if (metadata == null || !metadata.isRodPartsEnabled() || metadata.getLegacyConversionTarget() != null) {
                SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
                player.sendMessage("<c>That fishing rod does not support rod parts.");
                ctx.inventory().setItemStack(21, oldItem);
                if (!newItem.isAir()) {
                    player.addAndUpdateItem(new SkyBlockItem(newItem));
                }
            }
        });

        layout.slot(22, ItemStacks.item(Material.LIGHT_BLUE_STAINED_GLASS_PANE, """
                <9>ථ Hook
                <7>Place a <a>Fishing Rod <7>in the slot to the
                <7>left to view and modify its <9>Hook<7>!

                <e>Click to browse Hooks!"""), (_, viewCtx) -> viewCtx.push(new GUIHookGuide()));

        layout.slot(23, ItemStacks.item(Material.LIGHT_BLUE_STAINED_GLASS_PANE, """
                <9>ꨃ Line
                <7>Place a <a>Fishing Rod <7>in the slot to the
                <7>left to view and modify its <9>Line<7>!

                <e>Click to browse Lines!"""), (_, viewCtx) -> viewCtx.push(new GUILineGuide()));

        layout.slot(24, ItemStacks.item(Material.LIGHT_BLUE_STAINED_GLASS_PANE, """
                <9>࿉ Sinker
                <7>Place a <a>Fishing Rod <7>in the slot to the
                <7>left to view and modify its <9>Sinker<7>!

                <e>Click to browse Sinkers!"""), (_, viewCtx) -> viewCtx.push(new GUISinkerGuide()));

        layout.slot(50, ItemStacks.item(Material.BOOK, """
                <9>Rod Part Guide
                <7>View all of the <9>Rod Parts <7>that can be
                <7>applied to <a>Fishing Rods<7>! Can also be
                <7>accessed with <a>/rodparts<7>!

                <e>Click to view!"""), (_, viewCtx) -> viewCtx.push(new GUIRodPartGuide()));
    }
}
