package net.swofty.type.generic.collectibles;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.swofty.commons.bedwars.BedwarsLevelUtil;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.data.datapoints.DatapointLeaderboardLong;
import net.swofty.type.generic.data.handlers.BedWarsDataHandler;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;

import java.text.NumberFormat;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CollectibleEvaluator {

    private static final DateTimeFormatter WINDOW_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd").withZone(ZoneOffset.UTC);
    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getIntegerInstance(Locale.US);

    public static CollectibleSelectionCheck checkSelectable(
        HypixelPlayer player,
        CollectibleDefinition definition,
        boolean manuallyUnlocked
    ) {
        CollectibleUnlockRequirement requirement = definition.unlockRequirement();
        CollectibleEvent event = requirement.event();
        if (event != null && !event.isAvailableNow()) {
            return CollectibleSelectionCheck.blocked(unavailableReason(requirement, event));
        }

        Rank requiredRank = requirement.requiredRank();
        if (requiredRank != null && !player.getRank().isEqualOrHigherThan(requiredRank)) {
            return CollectibleSelectionCheck.blocked("<c>Requires "
                + requiredRank.prefixMarkup() + "<c>.");
        }

        return switch (requirement.method()) {
            case FREE, RANK -> CollectibleSelectionCheck.allowed();
            case MANUAL, CURRENCY -> manuallyUnlocked
                ? CollectibleSelectionCheck.allowed()
                : CollectibleSelectionCheck.blocked(defaultReason(requirement, "Locked."));
            case CUSTOM -> {
                if (manuallyUnlocked) {
                    yield CollectibleSelectionCheck.allowed();
                }
                String resolverKey = requirement.customResolverKey();
                boolean unlocked = CollectibleUnlockResolverRegistry.get(resolverKey)
                    .map(resolver -> resolver.isUnlocked(player, definition))
                    .orElse(false);
                if (unlocked) {
                    yield CollectibleSelectionCheck.allowed();
                }
                yield CollectibleSelectionCheck.blocked(defaultReason(requirement, "Locked."));
            }
            case BEDWARS_LEVEL -> {
                int requiredLevel = requirement.cost() == null ? 0 : requirement.cost().intValue();
                if (bedWarsLevel(player) >= requiredLevel) {
                    yield CollectibleSelectionCheck.allowed();
                }
                yield CollectibleSelectionCheck.blocked(defaultReason(requirement, "<c><l>!! </l><c>You must be level "
                    + NUMBER_FORMAT.format(requiredLevel) + " to use this\n<c>prestige!"));
            }
        };
    }

    private static int bedWarsLevel(HypixelPlayer player) {
        BedWarsDataHandler handler = BedWarsDataHandler.getUser(player);
        if (handler == null) {
            return 0;
        }
        long experience = handler.get(BedWarsDataHandler.Data.EXPERIENCE, DatapointLeaderboardLong.class).getValue();
        return BedwarsLevelUtil.calculateLevel(experience);
    }

    private static String defaultReason(CollectibleUnlockRequirement requirement, String fallback) {
        if (requirement.customDisplayText() != null && !requirement.customDisplayText().isBlank()) {
            return "<c>" + requirement.customDisplayText();
        }
        if (requirement.method() == CollectibleUnlockMethod.CURRENCY
            && requirement.cost() != null
            && requirement.cost() > 0) {
            return "<7>Cost: " + requirement.currency().getColor() + NUMBER_FORMAT.format(requirement.cost()) + " " + requirement.currency().getDisplayName() + "<7>.";
        }
        return fallback;
    }

    private static String unavailableReason(CollectibleUnlockRequirement requirement, CollectibleEvent event) {
        String customText = requirement.customDisplayText();
        if (event != null) {
            return "<c>Available during the " + event.displayName() + " Event.";
        }

        if (customText != null && !customText.isBlank()) {
            return "<c>" + customText;
        }

        return "<c>Unavailable right now.";
    }

    private static String formatTimestamp(Long timestampMs) {
        if (timestampMs == null) {
            return null;
        }
        return WINDOW_FORMATTER.format(Instant.ofEpochMilli(timestampMs));
    }
}
