package net.swofty.commons.skyblock;

import net.swofty.LinkType;
import net.swofty.PlayerField;
import net.swofty.codec.Codecs;

import java.util.UUID;

public final class CoopLinks {
    public static final PlayerField<UUID> COOP_KEY = PlayerField.create("skyblock", "_coop_key", Codecs.UUID, null);
    public static final LinkType<UUID> COOP = LinkType.create("coop", Codecs.UUID, COOP_KEY);

    private CoopLinks() {}
}
