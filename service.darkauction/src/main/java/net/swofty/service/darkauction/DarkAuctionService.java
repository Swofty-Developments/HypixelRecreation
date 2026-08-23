package net.swofty.service.darkauction;

import net.swofty.commons.ServiceType;
import net.swofty.service.generic.SkyBlockService;
import net.swofty.commons.redis.RedisMessageHandler;

import java.util.List;

public class DarkAuctionService implements SkyBlockService {
    private static DarkAuctionState currentAuction = null;

    public static void main(String[] args) {
        DarkAuctionScheduler.start();

        SkyBlockService.init(new DarkAuctionService());
    }

    @Override
    public ServiceType getType() {
        return ServiceType.DARK_AUCTION;
    }

    @Override
    public List<RedisMessageHandler> getEndpoints() {
        return loopThroughPackage("net.swofty.service.darkauction.endpoints", RedisMessageHandler.class).toList();
    }

    public static DarkAuctionState getCurrentAuction() {
        return currentAuction;
    }

    public static void setCurrentAuction(DarkAuctionState auction) {
        currentAuction = auction;
    }

    public static boolean hasActiveAuction() {
        return currentAuction != null;
    }
}
