package net.swofty.type.theend.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.function.Function;

public class EndNpcConfiguration extends HumanConfiguration {
    private static final String TEXTURE = "eyJ0aW1lc3RhbXAiOjE1NTk1OTMyMDkzMzIsInByb2ZpbGVJZCI6ImEyZjgzNDU5NWM4OTRhMjdhZGQzMDQ5NzE2Y2E5MTBjIiwicHJvZmlsZU5hbWUiOiJiUHVuY2giLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzM3Y2EwZmYxNzkzNmJlMzNlYWEzMTc2MjRkYjQ3NDRmYzNmMjkyZTYxMjM1Nzg1ODQ1MzI1OGU0ZjllN2NhNzkifX19";
    private static final String SIGNATURE = "LDlv2TZxU3LQIvO122hiA5sgcOr6q1uMlzpFY4g3l29+O6sjGJdJlL+IWqpSz5raBg3duW+kky8YhJgBKPsc5cLZPj9EqbSgF82URLIXaCdCEtouZNnqVBn6hb4ToserxU6O19LjyLDtO7IqzbfL07srAjWtV729awUUkhjcQa6QNeJW6d6EE37stRIlPcatkDuISB0IR7oZTmDtS3YNqNjSj7aqFPCuqKUJnExZ5OO1Mlgu0urP6AvMuS+6Uwr22ZwnW83yd55XCRMxVxN7Ehwal99wsF+EIOPcoUa0yG1hvlgoJuzjQwKpjLFQ0l9TnoAP6Gey4bn1fKyacFLARMZbuzzyn6ByUwCbuiqAbNJEx9MIOohJVQ1UR76StHpyVmywtx4q7CWucrMuwwh/315qIYB+wzxiycWLLBXVX4xdc1q6mn/K1loC787hAAconAI5s8jmK0tgfYXAnRON+oc2VWRwNtf1D2bJWTZVrsNpwCDEcAd7FuSlN2dQASEdByfF+aAc5T5zlXjZ4dtf5LZnQLqZYOznRFsaFIsWOcWbZn/qwvw975CXwY1a5GIPug29ocyiAEvQWd6jn0DhdxTW9myxFhLlhtwBXcWXM8HDhixJuy2YV99tQk32gk0HPA3sy+2SZDQZPRhDQFCf9fqdbQX/44lTnvtH0vt804g=";

    private final String name;
    private final Function<HypixelPlayer, Pos> position;
    private final Function<HypixelPlayer, Boolean> visible;

    public EndNpcConfiguration(String name, Pos position) {
        this(name, ignored -> position, ignored -> true);
    }

    public EndNpcConfiguration(String name, Function<HypixelPlayer, Pos> position) {
        this(name, position, ignored -> true);
    }

    public EndNpcConfiguration(String name, Pos position, Function<HypixelPlayer, Boolean> visible) {
        this(name, ignored -> position, visible);
    }

    public EndNpcConfiguration(String name, Function<HypixelPlayer, Pos> position,
                               Function<HypixelPlayer, Boolean> visible) {
        this.name = name;
        this.position = position;
        this.visible = visible;
    }

    @Override
    public String[] holograms(HypixelPlayer player) {
        return new String[]{"§d" + name, "§e§lCLICK"};
    }

    @Override
    public String signature(HypixelPlayer player) {
        return SIGNATURE;
    }

    @Override
    public String texture(HypixelPlayer player) {
        return TEXTURE;
    }

    @Override
    public Pos position(HypixelPlayer player) {
        return position.apply(player);
    }

    @Override
    public boolean looking(HypixelPlayer player) {
        return true;
    }

    @Override
    public boolean visible(HypixelPlayer player) {
        return visible.apply(player);
    }

    @Override
    public String chatName(HypixelPlayer player) {
        return "§d" + name;
    }
}
