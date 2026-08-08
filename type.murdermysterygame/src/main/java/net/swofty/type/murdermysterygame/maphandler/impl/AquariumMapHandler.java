package net.swofty.type.murdermysterygame.maphandler.impl;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.PotionContents;
import net.minestom.server.potion.PotionType;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.type.murdermysterygame.game.Game;
import net.swofty.type.murdermysterygame.game.GameStatus;
import net.swofty.type.murdermysterygame.maphandler.InteractionType;
import net.swofty.type.murdermysterygame.maphandler.InteractiveElement;
import net.swofty.type.murdermysterygame.maphandler.MapHandler;
import net.swofty.type.murdermysterygame.user.MurderMysteryPlayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class AquariumMapHandler extends MapHandler {
    private boolean bridgeRaising = false;

    @Override
    public String getMapId() {
        return "aquarium";
    }

    @Override
    public List<InteractiveElement> getInteractiveElements() {
        List<InteractiveElement> elements = new ArrayList<>();

        // Bridge Raising Buttons (1 gold each)
        elements.add(InteractiveElement.builder("bridge_raise")
                .type(InteractionType.BUTTON)
                .position(-11, 75, 53)
                .position(-11, 75, 57)
                .position(2, 75, 65)
                .position(-2, 75, 65)
                .position(11, 75, 53)
                .position(11, 75, 57)
                .goldCost(1)
                .action(this::onBridgeRaise)
                .build());

        // Floor Removal Lever (2 gold)
        elements.add(InteractiveElement.builder("floor_removal")
                .type(InteractionType.LEVER)
                .position(-44, 67, 25)
                .goldCost(2)
                .action(this::onFloorRemoval)
                .build());

        // Trading Button (tiered purchasing - handles its own gold validation)
        elements.add(InteractiveElement.builder("trading_button")
                .type(InteractionType.BUTTON)
                .position(-40, 67, 42)
                .goldCost(0) // Custom cost validation in action
                .action(this::onTradingButton)
                .build());

        elements.add(InteractiveElement.builder("trading_button_2")
                .type(InteractionType.BUTTON)
                .position(0, 75, 73)
                .goldCost(0) // Custom cost validation in action
                .action(this::onTradingButton)
                .build());

        return elements;
    }

    private void onBridgeRaise(PlayerBlockInteractEvent event,
                               MurderMysteryPlayer player,
                               Game game,
                               MapHandler handler) {
        if (bridgeRaising) {
            return; // Already in progress
        }
        bridgeRaising = true;

        // Bridge region: from (5, 70, 48) to (-5, 75, 59)
        int minX = Math.min(5, -5);
        int maxX = Math.max(5, -5);
        int minY = Math.min(70, 75);
        int maxY = Math.max(70, 75);
        int minZ = Math.min(48, 59);
        int maxZ = Math.max(48, 59);

        final int maxHeight = 6;

        Map<Point, Block> volumeSnapshot = new HashMap<>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY + maxHeight; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Point pos = new Vec(x, y, z);
                    volumeSnapshot.put(pos, game.getInstanceContainer().getBlock(pos));
                }
            }
        }

        // Identify bridge blocks (non-air in original region)
        Map<Point, Block> bridgeBlocks = new HashMap<>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    Point pos = new Vec(x, y, z);
                    Block block = volumeSnapshot.get(pos);
                    if (block != null && !block.air()) {
                        bridgeBlocks.put(pos, block);
                    }
                }
            }
        }

        AtomicInteger phase = new AtomicInteger(0);
        AtomicInteger height = new AtomicInteger(0);
        AtomicInteger holdCounter = new AtomicInteger(0);

        var task = MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (game.getGameStatus() != GameStatus.IN_PROGRESS) {
                // Restore original state and exit
                restoreVolume(game, volumeSnapshot);
                bridgeRaising = false;
                return;
            }

            int currentPhase = phase.get();

            if (currentPhase == 0) {
                // RAISING PHASE
                int h = height.incrementAndGet();

                // For each bridge block, move it up by 1
                for (Map.Entry<Point, Block> entry : bridgeBlocks.entrySet()) {
                    Point originalPos = entry.getKey();
                    Block block = entry.getValue();

                    // When raising, just set leaving position to AIR (bridge moves up, leaves air behind)
                    Point leavingPos = new Vec(originalPos.x(), originalPos.y() + h - 1, originalPos.z());
                    game.getInstanceContainer().setBlock(leavingPos, Block.AIR);

                    // Place bridge block at new position
                    Point newPos = new Vec(originalPos.x(), originalPos.y() + h, originalPos.z());
                    game.getInstanceContainer().setBlock(newPos, block);
                }

                // Play piston sound for bridge movement
                game.getInstanceContainer().playSound(
                        Sound.sound(Key.key("minecraft:block.piston.extend"), Sound.Source.BLOCK, 1f, 1f),
                        new Pos(0, 72, 54));

                if (h >= maxHeight) {
                    phase.set(1); // Switch to holding
                }
            } else if (currentPhase == 1) {
                if (holdCounter.incrementAndGet() >= 20) {
                    phase.set(2);
                }
            } else if (currentPhase == 2) {
                int h = height.getAndDecrement();

                for (Map.Entry<Point, Block> entry : bridgeBlocks.entrySet()) {
                    Point originalPos = entry.getKey();
                    Block block = entry.getValue();

                    Point leavingPos = new Vec(originalPos.x(), originalPos.y() + h, originalPos.z());
                    Block originalBlock = volumeSnapshot.get(leavingPos);
                    game.getInstanceContainer().setBlock(leavingPos, originalBlock != null ? originalBlock : Block.AIR);

                    Point newPos = new Vec(originalPos.x(), originalPos.y() + h - 1, originalPos.z());
                    game.getInstanceContainer().setBlock(newPos, block);
                }

                // Play piston sound for bridge movement
                game.getInstanceContainer().playSound(
                        Sound.sound(Key.key("minecraft:block.piston.contract"), Sound.Source.BLOCK, 1f, 1f),
                        new Pos(0, 72, 54));

                if (h <= 1) {
                    bridgeRaising = false;
                }
            }
        }).delay(TaskSchedule.millis(500)).repeat(TaskSchedule.millis(500)).schedule();

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            task.cancel();
            if (bridgeRaising) {
                bridgeRaising = false;
            }
        }).delay(TaskSchedule.millis(500 * 35)).schedule();
    }

    private void restoreVolume(Game game, Map<Point, Block> volumeSnapshot) {
        for (Map.Entry<Point, Block> entry : volumeSnapshot.entrySet()) {
            game.getInstanceContainer().setBlock(entry.getKey(), entry.getValue());
        }
    }

    private void onFloorRemoval(PlayerBlockInteractEvent event,
                                MurderMysteryPlayer player,
                                Game game,
                                MapHandler handler) {
        int minX = Math.min(-32, -40);
        int maxX = Math.max(-32, -40);
        int y = 65;
        int minZ = Math.min(27, 19);
        int maxZ = Math.max(27, 19);

        Map<Point, Block> removedBlocks = new HashMap<>();
        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                Point pos = new Vec(x, y, z);
                Block block = game.getInstanceContainer().getBlock(pos);
                if (block == Block.LIGHT_BLUE_STAINED_GLASS) {
                    removedBlocks.put(pos, block);
                    game.getInstanceContainer().setBlock(pos, Block.AIR);
                }
            }
        }

        // Play glass breaking sound
        game.getInstanceContainer().playSound(
                Sound.sound(Key.key("minecraft:block.glass.break"), Sound.Source.BLOCK, 1f, 1f),
                new Pos(-36, 65, 23));

        // Regenerate after 5 seconds
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (game.getGameStatus() == GameStatus.IN_PROGRESS) {
                for (Map.Entry<Point, Block> entry : removedBlocks.entrySet()) {
                    game.getInstanceContainer().setBlock(entry.getKey(), entry.getValue());
                }
                game.getInstanceContainer().playSound(
                        Sound.sound(Key.key("minecraft:block.note_block.bell"), Sound.Source.BLOCK, 1f, 1f),
                        new Pos(-36, 65, 23));
            }
        }).delay(TaskSchedule.seconds(5)).schedule();
    }

    private void onTradingButton(PlayerBlockInteractEvent event,
                                 MurderMysteryPlayer player,
                                 Game game,
                                 MapHandler handler) {
        int playerGold = game.getGoldManager().countGoldInInventory(player);

        // Tier 1: 8 gold - bow + arrow + drinkable speed potion
        if (playerGold >= 8) {
            game.getGoldManager().removeGoldFromInventory(player, 8);
            giveTier1Items(player);
            return;
        }

        // Tier 2: 5 gold - splash slowness + splash weakness (60s protection)
        if (playerGold >= 5) {
            game.getGoldManager().removeGoldFromInventory(player, 5);
            giveTier2Items(player);
            return;
        }

        // Tier 3: 1 gold - splash blindness + splash nightvision
        if (playerGold >= 1) {
            game.getGoldManager().removeGoldFromInventory(player, 1);
            giveTier3Items(player);
            return;
        }

        player.sendMessage(Component.text("This action requires 1 gold", NamedTextColor.RED));
    }

    private void giveTier1Items(MurderMysteryPlayer player) {
        // Bow
        ItemStack bow = ItemStack.builder(Material.BOW)
                .customName(Component.text("Trader's Bow", NamedTextColor.GOLD))
                .build();
        player.getInventory().addItemStack(bow);

        // Arrow
        player.getInventory().addItemStack(ItemStack.of(Material.ARROW, 1));

        // Speed potion (drinkable)
        ItemStack speedPotion = ItemStack.builder(Material.POTION)
                .set(DataComponents.POTION_CONTENTS, new PotionContents(PotionType.SWIFTNESS))
                .build();
        player.getInventory().addItemStack(speedPotion);
    }

    private void giveTier2Items(MurderMysteryPlayer player) {
        // Splash Slowness
        ItemStack slownessPotion = ItemStack.builder(Material.SPLASH_POTION)
                .set(DataComponents.POTION_CONTENTS, new PotionContents(PotionType.SLOWNESS))
                .build();
        player.getInventory().addItemStack(slownessPotion);

        // Splash Weakness (custom name to indicate protection)
        ItemStack weaknessPotion = ItemStack.builder(Material.SPLASH_POTION)
                .set(DataComponents.POTION_CONTENTS, new PotionContents(PotionType.WEAKNESS))
                .customName(Component.text("Protective Weakness", NamedTextColor.LIGHT_PURPLE))
                .build();
        player.getInventory().addItemStack(weaknessPotion);
    }

    private void giveTier3Items(MurderMysteryPlayer player) {
        // Splash Blindness (using mundane with custom name since blindness isn't a standard potion type)
        ItemStack blindnessPotion = ItemStack.builder(Material.SPLASH_POTION)
                .set(DataComponents.POTION_CONTENTS, new PotionContents(PotionType.MUNDANE))
                .customName(Component.text("Blinding Splash", NamedTextColor.DARK_GRAY))
                .build();
        player.getInventory().addItemStack(blindnessPotion);

        // Splash Night Vision
        ItemStack nightVisionPotion = ItemStack.builder(Material.SPLASH_POTION)
                .set(DataComponents.POTION_CONTENTS, new PotionContents(PotionType.NIGHT_VISION))
                .build();
        player.getInventory().addItemStack(nightVisionPotion);
    }
}
