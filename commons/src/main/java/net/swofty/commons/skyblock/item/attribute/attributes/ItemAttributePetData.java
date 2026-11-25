package net.swofty.commons.skyblock.item.attribute.attributes;

import lombok.Getter;
import lombok.Setter;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.item.attribute.ItemAttribute;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.List;

public class ItemAttributePetData extends ItemAttribute<ItemAttributePetData.PetData> {
    @Override
    public String getKey() {
        return "pet_data";
    }

    @Override
    public PetData getDefaultValue(@Nullable ItemStatistics defaultStatistics) {
        return new PetData(0, null, null);
    }

    @Override
    public PetData loadFromString(String string) {
        if (string == null || string.isEmpty() || string.equals("null")) {
            return new PetData(0, null, null);
        }

        String[] parts = string.split(":", -1);

        try {
            double experience = Double.parseDouble(parts[0]);
            ItemType skinId = parts[1].isBlank() ? null : ItemType.get(parts[1]);
            String skinVariant = parts[2].isBlank() ? null : parts[2];
            return new PetData(experience, skinId, skinVariant);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException e) {
            return new PetData(0, null, null);
        }
    }

    @Override
    public String saveIntoString() {
        if (getValue() == null) return "null";
        PetData data = getValue();
        String exp = String.valueOf(data.getExperience());
        String skinId = data.getSkinId() == null ? "" : data.getSkinId().name();
        String skinVariant = data.getSkinVariant() == null ? "" : data.getSkinVariant();
        return exp + ":" + skinId + ":" + skinVariant;
    }

    @Getter
    @Setter
    public static class PetData {
        private double experience;
        private ItemType skinId;
        private String skinVariant;

        public PetData(double experience, ItemType skinId, String skinVariant) {
            this.experience = experience;
            this.skinId = skinId;
            this.skinVariant = skinVariant;
        }

        // Returns true if the level was increased
        public boolean addExperience(double experience, Rarity rarity) {
            Integer currentLevel = getAsLevel(rarity);
            this.experience += experience;
            return getAsLevel(rarity) > currentLevel;
        }

        public void setLevel(int level, Rarity rarity) {
            this.experience = getExperienceForLevel(level, rarity);
        }

        public double getExperienceInCurrentLevel(Rarity rarity) {
            // Get the experience required to level up
            // Minus the last reached goal from the current experience
            // Return the difference
            double currentLevelGoal = getExperienceForLevel(getAsLevel(rarity), rarity);
            double nextLevelGoal = getExperienceForLevel(getAsLevel(rarity) + 1, rarity);
            return nextLevelGoal - (currentLevelGoal - getAsExperience(rarity));
        }

        public Integer getAsLevel(Rarity rarity) {
            return switch (rarity) {
                case COMMON -> getLevel(experience, COMMON_XP_GOALS);
                case UNCOMMON -> getLevel(experience, UNCOMMON_XP_GOALS);
                case RARE -> getLevel(experience, RARE_XP_GOALS);
                case EPIC -> getLevel(experience, EPIC_XP_GOALS);
                case LEGENDARY -> getLevel(experience, LEGENDARY_XP_GOALS);
                case MYTHIC -> getLevel(experience, MYTHIC_XP_GOALS);
                default -> 0;
            };
        }

        public double getAsExperience(Rarity rarity) {
            return switch (rarity) {
                case COMMON -> experience - COMMON_XP_GOALS.get(getAsLevel(Rarity.COMMON));
                case UNCOMMON -> experience - UNCOMMON_XP_GOALS.get(getAsLevel(Rarity.UNCOMMON));
                case RARE -> experience - RARE_XP_GOALS.get(getAsLevel(Rarity.RARE));
                case EPIC -> experience - EPIC_XP_GOALS.get(getAsLevel(Rarity.EPIC));
                case LEGENDARY -> experience - LEGENDARY_XP_GOALS.get(getAsLevel(Rarity.LEGENDARY));
                case MYTHIC -> experience - MYTHIC_XP_GOALS.get(getAsLevel(Rarity.MYTHIC));
                default -> 0;
            };
        }

        public long getExperienceForLevel(int level, Rarity rarity) {
            return switch (rarity) {
                case COMMON -> COMMON_XP_GOALS.get(level - 1);
                case UNCOMMON -> UNCOMMON_XP_GOALS.get(level - 1);
                case RARE -> RARE_XP_GOALS.get(level - 1);
                case EPIC -> EPIC_XP_GOALS.get(level - 1);
                case LEGENDARY -> LEGENDARY_XP_GOALS.get(level - 1);
                case MYTHIC -> MYTHIC_XP_GOALS.get(level - 1);
                default -> 0;
            };
        }

        private Integer getLevel(double xp, List<Integer> goals) {
            for (int i = 0; i < goals.size(); i++) {
                if (xp < goals.get(i)) {
                    return i;
                }
            }
            return goals.size();
        }
    }

    protected static final List<Integer> COMMON_XP_GOALS = Arrays.asList(0, 100, 210, 330, 460, 605, 765, 940, 1130, 1340, 1570, 1820, 2095, 2395, 2725, 3085, 3485, 3925, 4415,
            4955, 5555, 6215, 6945, 7745, 8625, 9585, 10635, 11785, 13045, 14425, 15935, 17585, 19385, 21345, 23475, 25785, 28285, 30985, 33905, 37065, 40485, 44185, 48185,
            52535, 57285, 62485, 68185, 74485, 81485, 89285, 97985, 107685, 118485, 130485, 143785, 158485, 174685, 192485, 211985, 233285, 256485, 281685, 309085, 338885,
            371285, 406485, 444685, 486085, 530885, 579285, 631485, 687685, 748085, 812885, 882285, 956485, 1035685, 1120385, 1211085, 1308285, 1412485, 1524185, 1643885,
            1772085, 1909285, 2055985, 2212685, 2380385, 2560085, 2752785, 2959485, 3181185, 3418885, 3673585, 3946285, 4237985, 4549685, 4883385, 5241085, 5624785);

    protected static final List<Integer> UNCOMMON_XP_GOALS = Arrays.asList(0, 175, 365, 575, 805, 1055, 1330, 1630, 1960, 2320, 2720, 3160, 3650, 4190, 4790, 5450,
            6180, 6980, 7860, 8820, 9870, 11020, 12280, 13660, 15170, 16820, 18620, 20580, 22710, 25020, 27520, 30220, 33140, 36300, 39720, 43420, 47420, 51770, 56520,
            61720, 67420, 73720, 80720, 88520, 97220, 106920, 117720, 129720, 143020, 157720, 173920, 191720, 211220, 232520, 255720, 280920, 308320, 338120, 370520,
            405720, 443920, 485320, 530120, 578520, 630720, 686920, 747320, 812120, 881520, 955720, 1034920, 1119620, 1210320, 1307520, 1411720, 1523420, 1643120, 1771320,
            1908520, 2055220, 2211920, 2379620, 2559320, 2752020, 2958720, 3180420, 3418120, 3672820, 3945520, 4237220, 4548920, 4882620, 5240320, 5624020, 6035720,
            6477420, 6954120, 7470820, 8032520, 8644220);

    protected static final List<Integer> RARE_XP_GOALS = Arrays.asList(0, 275, 575, 905, 1265, 1665, 2105, 2595, 3135, 3735, 4395, 5125, 5925, 6805, 7765, 8815, 9965,
            11225, 12605, 14115, 15765, 17565, 19525, 21655, 23965, 26465, 29165, 32085, 35245, 38665, 42365, 46365, 50715, 55465, 60665, 66365, 72665, 79665, 87465,
            96165, 105865, 116665, 128665, 141965, 156665, 172865, 190665, 210165, 231465, 254665, 279865, 307265, 337065, 369465, 404665, 442865, 484265, 529065, 577465,
            629665, 685865, 746265, 811065, 880465, 954665, 1033865, 1118565, 1209265, 1306465, 1410665, 1522365, 1642065, 1770265, 1907465, 2054165, 2210865, 2378565,
            2558265, 2750965, 2957665, 3179365, 3417065, 3671765, 3944465, 4236165, 4547865, 4881565, 5239265, 5622965, 6034665, 6476365, 6953065, 7469765, 8031465, 8643165,
            9309865, 10036565, 10828265, 11689965, 12626665);

    protected static final List<Integer> EPIC_XP_GOALS = Arrays.asList(0, 440, 930, 1470, 2070, 2730, 3460, 4260, 5140, 6100, 7150, 8300, 9560, 10940, 12450, 14100, 15900,
            17860, 19990, 22300, 24800, 27500, 30420, 33580, 37000, 40700, 44700, 49050, 53800, 59000, 64700, 71000, 78000, 85800, 94500, 104200, 115000, 127000, 140300,
            155000, 171200, 189000, 208500, 229800, 253000, 278200, 305600, 335400, 367800, 403000, 441200, 482600, 527400, 575800, 628000, 684200, 744600, 809400, 878800,
            953000, 1032200, 1116900, 1207600, 1304800, 1409000, 1520700, 1640400, 1768600, 1905800, 2052500, 2209200, 2376900, 2556600, 2749300, 2956000, 3177700, 3415400,
            3670100, 3942800, 4234500, 4546200, 4879900, 5237600, 5621300, 6033000, 6474700, 6951400, 7468100, 8029800, 8641500, 9308200, 10034900, 10826600, 11688300, 12625000,
            13641700, 14743400, 15935100, 17221800, 18608500);

    protected static final List<Integer> LEGENDARY_XP_GOALS = Arrays.asList(0, 660, 1390, 2190, 3070, 4030, 5080, 6230, 7490, 8870, 10380, 12030, 13830, 15790, 17920, 20230,
            22730, 25430, 28350, 31510, 34930, 38630, 42630, 46980, 51730, 56930, 62630, 68930, 75930, 83730, 92430, 102130, 112930, 124930, 138230, 152930, 169130, 186930,
            206430, 227730, 250930, 276130, 303530, 333330, 365730, 400930, 439130, 480530, 525330, 573730, 625930, 682130, 742530, 807330, 876730, 950930, 1030130, 1114830,
            1205530, 1302730, 1406930, 1518630, 1638330, 1766530, 1903730, 2050430, 2207130, 2374830, 2554530, 2747230, 2953930, 3175630, 3413330, 3668030, 3940730, 4232430,
            4544130, 4877830, 5235530, 5619230, 6030930, 6472630, 6949330, 7466030, 8027730, 8639430, 9306130, 10032830, 10824530, 11686230, 12622930, 13639630, 14741330,
            15933030, 17219730, 18606430, 20103130, 21719830, 23466530, 25353230);

    protected static final List<Integer> MYTHIC_XP_GOALS = LEGENDARY_XP_GOALS;
}
