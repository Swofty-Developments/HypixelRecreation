package net.swofty.type.skyblockgeneric.entity.mob.mobs.spidersden;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.swofty.type.skyblockgeneric.entity.mob.impl.ProfiledBestiaryMob;
import net.swofty.type.skyblockgeneric.entity.mob.impl.StandardMobDefinitions;

public class MobSplitterSpider extends ProfiledBestiaryMob {
    public MobSplitterSpider() {
        super(StandardMobDefinitions.SPLITTER_SPIDER);
    }

    @Override
    public void kill() {
        Instance instance = getInstance();
        Pos position = getPosition();
        super.kill();
        if (instance == null) return;
        new MobSilverfish().setInstance(instance, position.add(0.35, 0, 0));
        new MobSilverfish().setInstance(instance, position.add(-0.35, 0, 0));
    }
}
