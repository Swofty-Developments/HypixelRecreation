package net.swofty.service.jacobscontest;

import net.swofty.commons.protocol.objects.jacobscontest.GetJacobContestScheduleProtocol;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Random;

public final class JacobsContestScheduler {
    private static final JacobsContestConfig CONFIG = JacobsContestConfig.load();

    private JacobsContestScheduler() {
    }

    public static List<GetJacobContestScheduleProtocol.ContestScheduleEntry> generateYear(int year) {
        List<GetJacobContestScheduleProtocol.ContestScheduleEntry> result = new ArrayList<>();
        int contestIndex = 0;
        long yearLength = CONFIG.getYearLength();
        long contestInterval = (long) CONFIG.getIntervalDays() * CONFIG.getDayLength();
        long contestDuration = (long) CONFIG.getDurationDays() * CONFIG.getDayLength();
        for (long start = 0; start < yearLength; start += contestInterval) {
            result.add(new GetJacobContestScheduleProtocol.ContestScheduleEntry(
                contestIndex,
                start,
                start + contestDuration,
                (int) (start / CONFIG.getDayLength()) + 1,
                pickCrops(year, contestIndex)
            ));
            contestIndex++;
        }
        return result;
    }

    public static int getYear(long calendarElapsed) {
        return (int) (calendarElapsed / CONFIG.getYearLength()) + 1;
    }

    public static int getActiveIndex(long calendarElapsed, List<GetJacobContestScheduleProtocol.ContestScheduleEntry> entries) {
        long elapsedInYear = calendarElapsed % CONFIG.getYearLength();
        for (GetJacobContestScheduleProtocol.ContestScheduleEntry entry : entries) {
            if (elapsedInYear >= entry.startTime() && elapsedInYear < entry.endTime()) {
                return entry.index();
            }
        }
        return -1;
    }

    public static List<GetJacobContestScheduleProtocol.ContestScheduleEntry> getUpcoming(
        long calendarElapsed,
        int requestedCount
    ) {
        int year = getYear(calendarElapsed);
        long elapsedInYear = calendarElapsed % CONFIG.getYearLength();
        List<GetJacobContestScheduleProtocol.ContestScheduleEntry> yearSchedule = generateYear(year);
        List<GetJacobContestScheduleProtocol.ContestScheduleEntry> result = new ArrayList<>();

        for (GetJacobContestScheduleProtocol.ContestScheduleEntry entry : yearSchedule) {
            if (entry.endTime() <= elapsedInYear) {
                continue;
            }
            result.add(entry);
            if (result.size() >= requestedCount) {
                return result;
            }
        }

        List<GetJacobContestScheduleProtocol.ContestScheduleEntry> nextYear = generateYear(year + 1);
        for (GetJacobContestScheduleProtocol.ContestScheduleEntry entry : nextYear) {
            result.add(entry);
            if (result.size() >= requestedCount) {
                break;
            }
        }
        return result;
    }

    private static List<String> pickCrops(int year, int contestIndex) {
        Random random = new Random(Objects.hash(year, contestIndex, "jacobs-contest"));
        List<String> pool = new ArrayList<>(CONFIG.getCrops());
        List<String> picks = new ArrayList<>(3);
        for (int i = 0; i < 3 && !pool.isEmpty(); i++) {
            picks.add(pool.remove(random.nextInt(pool.size())));
        }
        return picks;
    }
}
