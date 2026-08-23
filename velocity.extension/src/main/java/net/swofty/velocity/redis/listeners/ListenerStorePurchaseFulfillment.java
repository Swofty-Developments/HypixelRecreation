package net.swofty.velocity.redis.listeners;

import com.mongodb.client.MongoCollection;
import com.mongodb.client.model.FindOneAndUpdateOptions;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.UpdateOptions;
import net.kyori.adventure.inventory.Book;
import net.swofty.PlayerField;
import net.swofty.codec.Codecs;
import net.swofty.commons.StringUtility;
import net.swofty.commons.data.SwoftyData;
import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.protocol.objects.proxy.to.StorePurchaseFulfillmentProtocol;
import net.swofty.commons.redis.RedisMessageContext;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.commons.text.Text;
import net.swofty.velocity.SkyBlockVelocity;
import net.swofty.velocity.data.StoreEntitlementsDatabase;
import net.swofty.velocity.text.ProxyText;
import org.bson.Document;
import org.tinylog.Logger;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class ListenerStorePurchaseFulfillment implements RedisMessageHandler<
    StorePurchaseFulfillmentProtocol.Request,
    StorePurchaseFulfillmentProtocol.Response> {

    private static final String SUPPORT_URL = "https://support.hypixel.net/";
    private static final UpdateOptions UPSERT = new UpdateOptions().upsert(true);

    private static final PlayerField<String> RANK = PlayerField.create("hypixel", "rank", Codecs.STRING, null);
    private static final FindOneAndUpdateOptions RETURN_UPDATED =
            new FindOneAndUpdateOptions().returnDocument(ReturnDocument.AFTER);
    private static final Map<String, Integer> STORE_RANK_STRENGTH = Map.of(
        "DEFAULT", 0,
        "VIP", 1,
        "VIP_PLUS", 2,
        "MVP", 3,
        "MVP_PLUS", 4,
        "MVP_PLUS_PLUS", 5
    );
    private static final ScheduledExecutorService EXPIRATION_RECONCILER =
        Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "store-rank-expiration-reconciler");
            thread.setDaemon(true);
            return thread;
        });

    @Override
    public RedisProtocol<StorePurchaseFulfillmentProtocol.Request, StorePurchaseFulfillmentProtocol.Response> protocol() {
        return new StorePurchaseFulfillmentProtocol();
    }

    @Override
    public StorePurchaseFulfillmentProtocol.Response handle(
        StorePurchaseFulfillmentProtocol.Request message,
        RedisMessageContext context
    ) {
        try {
            return fulfill(message);
        } catch (Exception exception) {
            Logger.error(exception, "Failed to fulfill store purchase {}", message.purchaseId());
            return new StorePurchaseFulfillmentProtocol.Response(false, false, exception.getMessage());
        }
    }

    private StorePurchaseFulfillmentProtocol.Response fulfill(StorePurchaseFulfillmentProtocol.Request purchase) {
        UUID playerUuid = UUID.fromString(purchase.playerUuid());
        MongoCollection<Document> entitlements = entitlementCollection();
        Date now = new Date();
        Date paidAt = new Date(purchase.paidAt() > 0 ? purchase.paidAt() : now.getTime());

        entitlements.updateOne(
                playerFilter(playerUuid),
                new Document("$setOnInsert", initialProjection(purchase, now)),
                UPSERT
        );

        Document projection = entitlements.findOneAndUpdate(
                unappliedPurchaseFilter(playerUuid, purchase.purchaseId()),
                updateFor(purchase, now, paidAt, !isOnline(playerUuid)),
                RETURN_UPDATED
        );
        boolean duplicate = projection == null;

        if (duplicate) {
            projection = entitlements.find(playerFilter(playerUuid)).first();
        } else {
            preserveTemporaryRankFallback(playerUuid, entitlements, purchase);
        }

        if (projection == null) {
            return new StorePurchaseFulfillmentProtocol.Response(false, duplicate, "Projection was not written.");
        }

        applyRankProjection(playerUuid, projection);
        if (!duplicate) {
            notifyOnlinePlayer(playerUuid, purchase);
        }
        return new StorePurchaseFulfillmentProtocol.Response(true, duplicate, null);
    }

    public static void startRankExpirationReconciler() {
        EXPIRATION_RECONCILER.scheduleWithFixedDelay(() -> {
            try {
                MongoCollection<Document> collection = entitlementCollection();
                Date now = new Date();
                Document expiredRankFilter = new Document("entitlements", new Document("$elemMatch",
                    new Document("type", "RANK").append("expiresAt", new Document("$lte", now))));

                for (Document projection : collection.find(expiredRankFilter)) {
                    applyRankProjection(UUID.fromString(projection.getString("_id")), projection);
                }
            } catch (Exception exception) {
                Logger.error(exception, "Failed to reconcile expired store rank entitlements");
            }
        }, 1, 60, TimeUnit.SECONDS);
    }

    private void preserveTemporaryRankFallback(
        UUID playerUuid,
        MongoCollection<Document> collection,
        StorePurchaseFulfillmentProtocol.Request message
    ) {
        boolean hasTemporaryRank = message.entitlements().stream()
            .anyMatch(entitlement -> "RANK".equals(entitlement.type()) && entitlement.durationDays() != null);
        if (!hasTemporaryRank) return;

        Document projection = collection.find(new Document("_id", playerUuid.toString())).first();
        if (projection != null && projection.getString("temporaryRankFallback") != null) return;

        String currentRank = currentProfileRank(playerUuid);
        if ("MVP_PLUS_PLUS".equals(currentRank)) {
            currentRank = "MVP_PLUS";
        }

        collection.updateOne(
            new Document("_id", playerUuid.toString())
                .append("temporaryRankFallback", new Document("$exists", false)),
            new Document("$set", new Document("temporaryRankFallback", currentRank))
        );
    }

    private Document updateFor(
        StorePurchaseFulfillmentProtocol.Request message,
        Date now,
        Date paidAt,
        boolean queueNotification
    ) {
        Document set = new Document("playerName", message.playerName())
            .append("updatedAt", now);
        Document addToSet = new Document("appliedPurchaseIds", message.purchaseId());
        Document inc = new Document();
        Document eachEntitlements = new Document("$each", entitlementDocuments(message, paidAt));
        addToSet.append("entitlements", eachEntitlements);
        if (queueNotification) {
            addToSet.append("pendingStoreNotifications", notificationDocument(message, paidAt));
        }

        for (StorePurchaseFulfillmentProtocol.Entitlement entitlement : message.entitlements()) {
            switch (entitlement.type()) {
                case "STORE_CURRENCY" -> inc.append("storeGold", entitlement.amount());
                case "SKYBLOCK_GEMS" -> inc.append("skyBlockGems", entitlement.amount());
                case "FEATURE" -> addToSet.append("featureFlags", entitlement.key());
                case "BOOSTER" -> inc.append("boosters." + entitlement.key(), entitlement.amount());
                case "COSMETIC" -> inc.append("cosmetics." + entitlement.key(), entitlement.amount());
                default -> {
                }
            }
        }

        Document update = new Document("$set", set)
            .append("$addToSet", addToSet);
        if (!inc.isEmpty()) {
            update.append("$inc", inc);
        }
        return update;
    }

    private static void notifyOnlinePlayer(UUID playerUuid, StorePurchaseFulfillmentProtocol.Request message) {
        SkyBlockVelocity.getServer().getPlayer(playerUuid).ifPresent(player -> {
            boolean hasRank = message.entitlements().stream()
                .anyMatch(entitlement -> "RANK".equals(entitlement.type()));

            if (hasRank) {
                player.openBook(purchaseBook(message.entitlements()));
                return;
            }

            player.sendMessage(Text.of("<b>{}", deliveryMessage(message)));
        });
    }

    private static Book purchaseBook(List<StorePurchaseFulfillmentProtocol.Entitlement> entitlements) {
        return Book.builder().addPage(ProxyText.render(
                "<0>Your purchase has been processed!\n\nYou've received the following items:\n{0}\n\n"
                        + "If you have any problems, <d><n><click:url:'{1}'>contact support.",
                rankItems(entitlements), SUPPORT_URL)).build();
    }

    private static Text rankItems(List<StorePurchaseFulfillmentProtocol.Entitlement> entitlements) {
        List<Text> ranks = entitlements.stream()
            .filter(entitlement -> "RANK".equals(entitlement.type()))
            .map(ListenerStorePurchaseFulfillment::rankItem)
                .toList();
        if (ranks.isEmpty()) return Text.of("<0>Rank");

        return Text.join(Text.of("\n"), ranks);
    }

    private static Text rankItem(StorePurchaseFulfillmentProtocol.Entitlement entitlement) {
        Long durationDays = entitlement.durationDays();
        return durationDays == null
                ? Text.of("{}<0> Rank", rankName(entitlement.key()))
                : Text.of("{}<0> Rank ({} days)", rankName(entitlement.key()), durationDays);
    }

    private static Text rankName(String rank) {
        return switch (rank) {
            case "VIP" -> Text.of("<a>VIP");
            case "VIP_PLUS" -> Text.of("<a>VIP<6>+");
            case "MVP" -> Text.of("<b>MVP");
            case "MVP_PLUS" -> Text.of("<b>MVP<c>+");
            case "MVP_PLUS_PLUS" -> Text.of("<6>MVP<c>++");
            default -> Text.of("<0>{}", readableKey(rank));
        };
    }

    private static String deliveryMessage(StorePurchaseFulfillmentProtocol.Request message) {
        String packageName = message.productName();

        long skyBlockGems = entitlementAmount(message.entitlements(), "SKYBLOCK_GEMS");
        if (skyBlockGems > 0) {
            packageName = StringUtility.commaify(skyBlockGems) + " SkyBlock Gems";
        }

        long gold = entitlementAmount(message.entitlements(), "STORE_CURRENCY");
        if (gold > 0) {
            packageName = StringUtility.commaify(gold) + " Gold";
        }

        return "Your package of " + packageName
            + " has been processed and delivered. You may need to log out and back in to receive the full effects.";
    }

    private static long entitlementAmount(
        List<StorePurchaseFulfillmentProtocol.Entitlement> entitlements,
        String type
    ) {
        return entitlements.stream()
            .filter(entitlement -> type.equals(entitlement.type()))
            .mapToLong(StorePurchaseFulfillmentProtocol.Entitlement::amount)
            .sum();
    }

    private static String readableKey(String key) {
        if (key == null || key.isBlank()) return "Unknown";
        return switch (key) {
            case "VIP_PLUS" -> "VIP+";
            case "MVP_PLUS" -> "MVP+";
            case "MVP_PLUS_PLUS" -> "MVP++";
            default -> StringUtility.toNormalCase(key);
        };
    }

    private Document initialProjection(StorePurchaseFulfillmentProtocol.Request message, Date now) {
        return new Document("_id", message.playerUuid())
            .append("playerName", message.playerName())
            .append("createdAt", now)
            .append("updatedAt", now)
            .append("storeGold", 0L)
            .append("skyBlockGems", 0L);
    }

    private List<Document> entitlementDocuments(StorePurchaseFulfillmentProtocol.Request message, Date paidAt) {
        return message.entitlements().stream().map(entitlement -> {
            Document document = new Document("purchaseId", message.purchaseId())
                .append("productId", message.productId())
                .append("productName", message.productName())
                .append("type", entitlement.type())
                .append("key", entitlement.key())
                .append("amount", entitlement.amount())
                .append("awardedAt", paidAt);

            if (entitlement.durationDays() != null) {
                Instant expiresAt = paidAt.toInstant().plus(entitlement.durationDays(), ChronoUnit.DAYS);
                document.append("durationDays", entitlement.durationDays())
                    .append("expiresAt", Date.from(expiresAt));
            }

            return document;
        }).toList();
    }

    private Document notificationDocument(StorePurchaseFulfillmentProtocol.Request message, Date paidAt) {
        return new Document("purchaseId", message.purchaseId())
            .append("productId", message.productId())
            .append("productName", message.productName())
            .append("createdAt", paidAt)
            .append("entitlements", entitlementDocuments(message, paidAt));
    }

    private static void applyRankProjection(UUID playerUuid, Document projection) {
        String highestRank = highestActiveStoreRank(projection);
        String currentRank = currentProfileRank(playerUuid);

        if ("STAFF".equals(currentRank) || "YOUTUBE".equals(currentRank)) {
            return;
        }

        if (highestRank == null) {
            if ("MVP_PLUS_PLUS".equals(currentRank)) {
                String fallback = projection.getString("temporaryRankFallback");
                setProfileRank(playerUuid, fallback != null ? fallback : "DEFAULT");
            }
            return;
        }

        int currentStrength = STORE_RANK_STRENGTH.getOrDefault(currentRank, 0);
        int targetStrength = STORE_RANK_STRENGTH.getOrDefault(highestRank, 0);
        if (targetStrength < currentStrength && !"MVP_PLUS_PLUS".equals(currentRank)) {
            return;
        }

        setProfileRank(playerUuid, highestRank);
    }

    private static String currentProfileRank(UUID playerUuid) {
        try {
            return deserializeRank(SwoftyData.account().get(playerUuid, RANK));
        } finally {
            SwoftyData.account().unload(playerUuid);
        }
    }

    private static void setProfileRank(UUID playerUuid, String rank) {
        try {
            SwoftyData.account().set(playerUuid, RANK, serializeRank(rank));
        } finally {
            SwoftyData.account().unload(playerUuid);
        }
    }

    private static MongoCollection<Document> entitlementCollection() {
        return StoreEntitlementsDatabase.collection();
    }

    private static Document playerFilter(UUID playerUuid) {
        return new Document("_id", playerUuid.toString());
    }

    private static Document unappliedPurchaseFilter(UUID playerUuid, String purchaseId) {
        return playerFilter(playerUuid)
                .append("appliedPurchaseIds", new Document("$ne", purchaseId));
    }

    private static boolean isOnline(UUID playerUuid) {
        return SkyBlockVelocity.getServer().getPlayer(playerUuid).isPresent();
    }

    private static String highestActiveStoreRank(Document projection) {
        List<Document> entitlements = projection.getList("entitlements", Document.class, List.of());
        Instant now = Instant.now();
        String best = null;
        int bestStrength = -1;

        for (Document entitlement : entitlements) {
            if (!"RANK".equals(entitlement.getString("type"))) continue;
            Date expiresAt = entitlement.getDate("expiresAt");
            if (expiresAt != null && expiresAt.toInstant().isBefore(now)) continue;

            String rank = entitlement.getString("key");
            int strength = STORE_RANK_STRENGTH.getOrDefault(rank, -1);
            if (strength > bestStrength) {
                best = rank;
                bestStrength = strength;
            }
        }

        return best;
    }

    private static String deserializeRank(String value) {
        if (value == null || value.isBlank()) return "DEFAULT";
        return value.replace("\"", "");
    }

    private static String serializeRank(String rank) {
        return "\"" + rank + "\"";
    }

}
