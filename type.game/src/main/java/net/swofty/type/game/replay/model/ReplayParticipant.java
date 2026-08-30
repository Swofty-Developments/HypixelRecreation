package net.swofty.type.game.replay.model;

import java.util.UUID;

public record ReplayParticipant(
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
