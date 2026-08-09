package net.swofty.type.garden;

import net.swofty.type.garden.chips.GardenChipService;
import net.swofty.type.garden.composter.GardenComposterService;
import net.swofty.type.garden.config.GardenConfigRegistry;
import net.swofty.type.garden.greenhouse.GardenGreenhouseService;
import net.swofty.type.garden.level.GardenLevelService;
import net.swofty.type.garden.milestone.GardenMilestoneService;
import net.swofty.type.garden.pest.GardenPestService;
import net.swofty.type.garden.shop.GardenDeskService;
import net.swofty.type.garden.visitor.GardenVisitorService;

public final class GardenServices {
    private static final GardenVisitorService VISITOR_SERVICE = new GardenVisitorService();
    private static final GardenPestService PEST_SERVICE = new GardenPestService();
    private static final GardenComposterService COMPOSTER_SERVICE = new GardenComposterService();
    private static final GardenChipService CHIP_SERVICE = new GardenChipService();
    private static final GardenGreenhouseService GREENHOUSE_SERVICE = new GardenGreenhouseService();
    private static final GardenLevelService LEVEL_SERVICE = new GardenLevelService();
    private static final GardenDeskService DESK_SERVICE = new GardenDeskService();
    private static final GardenMilestoneService MILESTONE_SERVICE = new GardenMilestoneService();

    private GardenServices() {
    }

    public static void initialize() {
        GardenConfigRegistry.reload();
        VISITOR_SERVICE.reload();
        PEST_SERVICE.reload();
        COMPOSTER_SERVICE.reload();
        CHIP_SERVICE.reload();
        GREENHOUSE_SERVICE.reload();
        LEVEL_SERVICE.reload();
        DESK_SERVICE.reload();
        MILESTONE_SERVICE.reload();
    }

    public static GardenVisitorService visitors() {
        return VISITOR_SERVICE;
    }

    public static GardenPestService pests() {
        return PEST_SERVICE;
    }

    public static GardenComposterService composter() {
        return COMPOSTER_SERVICE;
    }

    public static GardenChipService chips() {
        return CHIP_SERVICE;
    }

    public static GardenGreenhouseService greenhouse() {
        return GREENHOUSE_SERVICE;
    }

    public static GardenLevelService levels() {
        return LEVEL_SERVICE;
    }

    public static GardenDeskService desk() {
        return DESK_SERVICE;
    }

    public static GardenMilestoneService milestones() {
        return MILESTONE_SERVICE;
    }
}
