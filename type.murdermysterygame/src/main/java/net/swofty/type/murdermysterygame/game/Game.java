package net.swofty.type.murdermysterygame.game;

import lombok.Getter;
import net.hollowcube.polar.PolarLoader;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import net.minestom.server.MinecraftServer;
import net.minestom.server.color.TeamColor;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.metadata.item.ItemEntityMeta;
import net.minestom.server.event.Event;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.TeamsPacket;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.ServerType;
import net.swofty.commons.ServiceType;
import net.swofty.commons.murdermystery.MurderMysteryGameType;
import net.swofty.commons.murdermystery.MurderMysteryLeaderboardMode;
import net.swofty.commons.murdermystery.map.MurderMysteryMapsConfig;
import net.swofty.type.game.game.AbstractGame;
import net.swofty.type.game.game.CountdownConfig;
import net.swofty.type.game.game.GameState;
import net.swofty.type.game.game.Game.JoinResult;
import net.swofty.type.generic.achievement.PlayerAchievementHandler;
import net.swofty.type.generic.data.datapoints.DatapointMurderMysteryModeStats;
import net.swofty.type.generic.data.handlers.MurderMysteryDataHandler;
import net.swofty.type.generic.event.HypixelEventHandler;
import net.swofty.type.generic.experience.PlayerExperienceHandler;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.murdermysterygame.TypeMurderMysteryGameLoader;
import net.swofty.type.murdermysterygame.gold.GoldManager;
import net.swofty.type.murdermysterygame.replay.MurderMysteryReplayManager;
import net.swofty.type.murdermysterygame.role.GameRole;
import net.swofty.type.murdermysterygame.role.RoleManager;
import net.swofty.type.murdermysterygame.user.MurderMysteryPlayer;
import net.swofty.type.murdermysterygame.weapon.WeaponManager;

import java.io.File;
import java.time.Duration;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

@Getter
public class Game extends AbstractGame<MurderMysteryPlayer> {
    public static final Tag<Boolean> ELIMINATED_TAG = Tag.Boolean("eliminated");

    private final MurderMysteryGameType gameType;
    private final MurderMysteryMapsConfig.MapEntry mapEntry;

    private final RoleManager roleManager;
    private final GoldManager goldManager;
    private final WeaponManager weaponManager;
    private MurderMysteryReplayManager replayManager;

    private boolean murdererReceivedSword = false;
    private boolean forceCountdownAnnouncements = false;
    private long gameStartTime = 0;
    private long murdererSwordTime = 0;
    private Entity droppedDetectiveBow = null;
    private boolean detectiveBowPickedUp = false;
    private MurderMysteryPlayer murdererKiller = null;
    private final Map<UUID, Integer> murdererKillsThisGame = new HashMap<>();
    private int deathCount = 0;
    private long lastMurdererKillTime = 0;
    private final Map<UUID, Game.KillType> murdererKillMethods = new HashMap<>();

    public Game(MurderMysteryMapsConfig.MapEntry mapEntry,
                InstanceContainer instanceContainer,
                MurderMysteryGameType gameType) {
        super(instanceContainer, event -> HypixelEventHandler.callCustomEvent((Event) event));

        this.gameType = gameType;
        this.mapEntry = mapEntry;

        this.roleManager = new RoleManager(this);
        this.goldManager = new GoldManager(this);
        this.weaponManager = new WeaponManager(this);
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
    public JoinResult join(MurderMysteryPlayer player) {
        JoinResult result = super.join(player);
        if (!(result instanceof JoinResult.Success)) {
            if (result instanceof JoinResult.Denied denied) {
                player.sendMessage(Component.text(denied.reason(), NamedTextColor.RED));
            }
            player.sendTo(ServerType.MURDER_MYSTERY_LOBBY);
            return result;
        }

        setupPlayerForWaiting(player);

        broadcastMessage(Component.empty()
                .append(Component.text(player.getFullDisplayName()))
                .append(Component.text(" has joined ", NamedTextColor.YELLOW))
                .append(Component.text("(", NamedTextColor.YELLOW))
                .append(Component.text(getPlayers().size(), NamedTextColor.AQUA))
                .append(Component.text("/", NamedTextColor.YELLOW))
                .append(Component.text(gameType.getMaxPlayers(), NamedTextColor.AQUA))
                .append(Component.text(")!", NamedTextColor.YELLOW)));

        return result;
    }

    @Override
    public void leave(MurderMysteryPlayer player) {
        if (getPlayer(player.getUuid()).isEmpty()) return;
        super.leave(player);
        player.sendTo(ServerType.MURDER_MYSTERY_LOBBY);

        if (getState() == GameState.COUNTDOWN && !hasMinimumPlayers()) {
            getCountdown().terminate();
            forceCountdownAnnouncements = false;
            setState(GameState.WAITING);
            broadcastMessage(Component.text("Countdown cancelled - not enough players!", NamedTextColor.RED));
        }
    }

    public void disconnect(MurderMysteryPlayer player) {
        if (getState() == GameState.IN_PROGRESS) {
            handleDisconnect(player);
        } else {
            leave(player);
        }
    }

    public boolean hasDisconnectedPlayer(UUID uuid) {
        return disconnectedPlayers.containsKey(uuid);
    }

    public void rejoin(MurderMysteryPlayer player) {
        if (!handleRejoin(player)) {
            player.sendTo(ServerType.MURDER_MYSTERY_LOBBY);
        }
    }

    @Override
    public boolean handleRejoin(MurderMysteryPlayer player) {
        if (!super.handleRejoin(player)) return false;

        GameRole role = roleManager.getRole(player.getUuid());
        if (role != null) {
            setupPlayerForGame(player, role);
            addPlayerToHiddenNametagsTeam(player);
            player.setInstance(getInstanceContainer(), getWaitingPosition());
            player.sendMessage(Component.text("You have rejoined the game!", NamedTextColor.GREEN));
        } else {
            setupPlayerForSpectator(player);
            player.setInstance(getInstanceContainer(), getWaitingPosition());
            player.sendMessage(Component.text("You have rejoined as a spectator.", NamedTextColor.GRAY));
        }
        return true;
    }

    @Override
    public void start() {
        if (getState() == GameState.IN_PROGRESS) return;
        super.start();
        if (getState() != GameState.IN_PROGRESS) return;
        forceCountdownAnnouncements = false;

        gameStartTime = System.currentTimeMillis();
        murdererReceivedSword = false;

        roleManager.assignRoles();
        setupHiddenNametags();

        for (MurderMysteryPlayer player : getPlayers()) {
            GameRole role = roleManager.getRole(player.getUuid());
            setupPlayerForGame(player, role);
            announceRole(player, role);
        }

        replayManager = new MurderMysteryReplayManager(this, new ProxyService(ServiceType.REPLAY));
        replayManager.startRecording();
        broadcastMessage(Component.text("Teaming with the Murderer is not allowed!", NamedTextColor.RED, net.kyori.adventure.text.format.TextDecoration.BOLD));
        goldManager.startSpawning();
        startKillZoneCheck();
        startMurdererSwordCountdown();
        startSurvivalRewards();

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (getState() == GameState.IN_PROGRESS) {
                endGame(WinCondition.TIME_EXPIRED);
            }
        }).delay(TaskSchedule.minutes(5)).schedule();
    }

    public void startGame() {
        start();
    }

    private void startMurdererSwordCountdown() {
        final int[] secondsRemaining = {30};

        var task = MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (getState() != GameState.IN_PROGRESS) return;

            secondsRemaining[0]--;

            if (secondsRemaining[0] <= 5 && secondsRemaining[0] > 0) {
                String word = secondsRemaining[0] == 1 ? "second" : "seconds";
                broadcastMessage(Component.empty()
                        .append(Component.text("The Murderer receives their sword in ", NamedTextColor.YELLOW))
                        .append(Component.text(secondsRemaining[0], NamedTextColor.RED))
                        .append(Component.text(" " + word + "!", NamedTextColor.YELLOW)));
            }
        }).delay(TaskSchedule.seconds(1)).repeat(TaskSchedule.seconds(1)).schedule();

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (getState() != GameState.IN_PROGRESS) return;
            task.cancel();
            murdererReceivedSword = true;
            murdererSwordTime = System.currentTimeMillis();

            for (MurderMysteryPlayer murderer : roleManager.getPlayersWithRole(GameRole.MURDERER)) {
                if (!murderer.isEliminated()) {
                    weaponManager.giveMurdererKnife(murderer);
                }
            }

            broadcastMessage(Component.text("The Murderer has received their sword!", NamedTextColor.YELLOW));
        }).delay(TaskSchedule.seconds(30)).schedule();
    }

    private void startSurvivalRewards() {
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (getState() != GameState.IN_PROGRESS) return;

            MurderMysteryLeaderboardMode leaderboardMode = MurderMysteryLeaderboardMode.fromGameType(gameType);

            for (MurderMysteryPlayer player : getPlayers()) {
                if (!player.isEliminated()) {
                    player.addTokens(40);

                    MurderMysteryDataHandler handler = MurderMysteryDataHandler.getUser(player);
                    if (handler != null) {
                        DatapointMurderMysteryModeStats statsDP = handler.get(
                                MurderMysteryDataHandler.Data.MODE_STATS,
                                DatapointMurderMysteryModeStats.class);
                        statsDP.getValue().recordTokens(leaderboardMode, 40);
                    }

                    player.sendMessage(Component.text("+40 Tokens! Survived 30 seconds", NamedTextColor.DARK_GREEN));
                }
            }
        }).delay(TaskSchedule.seconds(30)).repeat(TaskSchedule.seconds(30)).schedule();
    }

    public boolean hasMurdererReceivedSword() {
        return murdererReceivedSword;
    }

    private void startKillZoneCheck() {
        var killRegions = mapEntry.getConfiguration() != null ? mapEntry.getConfiguration().getKillRegions() : null;
        if (killRegions == null || killRegions.isEmpty()) return;

        boolean isAquariumMap = mapEntry.getName() != null &&
                mapEntry.getName().toLowerCase().contains("aquarium");

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (getState() != GameState.IN_PROGRESS) return;

            List<MurderMysteryPlayer> playersToCheck = new ArrayList<>(getPlayers());
            for (MurderMysteryPlayer player : playersToCheck) {
                if (player.isEliminated()) continue;

                double x = player.getPosition().x();
                double y = player.getPosition().y();
                double z = player.getPosition().z();

                for (MurderMysteryMapsConfig.KillRegion region : killRegions) {
                    if (region.contains(x, y, z)) {
                        if (isAquariumMap) {
                            PlayerAchievementHandler achHandler = new PlayerAchievementHandler(player);
                            achHandler.addProgress("murdermystery.jaws", 1);
                        }
                        onEnvironmentalDeath(player);
                        break;
                    }
                }
            }
        }).repeat(TaskSchedule.tick(5)).schedule();
    }

    public void onEnvironmentalDeath(MurderMysteryPlayer victim) {
        onEnvironmentalDeath(victim, "You fell out of the world.");
    }

    public void onEnvironmentalDeath(MurderMysteryPlayer victim, String deathReason) {
        GameRole victimRole = roleManager.getRole(victim.getUuid());

        victim.setEliminated(true);
        victim.setTag(ELIMINATED_TAG, true);
        setupPlayerForSpectator(victim);

        if (replayManager != null) {
            replayManager.recordEnvironmentalDeath(victim, deathReason);
        }

        sendDeathMessage(victim, deathReason);

        if (victimRole == GameRole.DETECTIVE) {
            dropDetectiveBow(victim);
        }

        checkWinConditions();
    }

    private void setupPlayerForWaiting(MurderMysteryPlayer player) {
        Pos waitingPos = getWaitingPosition();

        if (player.getInstance() == null || !player.getInstance().getUuid().equals(getInstanceContainer().getUuid())) {
            player.setInstance(getInstanceContainer(), waitingPos);
        } else {
            player.teleport(waitingPos);
        }

        player.getInventory().clear();
        player.getInventory().setItemStack(8,
                TypeMurderMysteryGameLoader.getItemHandler().getItem("leave_game").getItemStack());
        player.setFlying(false);
        player.setGameMode(GameMode.ADVENTURE);
        player.setEliminated(false);
        player.resetGold();
        player.setFood(20);
        player.setFoodSaturation(20.0f);
    }

    private Pos getWaitingPosition() {
        var config = mapEntry.getConfiguration();
        if (config != null && config.getLocations() != null && config.getLocations().getWaiting() != null) {
            var waiting = config.getLocations().getWaiting();
            return new Pos(waiting.x(), waiting.y(), waiting.z(), waiting.yaw(), waiting.pitch());
        }
        return new Pos(0, 66, 0);
    }

    private void setupPlayerForGame(MurderMysteryPlayer player, GameRole role) {
        player.getInventory().clear();
        player.setGameMode(GameMode.ADVENTURE);

        Pos spawnPos = getRandomSpawnPosition();
        player.teleport(spawnPos);

        player.setFood(20);
        player.setFoodSaturation(20.0f);

        if (role == GameRole.DETECTIVE) {
            weaponManager.giveDetectiveBow(player);
        }
    }

    private void setupPlayerForSpectator(MurderMysteryPlayer player) {
        player.getInventory().clear();
        player.getInventory().setItemStack(0,
                TypeMurderMysteryGameLoader.getItemHandler().getItem("spectator_compass").getItemStack());
        player.getInventory().setItemStack(7,
                TypeMurderMysteryGameLoader.getItemHandler().getItem("play_again").getItemStack());
        player.getInventory().setItemStack(8,
                TypeMurderMysteryGameLoader.getItemHandler().getItem("leave_game").getItemStack());

        for (MurderMysteryPlayer otherPlayer : getPlayers()) {
            if (!otherPlayer.equals(player) && !otherPlayer.isEliminated()) {
                player.removeViewer(otherPlayer);
                player.updateOldViewer(otherPlayer);
            }
        }

        player.setAllowFlying(true);
        player.setFlying(true);
        player.setFood(20);
        player.setFoodSaturation(20.0f);
    }

    private void setupHiddenNametags() {
        List<String> playerNames = getPlayers().stream()
                .map(MurderMysteryPlayer::getUsername)
                .toList();

        TeamsPacket createTeamPacket = new TeamsPacket(
                "mm_hidden",
                new TeamsPacket.CreateTeamAction(
                        new TeamsPacket.Settings(
                                Component.empty(),
                                Component.empty(),
                                Component.empty(),
                                TeamsPacket.NameTagVisibility.NEVER,
                                TeamsPacket.CollisionRule.ALWAYS,
                                TeamColor.WHITE,
                                (byte) 0x00
                        ),
                        playerNames
                )
        );

        for (MurderMysteryPlayer player : getPlayers()) {
            player.sendPacket(createTeamPacket);
        }
    }

    private void addPlayerToHiddenNametagsTeam(MurderMysteryPlayer newPlayer) {
        List<String> allPlayerNames = getPlayers().stream()
                .map(MurderMysteryPlayer::getUsername)
                .toList();

        TeamsPacket createTeamPacket = new TeamsPacket(
                "mm_hidden",
                new TeamsPacket.CreateTeamAction(
                        new TeamsPacket.Settings(
                                Component.empty(),
                                Component.empty(),
                                Component.empty(),
                                TeamsPacket.NameTagVisibility.NEVER,
                                TeamsPacket.CollisionRule.ALWAYS,
                                TeamColor.WHITE,
                                (byte) 0x00
                        ),
                        allPlayerNames
                )
        );
        newPlayer.sendPacket(createTeamPacket);

        TeamsPacket addPlayerPacket = new TeamsPacket(
                "mm_hidden",
                new TeamsPacket.AddEntitiesToTeamAction(List.of(newPlayer.getUsername()))
        );

        for (MurderMysteryPlayer player : getPlayers()) {
            if (!player.equals(newPlayer)) {
                player.sendPacket(addPlayerPacket);
            }
        }
    }

    private Pos getRandomSpawnPosition() {
        var config = mapEntry.getConfiguration();
        if (config != null && config.getPlayerSpawns() != null && !config.getPlayerSpawns().isEmpty()) {
            var spawns = config.getPlayerSpawns();
            var spawn = spawns.get(ThreadLocalRandom.current().nextInt(spawns.size()));
            return new Pos(spawn.x(), spawn.y(), spawn.z());
        }
        return getWaitingPosition();
    }

    private void announceRole(MurderMysteryPlayer player, GameRole role) {
        Title title = Title.title(
                Component.text("You are the " + role.getDisplayName(), role.getColor()),
                Component.text(role.getDescription(), NamedTextColor.GRAY),
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(3), Duration.ofMillis(500))
        );
        player.showTitle(title);
    }

    public void onPlayerKill(MurderMysteryPlayer killer, MurderMysteryPlayer victim) {
        onPlayerKill(killer, victim, KillType.KNIFE);
    }

    public void onPlayerKill(MurderMysteryPlayer killer, MurderMysteryPlayer victim, KillType killType) {
        GameRole killerRole = roleManager.getRole(killer.getUuid());
        GameRole victimRole = roleManager.getRole(victim.getUuid());

        killer.addKill();

        recordKillStats(killer, killType);

        deathCount++;

        killer.recordKillTimestamp();

        killer.getQuestHandler().addProgressByTrigger("murdermystery.kills", 1);

        if (killerRole == GameRole.MURDERER) {
            int currentKills = murdererKillsThisGame.getOrDefault(killer.getUuid(), 0) + 1;
            murdererKillsThisGame.put(killer.getUuid(), currentKills);
            lastMurdererKillTime = System.currentTimeMillis();

            PlayerAchievementHandler killerAchHandler = new PlayerAchievementHandler(killer);

            killerAchHandler.addProgress("murdermystery.stabber", 1);

            killerAchHandler.addProgress("murdermystery.wheres_my_emp", 1);

            if (killer.getKillsInLast5Seconds() >= 5) {
                killerAchHandler.addProgress("murdermystery.slice_n_dice", 1);
            }

            if (victimRole == GameRole.DETECTIVE && murdererSwordTime > 0) {
                if (System.currentTimeMillis() - murdererSwordTime <= 30000) {
                    killerAchHandler.addProgress("murdermystery.not_today", 1);
                }
            }

            if (killType == KillType.KNIFE) {
                killer.resetBowKillStreak();
            }
        }

        if (victimRole == GameRole.MURDERER) {
            murdererKiller = killer;

            if (gameType != MurderMysteryGameType.ASSASSINS) {
                killer.getQuestHandler().addProgressByTrigger("murdermystery.power_play", 1);

                killer.getQuestHandler().addProgressByTrigger("murdermystery.hero", 1);

                if (killerRole == GameRole.DETECTIVE) {
                    long elapsedMs = System.currentTimeMillis() - gameStartTime;
                    if (elapsedMs < 120_000) {
                        killer.getQuestHandler().addProgressByTrigger("murdermystery.sherlock", 1);
                    }
                }

                PlayerAchievementHandler killerAchHandler = new PlayerAchievementHandler(killer);

                if (deathCount == 1) {
                    killerAchHandler.addProgress("murdermystery.clean_round", 1);
                }

                int aliveNonMurderers = countAliveNonMurderers();
                if (aliveNonMurderers == 1) {
                    killerAchHandler.addProgress("murdermystery.close_enough", 1);
                }

                if (lastMurdererKillTime > 0 && System.currentTimeMillis() - lastMurdererKillTime <= 3000) {
                    killerAchHandler.addProgress("murdermystery.caught_in_the_act", 1);
                }

                if (gameType == MurderMysteryGameType.DOUBLE_UP) {
                    murdererKillMethods.put(killer.getUuid(), killType);
                    long killsOfMurderersByThisPlayer = murdererKillMethods.entrySet().stream()
                            .filter(e -> e.getKey().equals(killer.getUuid()))
                            .count();
                    if (killsOfMurderersByThisPlayer >= 2) {
                        Set<KillType> usedTypes = new HashSet<>();
                        for (var entry : murdererKillMethods.entrySet()) {
                            if (entry.getKey().equals(killer.getUuid())) {
                                usedTypes.add(entry.getValue());
                            }
                        }
                        if (usedTypes.size() >= 2) {
                            killerAchHandler.addProgress("murdermystery.double_duty", 1);
                        }
                    }
                }
            }
        }

        victim.setEliminated(true);
        victim.setTag(ELIMINATED_TAG, true);
        setupPlayerForSpectator(victim);

        if (replayManager != null) {
            replayManager.recordKill(killer, victim, killType);
        }

        if (gameType == MurderMysteryGameType.ASSASSINS) {
            handleAssassinKill(killer, victim);
        } else {
            handleClassicKill(killer, victim, killerRole, victimRole);
        }

        if (victimRole == GameRole.DETECTIVE) {
            dropDetectiveBow(victim);
        }

        checkWinConditions();
    }

    private void recordKillStats(MurderMysteryPlayer killer, KillType killType) {
        MurderMysteryDataHandler handler = MurderMysteryDataHandler.getUser(killer);
        if (handler == null) return;

        MurderMysteryLeaderboardMode leaderboardMode = MurderMysteryLeaderboardMode.fromGameType(gameType);
        DatapointMurderMysteryModeStats statsDP = handler.get(
                MurderMysteryDataHandler.Data.MODE_STATS,
                DatapointMurderMysteryModeStats.class);
        var stats = statsDP.getValue();

        GameRole killerRole = roleManager.getRole(killer.getUuid());
        if (killerRole == GameRole.MURDERER) {
            stats.recordKillAsMurderer(leaderboardMode);
        }

        switch (killType) {
            case BOW -> stats.recordBowKill(leaderboardMode);
            case KNIFE -> stats.recordKnifeKill(leaderboardMode);
            case THROWN_KNIFE -> stats.recordThrownKnifeKill(leaderboardMode);
            case TRAP -> stats.recordTrapKill(leaderboardMode);
        }
    }

    public enum KillType {
        BOW, KNIFE, THROWN_KNIFE, TRAP
    }

    private void dropDetectiveBow(MurderMysteryPlayer detective) {
        Pos deathPos = detective.getPosition();

        Entity bowEntity = new Entity(EntityType.ITEM);
        ItemEntityMeta meta = (ItemEntityMeta) bowEntity.getEntityMeta();
        meta.setItem(ItemStack.of(Material.BOW));
        bowEntity.setInstance(getInstanceContainer(), deathPos);
        droppedDetectiveBow = bowEntity;
        detectiveBowPickedUp = false;

        if (replayManager != null) {
            replayManager.recordBowDrop();
        }

        broadcastMessage(Component.empty()
                .append(Component.text("The Bow has been dropped! ", NamedTextColor.GOLD))
                .append(Component.text("Find the Bow for a chance to kill the Murderer.", NamedTextColor.YELLOW)));
    }

    public boolean isDroppedDetectiveBow(Entity entity) {
        return droppedDetectiveBow != null && droppedDetectiveBow.equals(entity);
    }

    public void onDetectiveBowPickedUp(MurderMysteryPlayer player) {
        if (droppedDetectiveBow != null) {
            droppedDetectiveBow.remove();
            droppedDetectiveBow = null;
            detectiveBowPickedUp = true;

            weaponManager.giveInnocentBow(player);

            if (replayManager != null) {
                replayManager.recordBowPickup(player);
            }

            broadcastMessage(Component.text("A player has picked up the Bow!", NamedTextColor.YELLOW));
        }
    }

    public void sendDeathMessage(MurderMysteryPlayer player, String reason) {
        player.sendMessage(Component.empty()
                .append(Component.text("YOU DIED! ", NamedTextColor.RED))
                .append(Component.text(reason, NamedTextColor.YELLOW)));
    }

    private void handleClassicKill(MurderMysteryPlayer killer, MurderMysteryPlayer victim,
                                   GameRole killerRole, GameRole victimRole) {
        if (killerRole == GameRole.MURDERER) {
            sendDeathMessage(victim, "You were killed by the Murderer.");
        } else {
            if (victimRole == GameRole.MURDERER) {
                broadcastMessage(Component.text(killer.getUsername() + " killed the murderer!", NamedTextColor.GREEN));
                sendDeathMessage(victim, "You were identified and eliminated.");
            } else {
                sendDeathMessage(victim, "You were shot by " + killer.getUsername() + ".");

                killer.setEliminated(true);
                killer.setTag(ELIMINATED_TAG, true);
                setupPlayerForSpectator(killer);
                broadcastMessage(Component.text(killer.getUsername() + " killed an innocent and was struck by lightning!", NamedTextColor.RED));
                sendDeathMessage(killer, "You killed an innocent player.");

                GameRole killerRole2 = roleManager.getRole(killer.getUuid());
                if (killerRole2 == GameRole.DETECTIVE) {
                    dropDetectiveBow(killer);
                }
            }
        }
    }

    private void handleAssassinKill(MurderMysteryPlayer killer, MurderMysteryPlayer victim) {
        UUID targetUuid = roleManager.getAssassinTarget(killer.getUuid());
        if (victim.getUuid().equals(targetUuid)) {
            killer.sendMessage(Component.text("Target eliminated! New target assigned.", NamedTextColor.GREEN));
            UUID newTarget = roleManager.getAssassinTarget(victim.getUuid());
            roleManager.reassignTarget(killer.getUuid(), newTarget);

            killer.getQuestHandler().addProgressByTrigger("murdermystery.assassin_target_kills", 1);

            PlayerAchievementHandler achHandler = new PlayerAchievementHandler(killer);

            achHandler.addProgress("murdermystery.hitman", 1);

            if (killer.getKillsInLast5Seconds() >= 2) {
                achHandler.addProgress("murdermystery.sixth_sense", 1);
            }
        } else {
            killer.setEliminated(true);
            killer.setTag(ELIMINATED_TAG, true);
            setupPlayerForSpectator(killer);
            broadcastMessage(Component.text(killer.getUsername() + " attacked the wrong person!", NamedTextColor.RED));
        }
    }

    @Override
    public void checkWinConditions() {
        if (getState() != GameState.IN_PROGRESS) return;

        if (gameType == MurderMysteryGameType.ASSASSINS) {
            int aliveCount = countAlivePlayers();
            if (aliveCount <= 1) {
                endGame(WinCondition.LAST_STANDING);
            }
        } else {
            int aliveMurderers = roleManager.countAliveWithRole(GameRole.MURDERER);
            int aliveInnocents = roleManager.countAliveWithRole(GameRole.INNOCENT)
                    + roleManager.countAliveWithRole(GameRole.DETECTIVE);

            if (aliveMurderers == 0) {
                endGame(WinCondition.INNOCENTS_WIN);
            } else if (aliveInnocents == 0) {
                endGame(WinCondition.MURDERER_WINS);
            }
        }
    }

    private void endGame(WinCondition condition) {
        setState(GameState.ENDING);
        goldManager.stopSpawning();
        if (replayManager != null) {
            replayManager.recordGameEnd(condition.name(), getLastStandingPlayer());
            replayManager.stopRecording();
        }

        recordGameStats(condition);

        for (MurderMysteryPlayer player : getPlayers()) {
            GameRole role = roleManager.getRole(player.getUuid());
            if (role == GameRole.MURDERER) {
                int kills = murdererKillsThisGame.getOrDefault(player.getUuid(), 0);
                if (kills >= 5) {
                    player.getQuestHandler().addProgressByTrigger("murdermystery.murder_spree", 1);
                }
            }
        }

        if (gameType == MurderMysteryGameType.ASSASSINS) {
            MurderMysteryPlayer topKiller = null;
            int maxKills = 0;
            boolean tied = false;

            for (MurderMysteryPlayer player : getPlayers()) {
                int kills = player.getKillsThisGame();
                if (kills > maxKills) {
                    maxKills = kills;
                    topKiller = player;
                    tied = false;
                } else if (kills == maxKills && kills > 0) {
                    tied = true;
                }
            }

            if (topKiller != null && !tied && maxKills > 0) {
                topKiller.getQuestHandler().addProgressByTrigger("murdermystery.serial_killer", 1);
            }
        }

        long elapsedMs = System.currentTimeMillis() - gameStartTime;
        long remainingMs = (5 * 60 * 1000) - elapsedMs;
        boolean innocentsWon = (condition == WinCondition.INNOCENTS_WIN || condition == WinCondition.TIME_EXPIRED);
        boolean murdererWon = (condition == WinCondition.MURDERER_WINS);

        for (MurderMysteryPlayer player : getPlayers()) {
            GameRole role = roleManager.getRole(player.getUuid());
            if (role == null) continue;

            PlayerAchievementHandler achHandler = new PlayerAchievementHandler(player);

            if (murdererWon && role == GameRole.MURDERER) {
                achHandler.addProgress("murdermystery.youre_all_mine", 1);

                if (remainingMs <= 15000) {
                    achHandler.addProgress("murdermystery.calculated", 1);
                }

                if (remainingMs >= 120000) {
                    achHandler.addProgress("murdermystery.uncalculated", 1);
                }
            }

            if (innocentsWon && (role == GameRole.INNOCENT || role == GameRole.DETECTIVE)) {
                if (!player.isEliminated()) {
                    achHandler.addProgress("murdermystery.peace_is_mine", 1);
                }

                if (condition == WinCondition.TIME_EXPIRED) {
                    if (role == GameRole.INNOCENT && !player.isEliminated()) {
                        achHandler.addProgress("murdermystery.catch_me_if_you_can", 1);
                    }

                    if (role == GameRole.INNOCENT && !player.isEliminated() && !player.isHasCollectedGoldThisGame()) {
                        achHandler.addProgress("murdermystery.no_money_no_problems", 1);
                    }
                }
            }

            if (murdererKiller != null && murdererKiller.getUuid().equals(player.getUuid())) {
                GameRole heroRole = roleManager.getRole(murdererKiller.getUuid());
                if (heroRole != GameRole.DETECTIVE) {
                    achHandler.addProgress("murdermystery.saving_the_day", 1);
                }

                if (gameType != MurderMysteryGameType.ASSASSINS) {
                    achHandler.addProgress("murdermystery.countermeasures", 1);
                }
            }

            if (role == GameRole.MURDERER || role == GameRole.DETECTIVE) {
                GameRole lastRole = player.getLastGameRole();
                if (lastRole != null && (lastRole == GameRole.MURDERER || lastRole == GameRole.DETECTIVE)) {
                    achHandler.addProgress("murdermystery.i_am_special", 1);
                }
            }
            player.setLastGameRole(role);
        }

        murdererKillsThisGame.clear();

        String message = switch (condition) {
            case INNOCENTS_WIN -> "The Innocents have won!";
            case MURDERER_WINS -> "The Murderer has won!";
            case TIME_EXPIRED -> "Time's up! Innocents survived!";
            case LAST_STANDING -> getLastStandingPlayer() != null ?
                    getLastStandingPlayer().getUsername() + " is the last one standing!" :
                    "Game Over!";
        };

        NamedTextColor color = switch (condition) {
            case INNOCENTS_WIN, TIME_EXPIRED -> NamedTextColor.GREEN;
            case MURDERER_WINS -> NamedTextColor.RED;
            case LAST_STANDING -> NamedTextColor.GOLD;
        };

        Title title = Title.title(
                Component.text(message, color),
                Component.empty(),
                Title.Times.times(Duration.ofMillis(500), Duration.ofSeconds(5), Duration.ofMillis(500))
        );
        getPlayers().forEach(p -> p.showTitle(title));

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            sendGameResults(condition);
        }).delay(TaskSchedule.seconds(2)).schedule();

        MinecraftServer.getSchedulerManager().buildTask(() -> {
            List<MurderMysteryPlayer> playersToRemove = new ArrayList<>(getPlayers());
            for (MurderMysteryPlayer player : playersToRemove) {
                leave(player);
            }
            roleManager.clear();
            murdererKiller = null;

            resetInstance();

            gameStartTime = 0;
            setState(GameState.WAITING);
        }).delay(TaskSchedule.seconds(10)).schedule();
    }

    private void sendGameResults(WinCondition condition) {
        String thickBar = "§a§l▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬▬";

        for (MurderMysteryPlayer player : getPlayers()) {
            player.sendMessage(Component.text(thickBar));
            player.sendMessage(Component.text("                    ")
                    .append(Component.text("MURDER MYSTERY", NamedTextColor.WHITE, TextDecoration.BOLD)));

            if (gameType == MurderMysteryGameType.ASSASSINS) {
                MurderMysteryPlayer winner = getLastStandingPlayer();
                if (winner != null) {
                    player.sendMessage(Component.text("              ")
                            .append(Component.text("Winner: ", NamedTextColor.WHITE, TextDecoration.BOLD))
                            .append(Component.text(winner.getUsername(), NamedTextColor.GOLD)));

                    player.sendMessage(Component.empty());
                    player.sendMessage(Component.text(" §7Last Standing: ")
                            .append(Component.text(winner.getFullDisplayName()))
                            .append(Component.text(" §7(§6" + winner.getKillsThisGame() + "§7 kills)")));
                } else {
                    player.sendMessage(Component.text("              ")
                            .append(Component.text("Winner: ", NamedTextColor.WHITE, TextDecoration.BOLD))
                            .append(Component.text("None", NamedTextColor.GRAY)));
                }
            } else {
                boolean innocentsWon = (condition == WinCondition.INNOCENTS_WIN || condition == WinCondition.TIME_EXPIRED);
                player.sendMessage(Component.text("         ")
                        .append(Component.text("Winner: ", NamedTextColor.WHITE, TextDecoration.BOLD))
                        .append(innocentsWon ?
                                Component.text("INNOCENTS", NamedTextColor.GREEN) :
                                Component.text("MURDERER", NamedTextColor.RED)));

                player.sendMessage(Component.empty());

                MurderMysteryPlayer detective = roleManager.getPlayersWithRole(GameRole.DETECTIVE).stream().findFirst().orElse(null);
                if (detective != null) {
                    Component detectiveName = detective.isEliminated() ?
                            Component.text(detective.getFullDisplayName()).decorate(TextDecoration.STRIKETHROUGH) :
                            Component.text(detective.getFullDisplayName());
                    player.sendMessage(Component.text(" §7Detective: ").append(detectiveName));
                }

                MurderMysteryPlayer murderer = roleManager.getPlayersWithRole(GameRole.MURDERER).stream().findFirst().orElse(null);
                if (murderer != null) {
                    Component murdererName = murderer.isEliminated() ?
                            Component.text(murderer.getFullDisplayName()).decorate(TextDecoration.STRIKETHROUGH) :
                            Component.text(murderer.getFullDisplayName());
                    player.sendMessage(Component.text(" §7Murderer: ")
                            .append(murdererName)
                            .append(Component.text(" §7(§6" + murderer.getKillsThisGame() + "§7 kills)")));
                }

                if (murdererKiller != null && roleManager.getRole(murdererKiller.getUuid()) != GameRole.DETECTIVE) {
                    player.sendMessage(Component.text(" §7Hero: ")
                            .append(Component.text(murdererKiller.getFullDisplayName())));
                }
            }

            player.sendMessage(Component.text(thickBar));

            player.sendMessage(Component.text("                 ")
                    .append(Component.text("Reward Summary", NamedTextColor.WHITE, TextDecoration.BOLD)));
            player.sendMessage(Component.text("   §7You earned:"));
            player.sendMessage(Component.text("   §a+" + player.getTokensEarnedThisGame() + " Murder Mystery Tokens"));
            player.sendMessage(Component.text("   §b+267 Hypixel Experience"));

            player.sendMessage(Component.text(thickBar));

            PlayerExperienceHandler expHandler = new PlayerExperienceHandler(player);
            expHandler.addExperience(267);
        }
    }

    private void recordGameStats(WinCondition condition) {
        MurderMysteryLeaderboardMode leaderboardMode = MurderMysteryLeaderboardMode.fromGameType(gameType);
        long gameDuration = System.currentTimeMillis() - gameStartTime;

        boolean innocentsWon = (condition == WinCondition.INNOCENTS_WIN || condition == WinCondition.TIME_EXPIRED);
        boolean murdererWon = (condition == WinCondition.MURDERER_WINS);

        for (MurderMysteryPlayer player : getPlayers()) {
            MurderMysteryDataHandler handler = MurderMysteryDataHandler.getUser(player);
            if (handler == null) continue;

            DatapointMurderMysteryModeStats statsDP = handler.get(
                    MurderMysteryDataHandler.Data.MODE_STATS,
                    DatapointMurderMysteryModeStats.class);
            var stats = statsDP.getValue();

            stats.recordGamePlayed(leaderboardMode);

            GameRole role = roleManager.getRole(player.getUuid());
            if (role == null) continue;

            if (gameType == MurderMysteryGameType.ASSASSINS) {
                MurderMysteryPlayer winner = getLastStandingPlayer();
                if (winner != null && winner.getUuid().equals(player.getUuid())) {
                    stats.recordWin(leaderboardMode);

                    player.getQuestHandler().addProgressByTrigger("murdermystery.games_won", 1);
                }
            } else {
                if (innocentsWon && (role == GameRole.INNOCENT || role == GameRole.DETECTIVE)) {
                    if (role == GameRole.DETECTIVE) {
                        stats.recordDetectiveWin(leaderboardMode);
                        stats.setQuickestDetectiveWin(leaderboardMode, gameDuration);
                    } else {
                        stats.recordWin(leaderboardMode);
                    }

                    player.getQuestHandler().addProgressByTrigger("murdermystery.games_won", 1);

                    if (murdererKiller != null && murdererKiller.getUuid().equals(player.getUuid())
                            && role != GameRole.DETECTIVE) {
                        stats.recordKillAsHero(leaderboardMode);
                    }
                } else if (murdererWon && role == GameRole.MURDERER) {
                    stats.recordMurdererWin(leaderboardMode);
                    stats.setQuickestMurdererWin(leaderboardMode, gameDuration);

                    player.getQuestHandler().addProgressByTrigger("murdermystery.games_won", 1);
                    player.getQuestHandler().addProgressByTrigger("murdermystery.power_play", 1);
                }
            }
        }
    }

    @Override
    public boolean hasMinimumPlayers() {
        return getPlayers().size() >= gameType.getMinPlayers();
    }

    private int countAlivePlayers() {
        return (int) getPlayers().stream()
                .filter(p -> !p.isEliminated())
                .count();
    }

    private int countAliveNonMurderers() {
        return (int) getPlayers().stream()
                .filter(p -> !p.isEliminated())
                .filter(p -> roleManager.getRole(p.getUuid()) != GameRole.MURDERER)
                .count();
    }

    private MurderMysteryPlayer getLastStandingPlayer() {
        return getPlayers().stream()
                .filter(p -> !p.isEliminated())
                .findFirst()
                .orElse(null);
    }

    public void forceStart() {
        forceStart(5);
    }

    public void forceStart(int seconds) {
        if (getState() != GameState.WAITING && getState() != GameState.COUNTDOWN) return;
        if (!hasMinimumPlayers()) return;
        if (!getCountdown().isActive() && !getCountdown().start()) return;
        setState(GameState.COUNTDOWN);
        forceCountdownAnnouncements = true;
        broadcastMessage(Component.text("Game force started! Starting in " + seconds + " seconds!", NamedTextColor.GREEN));
        getCountdown().setRemainingSeconds(seconds);
    }

    public Audience getPlayersAsAudience() {
        return Audience.audience(getPlayers().stream().map(MurderMysteryPlayer::getServerPlayer).toList());
    }

    public InstanceContainer getInstanceContainer() {
        return (InstanceContainer) getInstance();
    }

    public List<UUID> getDisconnectedPlayerUuids() {
        return new ArrayList<>(disconnectedPlayers.keySet());
    }

    public void broadcastMessage(Component message) {
        getPlayersAsAudience().sendMessage(message);
        if (replayManager != null) replayManager.recordAnnouncement(message);
    }

    public void setBlock(Point position, Block block) {
        Block previous = getInstanceContainer().getBlock(position);
        getInstanceContainer().setBlock(position, block);
        if (replayManager != null) {
            replayManager.recordBlockChange(position.blockX(), position.blockY(), position.blockZ(),
                    previous.stateId(), block.stateId());
        }
    }

    public void playSound(Sound sound, Pos position) {
        getInstanceContainer().playSound(sound, position);
        if (replayManager != null) {
            replayManager.recordSound(sound, position.x(), position.y(), position.z());
        }
    }

    @lombok.SneakyThrows
    private void resetInstance() {
        // Remove all entities from the instance (dropped items, etc.)
        for (Entity entity : getInstanceContainer().getEntities()) {
            if (!(entity instanceof Player)) {
                entity.remove();
            }
        }

        // Reload the map from the polar file by unloading all chunks
        // When chunks are re-loaded, they'll come fresh from the PolarLoader
        PolarLoader loader = new PolarLoader(new File("./configuration/murdermystery/" + mapEntry.getId() + ".polar").toPath());
        getInstanceContainer().setChunkLoader(loader);

        // Unload all chunks so they reload fresh from the polar file
        getInstanceContainer().getChunks().forEach(getInstanceContainer()::unloadChunk);
    }

    private enum WinCondition {
        INNOCENTS_WIN, MURDERER_WINS, TIME_EXPIRED, LAST_STANDING
    }

    /**
     * Checks if the game can accept new players (party warp validation).
     * @return true if game is in WAITING state and can accept players
     */
    public boolean canAcceptNewPlayers() {
        return getState() == GameState.WAITING || getState() == GameState.COUNTDOWN;
    }

    /**
     * Gets the number of available slots in this game.
     * @return number of slots available for new players
     */
    public int getAvailableSlots() {
        return Math.max(0, gameType.getMaxPlayers() - getPlayers().size());
    }

    /**
     * Checks if the game can accept a party warp and returns an error message if not.
     * @return null if warp is allowed, otherwise an error message
     */
    public String canAcceptPartyWarp() {
        if (getState() == GameState.IN_PROGRESS) {
            return "Cannot warp - game has already started";
        }
        if (getState() == GameState.ENDING) {
            return "Cannot warp - game is ending";
        }
        return null; // Warp is allowed
    }
}
