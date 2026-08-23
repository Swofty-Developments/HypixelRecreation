package net.swofty.type.ravengardgeneric.data;

import net.swofty.commons.ServerType;
import net.swofty.commons.data.SwoftyData;
import net.swofty.type.generic.data.domain.DomainKey;
import net.swofty.type.generic.data.domain.PlayerDataDomain;
import net.swofty.type.generic.data.domain.PlayerDataService;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.ravengardgeneric.profile.RavengardProfiles;
import net.swofty.type.ravengardgeneric.user.RavengardPlayer;
import org.tinylog.Logger;

import java.util.UUID;

public final class RavengardDomain implements PlayerDataDomain<RavengardDataHandler> {
    public static final DomainKey<RavengardDataHandler> KEY = new DomainKey<>("ravengard", RavengardDataHandler.class);

    @Override
    public DomainKey<RavengardDataHandler> key() {
        return KEY;
    }

    @Override
    public boolean appliesTo(ServerType type) {
        return true;
    }

    @Override
    public int order() {
        return 30;
    }

    @Override
    public void load(UUID uuid) {
        if (PlayerDataService.isLoaded(KEY, uuid)) return;
        RavengardDataHandler handler = RavengardDataHandler.initUserWithDefaultData(uuid);
        handler.attachProfile(RavengardProfileIndex.read(uuid).selected());
        PlayerDataService.store(KEY, uuid, handler);
    }

    @Override
    public void applyToPlayer(HypixelPlayer player) {
        PlayerDataService.get(KEY, player.getUuid()).runOnLoad(player);
    }

    @Override
    public void save(HypixelPlayer player) {
        RavengardDataHandler handler = PlayerDataService.find(KEY, player.getUuid()).orElse(null);
        if (handler == null) return;
        if (player instanceof RavengardPlayer ravengardPlayer) {
            RavengardProfiles.endSessionAndSave(ravengardPlayer);
        }
        handler.runOnSave(player);
        handler.saveBackedData();
    }

    @Override
    public void unload(UUID uuid) {
        RavengardDataHandler handler = PlayerDataService.find(KEY, uuid).orElse(null);
        PlayerDataService.evict(KEY, uuid);
        if (handler == null || handler.getCurrentProfileId() == null) return;

        try {
            SwoftyData.profile().unload(handler.getCurrentProfileId());
        } catch (Exception exception) {
            Logger.error(exception, "Failed to release the Ravengard profile container {}",
                    handler.getCurrentProfileId());
        }
    }

    public static boolean isProfileHosted(UUID profileId) {
        if (profileId == null) return false;
        for (RavengardDataHandler handler : PlayerDataService.loaded(KEY)) {
            if (profileId.equals(handler.getCurrentProfileId())) return true;
        }
        return false;
    }
}
