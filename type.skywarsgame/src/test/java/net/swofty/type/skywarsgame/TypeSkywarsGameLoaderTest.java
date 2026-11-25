package net.swofty.type.skywarsgame;

import net.swofty.commons.skywars.SkywarsGameType;
import net.swofty.commons.skywars.map.SkywarsMapsConfig;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TypeSkywarsGameLoaderTest {
    @Test
    void returnsDistinctConfiguredGameTypes() {
        SkywarsMapsConfig.MapEntry entry = new SkywarsMapsConfig.MapEntry();
        SkywarsMapsConfig.MapEntry.MapConfiguration configuration =
                new SkywarsMapsConfig.MapEntry.MapConfiguration();
        configuration.setTypes(List.of(SkywarsGameType.SOLO_NORMAL, SkywarsGameType.SOLO_NORMAL,
                SkywarsGameType.DOUBLES_NORMAL));
        entry.setConfiguration(configuration);

        assertEquals(List.of(SkywarsGameType.SOLO_NORMAL, SkywarsGameType.DOUBLES_NORMAL),
                TypeSkywarsGameLoader.getSupportedTypes(entry));
        assertTrue(TypeSkywarsGameLoader.supportsGameType(entry, SkywarsGameType.DOUBLES_NORMAL));
        assertFalse(TypeSkywarsGameLoader.supportsGameType(entry, SkywarsGameType.SOLO_INSANE));
    }

    @Test
    void rejectsMapsWithoutConfiguredGameTypes() {
        SkywarsMapsConfig.MapEntry entry = new SkywarsMapsConfig.MapEntry();

        assertTrue(TypeSkywarsGameLoader.getSupportedTypes(entry).isEmpty());
    }
}
