package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.bags;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.StatefulView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.SackComponent;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class GUISack implements StatefulView<GUISack.SackState> {
    private final ItemType itemTypeLinker;
    private final boolean closeGUIButton;
    private final SackSize sackSize;

    private static final List<SackSize> SACK_SIZES = List.of(
            new SackSize(14, InventoryType.CHEST_4_ROW, List.of(
                    10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25)),
            new SackSize(21, InventoryType.CHEST_5_ROW, List.of(
                    10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34)),
            new SackSize(28, InventoryType.CHEST_6_ROW, List.of(
                    10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43)),
            new SackSize(45, InventoryType.CHEST_6_ROW, List.of(
                    0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44))
    );

    private static SackSize getSackSize(ItemType sack) {
        int sackItemSize = 0;
        SkyBlockItem item = new SkyBlockItem(sack);

        if (item.hasComponent(SackComponent.class)) {
            SackComponent sackInstance = item.getComponent(SackComponent.class);
            sackItemSize = sackInstance.getValidItems().size();
        }

        final int finalSackItemSize = sackItemSize;

        return SACK_SIZES.stream()
                .filter(sackSize -> sackSize.getAmountItems() >= finalSackItemSize)
                .min(Comparator.comparingInt(SackSize::getAmountItems))
                .orElse(SACK_SIZES.getLast());
    }

    public GUISack(ItemType sack, boolean closeGUIButton) {
        this.itemTypeLinker = sack;
        this.closeGUIButton = closeGUIButton;
        this.sackSize = getSackSize(sack);
    }

    @Override
    public ViewConfiguration<SackState> configuration() {
        return new ViewConfiguration<>(Text.literal(StringUtility.toNormalCase(itemTypeLinker.name())), sackSize.getInventoryType());
    }

    @Override
    public SackState initialState() {
        return new SackState();
    }

    @Override
    public void layout(ViewLayout<SackState> layout, SackState state, ViewContext ctx) {
        Components.fill(layout);

        int backSlot = switch (sackSize.getInventoryType()) {
            case InventoryType.CHEST_4_ROW -> 31;
            case InventoryType.CHEST_5_ROW -> 40;
            case InventoryType.CHEST_6_ROW -> 49;
            default -> 31;
        };

        if (!closeGUIButton) {
            layout.slot(backSlot, (s, c) -> ItemStacks.item(Material.ARROW, 1,
                            Text.key("gui_sbmenu.bags.sack.go_back"),
                            List.of(Text.key("gui_sbmenu.bags.sack.go_back.lore"))),
                    (click, c) -> c.player().openView(new GUISackOfSacks()));
        } else {
            Components.close(layout, backSlot);
        }

        List<SkyBlockItem> sackItems = new ArrayList<>();
        SkyBlockItem item = new SkyBlockItem(itemTypeLinker);

        if (item.hasComponent(SackComponent.class)) {
            SackComponent sackInstance = item.getComponent(SackComponent.class);
            for (ItemType linker : sackInstance.getValidItems()) {
                sackItems.add(new SkyBlockItem(linker));
            }
        }

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        int finalMaxStorage = player.getMaxSackStorage(itemTypeLinker);

        int index = 0;
        for (Integer slot : sackSize.getSlots()) {
            if (index < sackItems.size()) {
                SkyBlockItem skyBlockItem = sackItems.get(index);
                ItemType linker = skyBlockItem.getAttributeHandler().getPotentialType();
                int finalIndex = index;

                layout.slot(slot, (s, c) -> {
                    SkyBlockPlayer p = (SkyBlockPlayer) c.player();
                    ItemStack.Builder builder = PlayerItemUpdater.playerUpdate(p, skyBlockItem.getItemStack());
                    List<Text> lore = new ArrayList<>();
                    Integer amount = p.getSackItems().getAmount(linker);
                    lore.add(Text.empty());
                    lore.add(Text.key("gui_sbmenu.bags.sack.stored",
                            Text.empty(),
                            Text.of(amount == finalMaxStorage ? "<a>{}" : "<e>{}", amount),
                            StringUtility.shortenNumber(StringUtility.roundTo(finalMaxStorage, 0))));
                    lore.add(Text.empty());
                    if (amount != 0) {
                        lore.add(Text.key("gui_sbmenu.bags.sack.right_click_stack"));
                        lore.add(Text.key("gui_sbmenu.bags.sack.click_to_pickup"));
                    } else {
                        lore.add(Text.key("gui_sbmenu.bags.sack.empty"));
                    }
                    return ItemStacks.lore(builder, lore);
                }, (click, c) -> handleSackItemClick(click, c, linker));
            }
            index++;
        }
    }

    private void handleSackItemClick(ClickContext<SackState> click, ViewContext ctx, ItemType linker) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        Integer amount = player.getSackItems().getAmount(linker);

        if (click.click() instanceof Click.Right) {
            if (amount == 0) return;
            if (amount >= 64) {
                player.getSackItems().decrease(linker, 64);
                SkyBlockItem itemAdded = new SkyBlockItem(linker);
                itemAdded.setAmount(64);
                player.addAndUpdateItem(itemAdded);
            } else {
                player.getSackItems().decrease(linker, amount);
                SkyBlockItem itemAdded = new SkyBlockItem(linker);
                itemAdded.setAmount(amount);
                player.addAndUpdateItem(itemAdded);
            }
            ctx.session(SackState.class).refresh();
        } else if (click.click() instanceof Click.Left) {
            int airSlots = 0;
            for (ItemStack itemStack : player.getInventory().getItemStacks()) {
                if (itemStack.isAir()) airSlots++;
            }
            int maxSpace = 64 * airSlots;
            if (amount >= maxSpace) {
                player.getSackItems().decrease(linker, maxSpace);
                SkyBlockItem itemAdded = new SkyBlockItem(linker);
                itemAdded.setAmount(64);
                for (int i = 0; i < airSlots; i++) {
                    player.addAndUpdateItem(itemAdded);
                }
            } else {
                player.getSackItems().decrease(linker, amount);
                SkyBlockItem itemAdded = new SkyBlockItem(linker);
                itemAdded.setAmount(amount);
                player.addAndUpdateItem(itemAdded);
            }
            ctx.session(SackState.class).refresh();
        }
    }

    public record SackState() {}
}
