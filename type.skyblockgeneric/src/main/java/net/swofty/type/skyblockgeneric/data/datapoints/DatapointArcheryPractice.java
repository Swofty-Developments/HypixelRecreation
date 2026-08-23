package net.swofty.type.skyblockgeneric.data.datapoints;

import com.mongodb.lang.Nullable;
import lombok.Getter;
import lombok.Setter;
import net.minestom.server.coordinate.Pos;
import net.swofty.commons.protocol.Serializer;
import net.swofty.type.generic.entity.hologram.PlayerHolograms;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.data.SkyBlockDatapoint;
import net.swofty.type.skyblockgeneric.targetpractice.PracticeTargets;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class DatapointArcheryPractice extends SkyBlockDatapoint<DatapointArcheryPractice.ArcheryPracticeData> {
    private static final Pos ARCHERY_PRACTICE_HOLOGRAM_POS = new Pos(-56.5, 62, -82.5);

    public static Pos getArcheryPosition() {
        return ARCHERY_PRACTICE_HOLOGRAM_POS;
    }

    public static Serializer<DatapointArcheryPractice.ArcheryPracticeData> serializer = new Serializer<>() {
        @Override
        public String serialize(ArcheryPracticeData value) {
            JSONObject obj = new JSONObject();
            obj.put("targetPracticeLevel", value.getTargetPracticeLevel().name());
            return obj.toString();
        }

        @Override
        public ArcheryPracticeData deserialize(String json) {
            JSONObject obj = new JSONObject(json);
            ArcheryPracticeData data = new ArcheryPracticeData();
            data.targetPracticeLevel = TargetPracticeLevels.valueOf(obj.getString("targetPracticeLevel"));
            return data;
        }

        @Override
        public ArcheryPracticeData clone(ArcheryPracticeData value) {
            return new ArcheryPracticeData() {{
                setTargetPracticeLevel(value.getTargetPracticeLevel());
            }};
        }
    };

    public DatapointArcheryPractice(String key, DatapointArcheryPractice.ArcheryPracticeData value) {
        super(key, value, serializer);
    }

    public DatapointArcheryPractice(String key) {
        super(key, new ArcheryPracticeData(), serializer);
    }

    @Setter
    @Getter
    public static class ArcheryPracticeData {
        private TargetPracticeLevels targetPracticeLevel;
        private PlayerHolograms.ExternalPlayerHologram hologram;
        private List<PracticeTargets> targetsHitList = new ArrayList<>();

        public ArcheryPracticeData() {
            this.targetPracticeLevel = TargetPracticeLevels.FIRST_LEVEL;
        }

        public void incrementTargetsHit(PracticeTargets target) {
            this.targetsHitList.add(target);
        }

        public boolean hasHitTarget(PracticeTargets target) {
            return this.targetsHitList.contains(target);
        }

        public void resetTargetsHit() {
            this.targetsHitList.clear();
        }

        public void initializeHologram(HypixelPlayer player) {
            this.hologram = PlayerHolograms.ExternalPlayerHologram.builder()
                    .player(player)
                    .pos(ARCHERY_PRACTICE_HOLOGRAM_POS)
                    .text(this.targetPracticeLevel.hologramText)
                    .build();
            PlayerHolograms.addExternalPlayerHologram(this.hologram);
        }

        public void incrementLevel(HypixelPlayer player) {
            TargetPracticeLevels nextLevel = this.targetPracticeLevel.getNextLevel();

            if (nextLevel == null) return;
            this.targetPracticeLevel = nextLevel;

            if (this.hologram != null) {
                PlayerHolograms.removeExternalPlayerHologram(hologram);
            }
            this.hologram = PlayerHolograms.ExternalPlayerHologram.builder()
                    .player(player)
                    .pos(ARCHERY_PRACTICE_HOLOGRAM_POS)
                    .text(nextLevel.hologramText)
                    .build();
            PlayerHolograms.addExternalPlayerHologram(this.hologram);
        }

        public boolean hasCompletedALevel() {
            return this.targetPracticeLevel != TargetPracticeLevels.FIRST_LEVEL;
        }
    }

    @Getter
    public enum TargetPracticeLevels {
        FIRST_LEVEL(new String[] {
                "<a>Target Practice I",
                "Shoot all targets in 25s",
                "<e><l>ACTIVATE TO START",
        }, 25),
        SECOND_LEVEL(new String[] {
                "<6>Target Practice II",
                "Shoot all targets in 15s",
                "<e><l>ACTIVATE TO START",
        }, 15),
        THIRD_LEVEL(new String[] {
                "<c>Target Practice III",
                "Shoot all targets in 12s",
                "<e><l>ACTIVATE TO START",
        }, 12),
        FOURTH_LEVEL(new String[] {
                "<4>Target Practice IV",
                "Shoot all targets in 11s",
                "<e><l>ACTIVATE TO START",
        }, 11),
        CONCLUDED(new String[] {
                "<c>You have completed all levels!",
        }, 0)
        ;

        private final String[] hologramText;
        private final int timeLimitSeconds;

        TargetPracticeLevels(String[] hologramText, int timeLimitSeconds) {
            this.hologramText = hologramText;
            this.timeLimitSeconds = timeLimitSeconds;
        }

        public @Nullable TargetPracticeLevels getNextLevel() {
            int nextOrdinal = this.ordinal() + 1;
            TargetPracticeLevels[] values = TargetPracticeLevels.values();
            if (nextOrdinal < values.length) {
                return values[nextOrdinal];
            }
            return null;
        }

        public int getLevelNumber() {
            return this.ordinal() + 1;
        }
    }
}
