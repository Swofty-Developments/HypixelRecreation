package net.swofty.type.skyblockgeneric.fishing;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.kyori.adventure.key.Key;
import net.swofty.commons.loot.LootTable;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.skyblockgeneric.entity.FishingHook;
import net.swofty.type.skyblockgeneric.fishing.bait.FishingBaitService;
import net.swofty.type.skyblockgeneric.fishing.catches.CatchAwardContext;
import net.swofty.type.skyblockgeneric.fishing.catches.CatchPayload;
import net.swofty.type.skyblockgeneric.fishing.hotspot.FishingHotspotService;
import net.swofty.type.skyblockgeneric.fishing.item.FishingItemSupport;
import net.swofty.type.skyblockgeneric.fishing.resolver.FishingCatchResolver;
import net.swofty.type.skyblockgeneric.fishing.rod.FishingRodPartService;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.FishingBaitComponent;
import net.swofty.type.skyblockgeneric.item.components.FishingRodPartComponent;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FishingService {
    private static final Map<UUID, FishingSession> SESSIONS = new ConcurrentHashMap<>();

    public static FishingSession beginCast(SkyBlockPlayer player, SkyBlockItem rod, FishingMedium medium) {
        FishingBaitComponent bait = FishingBaitService.getFirstAvailableBait(player, medium);
        FishingSession session = new FishingSession(
            player.getUuid(),
            rod.getAttributeHandler().getPotentialType().name(),
            medium,
            bait == null ? null : bait.getItemId(),
            System.currentTimeMillis(),
            0L,
            0L,
            false,
            false
        );
        SESSIONS.put(player.getUuid(), session);
        return session;
    }

    public static @Nullable FishingSession getSession(UUID playerUuid) {
        return SESSIONS.get(playerUuid);
    }

    public static void updateSession(FishingSession session) {
        SESSIONS.put(session.ownerUuid(), session);
    }

    public static void clearSession(UUID playerUuid) {
        SESSIONS.remove(playerUuid);
    }

    public static long computeWaitTicks(SkyBlockPlayer player, SkyBlockItem rod, @Nullable FishingBaitComponent bait) {
        double fishingSpeed = player.getStatistics().allStatistics().getOverall(net.swofty.commons.skyblock.statistics.ItemStatistic.FISHING_SPEED)
            + rod.getAttributeHandler().getStatistics().getOverall(net.swofty.commons.skyblock.statistics.ItemStatistic.FISHING_SPEED)
            + FishingRodPartService.getStatistics(rod).getOverall(net.swofty.commons.skyblock.statistics.ItemStatistic.FISHING_SPEED);
        if (bait != null) {
            SkyBlockItem baitItem = FishingItemSupport.getItem(bait.getItemId());
            if (baitItem != null) {
                fishingSpeed += baitItem.getAttributeHandler().getStatistics().getOverall(net.swofty.commons.skyblock.statistics.ItemStatistic.FISHING_SPEED);
            }
            if ("CORRUPTED_BAIT".equals(bait.getItemId())) {
                fishingSpeed /= 2.0D;
            }
        }
        long baseTicks = 80L;
        return Math.max(20L, Math.round(baseTicks - Math.min(50D, fishingSpeed / 2D)));
    }

    public static @Nullable CatchPayload resolveCatch(SkyBlockPlayer player, SkyBlockItem rod, FishingHook hook) {
        FishingSession session = getSession(player.getUuid());
        if (session == null || session.resolved()) {
            return null;
        }

        FishingBaitComponent bait = FishingItemSupport.getBait(session.baitItemId());
        SkyBlockRegion region = player.getRegion();
        var hotspotBuffs = FishingHotspotService.getActiveHotspotBuffs(player, session.medium(), hook.getSpawnPosition());
        boolean hotspotActive = hasAnyBuffValue(hotspotBuffs);
        FishingContext context = new FishingContext(
            player,
            rod,
            session.medium(),
            bait,
            FishingRodPartService.getHook(rod),
            FishingRodPartService.getLine(rod),
            FishingRodPartService.getSinker(rod),
            region == null ? null : region.getType().name(),
            hook.getSpawnPosition(),
            hotspotActive,
            hotspotBuffs,
            System.currentTimeMillis() - session.castAt()
        );

        CatchPayload payload = FishingCatchResolver.resolve(context);
        if (payload == null) {
            return null;
        }

        consumeBait(player, rod, session);
        awardCatch(player, rod, hook, payload);
        updateSession(session.withResolved(true));
        return payload;
    }

    private static void consumeBait(SkyBlockPlayer player, SkyBlockItem rod, FishingSession session) {
        if (session.baitItemId() == null) {
            return;
        }
        if (rollBaitPreservation(rod)) {
            return;
        }
        FishingBaitService.consumeOneBait(player, session.baitItemId());
    }

    private static boolean rollBaitPreservation(SkyBlockItem rod) {
        var caster = rod.getAttributeHandler().getEnchantment(net.swofty.type.skyblockgeneric.enchantment.EnchantmentType.CASTER);
        if (caster != null && Math.random() * 100 < caster.level()) {
            return true;
        }
        FishingRodPartComponent sinker = FishingRodPartService.getSinker(rod);
        return sinker != null
                && sinker.getBaitPreservationChance() > 0
                && Math.random() * 100 < sinker.getBaitPreservationChance();
    }

    private static void awardCatch(SkyBlockPlayer player, SkyBlockItem rod, FishingHook hook, CatchPayload payload) {
        markFirstFishToggle(player);
        rollSinkerMaterialize(player, rod);

        payload.apply(new CatchAwardContext(player, rod, hook.getSpawnPosition()));

        if (!(payload instanceof CatchPayload.SeaCreature) && payload.skillXp() > 0) {
            player.getSkills().increase(player, net.swofty.type.skyblockgeneric.skill.SkillCategories.FISHING, payload.skillXp());
        }

        player.setItemInHand(rod);
    }

    private static void markFirstFishToggle(SkyBlockPlayer player) {
        if (!player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_CAUGHT_FIRST_FISH)) {
            player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_CAUGHT_FIRST_FISH, true);
        }
    }

    private static void rollSinkerMaterialize(SkyBlockPlayer player, SkyBlockItem rod) {
        FishingRodPartComponent sinker = FishingRodPartService.getSinker(rod);
        if (sinker == null || sinker.getMaterializedItemId() == null) {
            return;
        }
        boolean materialized = LootTable.rollSingle(Key.key("skyblock", "fishing/sinker_materialize"),
                sinker.getMaterializedItemId(), sinker.getMaterializedChance()).isPresent();
        if (materialized) {
            player.addAndUpdateItem(net.swofty.commons.skyblock.item.ItemType.valueOf(sinker.getMaterializedItemId()));
        }
    }

    private static boolean hasAnyBuffValue(net.swofty.commons.skyblock.statistics.ItemStatistics statistics) {
        for (net.swofty.commons.skyblock.statistics.ItemStatistic statistic : net.swofty.commons.skyblock.statistics.ItemStatistic.values()) {
            if (statistics.getOverall(statistic) != 0.0D) {
                return true;
            }
        }
        return false;
    }
}
