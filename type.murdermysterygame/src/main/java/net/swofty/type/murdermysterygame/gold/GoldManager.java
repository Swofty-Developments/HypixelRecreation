package net.swofty.type.murdermysterygame.gold;

import net.swofty.commons.mc.HypixelPosition;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.item.ItemEntityMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.CollectItemPacket;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.murdermystery.map.MurderMysteryMapsConfig;
import net.swofty.type.generic.achievement.PlayerAchievementHandler;
import net.swofty.type.murdermysterygame.game.Game;
import net.swofty.type.game.game.GameState;
import net.swofty.type.murdermysterygame.user.MurderMysteryPlayer;

import net.minestom.server.inventory.PlayerInventory;

import java.util.*;

public class GoldManager {
    private static final int GOLD_FOR_BOW = 10;
    private static final int SPAWN_INTERVAL_SECONDS = 5;
    private static final int MAX_GOLD_SPAWNED = 50;
    private static final double PICKUP_DISTANCE = 1.5;
    /** Closer than this and we count a spawn point as 'already occupied' — see #673. */
    private static final double SPAWN_POINT_OCCUPIED_DISTANCE = 1.0;

    private final Game game;
    private final List<Entity> spawnedGold = new ArrayList<>();
    private Task spawnTask;
    private Task pickupCheckTask;

    public GoldManager(Game game) {
        this.game = game;
    }

    public void startSpawning() {
        var config = game.getMapEntry().getConfiguration();
        if (config == null || config.getGoldSpawns() == null || config.getGoldSpawns().isEmpty()) {
            return;
        }

        List<HypixelPosition> spawnLocations = config.getGoldSpawns();

        spawnTask = MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (spawnedGold.size() >= MAX_GOLD_SPAWNED || spawnLocations.isEmpty()) {
                return;
            }
            HypixelPosition randomSpawn = spawnLocations.get(java.util.concurrent.ThreadLocalRandom.current().nextInt(spawnLocations.size()));
            Pos spawnPos = new Pos(randomSpawn.x(), randomSpawn.y(), randomSpawn.z());
            // Issue #673: cap each spawn point at one gold coin. The previous loop
            // rolled the same point repeatedly and stacked up to five coins there.
            if (isSpawnPointOccupied(spawnPos)) {
                return;
            }
            spawnGoldCoin(spawnPos);
        }).delay(TaskSchedule.seconds(3)).repeat(TaskSchedule.seconds(SPAWN_INTERVAL_SECONDS)).schedule();

        pickupCheckTask = MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (game.getState() != GameState.IN_PROGRESS) return;

            List<Entity> goldToRemove = new ArrayList<>();

            for (Entity goldEntity : new ArrayList<>(spawnedGold)) {
                if (goldEntity.isRemoved()) {
                    goldToRemove.add(goldEntity);
                    continue;
                }

                for (MurderMysteryPlayer player : game.getPlayers()) {
                    if (player.isEliminated()) continue;

                    double distance = goldEntity.getPosition().distance(player.getPosition());
                    if (distance <= PICKUP_DISTANCE) {
                        goldToRemove.add(goldEntity);

                        player.sendPacket(new CollectItemPacket(goldEntity.getEntityId(), player.getEntityId(), 1));

                        addGoldToSlot(player, 1);

                        player.sendMessage(Component.text("+1 Gold", NamedTextColor.GOLD));

                        trackGoldAchievements(player, 1);

                        int goldInInventory = countGoldInInventory(player);
                        if (goldInInventory >= GOLD_FOR_BOW) {
                            removeGoldFromInventory(player, GOLD_FOR_BOW);
                            game.getWeaponManager().giveInnocentBow(player);
                        }

                        goldEntity.remove();
                        break;
                    }
                }
            }

            spawnedGold.removeAll(goldToRemove);
        }).repeat(TaskSchedule.tick(2)).schedule();
    }

    public void stopSpawning() {
        if (spawnTask != null) {
            spawnTask.cancel();
        }
        if (pickupCheckTask != null) {
            pickupCheckTask.cancel();
        }
        spawnedGold.forEach(Entity::remove);
        spawnedGold.clear();
    }

    private void spawnGoldCoin(Pos position) {
        Entity goldEntity = new Entity(EntityType.ITEM);
        ItemEntityMeta meta = (ItemEntityMeta) goldEntity.getEntityMeta();
        meta.setItem(ItemStack.of(Material.GOLD_INGOT));
        goldEntity.setInstance(game.getInstanceContainer(), position);
        spawnedGold.add(goldEntity);
    }

    private boolean isSpawnPointOccupied(Pos position) {
        for (Entity existing : spawnedGold) {
            if (existing.isRemoved()) continue;
            if (existing.getPosition().distance(position) <= SPAWN_POINT_OCCUPIED_DISTANCE) {
                return true;
            }
        }
        return false;
    }

    public boolean collectGold(MurderMysteryPlayer player, Entity goldEntity) {
        if (!spawnedGold.contains(goldEntity)) return false;

        spawnedGold.remove(goldEntity);
        goldEntity.remove();

        addGoldToSlot(player, 1);

        trackGoldAchievements(player, 1);

        int goldInInventory = countGoldInInventory(player);
        if (goldInInventory >= GOLD_FOR_BOW) {
            removeGoldFromInventory(player, GOLD_FOR_BOW);
            game.getWeaponManager().giveInnocentBow(player);
            return true;
        }

        return false;
    }

    private void trackGoldAchievements(MurderMysteryPlayer player, int amount) {
        PlayerAchievementHandler achHandler = new PlayerAchievementHandler(player);

        player.addGoldCollectedThisGame(amount);

        achHandler.addProgress("murdermystery.gold_hunter", amount);

        achHandler.addProgress("murdermystery.hoarder", amount);

        long gameStartTime = game.getGameStartTime();
        if (gameStartTime > 0 && System.currentTimeMillis() - gameStartTime <= 60000) {
            player.addGoldInFirstMinute(amount);
            if (player.getGoldCollectedInFirstMinute() >= 10) {
                achHandler.addProgress("murdermystery.that_was_easy", 1);
            }
        }
    }

    public int getGoldForBow() {
        return GOLD_FOR_BOW;
    }

    public List<Entity> getSpawnedGold() {
        return spawnedGold;
    }

    public int countGoldInInventory(MurderMysteryPlayer player) {
        ItemStack slot8Item = player.getInventory().getItemStack(8);
        if (slot8Item.material() == Material.GOLD_INGOT) {
            return slot8Item.amount();
        }
        return 0;
    }

    public void removeGoldFromInventory(MurderMysteryPlayer player, int amount) {
        PlayerInventory inventory = player.getInventory();
        ItemStack slot8Item = inventory.getItemStack(8);
        if (slot8Item.material() == Material.GOLD_INGOT) {
            int remove = Math.min(slot8Item.amount(), amount);
            if (slot8Item.amount() > remove) {
                inventory.setItemStack(8, slot8Item.withAmount(slot8Item.amount() - remove));
            } else {
                inventory.setItemStack(8, ItemStack.AIR);
            }
        }
    }

    public void checkPlayerGoldForBow(MurderMysteryPlayer player) {
        int goldInInventory = countGoldInInventory(player);
        if (goldInInventory >= GOLD_FOR_BOW) {
            removeGoldFromInventory(player, GOLD_FOR_BOW);
            game.getWeaponManager().giveInnocentBow(player);
        }
    }

    private void addGoldToSlot(MurderMysteryPlayer player, int amount) {
        PlayerInventory inventory = player.getInventory();
        ItemStack slot8Item = inventory.getItemStack(8);
        
        if (slot8Item.material() == Material.GOLD_INGOT) {
            inventory.setItemStack(8, slot8Item.withAmount(slot8Item.amount() + amount));
        } else {
            inventory.setItemStack(8, ItemStack.of(Material.GOLD_INGOT, amount));
        }
    }
}
