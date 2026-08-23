package net.swofty.type.skyblockgeneric.loadout;

import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.UnderstandableSkyBlockItem;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointInventory;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointLoadouts;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointWardrobe;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.StandardItemComponent;
import net.swofty.type.skyblockgeneric.user.SkyBlockInventory;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.wardrobe.WardrobeService;

public final class LoadoutManager {
    public static final long TREE_SWAP_COOLDOWN = 10 * 60 * 1000L;

    private LoadoutManager() {
    }

    public static DatapointLoadouts.LoadoutsData data(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.LOADOUTS, DatapointLoadouts.class).getValue();
    }

    public static void save(SkyBlockPlayer player) {
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.LOADOUTS, DatapointLoadouts.class).setValue(data(player));
    }

    public static int unlockedLoadouts(SkyBlockPlayer player) {
        return Math.min(DatapointLoadouts.LOADOUT_COUNT, WardrobeService.rankSlots(player.getRank()) + communitySlots(player));
    }

    public static int unlockedEquipmentSets(SkyBlockPlayer player) {
        return Math.min(DatapointLoadouts.EQUIPMENT_SET_COUNT, WardrobeService.rankSlots(player.getRank()) + communitySlots(player));
    }

    private static int communitySlots(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.WARDROBE, DatapointWardrobe.class)
                .getValue().getCommunitySlots();
    }

    public static boolean equip(SkyBlockPlayer player, int index) {
        DatapointLoadouts.LoadoutsData data = data(player);
        if (index < 0 || index >= unlockedLoadouts(player)) return false;
        DatapointLoadouts.Loadout loadout = data.getLoadouts()[index];
        if (loadout.isEmpty()) {
            player.sendMessage("<c>You must customize this loadout before you can equip it!");
            return false;
        }
        long now = System.currentTimeMillis();
        if (!canSwap(data.getActiveHotmSlot(), loadout.getHotmSlot(), data.getLastHotmSwap(), now)
                || !canSwap(data.getActiveHotfSlot(), loadout.getHotfSlot(), data.getLastHotfSwap(), now)) {
            player.sendMessage("<c>You must wait before swapping skill trees again!");
            return false;
        }

        SkyBlockItem[] armor = resolvedArmor(player, loadout);
        player.setHelmet(stack(armor[0]));
        player.setChestplate(stack(armor[1]));
        player.setLeggings(stack(armor[2]));
        player.setBoots(stack(armor[3]));

        SkyBlockInventory inventory = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.INVENTORY, DatapointInventory.class).getValue();
        SkyBlockItem[] equipment = loadout.getEquipment();
        inventory.setNecklace(understandable(equipment[0]));
        inventory.setCloak(understandable(equipment[1]));
        inventory.setBelt(understandable(equipment[2]));
        inventory.setGloves(understandable(equipment[3]));
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.INVENTORY, DatapointInventory.class).setValue(inventory);

        player.getPetData().deselectCurrent();
        if (loadout.getPetType() != null) {
            try {
                player.getPetData().setEnabled(ItemType.valueOf(loadout.getPetType()), true);
            } catch (IllegalArgumentException ignored) {
            }
        }
        player.getPetData().updatePetEntityImpl(player);

        if (loadout.getHotmSlot() >= 0 && loadout.getHotmSlot() != data.getActiveHotmSlot()) {
            data.setActiveHotmSlot(loadout.getHotmSlot());
            data.setLastHotmSwap(now);
        }
        if (loadout.getHotfSlot() >= 0 && loadout.getHotfSlot() != data.getActiveHotfSlot()) {
            data.setActiveHotfSlot(loadout.getHotfSlot());
            data.setLastHotfSwap(now);
        }
        data.setEquipped(index);
        save(player);
        player.sendMessage("<a>Equipped {}!", loadout.getName());
        return true;
    }

    public static boolean switchTree(SkyBlockPlayer player, TreeType tree, int slot) {
        if (slot < 0 || slot >= 2) return false;
        DatapointLoadouts.LoadoutsData data = data(player);
        int active = tree == TreeType.HOTM ? data.getActiveHotmSlot() : data.getActiveHotfSlot();
        long lastSwap = tree == TreeType.HOTM ? data.getLastHotmSwap() : data.getLastHotfSwap();
        long now = System.currentTimeMillis();
        if (!canSwap(active, slot, lastSwap, now)) {
            player.sendMessage("<c>You must wait before swapping trees again!");
            return false;
        }
        if (tree == TreeType.HOTM) {
            data.setActiveHotmSlot(slot);
            data.setLastHotmSwap(now);
        } else {
            data.setActiveHotfSlot(slot);
            data.setLastHotfSwap(now);
        }
        save(player);
        return true;
    }

    private static boolean canSwap(int active, int requested, long lastSwap, long now) {
        return requested < 0 || requested == active || now - lastSwap >= TREE_SWAP_COOLDOWN;
    }

    public static boolean acceptsEquipment(int component, SkyBlockItem item) {
        if (item == null || item.isNA() || !item.hasComponent(StandardItemComponent.class)) return false;
        StandardItemComponent.StandardItemType type = item.getComponent(StandardItemComponent.class).getType();
        return switch (component) {
            case 0 -> type == StandardItemComponent.StandardItemType.NECKLACE;
            case 1 -> type == StandardItemComponent.StandardItemType.CLOAK;
            case 2 -> type == StandardItemComponent.StandardItemType.BELT;
            case 3 -> type == StandardItemComponent.StandardItemType.GLOVES
                    || type == StandardItemComponent.StandardItemType.BRACELET;
            default -> false;
        };
    }

    public static SkyBlockItem currentEquipment(SkyBlockPlayer player, int component) {
        SkyBlockInventory inventory = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.INVENTORY, DatapointInventory.class).getValue();
        UnderstandableSkyBlockItem item = switch (component) {
            case 0 -> inventory.getNecklace();
            case 1 -> inventory.getCloak();
            case 2 -> inventory.getBelt();
            default -> inventory.getGloves();
        };
        return new SkyBlockItem(item);
    }

    public static SkyBlockItem currentArmor(SkyBlockPlayer player, int component) {
        ItemStack item = switch (component) {
            case 0 -> player.getHelmet();
            case 1 -> player.getChestplate();
            case 2 -> player.getLeggings();
            default -> player.getBoots();
        };
        return item.isAir() ? null : new SkyBlockItem(item);
    }

    public static SkyBlockItem loadoutArmor(SkyBlockPlayer player, DatapointLoadouts.Loadout loadout, int component) {
        int setIndex = loadout.getArmorSet();
        if (setIndex < 0 || setIndex >= 27) return loadout.getArmor()[component];
        DatapointWardrobe.WardrobeData wardrobe = player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.WARDROBE, DatapointWardrobe.class).getValue();
        if (wardrobe.getEquippedSlot() == setIndex) return currentArmor(player, component);
        return wardrobe.getSets()[setIndex].getPieces()[component];
    }

    private static SkyBlockItem[] resolvedArmor(SkyBlockPlayer player, DatapointLoadouts.Loadout loadout) {
        SkyBlockItem[] armor = new SkyBlockItem[4];
        for (int i = 0; i < armor.length; i++) armor[i] = loadoutArmor(player, loadout, i);
        return armor;
    }

    private static ItemStack stack(SkyBlockItem item) {
        return item == null || item.isNA() ? ItemStack.AIR : item.getItemStack();
    }

    private static UnderstandableSkyBlockItem understandable(SkyBlockItem item) {
        return (item == null || item.isNA() ? new SkyBlockItem(Material.AIR) : item).toUnderstandable();
    }

    public enum TreeType {
        HOTM,
        HOTF
    }
}
