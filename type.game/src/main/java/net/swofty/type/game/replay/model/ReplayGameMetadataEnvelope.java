package net.swofty.type.game.replay.model;

public record ReplayGameMetadataEnvelope(String gameType, int schemaVersion, byte[] payload) {
    public ReplayGameMetadataEnvelope {
        payload = payload.clone();
    }

    @Override
    public byte[] payload() {
        return payload.clone();
    }

    @Override
    public boolean equals(Object other) {
        return other instanceof ReplayGameMetadataEnvelope envelope
                && schemaVersion == envelope.schemaVersion
                && gameType.equals(envelope.gameType)
                && java.util.Arrays.equals(payload, envelope.payload);
    }

    @Override
    public int hashCode() {
        return 31 * java.util.Objects.hash(gameType, schemaVersion) + java.util.Arrays.hashCode(payload);
    }
}
