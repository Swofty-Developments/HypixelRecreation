package net.swofty.type.replayviewer.playback.murdermystery;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.play.BlockBreakAnimationPacket;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.swofty.commons.replay.protocol.ReplayDataReader;
import net.swofty.type.game.replay.api.ReplayEvent;
import net.swofty.type.game.replay.api.ReplayPlaybackContext;
import net.swofty.type.game.replay.api.ReplayScoreboard;
import net.swofty.type.game.replay.api.ReplayStateDelta;
import net.swofty.type.game.replay.api.ReplayViewerAdapter;
import net.swofty.type.game.replay.delta.ReplayGameStateDelta;
import net.swofty.type.game.replay.event.ReplayComponentEvent;
import net.swofty.type.game.replay.event.ReplayEntityAnimationEvent;
import net.swofty.type.game.replay.event.ReplayParticleEvent;
import net.swofty.type.game.replay.event.ReplaySoundEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.replayviewer.playback.ReplaySession;
import net.swofty.type.replayviewer.util.ReplaySettingsUtil;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class MurderMysteryViewerAdapter implements ReplayViewerAdapter<MurderMysteryViewerMetadata, MurderMysteryViewerState> {
    public static final String GAME_TYPE = "MURDER_MYSTERY";
    public static final int SCHEMA_VERSION = 1;
    @Override
    public String gameType() {
        return GAME_TYPE;
    }

    @Override
    public int metadataSchemaVersion() {
        return SCHEMA_VERSION;
    }

    @Override
    public MurderMysteryViewerMetadata readMetadata(ReplayDataReader reader) throws IOException {
        String mode = reader.readString();
        int count = checkedCount(reader.readVarInt(), 1024, "players");
        List<MurderMysteryViewerMetadata.Player> players = new ArrayList<>(count);
        for (int index = 0; index < count; index++) {
            UUID uuid = reader.readUUID();
            String role = reader.readString();
            UUID target = reader.readBoolean() ? reader.readUUID() : null;
            players.add(new MurderMysteryViewerMetadata.Player(uuid, role, target));
        }
        return new MurderMysteryViewerMetadata(mode, players);
    }

    @Override
    public MurderMysteryViewerState readState(ReplayDataReader reader) throws IOException {
        int roleCount = checkedCount(reader.readVarInt(), 1024, "roles");
        Map<UUID, String> roles = new LinkedHashMap<>();
        for (int index = 0; index < roleCount; index++) roles.put(reader.readUUID(), reader.readString());
        int killCount = checkedCount(reader.readVarInt(), 1024, "kills");
        Map<UUID, Integer> kills = new LinkedHashMap<>();
        for (int index = 0; index < killCount; index++) kills.put(reader.readUUID(), reader.readVarInt());
        int eliminatedCount = checkedCount(reader.readVarInt(), 1024, "eliminated players");
        List<UUID> eliminated = new ArrayList<>(eliminatedCount);
        for (int index = 0; index < eliminatedCount; index++) eliminated.add(reader.readUUID());
        boolean sword = reader.readBoolean();
        boolean bow = reader.readBoolean();
        String condition = reader.readBoolean() ? reader.readString() : null;
        UUID winner = reader.readBoolean() ? reader.readUUID() : null;
        return new MurderMysteryViewerState(roles, kills, eliminated, sword, bow, condition, winner);
    }

    @Override
    public void restoreState(ReplayPlaybackContext context, MurderMysteryViewerState state) {
    }

    @Override
    public void applyDelta(ReplayPlaybackContext context, ReplayStateDelta delta) {
        if (!(delta instanceof ReplayGameStateDelta gameDelta) || gameDelta.gameTypeId() != 1) return;
        try (ReplayDataReader reader = new ReplayDataReader(gameDelta.payload())) {
            switch (reader.readUnsignedByte()) {
                case 1 -> {
                    reader.readUUID();
                    reader.readUUID();
                    reader.readString();
                }
                case 2 -> {
                    reader.readUUID();
                    reader.readString();
                }
                case 3 -> {
                    reader.readString();
                    if (reader.readBoolean()) reader.readUUID();
                }
                default -> throw new IOException("Unknown Murder Mystery replay delta");
            }
            if (reader.available() != 0) throw new IOException("Trailing Murder Mystery replay delta data");
        } catch (IOException exception) {
            throw new IllegalArgumentException("Invalid Murder Mystery replay delta", exception);
        }
    }

    @Override
    public void renderEvent(ReplayPlaybackContext context, ReplayEvent event) {
        if (!(context instanceof ReplaySession session)) return;
        if (event instanceof ReplayParticleEvent particleEvent) {
            byte[] bytes = particleEvent.packet();
            ParticlePacket packet = ParticlePacket.SERIALIZER.read(NetworkBuffer.wrap(bytes, 0, bytes.length));
            session.getViewers().stream()
                    .filter(viewer -> !(viewer instanceof HypixelPlayer player)
                            || ReplaySettingsUtil.getSettings(player).isShowParticles())
                    .forEach(viewer -> viewer.sendPacket(packet));
            return;
        }
        if (event instanceof ReplaySoundEvent soundEvent) {
            Sound.Source[] sources = Sound.Source.values();
            if (soundEvent.source() < 0 || soundEvent.source() >= sources.length) {
                throw new IllegalArgumentException("Unknown replay sound source: " + soundEvent.source());
            }
            Sound sound = Sound.sound(Key.key(soundEvent.soundId()), sources[soundEvent.source()],
                    soundEvent.volume(), soundEvent.pitch());
            session.getViewers().forEach(viewer -> viewer.playSound(sound,
                    soundEvent.x(), soundEvent.y(), soundEvent.z()));
            return;
        }
        if (event instanceof net.swofty.type.game.replay.event.ReplayBlockBreakEvent blockBreakEvent) {
            var position = blockBreakEvent.position();
            BlockBreakAnimationPacket packet = new BlockBreakAnimationPacket(blockBreakEvent.entityId(),
                    new Pos(position.x(), position.y(), position.z()), blockBreakEvent.stage());
            session.getViewers().forEach(viewer -> viewer.sendPacket(packet));
            return;
        }
        if (event instanceof ReplayEntityAnimationEvent animationEvent) {
            var entity = session.getEntityManager().getEntity(animationEvent.entityId());
            if (entity == null) return;
            EntityAnimationPacket.Animation animation = switch (animationEvent.animation()) {
                case SWING_MAIN_HAND -> EntityAnimationPacket.Animation.SWING_MAIN_ARM;
                case SWING_OFF_HAND -> EntityAnimationPacket.Animation.SWING_OFF_HAND;
                case TAKE_DAMAGE -> EntityAnimationPacket.Animation.TAKE_DAMAGE;
                case LEAVE_BED -> EntityAnimationPacket.Animation.LEAVE_BED;
                case CRITICAL_EFFECT -> EntityAnimationPacket.Animation.CRITICAL_EFFECT;
                case MAGIC_CRITICAL_EFFECT -> EntityAnimationPacket.Animation.MAGICAL_CRITICAL_EFFECT;
            };
            session.getViewers().forEach(viewer -> viewer.sendPacket(
                    new EntityAnimationPacket(entity.getEntityId(), animation)));
            return;
        }
        if (!(event instanceof ReplayComponentEvent componentEvent)) return;
        for (var viewer : session.getViewers()) {
            if (viewer instanceof HypixelPlayer player && !ReplaySettingsUtil.getSettings(player).isChatMessages()
                    && componentEvent.kind() != ReplayComponentEvent.Kind.TITLE) continue;
            switch (componentEvent.kind()) {
                case TITLE -> viewer.showTitle(Title.title(componentEvent.component(), Component.empty(),
                        Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(2), Duration.ofMillis(300))));
                case ACTION_BAR -> viewer.sendActionBar(componentEvent.component());
                default -> viewer.sendMessage(componentEvent.component());
            }
        }
    }

    @Override
    public ReplayScoreboard createScoreboard(ReplayPlaybackContext context) {
        return new MurderMysteryReplayScoreboard((ReplaySession) context);
    }

    private int checkedCount(int value, int maximum, String name) throws IOException {
        if (value < 0 || value > maximum) throw new IOException("Invalid Murder Mystery " + name + " count");
        return value;
    }
}
