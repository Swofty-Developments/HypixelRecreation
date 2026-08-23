package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCElise extends HypixelNPC {

    public NPCElise() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<d>Elise", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "NGzYYFH/0Q0n2ursf6R1Z7gW0B2SzNLhKy1o03FGgpfDXPlKvqoKicuoV7QMjFVjNbN9NZHbXdcmYhZ5u8fG2LXf7CZD1MkUQtGZ7X+dCWluqLe99SK5grRuuHl2AIsq/gHevsoNOQAbDLTsn9CFZcookPklb4sUlOeEYYQM85YZEyBaq5GB9T015dZW9FYjUDndMQ3UU8QkFvPBgztWoHDQs7+v5pbs5oGQKpw5W9JEwBoV4GJeDH8Rlym5rh91xNryhiOtvhkNF2aN4yT8GYypGu/DnRQZeVZzf40KATzMVjYTS7pgee3+BMlChwj9ylXH3m82fvrqxBhgS4+R5IJxc2WFUm5/EVC/cGQEB2vdZHtkyxP9dTz9A5hCggsfP2cMhcszlp+D++YJe/CM4ZEuRTdsBZlRrXaQVZSJevHCzteC2gOsbdkHOPacHQJaqMuS4fkmGBbQGBTZMoFtcXZALm5e6YwsW+uHBS3EX9pfKVG1Q9+jhFiMtpyzJPk2O/91hi1ph0zh6eG7QYHg6HVe+Pa1JLXzZtw4w/zBg60KqqIf0bayV9fWuSVnEeF2g+wpCCOJ6ldNNBFsnkalapRZ+PDyaa+1KXqypvcp7FAa6jPJ8xsqaZeumi1XcH82RMa6RPAA+8Cl7zRFMGBWES/XzmJgjwg+940G4PKK/lM=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYwMTI1NjQyMjU2MSwKICAicHJvZmlsZUlkIiA6ICJiMGQ0YjI4YmMxZDc0ODg5YWYwZTg2NjFjZWU5NmFhYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNaW5lU2tpbl9vcmciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWEyYWM2MTRlNTMzMzI4YzA3YTI1Y2ViY2IwMmE0MGQzYmM0MWMxY2MyYzI0YTY1MDY1ZjgzMGIwNTk1MmM0ZiIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(44.5, 77, 100.5, -180, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return false;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {

    }
}
