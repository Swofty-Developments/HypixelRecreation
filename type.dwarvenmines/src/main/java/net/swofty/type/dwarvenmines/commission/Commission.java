package net.swofty.type.dwarvenmines.commission;

import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.region.RegionType;

public class Commission {
    public final String name;
    public final CommissionCategory category;
    public final Objective objective;
    public final boolean oneTimeOnly;

    public Commission(
            String name,
            CommissionCategory category,
            Objective objective,
            boolean oneTimeOnly
    ) {
        this.name = name;
        this.category = category;
        this.objective = objective;
        this.oneTimeOnly = oneTimeOnly;
    }

    public Text generateDescription() {
        return switch (objective.type) {
            case MINE -> withSuffix(Text.of("Mine <a>{} <7>{} Ore",
                    objective.amount, getTargetName(objective.target)), true, false);
            case SLAY -> withSuffix(Text.of("Slay <a>{} <7>{}",
                    objective.amount, getTargetNamePlural(objective.target)), true, true);
            case DAMAGE -> withSuffix(Text.of("Damage {} <7>{} times",
                    getTargetNamePlural(objective.target), objective.amount), true, false);
            case PARTICIPATE -> Text.of("Participate in the {}.", getEventName(objective.event));
            case COLLECT -> withSuffix(Text.of("Collect {} {}",
                    objective.amount, getCollectibleName(objective.target)), false, true);
            case DEPOSIT -> withSuffix(Text.of("Deposit {} Tickets", objective.amount), false, true);
        };
    }

    private Text withSuffix(Text base, boolean allowLocation, boolean allowEvent) {
        RegionType region = allowLocation && !objective.location.isAny()
                ? objective.location.getRegion().orElse(null)
                : null;

        if (allowEvent && objective.event != EventType.NONE) {
            Text located = region == null ? base : base.append(" in <b>{}", region.getName());
            return located.append(" during the {}.", getEventName(objective.event));
        }
        if (region != null) {
            return base.append(" in <b>{}.", region.getName());
        }
        return base.append(".");
    }

    private String getTargetName(Objective.BlockTarget target) {
        return switch (target) {
            case MITHRIL -> "Mithril";
            case TITANIUM -> "Titanium";
            case GOBLIN -> "Goblin";
            case GLACITE_WALKER -> "Glacite Walker";
            case TREASURE_HOARDER -> "Treasure Hoarder";
            case GOLDEN_GOBLIN -> "Golden Goblin";
            case STAR_SENTRY -> "Star Sentry";
            case NONE -> "";
        };
    }

    private Text getTargetNamePlural(Objective.BlockTarget target) {
        return switch (target) {
            case MITHRIL -> Text.of("Mithril Ore");
            case TITANIUM -> Text.of("Titanium Ore");
            case GOBLIN -> Text.of("<c>Goblins");
            case GLACITE_WALKER -> Text.of("<b>Glacite Walkers");
            case TREASURE_HOARDER -> Text.of("<c>Treasure Hoarders");
            case GOLDEN_GOBLIN -> Text.of("<6>Golden Goblin");
            case STAR_SENTRY -> Text.of("Star Sentrys");
            case NONE -> Text.empty();
        };
    }

    private String getCollectibleName(Objective.BlockTarget target) {
        return switch (target) {
            case MITHRIL -> "Mithril Powder";
            default -> target.name();
        };
    }

    private Text getEventName(EventType event) {
        return switch (event) {
            case GOBLIN_RAID -> Text.of("<c>Goblin Raid <7>Event");
            case RAFFLE -> Text.of("<e>Raffle <7>Event");
            case DOUBLE_POWDER -> Text.of("2x Powder Event");
            case NONE -> Text.empty();
        };
    }
}
