package net.swofty.type.garden.events.custom;

import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.type.garden.GardenCropRegistry;
import net.swofty.type.garden.GardenServices;
import net.swofty.type.garden.config.GardenConfigRegistry;
import net.swofty.type.garden.gui.GardenGuiSupport;
import net.swofty.type.garden.plot.GardenPlotService;
import net.swofty.type.garden.user.SkyBlockGarden;
import net.swofty.type.generic.event.EventNodes;
import net.swofty.type.generic.event.HypixelEventClass;
import net.swofty.type.generic.event.phase.PhasedEvent;
import net.swofty.type.skyblockgeneric.event.custom.CustomBlockBreakEvent;
import net.swofty.type.skyblockgeneric.garden.GardenData;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

public class ActionGardenBlockBreak implements HypixelEventClass {
    @PhasedEvent(node = EventNodes.CUSTOM, requireDataLoaded = true)
    public void run(CustomBlockBreakEvent event) {
        SkyBlockPlayer player = event.getPlayer();
        if (!player.isOnGarden() || Boolean.TRUE.equals(event.getPlayerPlaced())) {
            return;
        }

        GardenCropRegistry.CropContext crop = GardenCropRegistry.fromMaterial(event.getMaterial());
        if (crop == null) {
            return;
        }

        long now = System.currentTimeMillis();
        GardenData.GardenVisitorsData visitors = GardenGuiSupport.visitors(player);
        GardenData.GardenPersonalData personal = GardenGuiSupport.personal(player);
        visitors.setLastFarmingActivityAt(now);

        double totalFortune = player.getStatistics().allStatistics().getOverall(ItemStatistic.FARMING_FORTUNE)
            + (crop.specificFortune() == null
            ? 0D
            : player.getStatistics().allStatistics().getOverall(crop.specificFortune()));
        personal.setSowdust(personal.getSowdust() + GardenServices.chips().calculateGardenSowdust(totalFortune, crop.doubleBreakCrop()));

        long harvestedAmount = getHarvestedAmount(event, crop.cropId());
        GardenServices.milestones().advanceCropMilestone(player, crop.cropId(), harvestedAmount);
        trySpawnPest(player, crop, now);
    }

    private long getHarvestedAmount(CustomBlockBreakEvent event, String cropId) {
        return event.getDrops().stream()
            .filter(drop -> {
                ItemType type = drop.getAttributeHandler().getPotentialType();
                if (type == null) {
                    return false;
                }
                if ("MUSHROOM".equalsIgnoreCase(cropId)) {
                    return type.name().contains("MUSHROOM");
                }
                return type.name().equalsIgnoreCase(cropId);
            })
            .mapToLong(SkyBlockItem::getAmount)
            .sum();
    }

    private void trySpawnPest(SkyBlockPlayer player, GardenCropRegistry.CropContext crop, long now) {
        GardenData.GardenCoreData core = GardenGuiSupport.core(player);
        GardenData.GardenPestsData pests = GardenGuiSupport.pests(player);
        if (core.getLevel() < GardenConfigRegistry.getInt(
            GardenConfigRegistry.getConfig("pests.yml"),
            "start_garden_level",
            5
        )) {
            return;
        }
        if (now < pests.getCooldownEndsAt()) {
            return;
        }

        List<Map<String, Object>> eligible = GardenServices.pests().getPests().stream()
            .filter(entry -> !GardenConfigRegistry.getBoolean(entry, "trap_only", false))
            .filter(entry -> core.getLevel() >= GardenConfigRegistry.getInt(entry, "garden_level", 5))
            .filter(entry -> GardenConfigRegistry.getString(entry, "crop", "").equalsIgnoreCase(crop.cropId()))
            .toList();
        if (eligible.isEmpty()) {
            return;
        }
        if (ThreadLocalRandom.current().nextDouble() > GardenServices.pests().getBaseSpawnChance()) {
            return;
        }

        Map<String, Object> chosen = eligible.get(ThreadLocalRandom.current().nextInt(eligible.size()));
        SkyBlockGarden garden = GardenGuiSupport.garden(player);
        if (garden == null) {
            return;
        }

        GardenPlotService plotService = garden.getPlotService();
        String plotId = plotService.getPlotAt(player.getPosition()) == null
            ? "central"
            : plotService.getPlotAt(player.getPosition()).id();

        GardenData.GardenPestState pest = new GardenData.GardenPestState();
        pest.setPestId(GardenConfigRegistry.getString(chosen, "id", ""));
        pest.setPlotId(plotId);
        pest.setSpawnedAt(now);
        pests.getActivePests().add(pest);
        pests.setCooldownEndsAt(now + GardenServices.pests().calculateSpawnCooldownSeconds(new net.swofty.type.garden.pest.GardenPestService.CooldownModifiers(
            false,
            0,
            false,
            0,
            pests.getRepellentEndsAt() > now,
            pests.isRepellentMax()
        )) * 1000L);
        pests.setLastSpawnCheckAt(now);

        String displayName = GardenConfigRegistry.getString(chosen, "display_name", "Pest");
        player.sendMessage("§cA §6" + displayName + " §chas infested your Garden!");
    }
}
