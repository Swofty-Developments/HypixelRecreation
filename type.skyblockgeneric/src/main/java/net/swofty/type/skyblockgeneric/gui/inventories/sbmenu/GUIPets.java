package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.inventory.click.Click;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.item.attribute.attributes.ItemAttributePetData;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.PaginatedView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ClickContext;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.PetComponent;
import net.swofty.type.skyblockgeneric.item.updater.NonPlayerItemUpdater;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;

public class GUIPets extends PaginatedView<SkyBlockItem, GUIPets.PetsState> {
    private static final int[] PAGINATED_SLOTS = {
            10, 11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    @Override
    public ViewConfiguration<PetsState> configuration() {
        return ViewConfiguration.withText(
            (state, ctx) -> Text.key("gui_sbmenu.pets.title", state.page() + 1,
                Math.max(1, (int) Math.ceil((double) getFilteredItems(state).size() / PAGINATED_SLOTS.length))),
                InventoryType.CHEST_6_ROW
        );
    }

    @Override
    protected int[] getPaginatedSlots() {
        return PAGINATED_SLOTS;
    }

    @Override
    protected List<SkyBlockItem> getFilteredItems(PetsState state) {
        List<SkyBlockItem> pets = new ArrayList<>(state.items());
        pets = pets.stream().filter(item -> !shouldFilterFromSearch(state, item)).toList();

        pets = new ArrayList<>(pets);
        switch (state.sortType()) {
            case LEVEL:
                pets.sort((pet1, pet2) -> {
                    ItemAttributePetData.PetData data1 = pet1.getAttributeHandler().getPetData();
                    Rarity rarity1 = pet1.getAttributeHandler().getRarity();
                    ItemAttributePetData.PetData data2 = pet2.getAttributeHandler().getPetData();
                    Rarity rarity2 = pet2.getAttributeHandler().getRarity();
                    int level1 = data1.getAsLevel(rarity1);
                    int level2 = data2.getAsLevel(rarity2);
                    return Integer.compare(level2, level1);
                });
                break;
            case RARITY:
                pets.sort((pet1, pet2) -> {
                    int rarity1 = pet1.getAttributeHandler().getRarity().ordinal();
                    int rarity2 = pet2.getAttributeHandler().getRarity().ordinal();
                    return Integer.compare(rarity2, rarity1);
                });
                break;
            case ALPHABETICAL:
                pets.sort((pet1, pet2) -> {
                    String name1 = pet1.getComponent(PetComponent.class).getPetName();
                    String name2 = pet2.getComponent(PetComponent.class).getPetName();
                    return name1.compareTo(name2);
                });
                break;
            case SKILL:
                pets.sort((pet1, pet2) -> {
                    SkillCategories skill1 = pet1.getComponent(PetComponent.class).getSkillCategory();
                    SkillCategories skill2 = pet2.getComponent(PetComponent.class).getSkillCategory();
                    return Integer.compare(skill2.ordinal(), skill1.ordinal());
                });
                break;
        }

        return pets;
    }

    @Override
    protected ItemStack.Builder renderItem(SkyBlockItem item, int index, HypixelPlayer player) {
        SkyBlockPlayer skyBlockPlayer = (SkyBlockPlayer) player;
        boolean isPetEnabled = skyBlockPlayer.getPetData().getEnabledPet() == item;

        ItemStack.Builder itemStack = new NonPlayerItemUpdater(item).getUpdatedItem();
        List<Text> lore = ItemStacks.loreText(itemStack);
        lore.add(Text.literal(" "));
        if (isPetEnabled) {
            ItemStacks.enchanted(itemStack);
            lore.add(Text.key("gui_sbmenu.pets.currently_active"));
            lore.add(Text.key("gui_sbmenu.pets.click_to_deselect"));
        } else {
            lore.add(Text.key("gui_sbmenu.pets.click_to_summon"));
        }
        return ItemStacks.lore(itemStack, lore);
    }

    @Override
    protected void onItemClick(ClickContext<PetsState> click, ViewContext ctx, SkyBlockItem item, int index) {
        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        PetsState state = click.state();
        boolean selected = player.getPetData().getEnabledPet() == item;

        if (selected) {
            player.getPetData().deselectCurrent();
            player.getPetData().updatePetEntityImpl(player);
            ctx.session(PetsState.class).update(s -> (PetsState) s.withItems(getPetsFromPlayer(player)));
            player.sendMessage(Text.key("gui_sbmenu.pets.msg.deselected", item.getDisplayName()));
            return;
        }

        if (state.convertToItem()) {
            player.addAndUpdateItem(item);
            player.getPetData().removePet(item.getAttributeHandler().getPotentialType());
            ctx.session(PetsState.class).update(s -> (PetsState) s.withItems(getPetsFromPlayer(player)));
            player.sendMessage(Text.key("gui_sbmenu.pets.msg.picked_up"));
            return;
        }

        player.getPetData().setEnabled(item.getAttributeHandler().getPotentialType(), true);
        player.getPetData().updatePetEntityImpl(player);
        player.sendMessage(Text.key("gui_sbmenu.pets.msg.selected", item.getDisplayName()));
        ctx.session(PetsState.class).update(s -> (PetsState) s.withItems(getPetsFromPlayer(player)));
    }

    @Override
    protected boolean shouldFilterFromSearch(PetsState state, SkyBlockItem item) {
        return !item.getDisplayName().toLowerCase().contains(state.query.toLowerCase());
    }

    @Override
    protected void layoutCustom(ViewLayout<PetsState> layout, PetsState state, ViewContext ctx) {
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        layout.slot(4, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            Text selectedPet = player.getPetData().getEnabledPet() == null
                ? Text.of("<c>None")
                : Text.literal(player.getPetData().getEnabledPet().getDisplayName());
            return ItemStacks.item(Material.BONE, 1,
                Text.key("gui_sbmenu.pets.info"),
                Text.keyLines("gui_sbmenu.pets.info.lore", selectedPet));
        });

        layout.slot(47, (s, c) -> {
            Text status = s.convertToItem() ? Text.of("<a>Enabled") : Text.of("<c>Disabled");
            ItemStack.Builder itemStack = ItemStacks.item(Material.DIAMOND, 1,
                Text.key("gui_sbmenu.pets.convert_to_item"),
                Text.keyLines("gui_sbmenu.pets.convert_to_item.lore", status));
            if (s.convertToItem())
                ItemStacks.enchanted(itemStack);
            return itemStack;
        }, (click, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            Text status = !click.state().convertToItem() ? Text.of("<a>ENABLED") : Text.of("<c>DISABLED");
            player.sendMessage(Text.key("gui_sbmenu.pets.msg.conversion_toggle", status));
            c.session(PetsState.class).update(s -> s.withConvertToItem(!s.convertToItem()));
        });

        layout.slot(51, (s, c) -> {
            List<Text> lore = new ArrayList<>();
            lore.add(Text.literal(" "));

            for (SortType randomSortType : SortType.values()) {
                lore.add(Text.of(randomSortType == s.sortType() ? "<e>> {}" : "<7>> {}",
                        StringUtility.toNormalCase(randomSortType.name())));
            }

            lore.add(Text.literal(" "));
            lore.add(Text.key("gui_sbmenu.pets.sort.right_click"));
            lore.add(Text.key("gui_sbmenu.pets.sort.click"));

            return ItemStacks.item(Material.HOPPER, 1, Text.key("gui_sbmenu.pets.sort"), lore);
        }, (click, c) -> {
            boolean isRightClick = click.click() instanceof Click.Right;

            int ordinal = click.state().sortType().ordinal();
            if (isRightClick) {
                ordinal--;
                if (ordinal < 0) ordinal = SortType.values().length - 1;
            } else {
                ordinal++;
                if (ordinal >= SortType.values().length) ordinal = 0;
            }

            SortType newSort = SortType.values()[ordinal];
            c.session(PetsState.class).update(s -> s.withSortType(newSort));
        });
    }

    @Override
    protected int getPreviousPageSlot() {
        return 45;
    }

    @Override
    protected int getNextPageSlot() {
        return 53;
    }

    private static List<SkyBlockItem> getPetsFromPlayer(SkyBlockPlayer player) {
        return new ArrayList<>(player.getPetData().getPetsMap().keySet().stream().toList());
    }

    public static PetsState createInitialState(SkyBlockPlayer player) {
        return new PetsState(getPetsFromPlayer(player), 0, "", SortType.LEVEL, false);
    }

    public record PetsState(
            List<SkyBlockItem> items,
            int page,
            String query,
            SortType sortType,
            boolean convertToItem
    ) implements PaginatedState<SkyBlockItem> {
        @Override
        public PaginatedState<SkyBlockItem> withPage(int page) {
            return new PetsState(items, page, query, sortType, convertToItem);
        }

        @Override
        public PaginatedState<SkyBlockItem> withItems(List<SkyBlockItem> items) {
            return new PetsState(items, page, query, sortType, convertToItem);
        }

        public PetsState withSortType(SortType sortType) {
            return new PetsState(items, page, query, sortType, convertToItem);
        }

        public PetsState withConvertToItem(boolean convertToItem) {
            return new PetsState(items, page, query, sortType, convertToItem);
        }
    }

    public enum SortType {
        LEVEL,
        RARITY,
        ALPHABETICAL,
        SKILL
    }
}
