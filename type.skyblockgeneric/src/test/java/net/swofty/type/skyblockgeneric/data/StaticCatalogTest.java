package net.swofty.type.skyblockgeneric.data;

import net.swofty.commons.ServerType;
import net.swofty.type.skyblockgeneric.data.crystals.CrystalCatalog;
import net.swofty.type.skyblockgeneric.data.regions.RegionCatalog;
import net.swofty.type.skyblockgeneric.region.RegionType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StaticCatalogTest {
    @Test
    void loadsRegionsFromYaml() {
        assertEquals(82, RegionCatalog.getAllRegions().size());
        assertEquals(RegionType.VILLAGE, RegionCatalog.getAllRegions().getFirst().getType());
        assertEquals(ServerType.SKYBLOCK_HUB, RegionCatalog.getAllRegions().getFirst().getServerType());
    }

    @Test
    void loadsCrystalsFromYaml() {
        assertEquals(12, CrystalCatalog.getAllCrystals().size());
        assertTrue(CrystalCatalog.getAllCrystals().stream()
            .anyMatch(crystal -> crystal.itemType.name().equals("FLOWER_CRYSTAL")));
    }
}
