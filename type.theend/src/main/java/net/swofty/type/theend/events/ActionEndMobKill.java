package net.swofty.type.theend.events;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.EventPhase;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.end.MobSpecialZealot;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.end.MobZealot;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.end.MobZealotBruiser;
import net.swofty.type.skyblockgeneric.event.custom.PlayerKilledSkyBlockMobEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class ActionEndMobKill implements HypixelEventClass {
    private static final double SPECIAL_ZEALOT_CHANCE = 1D / 420D;
    private static final double BRUISER_SPECIAL_ZEALOT_CHANCE = 1D / 210D;
    private static final Map<UUID, UUID> SPECIAL_CONTRIBUTORS = new ConcurrentHashMap<>();

    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true, phase = EventPhase.GAMEPLAY)
    public void run(PlayerKilledSkyBlockMobEvent event) {
        SkyBlockPlayer player = event.getPlayer();
        SkyBlockMob killedMob = event.getKilledMob();

        if (killedMob instanceof MobSpecialZealot specialZealot) {
            UUID contributorUuid = SPECIAL_CONTRIBUTORS.remove(specialZealot.getUuid());
            SkyBlockPlayer recipient = contributorUuid == null
                    ? player
                    : SkyBlockGenericLoader.getFromUUID(contributorUuid);
            if (recipient == null) recipient = player;
            recipient.addAndUpdateItem(ItemType.SUMMONING_EYE);
            return;
        }

        double chance = killedMob instanceof MobZealotBruiser
                ? BRUISER_SPECIAL_ZEALOT_CHANCE
                : killedMob instanceof MobZealot ? SPECIAL_ZEALOT_CHANCE : 0;
        if (chance == 0 || ThreadLocalRandom.current().nextDouble() >= chance) return;
        if (killedMob.getInstance() == null) return;

        MobSpecialZealot specialZealot = new MobSpecialZealot();
        specialZealot.setInstance(killedMob.getInstance(), killedMob.getPosition().add(
                ThreadLocalRandom.current().nextDouble(-2, 2), 0,
                ThreadLocalRandom.current().nextDouble(-2, 2)));
        SPECIAL_CONTRIBUTORS.put(specialZealot.getUuid(), player.getUuid());
        player.sendMessage("§dA special Zealot has spawned nearby!");
    }
}
