package net.swofty.type.generic.data.domain;

import net.swofty.commons.ServerType;
import net.swofty.type.generic.data.DataHandler;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.UUID;

public interface PlayerDataDomain<H extends DataHandler> {
    DomainKey<H> key();

    boolean appliesTo(ServerType type);

    void load(UUID uuid);

    default void attach(HypixelPlayer player) {}

    void applyToPlayer(HypixelPlayer player);

    void save(HypixelPlayer player);

    default void cleanup(HypixelPlayer player) {}

    void unload(UUID uuid);

    default int order() {
        return 0;
    }
}
