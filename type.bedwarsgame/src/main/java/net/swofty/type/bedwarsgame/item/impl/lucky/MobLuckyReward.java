package net.swofty.type.bedwarsgame.item.impl.lucky;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EntityCreature;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.ai.goal.MeleeAttackGoal;
import net.minestom.server.entity.ai.target.ClosestEntityTarget;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.metadata.monster.zombie.ZombieMeta;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.utils.time.TimeUnit;
import net.swofty.type.bedwarsgame.user.BedWarsPlayer;
import net.swofty.type.generic.utility.EntityUtility;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class MobLuckyReward extends LuckyReward {
    private static final Duration MOB_DESPAWN = Duration.ofSeconds(75);

    private final EntityType type;
    private final int count;
    private final boolean hostile;

    public MobLuckyReward(String name, EntityType type, int count, boolean hostile) {
        super(name);
        this.type = type;
        this.count = count;
        this.hostile = hostile;
    }

    @Override
    public void apply(BedWarsPlayer player, Pos openedAt) {
        for (int i = 0; i < count; i++) {
            EntityCreature mob = new EntityCreature(type);
            EntityUtility.nameEntityVisible(mob, "<e>{}", name());
            if (type == EntityType.ZOMBIE && name().contains("Baby") && mob.getEntityMeta() instanceof ZombieMeta meta) {
                meta.setBaby(true);
                mob.setItemInMainHand(ItemStack.of(Material.DIAMOND_SWORD));
            }
            mob.getAttribute(Attribute.MOVEMENT_SPEED).setBaseValue(0.25);
            mob.setInstance(player.getInstance(), openedAt.add(
                ThreadLocalRandom.current().nextDouble(-1, 1),
                0,
                ThreadLocalRandom.current().nextDouble(-1, 1)));
            if (hostile) {
                mob.addAIGroup(
                    List.of(new MeleeAttackGoal(mob, 1.0, 20, TimeUnit.SERVER_TICK)),
                    List.of(new ClosestEntityTarget(mob, 32, entity -> entity instanceof BedWarsPlayer))
                );
            }
            mob.scheduler().buildTask(mob::remove).delay(MOB_DESPAWN).schedule();
        }
    }
}
