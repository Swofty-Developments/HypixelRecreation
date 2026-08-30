package net.swofty.type.replayviewer.entity;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.packet.server.play.*;
import net.minestom.server.utils.validate.Check;
import org.intellij.lang.annotations.MagicConstant;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Getter
public class ReplayPlayerEntity extends LivingEntity {
    private final String playerName;
    private PlayerSkin skin;
    private final UUID actualUuid;
    @Setter
    private int belowScore = 0;
    private final int internalId;

    public ReplayPlayerEntity(String playerName,
                              String textureValue, String textureSignature, UUID actualUuid, int internalId) {
        super(EntityType.PLAYER, UUID.randomUUID());
        this.playerName = playerName;
        this.actualUuid = actualUuid;
        this.internalId = internalId;

        if (textureValue != null && !textureValue.isEmpty()) {
            this.skin = new PlayerSkin(textureValue, textureSignature);
        } else {
            this.skin = null;
        }

        set(DataComponents.CUSTOM_NAME, Component.text(playerName));
        setNoGravity(true);
        setAutoViewable(true);
        setCustomNameVisible(true);
    }

    @Override
    public void updateNewViewer(@NotNull Player player) {
        super.updateNewViewer(player);

        List<PlayerInfoUpdatePacket.Property> properties = skin != null ?
            List.of(new PlayerInfoUpdatePacket.Property("textures", skin.textures(), skin.signature())) :
            List.of();

        player.sendPackets(
            new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.ADD_PLAYER,
                new PlayerInfoUpdatePacket.Entry(
                    getUuid(),
                    playerName,
                    properties,
                    false,
                    0,
                    GameMode.CREATIVE,
                    Component.text(playerName),
                    null,
                    1, true)),
            new SpawnEntityPacket(this.getEntityId(), this.getUuid(), EntityType.PLAYER,
                getPosition(),
                    getPosition().yaw(),
                0,
                Vec.ZERO),
            new EntityHeadLookPacket(getEntityId(), getPosition().yaw()),
            new EntityMetaDataPacket(getEntityId(), Map.of(
                MetadataDef.Avatar.DISPLAYED_MODEL_PARTS_FLAGS.index(),
                Metadata.Byte((byte) 127) // 127 is all parts
            ))
        );

        sendEquipmentToViewer(player);
    }

    @Override
    public void updateOldViewer(Player player) {
        super.updateOldViewer(player);

        // Remove from player list
        player.sendPacket(new PlayerInfoRemovePacket(this.getUuid()));
    }


    @Override
    public @NonNull CompletableFuture<Void> teleport(@NonNull Pos position, @NonNull Vec velocity, long @Nullable [] chunks, @MagicConstant(flagsFromClass = RelativeFlags.class) int flags, boolean shouldConfirm) {
        Check.stateCondition(instance == null, "You need to use Entity#setInstance before teleporting an entity!");
        sendPacketToViewersAndSelf(
            new EntityHeadLookPacket(getEntityId(), position.yaw())
        );
        return super.teleport(position, velocity, chunks, flags, shouldConfirm);
    }

    public void takeVisualDamage() {
        sendPacketToViewersAndSelf(new DamageEventPacket(
            getEntityId(), MinecraftServer.getDamageTypeRegistry().getId(DamageType.PLAYER_ATTACK.asKey()),
            0,
            0,
            getPosition()
        ));
    }

    public void updateSkin(String textureValue, String textureSignature) {
        PlayerSkin newSkin = new PlayerSkin(textureValue, textureSignature);
        if (this.skin != null && this.skin.equals(newSkin)) return;

        this.skin = newSkin;

        sendPacketToViewersAndSelf(new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.ADD_PLAYER,
            new PlayerInfoUpdatePacket.Entry(
                getUuid(),
                playerName,
                List.of(new PlayerInfoUpdatePacket.Property("textures", skin.textures(), skin.signature())),
                false,
                0,
                GameMode.CREATIVE,
                Component.text(playerName),
                null,
                1, true)));
    }

    public String getScoreboardEntryName() {
        return playerName;
    }

    private void sendEquipmentToViewer(Player viewer) {
        Map<EquipmentSlot, ItemStack> equipments = new EnumMap<>(EquipmentSlot.class);
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack stack = getEquipment(slot);
            if (stack != null && !stack.isAir()) {
                equipments.put(slot, stack);
            }
        }

        if (!equipments.isEmpty()) {
            viewer.sendPacket(new EntityEquipmentPacket(getEntityId(), equipments));
        }
    }

}
