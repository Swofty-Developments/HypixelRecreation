package net.swofty.type.galatea.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCBrian extends HypixelNPC {

    public NPCBrian() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<3>Brian", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "WMwIZxxswxyvaQNLOE9y4OwBoaBPiy9wu8CZ9zmYfC8ZspOR7JJTTSJc4ujkHzlijTqC9tLmPKzFKX0gpQj+pDnr3Wm9ZdC/AYhvpAjy+msQmOUba5RU5qQgtI7NaYn88bWcpThyfILRZuHNfF1HXF/j1XkeV1xALV99Qe0EepthYytFu2tz5E0WvLn0q92037cwDjKDTQDp1sgRXnuHtpVIaorSAzOhdYQwPu5kJy7Ksd+VN/f+0mL5jyERfO9Nh4YJDn6JWomrHtq7vtGFUD7M8K/k2nJN2nk4nyE0yR/hVS/gUnOuAmop5ayQOOJCiSdjnT5cS+mLyotCb1xa6WZjK3AcB/1dJg0vtALxVZ5hdk/4bKpqccNR8uiP+kpW7aKF1LdpvQSshXlKMkrBCz2pvbgtOOy98OXnTXeF6cTYsu4pqaVQHmpILwCHx6fBxBaR7+nEr2AWIsNkt0iWnSWusKLzkw7fEeNiUnWlClB/LCO8/AOcdg3G6Zxn9vUKceG+1BcOOsJ9ObUL3ds4KQYkBUUzTfkBBJSZb/A6X0oBdSWpwmYirJEc5B45kx83Iz1ebS93FM8Iypmetd1lCr+gDrOe540HH5W1fLpnZTmIRtwx5788lFcpBVhwjJ1dXIwWI8Fr5DoY/3seBbdq0zLDmY33P3hNXly028PNqDQ=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTc0MTA5ODk1NjU1MSwKICAicHJvZmlsZUlkIiA6ICJlNzdlMDRhNmMyMzg0MGVmODZlOGViOWY4NGQ3OWVjNSIsCiAgInByb2ZpbGVOYW1lIiA6ICJMT1JJU0pUMTYiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMmNlNTI0ZDE0ZjZmYmU3NjRmODdmYjNhMjUxMDkzMzVlNDk0NjA2ZjlkZGQxMTkzZWIwNGIwMWJlOWQ5MWYxNyIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-628.5, 117, 47.5, -45, 0);
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
