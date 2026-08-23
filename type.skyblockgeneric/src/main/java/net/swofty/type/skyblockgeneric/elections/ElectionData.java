package net.swofty.type.skyblockgeneric.elections;

import lombok.Getter;
import lombok.Setter;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

@Getter
@Setter
public class ElectionData {
    private static final List<NamedTextColor> CANDIDATE_COLORS = List.of(
            NamedTextColor.RED, NamedTextColor.GREEN, NamedTextColor.AQUA,
            NamedTextColor.YELLOW, NamedTextColor.LIGHT_PURPLE);

    private int electionYear;

    private String currentMayor;
    private String currentMayorColor;
    private List<String> currentMayorPerks = new ArrayList<>();
    private int mayorElectedYear;

    private String currentMinister;
    private String currentMinisterColor;
    private String ministerPerk;

    private List<CandidateData> candidates = new ArrayList<>();
    private Map<String, Long> voteTallies = new HashMap<>();
    private boolean electionOpen;

    private Map<String, Integer> lastElectedYear = new HashMap<>();
    private Map<String, List<String>> candidateActivePerks = new HashMap<>();
    private Map<String, Boolean> failedPerkGainLastTime = new HashMap<>();
    private int specialCandidateIndex;
    private ElectionResult lastElectionResult;

    public ElectionData() {}

    public SkyBlockMayor getMayorEnum() {
        if (currentMayor == null) return null;
        try {
            return SkyBlockMayor.valueOf(currentMayor);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public SkyBlockMayor getMinisterEnum() {
        if (currentMinister == null) return null;
        try {
            return SkyBlockMayor.valueOf(currentMinister);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public SkyBlockMayor.Perk getMinisterPerkEnum() {
        if (ministerPerk == null) return null;
        try {
            return SkyBlockMayor.Perk.valueOf(ministerPerk);
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    public List<SkyBlockMayor.Perk> getCurrentMayorPerkEnums() {
        List<SkyBlockMayor.Perk> result = new ArrayList<>();
        for (String perkName : currentMayorPerks) {
            try {
                result.add(SkyBlockMayor.Perk.valueOf(perkName));
            } catch (IllegalArgumentException ignored) {}
        }
        return result;
    }

    public Map<String, Long> tallyVotes() {
        return new HashMap<>(voteTallies);
    }

    public void updateTallies(Map<String, Long> newTallies) {
        voteTallies.clear();
        voteTallies.putAll(newTallies);
    }

    public void startNewElection(int year) {
        this.electionYear = year;
        this.electionOpen = true;
        this.voteTallies.clear();
        this.candidates.clear();

        boolean isSpecialYear = (year % 8 == 0);

        List<SkyBlockMayor> regularPool = new ArrayList<>(SkyBlockMayor.getRegularMayors());
        if (currentMayor != null) {
            regularPool.removeIf(m -> m.name().equals(currentMayor));
        }

        Collections.shuffle(regularPool);

        int regularCount = isSpecialYear ? 4 : 5;
        List<SkyBlockMayor> selected = new ArrayList<>(regularPool.subList(0, Math.min(regularCount, regularPool.size())));

        for (SkyBlockMayor mayor : selected) {
            CandidateData candidate = new CandidateData();
            candidate.setMayorName(mayor.name());
            candidate.setIndex(selected.indexOf(mayor));

            List<String> perks = candidateActivePerks.get(mayor.name());
            if (perks == null || perks.isEmpty()) {
                SkyBlockMayor.Perk[] allPerks = mayor.getAllPerks();
                candidate.setActivePerks(List.of(allPerks[ThreadLocalRandom.current().nextInt(allPerks.length)].name()));
            } else {
                candidate.setActivePerks(new ArrayList<>(perks));
            }
            candidates.add(candidate);
        }

        if (isSpecialYear) {
            List<SkyBlockMayor> specials = SkyBlockMayor.getSpecialMayors();
            SkyBlockMayor specialCandidate = specials.get(specialCandidateIndex % specials.size());
            specialCandidateIndex++;

            CandidateData candidate = new CandidateData();
            candidate.setMayorName(specialCandidate.name());
            candidate.setIndex(candidates.size());
            candidate.setActivePerks(Arrays.stream(specialCandidate.getAllPerks()).map(Enum::name).toList());
            candidates.add(candidate);
        }

        boolean diazLongTerm = currentMinister != null
                && currentMinister.equals(SkyBlockMayor.DIAZ.name())
                && ministerPerk != null
                && ministerPerk.equals(SkyBlockMayor.Perk.LONG_TERM_INVESTMENT.name());

        if (diazLongTerm) {
            boolean alreadyInElection = candidates.stream()
                    .anyMatch(c -> c.getMayorName().equals(SkyBlockMayor.DIAZ.name()));
            if (!alreadyInElection) {
                CandidateData diazCandidate = new CandidateData();
                diazCandidate.setMayorName(SkyBlockMayor.DIAZ.name());
                diazCandidate.setIndex(candidates.size());
                diazCandidate.setActivePerks(Arrays.stream(SkyBlockMayor.DIAZ.getAllPerks()).map(Enum::name).toList());
                candidates.add(diazCandidate);
            }
        }
    }

    public int getYearsSinceLastElected(String mayorName, int currentYear) {
        Integer lastYear = lastElectedYear.get(mayorName);
        if (lastYear == null) return -1;
        return currentYear - lastYear;
    }

    public static TextColor colorForIndex(int index) {
        return index >= 0 && index < CANDIDATE_COLORS.size() ? CANDIDATE_COLORS.get(index) : NamedTextColor.WHITE;
    }

    public static String wireColorForIndex(int index) {
        return Text.colorTag(colorForIndex(index));
    }

    public static Text colored(String color, String value) {
        return color.indexOf('§') >= 0 ? Text.legacy(color + value) : Text.of(color + "{}", value);
    }

    @Getter
    @Setter
    public static class CandidateData {
        private int index;
        private String mayorName;
        private List<String> activePerks = new ArrayList<>();

        public CandidateData() {}

        public SkyBlockMayor getMayorEnum() {
            try {
                return SkyBlockMayor.valueOf(mayorName);
            } catch (IllegalArgumentException e) {
                return null;
            }
        }

        public List<SkyBlockMayor.Perk> getActivePerkEnums() {
            List<SkyBlockMayor.Perk> result = new ArrayList<>();
            for (String perkName : activePerks) {
                try {
                    result.add(SkyBlockMayor.Perk.valueOf(perkName));
                } catch (IllegalArgumentException ignored) {}
            }
            return result;
        }

        public TextColor getColor() {
            return colorForIndex(index);
        }

        public Text getColoredName() {
            return Text.of("<color:{}>{}", getColor(), StringUtility.capitalize(getMayorName()));
        }

        public boolean hasMinisterPerkMarker(SkyBlockMayor.Perk perk) {
            return activePerks.contains(perk.name());
        }
    }

    @Getter
    @Setter
    public static class ElectionResult {
        private int year;
        private List<CandidateResult> candidateResults = new ArrayList<>();

        public ElectionResult() {}
    }

    @Getter
    @Setter
    public static class CandidateResult {
        private String mayorName;
        private long votes;
        private double percentage;

        public CandidateResult() {}
    }
}
