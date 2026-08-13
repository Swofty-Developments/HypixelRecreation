package net.swofty.type.skyblockgeneric.item;

import net.swofty.commons.YamlFileUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertNotNull;

final class ExperimentationItemConfigTest {
    @Test
    @SuppressWarnings("unchecked")
    void experimentationItemsParseIntoRegisteredItems() throws IOException {
        Map<String, Object> root = YamlFileUtils.loadYaml(Path.of("..", "configuration", "skyblock", "items",
                "experimentation.yml").toFile());
        for (Map<String, Object> item : (List<Map<String, Object>>) root.get("items")) {
            assertNotNull(ItemConfigParser.parseItem(item), String.valueOf(item.get("id")));
        }
    }
}
