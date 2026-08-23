package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCOzanne extends HypixelNPC {

    public NPCOzanne() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Ozanne", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "ydKwHzLhzyVnZDtJlsJyWCtueMAGT878Lb2KN9L0QkGo/uqUSOaZBurWERVg9SEIVhDNe4UULMkax6Lb8rpAv/X+0mtS+UH2XPyn/78vEJBK+cojJvDRUVUrJHAaK5G0TT2ea8sFXs1R1QNLrwSymQQcrnMJAs+Re9cwtwUSLK9kMZaTnsMiYXpJZ/caF6dZI7iZGU4jlyfGXp/Xbxbva8NxDfRc/69ox9vwBP1M/3E48aetjW6SLphoVw4Gjgo4n8QGOWFoYNXD7AVBqPrBVPjKjqBtQ3dgGyQCRBShoxN1uqfbLuMfMX46LvsuPwdUiX/NAlOZzkpJAXvH2DddzZTmDTwrsOoqiBtDyhZuI4wfhbzPrztdeXJ0UbuMiZT794sQv+DeLX7DMmUvWJYwmB2kx74WXDENQD+6zDVnnXTGB7SOc2txvQ0T0Su+CdEv6ekVgQN4zhRzqtGUOBJgSaVEyzin9ZjQ8v7SkmNSNHDXF9QZ6YrNj6NAMili29rMgZ6dod2W5iKUJRX0t9aDbOj7AXYD4GzzW8fyuX7veLzfBxiaV1QnALjJFCwcoYKMRMrBGOEHvIIoNUfM0JsPAZPADRg3mzT6sIk2aySKrPdTKCzVHyXqHCV817WijG4LeqjEYuppSG/rx73IUxd6GpyQPbh+045XSYl+HMpv18s=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYxNzczNjQ4NDc0OCwKICAicHJvZmlsZUlkIiA6ICI0NWY3YTJlNjE3ODE0YjJjODAwODM5MmRmN2IzNWY0ZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJfSnVzdERvSXQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmY5YjUxNTYwODNmNWE3Nzk5Nzc2M2JhZWU5YjRhM2VhY2U5MmFhNDIzOWM2YjUzODM3MTE0NWExMjAyOTg5NCIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-54.5, 70, -65.5, 0, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        e.player().notImplemented();
    }

}
