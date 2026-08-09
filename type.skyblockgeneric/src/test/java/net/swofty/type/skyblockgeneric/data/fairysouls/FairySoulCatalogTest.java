package net.swofty.type.skyblockgeneric.data.fairysouls;

import net.swofty.commons.ServerType;
import net.swofty.type.skyblockgeneric.user.fairysouls.FairySoul;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class FairySoulCatalogTest {
    @Test
    void loadsTheCompleteCatalog() {
        FairySoul.cacheFairySouls();
        assertEquals(289, FairySoulCatalog.getAllSouls().size());
        assertEquals(271, FairySoulCatalog.getAllSouls().stream().filter(soul -> soul.getLocation() != null).count());
        assertEquals(ServerType.SKYBLOCK_LOTUS_ATOLL, FairySoul.getFairySoul(268).getServerType());
        assertEquals(ServerType.SKYBLOCK_SAFARI, FairySoul.getFairySoul(286).getServerType());
        assertNotNull(FairySoul.getFairySoul(225));
    }
}
