package net.swofty.type.skywarsgame.game;

import lombok.AccessLevel;
import lombok.Getter;
import net.kyori.adventure.title.Title;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.event.Event;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.item.ItemStack;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.Task;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.ServerType;
import net.swofty.commons.mc.HypixelPosition;
import net.swofty.commons.skywars.SkywarsGameType;
import net.swofty.commons.skywars.SkywarsLeaderboardMode;
import net.swofty.commons.skywars.SkywarsModeStats;
import net.swofty.commons.skywars.map.SkywarsMapsConfig;
import net.swofty.commons.text.Text;
import net.swofty.type.game.game.AbstractTeamGame;
import net.swofty.type.game.game.CountdownConfig;
import net.swofty.type.game.game.Game.JoinResult;
import net.swofty.type.game.game.GameState;
import net.swofty.type.game.game.event.PlayerAssignedTeamEvent;
import net.swofty.type.generic.data.datapoints.DatapointLong;
import net.swofty.type.generic.data.datapoints.DatapointSkywarsKitStats;
import net.swofty.type.generic.data.datapoints.DatapointSkywarsModeStats;
import net.swofty.type.generic.data.datapoints.DatapointSkywarsUnlocks;
import net.swofty.type.generic.data.handlers.SkywarsDataHandler;
import net.swofty.type.generic.experience.PlayerExperienceHandler;
import net.swofty.type.generic.event.HypixelEventHandler;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

@Getter
public class SkywarsGame extends AbstractTeamGame<SkywarsPlayer, SkywarsTeam> {
    public static final Tag<Boolean> ELIMINATED_TAG = Tag.Boolean("eliminated");

    public static final int FIRST_REFILL_SECONDS = 180;
    public static final int SECOND_REFILL_SECONDS = 360;
    public static final int DRAGON_SPAWN_SECONDS = 600;

    private final SkywarsGameType gameType;
    private final SkywarsMapsConfig.MapEntry mapEntry;

    @Getter(AccessLevel.NONE)
    private final Map<UUID, SkywarsPlayer> participants = new LinkedHashMap<>();
    @Getter(AccessLevel.NONE)
    private final Map<UUID, String> participantTeams = new HashMap<>();
    @Getter(AccessLevel.NONE)
    private final Map<String, Pos> teamCagePositions = new HashMap<>();
    @Getter(AccessLevel.NONE)
    private final Map<UUID, Long> boundaryWarningStartTime = new HashMap<>();

    private static final int BOUNDARY_WARNING_SECONDS = 5;

    private final CageManager cageManager;
    private final ChestManager chestManager;
    private final DragonManager dragonManager;
    private final LuckyBlock luckyBlockManager;
    private final OPRuleManager opRuleManager;
    private final GameStatTracker<SkywarsGameStat> gameStats = new GameStatTracker<>(SkywarsGameStat.class);

    private long gameStartTime;
    private GameEvent currentEvent = GameEvent.GAME_START;
    private boolean forceCountdownAnnouncements;
    @Getter(AccessLevel.NONE)
    private boolean resultsRecorded;
    @Getter(AccessLevel.NONE)
    private Task boundaryTask;
    @Getter(AccessLevel.NONE)
    private Task endCleanupTask;
    @Getter(AccessLevel.NONE)
    private Task emptyCheckTask;

    public SkywarsGame(SkywarsMapsConfig.MapEntry mapEntry,
                       InstanceContainer instanceContainer,
                       SkywarsGameType gameType) {
        super(Objects.requireNonNull(instanceContainer, "instanceContainer"),
                event -> HypixelEventHandler.callCustomEvent((Event) event));

        this.mapEntry = Objects.requireNonNull(mapEntry, "mapEntry");
        this.gameType = Objects.requireNonNull(gameType, "gameType");

        SkywarsMapsConfig.MapEntry.MapConfiguration config = Objects.requireNonNull(
                mapEntry.getConfiguration(),
                "mapEntry.configuration"
        );
        var islands = Objects.requireNonNull(config.getIslands(), "mapEntry.configuration.islands");
        if (islands.size() < gameType.getMaxTeams()) {
            throw new IllegalArgumentException("Map does not provide enough islands for " + gameType);
        }

        List<Pos> cagePositions = islands.stream()
                .map(island -> {
                    HypixelPosition cage = Objects.requireNonNull(island.getCageCenter(), "island.cageCenter");
                    return new Pos(cage.x(), cage.y(), cage.z(), cage.yaw(), cage.pitch());
                })
                .toList();

        HypixelPosition center = Objects.requireNonNull(config.getCenter(), "mapEntry.configuration.center");
        Pos centerPos = new Pos(center.x(), center.y(), center.z());

        ChestScanner.ChestScanResult scanResult = ChestScanner.scanForChests(
                instance,
                config.getBounds(),
                cagePositions,
                config.getVoidY()
        );
        List<Pos> islandChests = scanResult.islandChests();
        List<Pos> centerChests = scanResult.centerChests();

        this.cageManager = new CageManager(instance, cagePositions);
        this.chestManager = new ChestManager(this, instance, gameType, islandChests, centerChests);
        this.dragonManager = new DragonManager(this, instance, centerPos);

        if (gameType == SkywarsGameType.SOLO_LUCKY_BLOCK) {
            this.luckyBlockManager = new LuckyBlock(instance);
            this.luckyBlockManager.setGame(this);
            this.opRuleManager = new OPRuleManager(this);
        } else {
            this.luckyBlockManager = null;
            this.opRuleManager = null;
        }

        for (int teamId = 0; teamId < gameType.getMaxTeams(); teamId++) {
            registerTeam(new SkywarsTeam(teamId));
        }
    }

    @Override
    protected CountdownConfig getCountdownConfig() {
        return CountdownConfig.DEFAULT;
    }

    @Override
    protected void onCountdownCancelled() {
        forceCountdownAnnouncements = false;
        super.onCountdownCancelled();
    }

    @Override
    public int getMaxPlayers() {
        return gameType.getMaxPlayers();
    }

    @Override
    public int getMinPlayers() {
        return gameType.getMinPlayers();
    }

    @Override
    protected int getTeamSize() {
        return gameType.getTeamSize();
    }

    @Override
    protected boolean isTeamViable(SkywarsTeam team) {
        return team.getPlayerIds().stream().anyMatch(uuid -> {
            SkywarsPlayer player = players.get(uuid);
            return player != null && !player.isEliminated();
        });
    }

    @Override
    public void autoAssignTeams() {
        super.autoAssignTeams();
        getPlayers().forEach(player -> getPlayerTeam(player.getUuid())
                .ifPresent(team -> participantTeams.put(player.getUuid(), team.getId())));
    }

    @Override
    public JoinResult join(SkywarsPlayer player) {
        Objects.requireNonNull(player, "player");
        JoinResult result = super.join(player);
        if (!(result instanceof JoinResult.Success)) {
            String reason = result instanceof JoinResult.Denied denied
                    ? denied.reason()
                    : "Unable to join game";
            player.sendMessage("<c>{}", reason);
            player.sendTo(ServerType.SKYWARS_LOBBY);
            return result;
        }

        participants.put(player.getUuid(), player);
        assignToTeam(player);
        setupPlayerForWaiting(player);

        broadcastMessage("{}<e> has joined (<b>{}<e>/<b>{}<e>)!",
                player.getFullDisplayName(), getPlayers().size(), getMaxPlayers());
        return result;
    }

    @Override
    public void leave(SkywarsPlayer player) {
        if (getPlayer(player.getUuid()).isEmpty()) return;

        GameState stateAtLeave = getState();
        removePlayer(player, stateAtLeave);
        player.sendTo(ServerType.SKYWARS_LOBBY);
    }

    public void disconnect(SkywarsPlayer player) {
        if (getPlayer(player.getUuid()).isEmpty()) return;

        if (getState() != GameState.IN_PROGRESS) {
            removePlayer(player, getState());
            return;
        }

        String teamId = playerTeams.get(player.getUuid());
        if (!player.isEliminated()) {
            dropPlayerItems(player);
            player.setEliminated(true);
            player.setTag(ELIMINATED_TAG, true);
            broadcastMessage(EnvironmentalDeathType.QUIT.formatMessage(player));
        }

        removeFromTeam(player);
        handleDisconnect(player);
        releaseCage(player, teamId);
        player.setGameId(null);
    }

    private void removePlayer(SkywarsPlayer player, GameState stateAtLeave) {
        String teamId = playerTeams.get(player.getUuid());
        if (stateAtLeave == GameState.IN_PROGRESS && !player.isEliminated()) {
            dropPlayerItems(player);
            player.setEliminated(true);
            player.setTag(ELIMINATED_TAG, true);
            broadcastMessage(EnvironmentalDeathType.QUIT.formatMessage(player));
        } else if (stateAtLeave.isWaiting()) {
            broadcastMessage("{}<e> has quit!", player.getFullDisplayName());
        }

        super.leave(player);
        if (stateAtLeave.isWaiting()) {
            participants.remove(player.getUuid());
            participantTeams.remove(player.getUuid());
        }

        releaseCage(player, teamId);

        if (stateAtLeave == GameState.COUNTDOWN && !hasMinimumPlayers()) {
            cancelCountdown();
        }
    }

    @Override
    protected boolean canPlayerRejoin(SkywarsPlayer player) {
        return false;
    }

    private void assignToTeam(SkywarsPlayer player) {
        if (getPlayerTeam(player.getUuid()).isPresent()) return;

        SkywarsTeam team = getTeams().stream()
                .filter(candidate -> candidate.getPlayerCount() < getTeamSize())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No SkyWars team has capacity"));

        team.addPlayer(player.getUuid());
        playerTeams.put(player.getUuid(), team.getId());
        participantTeams.put(player.getUuid(), team.getId());
        eventDispatcher.accept(new PlayerAssignedTeamEvent<>(
                this,
                player.getServerPlayer(),
                team
        ));
    }

    private void cancelCountdown() {
        if (!getCountdown().isActive()) return;
        forceCountdownAnnouncements = false;
        getCountdown().terminate();
        setState(GameState.WAITING);
        broadcastMessage("<c>Not enough players to start the game.");
    }

    private void dropPlayerItems(SkywarsPlayer player) {
        Pos dropPos = player.getPosition();

        for (int i = 0; i < player.getInventory().getSize(); i++) {
            ItemStack item = player.getInventory().getItemStack(i);
            if (!item.isAir()) {
                ItemEntity itemEntity = new ItemEntity(item);
                itemEntity.setInstance(instance, dropPos.add(0, 1, 0));
                itemEntity.setPickupDelay(Duration.ofMillis(500));
                itemEntity.setVelocity(itemEntity.getVelocity().add(
                        (Math.random() - 0.5) * 5,
                        3,
                        (Math.random() - 0.5) * 5
                ));
            }
        }

        EquipmentSlot[] armorSlots = {
                EquipmentSlot.HELMET,
                EquipmentSlot.CHESTPLATE,
                EquipmentSlot.LEGGINGS,
                EquipmentSlot.BOOTS
        };
        for (EquipmentSlot slot : armorSlots) {
            ItemStack armor = player.getEquipment(slot);
            if (armor != null && !armor.isAir()) {
                ItemEntity itemEntity = new ItemEntity(armor);
                itemEntity.setInstance(instance, dropPos.add(0, 1, 0));
                itemEntity.setPickupDelay(Duration.ofMillis(500));
                itemEntity.setVelocity(itemEntity.getVelocity().add(
                        (Math.random() - 0.5) * 5,
                        3,
                        (Math.random() - 0.5) * 5
                ));
            }
        }

        player.getInventory().clear();
        for (EquipmentSlot slot : armorSlots) {
            player.setEquipment(slot, ItemStack.AIR);
        }
    }

    public int getPlayerTeam(SkywarsPlayer player) {
        return getPlayerTeam(player.getUuid())
                .map(SkywarsTeam::getTeamId)
                .orElse(-1);
    }

    private void setupPlayerForWaiting(SkywarsPlayer player) {
        String teamId = playerTeams.get(player.getUuid());
        if (teamId == null) {
            throw new IllegalStateException("Player has no team assignment");
        }

        player.resetGameState();
        Pos cagePos = teamCagePositions.computeIfAbsent(teamId, ignored -> cageManager.assignCage(player));
        player.setCagePosition(cagePos);

        if (player.getInstance() == null || !player.getInstance().getUuid().equals(instance.getUuid())) {
            player.setInstance(instance, cagePos);
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
        player.setAllowFlying(false);
        player.setFlying(false);
        player.setGameMode(GameMode.ADVENTURE);
        player.setTag(ELIMINATED_TAG, false);

        if (gameType == SkywarsGameType.SOLO_LUCKY_BLOCK) {
            player.sendActionBar(Text.of("<c>Kits and perks are disabled in Lucky Block SkyWars"));
        }
    }

    private static final Text THICK_BAR = Text.of("<a><l>▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬");

    @Override
    public void start() {
        if (!getState().isWaiting()) return;
        super.start();
        if (getState() != GameState.IN_PROGRESS) return;

        forceCountdownAnnouncements = false;
        autoAssignTeams();
        gameStartTime = System.currentTimeMillis();
        currentEvent = GameEvent.GAME_START;
        resultsRecorded = false;

        cageManager.openAllCages();

        for (SkywarsPlayer player : getPlayers()) {
            player.setGameMode(GameMode.SURVIVAL);
            player.setAllowFlying(false);
            player.setFlying(false);
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
        getPlayers().forEach(p -> p.showTitle(title));

        chestManager.scheduleRefills(
                () -> {
                    currentEvent = GameEvent.FIRST_REFILL;
                    broadcastMessage(Text.of("<6>Chests have been refilled!"));
                },
                () -> {
                    currentEvent = GameEvent.SECOND_REFILL;
                    broadcastMessage(Text.of("<6>Chests have been refilled for the last time!"));
                }
        );

        dragonManager.scheduleDragonSpawn(
                this::broadcastMessage,
                () -> currentEvent = GameEvent.DRAGON_SPAWN
        );

        boundaryTask = MinecraftServer.getSchedulerManager().buildTask(this::checkPlayerBoundaries)
                .repeat(TaskSchedule.tick(1))
                .schedule();
    }

    public void startGame() {
        start();
    }

    private void sendGameIntroMessage() {
        for (SkywarsPlayer player : getPlayers()) {
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
        if (getState() != GameState.IN_PROGRESS) return;

        SkywarsMapsConfig.MapEntry.MapConfiguration config = mapEntry.getConfiguration();
        int voidY = config.getVoidY();
        SkywarsMapsConfig.MapBounds bounds = config.getBounds();

        for (SkywarsPlayer player : List.copyOf(getPlayers())) {
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
            for (ItemStack item : kit.getStartingItems(gameType.getModeString())) {
                player.getInventory().addItemStack(item);
            }
            player.setSelectedKit(kitId);
        }
    }

    public void onPlayerKill(SkywarsPlayer killer, SkywarsPlayer victim, KillType killType) {
        if (getState() != GameState.IN_PROGRESS
                || killer == null
                || victim == null
                || killType == null
                || getPlayer(killer.getUuid()).isEmpty()
                || getPlayer(victim.getUuid()).isEmpty()
                || killer.isEliminated()
                || killer.getUuid().equals(victim.getUuid())
                || victim.isEliminated()) {
            return;
        }

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
        if (getState() != GameState.IN_PROGRESS
                || victim == null
                || deathType == null
                || getPlayer(victim.getUuid()).isEmpty()
                || victim.isEliminated()) {
            return;
        }

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

    @Override
    public void checkWinConditions() {
        if (getState() != GameState.IN_PROGRESS) return;
        super.checkWinConditions();
    }

    public void onDragonKilled(UUID killerUuid) {
        if (getState() != GameState.IN_PROGRESS) return;

        SkywarsPlayer killer = getPlayerByUuid(killerUuid);
        if (killer != null && killer.isEliminated()) killer = null;
        if (killer != null) {
            broadcastMessage(Text.of("<d>{} has slain the Ender Dragon!", killer.getUsername()));
        }
        SkywarsTeam winningTeam = killer == null
                ? getLastStandingTeam().orElse(null)
                : getPlayerTeam(killerUuid).orElseGet(() -> getLastStandingTeam().orElse(null));
        endGame(SkywarsWinCondition.DRAGON_DEATH, winningTeam, killer);
    }

    public void endGame(SkywarsWinCondition condition, SkywarsTeam winningTeam) {
        endGame(condition, winningTeam, null);
    }

    private void endGame(SkywarsWinCondition condition, SkywarsTeam winningTeam, SkywarsPlayer preferredWinner) {
        Objects.requireNonNull(condition, "condition");
        if (getState() != GameState.IN_PROGRESS || resultsRecorded) return;

        if (condition != SkywarsWinCondition.DRAGON_DEATH && winningTeam == null) {
            winningTeam = getLastStandingTeam().orElse(null);
        }

        resultsRecorded = true;
        currentEvent = GameEvent.GAME_END;
        end();
        cancelTask(boundaryTask);
        boundaryTask = null;
        chestManager.stop();
        dragonManager.cleanup();
        if (opRuleManager != null) opRuleManager.stopContinuousEffects();

        SkywarsPlayer winner = preferredWinner != null ? preferredWinner : getLastStandingPlayer();

        recordGameStats(winningTeam);
        sendGameResults(winner, winningTeam);

        endCleanupTask = MinecraftServer.getSchedulerManager().buildTask(() -> {
            endCleanupTask = null;
            List<SkywarsPlayer> playersToRemove = new ArrayList<>(getPlayers());
            for (SkywarsPlayer player : playersToRemove) {
                leave(player);
            }
            waitForEmptyThenDestroy();
        }).delay(TaskSchedule.seconds(10)).schedule();
    }

    public void endGame(SkywarsWinCondition condition) {
        endGame(condition, getLastStandingTeam().orElse(null));
    }

    private void sendGameResults(SkywarsPlayer winner, SkywarsTeam winningTeam) {
        for (SkywarsPlayer player : getPlayers()) {
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

            int coinsEarned = calculateCoinsEarned(player, winningTeam);
            long expEarned = 150 + (gameStats.get(player.getUuid(), SkywarsGameStat.KILLS) * 25);

            player.sendMessage("                 <f><l>Reward Summary");
            player.sendMessage("   <7>You earned:");
            player.sendMessage("   <6>+{} coins", coinsEarned);
            player.sendMessage("   <a>+{} souls", gameStats.get(player.getUuid(), SkywarsGameStat.SOULS));
            player.sendMessage("   <b>+{} Hypixel Experience", expEarned);

            player.sendMessage(THICK_BAR);

            PlayerExperienceHandler expHandler = new PlayerExperienceHandler(player);
            expHandler.addExperience(expEarned);
        }
    }

    private int calculateCoinsEarned(SkywarsPlayer player, SkywarsTeam winningTeam) {
        int coins = 10;
        coins += Math.toIntExact(gameStats.get(player.getUuid(), SkywarsGameStat.KILLS) * 5);
        coins += Math.toIntExact(gameStats.get(player.getUuid(), SkywarsGameStat.ASSISTS) * 2);
        if (isWinner(player, winningTeam)) {
            coins += 25;
        }
        return coins;
    }

    private void recordGameStats(SkywarsTeam winningTeam) {
        SkywarsLeaderboardMode mode = SkywarsLeaderboardMode.fromGameType(gameType);
        long gameDurationSeconds = (System.currentTimeMillis() - gameStartTime) / 1000;

        for (SkywarsPlayer player : participants.values()) {
            SkywarsDataHandler handler = SkywarsDataHandler.getUser(player);
            if (handler == null) continue;

            DatapointSkywarsModeStats statsDP = handler.get(
                    SkywarsDataHandler.Data.MODE_STATS,
                    DatapointSkywarsModeStats.class);
            SkywarsModeStats stats = statsDP.getValue();

            boolean isWinner = isWinner(player, winningTeam);
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

            int coinsEarned = calculateCoinsEarned(player, winningTeam);
            DatapointLong coinsDP = handler.get(SkywarsDataHandler.Data.COINS, DatapointLong.class);
            coinsDP.setValue(coinsDP.getValue() + coinsEarned);
        }
    }

    private boolean isWinner(SkywarsPlayer player, SkywarsTeam winningTeam) {
        return winningTeam != null && winningTeam.getId().equals(participantTeams.get(player.getUuid()));
    }

    @Override
    public boolean hasMinimumPlayers() {
        return getPlayers().size() >= getMinPlayers();
    }

    private SkywarsPlayer getLastStandingPlayer() {
        return getPlayers().stream()
                .filter(p -> !p.isEliminated())
                .findFirst()
                .orElse(null);
    }

    private Optional<SkywarsTeam> getLastStandingTeam() {
        return getViableTeams().stream().findFirst();
    }

    private SkywarsPlayer getPlayerByUuid(UUID uuid) {
        return uuid == null ? null : getPlayer(uuid).orElse(null);
    }

    public void forceStart(int seconds) {
        if (seconds <= 0) {
            throw new IllegalArgumentException("seconds must be positive");
        }
        if (!getState().isWaiting() || !hasMinimumPlayers()) return;

        forceCountdownAnnouncements = true;
        if (!getCountdown().isActive()) {
            setState(GameState.COUNTDOWN);
            if (!getCountdown().start()) {
                forceCountdownAnnouncements = false;
                setState(GameState.WAITING);
                return;
            }
        }
        getCountdown().setRemainingSeconds(seconds);
    }

    @Override
    public InstanceContainer getInstance() {
        return getInstanceContainer();
    }

    public InstanceContainer getInstanceContainer() {
        return (InstanceContainer) super.getInstance();
    }

    @Override
    public void dispose() {
        if (getState() == GameState.TERMINATED) return;

        cancelTask(boundaryTask);
        boundaryTask = null;
        cancelTask(endCleanupTask);
        endCleanupTask = null;
        cancelTask(emptyCheckTask);
        emptyCheckTask = null;
        dragonManager.cleanup();
        chestManager.reset();
        cageManager.reset();
        if (luckyBlockManager != null) luckyBlockManager.reset();
        if (opRuleManager != null) opRuleManager.reset();
        boundaryWarningStartTime.clear();
        participants.values().forEach(player -> {
            player.setGameId(null);
            player.setCagePosition(null);
        });
        participants.clear();
        participantTeams.clear();
        teamCagePositions.clear();
        playerTeams.clear();
        teams.values().forEach(team -> team.getPlayerIds().forEach(team::removePlayer));
        super.dispose();
    }

    private void releaseCage(SkywarsPlayer player, String teamId) {
        if (teamId == null) {
            cageManager.releaseCage(player);
            return;
        }
        if (getTeam(teamId).map(SkywarsTeam::hasPlayers).orElse(false)) {
            player.setCagePosition(null);
            return;
        }

        teamCagePositions.remove(teamId);
        cageManager.releaseCage(player);
    }

    private static void cancelTask(Task task) {
        if (task != null) task.cancel();
    }

    public boolean isInProgress() {
        return getState() == GameState.IN_PROGRESS;
    }

    public List<SkywarsPlayer> getAlivePlayers() {
        if (!isInProgress()) return List.of();

        return getPlayers().stream()
                .filter(p -> !p.isEliminated())
                .toList();
    }

    private void waitForEmptyThenDestroy() {
        if (getState() == GameState.TERMINATED) return;
        if (getInstanceContainer().getPlayers().isEmpty()) {
            emptyCheckTask = null;
            destroyAndRecreate();
            return;
        }

        emptyCheckTask = MinecraftServer.getSchedulerManager().buildTask(this::waitForEmptyThenDestroy)
                .delay(TaskSchedule.millis(500))
                .schedule();
    }

    private void destroyAndRecreate() {
        synchronized (TypeSkywarsGameLoader.class) {
            dispose();
            TypeSkywarsGameLoader.getGames().remove(this);
            MinecraftServer.getInstanceManager().unregisterInstance(getInstanceContainer());
            TypeSkywarsGameLoader.createGame(mapEntry, gameType);
        }
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
        if (getState() != GameState.IN_PROGRESS) return null;

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
                dragonManager.spawnDragonNow(
                        this::broadcastMessage,
                        () -> currentEvent = GameEvent.DRAGON_SPAWN
                );
            }
        }

        currentEvent = nextEvent;
        return nextEvent;
    }

    public String canAcceptPartyWarp() {
        if (getState() == GameState.IN_PROGRESS) {
            return "Cannot warp - game has already started";
        }
        if (getState().isEnding()) {
            return "Cannot warp - game is ending";
        }
        return null;
    }
}
