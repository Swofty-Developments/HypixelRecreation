package net.swofty.type.skywarsgame.game;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.title.Title;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.ServerType;
import net.swofty.commons.mc.HypixelPosition;
import net.swofty.commons.skywars.SkywarsGameType;
import net.swofty.commons.skywars.SkywarsLeaderboardMode;
import net.swofty.commons.skywars.SkywarsModeStats;
import net.swofty.commons.skywars.map.SkywarsMapsConfig;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.datapoints.DatapointLong;
import net.swofty.type.generic.data.datapoints.DatapointSkywarsKitStats;
import net.swofty.type.generic.data.datapoints.DatapointSkywarsModeStats;
import net.swofty.type.generic.data.datapoints.DatapointSkywarsUnlocks;
import net.swofty.type.generic.data.handlers.SkywarsDataHandler;
import net.swofty.type.generic.game.GameStatTracker;
import net.swofty.type.generic.utility.Titles;
import net.swofty.type.skywarsgame.TypeSkywarsGameLoader;
import net.swofty.type.skywarsgame.luckyblock.LuckyBlock;
import net.swofty.type.skywarsgame.luckyblock.oprule.OPRuleManager;
import net.swofty.type.skywarsgame.manager.CageManager;
import net.swofty.type.skywarsgame.manager.ChestManager;
import net.swofty.type.skywarsgame.manager.DragonManager;
import net.swofty.type.skywarsgame.perk.SkywarsPerkHandler;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;
import net.swofty.type.skywarsgame.util.ChestScanner;
import net.swofty.type.skywarslobby.kit.SkywarsKit;
import net.swofty.type.skywarslobby.kit.SkywarsKitRegistry;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Getter
public class SkywarsGame {
    public static final Tag<Boolean> ELIMINATED_TAG = Tag.Boolean("eliminated");

    public static final int FIRST_REFILL_SECONDS = 180;
    public static final int SECOND_REFILL_SECONDS = 360;
    public static final int DRAGON_SPAWN_SECONDS = 600;


    private final InstanceContainer instanceContainer;
    private final SkywarsGameType gameType;
    private final String gameId = UUID.randomUUID().toString();
    private final SkywarsMapsConfig.MapEntry mapEntry;

    private final List<SkywarsPlayer> players = new ArrayList<>();
    private final List<UUID> disconnectedPlayerUuids = new ArrayList<>();
    private final Map<Integer, List<SkywarsPlayer>> teams = new HashMap<>();
    private final Map<UUID, Long> boundaryWarningStartTime = new HashMap<>();

    private static final int BOUNDARY_WARNING_SECONDS = 5;

    private final CageManager cageManager;
    private final ChestManager chestManager;
    private final DragonManager dragonManager;
    private final LuckyBlock luckyBlockManager;
    private final OPRuleManager opRuleManager;
    private final SkywarsGameCountdown countdown;
    private final GameStatTracker<SkywarsGameStat> gameStats = new GameStatTracker<>(SkywarsGameStat.class);

    @Setter
    private SkywarsGameStatus gameStatus;
    private long gameStartTime = 0;
    private GameEvent currentEvent = GameEvent.GAME_START;

    public SkywarsGame(SkywarsMapsConfig.MapEntry mapEntry,
                       InstanceContainer instanceContainer,
                       SkywarsGameType gameType) {
        this.mapEntry = mapEntry;
        this.instanceContainer = instanceContainer;
        this.gameType = gameType;

        SkywarsMapsConfig.MapEntry.MapConfiguration config = mapEntry.getConfiguration();

        List<Pos> cagePositions = config.getIslands().stream()
                .map(island -> {
                    HypixelPosition cage = island.getCageCenter();
                    return new Pos(cage.x(), cage.y(), cage.z(), cage.yaw(), cage.pitch());
                })
                .toList();

        HypixelPosition center = config.getCenter();
        Pos centerPos = new Pos(center.x(), center.y(), center.z());

        ChestScanner.ChestScanResult scanResult = ChestScanner.scanForChests(
                instanceContainer,
                config.getBounds(),
                cagePositions,
                config.getVoidY()
        );
        List<Pos> islandChests = scanResult.islandChests();
        List<Pos> centerChests = scanResult.centerChests();

        this.cageManager = new CageManager(instanceContainer, cagePositions);
        this.chestManager = new ChestManager(instanceContainer, gameType, islandChests, centerChests);
        this.dragonManager = new DragonManager(this, instanceContainer, centerPos);

        if (gameType == SkywarsGameType.SOLO_LUCKY_BLOCK) {
            this.luckyBlockManager = new LuckyBlock(instanceContainer);
            this.luckyBlockManager.setGame(this);
            this.opRuleManager = new OPRuleManager(this);
        } else {
            this.luckyBlockManager = null;
            this.opRuleManager = null;
        }

        this.countdown = new SkywarsGameCountdown(this);

        this.gameStatus = SkywarsGameStatus.WAITING;
    }

    public void join(SkywarsPlayer player) {
        if (gameStatus != SkywarsGameStatus.WAITING) {
            player.sendMessage("<c>Game already in progress!");
            player.sendTo(ServerType.SKYWARS_LOBBY);
            return;
        }

        if (players.size() >= gameType.getMaxPlayers()) {
            player.sendMessage("<c>Game is full!");
            player.sendTo(ServerType.SKYWARS_LOBBY);
            return;
        }

        setupPlayerForWaiting(player);
        players.add(player);
        assignToTeam(player);
        player.setTag(Tag.String("gameId"), gameId);

        broadcastMessage(Text.of("{}<e> has joined (<b>{}<e>/<b>{}<e>)!",
                player.getFullDisplayName(), players.size(), gameType.getMaxPlayers()));

        if (hasMinimumPlayers() && !countdown.isActive()) {
            countdown.startCountdown();
        }
    }

    public void leave(SkywarsPlayer player) {
        if (gameStatus == SkywarsGameStatus.IN_PROGRESS && !player.isEliminated()) {
            dropPlayerItems(player);

            player.setEliminated(true);
            player.setTag(ELIMINATED_TAG, true);
            broadcastMessage(EnvironmentalDeathType.QUIT.formatMessage(player));
        } else if (gameStatus == SkywarsGameStatus.WAITING || gameStatus == SkywarsGameStatus.STARTING) {
            broadcastMessage(Text.of("{}<e> has quit!", player.getFullDisplayName()));
        }

        players.remove(player);
        removeFromTeam(player);
        player.removeTag(Tag.String("gameId"));
        cageManager.releaseCage(player);
        player.sendTo(ServerType.SKYWARS_LOBBY);

        countdown.checkCountdownConditions();

        if (gameStatus == SkywarsGameStatus.IN_PROGRESS) {
            checkWinConditions();
        }
    }

    public void disconnect(SkywarsPlayer player) {
        if (gameStatus == SkywarsGameStatus.IN_PROGRESS && !player.isEliminated()) {
            dropPlayerItems(player);

            player.setEliminated(true);
            player.setTag(ELIMINATED_TAG, true);
            broadcastMessage(EnvironmentalDeathType.QUIT.formatMessage(player));
        }

        disconnectedPlayerUuids.add(player.getUuid());
        players.remove(player);
        removeFromTeam(player);
        cageManager.releaseCage(player);

        if (gameStatus == SkywarsGameStatus.IN_PROGRESS) {
            checkWinConditions();
        }
    }

    private void assignToTeam(SkywarsPlayer player) {
        if (gameType.getTeamSize() == 1) {
            teams.put(players.size() - 1, new ArrayList<>(List.of(player)));
        } else {
            for (int i = 0; i < gameType.getMaxTeams(); i++) {
                List<SkywarsPlayer> team = teams.computeIfAbsent(i, k -> new ArrayList<>());
                if (team.size() < gameType.getTeamSize()) {
                    team.add(player);
                    return;
                }
            }
        }
    }

    private void removeFromTeam(SkywarsPlayer player) {
        for (List<SkywarsPlayer> team : teams.values()) {
            team.remove(player);
        }
    }

    private void dropPlayerItems(SkywarsPlayer player) {
        Pos dropPos = player.getPosition();

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItemStack(i);
            if (!item.isAir()) {
                ItemEntity itemEntity = new ItemEntity(item);
                itemEntity.setInstance(instanceContainer, dropPos.add(0, 1, 0));
                itemEntity.setPickupDelay(Duration.ofMillis(500));
                itemEntity.setVelocity(itemEntity.getVelocity().add(
                        (Math.random() - 0.5) * 5,
                        3,
                        (Math.random() - 0.5) * 5
                ));
            }
        }

        net.minestom.server.entity.EquipmentSlot[] armorSlots = {
                net.minestom.server.entity.EquipmentSlot.HELMET,
                net.minestom.server.entity.EquipmentSlot.CHESTPLATE,
                net.minestom.server.entity.EquipmentSlot.LEGGINGS,
                net.minestom.server.entity.EquipmentSlot.BOOTS
        };
        for (net.minestom.server.entity.EquipmentSlot slot : armorSlots) {
            ItemStack armor = player.getEquipment(slot);
            if (armor != null && !armor.isAir()) {
                ItemEntity itemEntity = new ItemEntity(armor);
                itemEntity.setInstance(instanceContainer, dropPos.add(0, 1, 0));
                itemEntity.setPickupDelay(Duration.ofMillis(500));
                itemEntity.setVelocity(itemEntity.getVelocity().add(
                        (Math.random() - 0.5) * 5,
                        3,
                        (Math.random() - 0.5) * 5
                ));
            }
        }

        player.getInventory().clear();
    }

    public int getPlayerTeam(SkywarsPlayer player) {
        for (Map.Entry<Integer, List<SkywarsPlayer>> entry : teams.entrySet()) {
            if (entry.getValue().contains(player)) {
                return entry.getKey();
            }
        }
        return -1;
    }

    private void setupPlayerForWaiting(SkywarsPlayer player) {
        Pos cagePos = cageManager.assignCage(player);

        if (player.getInstance() == null || !player.getInstance().getUuid().equals(instanceContainer.getUuid())) {
            player.setInstance(instanceContainer, cagePos);
        } else {
            player.teleport(cagePos);
        }

        player.getInventory().clear();
        if (gameType != SkywarsGameType.SOLO_LUCKY_BLOCK) {
            player.getInventory().setItemStack(0,
                    TypeSkywarsGameLoader.getItemHandler().getItem("kit_selector").getItemStack());
        }
        player.getInventory().setItemStack(8,
                TypeSkywarsGameLoader.getItemHandler().getItem("leave_game").getItemStack());
        player.setFlying(false);
        player.setGameMode(GameMode.ADVENTURE);
        player.resetGameState();

        if (gameType == SkywarsGameType.SOLO_LUCKY_BLOCK) {
            player.sendActionBar(Text.of("<c>Kits and perks are disabled in Lucky Block SkyWars"));
        }
    }

    private static final Text THICK_BAR = Text.of("<a><l>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

    public void startGame() {
        gameStatus = SkywarsGameStatus.IN_PROGRESS;
        gameStartTime = System.currentTimeMillis();

        cageManager.openAllCages();

        for (SkywarsPlayer player : players) {
            player.setGameMode(GameMode.SURVIVAL);
            player.getInventory().clear();
            if (gameType != SkywarsGameType.SOLO_LUCKY_BLOCK) {
                giveKitItems(player);
                SkywarsPerkHandler.applyPerkEffects(player, this);
            }
        }

        sendGameIntroMessage();

        broadcastMessage(Text.of("<e>Cages opened! <c>FIGHT!"));

        Title title = Titles.title(
                Text.of("<c>FIGHT!"),
                Text.empty(),
                Title.Times.times(Duration.ZERO, Duration.ofSeconds(2), Duration.ofMillis(500))
        );
        players.forEach(p -> p.showTitle(title));

        chestManager.scheduleRefills(
                () -> broadcastMessage(Text.of("<6>Chests have been refilled!")),
                () -> broadcastMessage(Text.of("<6>Chests have been refilled for the last time!"))
        );

        dragonManager.scheduleDragonSpawn(this::broadcastMessage);

        MinecraftServer.getSchedulerManager().buildTask(this::checkPlayerBoundaries)
                .repeat(TaskSchedule.tick(1))
                .schedule();
    }

    private void sendGameIntroMessage() {
        for (SkywarsPlayer player : players) {
            player.sendMessage(THICK_BAR);
            player.sendMessage("                         <f><l>SkyWars");
            player.sendMessage("");
            player.sendMessage("       <e><l>Gather resources and equipment on your");
            player.sendMessage("    <e><l>island in order to eliminate every other player.");
            player.sendMessage("       <e><l>Go to the center island for special chests");
            player.sendMessage("                   <e><l>with special items!");
            player.sendMessage("");
            player.sendMessage(THICK_BAR);
        }
    }

    private void checkPlayerBoundaries() {
        if (gameStatus != SkywarsGameStatus.IN_PROGRESS) return;

        SkywarsMapsConfig.MapEntry.MapConfiguration config = mapEntry.getConfiguration();
        int voidY = config.getVoidY();
        SkywarsMapsConfig.MapBounds bounds = config.getBounds();

        for (SkywarsPlayer player : new ArrayList<>(players)) {
            if (player.isEliminated()) continue;

            double playerX = player.getPosition().x();
            double playerY = player.getPosition().y();
            double playerZ = player.getPosition().z();

            if (playerY < voidY) {
                SkywarsPlayer lastDamager = getPlayerByUuid(player.getLastDamager());
                if (lastDamager != null && !lastDamager.isEliminated()) {
                    onPlayerKill(lastDamager, player, KillType.VOID);
                } else {
                    onEnvironmentalDeath(player, EnvironmentalDeathType.VOID);
                }
                boundaryWarningStartTime.remove(player.getUuid());
                continue;
            }

            if (bounds != null && !bounds.isWithinBounds(playerX, playerY, playerZ)) {
                if (!boundaryWarningStartTime.containsKey(player.getUuid())) {
                    boundaryWarningStartTime.put(player.getUuid(), System.currentTimeMillis());
                    player.sendMessage("<c>You are outside the border! Return within {} seconds!", BOUNDARY_WARNING_SECONDS);
                } else {
                    long warningStart = boundaryWarningStartTime.get(player.getUuid());
                    long elapsed = (System.currentTimeMillis() - warningStart) / 1000;

                    if (elapsed >= BOUNDARY_WARNING_SECONDS) {
                        onEnvironmentalDeath(player, EnvironmentalDeathType.VOID);
                        boundaryWarningStartTime.remove(player.getUuid());
                    } else {
                        int remaining = BOUNDARY_WARNING_SECONDS - (int) elapsed;
                        player.sendActionBar(Text.of("<c>Return to border: {}s", remaining));
                    }
                }
            } else {
                boundaryWarningStartTime.remove(player.getUuid());
            }
        }
    }

    private void giveKitItems(SkywarsPlayer player) {
        player.getInventory().clear();

        SkywarsDataHandler handler = SkywarsDataHandler.getUser(player);
        if (handler == null) return;

        DatapointSkywarsUnlocks unlocksDP = handler.get(
                SkywarsDataHandler.Data.UNLOCKS,
                DatapointSkywarsUnlocks.class);
        DatapointSkywarsUnlocks.SkywarsUnlocks unlocks = unlocksDP.getValue();

        String kitId = unlocks.getSelectedKitForMode(gameType.getModeString());

        SkywarsKit kit = SkywarsKitRegistry.getKit(kitId);
        if (kit == null) {
            kit = SkywarsKitRegistry.getDefaultKits().stream().findFirst().orElse(null);
        }

        if (kit != null) {
            for (net.minestom.server.item.ItemStack item : kit.getStartingItems(gameType.getModeString())) {
                player.getInventory().addItemStack(item);
            }
            player.setSelectedKit(kitId);
        }
    }

    public void onPlayerKill(SkywarsPlayer killer, SkywarsPlayer victim, KillType killType) {
        gameStats.increment(killer.getUuid(), SkywarsGameStat.KILLS);
        gameStats.increment(killer.getUuid(), SkywarsGameStat.SOULS);

        SkywarsPerkHandler.applyKillEffects(killer, victim, this);
        if (killType == KillType.VOID) {
            SkywarsPerkHandler.applyVoidKillEffects(killer, victim, this);
        }

        UUID assistDamager = victim.getAssistDamager();
        if (assistDamager != null && !assistDamager.equals(killer.getUuid())) {
            SkywarsPlayer assistant = getPlayerByUuid(assistDamager);
            if (assistant != null && !assistant.isEliminated()) {
                gameStats.increment(assistant.getUuid(), SkywarsGameStat.ASSISTS);
                assistant.sendMessage("<e>+1 Assist!");
                recordAssistStats(assistant);
            }
        }

        recordKillStats(killer, victim, killType);
        recordDeathStats(victim);

        victim.setEliminated(true);
        victim.setTag(ELIMINATED_TAG, true);
        victim.setupForSpectator();

        broadcastMessage(killType.formatMessage(victim, killer));

        checkWinConditions();
    }

    public void onEnvironmentalDeath(SkywarsPlayer victim, EnvironmentalDeathType deathType) {
        recordDeathStats(victim);

        victim.setEliminated(true);
        victim.setTag(ELIMINATED_TAG, true);
        victim.setupForSpectator();

        broadcastMessage(deathType.formatMessage(victim));

        checkWinConditions();
    }

    private void recordKillStats(SkywarsPlayer killer, SkywarsPlayer victim, KillType killType) {
        SkywarsDataHandler handler = SkywarsDataHandler.getUser(killer);
        if (handler == null) return;

        SkywarsLeaderboardMode mode = SkywarsLeaderboardMode.fromGameType(gameType);

        DatapointSkywarsModeStats statsDP = handler.get(
                SkywarsDataHandler.Data.MODE_STATS,
                DatapointSkywarsModeStats.class);
        SkywarsModeStats stats = statsDP.getValue();

        stats.recordKill(mode);

        switch (killType) {
            case MELEE -> stats.recordMeleeKill(mode);
            case BOW -> stats.recordBowKill(mode);
            case VOID -> stats.recordVoidKill(mode);
        }

        DatapointSkywarsKitStats kitStatsDP = handler.get(
                SkywarsDataHandler.Data.KIT_STATS,
                DatapointSkywarsKitStats.class);
        DatapointSkywarsKitStats.SkywarsKitStats kitStats = kitStatsDP.getValue();
        DatapointSkywarsKitStats.KitStatistics currentKitStats = kitStats.getStatsForKit(killer.getSelectedKit());

        switch (killType) {
            case MELEE -> currentKitStats.addMeleeKill();
            case BOW -> {
                currentKitStats.addBowKill();
                int distance = (int) killer.getPosition().distance(victim.getPosition());
                currentKitStats.setLongestBowKill(distance);
            }
            case VOID -> currentKitStats.addVoidKill();
            case FALL -> currentKitStats.addKill();
        }
    }

    private void recordDeathStats(SkywarsPlayer victim) {
        SkywarsDataHandler handler = SkywarsDataHandler.getUser(victim);
        if (handler == null) return;

        SkywarsLeaderboardMode mode = SkywarsLeaderboardMode.fromGameType(gameType);
        DatapointSkywarsModeStats statsDP = handler.get(
                SkywarsDataHandler.Data.MODE_STATS,
                DatapointSkywarsModeStats.class);
        SkywarsModeStats stats = statsDP.getValue();
        stats.recordDeath(mode);
    }

    private void recordAssistStats(SkywarsPlayer assistant) {
        SkywarsDataHandler handler = SkywarsDataHandler.getUser(assistant);
        if (handler == null) return;

        SkywarsLeaderboardMode mode = SkywarsLeaderboardMode.fromGameType(gameType);
        DatapointSkywarsModeStats statsDP = handler.get(
                SkywarsDataHandler.Data.MODE_STATS,
                DatapointSkywarsModeStats.class);
        SkywarsModeStats stats = statsDP.getValue();
        stats.recordAssist(mode);

        DatapointSkywarsKitStats kitStatsDP = handler.get(
                SkywarsDataHandler.Data.KIT_STATS,
                DatapointSkywarsKitStats.class);
        DatapointSkywarsKitStats.SkywarsKitStats kitStats = kitStatsDP.getValue();
        kitStats.getStatsForKit(assistant.getSelectedKit()).addAssist();
    }

    public void recordChestOpened(SkywarsPlayer player) {
        SkywarsDataHandler handler = SkywarsDataHandler.getUser(player);
        if (handler == null) return;

        SkywarsLeaderboardMode mode = SkywarsLeaderboardMode.fromGameType(gameType);
        handler.get(SkywarsDataHandler.Data.MODE_STATS, DatapointSkywarsModeStats.class)
                .getValue()
                .recordChestOpened(mode);
        handler.get(SkywarsDataHandler.Data.KIT_STATS, DatapointSkywarsKitStats.class)
                .getValue()
                .getStatsForKit(player.getSelectedKit())
                .addChestOpened();
    }

    public void checkWinConditions() {
        if (gameStatus != SkywarsGameStatus.IN_PROGRESS) return;

        if (gameType.getTeamSize() == 1) {
            int alivePlayers = countAlivePlayers();
            if (alivePlayers <= 1) {
                endGame(SkywarsWinCondition.LAST_PLAYER_STANDING);
            }
        } else {
            int aliveTeams = countAliveTeams();
            if (aliveTeams <= 1) {
                endGame(SkywarsWinCondition.LAST_TEAM_STANDING);
            }
        }
    }

    private int countAliveTeams() {
        int aliveTeams = 0;
        for (List<SkywarsPlayer> team : teams.values()) {
            boolean teamAlive = team.stream().anyMatch(p -> !p.isEliminated());
            if (teamAlive) aliveTeams++;
        }
        return aliveTeams;
    }

    public void onDragonKilled(UUID killerUuid) {
        SkywarsPlayer killer = getPlayerByUuid(killerUuid);
        if (killer != null) {
            broadcastMessage(Text.of("<d>{} has slain the Ender Dragon!", killer.getUsername()));
        }
        endGame(SkywarsWinCondition.DRAGON_DEATH);
    }

    private void endGame(SkywarsWinCondition condition) {
        gameStatus = SkywarsGameStatus.ENDING;
        dragonManager.cleanup();

        SkywarsPlayer winner = getLastStandingPlayer();

        recordGameStats(winner);
        sendGameResults(winner);

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            List<SkywarsPlayer> playersToRemove = new ArrayList<>(players);
            for (SkywarsPlayer player : playersToRemove) {
                leave(player);
            }
            players.clear();
            waitForEmptyThenDestroy();
        }).delay(TaskSchedule.seconds(10)).schedule();
    }

    private void sendGameResults(SkywarsPlayer winner) {
        for (SkywarsPlayer player : players) {
            player.sendMessage(THICK_BAR);
            player.sendMessage("                         <f><l>SkyWars");

            if (winner != null) {
                player.sendMessage("");
                player.sendMessage(" <7>Winner: {} <7>- <6>{} kills",
                        winner.getFullDisplayName(), gameStats.get(winner.getUuid(), SkywarsGameStat.KILLS));
            } else {
                player.sendMessage("                  <f><l>Winner: </l><7>None");
                player.sendMessage("");
            }

            player.sendMessage("");
            player.sendMessage(" <7>Your Stats:");
            player.sendMessage("   <7>Kills: <a>{}", gameStats.get(player.getUuid(), SkywarsGameStat.KILLS));
            player.sendMessage("   <7>Assists: <e>{}", gameStats.get(player.getUuid(), SkywarsGameStat.ASSISTS));

            player.sendMessage(THICK_BAR);

            int coinsEarned = calculateCoinsEarned(player, winner);
            long expEarned = 150 + (gameStats.get(player.getUuid(), SkywarsGameStat.KILLS) * 25);

            player.sendMessage("                 <f><l>Reward Summary");
            player.sendMessage("   <7>You earned:");
            player.sendMessage("   <6>+{} coins", coinsEarned);
            player.sendMessage("   <a>+{} souls", gameStats.get(player.getUuid(), SkywarsGameStat.SOULS));
            player.sendMessage("   <b>+{} Hypixel Experience", expEarned);

            player.sendMessage(THICK_BAR);

            net.swofty.type.generic.experience.PlayerExperienceHandler expHandler =
                    new net.swofty.type.generic.experience.PlayerExperienceHandler(player);
            expHandler.addExperience(expEarned);
        }
    }

    private int calculateCoinsEarned(SkywarsPlayer player, SkywarsPlayer winner) {
        int coins = 10;
        coins += Math.toIntExact(gameStats.get(player.getUuid(), SkywarsGameStat.KILLS) * 5);
        coins += Math.toIntExact(gameStats.get(player.getUuid(), SkywarsGameStat.ASSISTS) * 2);
        if (winner != null && winner.getUuid().equals(player.getUuid())) {
            coins += 25;
        }
        return coins;
    }

    private void recordGameStats(SkywarsPlayer winner) {
        SkywarsLeaderboardMode mode = SkywarsLeaderboardMode.fromGameType(gameType);
        long gameDurationSeconds = (System.currentTimeMillis() - gameStartTime) / 1000;

        for (SkywarsPlayer player : players) {
            SkywarsDataHandler handler = SkywarsDataHandler.getUser(player);
            if (handler == null) continue;

            DatapointSkywarsModeStats statsDP = handler.get(
                    SkywarsDataHandler.Data.MODE_STATS,
                    DatapointSkywarsModeStats.class);
            SkywarsModeStats stats = statsDP.getValue();

            boolean isWinner = winner != null && winner.getUuid().equals(player.getUuid());
            if (isWinner) {
                stats.recordWin(mode);
            } else {
                stats.recordLoss(mode);
            }

            long soulsEarned = gameStats.get(player.getUuid(), SkywarsGameStat.SOULS);
            stats.recordSoulGathered(mode, soulsEarned);

            DatapointSkywarsKitStats kitStatsDP = handler.get(
                    SkywarsDataHandler.Data.KIT_STATS,
                    DatapointSkywarsKitStats.class);
            DatapointSkywarsKitStats.SkywarsKitStats kitStats = kitStatsDP.getValue();
            DatapointSkywarsKitStats.KitStatistics currentKitStats = kitStats.getStatsForKit(player.getSelectedKit());

            currentKitStats.addTimePlayed(gameDurationSeconds);
            currentKitStats.setMostKillsInGame(Math.toIntExact(
                    gameStats.get(player.getUuid(), SkywarsGameStat.KILLS)));

            if (isWinner) {
                currentKitStats.addWin();
                currentKitStats.setFastestWin(gameDurationSeconds);
            }

            DatapointLong soulsDP = handler.get(SkywarsDataHandler.Data.SOULS, DatapointLong.class);
            soulsDP.setValue(soulsDP.getValue() + soulsEarned);

            int coinsEarned = calculateCoinsEarned(player, winner);
            DatapointLong coinsDP = handler.get(SkywarsDataHandler.Data.COINS, DatapointLong.class);
            coinsDP.setValue(coinsDP.getValue() + coinsEarned);
        }
    }

    public boolean hasMinimumPlayers() {
        return players.size() >= gameType.getMinPlayers();
    }

    private int countAlivePlayers() {
        return (int) players.stream()
                .filter(p -> !p.isEliminated())
                .count();
    }

    private SkywarsPlayer getLastStandingPlayer() {
        return players.stream()
                .filter(p -> !p.isEliminated())
                .findFirst()
                .orElse(null);
    }

    private SkywarsPlayer getPlayerByUuid(UUID uuid) {
        return players.stream()
                .filter(p -> p.getUuid().equals(uuid))
                .findFirst()
                .orElse(null);
    }

    public void forceStart(int seconds) {
        countdown.forceStart(seconds);
    }

    public void broadcastMessage(Text message) {
        Audience.audience(players).sendMessage(message);
    }

    public InstanceContainer getInstance() {
        return instanceContainer;
    }

    public boolean isInProgress() {
        return gameStatus == SkywarsGameStatus.IN_PROGRESS;
    }

    public List<SkywarsPlayer> getAlivePlayers() {
        return players.stream()
                .filter(p -> !p.isEliminated())
                .toList();
    }

    private void waitForEmptyThenDestroy() {
        if (instanceContainer.getPlayers().isEmpty()) {
            destroyAndRecreate();
            return;
        }

        MinecraftServer.getSchedulerManager().buildTask(this::waitForEmptyThenDestroy)
                .delay(TaskSchedule.millis(500))
                .schedule();
    }

    private void destroyAndRecreate() {
        TypeSkywarsGameLoader.getGames().remove(this);
        MinecraftServer.getInstanceManager().unregisterInstance(instanceContainer);
        TypeSkywarsGameLoader.createGame(mapEntry, gameType);
    }

    public enum KillType {
        MELEE(" was slain by "),
        BOW(" was shot by "),
        VOID(" was knocked into the void by "),
        FALL(" was pushed to their death by ");

        private final String reason;

        KillType(String reason) {
            this.reason = reason;
        }

        public Text formatMessage(SkywarsPlayer victim, SkywarsPlayer killer) {
            return Text.of("{}<e>{}</e>{}",
                    victim.getFullDisplayName(),
                    reason,
                    killer.getFullDisplayName());
        }
    }

    public enum EnvironmentalDeathType {
        VOID(" fell into the void"),
        FALL(" fell to their death"),
        LAVA(" was burned to a crisp"),
        FIRE(" went up in flames"),
        QUIT(" disconnected");

        private final String reason;

        EnvironmentalDeathType(String reason) {
            this.reason = reason;
        }

        public Text formatMessage(SkywarsPlayer victim) {
            return Text.of("{}<e>{}", victim.getFullDisplayName(), reason);
        }

        public Text formatMessage(String victimName) {
            return Text.of("{}<e>{}", victimName, reason);
        }
    }

    public enum GameEvent {
        GAME_START("Game Start"),
        FIRST_REFILL("First Chest Refill"),
        SECOND_REFILL("Second Chest Refill"),
        DRAGON_SPAWN("Dragon Spawn"),
        GAME_END("Game End");

        private final String displayName;

        GameEvent(String displayName) {
            this.displayName = displayName;
        }

        public String getDisplayName() {
            return displayName;
        }

        public GameEvent getNext() {
            return switch (this) {
                case GAME_START -> FIRST_REFILL;
                case FIRST_REFILL -> SECOND_REFILL;
                case SECOND_REFILL -> DRAGON_SPAWN;
                case DRAGON_SPAWN, GAME_END -> GAME_END;
            };
        }
    }

    public GameEvent skipToNextEvent() {
        if (gameStatus != SkywarsGameStatus.IN_PROGRESS) return null;

        GameEvent nextEvent = currentEvent.getNext();
        if (nextEvent == GameEvent.GAME_END) return null;

        switch (nextEvent) {
            case FIRST_REFILL -> {
                chestManager.triggerRefill(true);
                broadcastMessage(Text.of("<6>Chests have been refilled!"));
            }
            case SECOND_REFILL -> {
                chestManager.triggerRefill(false);
                broadcastMessage(Text.of("<6>Chests have been refilled for the last time!"));
            }
            case DRAGON_SPAWN -> {
                dragonManager.spawnDragonNow(this::broadcastMessage);
            }
        }

        currentEvent = nextEvent;
        return nextEvent;
    }

    public int getAvailableSlots() {
        return Math.max(0, gameType.getMaxPlayers() - players.size());
    }

    public String canAcceptPartyWarp() {
        if (gameStatus == SkywarsGameStatus.IN_PROGRESS) {
            return "Cannot warp - game has already started";
        }
        if (gameStatus == SkywarsGameStatus.ENDING) {
            return "Cannot warp - game is ending";
        }
        return null;
    }
}
