package net.swofty.type.generic.entity.npc.impl;

import lombok.Getter;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.*;
import net.minestom.server.entity.metadata.villager.VillagerMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.entity.hologram.PlayerHolograms;
import net.swofty.type.generic.entity.npc.NPCMovementController;
import net.swofty.type.generic.entity.npc.configuration.VillagerConfiguration;
import net.swofty.type.generic.user.HypixelPlayer;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

import java.util.*;

@Getter
public class NPCVillagerEntityImpl extends EntityCreature implements NPCViewable {
    private final List<HypixelPlayer> inRangeOf = Collections.synchronizedList(new ArrayList<>());
    private final HypixelPlayer viewer;
    private final PlayerHolograms.ExternalPlayerHologram holo;
    private final VillagerConfiguration config;
    private final NPCMovementController movementController;
    private String[] holograms;
    private Pos lastHologramPosition;

    public NPCVillagerEntityImpl(@NotNull HypixelPlayer viewer, Pos pos, @NotNull String bottomDisplay, VillagerProfession profession, VillagerConfiguration config, String[] holograms) {
        super(EntityType.VILLAGER);
        this.viewer = viewer;
        this.config = config;
        this.movementController = new NPCMovementController(this);
        this.holograms = holograms;
        this.lastHologramPosition = pos;


        VillagerMeta meta = (VillagerMeta) this.entityMeta;
        meta.setVillagerData(new VillagerMeta.VillagerData(
            VillagerType.PLAINS, profession, VillagerMeta.Level.EXPERT)
        );

        this.setCustomNameVisible(false);
        setAutoViewable(false);
        setNoGravity(true);

        PlayerHolograms.ExternalPlayerHologram holo = PlayerHolograms.ExternalPlayerHologram.builder()
            .pos(pos.add(0, getBoundingBox().height() - 0.1f, 0))
            .text(holograms)
            .player(viewer)
            .instance(config.instance())
            .build();

        this.holo = holo;
        PlayerHolograms.addExternalPlayerHologram(holo);
        setInstance(config.instance(), pos);
        addViewer(viewer);
        setPose(config.pose(viewer));
    }

    @Override
    public void remove() {
        movementController.stop();
        super.remove();
        PlayerHolograms.removeExternalPlayerHologram(holo);
    }

    @Override
    public void updateNewViewer(@NonNull Player player) {
        super.updateNewViewer(player);

        Map<EquipmentSlot, ItemStack> equipment = config.equipment((HypixelPlayer) player);
        if (equipment != null) {
            for (Map.Entry<EquipmentSlot, ItemStack> entry : equipment.entrySet()) {
                syncEntityEquipment(entry.getKey(), entry.getValue());
            }
        }
    }

    /**
     * Clears the cache for a player, is only run on quit
     * @param player The player to clear the cache for
     */
    public void clearCache(HypixelPlayer player) {
        inRangeOf.remove(player);
    }

    @Override
    public void tick(long time) {
        Instance instance = getInstance();
        Pos position = getPosition();

        if (instance == null) {
            return;
        }

        if (!instance.isChunkLoaded(position)) {
            instance.loadChunk(position).join();
        }

        try {
            super.tick(time);
        } catch (Exception e) {
            // Suppress odd warnings
        }
    }

    @Override
    public void updateNPC() {
        Pos npcPosition = getPosition();
        if (!npcPosition.asVec().equals(lastHologramPosition.asVec())) {
            PlayerHolograms.relocateExternalPlayerHologram(holo, npcPosition.add(0, getBoundingBox().height() - 0.1f, 0));
            lastHologramPosition = npcPosition;
        }

        if (!getPose().equals(config.pose(viewer))) {
            setPose(config.pose(viewer));
        }

        String[] newHolograms = config.hologramTexts(viewer).stream()
                .map(Text::serialize)
                .toArray(String[]::new);
        if (!Arrays.equals(newHolograms, holograms)) {
            PlayerHolograms.updateExternalPlayerHologramText(holo, newHolograms);
            this.holograms = newHolograms;
        }
    }
}
