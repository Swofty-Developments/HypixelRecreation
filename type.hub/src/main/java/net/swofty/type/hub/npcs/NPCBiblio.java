package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.hub.gui.GUIBiblio;

public class NPCBiblio extends HypixelNPC {

    public NPCBiblio() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<6><l>SkyBlock Wiki", "Biblio", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "vV2/3ouX5+qRmFZzCRTz7aAniDmjJjictDu0/9krwI3x3FJdyL2eG/5GpEE2SKCY7J/znjKXs9oWaRvspM2DW0KVRspVlJYtz8hagJjRXRBM2mzIjXTzzVXNi+aHCzz3AARxd+9cWYOcEKKqh6snYSX2uJNBVz9cgfelykktL+TccVk2cbdLg8VMkZe24rXKQioOEc0QTYM0v7LkLODEvuWRL439j6Sa4sfq4FPqJzPtRUgjexMQtoNkCQBJdG266rnzXJjsh8Mgx8NChozmtY3tMyi8c4s1mAE5YQlI5P3sWHU5kOuAq2zpftI6wkSeDdMoxIIbWk+MINsKH4zMfiXK2u0FxViU654CWEN68RcXOpjVGK8ca+1quTeyHI9wu7FCY0Jm1aCo4MxJNhdWD8IjvhdC1pzcHpca+t7xAE7/0alWpMk0SkQSbHp+OqmGju29FNcguYsPQzAHKUB37XBANMqPKbrPdbKAL687kQy1FBri00V2Z83iYsq2vquUo1oqwzQ2UVY6ZrCamcr8ATvGpDMXJt4DSkUhHk3gP4lny3gL30n5dXyNxiJTn8UPqkDgBRRgAAKxYnhlzwku10/3gMT9N45ZWp/SokilGah5lG/0ZHH57EyTevOSD/q2i3y4qnHi2LU19HTvKDopwXh8mIn13O46zgh55izaVqg=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1NzU4Mzk4MDQwMTQsInByb2ZpbGVJZCI6IjkxOGEwMjk1NTlkZDRjZTZiMTZmN2E1ZDUzZWZiNDEyIiwicHJvZmlsZU5hbWUiOiJCZWV2ZWxvcGVyIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS83MDE5Mjg5MDU1ODkwYzEyMTFhY2QzNzI1YTcwZjA1MTU1ZTZlYzE2ZWE4MzI2YzM3ZjQxYjIxNjEyNjYxZGI4In19fQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(8.500, 79.000, 10.500, 75, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        new GUIBiblio().open(e.player());
    }

}
