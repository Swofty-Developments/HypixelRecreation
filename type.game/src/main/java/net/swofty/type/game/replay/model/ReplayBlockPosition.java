package net.swofty.type.game.replay.model;

public record ReplayBlockPosition(int x, int y, int z) implements Comparable<ReplayBlockPosition> {
    @Override
    public int compareTo(ReplayBlockPosition other) {
        int result = Integer.compare(x, other.x);
        if (result != 0) return result;
        result = Integer.compare(y, other.y);
        return result != 0 ? result : Integer.compare(z, other.z);
    }
}
