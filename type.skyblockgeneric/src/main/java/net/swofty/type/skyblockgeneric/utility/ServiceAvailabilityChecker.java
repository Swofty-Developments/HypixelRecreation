package net.swofty.type.skyblockgeneric.utility;

import net.swofty.commons.ServiceType;
import net.swofty.proxyapi.ProxyService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.tinylog.Logger;

import java.util.concurrent.CompletableFuture;
import java.util.function.Supplier;

/**
 * Utility class to handle service availability checks in a non-blocking manner.
 * Provides consistent error handling and user feedback for service connectivity.
 */
public class ServiceAvailabilityChecker {

    /**
     * Checks if a service is online and executes appropriate callbacks.
     * Uses async pattern to avoid blocking threads.
     *
     * @param serviceType The service type to check
     * @param player The player to notify if service is offline
     * @param onOnline Callback to execute if service is online
     * @param onOffline Optional callback to execute if service is offline (can be null)
     * @return CompletableFuture that completes when check is done
     */
    public static CompletableFuture<Void> checkAndExecute(
            ServiceType serviceType,
            SkyBlockPlayer player,
            Runnable onOnline,
            Runnable onOffline
    ) {
        ProxyService service = new ProxyService(serviceType);

        return service.isOnline().thenAccept(isOnline -> {
            if (isOnline) {
                onOnline.run();
            } else {
                Logger.warn("Service {} is offline for player {}", serviceType, player.getUuid());
                notifyPlayerServiceOffline(player, serviceType);
                if (onOffline != null) {
                    onOffline.run();
                }
            }
        }).exceptionally(ex -> {
            Logger.error(ex, "Error checking service {} availability for player {}",
                    serviceType, player.getUuid());
            player.sendMessage("<c>An error occurred while connecting to the service.");
            if (onOffline != null) {
                onOffline.run();
            }
            return null;
        });
    }

    /**
     * Checks if a service is online and executes a callback, with no offline handler.
     *
     * @param serviceType The service type to check
     * @param player The player to notify if service is offline
     * @param onOnline Callback to execute if service is online
     * @return CompletableFuture that completes when check is done
     */
    public static CompletableFuture<Void> checkAndExecute(
            ServiceType serviceType,
            SkyBlockPlayer player,
            Runnable onOnline
    ) {
        return checkAndExecute(serviceType, player, onOnline, null);
    }

    /**
     * Checks if a service is online synchronously (blocking).
     * WARNING: This method blocks the calling thread. Use async version when possible.
     *
     * @param serviceType The service type to check
     * @return true if service is online, false otherwise
     */
    public static boolean isOnlineBlocking(ServiceType serviceType) {
        try {
            return new ProxyService(serviceType).isOnline().join();
        } catch (Exception e) {
            Logger.error(e, "Error checking service {} availability (blocking)", serviceType);
            return false;
        }
    }

    /**
     * Checks if a service is online synchronously and notifies player if offline.
     * WARNING: This method blocks the calling thread.
     *
     * @param serviceType The service type to check
     * @param player The player to notify if service is offline
     * @return true if service is online, false otherwise
     */
    public static boolean isOnlineBlockingWithNotification(ServiceType serviceType, SkyBlockPlayer player) {
        boolean online = isOnlineBlocking(serviceType);
        if (!online) {
            notifyPlayerServiceOffline(player, serviceType);
        }
        return online;
    }

    /**
     * Gets a CompletableFuture for service online status.
     *
     * @param serviceType The service type to check
     * @return CompletableFuture<Boolean> that completes with online status
     */
    public static CompletableFuture<Boolean> isOnlineAsync(ServiceType serviceType) {
        return new ProxyService(serviceType).isOnline()
                .exceptionally(ex -> {
                    Logger.error(ex, "Error checking service {} availability", serviceType);
                    return false;
                });
    }

    /**
     * Notifies a player that a service is currently offline.
     *
     * @param player The player to notify
     * @param serviceType The service that is offline
     */
    private static void notifyPlayerServiceOffline(SkyBlockPlayer player, ServiceType serviceType) {
        String serviceName = getServiceDisplayName(serviceType);
        player.sendMessage("<c>{} is currently offline. Please try again later.", serviceName);
    }

    /**
     * Gets a user-friendly display name for a service type.
     *
     * @param serviceType The service type
     * @return Display name for the service
     */
    private static String getServiceDisplayName(ServiceType serviceType) {
        return switch (serviceType) {
            case AUCTION_HOUSE -> "Auction House";
            case BAZAAR -> "Bazaar";
            default -> serviceType.name().replace("_", " ");
        };
    }

    /**
     * Validates that a service is online before executing an action.
     * If offline, shows error message to player and returns early.
     *
     * @param serviceType The service to check
     * @param player The player performing the action
     * @param action The action to execute if service is online (returns CompletableFuture)
     * @param <T> The type of the CompletableFuture result
     * @return CompletableFuture with the action result, or completed future if service offline
     */
    public static <T> CompletableFuture<T> requireOnline(
            ServiceType serviceType,
            SkyBlockPlayer player,
            Supplier<CompletableFuture<T>> action
    ) {
        return isOnlineAsync(serviceType).thenCompose(online -> {
            if (online) {
                return action.get();
            } else {
                notifyPlayerServiceOffline(player, serviceType);
                return CompletableFuture.completedFuture(null);
            }
        });
    }
}
