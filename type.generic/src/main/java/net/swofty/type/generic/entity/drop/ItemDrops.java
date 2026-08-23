package net.swofty.type.generic.entity.drop;

import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;

public final class ItemDrops {

    private ItemDrops() {
    }

    public static VanillaItemEntity throwFromPlayer(Player player, ItemStack itemStack) {
        return throwFromPlayer(player, new VanillaItemEntity(itemStack));
    }

    public static <T extends VanillaItemEntity> T throwFromPlayer(Player player, T entity) {
        return throwFrom(entity, player.getInstance(), player.getPosition(), player.getEyeHeight());
    }

    public static <T extends VanillaItemEntity> T throwFrom(T entity, Instance instance, Pos playerPosition,
                                                            double eyeHeight) {
        entity.setPickupDelayTicks(ItemDropPhysics.THROW_PICKUP_DELAY_TICKS);
        entity.setInstance(instance, ItemDropPhysics.throwPosition(playerPosition, eyeHeight));
        entity.setVelocity(ItemDropPhysics.throwVelocity(playerPosition.yaw(), playerPosition.pitch()));
        return entity;
    }

    public static VanillaItemEntity dropFromBlock(Instance instance, Point blockPosition, ItemStack itemStack) {
        return dropFromBlock(new VanillaItemEntity(itemStack), instance, blockPosition);
    }

    public static <T extends VanillaItemEntity> T dropFromBlock(T entity, Instance instance, Point blockPosition) {
        return dropAt(entity, instance, ItemDropPhysics.blockDropPosition(blockPosition));
    }

    public static VanillaItemEntity dropAt(Instance instance, Pos position, ItemStack itemStack) {
        return dropAt(new VanillaItemEntity(itemStack), instance, position);
    }

    public static <T extends VanillaItemEntity> T dropAt(T entity, Instance instance, Pos position) {
        entity.setPickupDelayTicks(ItemDropPhysics.BLOCK_DROP_PICKUP_DELAY_TICKS);
        entity.setInstance(instance, position);
        entity.setVelocity(ItemDropPhysics.blockDropVelocity());
        return entity;
    }
}
