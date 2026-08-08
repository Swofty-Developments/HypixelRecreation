package net.swofty.commons.replay.protocol;

public enum ReplaySection {
    SNAPSHOT(1),
    DELTA(2),
    EVENT(3);

    private final int id;

    ReplaySection(int id) {
        this.id = id;
    }

    public int id() {
        return id;
    }

    public static ReplaySection fromId(int id) {
        for (ReplaySection section : values()) {
            if (section.id == id) {
                return section;
            }
        }
        throw new IllegalArgumentException("Unknown replay section: " + id);
    }
}
