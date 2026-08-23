package net.swofty.service.friend;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.Filters;
import net.swofty.commons.friend.*;
import net.swofty.commons.friend.events.*;
import net.swofty.commons.friend.events.response.*;
import net.swofty.commons.presence.PresenceInfo;
import net.swofty.commons.protocol.objects.friend.FriendEventPushProtocol;
import net.swofty.commons.protocol.objects.messaging.SendMessagePushProtocol;
import net.swofty.commons.redis.RedisClient;
import net.swofty.commons.text.Text;
import org.bson.Document;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class FriendCache {
    private static final Map<UUID, FriendData> cachedFriendData = new ConcurrentHashMap<>();
    private static final int FRIENDS_PER_PAGE = 10;
    private static final long REQUEST_EXPIRATION_MS = 5 * 60 * 1000; // 5 minutes
    private static ScheduledExecutorService expirationScheduler;

    public static void startExpirationChecker() {
        if (expirationScheduler != null) {
            return;
        }
        expirationScheduler = Executors.newSingleThreadScheduledExecutor();
        expirationScheduler.scheduleAtFixedRate(FriendCache::checkExpiredRequests, 30, 30, TimeUnit.SECONDS);
    }

    public static void stopExpirationChecker() {
        if (expirationScheduler != null) {
            expirationScheduler.shutdown();
            expirationScheduler = null;
        }
    }

    private static void checkExpiredRequests() {
        try {
            FriendDatabase db = new FriendDatabase(null);
            List<PendingFriendRequest> allRequests = db.getAllPendingRequests();
            long now = System.currentTimeMillis();

            for (PendingFriendRequest request : allRequests) {
                if (now - request.getTimestamp() > REQUEST_EXPIRATION_MS) {
                    db.removePendingRequest(request.getFrom(), request.getTo());
                    sendEvent(new FriendRequestExpiredResponseEvent(
                            request.getFrom(),
                            request.getTo(),
                            request.getFromName(),
                            request.getToName()
                    ));
                }
            }
        } catch (Exception e) {
            org.tinylog.Logger.error(e, "Failed to check expired friend requests");
        }
    }

    public static FriendData getFriendData(UUID playerUuid) {
        FriendData cached = cachedFriendData.get(playerUuid);
        if (cached != null) {
            return cached;
        }

        FriendDatabase db = new FriendDatabase(playerUuid.toString());
        FriendData data = db.getFriendData(playerUuid);
        if (data == null) {
            data = FriendData.createEmpty(playerUuid);
            db.saveFriendData(data);
        }
        cachedFriendData.put(playerUuid, data);
        return data;
    }

    public static boolean areFriends(UUID player1, UUID player2) {
        FriendData data = getFriendData(player1);
        return data.isFriendWith(player2);
    }

    public static List<PendingFriendRequest> getPendingRequestsFor(UUID playerUuid) {
        FriendDatabase db = new FriendDatabase(null);
        return db.getPendingRequestsFor(playerUuid);
    }

    public static void handleAddRequest(FriendAddRequestEvent event, String senderName, String targetName) {
        UUID sender = event.getSender();
        UUID target = event.getTarget();

        FriendData senderData = getFriendData(sender);
        FriendData targetData = getFriendData(target);

        if (sender.equals(target)) {
            sendErrorToPlayer(sender, "You cannot add yourself as a friend!");
            return;
        }

        if (senderData.isFriendWith(target)) {
            sendErrorToPlayer(sender, "You are already friends with " + targetName + "!");
            return;
        }

        if (!targetData.getSettings().isAcceptingRequests()) {
            sendErrorToPlayer(sender, targetName + " is not accepting friend requests!");
            return;
        }

        FriendDatabase db = new FriendDatabase(null);
        if (db.hasPendingRequest(sender, target)) {
            sendErrorToPlayer(sender, "You have already sent a friend request to " + targetName + "!");
            return;
        }

        if (db.hasPendingRequest(target, sender)) {
            handleAcceptRequest(new FriendAcceptRequestEvent(sender, target), senderName, targetName);
            return;
        }

        PendingFriendRequest request = PendingFriendRequest.create(sender, target, senderName, targetName);
        db.addPendingRequest(request);

        sendEvent(new FriendRequestSentResponseEvent(sender, target, targetName));
        sendEvent(new FriendRequestReceivedResponseEvent(sender, target, senderName));
    }

    public static void handleAcceptRequest(FriendAcceptRequestEvent event, String accepterName, String requesterName) {
        UUID accepter = event.getAccepter();
        UUID requester = event.getRequester();

        FriendDatabase db = new FriendDatabase(null);
        PendingFriendRequest request = db.getPendingRequest(requester, accepter);

        if (request == null) {
            sendErrorToPlayer(accepter, "You don't have a friend request from " + requesterName + "!");
            return;
        }

        FriendData accepterData = getFriendData(accepter);
        FriendData requesterData = getFriendData(requester);

        accepterData.addFriend(Friend.create(requester));
        requesterData.addFriend(Friend.create(accepter));

        persistFriendData(accepter);
        persistFriendData(requester);

        db.removePendingRequest(requester, accepter);

        sendEvent(new FriendAddedResponseEvent(accepter, requester, accepterName, requesterName));
    }

    public static void handleDenyRequest(FriendDenyRequestEvent event, String denierName) {
        UUID denier = event.getDenier();
        UUID requester = event.getRequester();

        FriendDatabase db = new FriendDatabase(null);
        PendingFriendRequest request = db.getPendingRequest(requester, denier);

        if (request == null) {
            sendErrorToPlayer(denier, "You don't have a friend request from that player!");
            return;
        }

        db.removePendingRequest(requester, denier);

        sendEvent(new FriendDeniedResponseEvent(denier, requester, denierName));
    }

    public static void handleRemoveRequest(FriendRemoveRequestEvent event, String removerName, String targetName) {
        UUID remover = event.getRemover();
        UUID target = event.getTarget();

        FriendData removerData = getFriendData(remover);
        FriendData targetData = getFriendData(target);

        if (!removerData.isFriendWith(target)) {
            sendErrorToPlayer(remover, "You are not friends with " + targetName + "!");
            return;
        }

        removerData.removeFriend(target);
        targetData.removeFriend(remover);

        persistFriendData(remover);
        persistFriendData(target);

        sendEvent(new FriendRemovedResponseEvent(remover, target, removerName));
    }

    public static void handleRemoveAllRequest(FriendRemoveAllRequestEvent event) {
        UUID player = event.getPlayer();
        FriendData playerData = getFriendData(player);

        List<UUID> friendsToRemove = playerData.getFriends().stream()
                .filter(f -> !f.isBestFriend())
                .map(Friend::getUuid)
                .toList();

        int removedCount = friendsToRemove.size();

        for (UUID friendUuid : friendsToRemove) {
            FriendData friendData = getFriendData(friendUuid);
            friendData.removeFriend(player);
            persistFriendData(friendUuid);
        }

        playerData.removeAllNonBestFriends();
        persistFriendData(player);

        sendEvent(new FriendRemoveAllResponseEvent(player, removedCount));
    }

    public static void handleToggleBestRequest(FriendToggleBestRequestEvent event, String targetName) {
        UUID player = event.getPlayer();
        UUID target = event.getTarget();

        FriendData playerData = getFriendData(player);
        Friend friend = playerData.getFriend(target);

        if (friend == null) {
            sendErrorToPlayer(player, "You are not friends with " + targetName + "!");
            return;
        }

        boolean newBestStatus = !friend.isBestFriend();
        friend.setBestFriend(newBestStatus);
        persistFriendData(player);

        sendEvent(new FriendBestToggledResponseEvent(player, target, targetName, newBestStatus));
    }

    public static void handleSetNicknameRequest(FriendSetNicknameRequestEvent event, String targetName) {
        UUID player = event.getPlayer();
        UUID target = event.getTarget();
        String nickname = event.getNickname();

        FriendData playerData = getFriendData(player);
        Friend friend = playerData.getFriend(target);

        if (friend == null) {
            sendErrorToPlayer(player, "You are not friends with " + targetName + "!");
            return;
        }

        friend.setNickname(nickname);
        persistFriendData(player);

        sendEvent(new FriendNicknameSetResponseEvent(player, target, targetName, nickname));
    }

    public static void handleToggleSettingRequest(FriendToggleSettingRequestEvent event) {
        UUID player = event.getPlayer();
        FriendSettingType settingType = event.getSettingType();

        FriendData playerData = getFriendData(player);
        FriendSettings settings = playerData.getSettings();

        boolean newValue;
        switch (settingType) {
            case ACCEPTING_REQUESTS -> {
                newValue = !settings.isAcceptingRequests();
                settings.setAcceptingRequests(newValue);
            }
            case JOIN_LEAVE_NOTIFICATIONS -> {
                newValue = !settings.isJoinLeaveNotifications();
                settings.setJoinLeaveNotifications(newValue);
            }
            default -> {
                return;
            }
        }

        persistFriendData(player);
        sendEvent(new FriendSettingToggledResponseEvent(player, settingType, newValue));
    }

    public static void handleListRequest(FriendListRequestEvent event) {
        UUID player = event.getPlayer();
        int page = event.getPage();
        boolean bestOnly = event.isBestOnly();

        FriendData playerData = getFriendData(player);
        List<Friend> friends = bestOnly ? playerData.getBestFriends() : new ArrayList<>(playerData.getFriends());

        int totalFriends = friends.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalFriends / FRIENDS_PER_PAGE));
        page = Math.clamp(page, 1, totalPages);

        int startIndex = (page - 1) * FRIENDS_PER_PAGE;
        int endIndex = Math.min(startIndex + FRIENDS_PER_PAGE, totalFriends);

        List<Friend> pageFriends = friends.subList(startIndex, endIndex);
        Map<UUID, String> playerNames = resolvePlayerNames(pageFriends.stream()
                .map(Friend::getUuid)
                .toList());
        List<UUID> friendUuids = pageFriends.stream().map(Friend::getUuid).toList();
        Map<UUID, Boolean> onlineStatus = PresenceStorage.getOnlineStatus(friendUuids);
        Map<UUID, net.swofty.commons.presence.PresenceInfo> presenceInfo = PresenceStorage.getMap(friendUuids);

        List<FriendListResponseEvent.FriendListEntry> entries = new ArrayList<>();
        for (Friend friend : pageFriends) {
            String name = playerNames.getOrDefault(friend.getUuid(), "Unknown");
            boolean isOnline = onlineStatus.getOrDefault(friend.getUuid(), false);
            net.swofty.commons.presence.PresenceInfo pInfo = presenceInfo.get(friend.getUuid());
            long lastSeen = pInfo != null ? pInfo.getLastSeen() : 0L;
            String server = (pInfo != null && pInfo.isOnline() && pInfo.getServerType() != null)
                    ? formatServerDisplay(pInfo)
                    : null;
            long friendSince = friend.getAddedTimestamp();
            entries.add(new FriendListResponseEvent.FriendListEntry(
                    friend.getUuid(),
                    name,
                    friend.getNickname(),
                    friend.isBestFriend(),
                    isOnline,
                    lastSeen,
                    friendSince,
                    server
            ));
        }

        sendEvent(new FriendListResponseEvent(player, entries, page, totalPages, bestOnly));
    }

    public static void handleRequestsListRequest(FriendRequestsListEvent event) {
        UUID player = event.getPlayer();
        int page = event.getPage();

        FriendDatabase db = new FriendDatabase(null);
        List<PendingFriendRequest> requests = db.getPendingRequestsFor(player);

        int totalRequests = requests.size();
        int totalPages = Math.max(1, (int) Math.ceil((double) totalRequests / FRIENDS_PER_PAGE));
        page = Math.clamp(page, 1, totalPages);

        int startIndex = (page - 1) * FRIENDS_PER_PAGE;
        int endIndex = Math.min(startIndex + FRIENDS_PER_PAGE, totalRequests);

        List<PendingFriendRequest> pageRequests = requests.subList(startIndex, endIndex);
        Map<UUID, String> playerNames = resolvePlayerNames(pageRequests.stream()
                .map(PendingFriendRequest::getFrom)
                .toList());

        List<FriendRequestsListResponseEvent.FriendRequestEntry> entries = new ArrayList<>();
        for (PendingFriendRequest request : pageRequests) {
            String senderName = playerNames.getOrDefault(request.getFrom(), request.getFromName());
            entries.add(new FriendRequestsListResponseEvent.FriendRequestEntry(
                    request.getFrom(),
                    senderName,
                    request.getTimestamp()
            ));
        }

        sendEvent(new FriendRequestsListResponseEvent(player, entries, page, totalPages));
    }

    public static void handlePlayerJoin(UUID playerUuid, String playerName) {
        PresenceStorage.upsertPreservingServer(new net.swofty.commons.presence.PresenceInfo(
                playerUuid,
                true,
                null,
                null,
                System.currentTimeMillis()
        ));

        FriendData playerData = getFriendData(playerUuid);

        for (Friend friend : playerData.getFriends()) {
            PresenceInfo friendPresence = PresenceStorage.get(friend.getUuid());
            if (friendPresence == null || !friendPresence.isOnline()) continue;

            FriendData friendData = cachedFriendData.get(friend.getUuid());
            if (friendData == null) {
                friendData = getFriendData(friend.getUuid());
                cachedFriendData.put(friend.getUuid(), friendData);
            }

            if (friendData != null && friendData.getSettings().isJoinLeaveNotifications()) {
                sendEvent(new FriendJoinNotificationEvent(friend.getUuid(), playerUuid, playerName));
            }
        }
    }

    public static void handlePlayerLeave(UUID playerUuid, String playerName) {
        PresenceStorage.upsertPreservingServer(new PresenceInfo(
                playerUuid,
                false,
                null,
                null,
                System.currentTimeMillis()
        ));

        FriendData playerData = cachedFriendData.get(playerUuid);
        if (playerData == null) return;

        for (Friend friend : playerData.getFriends()) {
            net.swofty.commons.presence.PresenceInfo friendPresence = PresenceStorage.get(friend.getUuid());
            if (friendPresence == null || !friendPresence.isOnline()) continue;

            FriendData friendData = cachedFriendData.get(friend.getUuid());
            if (friendData == null) {
                friendData = getFriendData(friend.getUuid());
                cachedFriendData.put(friend.getUuid(), friendData);
            }

            if (friendData != null && friendData.getSettings().isJoinLeaveNotifications()) {
                sendEvent(new FriendLeaveNotificationEvent(friend.getUuid(), playerUuid, playerName));
            }
        }

        cachedFriendData.remove(playerUuid);
    }

    public static String getPlayerName(UUID uuid) {
        return resolvePlayerNames(List.of(uuid)).getOrDefault(uuid, "Unknown");
    }

    private static Map<UUID, String> resolvePlayerNames(Collection<UUID> uuids) {
        Map<UUID, String> names = new HashMap<>();
        if (uuids == null || uuids.isEmpty() || FriendDatabase.database == null) return names;

        List<String> idStrings = uuids.stream().map(UUID::toString).toList();
        names.putAll(fetchNamesFromCollection("data", idStrings));

        Set<UUID> unresolved = new HashSet<>(uuids);
        unresolved.removeAll(names.keySet());
        if (!unresolved.isEmpty()) {
            List<String> unresolvedIds = unresolved.stream().map(UUID::toString).toList();
            names.putAll(fetchNamesFromCollection("profiles", unresolvedIds));
        }

        return names;
    }

    private static Map<UUID, String> fetchNamesFromCollection(String collectionName, List<String> ids) {
        Map<UUID, String> names = new HashMap<>();
        try {
            MongoCollection<Document> collection = FriendDatabase.database.getCollection(collectionName);
            if (collection == null || ids.isEmpty()) return names;

            for (Document doc : collection.find(Filters.in("_id", ids))) {
                String id = doc.getString("_id");
                if (id == null) continue;

                String ign = parseStoredName(doc.getString("ign"));
                if (ign == null && doc.containsKey("ignLowercase")) {
                    ign = parseStoredName(doc.getString("ignLowercase"));
                }

                if (ign != null) {
                    names.put(UUID.fromString(id), ign);
                }
            }
        } catch (Exception e) {
            Logger.error(e, "Failed to resolve player names from collection {}", collectionName);
        }
        return names;
    }

    private static String parseStoredName(String raw) {
        if (raw == null) return null;
        raw = raw.trim();
        if (raw.startsWith("\"") && raw.endsWith("\"") && raw.length() >= 2) {
            raw = raw.substring(1, raw.length() - 1);
        }
        return raw.isEmpty() ? null : raw;
    }

    private static String formatServerDisplay(net.swofty.commons.presence.PresenceInfo info) {
        String type = info.getServerType();
        String id = info.getServerId();
        if (type == null && id == null) return null;
        if (id != null && id.matches("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$")) {
            return type; // hide raw UUIDs; show type only
        }
        if (type != null && id != null) return type + " - " + id;
        return type != null ? type : id;
    }

    private static void persistFriendData(UUID playerUuid) {
        FriendData data = cachedFriendData.get(playerUuid);
        if (data != null) {
            FriendDatabase db = new FriendDatabase(playerUuid.toString());
            db.saveFriendData(data);
        }
    }

    private static void sendEvent(FriendEvent event) {
        RedisClient.requestAllServersFromService(
                new FriendEventPushProtocol(),
                new FriendEventPushProtocol.Request(
                        event.getClass().getSimpleName(),
                        event.getSerializer().serialize(event),
                        event.getParticipants()
                ),
                300
        );
    }

    private static void sendErrorToPlayer(UUID playerUUID, String message) {
        sendMessageToPlayer(playerUUID, Text.of("<sep>\n<c>{}\n<sep>", message).serialize());
    }

    private static void sendMessageToPlayer(UUID playerUUID, String message) {
        RedisClient.requestAllServersFromService(
                new SendMessagePushProtocol(),
                new SendMessagePushProtocol.Request(playerUUID, message),
                300
        );
    }
}
