package net.swofty.type.game.replay.model;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public record ReplayEntityState(
        int replayEntityId,
        UUID uuid,
        int entityTypeId,
        double x,
        double y,
        double z,
        float yaw,
        float pitch,
        double velocityX,
        double velocityY,
        double velocityZ,
        int poseId,
        boolean visible,
        boolean glowing,
        int flags,
        Lifecycle lifecycle,
        Map<Integer, byte[]> equipment,
        float health,
        float maximumHealth,
        List<ReplayPotionEffectState> effects,
        PlayerState player,
        byte[] typePayload
) {
    public ReplayEntityState {
        if (replayEntityId < 0 || entityTypeId < 0)
            throw new IllegalArgumentException("Invalid replay entity identity");
        lifecycle = java.util.Objects.requireNonNull(lifecycle, "lifecycle");
        equipment = copyEquipment(equipment);
        effects = List.copyOf(effects);
        typePayload = typePayload.clone();
    }

    @Override
    public Map<Integer, byte[]> equipment() {
        return copyEquipment(equipment);
    }

    @Override
    public byte[] typePayload() {
        return typePayload.clone();
    }

    private static Map<Integer, byte[]> copyEquipment(Map<Integer, byte[]> source) {
        java.util.LinkedHashMap<Integer, byte[]> copy = new java.util.LinkedHashMap<>();
        source.forEach((slot, item) -> copy.put(slot, item.clone()));
        return java.util.Collections.unmodifiableMap(copy);
    }

    public enum Lifecycle {
        ALIVE,
        DYING,
        DEAD_WAITING,
        RESPAWNING,
        ELIMINATED,
        SPECTATOR,
        DESPAWNED
    }

    public record PlayerState(
            UUID participantUuid,
            String textureValue,
            String textureSignature,
            String displayJson,
            String teamId,
            int gameMode,
            boolean legitimateSpectator,
            byte[] heldItem
    ) {
        public PlayerState {
            heldItem = heldItem.clone();
        }

        @Override
        public byte[] heldItem() {
            return heldItem.clone();
        }

        @Override
        public boolean equals(Object other) {
            return other instanceof PlayerState state
                    && gameMode == state.gameMode
                    && legitimateSpectator == state.legitimateSpectator
                    && java.util.Objects.equals(participantUuid, state.participantUuid)
                    && java.util.Objects.equals(textureValue, state.textureValue)
                    && java.util.Objects.equals(textureSignature, state.textureSignature)
                    && java.util.Objects.equals(displayJson, state.displayJson)
                    && java.util.Objects.equals(teamId, state.teamId)
                    && Arrays.equals(heldItem, state.heldItem);
        }

        @Override
        public int hashCode() {
            return 31 * java.util.Objects.hash(participantUuid, textureValue, textureSignature, displayJson, teamId,
                    gameMode, legitimateSpectator) + Arrays.hashCode(heldItem);
        }
    }

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof ReplayEntityState state)) return false;
        return replayEntityId == state.replayEntityId
                && entityTypeId == state.entityTypeId
                && Double.compare(x, state.x) == 0
                && Double.compare(y, state.y) == 0
                && Double.compare(z, state.z) == 0
                && Float.compare(yaw, state.yaw) == 0
                && Float.compare(pitch, state.pitch) == 0
                && Double.compare(velocityX, state.velocityX) == 0
                && Double.compare(velocityY, state.velocityY) == 0
                && Double.compare(velocityZ, state.velocityZ) == 0
                && poseId == state.poseId
                && visible == state.visible
                && glowing == state.glowing
                && flags == state.flags
                && Float.compare(health, state.health) == 0
                && Float.compare(maximumHealth, state.maximumHealth) == 0
                && java.util.Objects.equals(uuid, state.uuid)
                && lifecycle == state.lifecycle
                && equipmentEquals(equipment, state.equipment)
                && effects.equals(state.effects)
                && java.util.Objects.equals(player, state.player)
                && Arrays.equals(typePayload, state.typePayload);
    }

    @Override
    public int hashCode() {
        return 31 * java.util.Objects.hash(replayEntityId, uuid, entityTypeId, x, y, z, yaw, pitch,
                velocityX, velocityY, velocityZ, poseId, visible, glowing, flags, lifecycle, effects, player,
                health, maximumHealth) + Arrays.hashCode(typePayload);
    }

    private static boolean equipmentEquals(Map<Integer, byte[]> first, Map<Integer, byte[]> second) {
        if (!first.keySet().equals(second.keySet())) return false;
        return first.entrySet().stream().allMatch(entry -> Arrays.equals(entry.getValue(), second.get(entry.getKey())));
    }
}
