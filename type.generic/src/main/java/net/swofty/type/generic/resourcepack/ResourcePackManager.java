package net.swofty.type.generic.resourcepack;

import lombok.Getter;
import net.kyori.adventure.resource.ResourcePackInfo;
import net.kyori.adventure.resource.ResourcePackRequest;
import net.swofty.commons.text.Text;
import net.minestom.server.entity.Player;
import org.tinylog.Logger;

import java.net.URI;
import java.util.UUID;

public class ResourcePackManager {
    @Getter
    private static ResourcePackManager instance;

    @Getter
    private final HypixelResourcePack activePack;

    public ResourcePackManager(HypixelResourcePack pack) {
        this.activePack = pack;
        instance = this;
    }

    public void initialize() {
        activePack.initialize();
    }

    public void sendPack(Player player) {
        sendPackBlocking(player, 0);
    }

    /**
     * Pushes the pack and, when {@code timeoutSeconds > 0}, waits for the client's
     * terminal pack status. The pack must finish applying during the configuration
     * phase, before any world data streams: a mid-game resource reload leaves
     * already-received chunk sections permanently unrendered on the client.
     */
    public void sendPackBlocking(Player player, int timeoutSeconds) {
        String packUrl = activePack.getPackUrl();
        String packHash = activePack.getPackHash();

        if (packUrl == null || packUrl.isEmpty() || packHash == null || packHash.isEmpty()) {
            Logger.warn("Resource pack URL or hash not configured, skipping pack send for " + player.getUsername());
            return;
        }

        // the cookie lives for the client connection and rides across backend
        // transfers, so the pack applies exactly once per session instead of
        // triggering a full client resource reload on every server switch
        try {
            byte[] applied = player.getPlayerConnection()
                    .fetchCookie("hypixel:applied_pack")
                    .get(3, java.util.concurrent.TimeUnit.SECONDS);
            if (applied != null && packHash.equals(new String(applied, java.nio.charset.StandardCharsets.UTF_8))) {
                Logger.info("Pack {} already applied for {}, skipping push",
                        packHash.substring(0, 8), player.getUsername());
                return;
            }
        } catch (Exception exception) {
            Logger.warn("Cookie fetch failed for {}, pushing pack anyway", player.getUsername());
        }

        ResourcePackInfo info = ResourcePackInfo.resourcePackInfo(
                UUID.nameUUIDFromBytes(packHash.getBytes()),
                URI.create(packUrl),
                packHash
        );

        java.util.concurrent.CompletableFuture<Void> resolved = new java.util.concurrent.CompletableFuture<>();
        ResourcePackRequest request = ResourcePackRequest.resourcePackRequest()
                .packs(info)
                .replace(true)
                .required(activePack.isRequired())
                .prompt(Text.of("<a>This resource pack is required to play on Hypixel.").asComponent())
                .callback((packId, status, audience) -> {
                    if (status.intermediate()) return;
                    if (status == net.kyori.adventure.resource.ResourcePackStatus.SUCCESSFULLY_LOADED) {
                        player.getPlayerConnection().storeCookie("hypixel:applied_pack",
                                packHash.getBytes(java.nio.charset.StandardCharsets.UTF_8));
                    } else {
                        Logger.warn("Resource pack {} for {} finished with status {}",
                                packHash.substring(0, 8), player.getUsername(), status);
                    }
                    resolved.complete(null);
                })
                .build();

        player.sendResourcePacks(request);

        if (timeoutSeconds > 0) {
            try {
                resolved.get(timeoutSeconds, java.util.concurrent.TimeUnit.SECONDS);
            } catch (Exception exception) {
                Logger.warn("Resource pack for {} did not resolve within {}s, continuing",
                        player.getUsername(), timeoutSeconds);
            }
        }
    }
}
