package net.swofty.type.bedwarsgame.game;

import lombok.Getter;
import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.title.TitlePart;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.GameMode;
import net.minestom.server.event.Event;
import net.minestom.server.instance.InstanceContainer;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.tag.Tag;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.ChatUtility;
import net.swofty.commons.ServerType;
import net.swofty.commons.ServiceType;
import net.swofty.commons.bedwars.BedWarsGameType;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig.MapTeam;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig.TeamKey;
import net.swofty.commons.mc.HypixelPosition;
import net.swofty.commons.party.FullParty;
import net.swofty.commons.text.Text;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.bedwarsgame.BedWarsGameScoreboard;
import net.swofty.type.bedwarsgame.TypeBedWarsGameLoader;
import net.swofty.type.bedwarsgame.events.custom.BedDestroyedEvent;
import net.swofty.type.bedwarsgame.replay.BedWarsReplayManager;
import net.swofty.type.bedwarsgame.shop.TeamUpgradeId;
import net.swofty.type.bedwarsgame.shop.TrapId;
import net.swofty.type.bedwarsgame.shop.impl.AxeShopItem;
import net.swofty.type.bedwarsgame.shop.impl.PickaxeShopItem;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.bedwarsgame.user.ExperienceCause;
import net.swofty.type.bedwarsgame.user.TokenCause;
import net.swofty.type.bedwarsgame.util.BedWarsInventoryManipulator;
import net.swofty.type.game.game.AbstractTeamGame;
import net.swofty.type.game.game.CountdownConfig;
import net.swofty.type.game.game.GameState;
import net.swofty.type.game.game.event.GameTeamWinConditionEvent;
import net.swofty.type.game.game.event.PlayerAssignedTeamEvent;
import net.swofty.type.generic.data.datapoints.DatapointBedWarsHotbar;
import net.swofty.type.generic.event.HypixelEventHandler;
import net.swofty.type.generic.game.GameStatTracker;
import net.swofty.type.generic.i18n.HypixelTranslator;
import net.swofty.type.generic.party.PartyManager;
import net.swofty.type.generic.utility.ScheduleUtility;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Getter
public class BedWarsGame extends AbstractTeamGame<BedWarsPlayer, BedWarsTeam> {
    public static final Tag<Boolean> ELIMINATED_TAG = Tag.Boolean("eliminated");

    private final BedWarsMapsConfig.MapEntry mapEntry;
    private final BedWarsGameType gameType;

    private final BedWarsGeneratorManager generatorManager;
    private final BedWarsWorldManager worldManager;
    private final BedWarsRespawnHandler respawnHandler;
    private final BedWarsGameEventManager gameEventManager;
    private final BedWarsReplayManager replayManager;
    private final SwappageManager swappageManager;
    private final OneBlockManager oneBlockManager;
    private final GameStatTracker<BedWarsGameStat> gameStats = new GameStatTracker<>(BedWarsGameStat.class);

    private final Map<TeamKey, Map<Integer, ItemStack>> teamChests = new EnumMap<>(TeamKey.class);
    private final Map<UUID, Map<Integer, ItemStack>> enderChests = new HashMap<>();
    private final Map<UUID, TeamKey> trackers = new HashMap<>();

    public BedWarsGame(
        BedWarsMapsConfig.MapEntry mapEntry,
        InstanceContainer instance,
        BedWarsGameType gameType
    ) {
        // these need to happen before super() is called
        this.mapEntry = mapEntry;
        this.gameType = gameType;

        super(instance, event -> HypixelEventHandler.callCustomEvent((Event) event));

        // Initialize teams from map config, constrained by game mode team count
        List<TeamKey> teamKeys = new ArrayList<>(mapEntry.getConfiguration().getTeams().keySet());
        Collections.shuffle(teamKeys);
        int teamsToRegister = Math.min(gameType.getTeams(), teamKeys.size());
        for (int i = 0; i < teamsToRegister; i++) {
            registerTeam(new BedWarsTeam(teamKeys.get(i)));
        }

        // Initialize managers
        this.generatorManager = new BedWarsGeneratorManager(this);
        this.worldManager = new BedWarsWorldManager(this);
        this.respawnHandler = new BedWarsRespawnHandler(this);
        this.gameEventManager = new BedWarsGameEventManager(this);
        this.swappageManager = new SwappageManager(this);
        this.oneBlockManager = new OneBlockManager(this);

        // Initialize replay with service connection
        ProxyService replayService = new ProxyService(ServiceType.REPLAY);
        this.replayManager = new BedWarsReplayManager(this, replayService);

        // Start scoreboard
        BedWarsGameScoreboard.start(this);
    }

    @Override
    protected CountdownConfig getCountdownConfig() {
        return CountdownConfig.withAcceleration(30, 10, getMaxPlayers());
    }

    @Override
    public int getMaxPlayers() {
        // During construction (before teams are registered) getTeams() is empty; fall
        // back to the game type's team count so the countdown gets a sane max-player value.
        int teamCount = getTeams().isEmpty() ? gameType.getTeams() : getTeams().size();
        return teamCount * getTeamSize();
    }

    @Override
    public int getMinPlayers() {
        return getTeamSize() * Math.min(2, getTeams().size());
    }

    @Override
    protected int getTeamSize() {
        return Math.max(1, gameType.getTeamSize());
    }

    public boolean isBedAlive(TeamKey teamKey) {
        return getTeam(teamKey.name())
            .map(BedWarsTeam::isBedAlive)
            .orElse(false);
    }

    public boolean isPlayerCurrentlyPlaying(UUID playerUuid) {
        BedWarsPlayer player = getPlayer(playerUuid).get();
        return getPlayerTeam(playerUuid).isPresent()
            && !Boolean.TRUE.equals(player.getTag(ELIMINATED_TAG))
            && !(player.getGameMode() == GameMode.ADVENTURE || player.getGameMode() == GameMode.SPECTATOR)
            && !player.isInvisible();
    }

    /**
     * Gets players on a specific team.
     */
    public List<BedWarsPlayer> getPlayersOnTeam(TeamKey teamKey) {
        return getPlayers().stream()
            .filter(p -> teamKey.equals(p.getTeamKey()))
            .toList();
    }

    /**
     * Gets team traps for a team.
     */
    public List<TrapId> getTeamTraps(TeamKey teamKey) {
        return getTeam(teamKey.name())
            .map(BedWarsTeam::getTraps)
            .orElse(List.of());
    }

    public void equipTeamArmor(BedWarsPlayer player, TeamKey teamKey) {
        respawnHandler.equipTeamArmor(player, teamKey);
    }

    public void updatePlayerHealthDisplay(BedWarsPlayer player) {
        if (player.getBelowNameTag() == null) {
            player.setBelowNameTag("health", "<c>❤");
        }
        int health = (int) (player.getHealth() + player.getAdditionalHearts());
        player.getBelowNameTag().updateScore(player, health);
    }

    public void addTeamTrap(TeamKey teamKey, TrapId trapId) {
        getTeam(teamKey.name()).ifPresent(team -> team.addTrap(trapId));
    }

    public void swapTeamState(BedWarsTeam first, BedWarsTeam second) {
        first.swapState(second);
        Map<Integer, ItemStack> firstChest = teamChests.remove(first.getTeamKey());
        Map<Integer, ItemStack> secondChest = teamChests.remove(second.getTeamKey());
        if (secondChest != null) teamChests.put(first.getTeamKey(), secondChest);
        if (firstChest != null) teamChests.put(second.getTeamKey(), firstChest);
    }

    public int getTeamUpgradeLevel(TeamKey teamKey, TeamUpgradeId upgradeId) {
        return getTeam(teamKey.name())
            .map(team -> team.getUpgradeLevel(upgradeId))
            .orElse(0);
    }

    public void onPlayerRejoin(BedWarsPlayer player, DisconnectedPlayerData data) {
        restorePlayerData(player, data.savedData());
        player.setInstance(instance);

        Optional<BedWarsTeam> teamOpt = getPlayerTeam(player.getUuid());
        if (teamOpt.isEmpty()) {
            player.sendTo(ServerType.BEDWARS_LOBBY);
            return;
        }

        BedWarsTeam team = teamOpt.get();
        player.setTeamName(team.getTeamKey());

        broadcastMessage("<color:{}>{} <7>reconnected.", team.getColor(), player.getUsername());

        if (!team.isBedAlive()) {
            setupAsSpectator(player);
        } else {
            respawnHandler.startRespawn(player);
        }
    }

    // find a better place for this method
    public static void literalSetupSpectator(BedWarsPlayer player) {
        player.setGameMode(GameMode.ADVENTURE);
        player.setInvisible(true);
        player.setFlying(true);
        player.getInventory().clear();
    }

    public void setupPlayer(BedWarsPlayer player) {
        TeamKey teamKey = player.getTeamKey();
        MapTeam playerTeam = getMapEntry().getConfiguration().getTeams().get(teamKey);
        HypixelPosition spawnPos = playerTeam.getSpawn();
        Pos spawnPoint = new Pos(spawnPos.x(), spawnPos.y(), spawnPos.z(), spawnPos.yaw(), spawnPos.pitch());

        player.getInventory().clear();
        player.clearTitle();
        player.setVelocity(Vec.ZERO); // prevent high velocity movement on respawn
        player.teleport(spawnPoint);
        player.setRespawnPoint(spawnPoint);
        player.setGameMode(GameMode.SURVIVAL);
        player.setInvisible(false);
        player.setInvulnerable(true);
        player.setFlying(false);
        BedWarsInventoryManipulator.addItemWithHotbarPriority(player, ItemStack.of(Material.WOODEN_SWORD), DatapointBedWarsHotbar.HotbarItemType.MELEE);
        player.setHealth(20f);
        player.setFood(20);
        equipTeamArmor(player, teamKey);

        // hypixel seems to do something like this
        ScheduleUtility.delay(() -> {
            player.setInvulnerable(false);
        }, TaskSchedule.seconds(1));

        // Give back the downgraded tools
        AxeShopItem axeShopItem = new AxeShopItem();
        Integer currentAxeLevel = player.getTag(AxeShopItem.AXE_UPGRADE_TAG);
        if (currentAxeLevel != null && currentAxeLevel > 0) {
            BedWarsInventoryManipulator.addItemWithHotbarPriority(player,
                ItemStack.of(axeShopItem.getTier(currentAxeLevel - 1).material()),
                DatapointBedWarsHotbar.HotbarItemType.AXE);
        }

        PickaxeShopItem pickaxeShopItem = new PickaxeShopItem();
        Integer currentPickaxeLevel = player.getTag(PickaxeShopItem.PICKAXE_UPGRADE_TAG);
        if (currentPickaxeLevel != null && currentPickaxeLevel > 0) {
            BedWarsInventoryManipulator.addItemWithHotbarPriority(player,
                ItemStack.of(pickaxeShopItem.getTier(currentPickaxeLevel - 1).material()),
                DatapointBedWarsHotbar.HotbarItemType.PICKAXE);
        }

        if (gameType != BedWarsGameType.ONE_EIGHT) {
            BedWarsInventoryManipulator.addItemWithHotbarPriority(player, TypeBedWarsGameLoader.getItemHandler().getItem("compass").getItemStack(),
                DatapointBedWarsHotbar.HotbarItemType.COMPASS);
        }

        // equip the player with team armor
        equipTeamArmor(player, teamKey);

        Integer protectionLevel = player.getTag(TeamUpgradeId.REINFORCED_ARMOR.levelTag());
        if (protectionLevel != null) {
            TypeBedWarsGameLoader.getTeamShopManager().getUpgrade(TeamUpgradeId.REINFORCED_ARMOR).applyEffect(this, teamKey, protectionLevel);
        }

        Integer cushionedBootsLevel = player.getTag(TeamUpgradeId.CUSHIONED_BOOTS.levelTag());
        if (cushionedBootsLevel != null) {
            TypeBedWarsGameLoader.getTeamShopManager().getUpgrade(TeamUpgradeId.CUSHIONED_BOOTS).applyEffect(this, teamKey, cushionedBootsLevel);
        }

        Integer sharpnessLevel = player.getTag(TeamUpgradeId.SHARPNESS.levelTag());
        if (sharpnessLevel != null) {
            TypeBedWarsGameLoader.getTeamShopManager().getUpgrade(TeamUpgradeId.SHARPNESS).applyEffect(this, teamKey, sharpnessLevel);
        }
    }

    @Override
    protected boolean canPlayerRejoin(BedWarsPlayer player) {
        return getPlayerTeam(player.getUuid())
            .map(BedWarsTeam::isBedAlive)
            .orElse(false);
    }

    @Override
    protected Map<String, Object> savePlayerData(BedWarsPlayer player) {
        Map<String, Object> data = new HashMap<>();
        data.put("armorLevel", player.getTag(TypeBedWarsGameLoader.ARMOR_LEVEL_TAG));
        return data;
    }

    protected void restorePlayerData(BedWarsPlayer player, Map<String, Object> savedData) {
        Integer armorLevel = (Integer) savedData.get("armorLevel");
        if (armorLevel != null) {
            player.setTag(TypeBedWarsGameLoader.ARMOR_LEVEL_TAG, armorLevel);
        }
    }

    @Override
    protected boolean isTeamViable(BedWarsTeam team) {
        // Team is viable if bed is alive OR has active players OR has rejoinable disconnected players
        if (team.isBedAlive()) return true;

        boolean hasActivePlayers = getPlayers().stream()
            .anyMatch(p -> team.hasPlayer(p.getUuid()) && !Boolean.TRUE.equals(p.getTag(ELIMINATED_TAG)));

        boolean hasRejoinablePlayers = disconnectedPlayers.values().stream()
            .anyMatch(info -> {
                Optional<BedWarsTeam> t = getPlayerTeam(info.uuid());
                return t.isPresent() && t.get().getId().equals(team.getId()) && team.isBedAlive();
            });

        return hasActivePlayers || hasRejoinablePlayers;
    }

    public void onBedDestroyed(TeamKey teamKey, BedWarsPlayer destroyer) {
        getTeam(teamKey.name()).ifPresent(team -> {
            team.destroyBed();
            MapTeam mapTeam = mapEntry.getConfiguration().getTeams().get(teamKey);
            if (mapTeam == null) {
                Logger.error("No map team configuration found for team {}", teamKey.name());
                return;
            }
            BedWarsMapsConfig.TwoBlockPosition bedPos = mapTeam.getBed();
            if (bedPos == null || bedPos.feet() == null || bedPos.head() == null) {
                Logger.error("No bed position found for team {}", teamKey.name());
                return;
            }

            destroyer.getInstance().setBlock(bedPos.feet().asBlockVec(), Block.AIR);
            destroyer.getInstance().setBlock(bedPos.head().asBlockVec(), Block.AIR);

            // Record to replay
            if (replayManager.isRecording()) {
                replayManager.recordBedDestroyed(teamKey, destroyer);
            }

            eventDispatcher.accept(new BedDestroyedEvent(
                    this,
                teamKey,
                destroyer
            ));

            checkWinConditions();
        });
    }

    /**
     * Respawns a team's bed. Usually an admin action, Lucky Block Bed Wars beds can be placed at any location,
     * which isn't supported yet.
     */
    public void respawnBed(TeamKey teamKey) {
        getTeam(teamKey.name()).ifPresent(team -> {
            team.setBedAlive(true);

            // Place the bed blocks back
            worldManager.placeBedForTeam(teamKey, mapEntry.getConfiguration().getTeams().get(teamKey));

            // Record to replay
            if (replayManager.isRecording()) {
                replayManager.recordBedRespawned(teamKey);
            }

            broadcastMessage("<color:{}>Team {}'s <a>bed has been respawned!", teamKey.chatColor(), teamKey.getName());
        });
    }

    public void onPlayerEliminated(BedWarsPlayer player) {
        player.setTag(ELIMINATED_TAG, true);
        setupAsSpectator(player);
        checkWinConditions();

        getPlayerTeam(player.getUuid()).ifPresent(team -> {
            boolean allEliminated = getPlayers().stream()
                .filter(p -> team.hasPlayer(p.getUuid()))
                .allMatch(p -> Boolean.TRUE.equals(p.getTag(ELIMINATED_TAG)));

            boolean allDisconnectedOrEliminated = getPlayers().stream()
                .filter(p -> team.hasPlayer(p.getUuid()))
                .allMatch(p -> Boolean.TRUE.equals(p.getTag(ELIMINATED_TAG)) || !isPlayerCurrentlyPlaying(p.getUuid()));

            if (allEliminated || allDisconnectedOrEliminated) {
                onTeamEliminated(team);
            }
        });
    }

    private void setupAsSpectator(BedWarsPlayer player) {
        player.setGameMode(GameMode.ADVENTURE);
        player.setInvisible(true);
        player.setFlying(true);
        player.setAllowFlying(true);
        updatePlayerHealthDisplay(player);

        if (replayManager.isRecording()) {
            replayManager.recordPlayerInvisibility(player, true);
        }

        HypixelPosition spectatorPos = mapEntry.getConfiguration().getLocations().getSpectator();
        if (spectatorPos != null) {
            player.teleport(new Pos(spectatorPos.x(), spectatorPos.y(), spectatorPos.z()));
        }

        player.sendTitlePart(TitlePart.TITLE, Text.of("<7>SPECTATING").asComponent());
        player.sendTitlePart(TitlePart.SUBTITLE, Text.of("<c>Your bed was destroyed.").asComponent());

        player.getInventory().setItemStack(0, TypeBedWarsGameLoader.getItemHandler().getItem("teleporter").getItemStack());
        player.getInventory().setItemStack(4, TypeBedWarsGameLoader.getItemHandler().getItem("spectator_settings").getItemStack());
        player.getInventory().setItemStack(7, TypeBedWarsGameLoader.getItemHandler().getItem("play_again").getItemStack());
        player.getInventory().setItemStack(8, TypeBedWarsGameLoader.getItemHandler().getItem("leave_game").getItemStack());
    }

    public Map<TeamKey, MapTeam> getActiveTeamConfigs() {
        Map<TeamKey, MapTeam> configs = new EnumMap<>(TeamKey.class);
        Map<TeamKey, MapTeam> allTeamConfigs = mapEntry.getConfiguration().getTeams();

        for (BedWarsTeam team : getActiveTeams()) {
            TeamKey key = team.getTeamKey();
            if (allTeamConfigs.containsKey(key)) {
                configs.put(key, allTeamConfigs.get(key));
            }
        }

        return configs;
    }

    public void teleportPlayersToSpawns() {
        Map<TeamKey, MapTeam> teamConfigs = mapEntry.getConfiguration().getTeams();

        for (BedWarsPlayer player : getPlayers()) {
            getPlayerTeam(player.getUuid()).ifPresent(team -> {
                MapTeam config = teamConfigs.get(team.getTeamKey());
                if (config != null) {
                    setupPlayer(player);
                    player.setDisplayName(
                        "<color:{0}><l>{1} </l><color:{0}>{2}",
                        team.getColor(), team.firstLetter(), player.getUsername()
                    );
                    player.reveal();
                }
            });
        }
    }

    public void startTimePlayedRewards() {
        MinecraftServer.getSchedulerManager().buildTask(() -> {
            if (state != GameState.IN_PROGRESS) return;
            for (BedWarsPlayer player : getPlayers()) {
                if (Boolean.TRUE.equals(player.getTag(ELIMINATED_TAG))) continue; // Skip eliminated spectators
                player.token(TokenCause.TIME_PLAYED);
                player.xp(ExperienceCause.TIME_PLAYED);
            }
        }).delay(TaskSchedule.minutes(1)).repeat(TaskSchedule.minutes(1)).schedule();
    }

    // this is per game type, dreams modes have different looks
    public void sendGameStartMessage() {
        String line = "■".repeat(50);
        List<Text> messages = new ArrayList<>();
        messages.add(Text.of("<a>{}", line));
        messages.add(Text.of("<f><l>{}",
            ChatUtility.FontInfo.center(Text.key(gameType.getGameStartTitleKey()).plain(HypixelTranslator.defaultLocale))));
        messages.add(Text.literal(" "));
        ChatUtility.FontInfo.wrap(Text.key(gameType.getGameStartDescriptionKey()).plain(HypixelTranslator.defaultLocale))
            .forEach(message -> messages.add(Text.of("<e>{}", ChatUtility.FontInfo.center(message))));
        messages.add(Text.literal(" "));
        messages.add(Text.of("<a>{}", line));
        Audience audience = Audience.audience(getPlayers());
        for (Text msg : messages) {
            audience.sendMessage(msg);
        }
    }

    public List<UUID> getDisconnectedPlayerUuids() {
        return new ArrayList<>(disconnectedPlayers.keySet());
    }

    @Override
    public void autoAssignTeams() {
        List<BedWarsPlayer> unassignedPlayers = getPlayers().stream()
            .filter(player -> !playerTeams.containsKey(player.getUuid()))
            .toList();

        if (unassignedPlayers.isEmpty() || getTeams().isEmpty()) {
            return;
        }

        Map<UUID, BedWarsPlayer> playersByUuid = new HashMap<>();
        for (BedWarsPlayer player : unassignedPlayers) {
            playersByUuid.put(player.getUuid(), player);
        }

        List<List<BedWarsPlayer>> groups = new ArrayList<>();
        Set<UUID> processed = new HashSet<>();

        for (BedWarsPlayer player : unassignedPlayers) {
            if (!processed.add(player.getUuid())) {
                continue;
            }

            List<BedWarsPlayer> group = new ArrayList<>();
            FullParty party = PartyManager.getPartyFromPlayer(player);
            if (party != null) {
                for (UUID partyMemberUuid : party.getParticipants()) {
                    BedWarsPlayer member = playersByUuid.get(partyMemberUuid);
                    if (member != null) {
                        group.add(member);
                    }
                }
            }

            if (group.isEmpty()) {
                group.add(player);
            }

            for (BedWarsPlayer member : group) {
                processed.add(member.getUuid());
            }
            groups.add(group);
        }

        Collections.shuffle(groups);
        groups.sort(Comparator.comparingInt((List<BedWarsPlayer> group) -> group.size()).reversed());

        List<BedWarsTeam> shuffledTeams = new ArrayList<>(getTeams());
        Collections.shuffle(shuffledTeams);

        Map<String, Integer> teamPriority = new HashMap<>();
        for (int i = 0; i < shuffledTeams.size(); i++) {
            teamPriority.put(shuffledTeams.get(i).getId(), i);
        }

        for (List<BedWarsPlayer> group : groups) {
            BedWarsTeam targetTeam = selectTargetTeam(group.size(), teamPriority);
            if (targetTeam != null) {
                assignGroupToTeam(group, targetTeam);
                continue;
            }

            for (BedWarsPlayer member : group) {
                BedWarsTeam fallbackTeam = selectTargetTeam(1, teamPriority);
                if (fallbackTeam == null) {
                    break;
                }
                assignPlayerToTeam(member, fallbackTeam);
            }
        }
    }

    private BedWarsTeam selectTargetTeam(int requiredSlots, Map<String, Integer> teamPriority) {
        return getTeams().stream()
            .filter(team -> team.getPlayerCount() + requiredSlots <= getTeamSize())
            .min(Comparator
                .comparingInt(BedWarsTeam::getPlayerCount)
                .thenComparingInt(team -> teamPriority.getOrDefault(team.getId(), Integer.MAX_VALUE)))
            .orElse(null);
    }

    private void assignGroupToTeam(List<BedWarsPlayer> players, BedWarsTeam team) {
        for (BedWarsPlayer player : players) {
            assignPlayerToTeam(player, team);
        }
    }

    private void assignPlayerToTeam(BedWarsPlayer player, BedWarsTeam team) {
        reassignPlayer(player.getUuid(), team);
    }

    public void reassignPlayer(UUID playerId, BedWarsTeam team) {
        getPlayerTeam(playerId).ifPresent(current -> current.removePlayer(playerId));
        team.addPlayer(playerId);
        playerTeams.put(playerId, team.getId());
        getPlayer(playerId).ifPresent(player -> eventDispatcher.accept(new PlayerAssignedTeamEvent<>(
                this, player.getServerPlayer(), team
        )));
    }

    // widens access modifier
    @Override
    public void checkWinConditions() {
        super.checkWinConditions();
    }

    public void forceGameEnd() {
        HypixelEventHandler.callCustomEvent(new GameTeamWinConditionEvent<BedWarsTeam>(this, Optional.empty()));
    }
}
