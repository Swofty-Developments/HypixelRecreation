package net.swofty.type.hub.gui;

import net.minestom.server.component.DataComponents;
import net.minestom.server.event.inventory.InventoryCloseEvent;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.item.attribute.attributes.ItemAttributeRuneInfusedWith;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.HypixelInventoryGUI;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.inventory.item.GUIClickableItem;
import net.swofty.type.generic.gui.inventory.item.GUIItem;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.RuneComponent;
import net.swofty.type.skyblockgeneric.item.components.RuneableComponent;
import net.swofty.type.skyblockgeneric.item.updater.PlayerItemUpdater;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class GUIRunicPedestal extends HypixelInventoryGUI {
    private static final int MAX_RUNE_LEVEL = 3;
    private static final int[] BOTTOM_SLOTS = {45, 46, 47, 48, 50, 51, 52, 53};
    private static final int[] LEFT_RUNIC_SLOTS = {10, 11, 12};
    private static final int[] RIGHT_RUNIC_SLOTS = {14, 15, 16};
    private static final int[] CONNECTOR_RUNIC_SLOTS = {22};
    private static final ScheduledExecutorService animationScheduler = Executors.newScheduledThreadPool(1);

    private SkyBlockItem itemOnLeft = null;
    private SkyBlockItem itemOnRight = null;
    private SkyBlockItem outputItem = null;
    private boolean isFusing = false;
    private CompletableFuture<Boolean> fusingAnimation = null;

    public GUIRunicPedestal() {
        super("Runic Pedestal", InventoryType.CHEST_6_ROW);
    }

    @Override
    public boolean allowHotkeying() {
        return false;
    }

    @Override
    public void onOpen(InventoryGUIOpenEvent e) {
        fill(Material.BLACK_STAINED_GLASS_PANE, "");
        setGlassPanes(BOTTOM_SLOTS, Material.WHITE_STAINED_GLASS_PANE);
        setGlassPanes(LEFT_RUNIC_SLOTS, Material.WHITE_STAINED_GLASS_PANE);
        setGlassPanes(RIGHT_RUNIC_SLOTS, Material.WHITE_STAINED_GLASS_PANE);
        setGlassPanes(CONNECTOR_RUNIC_SLOTS, Material.WHITE_STAINED_GLASS_PANE);
        set(GUIClickableItem.getCloseItem(49));

        set(new GUIClickableItem(44) {
            @Override
            public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p; 
                new GUIRuneRemoval().open(player);
            }

            @Override
            public ItemStack.Builder getItem(HypixelPlayer p) {
                return ItemStacks.item(Material.CAULDRON, """
                        <e>Rune Removal
                        <7>Sometimes, simplicity is most beautiful.""");
            }
        });
        set(GUIClickableItem.getCloseItem(49));

        updateFromItem(null, null);
    }

    public void updateFromItem(SkyBlockItem itemPutOnLeft, SkyBlockItem itemPutOnRight) {
        this.itemOnLeft = itemPutOnLeft;
        this.itemOnRight = itemPutOnRight;

        setGlassPanes(BOTTOM_SLOTS, Material.WHITE_STAINED_GLASS_PANE);
        setGlassPanes(LEFT_RUNIC_SLOTS, Material.WHITE_STAINED_GLASS_PANE);
        setGlassPanes(RIGHT_RUNIC_SLOTS, Material.WHITE_STAINED_GLASS_PANE);
        setGlassPanes(CONNECTOR_RUNIC_SLOTS, Material.WHITE_STAINED_GLASS_PANE);

        if (itemOnLeft == null) {
            set(new GUIClickableItem(19) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    ItemStack stack = p.getInventory().getCursorItem();

                    if (stack.get(DataComponents.CUSTOM_NAME) == null) {
                        updateFromItem(new SkyBlockItem(stack), itemOnRight);
                        return;
                    }

                    updateFromItem(new SkyBlockItem(stack), itemOnRight);
                }

                public boolean canPickup() {
                    return true;
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return ItemStack.builder(Material.AIR);
                }
            });
        } else {
            setGlassPanes(LEFT_RUNIC_SLOTS, Material.PURPLE_STAINED_GLASS_PANE);
            set(new GUIClickableItem(19) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    if (isFusing) {
                        player.sendMessage("<c>You cannot remove the item on the left while fusing!");
                        return;
                    }

                    player.addAndUpdateItem(itemOnLeft);
                    updateFromItem(null, itemOnRight);
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    SkyBlockPlayer player = (SkyBlockPlayer) p;
                    return PlayerItemUpdater.playerUpdate(player, itemOnLeft.getItemStack());
                }
            });
        }

        if (itemOnRight == null) {
            set(new GUIClickableItem(25) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    ItemStack stack = p.getInventory().getCursorItem();

                    if (stack.get(DataComponents.CUSTOM_NAME) == null) {
                        updateFromItem(itemOnLeft, new SkyBlockItem(stack));
                        return;
                    }

                    updateFromItem(itemOnLeft, new SkyBlockItem(stack));
                }

                @Override
                public boolean canPickup() {
                    return true;
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p; 
                    return ItemStack.builder(Material.AIR);
                }
            });
        } else {
            setGlassPanes(RIGHT_RUNIC_SLOTS, Material.PURPLE_STAINED_GLASS_PANE);
            set(new GUIClickableItem(25) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p; 
                    if (isFusing) {
                        player.sendMessage("<c>You cannot remove the item on the right while fusing!");
                        return;
                    }

                    player.addAndUpdateItem(itemOnRight);
                    updateFromItem(itemOnLeft, null);
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p; 
                    return PlayerItemUpdater.playerUpdate(player, itemOnRight.getItemStack());
                }
            });
        }

        if (itemOnLeft == null || itemOnLeft.isAir()
                || itemOnRight == null || itemOnRight.isAir()) {
            set(new GUIItem(13) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return ItemStacks.item(Material.END_PORTAL_FRAME, """
                            <a>Apply a Rune or Fuse Two Runes
                            <7>Add the rune to your provided item
                            <7>or provide two runes to attempt to
                            <7>fuse them.""");
                }
            });
            set(new GUIItem(31) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return ItemStacks.item(Material.BARRIER, """
                            <c>Runic Pedestal
                            <7>Place a target item in the left slot
                            <7>and a sacrifice rune in the right slot
                            <7>to add the two runes effect's to the item
                            <7>or add two runes to attempt to fuse
                            <7>them!""");
                }
            });
            updateItemStacks(getInventory(), getPlayer());
            return;
        }

        RunicMerge runicMerge = null;
        for (RunicMerge merge : RunicMerge.getMerges()) {
            if (merge.isValidType(itemOnLeft, itemOnRight)) {
                runicMerge = merge;
                break;
            }
        }

        if (runicMerge == null) {
            set(new GUIItem(13) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return ItemStacks.item(Material.END_PORTAL_FRAME, """
                            <a>Apply a Rune or Fuse Two Runes
                            <7>Add the rune to your provided item
                            <7>or provide two runes to attempt to
                            <7>fuse them.""");
                }
            });
            set(new GUIItem(31) {
                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return ItemStacks.item(Material.BARRIER, """
                            <c>Error!
                            <7>You cannot combine those items!""");
                }
            });
        } else {
            RunicMerge finalRunicMerge = runicMerge;
            setGlassPanes(BOTTOM_SLOTS, Material.PURPLE_STAINED_GLASS_PANE);
            setGlassPanes(CONNECTOR_RUNIC_SLOTS, Material.PURPLE_STAINED_GLASS_PANE);

            set(new GUIClickableItem(13) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                SkyBlockPlayer player = (SkyBlockPlayer) p; 
                    if (isFusing) {
                        player.sendMessage("<c>You cannot apply a rune while fusing!");
                        return;
                    }

                    isFusing = true;
                    outputItem = finalRunicMerge.merge(itemOnLeft, itemOnRight);
                    itemOnLeft = null;
                    itemOnRight = null;

                    startFusingAnimation().thenAccept((hasDisconnected) -> {
                        if (hasDisconnected)
                            return;

                        isFusing = false;
                        updateFromItem(null, null);
                        player.addAndUpdateItem(outputItem);
                        finalRunicMerge.onSuccess(player, outputItem);
                        outputItem = null;
                    });
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    return ItemStacks.item(Material.END_PORTAL_FRAME, 1, """
                            <a>Combine Items
                            <7>Combine the provided items.
                            <r>\s
                            <e>Click to combine!""");
                }
            });
            set(new GUIClickableItem(31) {
                @Override
                public void run(InventoryPreClickEvent e, HypixelPlayer p) {
                    p.sendMessage("<c>You must click the Portal Frame above to combine the two items!");
                }

                @Override
                public ItemStack.Builder getItem(HypixelPlayer p) {
                    SkyBlockItem mergedItem = finalRunicMerge.merge(itemOnLeft, itemOnRight);
                    List<Text> lore = new ArrayList<>(mergedItem.getLore().stream().map(Text::legacy).toList());
                    lore.add(Text.of("<8><m>-------------------</m>"));
                    lore.add(Text.of("<a>This is the item you will get."));
                    lore.add(Text.of("<a>Click the <c>PORTAL FRAME ABOVE </c>to"));
                    lore.add(Text.of("<a>combine."));

                    return ItemStacks.item(mergedItem.getMaterial(), 1,
                            mergedItem.getDisplayName(), lore);
                }
            });
        }

        updateItemStacks(getInventory(), getPlayer());
    }

    public static abstract class RunicMerge {
        public abstract SkyBlockItem merge(SkyBlockItem itemOnLeft, SkyBlockItem itemOnRight);
        public abstract boolean isValidType(SkyBlockItem itemOnLeft, SkyBlockItem itemOnRight);
        public abstract void onSuccess(SkyBlockPlayer player, SkyBlockItem outputItem);

        public static List<RunicMerge> getMerges() {
            return List.of(new RuneRunicMerge(), new RuneWithItemRunicMerge());
        }
    }

    public static class RuneRunicMerge extends RunicMerge {
        @Override
        public SkyBlockItem merge(SkyBlockItem itemOnLeft, SkyBlockItem itemOnRight) {
            int runeLevelOnLeft = itemOnLeft.getAttributeHandler().getRuneLevel();
            int runeLevelOnRight = itemOnRight.getAttributeHandler().getRuneLevel();

            SkyBlockItem toReturn = itemOnLeft.clone();
            toReturn.getAttributeHandler().setRuneLevel(runeLevelOnLeft + runeLevelOnRight);
            return toReturn;
        }

        @Override
        public boolean isValidType(SkyBlockItem itemOnLeft, SkyBlockItem itemOnRight) {
            if (!(itemOnLeft.hasComponent(RuneComponent.class)) || !(itemOnRight.hasComponent(RuneComponent.class))) {
                return false;
            }
            String runeTypeOnLeft = itemOnLeft.getAttributeHandler().getTypeAsString();
            String runeTypeOnRight = itemOnRight.getAttributeHandler().getTypeAsString();
            if (!runeTypeOnLeft.equals(runeTypeOnRight))
                return false;

            int runeLevelOnLeft = itemOnLeft.getAttributeHandler().getRuneLevel();
            int runeLevelOnRight = itemOnRight.getAttributeHandler().getRuneLevel();

            return runeLevelOnLeft + runeLevelOnRight <= MAX_RUNE_LEVEL;
        }

        @Override
        public void onSuccess(SkyBlockPlayer player, SkyBlockItem outputItem) {
            int level = outputItem.getAttributeHandler().getRuneLevel();

            player.getSkills().increase(player, SkillCategories.RUNECRAFTING, 15D);

            sendSuccessMessage(player, "Combining Runes to Level {}", level);
        }
    }

    public static class RuneWithItemRunicMerge extends RunicMerge {
        @Override
        public SkyBlockItem merge(SkyBlockItem itemOnLeft, SkyBlockItem itemOnRight) {
            SkyBlockItem runeItem = itemOnLeft.hasComponent(RuneComponent.class) ? itemOnLeft : itemOnRight;
            SkyBlockItem item = itemOnLeft.hasComponent(RuneComponent.class) ? itemOnRight : itemOnLeft;

            SkyBlockItem toReturn = item.clone();
            ItemAttributeRuneInfusedWith.RuneData runeData = toReturn.getAttributeHandler().getRuneData();
            runeData.setRuneType(runeItem.getAttributeHandler().getPotentialType());
            runeData.setLevel(runeItem.getAttributeHandler().getRuneLevel());
            toReturn.getAttributeHandler().setRuneData(runeData);
            return toReturn;
        }

        @Override
        public boolean isValidType(SkyBlockItem itemOnLeft, SkyBlockItem itemOnRight) {
            if (!(itemOnLeft.hasComponent(RuneComponent.class)) && !(itemOnLeft.hasComponent(RuneableComponent.class)))
                return false;
            if (!(itemOnRight.hasComponent(RuneComponent.class)) && !(itemOnRight.hasComponent(RuneableComponent.class)))
                return false;
            if (itemOnLeft.hasComponent(RuneComponent.class) && itemOnRight.hasComponent(RuneComponent.class))
                return false;
            if (itemOnLeft.hasComponent(RuneableComponent.class) && itemOnRight.hasComponent(RuneableComponent.class))
                return false;

            SkyBlockItem runableItem = itemOnLeft.hasComponent(RuneableComponent.class) ? itemOnLeft : itemOnRight;
            ItemAttributeRuneInfusedWith.RuneData runeData = runableItem.getAttributeHandler().getRuneData();

            return !runeData.hasRune();
        }

        @Override
        public void onSuccess(SkyBlockPlayer player, SkyBlockItem outputItem) {
            player.getSkills().increase(player, SkillCategories.RUNECRAFTING, 15D);

            ItemType appliedRune = outputItem.getAttributeHandler().getRuneData().getRuneType();
            Rarity rarity = appliedRune.rarity;

            sendSuccessMessage(player, "Applying {} ({}<d>)",
                    StringUtility.toNormalCase(appliedRune.name()), rarity.getDisplay());
        }
    }

    private static void sendSuccessMessage(SkyBlockPlayer player, String action, Object... arguments) {
        player.sendMessage("<d>-<5>15 <d>Runecrafting XP <7>- <r>" + action, arguments);
    }

    @Override
    public void onBottomClick(InventoryPreClickEvent e) {}

    @Override
    public void onClose(InventoryCloseEvent e, CloseReason reason) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.getPlayer();
        player.addAndUpdateItem(itemOnLeft);
        player.addAndUpdateItem(itemOnRight);
        player.addAndUpdateItem(outputItem);
        if (fusingAnimation != null && !fusingAnimation.isDone()) {
            fusingAnimation.complete(true);
        }
    }

    @Override
    public void suddenlyQuit(Inventory inventory, HypixelPlayer player) {
        ((SkyBlockPlayer) player).addAndUpdateItem(itemOnLeft);
        ((SkyBlockPlayer) player).addAndUpdateItem(itemOnRight);
        ((SkyBlockPlayer) player).addAndUpdateItem(outputItem);
        if (fusingAnimation != null && !fusingAnimation.isDone()) {
            fusingAnimation.complete(true);
        }
    }

    public CompletableFuture<Boolean> startFusingAnimation() {
        fusingAnimation = new CompletableFuture<>();

        // Incrementally change the colors of the slots from purple to pink and back multiple times over the duration
        int duration = 1500; // Duration in milliseconds
        int interval = 30; // Interval between each color update in milliseconds
        int cycles = 5; // Number of times to transition from purple to pink and back

        int totalSteps = duration / interval;
        int stepsPerCycle = totalSteps / cycles;

        Material[] colors = {
                Material.PURPLE_STAINED_GLASS_PANE,
                Material.MAGENTA_STAINED_GLASS_PANE,
                Material.PINK_STAINED_GLASS_PANE
        };

        AtomicInteger currentStep = new AtomicInteger(0);
        ScheduledFuture<?>[] taskHolder = new ScheduledFuture<?>[1];

        taskHolder[0] = animationScheduler.scheduleAtFixedRate(() -> {
            int step = currentStep.getAndIncrement();

            if (step >= totalSteps) {
                // Cancel the task
                if (taskHolder[0] != null) {
                    taskHolder[0].cancel(false);
                }
                // Animation completed successfully
                fusingAnimation.complete(false);
                return;
            }

            int cycleIndex = step / stepsPerCycle;
            double progress = (double) (step % stepsPerCycle) / (stepsPerCycle - 1);

            if (cycleIndex % 2 == 0) {
                // Purple to pink
                int colorIndex = (int) Math.floor(progress * (colors.length - 1));
                setGlassPanes(BOTTOM_SLOTS, colors[colorIndex]);
                setGlassPanes(CONNECTOR_RUNIC_SLOTS, colors[colorIndex]);
                setGlassPanes(LEFT_RUNIC_SLOTS, colors[colorIndex]);
                setGlassPanes(RIGHT_RUNIC_SLOTS, colors[colorIndex]);
            } else {
                // Pink to purple
                int colorIndex = (int) Math.floor((1 - progress) * (colors.length - 1));
                setGlassPanes(BOTTOM_SLOTS, colors[colorIndex]);
                setGlassPanes(CONNECTOR_RUNIC_SLOTS, colors[colorIndex]);
                setGlassPanes(LEFT_RUNIC_SLOTS, colors[colorIndex]);
                setGlassPanes(RIGHT_RUNIC_SLOTS, colors[colorIndex]);
            }
        }, 0, interval, TimeUnit.MILLISECONDS);

        return fusingAnimation;
    }

    private void setGlassPanes(int[] slots, Material material) {
        for (int slot : slots) {
            set(slot, ItemStacks.filler(material));
            getInventory().setItemStack(slot, ItemStacks.filler(material).build());
        }
    }
}
