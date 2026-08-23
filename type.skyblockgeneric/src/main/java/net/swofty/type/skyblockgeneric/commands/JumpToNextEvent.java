package net.swofty.type.skyblockgeneric.commands;

import net.minestom.server.command.builder.arguments.ArgumentString;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.command.CommandParameters;
import net.swofty.type.generic.command.HypixelCommand;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.calendar.CalendarEvent;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;

@CommandParameters(labels = "jumptonextevent skiptoevent",
        description = "Jumps to the next SkyBlock calendar event",
        usage = "/jumptonextevent <eventName>",
        permission = Rank.STAFF,
        allowsConsole = true)
public class JumpToNextEvent extends HypixelCommand {
    @Override
    public void registerUsage(HypixelCommand.MinestomCommand command) {
        ArgumentString eventNameArg = new ArgumentString("eventName");
        eventNameArg.setSuggestionCallback((sender, context, suggestion) -> {
            for (CalendarEvent event : CalendarEvent.getAllEvents()) {
                suggest(suggestion, event.id(), event.getDisplayName(SkyBlockCalendar.getYear()));
            }
        });
        command.addSyntax((sender, context) -> {
            String eventName = context.get(eventNameArg);
            CalendarEvent event = CalendarEvent.fromId(eventName);
            if (event == null) {
                sender.sendMessage("<c>Invalid event name. Available events:");
                for (CalendarEvent e : CalendarEvent.getAllEvents()) {
                    sender.sendMessage(Text.of("<7>- {}", e.getDisplayName(SkyBlockCalendar.getYear())));
                }
                return;
            }

            long currentElapsed = SkyBlockCalendar.getElapsed();
            int currentYear = SkyBlockCalendar.getYear();
            long timeInYear = currentElapsed % SkyBlockCalendar.YEAR;

            Long nextEventTime = null;
            int targetYear = currentYear;

            for (Long eventTime : event.times()) {
                if (eventTime > timeInYear) {
                    nextEventTime = eventTime;
                    break;
                }
            }

            if (nextEventTime == null && !event.times().isEmpty()) {
                nextEventTime = event.times().getFirst();
                targetYear = currentYear + 1;
            }

            if (nextEventTime == null) {
                sender.sendMessage("<c>Could not find next occurrence of this event.");
                return;
            }

            long newElapsed = ((long) (targetYear - 1) * SkyBlockCalendar.YEAR) + nextEventTime;
            SkyBlockCalendar.setElapsed(newElapsed - 20);

            sender.sendMessage(Text.of("<a>Jumped to {}!", event.getDisplayName(targetYear)));
            sender.sendMessage(Text.of("<7>New date: {} {}, Year {}", SkyBlockCalendar.getMonthName(), SkyBlockCalendar.getDay(), SkyBlockCalendar.getYear()));

        }, eventNameArg);
    }
}
