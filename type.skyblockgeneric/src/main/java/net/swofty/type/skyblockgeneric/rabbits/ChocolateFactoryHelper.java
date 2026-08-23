package net.swofty.type.skyblockgeneric.rabbits;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.UtilityClass;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointChocolateFactory.EmployeeType;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.function.Consumer;

@UtilityClass
public class ChocolateFactoryHelper {
    private static final int SECONDS_PER_HOUR = 3600;

    private static final int HAND_BAKED_MAX_INTERNAL_LEVEL = 9;
    private static final long HAND_BAKED_COST_STEP = 500L;

    private static final long RABBIT_BARN_BASE_COST = 5000L;
    private static final double RABBIT_BARN_COST_GROWTH = 1.05;

    private static final String RABBIT_BRO_NAME = "Rabbit Bro";
    private static final int RABBIT_BRO_SPECIAL_MAX_LEVEL = 10;
    private static final int RABBIT_BRO_BASE_COST = 30;
    private static final int RABBIT_BRO_CF_SCALING = 20;
    private static final int EMPLOYEE_BASE_COST = 216;
    private static final double EMPLOYEE_CF_SCALING = 144.0;
    private static final double EMPLOYEE_COST_GROWTH = 1.05;

    private static final NumberFormat NUMBER_FORMAT = NumberFormat.getNumberInstance(Locale.US);
    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("#,##0.0");

    public static void updateProduction(SkyBlockPlayer player) {
        mutateData(player, DatapointChocolateFactory.ChocolateFactoryData::updateChocolateFromProduction);
    }

    public static DatapointChocolateFactory.ChocolateFactoryData getData(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.CHOCOLATE_FACTORY, DatapointChocolateFactory.class)
                .getValue();
    }

    public static void saveData(SkyBlockPlayer player, DatapointChocolateFactory.ChocolateFactoryData data) {
        player.getSkyblockDataHandler()
                .get(SkyBlockDataHandler.Data.CHOCOLATE_FACTORY, DatapointChocolateFactory.class)
                .setValue(data);
    }

    public static void handleClick(SkyBlockPlayer player) {
        mutateData(player, DatapointChocolateFactory.ChocolateFactoryData::click);
    }

    public static String formatChocolate(long amount) {
        return NUMBER_FORMAT.format(amount);
    }

    public static String formatProductionPerSecond(double production) {
        return DECIMAL_FORMAT.format(production) + "/s";
    }

    /**
     * Formats production per hour for display
     */
    public static String formatProductionPerHour(double productionPerSecond) {
        double perHour = productionPerSecond * SECONDS_PER_HOUR;
        return formatChocolate((long) perHour) + "/h";
    }

    /**
     * Gets the upgrade cost for Hand-Baked Chocolate
     * Level 1→2: 500, Level 2→3: 1000, etc.
     * Internal level 0 = display level 1, so cost = 500 * (internalLevel + 1)
     * Max internal level is 9 (display level 10)
     */
    public static long getHandBakedChocolateCost(int currentLevel) {
        if (currentLevel >= HAND_BAKED_MAX_INTERNAL_LEVEL) return 0; // Already maxed at level 10
        return HAND_BAKED_COST_STEP * (currentLevel + 1);
    }

    /**
     * Gets the upgrade cost for Rabbit Barn
     * Level 1 is unlocked by default (free)
     * Level 2 costs 5,000 Chocolate
     * Subsequent levels: floor(5000 × 1.05^(L-2)) where L is target level
     */
    public static long getRabbitBarnCost(int currentLevel) {
        // Level 1 is free (unlocked by default)
        if (currentLevel == 0) {
            return 0;
        }
        // Cost formula: 5000 × 1.05^(currentLevel-1), rounded
        return Math.round(RABBIT_BARN_BASE_COST * Math.pow(RABBIT_BARN_COST_GROWTH, currentLevel - 1));
    }

    /**
     * Gets the upgrade cost for Rabbit Shrine
     * Max level is 20, providing 40% extra chance for higher rarity rabbits
     */
    public static long getRabbitShrineCost(int currentLevel) {
        // Cost to upgrade TO level (currentLevel + 1)
        return switch (currentLevel) {
            case 0 -> 10_000_000L;   // To level 1
            case 1 -> 20_000_000L;   // To level 2
            case 2 -> 30_000_000L;   // To level 3
            case 3 -> 40_000_000L;   // To level 4
            case 4 -> 60_000_000L;   // To level 5
            case 5 -> 80_000_000L;   // To level 6
            case 6 -> 100_000_000L;  // To level 7
            case 7 -> 120_000_000L;  // To level 8
            case 8 -> 150_000_000L;  // To level 9
            case 9 -> 200_000_000L;  // To level 10
            case 10 -> 250_000_000L; // To level 11
            case 11 -> 300_000_000L; // To level 12
            case 12 -> 350_000_000L; // To level 13
            case 13 -> 400_000_000L; // To level 14
            case 14 -> 450_000_000L; // To level 15
            case 15 -> 500_000_000L; // To level 16
            case 16 -> 550_000_000L; // To level 17
            case 17 -> 600_000_000L; // To level 18
            case 18 -> 650_000_000L; // To level 19
            case 19 -> 700_000_000L; // To level 20
            default -> 0L;           // Already maxed
        };
    }

    /**
     * Gets the upgrade cost for Time Tower based on prestige level
     * Level 1 is unlocked by default (free)
     */
    public static long getTimeTowerCost(int currentLevel, int prestigeLevel) {
        // Level 1 is free (unlocked by default)
        if (currentLevel == 0) {
            return 0;
        }

        // Base cost depends on prestige (Factory II = prestige 1, Factory III = prestige 2, etc.)
        long baseCost = switch (prestigeLevel) {
            case 1 -> 5_500_000L;  // Factory II
            case 2 -> 6_000_000L;  // Factory III
            case 3 -> 6_500_000L;  // Factory IV
            case 4 -> 7_000_000L;  // Factory V
            default -> 7_500_000L; // Factory VI+
        };

        // Multiplier for each level (upgrading TO level currentLevel+1)
        int multiplier = switch (currentLevel) {
            case 1 -> 1;   // To level 2
            case 2 -> 2;   // To level 3
            case 3 -> 3;   // To level 4
            case 4 -> 4;   // To level 5
            case 5 -> 6;   // To level 6
            case 6 -> 8;   // To level 7
            case 7 -> 10;  // To level 8
            case 8 -> 12;  // To level 9
            case 9 -> 14;  // To level 10
            case 10 -> 16; // To level 11
            case 11 -> 20; // To level 12
            case 12 -> 24; // To level 13
            case 13 -> 30; // To level 14
            case 14 -> 40; // To level 15
            default -> 40;
        };

        return baseCost * multiplier;
    }

    /**
     * Gets the upgrade cost for Time Tower (legacy, uses minimum prestige)
     */
    public static long getTimeTowerCost(int currentLevel) {
        return getTimeTowerCost(currentLevel, 1);
    }

    /**
     * Gets the upgrade cost for Coach Jackrabbit
     * Max level is 20, providing +0.2x CpS multiplier
     */
    public static long getCoachJackrabbitCost(int currentLevel) {
        // Cost to upgrade TO level (currentLevel + 1)
        return switch (currentLevel) {
            case 0 -> 2_200_000L;    // To level 1
            case 1 -> 3_900_000L;    // To level 2
            case 2 -> 5_300_000L;    // To level 3
            case 3 -> 7_200_000L;    // To level 4
            case 4 -> 9_700_000L;    // To level 5
            case 5 -> 13_000_000L;   // To level 6
            case 6 -> 18_000_000L;   // To level 7
            case 7 -> 24_000_000L;   // To level 8
            case 8 -> 32_000_000L;   // To level 9
            case 9 -> 43_000_000L;   // To level 10
            case 10 -> 59_000_000L;  // To level 11
            case 11 -> 79_000_000L;  // To level 12
            case 12 -> 110_000_000L; // To level 13
            case 13 -> 140_000_000L; // To level 14
            case 14 -> 190_000_000L; // To level 15
            case 15 -> 260_000_000L; // To level 16
            case 16 -> 350_000_000L; // To level 17
            case 17 -> 480_000_000L; // To level 18
            case 18 -> 650_000_000L; // To level 19
            case 19 -> 870_000_000L; // To level 20
            default -> 0L;           // Already maxed
        };
    }

    public static int getEmployeeIndex(String employeeName) {
        return getEmployeeType(employeeName).getIndex();
    }

    /**
     * Gets the cost to hire/upgrade an employee at a specific level
     * For Rabbit Bro levels 1-10: (30 + 20 × CF) × multiplier
     * For all other cases: base_cost × 1.05^L where base_cost = (216 + 144 × CF) × i²
     */
    public static long getEmployeeCost(String employeeName, int targetLevel, int chocolateFactoryLevel) {
        EmployeeType employeeType = getEmployeeType(employeeName);
        int employeeIndex = employeeType.getIndex();

        if (RABBIT_BRO_NAME.equals(employeeName) && targetLevel <= RABBIT_BRO_SPECIAL_MAX_LEVEL) {
            double multiplier = getRabbitBroMultiplier(targetLevel);
            return (long) ((RABBIT_BRO_BASE_COST + (long) RABBIT_BRO_CF_SCALING * chocolateFactoryLevel) * multiplier);
        }

        double baseCost = (EMPLOYEE_BASE_COST + EMPLOYEE_CF_SCALING * chocolateFactoryLevel) * (employeeIndex * employeeIndex);
        return (long) (baseCost * Math.pow(EMPLOYEE_COST_GROWTH, targetLevel));
    }

    /**
     * Gets the cost to hire/upgrade an employee (uses player's CF level)
     */
    public static long getEmployeeCost(SkyBlockPlayer player, String employeeName, int targetLevel) {
        int cfLevel = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.CHOCOLATE_FACTORY, DatapointChocolateFactory.class).getValue().getPrestigeLevel() + 1; // Prestige 0 = CF1, etc.
        return getEmployeeCost(employeeName, targetLevel, cfLevel);
    }

    /**
     * Legacy method - assumes CF level 1
     */
    public static long getEmployeeCost(String employeeName, int targetLevel) {
        return getEmployeeCost(employeeName, targetLevel, 1);
    }

    /**
     * Gets the multiplier for Rabbit Bro's first 10 levels
     */
    private static double getRabbitBroMultiplier(int level) {
        return switch (level) {
            case 1 -> 1;
            case 2 -> 2;
            case 3 -> 4;
            case 4 -> 6;
            case 5 -> 8;
            case 6 -> 9;
            case 7 -> 9.5;
            case 8 -> 10;
            case 9 -> 10.5;
            default -> 11;
        };
    }

    /**
     * Gets the base production per level for an employee type
     * This is the chocolate per second gained per employee level
     */
    public static double getEmployeeBaseProduction(String employeeName) {
        return getEmployeeType(employeeName).getBaseProductionPerLevel();
    }

    /**
     * Gets the employee that must be at level 20 to unlock the given employee
     * Returns null if no prerequisite (Rabbit Bro)
     */
    public static String getEmployeePrerequisite(String employeeName) {
        EmployeeType employeeType = getEmployeeType(employeeName);
        return employeeType == EmployeeType.RABBIT_BRO ? null : EmployeeType.values()[employeeType.ordinal() - 1].getName();
    }

    /**
     * Checks if an employee is unlocked for a player
     */
    public static boolean isEmployeeUnlocked(SkyBlockPlayer player, String employeeName) {
        EmployeeType employeeType = getEmployeeType(employeeName);
        if (employeeType == EmployeeType.RABBIT_BRO) return true;

        DatapointChocolateFactory.ChocolateFactoryData data = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.CHOCOLATE_FACTORY, DatapointChocolateFactory.class).getValue();
        DatapointChocolateFactory.EmployeeData prereqEmployee = data.getEmployees().get(EmployeeType.values()[employeeType.ordinal() - 1]);
        return prereqEmployee != null && prereqEmployee.getLevel() >= 20;
    }

    /**
     * Tries to purchase an upgrade
     *
     * @return true if purchase was successful
     */
    public static boolean purchaseUpgrade(SkyBlockPlayer player, UpgradeType type) {
        DatapointChocolateFactory datapoint = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.CHOCOLATE_FACTORY, DatapointChocolateFactory.class);
        DatapointChocolateFactory.ChocolateFactoryData data = datapoint.getValue();

        long cost = switch (type) {
            case HAND_BAKED_CHOCOLATE -> getHandBakedChocolateCost(data.getHandBakedChocolateLevel());
            case RABBIT_BARN -> getRabbitBarnCost(data.getRabbitBarnLevel());
            case RABBIT_SHRINE -> getRabbitShrineCost(data.getRabbitShrineLevel());
            case TIME_TOWER -> getTimeTowerCost(data.getTimeTowerLevel(), data.getPrestigeLevel());
            case COACH_JACKRABBIT -> getCoachJackrabbitCost(data.getCoachJackrabbitLevel());
        };

        if (data.removeChocolate(cost)) {
            switch (type) {
                case HAND_BAKED_CHOCOLATE -> data.setHandBakedChocolateLevel(data.getHandBakedChocolateLevel() + 1);
                case RABBIT_BARN -> data.setRabbitBarnLevel(data.getRabbitBarnLevel() + 1);
                case RABBIT_SHRINE -> data.setRabbitShrineLevel(data.getRabbitShrineLevel() + 1);
                case TIME_TOWER -> data.setTimeTowerLevel(data.getTimeTowerLevel() + 1);
                case COACH_JACKRABBIT -> data.setCoachJackrabbitLevel(data.getCoachJackrabbitLevel() + 1);
            }
            datapoint.setValue(data);
            return true;
        }
        return false;
    }

    /**
     * Tries to hire or upgrade an employee
     *
     * @return true if purchase was successful
     */
    public static boolean hireOrUpgradeEmployee(SkyBlockPlayer player, String employeeName) {
        DatapointChocolateFactory datapoint = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.CHOCOLATE_FACTORY, DatapointChocolateFactory.class);
        DatapointChocolateFactory.ChocolateFactoryData data = datapoint.getValue();

        EmployeeType employeeType = getEmployeeType(employeeName);
        DatapointChocolateFactory.EmployeeData existing = data.getEmployees().get(employeeType);
        int targetLevel = existing != null ? existing.getLevel() + 1 : 1;

        // Check if employee is unlocked (prerequisite employee at level 20)
        if (!isEmployeeUnlocked(player, employeeName)) {
            return false;
        }

        long cost = getEmployeeCost(player, employeeName, targetLevel);

        if (data.removeChocolate(cost)) {
            data.setEmployee(employeeType, targetLevel);
            datapoint.setValue(data);
            return true;
        }
        return false;
    }

    /**
     * Activates the Time Tower for the player
     *
     * @return true if activation was successful
     */
    public static boolean activateTimeTower(SkyBlockPlayer player) {
        DatapointChocolateFactory datapoint = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.CHOCOLATE_FACTORY, DatapointChocolateFactory.class);
        DatapointChocolateFactory.ChocolateFactoryData data = datapoint.getValue();

        if (data.activateTimeTower()) {
            datapoint.setValue(data);
            return true;
        }
        return false;
    }

    /**
     * Adds a Time Tower charge to the player
     */
    public static void addTimeTowerCharge(SkyBlockPlayer player) {
        mutateData(player, data -> data.setTimeTowerCharges(data.getTimeTowerCharges() + 1));
    }

    /**
     * Gets the prestige rank name based on level
     */
    public static String getPrestigeRankName(int level) {
        return Prestige.fromLevel(level).getName();
    }

    /**
     * Gets the prestige rank color based on level
     */
    public static String getPrestigeRankColor(int level) {
        return Prestige.fromLevel(level).getColor();
    }

    /**
     * Gets the Prestige enum based on level
     */
    public static Prestige getPrestige(int level) {
        return Prestige.fromLevel(level);
    }

    private static void mutateData(SkyBlockPlayer player, Consumer<DatapointChocolateFactory.ChocolateFactoryData> mutator) {
        DatapointChocolateFactory datapoint = player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.CHOCOLATE_FACTORY, DatapointChocolateFactory.class);
        DatapointChocolateFactory.ChocolateFactoryData data = datapoint.getValue();
        mutator.accept(data);
        datapoint.setValue(data);
    }

    private static EmployeeType getEmployeeType(String employeeName) {
        return EmployeeType.fromName(employeeName)
                .orElseThrow(() -> new IllegalArgumentException("Unknown chocolate factory employee: " + employeeName));
    }

    @Getter
    @AllArgsConstructor
    public enum Prestige {
        NEWCOMER(0, "Newcomer", "<7>"),
        APPRENTICE(1, "Apprentice", "<a>"),
        WORKER(2, "Worker", "<9>"),
        JOURNEYMAN(3, "Journeyman", "<5>"),
        EXPERT(4, "Expert", "<6>"),
        MASTER(5, "Master", "<d>"),
        GRANDMASTER(6, "Grandmaster", "<b>");

        private final int level;
        private final String name;
        private final String color;

        public String getFormattedName() {
            return color + name;
        }

        public static Prestige fromLevel(int level) {
            for (Prestige prestige : values()) {
                if (prestige.level == level) {
                    return prestige;
                }
            }
            return NEWCOMER;
        }
    }

    public enum UpgradeType {
        HAND_BAKED_CHOCOLATE,
        RABBIT_BARN,
        RABBIT_SHRINE,
        TIME_TOWER,
        COACH_JACKRABBIT
    }
}
