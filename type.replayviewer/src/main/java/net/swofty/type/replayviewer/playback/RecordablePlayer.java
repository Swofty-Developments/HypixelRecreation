package net.swofty.type.replayviewer.playback;

import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.nbt.BinaryTagIO;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.MinecraftServer;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.instance.block.Block;
import net.minestom.server.item.ItemStack;
import net.minestom.server.network.NetworkBuffer;
import net.minestom.server.network.packet.server.play.BlockBreakAnimationPacket;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.minestom.server.utils.inventory.PlayerInventoryUtils;
import net.swofty.type.game.replay.recordable.*;
import net.swofty.type.game.replay.recordable.bedwars.RecordableBedDestruction;
import net.swofty.type.game.replay.recordable.bedwars.RecordableKill;
import net.swofty.type.game.replay.recordable.bedwars.RecordableTeamElimination;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.replayviewer.entity.ReplayDroppedItemEntity;
import net.swofty.type.replayviewer.entity.ReplayEntity;
import net.swofty.type.replayviewer.entity.ReplayPlayerEntity;
import net.swofty.type.replayviewer.util.ReplaySettingsUtil;
import org.intellij.lang.annotations.Subst;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RecordablePlayer {

    public static void play(Recordable recordable, ReplaySession session) {
        switch (recordable.getType()) {
            case BLOCK_CHANGE -> playBlockChange((RecordableBlockChange) recordable, session);
            case BLOCK_BREAK_ANIMATION -> playBlockBreakAnimation((RecordableBlockBreakAnimation) recordable, session);
            case ENTITY_SPAWN -> playEntitySpawn((RecordableEntitySpawn) recordable, session);
            case ENTITY_DESPAWN -> playEntityDespawn((RecordableEntityDespawn) recordable, session);
            case ENTITY_LOCATIONS -> playEntityLocations((RecordableEntityLocations) recordable, session);
            case ENTITY_ANIMATION -> playEntityAnimation((RecordableEntityAnimation) recordable, session);
            case ENTITY_EQUIPMENT -> playEntityEquipment((RecordableEntityEquipment) recordable, session);
            case ENTITY_EFFECT -> playEntityEffect((RecordableEntityEffect) recordable, session);
            case PLAYER_SNEAK -> playPlayerSneak((RecordablePlayerSneak) recordable, session);
            case PLAYER_SPRINT -> playPlayerSprint((RecordablePlayerSprint) recordable, session);
            case PLAYER_ARM_SWING -> playPlayerArmSwing((RecordablePlayerArmSwing) recordable, session);
            case PLAYER_DEATH -> playPlayerDeath((RecordablePlayerDeath) recordable, session);
            case PLAYER_RESPAWN -> playPlayerRespawn((RecordablePlayerRespawn) recordable, session);
            case PLAYER_SKIN -> playPlayerSkin((RecordablePlayerSkin) recordable, session);
            case PLAYER_DISPLAY_NAME -> playPlayerDisplayName((RecordablePlayerDisplayName) recordable, session);
            case PLAYER_HEALTH -> playPlayerHealth((RecordablePlayerHealth) recordable, session);
            case PLAYER_CHAT -> playPlayerChat((RecordablePlayerChat) recordable, session);
            case PARTICLE -> playParticle((RecordableParticle) recordable, session);
            case SOUND -> playSound((RecordableSound) recordable, session);
            case EXPLOSION -> playExplosion((RecordableExplosion) recordable, session);
            case PLAYER_HAND_ITEM -> playHandItem((RecordablePlayerHandItem) recordable, session);

            // bw
            case BEDWARS_BED_DESTRUCTION -> playBedDestruction((RecordableBedDestruction) recordable, session);
            case BEDWARS_KILL -> playFinalKill((RecordableKill) recordable, session);
            case BEDWARS_TEAM_ELIMINATION -> playTeamElimination((RecordableTeamElimination) recordable, session);

            // Dropped items
            case DROPPED_ITEM -> playDroppedItem((RecordableDroppedItem) recordable, session);
            case ITEM_PICKUP -> playItemPickup((RecordableItemPickup) recordable, session);

            // Dynamic text displays
            case DYNAMIC_TEXT_DISPLAY -> playDynamicTextDisplay((RecordableDynamicTextDisplay) recordable, session);
            case TEXT_DISPLAY_UPDATE -> playTextDisplayUpdate((RecordableTextDisplayUpdate) recordable, session);

            // NPC enhancements
            case NPC_DISPLAY_NAME -> playNpcDisplayName((RecordableNpcDisplayName) recordable, session);
            case NPC_TEXT_LINE -> playNpcTextLine((RecordableNpcTextLine) recordable, session);

            default -> {
            }
        }

        if (recordable.isEntityState() && recordable.getEntityId() >= 0) {
            trackEntityState(recordable, session);
        }
    }

    private static void playBlockChange(RecordableBlockChange rec, ReplaySession session) {
        Block block = Block.fromStateId(rec.getBlockStateId());
        if (block != null) {
            session.getInstance().setBlock(rec.getX(), rec.getY(), rec.getZ(), block);
        }
        // play sound to all viewers
        for (var viewer : session.getViewers()) {
            viewer.playSound(
                Sound.sound(
                    Key.key("minecraft:block." + block.key().value() + ".break"),
                    Sound.Source.BLOCK, 1.0f, 1.0f
                ),
                rec.getX(), rec.getY(), rec.getZ()
            );
        }
    }

    private static void playEntitySpawn(RecordableEntitySpawn rec, ReplaySession session) {
        Pos pos = new Pos(rec.getX(), rec.getY(), rec.getZ(), rec.getYaw(), rec.getPitch());

        // Determine entity type
        EntityType type = EntityType.fromId(rec.getEntityTypeId());
        if (type == null) type = EntityType.PLAYER;

        if (type == EntityType.PLAYER) {
            var skinData = session.getStateTracker().getSkin(rec.getEntityId());
            String displayName = session.getMetadata().getPlayers().get(rec.getEntityUuid());
            if (displayName == null) displayName = "Player";

            ReplayPlayerEntity playerEntity =
                new ReplayPlayerEntity(
                    displayName,
                    skinData != null ? skinData.textureValue() : null,
                    skinData != null ? skinData.textureSignature() : null,
                    rec.getEntityUuid(),
                    rec.getEntityId()
                );
            if (session.isRebuildingState()) {
                playerEntity.setAutoViewable(false);
            }
            session.getEntityManager().spawnEntity(rec.getEntityId(), playerEntity, pos);
        } else {
            ReplayEntity entity = new ReplayEntity(type, rec.getEntityId(), rec.getEntityUuid());
            if (session.isRebuildingState()) {
                entity.setAutoViewable(false);
            }
            session.getEntityManager().spawnEntity(rec.getEntityId(), entity, pos);
        }
    }

    private static void playEntityDespawn(RecordableEntityDespawn rec, ReplaySession session) {
        session.getEntityManager().despawnEntity(rec.getEntityId());
    }

    private static void playHandItem(RecordablePlayerHandItem rec, ReplaySession session) {
        Entity entity = session.getEntityManager().getEntity(rec.getEntityId());
        if (entity instanceof LivingEntity livingEntity) {
            try {
                ItemStack item = nbtBytesToItem(rec.getItemBytes());
                livingEntity.setItemInMainHand(item);
            } catch (IOException e) {
                Logger.error(e);
            }
        }
    }

    private static void playEntityLocations(RecordableEntityLocations rec, ReplaySession session) {
        for (var entry : rec.getEntries()) {
            Pos pos = new Pos(entry.x(), entry.y(), entry.z(), entry.yaw(), entry.pitch());
            session.getEntityManager().updateEntityPosition(entry.entityId(), pos);
            if (session.getNpcManager().getNpcData(entry.entityId()) != null) {
                session.getNpcManager().updateNpcPosition(entry.entityId(), pos);
            }
        }
    }

    private static void playBlockBreakAnimation(RecordableBlockBreakAnimation rec, ReplaySession session) {
        BlockBreakAnimationPacket packet = new BlockBreakAnimationPacket(
            rec.getEntityId(),
            new Pos(rec.getX(), rec.getY(), rec.getZ()),
            rec.getStage()
        );

        for (var viewer : session.getViewers()) {
            viewer.sendPacket(packet);
        }
    }

    private static void playEntityAnimation(RecordableEntityAnimation rec, ReplaySession session) {
        Entity entity = session.getEntityManager().getEntity(rec.getEntityId());
        if (entity instanceof ReplayPlayerEntity livingEntity) {
            switch (rec.getAnimation()) {
                case SWING_MAIN_HAND -> livingEntity.swingMainHand();
                case SWING_OFFHAND -> livingEntity.swingOffHand();
                case TAKE_DAMAGE -> livingEntity.takeVisualDamage();
            }
        }
    }

    private static ItemStack nbtBytesToItem(byte[] bytes) throws IOException {
        CompoundBinaryTag tag = BinaryTagIO.reader()
            .readNameless(new ByteArrayInputStream(bytes));

        return ItemStack.fromItemNBT(tag);
    }

    @Nullable
    private static EquipmentSlot convertSlot(int slotId) {
        return switch (slotId) {
            case PlayerInventoryUtils.BOOTS_SLOT -> EquipmentSlot.BOOTS;
            case PlayerInventoryUtils.LEGGINGS_SLOT -> EquipmentSlot.LEGGINGS;
            case PlayerInventoryUtils.CHESTPLATE_SLOT -> EquipmentSlot.CHESTPLATE;
            case PlayerInventoryUtils.HELMET_SLOT -> EquipmentSlot.HELMET;
            case 2 -> EquipmentSlot.BOOTS; // these seem to work from vanilla
            case 3 -> EquipmentSlot.LEGGINGS;
            case 4 -> EquipmentSlot.CHESTPLATE;
            case 5 -> EquipmentSlot.HELMET;
            case 0 -> EquipmentSlot.MAIN_HAND;
            case 1 -> EquipmentSlot.OFF_HAND;
            case 40 -> EquipmentSlot.OFF_HAND;
            default -> null;
        };
    }

    private static void playEntityEquipment(RecordableEntityEquipment rec, ReplaySession session) {
        Entity entity = session.getEntityManager().getEntity(rec.getEntityId());
        if (entity instanceof LivingEntity livingEntity) {
            try {
                EquipmentSlot slot = convertSlot(rec.getSlotId());
                if (slot != null) {
                    livingEntity.setEquipment(slot, nbtBytesToItem(rec.getItemBytes()));
                } else {
                    Logger.warn("Unknown equipment slot id: " + rec.getSlotId());
                }
            } catch (IOException e) {
                Logger.error(e);
            }
        }
    }

    private static void playEntityEffect(RecordableEntityEffect rec, ReplaySession session) {
        Entity entity = session.getEntityManager().getEntity(rec.getEntityId());
        if (entity instanceof LivingEntity livingEntity) {
            PotionEffect effect = PotionEffect.fromId(rec.getEffectId());
            if (effect != null) {
                // Flags: bit 0 = ambient, bit 1 = particles, bit 2 = icon
                byte flags = rec.getFlags();

                Potion potion = new Potion(effect, rec.getAmplifier(), rec.getDurationTicks(), flags);
                livingEntity.addEffect(potion);
            }
        }
    }

    private static void playPlayerSneak(RecordablePlayerSneak rec, ReplaySession session) {
        Entity entity = session.getEntityManager().getEntity(rec.getEntityId());
        if (entity != null) {
            entity.setSneaking(rec.isSneaking());
        }
    }

    private static void playPlayerSprint(RecordablePlayerSprint rec, ReplaySession session) {
        Entity entity = session.getEntityManager().getEntity(rec.getEntityId());
        if (entity != null) {
            entity.setSprinting(rec.isSprinting());
        }
    }

    private static void playPlayerArmSwing(RecordablePlayerArmSwing rec, ReplaySession session) {
        Entity entity = session.getEntityManager().getEntity(rec.getEntityId());
        if (entity instanceof net.minestom.server.entity.LivingEntity livingEntity) {
            if (rec.isMainHand()) {
                livingEntity.swingMainHand();
            } else {
                livingEntity.swingOffHand();
            }
        }
    }

    private static void playPlayerDeath(RecordablePlayerDeath rec, ReplaySession session) {
        // TODO: death
    }

    private static void playPlayerRespawn(RecordablePlayerRespawn rec, ReplaySession session) {
        Pos pos = new Pos(rec.getX(), rec.getY(), rec.getZ(), rec.getYaw(), rec.getPitch());
        session.getEntityManager().updateEntityPosition(rec.getEntityId(), pos);
    }

    private static void playParticle(RecordableParticle rec, ReplaySession session) {
        byte[] packetByteArray = rec.getData();
        ParticlePacket packet = ParticlePacket.SERIALIZER.read(NetworkBuffer.wrap(packetByteArray, 0, packetByteArray.length, MinecraftServer.process().registries()));

        for (var viewer : session.getViewers()) {
            if (viewer instanceof HypixelPlayer hypixelPlayer
                && !ReplaySettingsUtil.getSettings(hypixelPlayer).isShowParticles()) {
                continue;
            }
            viewer.sendPacket(packet);
        }
    }

    private static void playSound(RecordableSound rec, ReplaySession session) {
        @Subst("minecraft:block.note_block.pling") String soundId = rec.getSoundId();
        for (var viewer : session.getViewers()) {
            viewer.playSound(
                Sound.sound(
                    Key.key(soundId),
                    Sound.Source.values()[rec.getCategory()],
                        rec.getVolume(), rec.getPitch()
                ),
                rec.getX(), rec.getY(), rec.getZ()
            );
        }
    }

    private static void playExplosion(RecordableExplosion rec, ReplaySession session) {
        for (int[] pos : rec.getAffectedBlocks()) {
            session.getInstance().setBlock(pos[0], pos[1], pos[2], Block.AIR);
        }
    }

    private static void playPlayerSkin(RecordablePlayerSkin rec, ReplaySession session) {
        session.getStateTracker().trackSkin(rec.getEntityId(), rec.getTextureValue(), rec.getTextureSignature());

        Entity entity = session.getEntityManager().getEntity(rec.getEntityId());
        if (entity instanceof ReplayPlayerEntity playerEntity) {
            playerEntity.updateSkin(rec.getTextureValue(), rec.getTextureSignature());
        }
    }

    private static void playPlayerDisplayName(RecordablePlayerDisplayName rec, ReplaySession session) {
        Entity entity = session.getEntityManager().getEntity(rec.getEntityId());
        if (entity != null) {
            session.applyPlayerDisplayName(
                rec.getEntityId(),
                rec.getDisplayName(),
                rec.getPrefix(),
                rec.getSuffix(),
                rec.getNameColor()
            );
        }
    }

    private static void playPlayerChat(RecordablePlayerChat rec, ReplaySession session) {
        String playerName = session.getEntityDisplayName(rec.getEntityId());
        Text message = rec.isShout()
            ? Text.of("<6>[SHOUT] <r>{}<r>: {}", playerName, rec.getMessage())
            : Text.of("<7>{}<7>: <f>{}", playerName, rec.getMessage());

        for (var viewer : session.getViewers()) {
            if (viewer instanceof HypixelPlayer hypixelPlayer
                && !ReplaySettingsUtil.getSettings(hypixelPlayer).isChatMessages()) {
                continue;
            }
            viewer.sendMessage(message);
        }
    }

    private static void playPlayerHealth(RecordablePlayerHealth rec, ReplaySession session) {
        session.getStateTracker().trackHealth(rec.getEntityId(), rec.getHealth(), rec.getMaxHealth());
        Entity entity = session.getEntityManager().getEntity(rec.getEntityId());
        if (entity instanceof LivingEntity livingEntity) {
            livingEntity.setHealth(rec.getHealth());
        }

        session.updateBelowNameScore(rec.getEntityId(), (int) rec.getHealth());
    }

    private static void playBedDestruction(RecordableBedDestruction rec, ReplaySession session) {
        String teamName = getTeamName(rec.getTeamId());
        TextColor teamColor = getTeamColor(rec.getTeamId());

        UUID destroyerUuid = getEntityUuid(session, rec.getDestroyerEntityId());
        TextColor destroyerColor = getTeamColorForPlayer(session, destroyerUuid);
        String destroyerName = session.getEntityDisplayName(rec.getDestroyerEntityId());

        Text message = Text.of(
            "<f><l>BED DESTRUCTION > </l><color:{}>{} Bed <7>has been destroyed by <color:{}>{}<7>!",
            teamColor, teamName, destroyerColor, destroyerName
        );

        for (var viewer : session.getViewers()) {
            if (viewer instanceof HypixelPlayer hypixelPlayer
                && !ReplaySettingsUtil.getSettings(hypixelPlayer).isChatMessages()) {
                continue;
            }
            viewer.sendMessage("");
            viewer.sendMessage(message);
            viewer.sendMessage("");
        }
    }

    private static void playFinalKill(RecordableKill rec, ReplaySession session) {
        String victimBaseName = session.getMetadata().getPlayers().get(rec.getVictimUuid());
        if (victimBaseName == null) {
            victimBaseName = String.valueOf(rec.getVictimEntityId());
        }
        TextColor victimColor = getTeamColor(rec.getVictimTeamId());

        String killerBaseName = null;
        TextColor killerColor = null;
        if (rec.getKillerUuid() != null) {
            killerBaseName = session.getMetadata().getPlayers().get(rec.getKillerUuid());
            if (killerBaseName != null) {
                killerColor = getTeamColorForPlayer(session, rec.getKillerUuid());
            }
        }

        Text deathMessage = switch (rec.getDeathCause()) {
            case 0 -> Text.of("<7><color:{}>{}<7> died.", victimColor, victimBaseName);
            case 1 -> killerBaseName != null
                ? Text.of("<7><color:{}>{}<7> was killed by <color:{}>{}<7>.", victimColor, victimBaseName, killerColor, killerBaseName)
                : Text.of("<7><color:{}>{}<7> died.", victimColor, victimBaseName);
            case 2 -> Text.of("<7><color:{}>{}<7> fell into the void.", victimColor, victimBaseName);
            case 3 -> killerBaseName != null
                ? Text.of("<7><color:{}>{}<7> was knocked into the void by <color:{}>{}<7>.", victimColor, victimBaseName, killerColor, killerBaseName)
                : Text.of("<7><color:{}>{}<7> fell into the void.", victimColor, victimBaseName);
            case 4 -> killerBaseName != null
                ? Text.of("<7><color:{}>{}<7> was shot by <color:{}>{}<7>.", victimColor, victimBaseName, killerColor, killerBaseName)
                : Text.of("<7><color:{}>{}<7> died.", victimColor, victimBaseName);
            case 5 -> killerBaseName != null
                ? Text.of("<7><color:{}>{}<7> was slain by <color:{}>{}<7>'s entity.", victimColor, victimBaseName, killerColor, killerBaseName)
                : Text.of("<7><color:{}>{}<7> died.", victimColor, victimBaseName);
            default -> Text.of("<7><color:{}>{}<7> died.", victimColor, victimBaseName);
        };

        Text message = rec.getFinalKill() != 0 ? deathMessage.append(" <b><l>FINAL KILL!") : deathMessage;
        for (var viewer : session.getViewers()) {
            if (viewer instanceof HypixelPlayer hypixelPlayer
                && !ReplaySettingsUtil.getSettings(hypixelPlayer).isChatMessages()) {
                continue;
            }
            viewer.sendMessage(message);
        }
    }

    private static void playTeamElimination(RecordableTeamElimination rec, ReplaySession session) {
        String teamName = getTeamName(rec.getTeamId());
        TextColor teamColor = getTeamColor(rec.getTeamId());
        Text message = Text.of(
            "<f><l>TEAM ELIMINATED > </l><color:{}>{} <7>has been eliminated!",
            teamColor, teamName
        );
        for (var viewer : session.getViewers()) {
            if (viewer instanceof HypixelPlayer hypixelPlayer
                && !ReplaySettingsUtil.getSettings(hypixelPlayer).isChatMessages()) {
                continue;
            }
            viewer.sendMessage("");
            viewer.sendMessage(message);
            viewer.sendMessage("");
        }
    }

    private static void playDroppedItem(RecordableDroppedItem rec, ReplaySession session) {
        Pos pos = new Pos(rec.getX(), rec.getY(), rec.getZ());

        ReplayDroppedItemEntity itemEntity = new ReplayDroppedItemEntity(
            rec.getEntityId(),
            rec.getEntityUuid(),
            rec.getItemNbt(),
            rec.getDespawnTick()
        );
        if (session.isRebuildingState()) {
            itemEntity.setAutoViewable(false);
        }

        itemEntity.setItemVelocity(rec.getVelocityX(), rec.getVelocityY(), rec.getVelocityZ());
        session.getEntityManager().spawnEntity(rec.getEntityId(), itemEntity, pos);

        // Track for auto-despawn
        session.getDroppedItemManager().trackItem(rec.getEntityId(), rec.getDespawnTick());
    }

    private static void playItemPickup(RecordableItemPickup rec, ReplaySession session) {
        // Remove the item entity (it was picked up)
        session.getEntityManager().despawnEntity(rec.getItemEntityId());
        session.getDroppedItemManager().untrackItem(rec.getItemEntityId());
    }

    private static void playDynamicTextDisplay(RecordableDynamicTextDisplay rec, ReplaySession session) {
        Pos pos = new Pos(rec.getX(), rec.getY(), rec.getZ());

        session.getDynamicTextManager().createDisplay(
            rec.getEntityId(),
            rec.getEntityUuid(),
            pos,
            rec.getTextLines(),
            rec.getDisplayType(),
            rec.getDisplayIdentifier(),
            rec.getTick()
        );
    }

    private static void playTextDisplayUpdate(RecordableTextDisplayUpdate rec, ReplaySession session) {
        session.getDynamicTextManager().updateDisplayText(
            rec.getEntityId(),
            rec.getNewTextLines(),
            rec.isReplaceAll(),
            rec.getStartLineIndex(),
            rec.getTick()
        );
    }

    private static void playNpcDisplayName(RecordableNpcDisplayName rec, ReplaySession session) {
        session.getNpcManager().updateNpcDisplayName(
            rec.getEntityId(),
            rec.getDisplayName(),
            rec.getPrefix(),
            rec.getSuffix(),
            rec.getNameColor(),
            rec.isVisible()
        );
    }

    private static void playNpcTextLine(RecordableNpcTextLine rec, ReplaySession session) {
        session.getNpcManager().updateNpcTextLines(
            rec.getEntityId(),
            rec.getTextLines(),
            rec.getYOffset(),
            rec.getDisplayDurationTicks()
        );
    }

    private static String getTeamName(byte teamId) {
        return switch (teamId) {
            case 0 -> "Red";
            case 1 -> "Blue";
            case 2 -> "Green";
            case 3 -> "Yellow";
            case 4 -> "Aqua";
            case 5 -> "White";
            case 6 -> "Pink";
            case 7 -> "Gray";
            default -> "Team " + teamId;
        };
    }

    private static TextColor getTeamColor(byte teamId) {
        return switch (teamId) {
            case 0 -> NamedTextColor.RED;
            case 1 -> NamedTextColor.BLUE;
            case 2 -> NamedTextColor.GREEN;
            case 3 -> NamedTextColor.YELLOW;
            case 4 -> NamedTextColor.AQUA;
            case 5 -> NamedTextColor.WHITE;
            case 6 -> NamedTextColor.LIGHT_PURPLE;
            default -> NamedTextColor.GRAY;
        };
    }

    private static UUID getEntityUuid(ReplaySession session, int entityId) {
        Entity entity = session.getEntityManager().getEntity(entityId);
        if (entity instanceof ReplayPlayerEntity playerEntity) {
            return playerEntity.getActualUuid();
        }
        if (entity instanceof ReplayEntity replayEntity) {
            return replayEntity.getRecordedUuid();
        }
        return null;
    }

    private static TextColor getTeamColorForPlayer(ReplaySession session, UUID playerUuid) {
        if (playerUuid == null) {
            return NamedTextColor.GRAY;
        }

        for (Map.Entry<String, List<UUID>> entry : session.getMetadata().getTeams().entrySet()) {
            if (!entry.getValue().contains(playerUuid)) {
                continue;
            }
            try {
                byte teamId = Byte.parseByte(entry.getKey());
                return getTeamColor(teamId);
            } catch (NumberFormatException ignored) {
                switch (entry.getKey().toUpperCase()) {
                    case "RED" -> {
                        return getTeamColor((byte) 0);
                    }
                    case "BLUE" -> {
                        return getTeamColor((byte) 1);
                    }
                    case "GREEN" -> {
                        return getTeamColor((byte) 2);
                    }
                    case "YELLOW" -> {
                        return getTeamColor((byte) 3);
                    }
                    case "AQUA" -> {
                        return getTeamColor((byte) 4);
                    }
                    case "WHITE" -> {
                        return getTeamColor((byte) 5);
                    }
                    case "PINK" -> {
                        return getTeamColor((byte) 6);
                    }
                    case "GRAY", "GREY" -> {
                        return getTeamColor((byte) 7);
                    }
                    default -> {
                    }
                }
            }
        }

        return NamedTextColor.GRAY;
    }

    private static void trackEntityState(Recordable recordable, ReplaySession session) {
        session.getStateTracker().track(recordable);
    }
}
