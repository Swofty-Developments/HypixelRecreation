package net.swofty.type.skywarsgame.manager;

import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.skywars.SkywarsGameType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.entity.hologram.HologramEntity;
import net.swofty.type.skywarsgame.game.SkywarsGame;
import net.swofty.type.skywarsgame.loot.ChestLootTable;
import net.swofty.type.skywarsgame.loot.LootTier;

import java.util.*;

public class ChestManager {
    private final Instance instance;
    private final SkywarsGameType gameType;
    private final List<Pos> islandChests;
    private final List<Pos> centerChests;
    private final Map<Pos, Inventory> openedChests = new HashMap<>();
    private final Set<Pos> refilledOnce = new HashSet<>();
    private final Map<Pos, HologramEntity> chestHolograms = new HashMap<>();
    private boolean refillScheduled = false;
    private int refillCount = 0;
    private long gameStartTime = 0;

    public ChestManager(Instance instance, SkywarsGameType gameType, List<Pos> islandChests, List<Pos> centerChests) {
        this.instance = instance;
        this.gameType = gameType;
        this.islandChests = new ArrayList<>(islandChests);
        this.centerChests = new ArrayList<>(centerChests);
    }

    public Inventory getChestInventory(Pos pos) {
        if (openedChests.containsKey(pos)) {
            return openedChests.get(pos);
        }

        Inventory chest = new Inventory(InventoryType.CHEST_3_ROW, "Chest");
        LootTier tier = islandChests.contains(pos) ? LootTier.ISLAND : LootTier.CENTER;
        fillChest(chest, tier);
        openedChests.put(pos, chest);
        return chest;
    }

    private void fillChest(Inventory chest, LootTier tier) {
        chest.clear();
        List<ItemStack> loot = ChestLootTable.generateLoot(tier, gameType.isInsane(), gameType);

        java.util.concurrent.ThreadLocalRandom random = java.util.concurrent.ThreadLocalRandom.current();
        Set<Integer> usedSlots = new HashSet<>();

        for (ItemStack item : loot) {
            int slot;
            int attempts = 0;
            do {
                slot = random.nextInt(27);
                attempts++;
            } while (usedSlots.contains(slot) && attempts < 50);

            if (usedSlots.add(slot)) {
                chest.setItemStack(slot, item);
            }
        }
    }

    public void scheduleRefills(Runnable broadcastFirst, Runnable broadcastSecond) {
        if (refillScheduled) return;
        refillScheduled = true;
        gameStartTime = System.currentTimeMillis();

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            refillAllChests();
            removeAllHolograms();
            refillCount = 1;
            broadcastFirst.run();
        }).delay(TaskSchedule.seconds(SkywarsGame.FIRST_REFILL_SECONDS)).schedule();

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            refillAllChests();
            removeAllHolograms();
            refillCount = 2;
            broadcastSecond.run();
        }).delay(TaskSchedule.seconds(SkywarsGame.SECOND_REFILL_SECONDS)).schedule();

        MinecraftServer.getSchedulerManager().buildTask(this::updateAllHolograms)
                .repeat(TaskSchedule.tick(20))
                .schedule();
    }

    private void refillAllChests() {
        for (Pos pos : openedChests.keySet()) {
            LootTier tier = islandChests.contains(pos) ? LootTier.ISLAND : LootTier.CENTER;
            fillChest(openedChests.get(pos), tier);
        }
    }

    public boolean isChestPosition(Pos pos) {
        return islandChests.contains(pos) || centerChests.contains(pos);
    }

    public void reset() {
        removeAllHolograms();
        openedChests.clear();
        refilledOnce.clear();
        chestHolograms.clear();
        refillScheduled = false;
        refillCount = 0;
        gameStartTime = 0;
    }

    public void triggerRefill(boolean isFirst) {
        refillAllChests();
        removeAllHolograms();
        refillCount = isFirst ? 1 : 2;
    }

    public void onChestClosed(Pos chestPos) {
        if (chestHolograms.containsKey(chestPos)) return;

        HologramEntity hologram = new HologramEntity(getHologramText());
        hologram.setAutoViewable(true);
        hologram.setInstance(instance, chestPos.add(0.5, 0, 0.5));
        chestHolograms.put(chestPos, hologram);
    }

    public Pos getChestPositionForInventory(Inventory inventory) {
        for (Map.Entry<Pos, Inventory> entry : openedChests.entrySet()) {
            if (entry.getValue() == inventory) {
                return entry.getKey();
            }
        }
        return null;
    }

    private Text getHologramText() {
        if (refillCount >= 2) {
            return Text.of("<c>Looted");
        }
        long remainingSeconds = getSecondsUntilNextRefill();
        return Text.of("<a>{}", formatTime(remainingSeconds));
    }

    private long getSecondsUntilNextRefill() {
        if (gameStartTime == 0) return 0;

        long elapsedMs = System.currentTimeMillis() - gameStartTime;
        long elapsedSeconds = elapsedMs / 1000;

        if (refillCount == 0) {
            return Math.max(0, SkywarsGame.FIRST_REFILL_SECONDS - elapsedSeconds);
        } else if (refillCount == 1) {
            return Math.max(0, SkywarsGame.SECOND_REFILL_SECONDS - elapsedSeconds);
        }
        return 0;
    }

    private String formatTime(long seconds) {
        long minutes = seconds / 60;
        long secs = seconds % 60;
        return String.format("%d:%02d", minutes, secs);
    }

    private void updateAllHolograms() {
        if (refillCount >= 2) return;

        Text newText = getHologramText();
        for (HologramEntity hologram : chestHolograms.values()) {
            hologram.setText(newText);
        }
    }

    private void removeAllHolograms() {
        for (HologramEntity hologram : chestHolograms.values()) {
            hologram.remove();
        }
        chestHolograms.clear();
    }
}
