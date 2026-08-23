package net.swofty.commons.skyblock;

import net.swofty.LinkedField;
import net.swofty.PlayerField;
import net.swofty.codec.Codecs;

import java.util.UUID;

public final class SkyBlockProfileFields {
    public static final String NAMESPACE = "skyblock";
    public static final String ISLAND_UUID_KEY = "island_uuid";

    public static final PlayerField<String> DOCUMENT =
            PlayerField.create(NAMESPACE, "_doc", Codecs.STRING, null);

    public static final LinkedField<UUID, String> ISLAND_UUID =
            LinkedField.create("coop", ISLAND_UUID_KEY, Codecs.STRING, null, CoopLinks.COOP);

    private SkyBlockProfileFields() {}
}
