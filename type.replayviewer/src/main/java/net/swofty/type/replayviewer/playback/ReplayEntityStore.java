package net.swofty.type.replayviewer.playback;

import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.metadata.other.PrimedTntMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.swofty.commons.replay.protocol.ReplayDataReader;
import net.swofty.type.game.replay.model.ReplayEntityState;
import net.swofty.type.game.replay.model.ReplayParticipant;
import net.swofty.type.replayviewer.entity.ReplayDroppedItemEntity;
import net.swofty.type.replayviewer.entity.ReplayEntity;
import net.swofty.type.replayviewer.entity.ReplayEntityManager;
import net.swofty.type.replayviewer.entity.ReplayPlayerEntity;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;

public final class ReplayEntityStore {
    private final ReplayEntityManager projection;
    private final Function<UUID, ReplayParticipant> participants;
    private final Map<Integer, ReplayEntityState> states = new HashMap<>();

    public ReplayEntityStore(ReplayEntityManager projection, Function<UUID, ReplayParticipant> participants) {
        this.projection = projection;
        this.participants = participants;
    }

    public void restore(Map<Integer, ReplayEntityState> states) {
        projection.cleanup();
        this.states.clear();
        this.states.putAll(states);
        states.values().stream().sorted(java.util.Comparator.comparingInt(ReplayEntityState::replayEntityId)).forEach(this::project);
    }

    public void upsert(ReplayEntityState state) {
        ReplayEntityState previous = states.put(state.replayEntityId(), state);
        Entity entity = projection.getEntity(state.replayEntityId());
        if (!isProjected(state)) {
            if (entity != null) projection.despawnEntity(state.replayEntityId());
            return;
        }
        if (entity == null || entity.getEntityType().id() != state.entityTypeId()
                || (state.player() != null) != (entity instanceof ReplayPlayerEntity)) {
            if (entity != null) projection.despawnEntity(state.replayEntityId());
            project(state);
            return;
        }
        apply(entity, state, previous);
    }

    public void remove(int replayEntityId) {
        states.remove(replayEntityId);
        projection.despawnEntity(replayEntityId);
    }

    public Map<Integer, ReplayEntityState> states() {
        return Map.copyOf(states);
    }

    private void project(ReplayEntityState state) {
        if (!isProjected(state)) return;
        EntityType entityType = EntityType.fromId(state.entityTypeId());
        if (entityType == null)
            throw new IllegalArgumentException("Unknown replay entity type: " + state.entityTypeId());
        Entity entity;
        if (state.player() != null) {
            ReplayParticipant participant = participants.apply(state.player().participantUuid());
            String username = participant == null ? state.player().participantUuid().toString() : participant.username();
            entity = new ReplayPlayerEntity(username, state.player().textureValue(), state.player().textureSignature(),
                    state.player().participantUuid(), state.replayEntityId());
        } else if (entityType == EntityType.ITEM) {
            entity = new ReplayDroppedItemEntity(state.replayEntityId(), state.uuid(), state.typePayload(), 0);
        } else {
            entity = new ReplayEntity(entityType, state.replayEntityId(), state.uuid());
        }
        apply(entity, state, null);
        projection.spawnEntity(state.replayEntityId(), entity,
                new Pos(state.x(), state.y(), state.z(), state.yaw(), state.pitch()));
    }

    private void apply(Entity entity, ReplayEntityState state, ReplayEntityState previous) {
        entity.setNoGravity(true);
        entity.setVelocity(new Vec(state.velocityX(), state.velocityY(), state.velocityZ()));
        entity.setGlowing(state.glowing());
        entity.setInvisible(!state.visible());
        entity.setSneaking((state.flags() & 1) != 0);
        entity.setSprinting((state.flags() & 2) != 0);
        if (state.poseId() >= 0 && state.poseId() < EntityPose.values().length)
            entity.setPose(EntityPose.values()[state.poseId()]);
        if (state.lifecycle() == ReplayEntityState.Lifecycle.DYING) entity.setPose(EntityPose.DYING);
        if (entity.getInstance() != null) {
            entity.teleport(new Pos(state.x(), state.y(), state.z(), state.yaw(), state.pitch()));
        }
        if (entity instanceof LivingEntity living) {
            if (state.maximumHealth() > 0 && (previous == null || state.maximumHealth() != previous.maximumHealth())) {
                living.getAttribute(Attribute.MAX_HEALTH).setBaseValue(state.maximumHealth());
            }
            if (state.health() > 0 && (previous == null || state.health() != previous.health()))
                living.setHealth(state.health());
            if (previous == null || !equipmentEquals(previous.equipment(), state.equipment())) {
                for (EquipmentSlot slot : EquipmentSlot.values()) living.setEquipment(slot, ItemStack.AIR);
                for (var equipment : state.equipment().entrySet()) {
                    if (equipment.getKey() >= 0 && equipment.getKey() < EquipmentSlot.values().length) {
                        living.setEquipment(EquipmentSlot.values()[equipment.getKey()], readItem(equipment.getValue()));
                    }
                }
            }
            if (previous == null || !previous.effects().equals(state.effects())) {
                for (var active : List.copyOf(living.getActiveEffects())) living.removeEffect(active.potion().effect());
                for (var effect : state.effects()) {
                    PotionEffect type = PotionEffect.fromId(effect.effectId());
                    if (type != null)
                        living.addEffect(new Potion(type, effect.amplifier(), effect.remainingTicks(), effect.flags()));
                }
            }
        }
        if (entity instanceof ReplayPlayerEntity player && state.player() != null && previous != null
                && previous.player() != null && state.player().textureValue() != null
                && (!java.util.Objects.equals(previous.player().textureValue(), state.player().textureValue())
                || !java.util.Objects.equals(previous.player().textureSignature(), state.player().textureSignature()))) {
            player.updateSkin(state.player().textureValue(), state.player().textureSignature());
        }
        if (entity.getEntityType() == EntityType.TNT && state.typePayload().length > 0
                && entity.getEntityMeta() instanceof PrimedTntMeta tntMeta) {
            try (ReplayDataReader reader = new ReplayDataReader(state.typePayload())) {
                tntMeta.setFuseTime(reader.readVarInt());
                if (reader.available() != 0) throw new IOException("Trailing TNT replay payload");
            } catch (IOException exception) {
                throw new IllegalArgumentException("Invalid TNT replay payload", exception);
            }
        }
    }

    private boolean isProjected(ReplayEntityState state) {
        return state.visible() && state.lifecycle() != ReplayEntityState.Lifecycle.DESPAWNED
                && state.lifecycle() != ReplayEntityState.Lifecycle.ELIMINATED
                && state.lifecycle() != ReplayEntityState.Lifecycle.DEAD_WAITING
                && state.lifecycle() != ReplayEntityState.Lifecycle.RESPAWNING;
    }

    private boolean equipmentEquals(Map<Integer, byte[]> first, Map<Integer, byte[]> second) {
        if (!first.keySet().equals(second.keySet())) return false;
        return first.entrySet().stream().allMatch(entry -> java.util.Arrays.equals(entry.getValue(), second.get(entry.getKey())));
    }

    private ItemStack readItem(byte[] bytes) {
        if (bytes.length == 0) return ItemStack.AIR;
        try {
            CompoundBinaryTag tag = BinaryTagIO.reader().readNameless(new ByteArrayInputStream(bytes));
            return ItemStack.fromItemNBT(tag);
        } catch (IOException exception) {
            throw new IllegalArgumentException("Invalid replay item data", exception);
        }
    }
}
