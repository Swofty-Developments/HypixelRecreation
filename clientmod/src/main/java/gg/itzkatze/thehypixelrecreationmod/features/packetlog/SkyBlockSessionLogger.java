package gg.itzkatze.thehypixelrecreationmod.features.packetlog;

import gg.itzkatze.thehypixelrecreationmod.utils.ChatUtils;
import gg.itzkatze.thehypixelrecreationmod.utils.StringUtility;
import gg.itzkatze.thehypixelrecreationmod.mixin.BossHealthOverlayAccessor;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.network.protocol.BundlePacket;
import net.minecraft.network.protocol.Packet;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Set;

public final class SkyBlockSessionLogger {
    private static final Path LOG_DIR = FabricLoader.getInstance().getGameDir().resolve("packet-logs");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final DateTimeFormatter FILE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd_HH-mm-ss");
    private static final Set<String> USEFUL_PACKETS = Set.of(
            "Chat", "SystemChat", "ActionBar", "SetTitle", "SetSubtitle", "ClearTitles",
            "Boss", "Sound", "Particle", "Explode", "LevelEvent", "GameEvent",
            "Block", "SectionBlocks", "Chunk", "Light", "BlockEntity",
            "AddEntity", "RemoveEntities", "MoveEntity", "TeleportEntity", "EntityPositionSync",
            "SetEntity", "RotateHead", "TakeItemEntity", "DamageEvent",
            "PlayerPosition", "Respawn", "SetTime", "SetSpawn",
            "Objective", "Score", "TabList", "PlayerInfo",
            "Container", "SetSlot", "SetHeldSlot", "Cooldown", "OpenScreen",
            "Animate", "AwardStats", "UpdateAdvancements", "CustomPayload"
    );
    private static final Set<String> USEFUL_OUTBOUND = Set.of(
            "Interact", "UseItem", "PlayerAction", "MovePlayer", "Chat", "Command",
            "ContainerClick", "SetCarriedItem", "Swing"
    );
    private static final int MAX_LINE_LENGTH = 6_000;

    private static BufferedWriter writer;
    private static Path logPath;
    private static int inboundCount;
    private static int outboundCount;
    private static int ticksSinceFlush;
    private static int captureTick;

    private SkyBlockSessionLogger() {
    }

    public static synchronized boolean isActive() {
        return writer != null;
    }

    public static synchronized Path start() throws IOException {
        if (isActive()) throw new IllegalStateException("A SkyBlock capture is already active.");
        Files.createDirectories(LOG_DIR);
        logPath = LOG_DIR.resolve("skyblock_session_" + LocalDateTime.now().format(FILE_FORMAT) + ".log");
        writer = Files.newBufferedWriter(logPath, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        inboundCount = 0;
        outboundCount = 0;
        ticksSinceFlush = 0;
        captureTick = 0;
        write("# skyblock packet capture v1");
        write("# started=" + LocalDateTime.now());
        Minecraft client = Minecraft.getInstance();
        if (client.player != null) {
            write("# player=" + client.player.getName().getString()
                    + " pos=" + position(client.player.getX(), client.player.getY(), client.player.getZ()));
        }
        if (client.level != null) {
            write("# dimension=" + client.level.dimension().identifier());
            for (var entity : client.level.entitiesForRendering()) {
                if (client.player != null && entity.distanceToSqr(client.player) > 128 * 128) continue;
                write("# entity id=" + entity.getId() + " uuid=" + entity.getUUID()
                        + " type=" + entity.getType().toShortString()
                        + " pos=" + position(entity.getX(), entity.getY(), entity.getZ())
                        + " yaw=" + String.format("%.2f", entity.getYRot())
                        + " name=" + compact(entity.getName().getString()));
            }
        }
        var bossEvents = ((BossHealthOverlayAccessor) client.gui.hud.getBossOverlay()).recreation$getEvents();
        bossEvents.forEach((uuid, event) -> write("# bossbar uuid=" + uuid
                + " progress=" + event.getProgress() + " name=" + StringUtility.toLegacyString(event.getName())));
        write("");
        writer.flush();
        return logPath;
    }

    public static synchronized StopResult stop() {
        if (!isActive()) throw new IllegalStateException("No SkyBlock capture is active.");
        Path path = logPath;
        write("");
        write("# stopped=" + LocalDateTime.now() + " inbound=" + inboundCount + " outbound=" + outboundCount);
        close();
        logPath = null;
        return new StopResult(path, inboundCount, outboundCount);
    }

    public static synchronized void recordInbound(Packet<?> packet) {
        record("S2C", packet, true);
    }

    public static synchronized void recordOutbound(Packet<?> packet) {
        record("C2S", packet, false);
    }

    private static void record(String direction, Packet<?> packet, boolean inbound) {
        if (!isActive()) return;
        if (packet instanceof BundlePacket<?> bundle) {
            for (Packet<?> child : bundle.subPackets()) record(direction, child, inbound);
            return;
        }
        String name = packet.getClass().getSimpleName();
        if (!matches(name, inbound ? USEFUL_PACKETS : USEFUL_OUTBOUND)) return;
        if (inbound) inboundCount++;
        else outboundCount++;
        String value = compact(String.valueOf(packet));
        write("[" + LocalTime.now().format(TIME_FORMAT) + "] " + direction + " " + name + " | " + value);
    }

    public static synchronized void tick() {
        if (!isActive()) return;
        Minecraft client = Minecraft.getInstance();
        if (client.level == null) {
            StopResult result = stop();
            ChatUtils.warn("Left the world; SkyBlock capture saved with " + result.totalPackets()
                    + " packets: " + result.path().getFileName());
            return;
        }
        if (++ticksSinceFlush < 20) return;
        ticksSinceFlush = 0;
        captureTick += 20;
        if (client.player != null) write("[tick " + captureTick + "] PLAYER pos="
                + position(client.player.getX(), client.player.getY(), client.player.getZ())
                + " yaw=" + String.format("%.2f", client.player.getYRot())
                + " pitch=" + String.format("%.2f", client.player.getXRot()));
        try {
            writer.flush();
        } catch (IOException exception) {
            ChatUtils.error("SkyBlock capture flush failed: " + exception.getMessage());
            close();
        }
    }

    private static boolean matches(String name, Set<String> tokens) {
        for (String token : tokens) if (name.contains(token)) return true;
        return false;
    }

    private static String compact(String value) {
        String compact = value.replace('\n', ' ').replace('\r', ' ').replaceAll("\\s+", " ");
        return compact.length() <= MAX_LINE_LENGTH ? compact : compact.substring(0, MAX_LINE_LENGTH) + "…";
    }

    private static String position(double x, double y, double z) {
        return "%.3f,%.3f,%.3f".formatted(x, y, z);
    }

    private static void write(String line) {
        if (writer == null) return;
        try {
            writer.write(line);
            writer.newLine();
        } catch (IOException exception) {
            ChatUtils.error("SkyBlock capture write failed: " + exception.getMessage());
            close();
        }
    }

    private static void close() {
        if (writer == null) return;
        try {
            writer.flush();
            writer.close();
        } catch (IOException ignored) {
        }
        writer = null;
    }

    public record StopResult(Path path, int inboundPackets, int outboundPackets) {
        public int totalPackets() {
            return inboundPackets + outboundPackets;
        }
    }
}
