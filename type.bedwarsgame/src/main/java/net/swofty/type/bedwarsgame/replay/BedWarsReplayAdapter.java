package net.swofty.type.bedwarsgame.replay;

import io.github.term4.polyp.entity.PrimedTnt;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.ItemEntity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.Player;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.item.ItemStack;
import net.minestom.server.potion.TimedPotion;
import net.swofty.commons.replay.protocol.ReplayDataReader;
import net.swofty.commons.replay.protocol.ReplayDataWriter;
import net.swofty.type.bedwarsgame.game.BedWarsGame;
import net.swofty.type.bedwarsgame.game.BedWarsTeam;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.game.replay.ReplayRecorder;
import net.swofty.type.game.replay.api.ReplayGameAdapter;
import net.swofty.type.game.replay.bedwars.BedWarsReplayMetadata;
import net.swofty.type.game.replay.bedwars.BedWarsReplayState;
import net.swofty.type.game.replay.delta.ReplayEntityUpsertDelta;
import net.swofty.type.game.replay.delta.ReplayGameStateDelta;
import net.swofty.type.game.replay.model.ReplayEntityState;
import net.swofty.type.game.replay.model.ReplayPotionEffectState;
import net.swofty.type.game.replay.model.ReplaySnapshot;
import net.swofty.type.generic.entity.npc.impl.NPCViewable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public final class BedWarsReplayAdapter implements ReplayGameAdapter<BedWarsReplayMetadata, BedWarsReplayState> {
    public static final String GAME_TYPE = "BEDWARS";
    public static final int SCHEMA_VERSION = 2;
    public static final int DELTA_TYPE_ID = 1;
    private static final GsonComponentSerializer COMPONENTS = GsonComponentSerializer.gson();
    private static final LegacyComponentSerializer LEGACY_COMPONENTS = LegacyComponentSerializer.legacySection();
    private final BedWarsGame game;
    private final Set<UUID> deadPlayers = new java.util.HashSet<>();
    private final Set<UUID> dyingPlayers = new java.util.HashSet<>();
    private final Map<Integer, BedWarsReplayState.DisplayState> displays = new LinkedHashMap<>();
    private final Map<Integer, BedWarsReplayState.NpcState> npcs = new LinkedHashMap<>();
    private final Map<String, Integer> generatorTiers = new LinkedHashMap<>();

    public BedWarsReplayAdapter(BedWarsGame game) {
        this.game = game;
    }

    public void markDead(UUID playerUuid) {
        dyingPlayers.remove(playerUuid);
        deadPlayers.add(playerUuid);
    }

    public void markDying(UUID playerUuid) {
        deadPlayers.remove(playerUuid);
        dyingPlayers.add(playerUuid);
    }

    public void markAlive(UUID playerUuid) {
        deadPlayers.remove(playerUuid);
        dyingPlayers.remove(playerUuid);
    }

    @Override
    public String gameType() {
        return GAME_TYPE;
    }

    @Override
    public int metadataSchemaVersion() {
        return SCHEMA_VERSION;
    }

    @Override
    public BedWarsReplayMetadata captureMetadata() {
        List<BedWarsReplayMetadata.TeamDefinition> teams = new ArrayList<>();
        for (BedWarsTeam team : game.getTeams()) {
            var mapTeam = game.getMapEntry().getConfiguration().getTeams().get(team.getTeamKey());
            var bed = mapTeam == null ? null : mapTeam.getBed();
            teams.add(new BedWarsReplayMetadata.TeamDefinition(
                    team.getTeamKey().name(), team.getName(), team.getTeamKey().rgb(), List.copyOf(team.getPlayerIds()),
                    bed == null || bed.feet() == null ? null : new BedWarsReplayMetadata.BlockPosition(
                            (int) bed.feet().x(), (int) bed.feet().y(), (int) bed.feet().z()),
                    bed == null || bed.head() == null ? null : new BedWarsReplayMetadata.BlockPosition(
                            (int) bed.head().x(), (int) bed.head().y(), (int) bed.head().z())));
        }
        List<BedWarsReplayMetadata.GeneratorDefinition> generators = new ArrayList<>();
        var configuredGenerators = game.getMapEntry().getConfiguration().getGlobalGenerator();
        if (configuredGenerators != null) {
            configuredGenerators.forEach((type, definition) -> {
                if (definition.getLocations() == null) return;
                definition.getLocations().forEach(position -> generators.add(new BedWarsReplayMetadata.GeneratorDefinition(
                        type.name(), new BedWarsReplayMetadata.BlockPosition(
                        (int) position.x(), (int) position.y(), (int) position.z()))));
            });
        }
        return new BedWarsReplayMetadata(game.getGameType().name(), teams, generators);
    }

    @Override
    public BedWarsReplayState captureState() {
        Map<String, List<java.util.UUID>> members = new LinkedHashMap<>();
        Map<String, Boolean> beds = new LinkedHashMap<>();
        List<String> eliminated = new ArrayList<>();
        for (BedWarsTeam team : game.getTeams()) {
            members.put(team.getTeamKey().name(), List.copyOf(team.getPlayerIds()));
            beds.put(team.getTeamKey().name(), team.isBedAlive());
            if (!team.getPlayerIds().isEmpty() && team.getPlayerIds().stream().allMatch(uuid -> game.getPlayer(uuid)
                    .map(player -> Boolean.TRUE.equals(player.getTag(BedWarsGame.ELIMINATED_TAG))).orElse(true))) {
                eliminated.add(team.getTeamKey().name());
            }
        }
        return new BedWarsReplayState(members, beds, generatorTiers, List.of(), eliminated, null,
                List.copyOf(displays.values()), List.copyOf(npcs.values()));
    }

    @Override
    public void writeMetadata(ReplayDataWriter writer, BedWarsReplayMetadata metadata) throws IOException {
        writer.writeString(metadata.modeId());
        writer.writeVarInt(metadata.teams().size());
        for (var team : metadata.teams()) {
            writer.writeString(team.id());
            writer.writeString(team.name());
            writer.writeInt(team.color());
            writer.writeVarInt(team.initialMembers().size());
            for (var member : team.initialMembers()) writer.writeUUID(member);
            writePosition(writer, team.bedFeet());
            writePosition(writer, team.bedHead());
        }
        writer.writeVarInt(metadata.generators().size());
        for (var generator : metadata.generators()) {
            writer.writeString(generator.type());
            writePosition(writer, generator.position());
        }
    }

    @Override
    public void writeState(ReplayDataWriter writer, BedWarsReplayState state) throws IOException {
        writer.writeVarInt(state.teamMembers().size());
        for (var team : state.teamMembers().entrySet()) {
            writer.writeString(team.getKey());
            writer.writeVarInt(team.getValue().size());
            for (var member : team.getValue()) writer.writeUUID(member);
        }
        writer.writeVarInt(state.liveBeds().size());
        for (var bed : state.liveBeds().entrySet()) {
            writer.writeString(bed.getKey());
            writer.writeBoolean(bed.getValue());
        }
        writer.writeVarInt(state.generatorTiers().size());
        for (var generator : state.generatorTiers().entrySet()) {
            writer.writeString(generator.getKey());
            writer.writeVarInt(generator.getValue());
        }
        writer.writeVarInt(state.scoreboardJson().size());
        for (String line : state.scoreboardJson()) writer.writeString(line);
        writer.writeVarInt(state.eliminatedTeams().size());
        for (String team : state.eliminatedTeams()) writer.writeString(team);
        writeNullable(writer, state.winnerId());
        writer.writeVarInt(state.displays().size());
        for (var display : state.displays()) {
            writer.writeVarInt(display.entityId());
            writer.writeUUID(display.uuid());
            writer.writeLocation(display.x(), display.y(), display.z(), 0, 0);
            writeStrings(writer, display.lines());
            writer.writeString(display.displayType());
            writer.writeString(display.identifier());
        }
        writer.writeVarInt(state.npcs().size());
        for (var npc : state.npcs()) {
            writer.writeVarInt(npc.entityId());
            writer.writeString(npc.displayName());
            writeStrings(writer, npc.lines());
        }
    }

    public ReplaySnapshot captureSnapshot(ReplayRecorder recorder) {
        try {
            ReplayDataWriter stateWriter = new ReplayDataWriter();
            writeState(stateWriter, captureState());
            Map<Integer, ReplayEntityState> entities = new LinkedHashMap<>();
            for (Entity entity : game.getInstance().getEntities()) {
                if (isReplayVisible(entity)) entities.put(entity.getEntityId(), captureEntity(entity));
            }
            return new ReplaySnapshot(recorder.getCurrentTick(), recorder.snapshotBlockOverlay(), entities, stateWriter.toByteArray());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to capture Bed Wars replay snapshot", exception);
        }
    }

    public ReplayGameStateDelta bedStateDelta(String teamId, boolean live) {
        return gameDelta(writer -> {
            writer.writeByte(1);
            writer.writeString(teamId);
            writer.writeBoolean(live);
        });
    }

    public ReplayGameStateDelta teamMembershipDelta(java.util.UUID player, String teamId) {
        return gameDelta(writer -> {
            writer.writeByte(2);
            writer.writeUUID(player);
            writer.writeString(teamId);
        });
    }

    public ReplayGameStateDelta teamEliminationDelta(String teamId) {
        return gameDelta(writer -> {
            writer.writeByte(3);
            writer.writeString(teamId);
        });
    }

    public ReplayGameStateDelta generatorTierDelta(byte generatorType, byte tier) {
        generatorTiers.put(Byte.toString(generatorType), (int) tier);
        return gameDelta(writer -> {
            writer.writeByte(4);
            writer.writeByte(generatorType);
            writer.writeByte(tier);
        });
    }

    public ReplayGameStateDelta displayCreateDelta(int entityId, UUID uuid, Pos position, List<String> lines,
                                                   String displayType, String identifier) {
        List<String> componentJson = encodeComponents(lines);
        displays.put(entityId, new BedWarsReplayState.DisplayState(entityId, uuid, position.x(), position.y(),
                position.z(), componentJson, displayType, identifier));
        return gameDelta(writer -> {
            writer.writeByte(10);
            writer.writeVarInt(entityId);
            writer.writeUUID(uuid);
            writer.writeLocation(position.x(), position.y(), position.z(), position.yaw(), position.pitch());
            writeStrings(writer, componentJson);
            writer.writeString(displayType);
            writer.writeString(identifier);
        });
    }

    public ReplayGameStateDelta displayUpdateDelta(int entityId, List<String> lines, boolean replaceAll, int startIndex) {
        List<String> componentJson = encodeComponents(lines);
        BedWarsReplayState.DisplayState current = displays.get(entityId);
        if (current != null) {
            List<String> updated = new ArrayList<>(current.lines());
            if (replaceAll) updated = new ArrayList<>(componentJson);
            else {
                while (updated.size() < startIndex + componentJson.size())
                    updated.add(COMPONENTS.serialize(Component.empty()));
                for (int index = 0; index < componentJson.size(); index++)
                    updated.set(startIndex + index, componentJson.get(index));
            }
            displays.put(entityId, new BedWarsReplayState.DisplayState(entityId, current.uuid(), current.x(), current.y(),
                    current.z(), updated, current.displayType(), current.identifier()));
        }
        return gameDelta(writer -> {
            writer.writeByte(11);
            writer.writeVarInt(entityId);
            writeStrings(writer, componentJson);
            writer.writeBoolean(replaceAll);
            writer.writeVarInt(startIndex);
        });
    }

    public ReplayGameStateDelta npcPresentationDelta(int entityId, String displayName, List<String> lines) {
        String displayNameJson = COMPONENTS.serialize(LEGACY_COMPONENTS.deserialize(displayName));
        List<String> componentJson = encodeComponents(lines);
        npcs.put(entityId, new BedWarsReplayState.NpcState(entityId, displayNameJson, componentJson));
        return gameDelta(writer -> {
            writer.writeByte(12);
            writer.writeVarInt(entityId);
            writer.writeString(displayNameJson);
            writeStrings(writer, componentJson);
        });
    }

    public ReplayEntityUpsertDelta syntheticNpc(int entityId, UUID uuid, int entityTypeId, Pos position,
                                                String textureValue, String textureSignature) {
        ReplayEntityState.PlayerState player = entityTypeId == EntityType.PLAYER.id()
                ? new ReplayEntityState.PlayerState(uuid, textureValue, textureSignature,
                COMPONENTS.serialize(Component.text("NPC")), null, GameMode.CREATIVE.ordinal(), false, new byte[0])
                : null;
        return new ReplayEntityUpsertDelta(new ReplayEntityState(entityId, uuid, entityTypeId,
                position.x(), position.y(), position.z(), position.yaw(), position.pitch(), 0, 0, 0,
                0, true, false, 0, ReplayEntityState.Lifecycle.ALIVE, Map.of(), 20, 20,
                List.of(), player, new byte[0]));
    }

    private ReplayGameStateDelta gameDelta(DeltaWriter value) {
        try {
            ReplayDataWriter writer = new ReplayDataWriter();
            value.write(writer);
            return new ReplayGameStateDelta(DELTA_TYPE_ID, writer.toByteArray());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to encode Bed Wars replay delta", exception);
        }
    }

    public static BedWarsReplayMetadata readMetadata(ReplayDataReader reader) throws IOException {
        String mode = reader.readString();
        int count = checkedCount(reader.readVarInt(), 32, "teams");
        List<BedWarsReplayMetadata.TeamDefinition> teams = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            String id = reader.readString();
            String name = reader.readString();
            int color = reader.readInt();
            int memberCount = checkedCount(reader.readVarInt(), 1024, "team members");
            List<java.util.UUID> members = new ArrayList<>(memberCount);
            for (int member = 0; member < memberCount; member++) members.add(reader.readUUID());
            teams.add(new BedWarsReplayMetadata.TeamDefinition(id, name, color, members, readPosition(reader), readPosition(reader)));
        }
        int generatorCount = checkedCount(reader.readVarInt(), 4096, "generators");
        List<BedWarsReplayMetadata.GeneratorDefinition> generators = new ArrayList<>(generatorCount);
        for (int index = 0; index < generatorCount; index++) {
            generators.add(new BedWarsReplayMetadata.GeneratorDefinition(reader.readString(), readPosition(reader)));
        }
        return new BedWarsReplayMetadata(mode, teams, generators);
    }

    public static BedWarsReplayState readState(ReplayDataReader reader) throws IOException {
        int teamCount = checkedCount(reader.readVarInt(), 32, "teams");
        Map<String, List<java.util.UUID>> members = new LinkedHashMap<>();
        for (int team = 0; team < teamCount; team++) {
            String teamId = reader.readString();
            int memberCount = checkedCount(reader.readVarInt(), 1024, "team members");
            List<java.util.UUID> teamMembers = new ArrayList<>(memberCount);
            for (int member = 0; member < memberCount; member++) teamMembers.add(reader.readUUID());
            members.put(teamId, teamMembers);
        }
        int bedCount = checkedCount(reader.readVarInt(), 32, "beds");
        Map<String, Boolean> beds = new LinkedHashMap<>();
        for (int index = 0; index < bedCount; index++) beds.put(reader.readString(), reader.readBoolean());
        int generatorCount = checkedCount(reader.readVarInt(), 1024, "generators");
        Map<String, Integer> generators = new LinkedHashMap<>();
        for (int index = 0; index < generatorCount; index++) generators.put(reader.readString(), reader.readVarInt());
        int scoreboardCount = checkedCount(reader.readVarInt(), 15, "scoreboard lines");
        List<String> scoreboard = new ArrayList<>(scoreboardCount);
        for (int index = 0; index < scoreboardCount; index++) scoreboard.add(reader.readString());
        int eliminatedCount = checkedCount(reader.readVarInt(), 32, "eliminated teams");
        List<String> eliminated = new ArrayList<>(eliminatedCount);
        for (int index = 0; index < eliminatedCount; index++) eliminated.add(reader.readString());
        String winner = readNullable(reader);
        int displayCount = checkedCount(reader.readVarInt(), 4096, "displays");
        List<BedWarsReplayState.DisplayState> displays = new ArrayList<>(displayCount);
        for (int index = 0; index < displayCount; index++) {
            int entityId = reader.readVarInt();
            UUID uuid = reader.readUUID();
            double[] position = reader.readLocation();
            displays.add(new BedWarsReplayState.DisplayState(entityId, uuid, position[0], position[1], position[2],
                    readStrings(reader), reader.readString(), reader.readString()));
        }
        int npcCount = checkedCount(reader.readVarInt(), 4096, "NPCs");
        List<BedWarsReplayState.NpcState> npcs = new ArrayList<>(npcCount);
        for (int index = 0; index < npcCount; index++) {
            npcs.add(new BedWarsReplayState.NpcState(reader.readVarInt(), reader.readString(), readStrings(reader)));
        }
        return new BedWarsReplayState(members, beds, generators, scoreboard, eliminated, winner, displays, npcs);
    }

    public ReplayEntityState captureEntity(Entity entity) {
        var position = entity.getPosition();
        var velocity = entity.getVelocity();
        Map<Integer, byte[]> equipment = new LinkedHashMap<>();
        List<ReplayPotionEffectState> effects = new ArrayList<>();
        float health = 0;
        float maximumHealth = 0;
        if (entity instanceof LivingEntity living) {
            health = living.getHealth();
            maximumHealth = (float) living.getAttribute(Attribute.MAX_HEALTH).getValue();
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack item = living.getEquipment(slot);
                if (!item.isAir()) equipment.put(slot.ordinal(), BedWarsReplayManager.serializeItemStack(item));
            }
            for (TimedPotion timed : living.getActiveEffects()) {
                var potion = timed.potion();
                byte flags = (byte) ((potion.isAmbient() ? 1 : 0) | (potion.hasParticles() ? 2 : 0) | (potion.hasIcon() ? 4 : 0));
                effects.add(new ReplayPotionEffectState(potion.effect().id(), (byte) potion.amplifier(), potion.duration(), flags));
            }
        }
        ReplayEntityState.PlayerState playerState = null;
        ReplayEntityState.Lifecycle lifecycle = ReplayEntityState.Lifecycle.ALIVE;
        if (entity instanceof Player player) {
            BedWarsPlayer bedWarsPlayer = player instanceof BedWarsPlayer value ? value : null;
            boolean eliminated = bedWarsPlayer != null && Boolean.TRUE.equals(bedWarsPlayer.getTag(BedWarsGame.ELIMINATED_TAG));
            boolean spectator = player.getGameMode() == GameMode.SPECTATOR;
            boolean dead = deadPlayers.contains(player.getUuid());
            boolean dying = dyingPlayers.contains(player.getUuid());
            lifecycle = eliminated ? ReplayEntityState.Lifecycle.ELIMINATED
                    : dying ? ReplayEntityState.Lifecycle.DYING
                    : dead ? ReplayEntityState.Lifecycle.DEAD_WAITING
                    : spectator ? ReplayEntityState.Lifecycle.SPECTATOR
                    : player.isInvisible() ? ReplayEntityState.Lifecycle.DEAD_WAITING : ReplayEntityState.Lifecycle.ALIVE;
            var skin = player.getSkin();
            String teamId = bedWarsPlayer == null || bedWarsPlayer.getTeamKey() == null ? null : bedWarsPlayer.getTeamKey().name();
            playerState = new ReplayEntityState.PlayerState(
                    player.getUuid(), skin == null ? null : skin.textures(), skin == null ? null : skin.signature(),
                    COMPONENTS.serialize(player.getDisplayName()), teamId, player.getGameMode().ordinal(),
                    spectator && !eliminated && !dead && !dying,
                    BedWarsReplayManager.serializeItemStack(player.getItemInMainHand()));
        }
        byte[] typePayload;
        if (entity instanceof ItemEntity item) {
            typePayload = BedWarsReplayManager.serializeItemStack(item.getItemStack());
        } else if (entity instanceof PrimedTnt tnt) {
            try {
                ReplayDataWriter writer = new ReplayDataWriter();
                writer.writeVarInt(tnt.remainingFuseTicks());
                typePayload = writer.toByteArray();
            } catch (IOException exception) {
                throw new IllegalStateException("Failed to capture TNT replay state", exception);
            }
        } else {
            typePayload = new byte[0];
        }
        int flags = (entity.isSneaking() ? 1 : 0) | (entity.isSprinting() ? 2 : 0);
        return new ReplayEntityState(entity.getEntityId(), entity.getUuid(), entity.getEntityType().id(),
                position.x(), position.y(), position.z(), position.yaw(), position.pitch(),
                velocity.x(), velocity.y(), velocity.z(), entity.getPose().ordinal(), !entity.isInvisible(), entity.isGlowing(), flags,
                lifecycle, equipment, health, maximumHealth, effects, playerState, typePayload);
    }

    public boolean isReplayVisible(Entity entity) {
        return entity instanceof Player || (!(entity instanceof NPCViewable) && !entity.isInvisible());
    }

    private static void writePosition(ReplayDataWriter writer, BedWarsReplayMetadata.BlockPosition position) throws IOException {
        writer.writeBoolean(position != null);
        if (position != null) writer.writeBlockCoords(position.x(), position.y(), position.z());
    }

    private static BedWarsReplayMetadata.BlockPosition readPosition(ReplayDataReader reader) throws IOException {
        if (!reader.readBoolean()) return null;
        int[] position = reader.readBlockCoords();
        return new BedWarsReplayMetadata.BlockPosition(position[0], position[1], position[2]);
    }

    private static void writeNullable(ReplayDataWriter writer, String value) throws IOException {
        writer.writeBoolean(value != null);
        if (value != null) writer.writeString(value);
    }

    private static void writeStrings(ReplayDataWriter writer, List<String> values) throws IOException {
        writer.writeVarInt(values.size());
        for (String value : values) writer.writeString(value);
    }

    private static List<String> readStrings(ReplayDataReader reader) throws IOException {
        int count = checkedCount(reader.readVarInt(), 64, "text lines");
        List<String> values = new ArrayList<>(count);
        for (int index = 0; index < count; index++) values.add(reader.readString());
        return values;
    }

    private static List<String> encodeComponents(List<String> lines) {
        return lines.stream().map(LEGACY_COMPONENTS::deserialize).map(COMPONENTS::serialize).toList();
    }

    private static String readNullable(ReplayDataReader reader) throws IOException {
        return reader.readBoolean() ? reader.readString() : null;
    }

    private static int checkedCount(int value, int maximum, String name) throws IOException {
        if (value < 0 || value > maximum) throw new IOException("Invalid Bed Wars " + name + " count");
        return value;
    }

    @FunctionalInterface
    private interface DeltaWriter {
        void write(ReplayDataWriter writer) throws IOException;
    }
}
