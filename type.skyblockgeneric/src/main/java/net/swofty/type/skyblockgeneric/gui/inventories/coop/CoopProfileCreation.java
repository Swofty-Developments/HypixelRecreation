package net.swofty.type.skyblockgeneric.gui.inventories.coop;

import net.swofty.commons.data.SwoftyData;
import net.swofty.commons.skyblock.SkyBlockPlayerProfiles;
import net.swofty.type.generic.data.datapoints.DatapointBoolean;
import net.swofty.type.generic.data.datapoints.DatapointString;
import net.swofty.type.generic.data.mongodb.ProfilesDatabase;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.commons.skyblock.CoopLinks;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointUUID;
import net.swofty.type.skyblockgeneric.data.monogdb.CoopDatabase;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.UUID;

final class CoopProfileCreation {

    private CoopProfileCreation() {}

    static UUID create(SkyBlockPlayer player, CoopDatabase.Coop coop) {
        UUID profileId = UUID.randomUUID();
        SwoftyData.profile().link(profileId, CoopLinks.COOP, coop.coopUUID());

        SkyBlockDataHandler handler = SkyBlockDataHandler.initUserWithDefaultData(player.getUuid(), profileId);
        SkyBlockDataHandler source = coop.memberProfiles().isEmpty()
                ? null
                : sourceHandler(coop.memberProfiles().getFirst());

        if (source == null) {
            handler.get(SkyBlockDataHandler.Data.ISLAND_UUID, DatapointUUID.class).setValue(UUID.randomUUID());
            handler.get(SkyBlockDataHandler.Data.PROFILE_NAME, DatapointString.class)
                    .setValue(SkyBlockPlayerProfiles.getRandomName());
        } else {
            handler.get(SkyBlockDataHandler.Data.ISLAND_UUID, DatapointUUID.class)
                    .setValue(source.get(SkyBlockDataHandler.Data.ISLAND_UUID, DatapointUUID.class).getValue());
            handler.get(SkyBlockDataHandler.Data.PROFILE_NAME, DatapointString.class)
                    .setValue(source.get(SkyBlockDataHandler.Data.PROFILE_NAME, DatapointString.class).getValue());
            handler.get(SkyBlockDataHandler.Data.PROFILE_MODE, DatapointString.class)
                    .setValue(source.get(SkyBlockDataHandler.Data.PROFILE_MODE, DatapointString.class).getValue());
        }

        handler.get(SkyBlockDataHandler.Data.IS_COOP, DatapointBoolean.class).setValue(true);

        new ProfilesDatabase(profileId.toString()).saveDocument(handler.toProfileDocument());
        return profileId;
    }

    private static SkyBlockDataHandler sourceHandler(UUID memberProfile) {
        ProfilesDatabase database = new ProfilesDatabase(memberProfile.toString());
        if (database.exists()) return SkyBlockDataHandler.createFromProfileOnly(database.getDocument());

        SkyBlockPlayer owner = SkyBlockGenericLoader.getPlayerFromProfileUUID(memberProfile);
        return owner == null ? null : SkyBlockDataHandler.getUser(owner.getUuid());
    }
}
