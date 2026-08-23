package net.swofty.type.galatea.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCNemo extends HypixelNPC {

    public NPCNemo() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>Nemo", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "F3DkK6DdvlUDNA6GaVcGgS6QzbJ6alUayPhZmDKHkrr5TvnGVR2ucyw+gqI7MnIwEvXRVn0H/j5xgTxG/RacrfgXJ4AwFRt2UIPqDs9jVykoNypZqyRIa6nKwL7AF2H0hrKiiwmwgNYWysPS1qu+PflTvDHlGJaRWm62R/B/5YMQyYG2Vve9+MU5EDAnk7one7JVy/Efyo/tiBvF7FPbMhi+2gC+h5tlJP/Rfb2cGomSuYLE122GB+RUwB3UvRvRkBrOs49xAmZSbtvbvch5pUHFglus2vn8WGvUjefjDWX8tYjgOtZB8nZKBJ1hn6rhNJh9ky71SetZ1xoCJjCwAG2b5SJET7wf5hWfP5+a3uo/FA2hmEsB6/rRlacEsnMAOhNj7UUMV3IPbu51BHPhMg5BC64s8RYkTmzVKq/2Vr92dYKl+5R1PxGRtLA8U6y8aJC0Lj4iYbA5ZCoVzN0e+8sMZRYuT1YzOJu50VUV5oejXaxR0CTYhLxHVLWRaPI4lxOWj1uBcCWx0106rhos3KKFMtS7RbhGbQME04dcjc5HusU/81Tox5LODVwEEyhwB4A1FEebPjtMbDyx2qr/tNJmuEomlsTyw70I82vEoDqj22DXy5TSvEW1sJGkPm78Z5SJZ2q4LQMQOFnFyOj/GSRgjOoo6UnfodZSSRKC8TU=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTY2MDk2MDI3MDgwNSwKICAicHJvZmlsZUlkIiA6ICIwZjFjOTU0NDBmNjY0NjdkOGIzMzhiMzkwZjc3YjU2NCIsCiAgInByb2ZpbGVOYW1lIiA6ICJUYW5UYW5KaWFuZyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9hNDk1MjU4ZDhkN2RhNWEzODk0YTBjNGYxMjc1MTdhODZmM2JhMWMwNjNmZGViMGQ4YmNlZTBmYWY3NDEzYTEwIgogICAgfQogIH0KfQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-619.5, 87, -6.5, -45, 0);
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
