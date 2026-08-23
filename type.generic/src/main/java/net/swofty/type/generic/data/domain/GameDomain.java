package net.swofty.type.generic.data.domain;

import net.swofty.commons.ServerType;
import net.swofty.type.generic.data.DataHandler;
import net.swofty.type.generic.data.GameDataHandler;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.UUID;

public final class GameDomain implements PlayerDataDomain<DataHandler> {
    private final GameDataHandler handler;
    private final DomainKey<DataHandler> key;

    public GameDomain(GameDataHandler handler) {
        this.handler = handler;
        this.key = new DomainKey<>("game:" + handler.getHandlerId(), DataHandler.class);
    }

    @Override
    public DomainKey<DataHandler> key() {
        return key;
    }

    @Override
    public boolean appliesTo(ServerType type) {
        return true;
    }

    @Override
    public int order() {
        return 20;
    }

    @Override
    public void load(UUID uuid) {
        DataHandler gameHandler = handler.getHandler(uuid);
        if (gameHandler == null) {
            gameHandler = handler.initWithDefaults(uuid);
            gameHandler.loadBackedData();
            handler.cacheHandler(uuid, gameHandler);
        }
        PlayerDataService.store(key, uuid, gameHandler);
    }

    @Override
    public void applyToPlayer(HypixelPlayer player) {
        handler.runOnLoad(player.getUuid(), player);
    }

    @Override
    public void save(HypixelPlayer player) {
        DataHandler gameHandler = handler.getHandler(player.getUuid());
        if (gameHandler == null) return;
        gameHandler.runOnSave(player);
        gameHandler.saveBackedData();
        Leaderboards.sync(player.getUuid(), gameHandler);
    }

    @Override
    public void unload(UUID uuid) {
        handler.removeFromCache(uuid);
        PlayerDataService.evict(key, uuid);
    }
}
