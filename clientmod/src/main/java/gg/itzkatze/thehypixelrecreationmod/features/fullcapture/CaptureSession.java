package gg.itzkatze.thehypixelrecreationmod.features.fullcapture;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import gg.itzkatze.thehypixelrecreationmod.features.worldexport.LoadedChunkExporter;
import gg.itzkatze.thehypixelrecreationmod.utils.ChatUtils;
import gg.itzkatze.thehypixelrecreationmod.utils.PolarConvert;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.SharedConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.*;
import net.minecraft.world.inventory.ContainerInput;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

final class CaptureSession implements EventSink {
    private static final Path CAPTURE_ROOT = FabricLoader.getInstance().getGameDir().resolve("full-captures");
    private static final DateTimeFormatter FILE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    private static final int CHUNK_INTERVAL_TICKS = 10;
    private static final int INDEX_INTERVAL_TICKS = 600;
    private static final int FLUSH_INTERVAL_TICKS = 20;
    private static final int EVALUATION_TIMEOUT_TICKS = 100;
    private static final int EVALUATION_MIN_CHUNKS = 16;
    private static final double REJOIN_OVERLAP = 0.75;
    private static final double SIMILAR_OVERLAP = 0.5;

    private static final Set<String> IGNORED_PACKETS = Set.of(
            "ClientboundKeepAlivePacket",
            "ClientboundPingPacket",
            "ClientboundSetTimePacket",
            "ClientboundLevelChunkWithLightPacket",
            "ClientboundLightUpdatePacket",
            "ClientboundChunkBatchStartPacket",
            "ClientboundChunkBatchFinishedPacket",
            "ClientboundChunksBiomesPacket");

    private static final Set<String> MOVEMENT_PACKETS = Set.of(
            "ClientboundMoveEntityPacket",
            "ClientboundMoveEntityPacket$Pos",
            "ClientboundMoveEntityPacket$Rot",
            "ClientboundMoveEntityPacket$PosRot",
            "ClientboundTeleportEntityPacket",
            "ClientboundEntityPositionSyncPacket",
            "ClientboundSetEntityMotionPacket",
            "ClientboundRotateHeadPacket",
            "ClientboundMoveVehiclePacket");

    private static final Set<String> IGNORED_OUTBOUND = Set.of(
            "ServerboundMovePlayerPacket",
            "ServerboundMovePlayerPacket$Pos",
            "ServerboundMovePlayerPacket$Rot",
            "ServerboundMovePlayerPacket$PosRot",
            "ServerboundMovePlayerPacket$StatusOnly",
            "ServerboundKeepAlivePacket",
            "ServerboundPongPacket",
            "ServerboundClientTickEndPacket",
            "ServerboundPlayerInputPacket",
            "ServerboundAcceptTeleportationPacket",
            "ServerboundChatAckPacket");

    private final String name;
    private final boolean raw;
    private final Path directory;
    private final Path worldsDirectory;
    private final Path mapsDirectory;
    private final CaptureStreams streams;
    private final ScreenTracker screens;
    private final PlayerTracker player;
    private final HudTracker hud;
    private final EntityTracker entities;
    private final LocalDateTime startedAt = LocalDateTime.now();
    private final long startedAtMs = System.currentTimeMillis();
    private final List<WorldSegment> segments = new ArrayList<>();
    private final List<JsonObject> merges = new ArrayList<>();
    private final List<String> notes = new ArrayList<>();

    private ClientLevel trackedLevel;
    private WorldSegment current;
    private WorldSegment previous;
    private String pendingReason;
    private int nextWorldIndex = 1;
    private int evaluationDeadline;
    private int tick;
    private int chunkCountdown = 1;
    private int indexCountdown = INDEX_INTERVAL_TICKS;
    private int flushCountdown = FLUSH_INTERVAL_TICKS;
    private int packetCount;
    private int mapDumps;
    private int failures;
    private boolean stopped;

    private CaptureSession(String name, boolean raw, Path directory) throws IOException {
        this.name = name;
        this.raw = raw;
        this.directory = directory;
        this.worldsDirectory = directory.resolve("worlds");
        this.mapsDirectory = directory.resolve("maps");
        this.streams = new CaptureStreams(directory.resolve("streams"));
        this.screens = new ScreenTracker(this);
        this.player = new PlayerTracker(this);
        this.hud = new HudTracker(this);
        this.entities = new EntityTracker(this);
        Files.createDirectories(worldsDirectory);
    }

    static CaptureSession open(String name, boolean raw) throws IOException {
        String safeName = LoadedChunkExporter.sanitizeSessionName(name);
        Path directory = CAPTURE_ROOT.resolve(safeName + "_" + LocalDateTime.now().format(FILE_FORMAT));
        Files.createDirectories(directory);

        CaptureSession session = new CaptureSession(safeName, raw, directory);
        session.writeReadme();
        session.emitSessionStart();
        return session;
    }

    Path directory() {
        return directory;
    }

    String name() {
        return name;
    }

    @Override
    public void emit(String stream, String type, JsonObject payload) {
        if (stopped) {
            return;
        }

        JsonObject event = new JsonObject();
        event.addProperty("ms", elapsed());
        event.addProperty("tick", tick);
        event.addProperty("t", LocalTime.now().format(TIME_FORMAT));
        event.addProperty("world", current == null ? null : current.id());
        event.addProperty("type", type);
        event.add("data", payload);
        streams.write(stream, event);
    }

    void note(String text) {
        notes.add(elapsed() + "ms: " + text);
        if (current != null) {
            current.addNote(text);
        }

        JsonObject payload = new JsonObject();
        payload.addProperty("text", text);
        emit("timeline", "note", payload);
    }

    void split(String label) {
        if (current == null) {
            return;
        }
        current.forbidMerge();
        closeCurrent("manual-split" + (label == null ? "" : ":" + label));
    }

    void tick() {
        if (stopped) {
            return;
        }

        tick++;
        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;

        if (level == null) {
            if (current != null) {
                closeCurrent("disconnected");
            }
            trackedLevel = null;
            streams.flush();
            return;
        }

        if (level != trackedLevel) {
            if (current != null) {
                closeCurrent(pendingReason == null ? "level-change" : pendingReason);
            }
            trackedLevel = level;
        }

        if (current == null) {
            if (client.player == null) {
                return;
            }
            openSegment(client, level);
        }

        if (--chunkCountdown <= 0) {
            chunkCountdown = CHUNK_INTERVAL_TICKS;
            captureChunks(level);
        }

        evaluateRejoin();

        run(() -> screens.tick(client));
        run(() -> player.tick(client));
        run(() -> hud.tick(client));
        run(() -> entities.tick(client));

        if (--indexCountdown <= 0) {
            indexCountdown = INDEX_INTERVAL_TICKS;
            writeIndex();
        }

        if (--flushCountdown <= 0) {
            flushCountdown = FLUSH_INTERVAL_TICKS;
            streams.flush();
        }
    }

    void recordInbound(Packet<?> packet) {
        if (stopped || !Minecraft.getInstance().isSameThread()) {
            return;
        }

        if (packet instanceof BundlePacket<?> bundle) {
            for (Packet<?> sub : bundle.subPackets()) {
                recordInbound(sub);
            }
            return;
        }

        packetCount++;
        run(() -> route(packet));
    }

    void recordOutbound(Packet<?> packet) {
        if (stopped || !Minecraft.getInstance().isSameThread()) {
            return;
        }

        if (IGNORED_OUTBOUND.contains(packet.getClass().getSimpleName())
                || IGNORED_OUTBOUND.contains(nestedName(packet))) {
            return;
        }

        run(() -> {
            switch (packet) {
                case ServerboundChatPacket chat -> {
                    JsonObject payload = new JsonObject();
                    payload.addProperty("message", chat.message());
                    emit("outbound", "out:chat", payload);
                }
                case ServerboundChatCommandPacket command -> {
                    JsonObject payload = new JsonObject();
                    payload.addProperty("command", command.command());
                    emit("outbound", "out:command", payload);
                }
                default -> emit("outbound", "out:" + label(packet), CaptureJson.fields(packet));
            }
        });
    }

    void onSlotClick(AbstractContainerScreen<?> screen, Slot slot, int slotId, int button, ContainerInput input) {
        if (stopped) {
            return;
        }
        run(() -> screens.onSlotClick(screen, slot, slotId, button, input));
    }

    void onScreenClosed() {
        if (stopped) {
            return;
        }
        run(screens::onScreenClosed);
    }

    private void route(Packet<?> packet) {
        if (IGNORED_PACKETS.contains(packet.getClass().getSimpleName())) {
            return;
        }
        if (!raw && (MOVEMENT_PACKETS.contains(packet.getClass().getSimpleName())
                || MOVEMENT_PACKETS.contains(nestedName(packet)))) {
            return;
        }

        switch (packet) {
            case ClientboundLoginPacket login -> {
                emitPacket("timeline", login);
                closeCurrent("server-switch");
            }
            case ClientboundRespawnPacket respawn -> {
                emitPacket("timeline", respawn);
                closeCurrent("respawn");
            }
            case ClientboundForgetLevelChunkPacket forget -> captureChunkAt(forget.pos());
            case ClientboundSystemChatPacket chat -> {
                JsonObject payload = new JsonObject();
                payload.addProperty("overlay", chat.overlay());
                payload.add("message", CaptureJson.component(chat.content()));
                emit("chat", chat.overlay() ? "chat:actionbar" : "chat:system", payload);
            }
            case ClientboundPlayerChatPacket chat -> emit("chat", "chat:player", CaptureJson.fields(chat));
            case ClientboundDisguisedChatPacket chat -> {
                JsonObject payload = new JsonObject();
                payload.add("message", CaptureJson.component(chat.message()));
                emit("chat", "chat:disguised", payload);
            }
            case ClientboundSetTitleTextPacket title ->
                    emit("titles", "title:title", CaptureJson.component(title.text()));
            case ClientboundSetSubtitleTextPacket subtitle ->
                    emit("titles", "title:subtitle", CaptureJson.component(subtitle.text()));
            case ClientboundSetActionBarTextPacket actionBar ->
                    emit("titles", "title:actionbar", CaptureJson.component(actionBar.text()));
            case ClientboundSetTitlesAnimationPacket animation ->
                    emit("titles", "title:times", CaptureJson.fields(animation));
            case ClientboundClearTitlesPacket clear -> emit("titles", "title:clear", CaptureJson.fields(clear));
            case ClientboundSoundPacket sound -> emit("sounds", "sound:play", CaptureJson.fields(sound));
            case ClientboundSoundEntityPacket sound -> emit("sounds", "sound:entity", CaptureJson.fields(sound));
            case ClientboundStopSoundPacket sound -> emit("sounds", "sound:stop", CaptureJson.fields(sound));
            case ClientboundLevelParticlesPacket particles ->
                    emit("particles", "particle", CaptureJson.fields(particles));
            case ClientboundBlockUpdatePacket block -> emit("blocks", "block:update", CaptureJson.fields(block));
            case ClientboundSectionBlocksUpdatePacket section ->
                    emit("blocks", "block:section", CaptureJson.fields(section));
            case ClientboundBlockDestructionPacket destruction ->
                    emit("blocks", "block:destruction", CaptureJson.fields(destruction));
            case ClientboundBlockEventPacket event -> emit("blocks", "block:event", CaptureJson.fields(event));
            case ClientboundAddEntityPacket add -> entities.onAdd(add);
            case ClientboundRemoveEntitiesPacket remove -> entities.onRemove(remove);
            case ClientboundSetEntityDataPacket data -> entities.onData(data);
            case ClientboundSetEquipmentPacket equipment -> entities.onEquipment(equipment);
            case ClientboundOpenScreenPacket open -> screens.onOpenScreenPacket(open);
            case ClientboundContainerSetDataPacket data -> screens.onContainerData(data);
            case ClientboundContainerSetSlotPacket slot -> emit("gui", "gui:setSlot", CaptureJson.fields(slot));
            case ClientboundContainerSetContentPacket content -> {
                if (raw) {
                    emit("gui", "gui:setContent", CaptureJson.fields(content));
                }
            }
            case ClientboundContainerClosePacket close -> emit("gui", "gui:serverClose", CaptureJson.fields(close));
            case ClientboundMapItemDataPacket map -> dumpMap(map);
            default -> emitPacket("timeline", packet);
        }
    }

    private void emitPacket(String stream, Packet<?> packet) {
        emit(stream, "packet:" + label(packet), CaptureJson.fields(packet));
    }

    private static String label(Packet<?> packet) {
        return nestedName(packet)
                .replace("Clientbound", "")
                .replace("Serverbound", "")
                .replace("Packet", "");
    }

    private void openSegment(Minecraft client, ClientLevel level) {
        LoadedChunkExporter.SessionContext context;
        try {
            context = LoadedChunkExporter.captureCurrentContext(client);
        } catch (IllegalStateException exception) {
            return;
        }

        String reason = pendingReason == null ? (segments.isEmpty() ? "session-start" : "world-change") : pendingReason;
        pendingReason = null;
        current = new WorldSegment(nextWorldIndex++, context, level.registryAccess(), elapsed(), reason);
        current.setEntryPosition(playerPosition(client));
        evaluationDeadline = tick + EVALUATION_TIMEOUT_TICKS;
        resetTrackers();
        captureChunks(level);

        if (previous == null) {
            confirmSegment();
        }
    }

    private void resetTrackers() {
        screens.reset();
        player.reset();
        hud.reset();
        entities.reset();
    }

    private void confirmSegment() {
        segments.add(current);

        JsonObject payload = new JsonObject();
        payload.addProperty("world", current.id());
        payload.addProperty("dimension", current.dimension());
        payload.addProperty("server", current.source());
        payload.addProperty("chunks", current.chunkCount());
        emit("timeline", "world:enter", payload);
    }

    private void evaluateRejoin() {
        if (previous == null || current == null) {
            return;
        }
        if (current.chunkCount() < EVALUATION_MIN_CHUNKS && tick < evaluationDeadline) {
            return;
        }
        resolvePrevious();
    }

    private void resolvePrevious() {
        if (previous == null || current == null) {
            return;
        }

        double overlap = current.overlapWith(previous);
        if (!previous.mergeForbidden() && overlap >= REJOIN_OVERLAP) {
            JsonObject merge = new JsonObject();
            merge.addProperty("world", current.id());
            merge.addProperty("mergedInto", previous.id());
            merge.addProperty("overlap", CaptureJson.round(overlap));
            merges.add(merge);

            previous.absorb(current);
            current = previous;
            previous = null;
            emit("timeline", "world:rejoin", merge);
            return;
        }

        WorldSegment finished = previous;
        previous = null;
        writeSegment(finished);
        confirmSegment();
    }

    private void closeCurrent(String reason) {
        if (current == null) {
            return;
        }

        captureChunks(Minecraft.getInstance().level);
        resolvePrevious();
        current.close(elapsed(), reason);
        pendingReason = reason;

        JsonObject payload = new JsonObject();
        payload.addProperty("world", current.id());
        payload.addProperty("reason", reason);
        payload.addProperty("chunks", current.chunkCount());
        emit("timeline", "world:exit", payload);

        previous = current;
        current = null;
    }

    private void writeSegment(WorldSegment segment) {
        if (!segments.contains(segment)) {
            segments.add(segment);
        }
        for (WorldSegment other : segments) {
            if (other == segment || other.directory() == null) {
                continue;
            }
            double overlap = segment.overlapWith(other);
            if (overlap >= SIMILAR_OVERLAP) {
                segment.markDuplicate(other.id(), overlap);
                break;
            }
        }

        segment.write(worldsDirectory);
        writeSegmentIndex(segment);
        writeIndex();
    }

    private void captureChunks(ClientLevel level) {
        if (current == null || level == null) {
            return;
        }
        if (!current.context().matches(Minecraft.getInstance(), level)) {
            return;
        }
        run(() -> current.add(LoadedChunkExporter.captureLoadedChunks(level)));
    }

    private void captureChunkAt(ChunkPos position) {
        Minecraft client = Minecraft.getInstance();
        ClientLevel level = client.level;
        if (current == null || level == null || !current.context().matches(client, level)) {
            return;
        }
        LoadedChunkExporter.captureChunk(level, position.x(), position.z())
                .ifPresent(chunk -> current.add(chunk.packedPos(), chunk.chunkTag()));
    }

    private void dumpMap(ClientboundMapItemDataPacket packet) {
        JsonObject payload = new JsonObject();
        payload.addProperty("mapId", packet.mapId().id());
        payload.addProperty("scale", packet.scale());

        if (packet.colorPatch().isEmpty()) {
            emit("timeline", "map:data", payload);
            return;
        }

        MapItemSavedData.MapPatch patch = packet.colorPatch().get();
        mapDumps++;
        String file = "map_" + packet.mapId().id() + "_" + mapDumps + ".png";
        payload.addProperty("width", patch.width());
        payload.addProperty("height", patch.height());
        payload.addProperty("startX", patch.startX());
        payload.addProperty("startY", patch.startY());
        payload.addProperty("file", "maps/" + file);

        try {
            Files.createDirectories(mapsDirectory);
            BufferedImage image = new BufferedImage(patch.width(), patch.height(), BufferedImage.TYPE_INT_ARGB);
            for (int x = 0; x < patch.width(); x++) {
                for (int y = 0; y < patch.height(); y++) {
                    byte color = patch.mapColors()[x + y * patch.width()];
                    image.setRGB(x, y, MapColor.getColorFromPackedId(color & 0xFF));
                }
            }
            ImageIO.write(image, "png", mapsDirectory.resolve(file).toFile());
        } catch (Exception exception) {
            payload.addProperty("error", String.valueOf(exception.getMessage()));
        }

        emit("timeline", "map:data", payload);
    }

    FullCapture.StopResult stop(boolean convertPolar) {
        closeCurrent("session-stop");
        if (previous != null) {
            WorldSegment last = previous;
            previous = null;
            writeSegment(last);
        }

        emitSessionStop();
        stopped = true;

        int converted = 0;
        if (convertPolar) {
            converted = convertWorlds();
        }

        writeIndex();
        streams.close();
        return new FullCapture.StopResult(directory, segments.size(), totalChunks(), packetCount, streams.total(), converted);
    }

    private int convertWorlds() {
        List<PolarConvert.ConversionJob> jobs = new ArrayList<>();
        for (WorldSegment segment : segments) {
            if (segment.directory() == null || segment.chunkCount() == 0) {
                continue;
            }
            Path output = segment.directory().resolve(segment.id() + ".polar");
            jobs.add(new PolarConvert.ConversionJob(
                    segment.id(),
                    segment.directory(),
                    output,
                    segment.registries(),
                    () -> reload(segment),
                    List.of()));
        }

        int converted = 0;
        for (PolarConvert.ConversionOutcome outcome : PolarConvert.convertAll(jobs)) {
            WorldSegment segment = segmentById(outcome.job().worldId());
            if (segment == null) {
                continue;
            }
            if (outcome.error() == null) {
                converted++;
                segment.setPolarFile(segment.id() + "/" + outcome.job().outputPath().getFileName());
            } else {
                segment.setError(outcome.error());
            }
        }
        return converted;
    }

    private static Collection<CompoundTag> reload(WorldSegment segment) {
        try {
            return segment.reloadChunks().values();
        } catch (IOException exception) {
            segment.setError(String.valueOf(exception.getMessage()));
            return List.of();
        }
    }

    private WorldSegment segmentById(String id) {
        for (WorldSegment segment : segments) {
            if (segment.id().equals(id)) {
                return segment;
            }
        }
        return null;
    }

    private void emitSessionStart() {
        Minecraft client = Minecraft.getInstance();
        JsonObject payload = new JsonObject();
        payload.addProperty("session", name);
        payload.addProperty("startedAt", startedAt.toString());
        payload.addProperty("minecraft", SharedConstants.getCurrentVersion().name());
        payload.addProperty("raw", raw);
        payload.addProperty("player", client.getGameProfile().name());
        payload.addProperty("server", serverAddress());
        payload.addProperty("brand", client.getConnection() == null ? null : client.getConnection().serverBrand());
        emit("timeline", "session:start", payload);
    }

    private void emitSessionStop() {
        JsonObject payload = new JsonObject();
        payload.addProperty("durationMs", elapsed());
        payload.addProperty("worlds", segments.size());
        payload.addProperty("chunks", totalChunks());
        payload.addProperty("packets", packetCount);
        emit("timeline", "session:stop", payload);
        streams.flush();
    }

    FullCapture.Status status() {
        return new FullCapture.Status(
                name,
                directory,
                elapsed(),
                segments.size(),
                current == null ? "-" : current.id(),
                totalChunks(),
                packetCount,
                streams.total(),
                screens.screenCount(),
                hud.sidebarUpdates(),
                entities.spawnCount(),
                failures);
    }

    private int totalChunks() {
        int total = 0;
        for (WorldSegment segment : segments) {
            total += segment.chunkCount();
        }
        if (current != null && !segments.contains(current)) {
            total += current.chunkCount();
        }
        return total;
    }

    private void writeIndex() {
        Minecraft client = Minecraft.getInstance();

        JsonObject root = new JsonObject();
        root.addProperty("session", name);
        root.addProperty("formatVersion", 1);
        root.addProperty("startedAt", startedAt.toString());
        root.addProperty("durationMs", elapsed());
        root.addProperty("minecraft", SharedConstants.getCurrentVersion().name());
        root.addProperty("server", serverAddress());
        root.addProperty("brand", client.getConnection() == null ? null : client.getConnection().serverBrand());
        root.addProperty("player", client.getGameProfile().name());
        root.addProperty("rawPackets", raw);
        root.addProperty("packets", packetCount);
        root.addProperty("mapImages", mapDumps);
        root.addProperty("failures", failures);
        root.addProperty("streamError", streams.lastError());

        JsonArray worlds = new JsonArray();
        for (WorldSegment segment : segments) {
            worlds.add(segment.describe());
        }
        if (current != null && !segments.contains(current)) {
            worlds.add(current.describe());
        }
        root.add("worlds", worlds);

        JsonArray mergeArray = new JsonArray();
        merges.forEach(mergeArray::add);
        root.add("mergedWorlds", mergeArray);

        JsonObject counts = new JsonObject();
        streams.counts().forEach(counts::addProperty);
        root.add("streams", counts);

        JsonArray noteArray = new JsonArray();
        notes.forEach(noteArray::add);
        root.add("notes", noteArray);

        try {
            Files.writeString(directory.resolve("session.json"), GSON.toJson(root), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            failure("Failed to write session.json: " + exception.getMessage());
        }
    }

    private void writeSegmentIndex(WorldSegment segment) {
        if (segment.directory() == null) {
            return;
        }
        try {
            Files.createDirectories(segment.directory());
            Files.writeString(segment.directory().resolve("segment.json"),
                    GSON.toJson(segment.describe()), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            failure("Failed to write segment index: " + exception.getMessage());
        }
    }

    private void writeReadme() throws IOException {
        Files.writeString(directory.resolve("README.md"), """
                # Full capture session

                Everything the client observed while this capture was running, written for offline analysis.

                ## Layout

                - `session.json` - index: worlds, stream event counts, server, duration, notes. Rewritten
                  periodically, so it stays usable even if the game crashes mid-session.
                - `streams/*.jsonl` - one JSON object per line. Every line shares the envelope
                  `{ms, tick, t, world, type, data}` where `ms` is milliseconds since the capture started and
                  `world` names the world segment the event belongs to.
                - `worlds/world_XXX/region/*.mca` - the blocks of one world segment in vanilla Anvil format.
                - `worlds/world_XXX/world_XXX.polar` - the same world converted for Minestom.
                - `worlds/world_XXX/segment.json` - dimension, entry/exit reason, chunk count, similarity to
                  earlier segments.
                - `maps/*.png` - every map image the server sent.

                ## Worlds

                A new segment is opened whenever the client changes world: a server switch, a respawn, or a
                manual `/fullcapture split`. Segments never overwrite each other. Chunks are kept as first
                seen, so a segment is the world as it looked on arrival; later edits arrive as `block:*` events
                in `streams/blocks.jsonl`. If a transition lands back in a world that was just captured (a
                death, a rejoin) the segments are merged and the merge is recorded under `mergedWorlds` in
                `session.json`; events already tagged with the merged id belong to the world it merged into.

                ## Streams

                | file | contents |
                | --- | --- |
                | `timeline.jsonl` | session and world lifecycle, plus every packet without a dedicated stream |
                | `chat.jsonl` | system, player and disguised chat, and action bar messages sent as chat |
                | `gui.jsonl` | menu opens with every slot and its components, slot changes, clicks, closes |
                | `inventory.jsonl` | the player inventory snapshot and every later slot change |
                | `player.jsonl` | health, food, effects, gamemode, and a sampled position track |
                | `scoreboard.jsonl` | the rendered sidebar, re-emitted whenever it changes |
                | `tab.jsonl` | tab list header, footer and entries |
                | `bossbar.jsonl` | boss bars with name, progress, colour and overlay |
                | `titles.jsonl` | titles, subtitles, action bars and their timings |
                | `sounds.jsonl` | every sound the server played |
                | `particles.jsonl` | every particle effect the server spawned |
                | `blocks.jsonl` | block updates after the world was first captured |
                | `entities.jsonl` | entity spawns, removals, metadata, equipment, and a sampled position track |
                | `outbound.jsonl` | what the player did: chat, commands, clicks, interactions |

                Text is recorded twice: `legacy` carries section-sign colour codes, `json` carries the raw
                component tree. Items carry their id, count, name, lore, and every data component.
                """, StandardCharsets.UTF_8);
    }

    private String serverAddress() {
        Minecraft client = Minecraft.getInstance();
        return client.getCurrentServer() == null ? "unknown" : client.getCurrentServer().ip;
    }

    private JsonObject playerPosition(Minecraft client) {
        if (client.player == null) {
            return null;
        }
        JsonObject json = new JsonObject();
        json.addProperty("x", CaptureJson.round(client.player.getX()));
        json.addProperty("y", CaptureJson.round(client.player.getY()));
        json.addProperty("z", CaptureJson.round(client.player.getZ()));
        json.addProperty("yaw", CaptureJson.round(client.player.getYRot()));
        json.addProperty("pitch", CaptureJson.round(client.player.getXRot()));
        return json;
    }

    private long elapsed() {
        return System.currentTimeMillis() - startedAtMs;
    }

    private void run(Runnable action) {
        try {
            action.run();
        } catch (Exception exception) {
            failure(exception.getClass().getSimpleName() + ": " + exception.getMessage());
        }
    }

    private void failure(String message) {
        failures++;
        if (failures <= 5) {
            ChatUtils.error("Full capture: " + message);
        }
    }

    private static String nestedName(Packet<?> packet) {
        Class<?> type = packet.getClass();
        return type.getEnclosingClass() == null
                ? type.getSimpleName()
                : type.getEnclosingClass().getSimpleName() + "$" + type.getSimpleName();
    }

    static String describeDuration(long milliseconds) {
        Duration duration = Duration.ofMillis(milliseconds);
        return String.format("%dm %02ds", duration.toMinutes(), duration.toSecondsPart());
    }

}
