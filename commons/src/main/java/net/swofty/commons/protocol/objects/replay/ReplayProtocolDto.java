package net.swofty.commons.protocol.objects.replay;

import net.swofty.commons.ServerType;

import java.util.List;
import java.util.UUID;

public final class ReplayProtocolDto {
    private ReplayProtocolDto() {
    }

    public record Descriptor(
            UUID replayId,
            String gameId,
            String gameType,
            ServerType serverType,
            String serverId,
            String mapName,
            String mapHash,
            double mapCenterX,
            double mapCenterZ,
            int formatVersion,
            long startTime,
            long endTime,
            int durationTicks,
            long dataSize
    ) {
    }

    public record Participant(
            UUID uuid,
            int entityId,
            String username,
            String textureValue,
            String textureSignature,
            String displayNameJson,
            String prefixJson,
            String suffixJson
    ) {
    }

    public record GameMetadataEnvelope(String gameType, int schemaVersion, byte[] payload) {
        public GameMetadataEnvelope {
            if (gameType == null || gameType.isBlank())
                throw new IllegalArgumentException("gameType must not be blank");
            if (schemaVersion < 1) throw new IllegalArgumentException("schemaVersion must be positive");
            if (payload.length > 16 * 1024 * 1024)
                throw new IllegalArgumentException("Replay game metadata is too large");
            payload = payload.clone();
        }

        @Override
        public byte[] payload() {
            return payload.clone();
        }
    }

    public record Metadata(Descriptor descriptor, List<Participant> participants, GameMetadataEnvelope gameMetadata) {
        public Metadata {
            if (participants.size() > 10_000)
                throw new IllegalArgumentException("Replay contains too many participants");
            participants = List.copyOf(participants);
            for (Participant participant : participants) {
                validateComponent(participant.displayNameJson());
                validateComponent(participant.prefixJson());
                validateComponent(participant.suffixJson());
            }
        }

        private static void validateComponent(String component) {
            if (component == null || component.length() > 1_048_576) {
                throw new IllegalArgumentException("Invalid replay component JSON");
            }
        }
    }
}
