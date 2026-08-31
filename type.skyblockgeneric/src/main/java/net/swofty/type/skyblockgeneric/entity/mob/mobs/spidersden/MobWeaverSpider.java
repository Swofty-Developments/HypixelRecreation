package net.swofty.type.skyblockgeneric.entity.mob.mobs.spidersden;

import net.minestom.server.entity.Entity;
import net.minestom.server.entity.damage.EntityDamage;
import net.minestom.server.network.packet.server.play.ParticlePacket;
import net.minestom.server.particle.Particle;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.potion.Potion;
import net.minestom.server.potion.PotionEffect;
import net.swofty.type.skyblockgeneric.entity.mob.impl.ProfiledBestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.impl.StandardMobDefinitions;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class MobWeaverSpider extends ProfiledBestiaryMob {
    private long nextWebAt;

    public MobWeaverSpider() {
        super(StandardMobDefinitions.WEAVER_SPIDER);
    }

    @Override
    public void tick(long time) {
        super.tick(time);
        if (System.currentTimeMillis() < nextWebAt) return;
        Entity target = getTarget();
        if (!(target instanceof SkyBlockPlayer player) || target.getInstance() != getInstance()
                || target.getPosition().distance(getPosition()) > 12) return;
        player.damage(new EntityDamage(this, (float) definition.damage()));
        player.addEffect(new Potion(PotionEffect.SLOWNESS, (byte) 1, 40));
        Pos position = getPosition().add(0, 1, 0);
        getInstance().getPlayers().forEach(viewer -> viewer.sendPacket(new ParticlePacket(
                Particle.ITEM_COBWEB, true, true, position.x(), position.y(), position.z(),
                0.3f, 0.3f, 0.3f, 0.05f, 12)));
        nextWebAt = System.currentTimeMillis() + 2_000;
    }
}
