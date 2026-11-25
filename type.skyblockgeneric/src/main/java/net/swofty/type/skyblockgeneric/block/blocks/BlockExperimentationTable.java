package net.swofty.type.skyblockgeneric.block.blocks;

import lombok.NonNull;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.GameMode;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.event.entity.EntityAttackEvent;
import net.minestom.server.event.player.PlayerBlockBreakEvent;
import net.minestom.server.event.player.PlayerBlockInteractEvent;
import net.minestom.server.event.player.PlayerBlockPlaceEvent;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.tag.Tag;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.skyblockgeneric.block.SkyBlockBlock;
import net.swofty.type.skyblockgeneric.block.impl.BlockBreakable;
import net.swofty.type.skyblockgeneric.block.impl.BlockInteractable;
import net.swofty.type.skyblockgeneric.block.impl.BlockPlaceable;
import net.swofty.type.skyblockgeneric.block.impl.CustomSkyBlockBlock;
import net.swofty.type.skyblockgeneric.block.impl.EntityBackedBlock;
import net.swofty.type.skyblockgeneric.furniture.Furniture;
import net.swofty.type.skyblockgeneric.furniture.FurnitureLimitPool;
import net.swofty.type.skyblockgeneric.furniture.FurniturePlacement;
import net.swofty.type.skyblockgeneric.furniture.IslandFurnitureManager;
import net.swofty.type.skyblockgeneric.gui.inventories.experiments.GUIExperiments;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.UUID;

public final class BlockExperimentationTable implements CustomSkyBlockBlock, BlockPlaceable, BlockInteractable,
        BlockBreakable, EntityBackedBlock {
    private static final Tag<String> TABLE_ID_TAG = Tag.String("experimentation_table_id");
    private static final String FURNITURE_NAME = "experimentation_table";
    private static final String DISPLAY_NAME = "Experimentation Table";
    private static final String ENTITY_BACKED_BLOCK_TYPE = "EXPERIMENTATION_TABLE";

    @Override
    public @NonNull Block getDisplayMaterial() {
        return Block.ENCHANTING_TABLE;
    }

    @Override
    public @NonNull Boolean shouldPlace(SkyBlockPlayer player) {
        return HypixelConst.isIslandServer();
    }

    @Override
    public @NonNull Boolean shouldDestroy(SkyBlockPlayer player) {
        return HypixelConst.isIslandServer();
    }

    @Override
    public void onPlace(PlayerBlockPlaceEvent event, SkyBlockBlock block) {
        if (!HypixelConst.isIslandServer()) {
            event.setCancelled(true);
            return;
        }

        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        if (!player.getSkyBlockIsland().getFurnitureManager().place(player, FurnitureLimitPool.EXPERIMENTATION_TABLE,
                "Experimentation Table")) {
            event.setCancelled(true);
            return;
        }

        Point position = event.getBlockPosition();
        UUID tableId = UUID.randomUUID();
        float rotationYaw = Furniture.facingPlayerYaw(player.getPosition().yaw());
        try {
            List<LivingEntity> entities = Furniture.load(
                    FURNITURE_NAME,
                    event.getInstance(),
                    position.asPos().add(0.5, 0, 0.5),
                    rotationYaw
            );
            player.getSkyBlockIsland().getFurnitureManager().register(new FurniturePlacement(
                    tableId,
                    FURNITURE_NAME,
                    FurnitureLimitPool.EXPERIMENTATION_TABLE,
                    DISPLAY_NAME,
                    position.x(),
                    position.y(),
                    position.z(),
                    position.x() + 0.5,
                    position.y(),
                    position.z() + 0.5,
                    ENTITY_BACKED_BLOCK_TYPE,
                    rotationYaw
            ), entities);
            event.setBlock(Block.AIR);
        } catch (RuntimeException exception) {
            player.getSkyBlockIsland().getFurnitureManager().rollbackPlace(FurnitureLimitPool.EXPERIMENTATION_TABLE);
            event.setCancelled(true);
            player.sendMessage("<c>There was a problem placing the Experimentation Table.");
        }
    }

    @Override
    public void onInteract(PlayerBlockInteractEvent event, SkyBlockBlock block) {
        if (!HypixelConst.isIslandServer()) return;

        event.setBlockingItemUse(true);
        ((SkyBlockPlayer) event.getPlayer()).openView(new GUIExperiments());
    }

    @Override
    public void onBreak(PlayerBlockBreakEvent event, SkyBlockBlock block) {
        if (!HypixelConst.isIslandServer()) return;

        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        IslandFurnitureManager manager = player.getSkyBlockIsland().getFurnitureManager();
        FurniturePlacement placement = placementAt(event, manager);
        UUID tableId = parseId(event.getBlock().getTag(TABLE_ID_TAG));
        boolean removed = tableId != null && manager.remove(player, tableId);
        if (!removed && placement != null) removed = manager.remove(player, placement.id());
        if (!removed) manager.remove(player, FurnitureLimitPool.EXPERIMENTATION_TABLE, DISPLAY_NAME);

        event.setResultBlock(Block.AIR);
        if (event.getPlayer().getGameMode() != GameMode.CREATIVE) {
            player.addAndUpdateItem(new SkyBlockItem(ItemType.EXPERIMENTATION_TABLE));
        }
    }

    @Override
    public void onEntityInteract(PlayerEntityInteractEvent event, SkyBlockBlock block, String tableId) {
        if (!HypixelConst.isIslandServer()) return;

        try {
            if (((SkyBlockPlayer) event.getPlayer()).getSkyBlockIsland().getFurnitureManager()
                    .hasPlacement(UUID.fromString(tableId))) {
                ((SkyBlockPlayer) event.getPlayer()).openView(new GUIExperiments());
            }
        } catch (IllegalArgumentException ignored) {
        }
    }

    @Override
    public void onEntityAttack(EntityAttackEvent event, SkyBlockBlock block, String tableId) {
        if (!HypixelConst.isIslandServer()) return;

        try {
            UUID placementId = UUID.fromString(tableId);
            SkyBlockPlayer player = (SkyBlockPlayer) event.getEntity();
            IslandFurnitureManager manager = player.getSkyBlockIsland().getFurnitureManager();
            FurniturePlacement placement = manager.getPlacement(placementId);
            if (placement == null) return;

            if (!manager.remove(player, placementId)) return;
            Instance instance = event.getTarget().getInstance();
            if (instance != null) {
                instance.setBlock((int) placement.blockX(), (int) placement.blockY(), (int) placement.blockZ(), Block.AIR);
            }
            if (player.getGameMode() != GameMode.CREATIVE) {
                player.addAndUpdateItem(new SkyBlockItem(ItemType.EXPERIMENTATION_TABLE));
            }
        } catch (IllegalArgumentException ignored) {
        }
    }

    private static FurniturePlacement placementAt(PlayerBlockBreakEvent event, IslandFurnitureManager manager) {
        Point position = event.getBlockPosition();
        return manager.findPlacementAt(position.x(), position.y(), position.z());
    }

    private static UUID parseId(String tableId) {
        if (tableId == null) return null;
        try {
            return UUID.fromString(tableId);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }
}
