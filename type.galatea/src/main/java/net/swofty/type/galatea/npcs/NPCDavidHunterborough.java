package net.swofty.type.galatea.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.galatea.gui.GUIDavid;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCDavidHunterborough extends HypixelNPC {

    public NPCDavidHunterborough() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>David Hunterborough", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "CWc7tWqcRck36iSMZdxhiLy+eZW3PGMhFUpl7iFwEPF3oh0gWyo8olFVhdh00pRIeDLPBZ+3rEUT7CidBrhrlEYhOA2+eLSVkArfG9PaBIMR381Ua5G8ZASGjA9kdJ4IH1vU/3jH4iDZ52xr9K119uWdYouyPVVy3zKZ51zPYqCpOpcjNf9JfSMBMvNHQ4wpCc0Pwea/NDl/eQjbdhhGt8fPwvaoBkXB9YBlOHluVfk0BVOV4BuUafmHQ/QZWfXYkMm7U0FDJIhSiiBjxid17NEIUZrOMfsxlQ9G6TKfNp9bKsFoyYnu7tUfLuzrqk5vRXMvcYf7ZTc3DexISOLfx4gu9jkHTMGsdRuri8QvZSN+KIqhJn52/pYIkRtootarB2o/fBYI8nr7BEw0Anm2iDZnbZj0Rkxpxcdl6DLyukp974x+7Ft6b3n/UA/XYBU5HUpdqwBCWfPIWPFPTdkxeFFupdwJjwfXngI/8iZ6n7YTRfrOc1DahB19nRGkMcIyN1vmj243OATtk8Xf/U7M9wzjEXM4ELuLs7XnqWhb03cApjyKDUmyctK8QF5jX8QDrTxQzZnk+nxNXQ8XBlfaO6D4escHp662MTQv2EwO/vP9Lx1Vr3xDnWQT94OW0RK9XQOeh3S41Zh872FU9RrNTL08+llLMkCAMQZkdxV1u3o=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTcxNzY1NTMyMDEyMCwKICAicHJvZmlsZUlkIiA6ICIwNTljODIxYzhhODU0NGJiOWJiODVhOGMxNjVhYTc5YiIsCiAgInByb2ZpbGVOYW1lIiA6ICJoZWxsc3RydWNrZWR6IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzYyYzFhMzY1ZGJhYmY5MDk4OTYyZDI3YmRhODkxOWE5NDI1M2M5MWNjYjZjMzUzZGE0ZjY0MDZmNDMxNmZmYWIiCiAgICB9CiAgfQp9";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-593.5, 114, -13.5, -101, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        event.player().openView(new GUIDavid());
    }
}
