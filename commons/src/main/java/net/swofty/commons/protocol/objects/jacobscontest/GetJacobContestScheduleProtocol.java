package net.swofty.commons.protocol.objects.jacobscontest;

import net.swofty.commons.protocol.RedisProtocol;
import net.swofty.commons.protocol.Serializer;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GetJacobContestScheduleProtocol extends RedisProtocol<
    GetJacobContestScheduleProtocol.GetJacobContestScheduleMessage,
    GetJacobContestScheduleProtocol.GetJacobContestScheduleResponse> {

    @Override
    public Serializer<GetJacobContestScheduleMessage> getSerializer() {
        return new Serializer<>() {
            @Override
            public String serialize(GetJacobContestScheduleMessage value) {
                JSONObject json = new JSONObject();
                json.put("calendarElapsed", value.calendarElapsed);
                json.put("upcomingCount", value.upcomingCount);
                return json.toString();
            }

            @Override
            public GetJacobContestScheduleMessage deserialize(String json) {
                JSONObject jsonObject = new JSONObject(json);
                return new GetJacobContestScheduleMessage(
                    jsonObject.optLong("calendarElapsed", 0L),
                    jsonObject.optInt("upcomingCount", 3)
                );
            }

            @Override
            public GetJacobContestScheduleMessage clone(GetJacobContestScheduleMessage value) {
                return new GetJacobContestScheduleMessage(value.calendarElapsed, value.upcomingCount);
            }
        };
    }

    @Override
    public Serializer<GetJacobContestScheduleResponse> getReturnSerializer() {
        return new Serializer<>() {
            @Override
            public String serialize(GetJacobContestScheduleResponse value) {
                JSONObject json = new JSONObject();
                json.put("year", value.year);
                json.put("activeIndex", value.activeIndex);

                JSONArray contests = new JSONArray();
                for (ContestScheduleEntry entry : value.entries) {
                    JSONObject item = new JSONObject();
                    item.put("index", entry.index);
                    item.put("startTime", entry.startTime);
                    item.put("endTime", entry.endTime);
                    item.put("day", entry.day);
                    item.put("crops", entry.crops);
                    contests.put(item);
                }
                json.put("entries", contests);
                return json.toString();
            }

            @Override
            public GetJacobContestScheduleResponse deserialize(String json) {
                JSONObject jsonObject = new JSONObject(json);
                List<ContestScheduleEntry> entries = new ArrayList<>();
                JSONArray contests = jsonObject.optJSONArray("entries");
                if (contests != null) {
                    for (int i = 0; i < contests.length(); i++) {
                        JSONObject item = contests.getJSONObject(i);
                        JSONArray crops = item.optJSONArray("crops");
                        List<String> cropList = new ArrayList<>();
                        if (crops != null) {
                            for (int cropIndex = 0; cropIndex < crops.length(); cropIndex++) {
                                cropList.add(crops.getString(cropIndex));
                            }
                        }
                        entries.add(new ContestScheduleEntry(
                            item.optInt("index", 0),
                            item.optLong("startTime", 0L),
                            item.optLong("endTime", 0L),
                            item.optInt("day", 1),
                            cropList
                        ));
                    }
                }
                return new GetJacobContestScheduleResponse(
                    jsonObject.optInt("year", 1),
                    jsonObject.optInt("activeIndex", -1),
                    entries
                );
            }

            @Override
            public GetJacobContestScheduleResponse clone(GetJacobContestScheduleResponse value) {
                return new GetJacobContestScheduleResponse(value.year, value.activeIndex, List.copyOf(value.entries));
            }
        };
    }

    public record GetJacobContestScheduleMessage(long calendarElapsed, int upcomingCount) {
    }

    public record GetJacobContestScheduleResponse(int year, int activeIndex, List<ContestScheduleEntry> entries) {
    }

    public record ContestScheduleEntry(int index, long startTime, long endTime, int day, List<String> crops) {
    }
}
