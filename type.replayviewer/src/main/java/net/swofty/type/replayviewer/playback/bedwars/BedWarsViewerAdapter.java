package net.swofty.type.replayviewer.playback.bedwars;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.title.Title;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.play.BlockBreakAnimationPacket;
import net.minestom.server.network.packet.server.play.EntityAnimationPacket;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.swofty.commons.replay.protocol.ReplayDataReader;
import net.swofty.type.game.replay.api.*;
import net.swofty.type.game.replay.delta.ReplayGameStateDelta;
import net.swofty.type.game.replay.event.*;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.replayviewer.playback.ReplaySession;
import net.swofty.type.replayviewer.util.ReplaySettingsUtil;

import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class BedWarsViewerAdapter implements ReplayViewerAdapter<BedWarsViewerMetadata, BedWarsViewerState> {
    public static final String GAME_TYPE = "BEDWARS";
    public static final int SCHEMA_VERSION = 2;
    private static final GsonComponentSerializer COMPONENTS = GsonComponentSerializer.gson();
    private static final LegacyComponentSerializer LEGACY_COMPONENTS = LegacyComponentSerializer.legacySection();

    @Override
    public String gameType() {
        return GAME_TYPE;
    }

    @Override
    public int metadataSchemaVersion() {
        return SCHEMA_VERSION;
    }

    @Override
    public BedWarsViewerMetadata readMetadata(ReplayDataReader reader) throws IOException {
        String mode = reader.readString();
        int teamCount = checkedCount(reader.readVarInt(), 32, "teams");
        List<BedWarsViewerMetadata.Team> teams = new ArrayList<>(teamCount);
        for (int index = 0; index < teamCount; index++) {
            String id = reader.readString();
            String name = reader.readString();
            int color = reader.readInt();
            int memberCount = checkedCount(reader.readVarInt(), 1024, "team members");
            List<java.util.UUID> members = new ArrayList<>(memberCount);
            for (int member = 0; member < memberCount; member++) members.add(reader.readUUID());
            teams.add(new BedWarsViewerMetadata.Team(id, name, color, members, readPosition(reader), readPosition(reader)));
        }
        int generatorCount = checkedCount(reader.readVarInt(), 4096, "generators");
        List<BedWarsViewerMetadata.Generator> generators = new ArrayList<>(generatorCount);
        for (int index = 0; index < generatorCount; index++) {
            generators.add(new BedWarsViewerMetadata.Generator(reader.readString(), readPosition(reader)));
        }
        return new BedWarsViewerMetadata(mode, teams, generators);
    }

    @Override
    public BedWarsViewerState readState(ReplayDataReader reader) throws IOException {
        int teamCount = checkedCount(reader.readVarInt(), 32, "teams");
        Map<String, List<java.util.UUID>> teams = new LinkedHashMap<>();
        for (int index = 0; index < teamCount; index++) {
            String team = reader.readString();
            int memberCount = checkedCount(reader.readVarInt(), 1024, "team members");
            List<java.util.UUID> members = new ArrayList<>(memberCount);
            for (int member = 0; member < memberCount; member++) members.add(reader.readUUID());
            teams.put(team, members);
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
        String winner = reader.readBoolean() ? reader.readString() : null;
        int displayCount = checkedCount(reader.readVarInt(), 4096, "displays");
        List<BedWarsViewerState.DisplayState> displays = new ArrayList<>(displayCount);
        for (int index = 0; index < displayCount; index++) {
            int entityId = reader.readVarInt();
            java.util.UUID uuid = reader.readUUID();
            double[] position = reader.readLocation();
            displays.add(new BedWarsViewerState.DisplayState(entityId, uuid, position[0], position[1], position[2],
                    readComponentLines(reader), reader.readString(), reader.readString()));
        }
        int npcCount = checkedCount(reader.readVarInt(), 4096, "NPCs");
        List<BedWarsViewerState.NpcState> npcs = new ArrayList<>(npcCount);
        for (int index = 0; index < npcCount; index++) {
            npcs.add(new BedWarsViewerState.NpcState(reader.readVarInt(), decodeComponent(reader.readString()),
                    readComponentLines(reader)));
        }
        return new BedWarsViewerState(teams, beds, generators, scoreboard, eliminated, winner, displays, npcs);
    }

    @Override
    public void restoreState(ReplayPlaybackContext context, BedWarsViewerState state) {
        if (context instanceof ReplaySession session) {
            session.restoreBedWarsState(state.teamMembers(), state.liveBeds(), state.generatorTiers(), state.eliminatedTeams());
            for (var display : state.displays()) {
                session.getDynamicTextManager().createDisplay(display.entityId(), display.uuid(),
                        new Pos(display.x(), display.y(), display.z()), display.lines(), display.displayType(),
                        display.identifier(), session.getCurrentTick());
            }
            for (var npc : state.npcs()) {
                session.getNpcManager().updateNpcDisplayName(npc.entityId(), npc.displayName(), "", "", -1, false);
                session.getNpcManager().updateNpcTextLines(npc.entityId(), npc.lines(), 1.8, 0);
            }
        }
    }

    @Override
    public void applyDelta(ReplayPlaybackContext context, ReplayStateDelta delta) {
        if (!(context instanceof ReplaySession session) || !(delta instanceof ReplayGameStateDelta gameDelta)
                || gameDelta.gameTypeId() != 1) return;
        try (ReplayDataReader reader = new ReplayDataReader(gameDelta.payload())) {
            switch (reader.readUnsignedByte()) {
                case 1 -> session.applyBedState(reader.readString(), reader.readBoolean());
                case 2 -> session.applyPlayerTeam(reader.readUUID(), reader.readString());
                case 3 -> session.eliminateTeam(reader.readString());
                case 4 ->
                        session.applyGeneratorTier(Byte.toString((byte) reader.readByte()), reader.readUnsignedByte());
                case 10 -> {
                    int entityId = reader.readVarInt();
                    java.util.UUID uuid = reader.readUUID();
                    double[] position = reader.readLocation();
                    List<String> lines = readComponentLines(reader);
                    session.getDynamicTextManager().createDisplay(entityId, uuid,
                            new Pos(position[0], position[1], position[2], (float) position[3], (float) position[4]),
                            lines, reader.readString(), reader.readString(), session.getCurrentTick());
                }
                case 11 -> {
                    int entityId = reader.readVarInt();
                    List<String> lines = readComponentLines(reader);
                    session.getDynamicTextManager().updateDisplayText(entityId, lines, reader.readBoolean(),
                            reader.readVarInt(), session.getCurrentTick());
                }
                case 12 -> {
                    int entityId = reader.readVarInt();
                    String displayName = decodeComponent(reader.readString());
                    List<String> lines = readComponentLines(reader);
                    session.getNpcManager().updateNpcDisplayName(entityId, displayName, "", "", -1, false);
                    session.getNpcManager().updateNpcTextLines(entityId, lines, 1.8, 0);
                }
                default -> throw new IOException("Unknown Bed Wars replay delta");
            }
            if (reader.available() != 0) throw new IOException("Trailing Bed Wars replay delta data");
        } catch (IOException exception) {
            throw new IllegalArgumentException("Invalid Bed Wars replay delta", exception);
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
        if (event instanceof ReplayBlockBreakEvent blockBreakEvent) {
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
                case TITLE ->
                        viewer.showTitle(Title.title(componentEvent.component(), net.kyori.adventure.text.Component.empty(),
                                Title.Times.times(Duration.ofMillis(200), Duration.ofSeconds(2), Duration.ofMillis(300))));
                case ACTION_BAR -> viewer.sendActionBar(componentEvent.component());
                default -> viewer.sendMessage(componentEvent.component());
            }
        }
    }

    @Override
    public ReplayScoreboard createScoreboard(ReplayPlaybackContext context) {
        return new BedWarsReplayScoreboard((ReplaySession) context);
    }

    private BedWarsViewerMetadata.Position readPosition(ReplayDataReader reader) throws IOException {
        if (!reader.readBoolean()) return null;
        int[] position = reader.readBlockCoords();
        return new BedWarsViewerMetadata.Position(position[0], position[1], position[2]);
    }

    private int checkedCount(int value, int maximum, String name) throws IOException {
        if (value < 0 || value > maximum) throw new IOException("Invalid Bed Wars " + name + " count");
        return value;
    }

    private List<String> readStrings(ReplayDataReader reader) throws IOException {
        int count = checkedCount(reader.readVarInt(), 64, "text lines");
        List<String> values = new ArrayList<>(count);
        for (int index = 0; index < count; index++) values.add(reader.readString());
        return values;
    }

    private List<String> readComponentLines(ReplayDataReader reader) throws IOException {
        return readStrings(reader).stream().map(this::decodeComponent).toList();
    }

    private String decodeComponent(String json) {
        return LEGACY_COMPONENTS.serialize(COMPONENTS.deserialize(json));
    }
}
