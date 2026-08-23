package net.swofty.type.galatea.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCRosemary extends HypixelNPC {

    public NPCRosemary() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<a>Rosemary", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "ujENjLgmVYnQF2SF9F1xIx5XeCsU021iHBrcUZ6Av+PWHCrRtDba89Q662Jgl//ZsH6CvbA5MYVg0LvRdaXS0m10uTRt2WJvwPnd5+D/fJNfmdx1pjOTGa991JzKdNXHIDnfOtuWVXbX4diFJ319mHJWZPGJvNMqWIiXMPUqLZxmokI9i0ydMPMCMQZ0zKTyctTLxhOM+sczitiWu55LiQ5E6U4V2VJTsHR5NzMz1vMWbsRdAG0SkUePPrJqYRS5oKK2otjc1HxLk9I74/jnnnZdAkY8w5xe2eksrtv/xdVVxsIgyIzydt9dD8oxTYXtMyRpNOl6OIjhgv0nZ57dMqUCO7p0qNLYMaOdElYZKTCLKbe8uJarrNA5IeAazl+94ytT37s5o8EO1s0RECZRWEy+UiTKwfbMsOx38CrbY/KovR1vl1I8lZ2lTxBH9Y7hAWzsmMKMo+xCxi7hraWaWDqGFyEddJ9BTpwHYLI2OR5HA4fEYnH2NTAN19Bz2Ki0uEtfp+yUZoqsB1s4+2ZfeYm1yVxuVl8w3POWma9WzHGLIJ3+fW6GyilRKso1EjTloWWOdA/dg5ofpp3iFqjYTRcvKgm+ZYu0o4qrmJGY3C/NxbTrSXfxZO44F+zZGyWOeKcQ6ELDZqgi/EMREhPxS2zL1CDVsSqbWucKBiUStLI=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTY0NzU5ODczMTYzOSwKICAicHJvZmlsZUlkIiA6ICI5ZDQyNWFiOGFmZjg0MGU1OWM3NzUzZjc5Mjg5YjMyZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJUb21wa2luNDIiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTkwNDNkM2QzMWM1ZTc2ZmRhNzk0OTNmZDEzZTZmOGYzMTYxYTFhM2Q4ZWM3NGM2NTc5MmYwMzQwNmEzYTFhMyIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-703.5, 162, 3.5, 0, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        event.player().notImplemented();
    }
}
