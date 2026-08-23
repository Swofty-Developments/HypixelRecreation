package net.swofty.type.generic.entity;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.metadata.other.FallingBlockMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;

public class ExplosionBlockEntity extends Entity {
    private final Block blockType;
    private boolean hasLanded = false;
    private int ticksStationary = 0;
    private long created = 0;

    public ExplosionBlockEntity(Block block) {
        super(EntityType.FALLING_BLOCK);
        this.blockType = block;

        editEntityMeta(FallingBlockMeta.class, meta -> {
            meta.setBlock(block);
        });

        setNoGravity(false);
    }

    public void launchFrom(Pos center, double strength) {
        Pos current = getPosition();
        double dx = current.x() - center.x();
        double dz = current.z() - center.z();

        double dist = Math.sqrt(dx * dx + dz * dz);
        if (dist < 0.1) dist = 0.1;

        double vx = (dx / dist) * strength;
        double vy = strength * 0.8 + 5;
        double vz = (dz / dist) * strength;

        setVelocity(new Vec(vx, vy, vz));
        created = System.currentTimeMillis();
    }

    @Override
    public void tick(long time) {
        super.tick(time);

        if (hasLanded || isRemoved()) return;

        Vec velocity = getVelocity();
        long timePast = System.currentTimeMillis() - created;

        if (Math.abs(velocity.x()) < 0.01 && Math.abs(velocity.y()) == 0.784 && Math.abs(velocity.z()) < 0.01) {
            ticksStationary++;
            if (ticksStationary >= 3) {
                land();
            }
        } else {
            ticksStationary = 0;
        }
    }

    private void land() {
        if (hasLanded) return;
        hasLanded = true;

        Instance instance = getInstance();
        if (instance == null) {
            remove();
            return;
        }

        Pos pos = getPosition();
        int blockX = pos.blockX();
        int blockY = pos.blockY();
        int blockZ = pos.blockZ();

        Block existing = instance.getBlock(blockX, blockY, blockZ);
        if (!existing.solid()) {
            instance.setBlock(blockX, blockY, blockZ, blockType);
        }

        remove();
    }
}
