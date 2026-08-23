package net.swofty.type.skyblockgeneric.data;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import net.minestom.server.instance.SharedInstance;
import net.swofty.commons.skyblock.SkyBlockPlayerProfiles;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.data.domain.PlayerDataDomain;
import net.swofty.type.generic.data.domain.PlayerDataService;
import net.swofty.type.generic.data.mongodb.ProfilesDatabase;
import net.swofty.type.generic.data.mongodb.UserDatabase;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.island.IslandHostLookup;
import net.swofty.type.skyblockgeneric.user.island.SkyBlockIsland;
import org.bson.Document;
import org.jetbrains.annotations.Nullable;
import org.tinylog.Logger;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ProfileSwitcher {

    public enum Result {
        SWITCHED,
        TRANSFERRED,
        REFUSED,
        FAILED
    }

    public enum Refusal {
        NO_PROFILES("<c>Your profile list is not loaded yet, try again in a moment."),
        UNKNOWN_PROFILE("<c>That profile does not belong to you."),
        ALREADY_SELECTED("<c>You are already playing on that profile.");

        private final String message;

        Refusal(String message) {
            this.message = message;
        }

        public String message() {
            return message;
        }
    }

    public static CompletableFuture<Result> switchTo(SkyBlockPlayer player, UUID targetProfileId) {
        CompletableFuture<Result> future = new CompletableFuture<>();
        Thread.startVirtualThread(() -> {
            try {
                future.complete(run(player, targetProfileId));
            } catch (Throwable throwable) {
                Logger.error(throwable, "Failed to switch {} onto profile {}", player.getUuid(), targetProfileId);
                player.kick("<c>Something went wrong while switching profiles, please reconnect.");
                future.complete(Result.FAILED);
            }
        });
        return future;
    }

    public static @Nullable Refusal refuse(@Nullable SkyBlockPlayerProfiles profiles,
                                           @Nullable UUID activeProfileId,
                                           @Nullable UUID targetProfileId) {
        if (targetProfileId == null) return Refusal.UNKNOWN_PROFILE;
        if (profiles == null || profiles.getProfiles().isEmpty()) return Refusal.NO_PROFILES;
        if (!profiles.getProfiles().contains(targetProfileId)) return Refusal.UNKNOWN_PROFILE;
        if (targetProfileId.equals(activeProfileId)) return Refusal.ALREADY_SELECTED;
        if (activeProfileId == null && targetProfileId.equals(profiles.getCurrentlySelected())) {
            return Refusal.ALREADY_SELECTED;
        }
        return null;
    }

    public static UUID islandOf(UUID profileId) {
        Document document = ProfilesDatabase.fetchDocument(profileId);
        if (document == null) return profileId;

        String stored = document.getString(SkyBlockDataHandler.Data.ISLAND_UUID.getKey());
        if (stored == null) return profileId;

        String trimmed = stored.replace("\"", "").trim();
        if (trimmed.isEmpty() || trimmed.equals("null")) return profileId;

        try {
            return UUID.fromString(trimmed);
        } catch (IllegalArgumentException exception) {
            return profileId;
        }
    }

    private static Result run(SkyBlockPlayer player, UUID targetProfileId) {
        SkyBlockPlayerProfiles profiles = player.getProfiles();
        SkyBlockDataHandler handler = SkyBlockDataHandler.getUser(player);
        UUID activeProfileId = handler == null ? null : handler.getCurrentProfileId();

        Refusal refusal = refuse(profiles, activeProfileId, targetProfileId);
        if (refusal != null) {
            player.sendMessage(refusal.message());
            return Result.REFUSED;
        }

        if (HypixelConst.isIslandServer()) {
            UUID remoteHost = IslandHostLookup.findRemoteHost(islandOf(targetProfileId));
            if (remoteHost != null) return transfer(player, profiles, targetProfileId, remoteHost);
        }

        return switchInPlace(player, profiles, targetProfileId);
    }

    private static Result transfer(SkyBlockPlayer player, SkyBlockPlayerProfiles profiles,
                                   UUID targetProfileId, UUID host) {
        if (!player.isOnline()) return Result.FAILED;

        UUID previousProfileId = profiles.getCurrentlySelected();
        select(player, profiles, targetProfileId);

        HypixelConst.getTypeLoader().getTablistManager().nullifyCache(player);
        try {
            player.asProxyPlayer().transferToWithIndication(host).join();
            return Result.TRANSFERRED;
        } catch (Exception exception) {
            Logger.error(exception, "Failed to transfer {} to island host {}", player.getUuid(), host);
            select(player, profiles, previousProfileId);
            return Result.FAILED;
        }
    }

    private static Result switchInPlace(SkyBlockPlayer player, SkyBlockPlayerProfiles profiles, UUID targetProfileId) {
        PlayerDataDomain<SkyBlockDataHandler> domain = PlayerDataService.domain(SkyBlockDomain.KEY);
        boolean islandServer = HypixelConst.isIslandServer();
        AtomicReference<Result> result = new AtomicReference<>(Result.FAILED);

        PlayerDataService.runLifecycleTransition(player.getUuid(), () -> {
            SkyBlockDataHandler loaded = SkyBlockDataHandler.getUser(player);
            if (!player.isOnline() || loaded == null || targetProfileId.equals(loaded.getCurrentProfileId())) return;

            SkyBlockIsland previousIsland = player.getSkyBlockIsland();

            domain.save(player);

            player.closeInventory();
            player.getPetData().updatePetEntityImpl(null);
            player.getInventory().clear();

            if (islandServer) {
                moveTo(player, HypixelConst.getEmptyInstance());
                if (previousIsland != null) previousIsland.runVacantCheck();
            }

            domain.unload(player.getUuid());

            select(player, profiles, targetProfileId);

            domain.load(player.getUuid());
            domain.attach(player);

            if (islandServer) {
                SharedInstance instance = player.getSkyBlockIsland().getSharedInstance().join();
                moveTo(player, instance);
            }

            domain.applyToPlayer(player);
            HypixelConst.getTypeLoader().getTablistManager().nullifyCache(player);
            PlayerDataService.clearHandoff(player.getUuid());
            result.set(Result.SWITCHED);
        });

        return result.get();
    }

    private static void moveTo(SkyBlockPlayer player, SharedInstance instance) {
        if (instance == null || player.getInstance() == instance) return;
        player.setInstance(instance, player.getRespawnPoint()).join();
        player.teleport(player.getRespawnPoint());
    }

    private static void select(SkyBlockPlayer player, SkyBlockPlayerProfiles profiles, UUID profileId) {
        profiles.setCurrentlySelected(profileId);
        new UserDatabase(player.getUuid()).saveProfiles(profiles);
    }
}
