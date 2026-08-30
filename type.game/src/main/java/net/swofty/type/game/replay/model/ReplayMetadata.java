package net.swofty.type.game.replay.model;

import java.util.List;

public record ReplayMetadata(
        ReplayDescriptor descriptor,
        List<ReplayParticipant> participants,
        ReplayGameMetadataEnvelope gameMetadata
) {
    public ReplayMetadata {
        participants = List.copyOf(participants);
    }
}
