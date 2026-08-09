package net.swofty.commons.config;

import de.exlll.configlib.Comment;
import de.exlll.configlib.Configuration;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Map;

@Getter
@Configuration
@SuppressWarnings({"unused", "FieldMayBeFinal"})
public class Settings {

    @Comment("The host name or IP address to bind the server to")
    private String hostName = "0.0.0.0";

    @Comment("The MongoDB connection URI")
    private String mongodb = "mongodb://localhost";

    @Comment("The Redis connection URI")
    private String redisUri = "redis://localhost:6379";

    @Comment("The secret key used to authenticate with Velocity proxy")
    private String velocitySecret = "ixmSUgWOgvs7";

    @Comment("Whether to require player authentication on initial connection")
    private boolean requireAuth = false;

    @Comment("Integrations with services")
    private IntegrationSettings integrations = new IntegrationSettings();

    @Comment("Settings related to configuration of Limbo server connections")
    private LimboSettings limbo = new LimboSettings();

    @Comment("Resource pack settings keyed by pack name (e.g. testingpack, skyblockpack)")
    private Map<String, ResourcePackSettings> resourcePacks = Map.of(
        "testingpack", new ResourcePackSettings(),
        "skyblockpack", new ResourcePackSettings()
    );

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class LimboSettings {
        private String hostName = "127.0.0.1";
        private int port = 65535;
    }

    @Getter
    @Configuration
    @NoArgsConstructor
    public static class ResourcePackSettings {
        @Comment("Base URL of the pack server (e.g. http://0.0.0.0:7270)")
        private String serverUrl = "http://127.0.0.1:7270";
    }

    @Getter
    @Configuration
    @NoArgsConstructor(access = AccessLevel.PRIVATE)
    public static class IntegrationSettings {
        @Comment("Whether to enable Spark for performance monitoring")
        private boolean spark = false;

        @Comment("Whether to enable anti-cheat measures")
        private boolean anticheat = false;

        @Comment("Whether to enable ViaVersion for supporting multiple Minecraft versions. This may cause issues of any kind")
        private boolean viaVersion = false;

        @Comment("The DSN to use for Sentry error tracking. If empty, Sentry will be disabled")
        private String sentryDsn = "";
    }

}
