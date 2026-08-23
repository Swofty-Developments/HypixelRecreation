package net.swofty.type.skyblockgeneric.data.fairysouls;

import net.swofty.type.skyblockgeneric.user.fairysouls.FairySoul;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

class FairySoulCatalogTest {
    @Test
    void loadsTheCompleteCatalog() {
        FairySoul.cacheFairySouls();
        assertEquals(289, FairySoulCatalog.getAllSouls().size());
        assertEquals(261, FairySoulCatalog.getAllSouls().stream().filter(soul -> soul.getLocation() != null).count());
        assertNull(FairySoul.getFairySoul(268).getServerType());
        assertNull(FairySoul.getFairySoul(286).getServerType());
        assertNotNull(FairySoul.getFairySoul(225));
    }
}
