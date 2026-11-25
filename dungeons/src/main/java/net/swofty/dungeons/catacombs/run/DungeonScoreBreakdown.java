package net.swofty.dungeons.catacombs.run;

public record DungeonScoreBreakdown(
        int skill,
        int exploration,
        int speed,
        int bonus
) {
    public int total() {
        return skill + exploration + speed + bonus;
    }

    public DungeonScoreRank rank() {
        return DungeonScoreRank.fromScore(total());
    }

    public int rngMeterProgress() {
        return switch (rank()) {
            case S_PLUS -> total();
            case S -> (int) Math.round(total() * 0.7D);
            default -> 0;
        };
    }
}
