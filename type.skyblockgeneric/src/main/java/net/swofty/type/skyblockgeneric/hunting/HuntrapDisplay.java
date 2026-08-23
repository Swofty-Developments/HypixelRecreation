package net.swofty.type.skyblockgeneric.hunting;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.LivingEntity;
import net.minestom.server.entity.metadata.display.ItemDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.entity.BlockDisplayEntity;
import net.swofty.type.generic.entity.InteractionEntity;
import net.swofty.type.skyblockgeneric.entity.TextDisplayEntity;
import net.swofty.type.skyblockgeneric.item.components.AttributeShardComponent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public final class HuntrapDisplay {
    private final List<Entity> entities = new ArrayList<>();

    public HuntrapDisplay(Instance instance, Pos position, UUID owner, HuntrapTier tier,
                          AttributeDefinition caught, long catchAt, Consumer<SkyBlockPlayer> claim) {
        boolean closed = caught != null;
        float size = 0.6f;
        Side[] sides = {
                new Side(-size / 2, 0, -size / 2 + 0.1f, -90, 0),
                new Side(-size / 2 + 0.1f, 0, size / 2, -90, 90),
                new Side(size / 2, 0, size / 2, -90, 90),
                new Side(-size / 2, 0.3f, size / 2, -90, 0),
                closed ? new Side(-size / 2, 0.5f, -size / 2, 0, 0)
                        : new Side(-size / 2, 0.5f, size / 2, -90, 0)
        };
        for (Side side : sides) {
            BlockDisplayEntity display = new BlockDisplayEntity(Block.OAK_TRAPDOOR, meta -> {
                meta.setScale(new Vec(size, size, size));
                meta.setLeftRotation(rotation(side.xRotation, side.zRotation));
                meta.setTranslation(new Vec(side.x, side.y, side.z));
            });
            owned(display, owner);
            display.setInstance(instance, position);
            entities.add(display);
        }
        if (caught != null) {
            LivingEntity shard = new LivingEntity(net.minestom.server.entity.EntityType.ITEM_DISPLAY);
            shard.editEntityMeta(ItemDisplayMeta.class, meta -> {
                meta.setItemStack(AttributeShardComponent.create(caught, 1).getItemStack());
                meta.setScale(new Vec(0.45, 0.45, 0.45));
                meta.setTranslation(new Vec(0, 0.18, 0));
                meta.setHasNoGravity(true);
            });
            owned(shard, owner);
            shard.setInstance(instance, position.add(0, 0.18, 0));
            entities.add(shard);
        }
        Text message = caught == null
                ? Text.of("<f>Status: <c>Empty\n<f>Massive Time: <a>{}", remaining(catchAt))
                : Text.of("<f>Status: <a>Caught!\n<e>Click to collect!");
        TextDisplayEntity text = new TextDisplayEntity(message, meta -> {
        });
        owned(text, owner);
        text.setInstance(instance, position.add(0.5, 1.15, 0.5));
        entities.add(text);
        InteractionEntity interaction = new InteractionEntity(1, 1, (player, event) -> claim.accept((SkyBlockPlayer) player));
        owned(interaction, owner);
        interaction.setInstance(instance, position.add(0.5, 0.5, 0.5));
        entities.add(interaction);
    }

    public void remove() {
        entities.forEach(Entity::remove);
        entities.clear();
    }

    private static void owned(Entity entity, UUID owner) {
        entity.updateViewableRule(player -> player.getUuid().equals(owner));
    }

    private static String remaining(long catchAt) {
        long seconds = Math.max(0, (catchAt - System.currentTimeMillis()) / 1000);
        return "%dh %02dm %02ds".formatted(seconds / 3600, seconds / 60 % 60, seconds % 60);
    }

    private static float[] rotation(float x, float z) {
        double halfX = Math.toRadians(x) / 2;
        double halfZ = Math.toRadians(z) / 2;
        float sinX = (float) Math.sin(halfX);
        float cosX = (float) Math.cos(halfX);
        float sinZ = (float) Math.sin(halfZ);
        float cosZ = (float) Math.cos(halfZ);
        return new float[]{sinX * cosZ, -sinX * sinZ, cosX * sinZ, cosX * cosZ};
    }

    private record Side(float x, float y, float z, float xRotation, float zRotation) {
    }
}
