package io.github.term4.polyp.util;

import net.minestom.server.instance.block.Block;
import net.minestom.server.instance.block.BlockFace;
import org.jetbrains.annotations.NotNull;

/**
 * A block the entity's bounding box intersects (from {@link BlockContact#scanShapes}); {@code face} is the contact face
 * along the shallowest overlap axis (entity relative to block center).
 */
public record BlockContactHit(int blockX, int blockY, int blockZ, @NotNull Block block, @NotNull BlockFace face) {}
