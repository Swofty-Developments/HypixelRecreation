package net.swofty.type.skyblockgeneric.gui.inventories;

import lombok.SneakyThrows;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentSource;
import net.swofty.type.skyblockgeneric.enchantment.EnchantmentType;
import net.swofty.type.skyblockgeneric.enchantment.SkyBlockEnchantment;
import net.swofty.type.skyblockgeneric.enchantment.abstr.ConflictingEnch;
import net.swofty.type.skyblockgeneric.item.ItemAttributeHandler;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.EnchantableComponent;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.groups.EnchantItemGroups;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GUIEnchantmentTable extends HypixelInventoryGUI {
    private static final int[] PAGINATED_SLOTS_LIST_ENCHANTS = new int[]{
            12, 13, 14, 15, 16,
            21, 22, 23, 24, 25,
            30, 31, 32, 33, 34,
    };
    private static final int[] PAGINATED_SLOTS_LIST_LEVELS = new int[]{
            21, 22, 23, 24, 25,
            30, 31, 32, 33, 34,
    };

    private final int bookshelfPower;

    public GUIEnchantmentTable(Instance instance, Pos enchantmentTable) {
        super(Text.key("gui_enchantment.title"), InventoryType.CHEST_6_ROW);

        this.bookshelfPower = getBookshelfPower(instance, enchantmentTable);
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        fill(Material.BLACK_STAINED_GLASS_PANE, "");
        set(GUIClickableItem.getCloseItem(49));

        set(new GUIItem(48) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.BOOKSHELF, 1, Text.key("gui_enchantment.bookshelf_power"),
                    Text.keyLines("gui_enchantment.bookshelf_power.lore", bookshelfPower));
            }
        });

        set(new GUIItem(50) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.BOOK, 1, Text.key("gui_enchantment.enchantments_guide"),
                        Text.keyLines("gui_enchantment.enchantments_guide.lore"));
            }
        });

        set(new GUIItem(28) {
            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.ENCHANTING_TABLE, 1, Text.key("gui_enchantment.enchant_item_label"),
                        Text.keyLines("gui_enchantment.enchant_item_label.lore"));
            }
        });

        updateFromItem(null, null);
    }

    @SneakyThrows
    public void updateFromItem(SkyBlockItem item, EnchantmentType selected) {
        setTitle(selected == null
            ? Text.key("gui_enchantment.title")
            : Text.key("gui_enchantment.title_selected", StringUtility.toNormalCase(selected.name())));

        Arrays.stream(PAGINATED_SLOTS_LIST_ENCHANTS).forEach(slot -> set(slot, ItemStacks.named(
                Material.BLACK_STAINED_GLASS_PANE, "<7> "
        )));
        set(45, ItemStacks.named(Material.BLACK_STAINED_GLASS_PANE, "<7> "));

        if (item == null) {
            set(new GUIItem(23) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return ItemStacks.item(Material.GRAY_DYE, 1, Text.key("gui_enchantment.place_item"),
                            Text.keyLines("gui_enchantment.place_item.lore"));
                }
            });

            set(new GUIClickableItem(19) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    ItemStack stack = player.getInventory().getCursorItem();

                    if (stack.get(DataComponents.CUSTOM_NAME) == null) return;

                    e.setCancelled(true);
                    SkyBlockItem item = new SkyBlockItem(stack);
                    player.getInventory().setCursorItem(ItemStack.AIR);
                    updateFromItem(item, null);
                }

                @Override
                public boolean canPickup() {
                    return true;
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return ItemStack.builder(Material.AIR);
                }
            });

            updateItemStacks(getInventory(), getPlayer());
            return;
        }

        set(new GUIClickableItem(19) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                ItemStack stack = player.getInventory().getCursorItem();

                if (stack == ItemStack.AIR) {
                    e.setCancelled(true);
                    player.getInventory().setCursorItem(PlayerItemUpdater.playerUpdate(player, item.getItemStack()).build());
                    updateFromItem(null, null);
                }
            }

            @Override
            public boolean canPickup() {
                return true;
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p;
                return PlayerItemUpdater.playerUpdate(player, item.getItemStack());
            }
        });

        ItemType type = item.getAttributeHandler().getPotentialType();
        if (item.getItemStack().amount() > 1 || type == null || !(item.hasComponent(EnchantableComponent.class))) {
            set(new GUIItem(23) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return ItemStacks.item(Material.RED_DYE, 1, Text.key("gui_enchantment.invalid_item"),
                            List.of(Text.key("gui_enchantment.invalid_item.lore")));
                }
            });

            updateItemStacks(getInventory(), getPlayer());
            return;
        }

        List<EnchantItemGroups> enchantItemGroups = item.getComponent(EnchantableComponent.class).getEnchantItemGroups();
        List<EnchantmentType> enchantments = Arrays.stream(EnchantmentType.values())
                .filter(enchantmentType -> enchantmentType.getEnch().getGroups().stream().anyMatch(enchantItemGroups::contains))
                .filter(enchantmentType -> enchantmentType.getEnchFromTable() != null)
                .toList();

        if (enchantments.isEmpty()) {
            set(new GUIItem(23) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return ItemStacks.item(Material.RED_DYE, 1, Text.key("gui_enchantment.cannot_enchant"),
                            List.of(Text.key("gui_enchantment.cannot_enchant.lore")));
                }
            });
            updateItemStacks(getInventory(), getPlayer());
            return;
        }

        if (selected == null) {
            enchantments = enchantments.stream().limit(15).toList();
            int i = 0;
            for (EnchantmentType enchantmentType : enchantments) {
                assert enchantmentType.getEnchFromTable() != null;
                int finalI = i;
                set(new GUIClickableItem(PAGINATED_SLOTS_LIST_ENCHANTS[finalI]) {
                    @Override
                    public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                        SkyBlockPlayer player = (SkyBlockPlayer) p;
                        if (bookshelfPower < enchantmentType.getEnchFromTable().getRequiredBookshelfPower()) {
                            player.sendMessage(Text.key("gui_enchantment.requires_bookshelf_message", enchantmentType.getEnchFromTable().getRequiredBookshelfPower()));
                            return;
                        }

                        updateFromItem(item, enchantmentType);
                    }

                    @Override
                    public ItemStack.Builder getItem(HypixelPlayer p) {
                        SkyBlockPlayer player = (SkyBlockPlayer) p;
                        ItemAttributeHandler itemAttributeHandler = item.getAttributeHandler();

                        List<Text> lore = new ArrayList<>(Text.of("<7><wrap:30>{}</wrap>",
                                Text.parseLenient(enchantmentType.getDescription(1, player))).lines());
                        lore.add(Text.of("<a> "));

                        if (itemAttributeHandler.hasEnchantment(enchantmentType)) {
                            lore.add(Text.of("<a>  {} {:roman} <l>✓",
                                    StringUtility.toNormalCase(enchantmentType.name()),
                                    itemAttributeHandler.getEnchantment(enchantmentType).level()));
                        } else {
                            lore.add(Text.of("<c>  {} <l>✖", StringUtility.toNormalCase(enchantmentType.name())));
                        }

                        lore.add(Text.of("<a> "));

                        if (bookshelfPower < enchantmentType.getEnchFromTable().getRequiredBookshelfPower()) {
                            lore.add(Text.key("gui_enchantment.requires_bookshelf", enchantmentType.getEnchFromTable().getRequiredBookshelfPower()));
                        } else {
                            lore.add(Text.key("gui_enchantment.click_to_view"));
                        }

                        return ItemStacks.item(Material.ENCHANTED_BOOK, 1,
                                Text.of("<a>{}", StringUtility.toNormalCase(enchantmentType.name())),
                                lore
                        );
                    }
                });
                i++;
            }
            updateItemStacks(getInventory(), getPlayer());
            return;
        }

        int minLevel = selected.getEnch().getSources((SkyBlockPlayer) getPlayer()).stream().filter(source ->
                        source.getSource().equals(EnchantmentSource.SourceType.ENCHANTMENT_TABLE.toString()))
                .mapToInt(value -> value.minLevel).findAny().orElse(0);
        int maxLevel = selected.getEnch().getSources((SkyBlockPlayer) getPlayer()).stream().filter(source ->
                        source.getSource().equals(EnchantmentSource.SourceType.ENCHANTMENT_TABLE.toString()))
                .mapToInt(value -> value.maxLevel).findAny().orElse(0);

        int hasLevel = 0;
        if (item.getAttributeHandler().hasEnchantment(selected)) {
            hasLevel = item.getAttributeHandler().getEnchantment(selected).level();
        }

        for (int level = minLevel; level <= maxLevel; level++) {
            int finalLevel = level;
            int finalHasLevel = hasLevel;
            set(new GUIClickableItem(PAGINATED_SLOTS_LIST_LEVELS[finalLevel - 1]) {

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    int levelCost = selected.getEnchFromTable().getLevelsFromTableToApply(player).get(finalLevel);
                    List<Text> lore = new ArrayList<>(Text.of("<7><wrap:30>{}</wrap>",
                            Text.parseLenient(selected.getDescription(finalLevel, player))).lines());

                    lore.add(Text.of("<a> "));

                    if (selected.getEnch() instanceof ConflictingEnch conflictingEnch) {
                        for (EnchantmentType enchantmentType : conflictingEnch.getConflictingEnchantments()) {
                            if (item.getAttributeHandler().hasEnchantment(enchantmentType)) {
                                lore.add(Text.key("gui_enchantment.warning_remove_conflicting", StringUtility.toNormalCase(enchantmentType.name())));
                                break;
                            }
                        }
                    }

                    if (finalHasLevel == finalLevel) {
                        lore.addAll(Text.keyLines("gui_enchantment.already_present"));
                        lore.add(Text.of("<a> "));
                    }

                    lore.add(Text.key("gui_enchantment.cost_label"));

                    if (finalHasLevel > finalLevel) {
                        if (levelCost > player.getLevel())
                            lore.add(Text.key("gui_enchantment.cost_exp_levels_fail", levelCost));
                        else
                            lore.add(Text.key("gui_enchantment.cost_exp_levels_pass", levelCost));

                        lore.add(Text.of("<a> "));
                        lore.add(Text.key("gui_enchantment.higher_level_present"));
                        return ItemStacks.item(Material.GRAY_DYE, 1,
                                Text.of("<9>{} {:roman}", selected.getName(), finalLevel),
                                lore
                        );
                    }

                    if (levelCost > player.getLevel()) {
                        lore.add(Text.key("gui_enchantment.cost_exp_levels_fail", levelCost));
                        lore.add(Text.of("<a> "));
                        lore.add(Text.key("gui_enchantment.insufficient_levels"));
                    } else {
                        lore.add(Text.key("gui_enchantment.cost_exp_levels_pass", levelCost));
                        lore.add(Text.of("<a> "));
                        if (finalHasLevel >= finalLevel) {
                            lore.add(Text.key("gui_enchantment.click_to_remove"));
                        } else {
                            lore.add(Text.key("gui_enchantment.click_to_enchant"));
                        }
                    }

                    return ItemStacks.item(Material.ENCHANTED_BOOK, 1,
                            Text.of("<9>{} {:roman}", selected.getName(), finalLevel),
                            lore
                    );
                }

                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    if (e.getClickedItem().material() == Material.GRAY_DYE)
                        return;

                    String itemName = StringUtility.toNormalCase(type.name());

                    if (player.getLevel() < selected.getEnchFromTable().getLevelsFromTableToApply(player).get(finalLevel)) {
                        player.sendMessage(Text.key("gui_enchantment.insufficient_levels_message"));
                        return;
                    }

                    item.getAttributeHandler().removeEnchantment(selected);
                    if (finalHasLevel < finalLevel) {
                        item.getAttributeHandler().addEnchantment(
                                new SkyBlockEnchantment(selected, finalLevel)
                        );

                        if (selected.getEnch() instanceof ConflictingEnch conflictingEnch) {
                            for (EnchantmentType enchant : conflictingEnch.getConflictingEnchantments()) {
                                System.out.printf("conflicting enchant: " + enchant.name());
                                if (item.getAttributeHandler().hasEnchantment(enchant)) item.getAttributeHandler().removeEnchantment(enchant);
                            }
                        }

                        player.setLevel(player.getLevel() - selected.getEnchFromTable().getLevelsFromTableToApply(player).get(finalLevel));
                        player.sendMessage(Text.key("gui_enchantment.enchanted_message", itemName, StringUtility.toNormalCase(selected.name()), StringUtility.getAsRomanNumeral(finalLevel)));
                    } else {
                        int difference = finalHasLevel - finalLevel;

                        if (difference > 0) {
                            item.getAttributeHandler().addEnchantment(
                                    new SkyBlockEnchantment(selected, difference)
                            );
                        }

                        player.setLevel(player.getLevel() - selected.getEnchFromTable().getLevelsFromTableToApply(player).get(finalLevel));
                        player.sendMessage(Text.key("gui_enchantment.removed_message", StringUtility.toNormalCase(selected.name()), itemName));
                    }

                    updateFromItem(item, selected);
                }
            });
        }

        set(new GUIClickableItem(45) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                updateFromItem(item, null);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.ARROW, 1, Text.key("gui_enchantment.go_back"), List.of());
            }
        });

        updateItemStacks(getInventory(), getPlayer());
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onClose(InventoryCloseEvent e, CloseReason reason) {
        ((SkyBlockPlayer) e.getPlayer()).addAndUpdateItem(new SkyBlockItem(e.getInventory().getItemStack(19)));
    }

    @Override
    public void suddenlyQuit(Inventory inventory, HypixelPlayer p) {
        SkyBlockPlayer player = (SkyBlockPlayer) p;
        player.addAndUpdateItem(new SkyBlockItem(inventory.getItemStack(19)));
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {
    }

    public static Integer getBookshelfPower(Instance instance, Pos pos) {
        int power = 0;

        for (int x = -2; x <= 2; x++) {
            for (int y = 0; y <= 1; y++) {
                for (int z = -2; z <= 2; z++) {
                    if (StringUtility.getMaterialFromBlock(instance.getBlock(
                            pos.blockX() + x,
                            pos.blockY() + y,
                            pos.blockZ() + z)) == Material.BOOKSHELF) {
                        power++;
                    }
                }
            }
        }
        return Math.min(power, 60);
    }
}
