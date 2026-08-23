package net.swofty.type.galatea.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCAlan extends HypixelNPC {

    public NPCAlan() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>Alan", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "m4+5XcihPvzB9ui4Putmn7dSHlIR7Fm0LRA7dB2vC+6ZgD9LHVp+JB6W5xlET0XuJKt+fbRdD/QYpkxtfpcIQOYdQgUFlpHsxhM9qyUtStNh3XdaJmInojmgWAVpbPLcVbaAwC5vKs3VRaygMwIwDkRmIZah2JDODFQr341D6FWIzE2WW+3qi2cd8FAcBaWLcwZxcFcVB6c7Q/W3tu38rUl39LlWvC2ETzH8iwfQzpbJ4tyYFAglFQjwtvP1X7yex5Yw+4CsZPQHS8GQzUCGAuQJgsVRCg0ZFM/3C1PrOU9pS7qYTj8zRks2yja7z0D3MYtR2o47Tl7sM2f1QEcPEBUCFFZA0+03dze/sxHwiDhSAKGaqU8uQWjDcaT2+dtJepzzYqvg3o7Aa9okrARpfelWSuACEnl8Zv3SVOAs3HS98KUjQ0HysZhMfq6SlYKxxhSIu3hOcRSnE5NE8M8xR+eBvCde+nmKs0uhhDi/yZ2+WN3S81LwECSrPAGsGtQjvhRi21ITnx6wGYXwhRgecy76UVTN3qaf9UvJbCV+tQq7PykIOs7n8mnHNKkW/j4gd1RRu9SqOG+2Ntym21TpYmjXVdvUWkMaFZjMV9N9FryDUfq83Fc3XoztonjpEyD8lrwsX3Fa5ypAmmpM9hcK59dkA8H624yReTx0TZ1XDuA=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTcxOTMyOTA0MDEzMSwKICAicHJvZmlsZUlkIiA6ICJiOThjNGFkOGM2MjY0NGYxOWUzYzhhZTE0ZThhMDI1OSIsCiAgInByb2ZpbGVOYW1lIiA6ICJ5cnRlZGR5IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2IzZjFhNGYwMGNiYjBiMDRhNzgwZjQ3MjBmZGM1Y2NjNzY4MjhhMTI5YmU5NTBkNTZlNWY3OWI1NDQ1M2ZlNTEiCiAgICB9CiAgfQp9";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-625.5, 116, -36.5, 0, 0);
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
