package net.swofty.service.jacobscontest.endpoints;

import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.protocol.objects.jacobscontest.GetJacobContestScheduleProtocol;
import net.swofty.commons.redis.RedisMessageContext;
import net.swofty.commons.redis.RedisMessageHandler;
import net.swofty.service.jacobscontest.JacobsContestScheduler;

import java.util.List;

public class EndpointGetJacobContestSchedule implements RedisMessageHandler<
    GetJacobContestScheduleProtocol.GetJacobContestScheduleMessage,
    GetJacobContestScheduleProtocol.GetJacobContestScheduleResponse> {

    @Override
    public RedisProtocol<
        GetJacobContestScheduleProtocol.GetJacobContestScheduleMessage,
        GetJacobContestScheduleProtocol.GetJacobContestScheduleResponse> protocol() {
        return new GetJacobContestScheduleProtocol();
    }

    @Override
    public GetJacobContestScheduleProtocol.GetJacobContestScheduleResponse handle(
        GetJacobContestScheduleProtocol.GetJacobContestScheduleMessage body,
        RedisMessageContext context
    ) {
        int year = JacobsContestScheduler.getYear(body.calendarElapsed());
        List<GetJacobContestScheduleProtocol.ContestScheduleEntry> schedule = JacobsContestScheduler.generateYear(year);
        return new GetJacobContestScheduleProtocol.GetJacobContestScheduleResponse(
            year,
            JacobsContestScheduler.getActiveIndex(body.calendarElapsed(), schedule),
            JacobsContestScheduler.getUpcoming(body.calendarElapsed(), Math.max(1, body.upcomingCount()))
        );
    }
}
