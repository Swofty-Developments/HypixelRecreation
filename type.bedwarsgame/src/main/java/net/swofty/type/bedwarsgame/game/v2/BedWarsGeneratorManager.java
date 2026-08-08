package net.swofty.type.bedwarsgame.game.v2;

import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.item.component.CustomData;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.StringUtility;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig.MapTeam;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig.TeamKey;
import net.swofty.commons.mc.HypixelPosition;
import net.swofty.type.bedwarsgame.TypeBedWarsGameLoader;
import net.swofty.type.bedwarsgame.entity.TextDisplayEntity;
import net.swofty.type.bedwarsgame.item.impl.LuckyBlockItem;
import net.swofty.type.bedwarsgame.item.impl.LuckyBlockTier;
import net.swofty.type.bedwarsgame.shop.TeamUpgradeId;
import net.swofty.type.game.game.GameState;
import net.swofty.type.generic.entity.FloatingBlockEntity;
import org.tinylog.Logger;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BedWarsGeneratorManager {
    private static final int IRON_LIMIT = 48;
    private static final int GOLD_LIMIT = 16;
    private final BedWarsGame game;
    private final Map<TeamKey, List<Task>> teamGeneratorTasks = new EnumMap<>(TeamKey.class);
    private final Map<BedWarsMapsConfig.GlobalGeneratorKey, List<GeneratorDisplay>> generatorDisplays = new HashMap<>();
    private final Map<BedWarsMapsConfig.GlobalGeneratorKey, GeneratorLimits> generatorLimits = new HashMap<>();
    private Task globalTicker;

    public BedWarsGeneratorManager(BedWarsGame game) {
        this.game = game;
    }

    public void startTeamGenerators(Map<TeamKey, MapTeam> activeTeams) {
        BedWarsMapsConfig.MapEntry.MapConfiguration mapConfig = game.getMapEntry().getConfiguration();
        BedWarsMapsConfig.GeneratorSpeed generatorSpeed = mapConfig.getGeneratorSpeed();

        if (generatorSpeed == null) {
            Logger.warn("No generator speed configured for map");
            return;
        }

        activeTeams.forEach((teamKey, team) -> {
            HypixelPosition genLocation = team.getGenerator();
            if (genLocation == null) return;

            Pos spawnPosition = new Pos(genLocation.x(), genLocation.y(), genLocation.z());

            // Start iron generator
            startTeamGenerator(teamKey, BedWarsMapsConfig.GlobalGeneratorKey.IRON, generatorSpeed.getIronAmount(),
                generatorSpeed.getIronDelaySeconds(), spawnPosition);

            // Start gold generator
            startTeamGenerator(teamKey, BedWarsMapsConfig.GlobalGeneratorKey.GOLD, generatorSpeed.getGoldAmount(),
                generatorSpeed.getGoldDelaySeconds(), spawnPosition);
        });
    }

    // material type being string is annoying
    private void startTeamGenerator(TeamKey teamKey, BedWarsMapsConfig.GlobalGeneratorKey materialType,
                                    int baseAmount, int baseDelay, Pos spawnPosition) {
        Material itemMaterial = getMaterialFromType(materialType);
        if (itemMaterial == null) {
            Logger.warn("Invalid material type: {} for team {}", materialType, teamKey.getName());
            return;
        }

        AtomicInteger spawnRound = new AtomicInteger();
        Task task = MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (game.getState() != GameState.IN_PROGRESS) return;

            int forgeLevel = game.getTeamUpgradeLevel(teamKey, TeamUpgradeId.FORGE);
            double multiplier = calculateForgeMultiplier(itemMaterial, forgeLevel);

            int finalAmount = (int) Math.round(baseAmount * multiplier);
            if (finalAmount == 0 && baseAmount > 0 && multiplier > 1.0) finalAmount = 1;

            if (finalAmount > 0) {
                if (game.getGameType().isLuckyBlock()) {
                    LuckyBlockTier tier = switch (spawnRound.getAndIncrement()) {
                        case 4 -> LuckyBlockTier.NORMAL; // not official timing
                        case 5 -> LuckyBlockTier.PROMISING;
                        default -> null;
                    };
                    if (tier == null) return;

                    LuckyBlockItem luckyBlock = (LuckyBlockItem) TypeBedWarsGameLoader.getItemHandler().getItem("lucky_block");
                    spawnItem(luckyBlock.getItemStack(tier), 1, spawnPosition);
                }
                int currentAmount = countNearbyItems(spawnPosition, itemMaterial);
                int limit = itemMaterial == Material.IRON_INGOT ? IRON_LIMIT : GOLD_LIMIT;
                if (currentAmount < limit) {
                    spawnItem(itemMaterial, Math.min(finalAmount, limit - currentAmount), spawnPosition);
                }
            }

            if (spawnRound.get() > 7) spawnRound.set(0); // not official timing
        }).delay(TaskSchedule.seconds(baseDelay)).repeat(TaskSchedule.seconds(baseDelay)).schedule();

        teamGeneratorTasks.computeIfAbsent(teamKey, _ -> new ArrayList<>()).add(task);
    }

    public void startGlobalGenerators() {
        BedWarsMapsConfig.MapEntry.MapConfiguration mapConfig = game.getMapEntry().getConfiguration();
        if (mapConfig.getGlobalGenerator() == null) return;

        mapConfig.getGlobalGenerator().forEach(this::setupGlobalGenerator);

        if (globalTicker != null) globalTicker.cancel();
        globalTicker = MinecraftServer.getSchedulerManager().buildTask(() -> {
            updateGeneratorDisplays();
            tickGlobalGenerators();
        }).delay(TaskSchedule.seconds(1)).repeat(TaskSchedule.seconds(1)).schedule();
    }

    public void recordInitialGeneratorDisplays() {
        if (!game.getReplayManager().isRecording()) return;

        for (Map.Entry<BedWarsMapsConfig.GlobalGeneratorKey, List<GeneratorDisplay>> entry : generatorDisplays.entrySet()) {
            BedWarsMapsConfig.GlobalGeneratorKey generatorType = entry.getKey();
            String tierLabel = getTierLabelFor(generatorType);
            String capitalizedType = StringUtility.capitalize(generatorType.name());

            List<GeneratorDisplay> displays = entry.getValue();
            for (int i = 0; i < displays.size(); i++) {
                GeneratorDisplay display = displays.get(i);
                Pos position = display.spawnDisplay.getPosition();
                List<String> textLines = List.of(
                    "§e" + tierLabel,
                    (generatorType.equals(BedWarsMapsConfig.GlobalGeneratorKey.DIAMOND) ? "§b§l" : "§2§l") + capitalizedType,
                    "§eSpawns in §c" + display.countdown + "§e seconds!"
                );

                game.getReplayManager().recordGeneratorDisplay(
                    display.spawnDisplay.getEntityId(),
                    display.spawnDisplay.getUuid(),
                    position,
                    textLines,
                    "generator",
                    generatorType + "_" + i
                );
            }
        }
    }

    private void setupGlobalGenerator(BedWarsMapsConfig.GlobalGeneratorKey generatorType, BedWarsMapsConfig.MapEntry.MapConfiguration.GlobalGenerator config) {
        Material itemMaterial = getMaterialFromType(generatorType);
        if (itemMaterial == null) {
            Logger.warn("Invalid material for global generator: {}", generatorType);
            return;
        }

        List<HypixelPosition> locations = config.getLocations();
        if (locations == null || locations.isEmpty()) return;

        boolean isDiamond = generatorType.equals(BedWarsMapsConfig.GlobalGeneratorKey.DIAMOND);
        int delaySeconds = isDiamond
            ? game.getGameEventManager().getCurrentPhase().getDiamondSpawnSeconds()
            : game.getGameEventManager().getCurrentPhase().getEmeraldSpawnSeconds();
        int maxAmount = isDiamond
            ? game.getMapEntry().getConfiguration().getGeneratorSpeed().diamondMax
            : game.getMapEntry().getConfiguration().getGeneratorSpeed().emeraldMax;

        setupGlobalGeneratorDisplays(generatorType, locations, delaySeconds);
        generatorLimits.put(generatorType, new GeneratorLimits(
            itemMaterial, 1, maxAmount, locations));
    }

    private void setupGlobalGeneratorDisplays(BedWarsMapsConfig.GlobalGeneratorKey generatorType, List<HypixelPosition> locations, int delaySeconds) {
        boolean isDiamond = generatorType.equals(BedWarsMapsConfig.GlobalGeneratorKey.DIAMOND);
        NamedTextColor color = isDiamond ? NamedTextColor.AQUA : NamedTextColor.DARK_GREEN;
        String capitalizedType = StringUtility.capitalize(generatorType.name());

        for (HypixelPosition location : locations) {
            double locY = location.y() + 5.0;

            TextDisplayEntity tierDisplay = new TextDisplayEntity(
                Component.text("Tier I").color(NamedTextColor.YELLOW));
            tierDisplay.setInstance(game.getInstance(), new Pos(location.x(), locY, location.z()));

            locY -= 0.3;
            TextDisplayEntity titleDisplay = new TextDisplayEntity(
                Component.text(capitalizedType).color(color).decorate(TextDecoration.BOLD));
            titleDisplay.setInstance(game.getInstance(), new Pos(location.x(), locY, location.z()));

            locY -= 0.3;
            TextDisplayEntity spawnDisplay = new TextDisplayEntity(
                MiniMessage.miniMessage().deserialize("<yellow>Spawns in <red>" + delaySeconds + "</red> seconds!</yellow>"));
            spawnDisplay.setInstance(game.getInstance(), new Pos(location.x(), locY, location.z()));

            float size = 0.6f;
            locY -= size + 0.1 + 0.25;
            FloatingBlockEntity blockDisplay = new FloatingBlockEntity(
                getBlockFromType(generatorType),
                size,
                game.getInstance(),
                new Pos(location.x(), locY, location.z())
            );
            blockDisplay.startAnimation();

            generatorDisplays.computeIfAbsent(generatorType, _ -> new ArrayList<>())
                .add(new GeneratorDisplay(tierDisplay, titleDisplay, spawnDisplay, blockDisplay, delaySeconds));

            if (game.getReplayManager().isRecording()) {
                List<String> textLines = List.of(
                    "§eTier I",
                    (isDiamond ? "§b§l" : "§2§l") + capitalizedType,
                    "§eSpawns in §c" + delaySeconds + "§e seconds!"
                );
                game.getReplayManager().recordGeneratorDisplay(
                    spawnDisplay.getEntityId(),
                    spawnDisplay.getUuid(),
                    spawnDisplay.getPosition(),
                    textLines,
                    "generator",
                    generatorType + "_" + locations.indexOf(location)
                );
            }
        }
    }

    private void tickGlobalGenerators() {
        for (Map.Entry<BedWarsMapsConfig.GlobalGeneratorKey, GeneratorLimits> entry : generatorLimits.entrySet()) {
            BedWarsMapsConfig.GlobalGeneratorKey type = entry.getKey();
            GeneratorLimits limits = entry.getValue();
            List<GeneratorDisplay> displays = generatorDisplays.get(type);

            if (displays == null || displays.isEmpty()) continue;

            for (int i = 0; i < limits.locations.size(); i++) {
                HypixelPosition location = limits.locations.get(i);
                GeneratorDisplay display = i < displays.size() ? displays.get(i) : null;

                if (display == null) continue;

                display.countdown--;
                if (display.countdown <= 0) {
                    Pos spawnPos = new Pos(location.x(), location.y() + 1, location.z());
                    long currentItemCount = game.getInstance().getNearbyEntities(spawnPos, 1.5)
                        .stream()
                        .filter(ItemEntity.class::isInstance)
                        .map(ItemEntity.class::cast)
                        .filter(entity -> entity.getItemStack().material() == limits.material)
                        .mapToLong(entity -> entity.getItemStack().amount())
                        .sum();

                    if (game.getGameType().isLuckyBlock()) {
                        display.round++;

                        if (display.round % 4 != 0) continue; // not official timing

                        boolean isDiamond = type.equals(BedWarsMapsConfig.GlobalGeneratorKey.DIAMOND);
                        boolean isEmerald = type.equals(BedWarsMapsConfig.GlobalGeneratorKey.EMERALD);

                        LuckyBlockItem luckyBlock = (LuckyBlockItem) TypeBedWarsGameLoader.getItemHandler().getItem("lucky_block");
                        if (isDiamond) {
                            spawnItem(luckyBlock.getItemStack(LuckyBlockTier.FORTUNATE), 1, spawnPos);
                            spawnItem(luckyBlock.getItemStack(LuckyBlockTier.OFFENSIVE), 1, spawnPos);
                        }
                        if (isEmerald) {
                            spawnItem(luckyBlock.getItemStack(LuckyBlockTier.MIRACLE), 1, spawnPos);
                        }
                    }

                    if (currentItemCount < limits.maxAmount) {
                        spawnItem(limits.material, limits.amount, spawnPos);
                    }

                    display.countdown = display.maxCountdown;
                }
            }
        }
    }

    private void updateGeneratorDisplays() {
        for (List<GeneratorDisplay> displays : generatorDisplays.values()) {
            for (GeneratorDisplay display : displays) {
                String newText = "§eSpawns in §c" + display.countdown + "§e seconds!";
                display.spawnDisplay.setText(MiniMessage.miniMessage().deserialize(
                    "<yellow>Spawns in <red>" + display.countdown + "</red> seconds!</yellow>"));

                // Record display update for replay
                if (game.getReplayManager().isRecording()) {
                    game.getReplayManager().recordTextDisplayUpdate(
                        display.spawnDisplay.getEntityId(),
                        List.of(newText),
                        false,
                        2 // Update third line (spawn timer)
                    );
                }
            }
        }
    }

    public void updateDisplaysForEventChange() {
        for (Map.Entry<BedWarsMapsConfig.GlobalGeneratorKey, List<GeneratorDisplay>> e : generatorDisplays.entrySet()) {
            int max = e.getKey().equals(BedWarsMapsConfig.GlobalGeneratorKey.DIAMOND)
                ? game.getGameEventManager().getDiamondSpawnSeconds()
                : game.getGameEventManager().getEmeraldSpawnSeconds();

            String tierLabel = getTierLabelFor(e.getKey());

            for (GeneratorDisplay d : e.getValue()) {
                d.maxCountdown = max;
                d.countdown = max;
                d.tierDisplay.setText(Component.text(tierLabel).color(NamedTextColor.YELLOW));
            }
        }
    }

    private String getTierLabelFor(BedWarsMapsConfig.GlobalGeneratorKey type) {
        BedWarsGameEventManager.GamePhase phase = game.getGameEventManager().getCurrentPhase();
        int tier = 1;

        if (type.equals(BedWarsMapsConfig.GlobalGeneratorKey.DIAMOND)) {
            if (phase.ordinal() >= BedWarsGameEventManager.GamePhase.DIAMOND_II.ordinal()) tier = 2;
            if (phase.ordinal() >= BedWarsGameEventManager.GamePhase.DIAMOND_III.ordinal()) tier = 3;
        } else if (type.equals(BedWarsMapsConfig.GlobalGeneratorKey.EMERALD)) {
            if (phase.ordinal() >= BedWarsGameEventManager.GamePhase.EMERALD_II.ordinal()) tier = 2;
            if (phase.ordinal() >= BedWarsGameEventManager.GamePhase.EMERALD_III.ordinal()) tier = 3;
        }

        return switch (tier) {
            case 2 -> "Tier II";
            case 3 -> "Tier III";
            default -> "Tier I";
        };
    }

    public void stopAllGenerators() {
        teamGeneratorTasks.values().stream().flatMap(List::stream).forEach(Task::cancel);
        teamGeneratorTasks.clear();

        // Remove all display entities
        for (List<GeneratorDisplay> displays : generatorDisplays.values()) {
            for (GeneratorDisplay display : displays) {
                display.tierDisplay.remove();
                display.titleDisplay.remove();
                display.spawnDisplay.remove();
                display.blockDisplay.remove();
            }
        }
        generatorDisplays.clear();
        generatorLimits.clear();

        if (globalTicker != null) {
            globalTicker.cancel();
            globalTicker = null;
        }
    }

    public void dropItemsAtTeamGenerator(TeamKey teamKey, Collection<ItemStack> items) {
        if (game.getState() != GameState.IN_PROGRESS || teamKey == null || items.isEmpty()) return;

        MapTeam team = game.getMapEntry().getConfiguration().getTeams().get(teamKey);
        if (team == null || team.getGenerator() == null) return;

        HypixelPosition generator = team.getGenerator();
        Pos position = new Pos(generator.x(), generator.y(), generator.z());
        for (ItemStack item : items) {
            if (item == null || item.isAir()) continue;

            ItemEntity entity = new ItemEntity(item);
            entity.setPickupDelay(Duration.ofMillis(500));
            entity.setInstance(game.getInstance(), position);
        }
    }

    private void spawnItem(Material material, int amount, Pos position) {
        spawnItem(ItemStack.of(material), amount, position);
    }

    private void spawnItem(ItemStack stack, int amount, Pos position) {
        ItemStack item = stack.withAmount(amount);
        if (item.material() == Material.IRON_INGOT || item.material() == Material.GOLD_INGOT
                || item.material() == Material.DIAMOND || item.material() == Material.EMERALD) {
            item = item.with(DataComponents.CUSTOM_DATA,
                    new CustomData(CompoundBinaryTag.builder().putBoolean("generator", true).build()));
        }
        ItemEntity entity = new ItemEntity(item);
        entity.setPickupDelay(Duration.ofMillis(500));
        entity.setInstance(game.getInstance(), position);
        entity.setVelocity(new Vec(0, 0.1, 0));
    }

    private int countNearbyItems(Pos position, Material material) {
        return game.getInstance().getNearbyEntities(position, 1.5).stream()
                .filter(ItemEntity.class::isInstance)
                .map(ItemEntity.class::cast)
                .filter(entity -> entity.getItemStack().material() == material)
                .mapToInt(entity -> entity.getItemStack().amount())
                .sum();
    }

    private Material getMaterialFromType(BedWarsMapsConfig.GlobalGeneratorKey type) {
        return switch (type) {
            case IRON -> Material.IRON_INGOT;
            case GOLD -> Material.GOLD_INGOT;
            case DIAMOND -> Material.DIAMOND;
            case EMERALD -> Material.EMERALD;
        };
    }

    private Block getBlockFromType(BedWarsMapsConfig.GlobalGeneratorKey type) {
        return switch (type) {
            case IRON -> Block.IRON_BLOCK;
            case GOLD -> Block.GOLD_BLOCK;
            case DIAMOND -> Block.DIAMOND_BLOCK;
            case EMERALD -> Block.EMERALD_BLOCK;
        };
    }

    private double calculateForgeMultiplier(Material material, int forgeLevel) {
        if (material != Material.IRON_INGOT && material != Material.GOLD_INGOT) return 1.0;

        return switch (forgeLevel) {
            case 1 -> 1.5;
            case 2 -> 2.0;
            case 3, 4 -> 3.0;
            default -> 1.0;
        };
    }

    public void addTeamGeneratorTask(TeamKey teamKey, Task task) {
        teamGeneratorTasks.computeIfAbsent(teamKey, _ -> new ArrayList<>()).add(task);
    }

    private static class GeneratorDisplay {
        private final TextDisplayEntity tierDisplay;
        private final TextDisplayEntity titleDisplay;
        private final TextDisplayEntity spawnDisplay;
        private final FloatingBlockEntity blockDisplay;
        private int maxCountdown;
        private int countdown;
        private int round = 0;

        public GeneratorDisplay(TextDisplayEntity tierDisplay, TextDisplayEntity titleDisplay,
                                TextDisplayEntity spawnDisplay, FloatingBlockEntity blockDisplay, int delay) {
            this.tierDisplay = tierDisplay;
            this.titleDisplay = titleDisplay;
            this.spawnDisplay = spawnDisplay;
            this.blockDisplay = blockDisplay;
            this.maxCountdown = delay;
            this.countdown = delay;
        }
    }

    private record GeneratorLimits(Material material, int amount, int maxAmount,
                                   List<HypixelPosition> locations) {
    }
}
