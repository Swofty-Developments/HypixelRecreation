package net.swofty.type.bedwarsgame.events;

import net.minestom.server.entity.ItemEntity;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.event.item.ItemDropEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerStartDiggingEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.inventory.PlayerInventory;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig.MapTeam;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig.TeamKey;
import net.swofty.commons.mc.HypixelPosition;
import net.swofty.commons.text.Text;
import net.swofty.type.bedwarsgame.game.BedWarsGame;
import net.swofty.type.bedwarsgame.gui.GUIEnderChest;
import net.swofty.type.bedwarsgame.gui.GUITeamChest;
import net.swofty.type.bedwarsgame.shop.Currency;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.bedwarsgame.util.BedWarsInventoryManipulator;
import net.swofty.type.game.game.GameState;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.PhasedEvent;
import org.tinylog.Logger;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ActionGamePlayerEvent implements HypixelEventClass {

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false)
    public void run(InventoryPreClickEvent event) {
        if (!(event.getInventory() instanceof PlayerInventory)) {
            return;
        }

        BedWarsPlayer player = (BedWarsPlayer) event.getPlayer();
        if (event.getClickedItem().material().armor()) {
            event.setCancelled(true);
            return;
        }

        BedWarsGame game = player.getGame();
        if (game == null) {
            event.setCancelled(true);
            return;
        }

        if (game.getState() == GameState.WAITING) {
            event.setCancelled(true);
        }
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false)
    public void run(ItemDropEvent event) {
        switch (event.getItemStack().material().name()) {
            case "wooden_sword":
            case "wooden_pickaxe":
            case "wooden_axe":
            case "stone_axe":
            case "iron_pickaxe":
            case "iron_axe":
            case "diamond_pickaxe":
            case "diamond_axe":
                event.setCancelled(true);
                ((BedWarsPlayer) event.getPlayer()).sendMessage("<c>You cannot drop your tools!");
                break;
            default:
                ItemEntity itemEntity = new ItemEntity(event.getItemStack());
                itemEntity.setInstance(event.getPlayer().getInstance(), event.getPlayer().getPosition().add(0, event.getPlayer().getEyeHeight(), 0));
                itemEntity.setVelocity(event.getPlayer().getPosition().add(0, 0.3, 0).direction().mul(6));
                itemEntity.setPickupDelay(Duration.ofMillis(500));
                break;
        }
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false, isAsync = true)
    public void run(PlayerBlockInteractEvent event) {
        Block block = event.getBlock();
        BedWarsPlayer player = (BedWarsPlayer) event.getPlayer();
        if (player.getGame() == null) {
            event.setCancelled(true);
            return;
        }
        if (block.material() == Material.ENDER_CHEST) {
            player.openView(new GUIEnderChest());
            return;
        }

        if (block.material() != Material.CHEST) return;

        BedWarsGame game = player.getGame();
        if (game == null || game.getState() != GameState.IN_PROGRESS) {
            event.setCancelled(true);
            return;
        }

        TeamKey playerTeamKey = player.getTeamKey();
        if (playerTeamKey == null) {
            event.setCancelled(true);
            return;
        }

        double closestDistance = Double.MAX_VALUE;
        TeamKey chestTeamKey = null;

        for (Map.Entry<TeamKey, MapTeam> entry : game.getMapEntry().getConfiguration().getTeams().entrySet()) {
            TeamKey teamKey = entry.getKey();
            MapTeam team = entry.getValue();
            HypixelPosition teamSpawn = team.getSpawn();
            if (teamSpawn == null) continue;

            double distance = Math.sqrt(
                Math.pow(event.getBlockPosition().x() - teamSpawn.x(), 2) +
                    Math.pow(event.getBlockPosition().y() - teamSpawn.y(), 2) +
                    Math.pow(event.getBlockPosition().z() - teamSpawn.z(), 2)
            );

            if (distance < 10.0 && distance < closestDistance) {
                closestDistance = distance;
                chestTeamKey = teamKey;
            }
        }

        if (chestTeamKey == null) {
            return;
        }

        boolean sameTeam = chestTeamKey.equals(playerTeamKey);
        if (!sameTeam && game.isBedAlive(chestTeamKey)) {
            event.setCancelled(true);
            player.sendMessage("<c>You can only access enemy team chests if their bed is destroyed!");
            return;
        }

        GUITeamChest.open(player, chestTeamKey);
    }

    @PhasedEvent(node = EventNodes.PLAYER, requireDataLoaded = false)
    public void run(PlayerStartDiggingEvent event) {
        Block block = event.getBlock();
        BedWarsPlayer player = (BedWarsPlayer) event.getPlayer();
        ItemStack itemInHand = player.getItemInMainHand();

        BedWarsGame game = player.getGame();
        if (game == null || game.getState() != GameState.IN_PROGRESS) {
            return;
        }

        if (itemInHand.isAir()) {
            if (block.material() == Material.CHEST || block.material() == Material.ENDER_CHEST) {
                event.setCancelled(true);
            }
            return;
        }

        if (block.material() != Material.CHEST &&
            block.material() != Material.ENDER_CHEST) {
            return;
        }

        if (!BedWarsInventoryManipulator.canBeChested(itemInHand.material())) {
            player.sendMessage("<c>You cannot store this item in the chest!");
            event.setCancelled(true);
            return;
        }

        // Handle Ender Chest
        if (block.material() == Material.ENDER_CHEST) {
            event.setCancelled(true);
            try {
                Map<Integer, ItemStack> playerEnderChest = game.getEnderChests().computeIfAbsent(player.getUuid(), k -> new ConcurrentHashMap<>());
                boolean itemAdded = false;
                for (int i = 0; i < 27; i++) { // Standard ender chest size
                    if (playerEnderChest.get(i) == null || playerEnderChest.get(i).isAir()) {
                        playerEnderChest.put(i, itemInHand);
                        itemAdded = true;
                        break;
                    }
                }

                if (itemAdded) {
                    player.setItemInMainHand(ItemStack.AIR);
                    player.sendMessage(depositMessage(itemInHand, true));
                    if (player.allowsPersistentProgress())
                        player.getAchievementHandler().addProgressByTrigger("bedwars.chest_deposit", 1);
                } else {
                    player.sendMessage("<c>Your ender chest is full!");
                }
            } catch (Exception e) {
                Logger.error("Failed to add item to ender chest for player {}: {}", player.getUsername(), e.getMessage());
                e.printStackTrace();
                player.sendMessage("<c>Failed to add item to ender chest!");
            }
            return;
        }

        if (block.material() == Material.CHEST) {
            event.setCancelled(true);

            TeamKey playerTeamName = player.getTeamKey();
            if (playerTeamName == null) {
                return;
            }

            TeamKey chestTeamName = null;
            double closestDistance = Double.MAX_VALUE;

            for (Map.Entry<TeamKey, MapTeam> entry : game.getMapEntry().getConfiguration().getTeams().entrySet()) {
                TeamKey teamKey = entry.getKey();
                MapTeam team = entry.getValue();
                HypixelPosition teamSpawn = team.getSpawn();
                if (teamSpawn == null) continue;

                double distance = Math.sqrt(
                    Math.pow(event.getBlockPosition().x() - teamSpawn.x(), 2) +
                        Math.pow(event.getBlockPosition().y() - teamSpawn.y(), 2) +
                        Math.pow(event.getBlockPosition().z() - teamSpawn.z(), 2)
                );

                if (distance < 10.0 && distance < closestDistance) {
                    closestDistance = distance;
                    chestTeamName = teamKey;
                }
            }

            if (chestTeamName == null) {
                event.setCancelled(false);
                return;
            }

            boolean sameTeam = chestTeamName.equals(playerTeamName);
            if (!sameTeam && game.isBedAlive(chestTeamName)) {
                player.sendMessage("<c>You can only access enemy team chests if their bed is destroyed!");
                return;
            }

            try {
                Map<Integer, ItemStack> teamChest = game.getTeamChests().computeIfAbsent(chestTeamName, k -> new ConcurrentHashMap<>());
                boolean itemAdded = false;
                for (int i = 0; i < 27; i++) {
                    if (teamChest.get(i) == null || teamChest.get(i).isAir()) {
                        teamChest.put(i, itemInHand);
                        itemAdded = true;
                        break;
                    }
                }

                if (itemAdded) {
                    player.sendMessage(depositMessage(itemInHand, false));
                    player.setItemInMainHand(ItemStack.AIR);
                    if (player.allowsPersistentProgress())
                        player.getAchievementHandler().addProgressByTrigger("bedwars.chest_deposit", 1);
                } else {
                    player.sendMessage("<c>The team chest is full!");
                }
            } catch (Exception e) {
                Logger.error("Failed to add item to team chest for team {}: {}", chestTeamName, e);
                e.printStackTrace();
                player.sendMessage("<c>Failed to add item to chest!");
            }
        }
    }

    private Text depositMessage(ItemStack itemStack, boolean enderChest) {
        String chestName = enderChest ? "Ender Chest" : "Team Chest";
        Currency currency = Currency.byMaterial(itemStack.material());
        String itemName = StringUtility.capitalizeSentence(itemStack.material().name().toLowerCase().replace("minecraft:", "").replace("_", " "));
        Text amountText = currency != null
                ? Text.of("<color:{}>x{} {}", currency.getColor(), itemStack.amount(), itemName)
                : Text.of("<f>x{} {}", itemStack.amount(), itemName);
        return Text.of("<7>Deposited {}<7> into <d>{}<7>! <8>(?? Total)", amountText, chestName);
    }

}
