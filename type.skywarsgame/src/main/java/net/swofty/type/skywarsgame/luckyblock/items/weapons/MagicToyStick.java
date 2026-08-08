package net.swofty.type.skywarsgame.luckyblock.items.weapons;

import io.github.term4.polyp.Polyp;
import io.github.term4.polyp.mechanics.projectile.ProjectileBehavior;
import io.github.term4.polyp.mechanics.projectile.ProjectileSnapshot;
import io.github.term4.polyp.mechanics.projectile.entities.ManagedProjectile;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.minestom.server.coordinate.Point;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.Player;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockWeapon;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.util.List;

public class MagicToyStick implements LuckyBlockWeapon {

    public static final String ID = "magic_toy_stick";
    private static final double EXPLOSION_RADIUS = 5.0;
    private static final double KNOCKBACK_POWER = 30.0;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDisplayName() {
        return "Magic Toy Stick";
    }

    @Override
    public Material getBaseMaterial() {
        return Material.STICK;
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStack.builder(Material.STICK)
                .customName(Component.text("Magic Toy Stick", NamedTextColor.AQUA)
                        .decoration(TextDecoration.ITALIC, false))
                .lore(List.of(
                        Component.empty(),
                        Component.text("Launches a magic projectile", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("that explodes on impact!", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.empty(),
                        Component.text("No block damage, but", NamedTextColor.GRAY)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.text("launches players away!", NamedTextColor.RED)
                                .decoration(TextDecoration.ITALIC, false),
                        Component.empty(),
                        Component.text("Uses: ", NamedTextColor.GRAY)
                                .append(Component.text("1", NamedTextColor.GREEN))
                                .decoration(TextDecoration.ITALIC, false),
                        Component.empty(),
                        Component.text("LUCKY BLOCK ITEM", NamedTextColor.GOLD)
                                .decoration(TextDecoration.ITALIC, false)
                                .decoration(TextDecoration.BOLD, true)
                ))
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, ID)
                .build();
    }

    @Override
    public boolean onUse(SkywarsPlayer holder) {
        Instance instance = holder.getInstance();
        if (instance == null) return false;

        var projectile = Polyp.getInstance().services().projectiles().launch(
                ProjectileSnapshot.of(holder, io.github.term4.polyp.mechanics.projectile.types.Snowball.INSTANCE)
                        .withVelocity(holder.getPosition().direction().mul(2))
                        .withBehavior(new ProjectileBehavior() {
                            @Override
                            public void onImpact(ManagedProjectile projectile, @Nullable Entity hit) {
                                explode(projectile, instance);
                            }
                        }));

        if (projectile != null) projectile.scheduler().buildTask(projectile::remove)
                .delay(Duration.ofSeconds(10))
                .schedule();

        return true;
    }

    @Override
    public float onWeaponHit(SkywarsPlayer holder, Entity target, float damage) {
        return damage;
    }

    @Override
    public int getMaxUses() {
        return 1;
    }

    @Override
    public boolean hasUseEffect() {
        return true;
    }

    @Override
    public boolean hasOnHitEffect() {
        return false;
    }

    private static void explode(ManagedProjectile projectile, Instance instance) {
        Point impactPos = projectile.getPosition();

            for (Entity entity : instance.getEntities()) {
                if (entity == projectile || entity == projectile.getShooter()) continue;
                if (!(entity instanceof Player player)) continue;
                if (player.getGameMode().name().equals("SPECTATOR")) continue;

                double distance = entity.getPosition().distance(impactPos);
                if (distance > EXPLOSION_RADIUS) continue;

                Vec direction = entity.getPosition().sub(impactPos).asVec().normalize();
                double power = KNOCKBACK_POWER * (1 - (distance / EXPLOSION_RADIUS));

                Vec knockback = new Vec(
                        direction.x() * power,
                        Math.max(15, power * 0.5),
                        direction.z() * power
                );

                entity.setVelocity(knockback);
            }
    }
}
