package net.swofty.type.bedwarsconfigurator.autosetup;

import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.avatar.MannequinMeta;
import net.minestom.server.entity.metadata.cube.SlimeMeta;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.entity.metadata.other.ArmorStandMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.bedwars.map.BedWarsMapsConfig.TeamKey;
import net.swofty.commons.mc.HypixelPosition;

import java.util.*;

public class DebugMarkerManager {

    private static final Map<UUID, List<Entity>> playerMarkers = new HashMap<>();

    public static void showMarkers(UUID playerUuid, AutoSetupSession session, Instance instance) {
        hideMarkers(playerUuid);

        List<Entity> markers = new ArrayList<>();

        for (Map.Entry<TeamKey, AutoSetupSession.TeamConfig> entry : session.getTeams().entrySet()) {
            TeamKey team = entry.getKey();
            AutoSetupSession.TeamConfig config = entry.getValue();
            String teamColor = team.chatColor();

            if (config.getBedFeet() != null) {
                markers.add(createBlockHighlight(instance, config.getBedFeet().asHypixelPosition(), teamColor + team.getName() + " Bed (Feet)"));
            }
            if (config.getBedHead() != null) {
                markers.add(createBlockHighlight(instance, config.getBedHead().asHypixelPosition(), teamColor + team.getName() + " Bed (Head)"));
            }

            if (config.getSpawn() != null) {
                markers.add(createMannequin(instance, config.getSpawn(), teamColor + team.getName() + " Spawn"));
            }

            if (config.getGenerator() != null) {
                markers.addAll(createGeneratorMarkers(instance, config.getGenerator(), teamColor + team.getName() + " Generator", Material.IRON_INGOT));
            }

            if (config.getItemShop() != null) {
                markers.add(createMannequin(instance, config.getItemShop(), teamColor + team.getName() + " Item Shop"));
            }
            if (config.getTeamShop() != null) {
                markers.add(createMannequin(instance, config.getTeamShop(), teamColor + team.getName() + " Team Shop"));
            }
        }

        // Diamond generators
        int diamondIndex = 1;
        for (HypixelPosition pos : session.getDiamondGenerators()) {
            markers.addAll(createGeneratorMarkers(instance, pos, "§bDiamond Gen #" + diamondIndex++, Material.DIAMOND_BLOCK));
        }

        // Emerald generators
        int emeraldIndex = 1;
        for (HypixelPosition pos : session.getEmeraldGenerators()) {
            markers.addAll(createGeneratorMarkers(instance, pos, "§aEmerald Gen #" + emeraldIndex++, Material.EMERALD_BLOCK));
        }

        // Waiting location
        if (session.getWaitingLocation() != null) {
            HypixelPosition pos = new HypixelPosition(session.getWaitingLocation().x(), session.getWaitingLocation().y(), session.getWaitingLocation().z());
            markers.add(createMarker(instance, pos, "§eWaiting Spawn", Material.CLOCK));
        }

        // Spectator location
        if (session.getSpectatorLocation() != null) {
            HypixelPosition pos = new HypixelPosition(session.getSpectatorLocation().x(), session.getSpectatorLocation().y(), session.getSpectatorLocation().z());
            markers.add(createMarker(instance, pos, "§7Spectator Spawn", Material.ENDER_EYE));
        }

        // Bounds markers (corners)
        if (session.hasBounds()) {
            markers.add(createMarker(instance, new HypixelPosition(session.getMinX(), session.getMinY(), session.getMinZ()), "§8Bounds Min", Material.BARRIER));
            markers.add(createMarker(instance, new HypixelPosition(session.getMaxX(), session.getMaxY(), session.getMaxZ()), "§8Bounds Max", Material.BARRIER));
        }

        playerMarkers.put(playerUuid, markers);
    }

    public static void hideMarkers(UUID playerUuid) {
        List<Entity> markers = playerMarkers.remove(playerUuid);
        if (markers != null) {
            for (Entity marker : markers) {
                marker.remove();
            }
        }
    }

    private static Entity createMarker(Instance instance, HypixelPosition pos, String label, Material headItem) {
        LivingEntity armorStand = new LivingEntity(EntityType.ARMOR_STAND);

        ArmorStandMeta meta = (ArmorStandMeta) armorStand.getEntityMeta();
        meta.setMarker(true);
        meta.setInvisible(true);
        meta.setHasNoGravity(true);
        meta.setSmall(true);
        meta.setCustomNameVisible(true);

        armorStand.set(DataComponents.CUSTOM_NAME, Component.text(label));
        armorStand.setEquipment(EquipmentSlot.HELMET, ItemStack.of(headItem));

        armorStand.setInstance(instance, new Pos(pos.x(), pos.y() + 1.5, pos.z()));

        // floating animation
        MinecraftServer.getSchedulerManager().scheduleTask(() -> {
            if (armorStand.isRemoved()) return TaskSchedule.stop();

            double time = System.currentTimeMillis() / 500.0;
            double yOffset = Math.sin(time) * 0.1;
            Pos currentPos = armorStand.getPosition();
            armorStand.teleport(currentPos.withY(pos.y() + 1.5 + yOffset));

            return TaskSchedule.tick(2);
        }, TaskSchedule.immediate());

        return armorStand;
    }

    private static Entity createMannequin(Instance instance, HypixelPosition pos, String label) {
        Entity mannequin = new Entity(EntityType.MANNEQUIN);
        mannequin.setNoGravity(true);
        mannequin.setCustomName(Component.text(label));
        mannequin.setCustomNameVisible(true);
        mannequin.editEntityMeta(MannequinMeta.class, meta -> meta.setImmovable(true));
        mannequin.setInstance(instance, new Pos(pos.x(), pos.y(), pos.z(), pos.yaw(), pos.pitch()));
        return mannequin;
    }

    private static Entity createBlockHighlight(Instance instance, HypixelPosition pos, String label) {
        LivingEntity slime = new LivingEntity(EntityType.SLIME);
        slime.setInvisible(true);
        slime.setGlowing(true);
        slime.setNoGravity(true);
        slime.setCustomName(Component.text(label));
        slime.setCustomNameVisible(true);
        slime.editEntityMeta(SlimeMeta.class, meta -> meta.setSize(2));
        slime.setInstance(instance, new Pos(pos.x() + 0.5, pos.y(), pos.z() + 0.5));
        return slime;
    }

    private static List<Entity> createGeneratorMarkers(Instance instance, HypixelPosition pos, String label, Material item) {
        Entity itemDisplay = new Entity(EntityType.ITEM_DISPLAY);
        itemDisplay.setNoGravity(true);
        itemDisplay.editEntityMeta(ItemDisplayMeta.class, meta -> {
            meta.setItemStack(ItemStack.of(item));
            meta.setDisplayContext(ItemDisplayMeta.DisplayContext.GROUND);
        });
        itemDisplay.setInstance(instance, new Pos(pos.x(), pos.y(), pos.z()));

        LivingEntity text = new LivingEntity(EntityType.ARMOR_STAND);
        ArmorStandMeta meta = (ArmorStandMeta) text.getEntityMeta();
        meta.setMarker(true);
        meta.setInvisible(true);
        meta.setHasNoGravity(true);
        meta.setCustomNameVisible(true);
        text.set(DataComponents.CUSTOM_NAME, Component.text(label));
        text.setInstance(instance, new Pos(pos.x(), pos.y() + 1.25, pos.z()));
        return List.of(itemDisplay, text);
    }

    public static Entity createSingleMarker(Instance instance, double x, double y, double z, String label) {
        return createMarker(instance, new HypixelPosition(x, y, z), label, Material.ARMOR_STAND);
    }

    public static void refreshMarkers(UUID playerUuid, AutoSetupSession session, Instance instance) {
        if (playerMarkers.containsKey(playerUuid)) {
            showMarkers(playerUuid, session, instance);
        }
    }

    public static boolean areMarkersShown(UUID playerUuid) {
        return playerMarkers.containsKey(playerUuid) && !playerMarkers.get(playerUuid).isEmpty();
    }
}
