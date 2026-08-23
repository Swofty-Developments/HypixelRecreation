package net.swofty.type.skyblockgeneric.calendar;

import lombok.Getter;
import lombok.Setter;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.commands.MinionGenerationCommand;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class SkyBlockCalendar {
    private static final String DAY_SYMBOL = "☀";
    private static final String NIGHT_SYMBOL = "☽";

    public static final int YEAR = 8928000;
    public static final int MONTH = 744000;
    public static final int DAY = 24000;
    public static final int HOUR = 1000;

    public static final int INTEREST_INTERVAL = 31;
    public static final double INTEREST_RATE = 0.02;

    private static final List<String> MONTH_NAMES = Arrays.asList("Early Spring", "Spring",
            "Late Spring", "Early Summer", "Summer", "Late Summer", "Early Autumn",
            "Autumn", "Late Autumn", "Early Winter", "Winter", "Late Winter");

    private static final Set<String> firedEvents = new HashSet<>();

    @Getter
    @Setter
    private static long elapsed = 0L;

    public static void tick(Scheduler scheduler) {
        scheduler.submitTask(() -> {
            elapsed += 10L;
            checkForEvents(getElapsed() % YEAR);
            return TaskSchedule.tick(10);
        });
    }

    public static Integer getHoursUntilNextInterest() {
        // Interest happens every 31 hours
        return (int) (INTEREST_INTERVAL - (getElapsed() % DAY) / 1000);
    }

    public static Long getDifferenceInHours(long time) {
        return (getElapsed() - time) / HOUR;
    }

    public static int getYear() {
        return (int) (getElapsed() / YEAR) + 1;
    }

    public static int getMonth() {
        return ((int) (getElapsed() / MONTH) % 12) + 1;
    }

    public static int getMonth(long elapsed) {
        return ((int) (elapsed / MONTH) % 12) + 1;
    }

    public static int getDay() {
        return ((int) (getElapsed() / DAY) % 31) + 1;
    }

    public static int getHour() {
        int currentTime = (int) ((getElapsed() % DAY) - 6000);
        if (currentTime < 0) currentTime += DAY; // Adjust if before 6:00AM
        int hours = 6 + (currentTime / 1000); // 1 hour = 1000 units in elapsed time
        if (hours >= 24) hours -= 24; // Adjust if the time is past midnight
        return hours;
    }

    public static String getMonthName(int month) {
        if (month < 1 || month > 12)
            return "Unknown Month";
        return MONTH_NAMES.get(month - 1);
    }

    public static String getMonthName() {
        return getMonthName(getMonth());
    }

    public static void checkForEvents(Long time) {
        List<CalendarEvent> eventsAtTime = CalendarEvent.getCurrentEvents(time);
        int year = getYear();
        Set<String> stillRunning = new HashSet<>(eventsAtTime.size());

        for (CalendarEvent event : eventsAtTime) {
            stillRunning.add(event.id());
            if (firedEvents.add(event.id())) {
                event.action().accept(time, year);
            }
        }

        firedEvents.retainAll(stillRunning);
    }

    public static List<CalendarEvent> getCurrentEvents() {
        return CalendarEvent.getCurrentEvents(getElapsed() % YEAR);
    }

    /**
     * A record to hold information about upcoming events.
     * @param timeUntilBegin <b>ticks</b> until the event begins
     * @param duration duration of the event
     * @param year the year the event will occur
     */
    public record EventInfo(long timeUntilBegin, Duration duration, int year) {}

    public static Map<EventInfo, CalendarEvent> getEventsWithDurationUntil(int amount) {
        Map<EventInfo, CalendarEvent> result = new LinkedHashMap<>();
        long currentElapsed = getElapsed();
        int currentYear = getYear();

        int foundEvents = 0;
        int yearsAhead = 0;

        while (foundEvents < amount) {
            int targetYear = currentYear + yearsAhead;
            long yearStartElapsed = (long) (targetYear - 1) * YEAR;

            for (CalendarEvent event : CalendarEvent.getAllEvents()) {
                if (foundEvents >= amount) break;

                for (Long eventTime : event.times()) {
                    if (foundEvents >= amount) break;

                    long eventElapsed = yearStartElapsed + eventTime;

                    // Skip events that have already passed (including currently ongoing ones that started)
                    if (eventElapsed <= currentElapsed) {
                        continue;
                    }

                    long timeUntilBegin = eventElapsed - currentElapsed;

                    EventInfo info = new EventInfo(timeUntilBegin, event.duration(), targetYear);
                    result.put(info, event);
                    foundEvents++;
                }
            }
            yearsAhead++;

            // Safety check to prevent infinite loops
            if (yearsAhead > 100) break;
        }

        return result;
    }

    public static long ticksUntilEvent(CalendarEvent event) {
        long currentElapsed = getElapsed();
        int currentYear = getYear();

        for (int yearsAhead = 0; yearsAhead <= 100; yearsAhead++) {
            int targetYear = currentYear + yearsAhead;
            long yearStartElapsed = (long) (targetYear - 1) * YEAR;

            List<Long> sortedTimes = new ArrayList<>(event.times());
            Collections.sort(sortedTimes);

            for (Long eventTime : sortedTimes) {
                long eventElapsed = yearStartElapsed + eventTime;

                if (eventElapsed > currentElapsed) {
                    return eventElapsed - currentElapsed;
                }
            }
        }

        return -1;
    }

    public static Map<EventInfo, CalendarEvent> getEventsWithDurationUntilSkipSpecific(int amount, List<CalendarEvent> event) {
        Map<EventInfo, CalendarEvent> result = new LinkedHashMap<>();
        long currentElapsed = getElapsed();
        int currentYear = getYear();

        int foundEvents = 0;
        int yearsAhead = 0;

        while (foundEvents < amount) {
            int targetYear = currentYear + yearsAhead;
            long yearStartElapsed = (long) (targetYear - 1) * YEAR;

            for (CalendarEvent e : CalendarEvent.getAllEvents()) {
                if (event.contains(e)) continue; // Skip specified events
                if (foundEvents >= amount) break;

                for (Long eventTime : e.times()) {
                    if (foundEvents >= amount) break;

                    long eventElapsed = yearStartElapsed + eventTime;

                    // Skip events that have already passed (including currently ongoing ones that started)
                    if (eventElapsed <= currentElapsed) {
                        continue;
                    }

                    long timeUntilBegin = eventElapsed - currentElapsed;

                    EventInfo info = new EventInfo(timeUntilBegin, e.duration(), targetYear);
                    result.put(info, e);
                    foundEvents++;
                }
            }
            yearsAhead++;

            // Safety check to prevent infinite loops
            if (yearsAhead > 100) break;
        }

        return result;
    }

    public static String getMonthName(long elapsed) {
        return getMonthName((int) ((elapsed / MONTH) % 12) + 1);
    }

    public static int getDay(long elapsed) {
        return ((int) (elapsed / DAY) % 31) + 1;
    }

    public static Text getDisplay(long elapsed) {
        boolean isDaytime = true;
        int currentTime = (int) ((elapsed % DAY) - 6000);
        if (currentTime < 0) currentTime += DAY;
        int hours = 6 + (currentTime / 1000);
        int minutes = (currentTime % 1000) * 60 / 1000; // Corrected minutes calculation
        String formattedMinutes = String.format("%02d", minutes);
        if (hours >= 24) hours -= 24;
        if (hours < 6 || hours >= 20) isDaytime = false;

        String timePeriod = hours >= 12 ? "pm" : "am";
        hours = hours > 12 ? hours - 12 : (hours == 0 ? 12 : hours);

        Text symbol = isDaytime ? Text.of("<e>{}", DAY_SYMBOL) : Text.of("<b>{}", NIGHT_SYMBOL);

        return Text.of("{}:{}{} {}", hours, formattedMinutes, timePeriod, symbol)
                .appendIf(MinionGenerationCommand.divisionFactor != 1,
                        " <c>(Minion Speed: {})", MinionGenerationCommand.divisionFactor);
    }
}
