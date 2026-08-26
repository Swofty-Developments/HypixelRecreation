package net.swofty.type.skywarsgame.luckyblock.items.usables;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.instance.Instance;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.skywarsgame.TypeSkywarsGameLoader;
import net.swofty.type.skywarsgame.game.SkywarsGame;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockConsumable;
import net.swofty.type.skywarsgame.luckyblock.items.LuckyBlockItemRegistry;
import net.swofty.type.skywarsgame.user.SkywarsPlayer;

public class BerserkersRock implements LuckyBlockConsumable {

    private static final double SLAM_RADIUS = 5.0;
    private static final float SLAM_DAMAGE = 4.0f;
    private static final double KNOCKBACK_POWER = 25.0;
    private static final double UPWARD_POWER = 15.0;

    @Override
    public String getId() {
        return "berserkers_rock";
    }

    @Override
    public String getDisplayName() {
        return "Berserker's Rock";
    }

    @Override
    public ItemStack createItemStack() {
        return ItemStackCreator.item(Material.COBBLESTONE, """
                <c><l>Berserker's Rock</l>
                <7>Unleash a devastating
                <7>ground slam!

                <7>Damages and knocks back
                <7>all nearby enemies!

                <e>Right-click to use!""")
                .set(LuckyBlockItemRegistry.LUCKY_BLOCK_ITEM_TAG, getId())
                .build();
    }

    @Override
    public void onConsume(SkywarsPlayer player) {
        Instance instance = player.getInstance();
        if (instance == null) return;

        SkywarsGame game = TypeSkywarsGameLoader.getPlayerGame(player);
        if (game == null) return;

        Pos playerPos = player.getPosition();

        ParticlePacket explosionPacket = new ParticlePacket(
                Particle.EXPLOSION,
                false,
                false,
                playerPos.x(), playerPos.y() + 0.5, playerPos.z(),
                1.5f, 0.5f, 1.5f,
                0.1f,
                15
        );
        instance.sendGroupedPacket(explosionPacket);

        for (SkywarsPlayer target : game.getPlayers()) {
            if (target.equals(player) || target.isEliminated()) continue;

            double distance = target.getPosition().distance(playerPos);
            if (distance > SLAM_RADIUS) continue;

            target.damage(Damage.fromEntity(player, SLAM_DAMAGE));

            Vec direction = Vec.fromPoint(target.getPosition().sub(playerPos)).normalize();
            Vec knockback = direction.mul(KNOCKBACK_POWER).add(0, UPWARD_POWER, 0);
            target.setVelocity(knockback);

            target.sendMessage("<c>You were hit by a ground slam!");
        }

        player.sendMessage("<c>Ground Slam!");
    }
}
