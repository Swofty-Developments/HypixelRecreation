package net.swofty.commons.config;

import de.exlll.configlib.NameFormatters;
import de.exlll.configlib.YamlConfigurationProperties;
import de.exlll.configlib.YamlConfigurations;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public final class YamlConfigLoader {
    private static final YamlConfigurationProperties PROPERTIES = YamlConfigurationProperties.newBuilder()
        .setNameFormatter(NameFormatters.IDENTITY)
        .charset(StandardCharsets.UTF_8)
        .build();

    private YamlConfigLoader() {
    }

    public static <T> T load(Path location, Class<?> resourceOwner, String resource, Class<T> configurationType) {
        try (InputStream source = openSource(location, resourceOwner, resource)) {
            return YamlConfigurations.read(source, configurationType, PROPERTIES);
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to read YAML configuration " + location, exception);
        }
    }

    public static <T> void save(Path location, Class<T> configurationType, T configuration) {
        Path target = location.toAbsolutePath();
        Path temporary = null;
        try {
            Path directory = target.getParent();
            Files.createDirectories(directory);
            temporary = Files.createTempFile(directory, target.getFileName().toString(), ".tmp");
            YamlConfigurations.save(temporary, configurationType, configuration, PROPERTIES);
            try {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException exception) {
                Files.move(temporary, target, StandardCopyOption.REPLACE_EXISTING);
            }
            temporary = null;
        } catch (IOException exception) {
            throw new IllegalStateException("Unable to write YAML configuration " + location, exception);
        } finally {
            if (temporary != null) {
                try {
                    Files.deleteIfExists(temporary);
                } catch (IOException ignored) {
                }
            }
        }
    }

    private static InputStream openSource(Path location, Class<?> resourceOwner, String resource) throws IOException {
        if (Files.isRegularFile(location)) {
            return Files.newInputStream(location);
        }

        InputStream source = resourceOwner.getResourceAsStream(resource);
        if (source == null) {
            throw new FileNotFoundException(location.toString());
        }
        return source;
    }
}
