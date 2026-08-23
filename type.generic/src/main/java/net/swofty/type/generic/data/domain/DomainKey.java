package net.swofty.type.generic.data.domain;

import net.swofty.type.generic.data.DataHandler;

public final class DomainKey<H extends DataHandler> {
    private final String id;
    private final Class<H> type;

    public DomainKey(String id, Class<H> type) {
        this.id = id;
        this.type = type;
    }

    public String id() {
        return id;
    }

    public Class<H> type() {
        return type;
    }
}
