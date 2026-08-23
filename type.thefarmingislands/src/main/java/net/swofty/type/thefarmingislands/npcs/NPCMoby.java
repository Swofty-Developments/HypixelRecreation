package net.swofty.type.thefarmingislands.npcs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;

import net.swofty.type.generic.event.custom.NPCInteractEvent;

public class NPCMoby extends HypixelNPC {
    public NPCMoby() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Moby", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "O3vkTjT7tKfVpyQwg7q7m0PIqkfl1uvaQoeHMN9pwpOeMZ9kJH45rr4vJV6eb8qjWoOpYl01p8uLHyW3+TSFPuCDBsQSKIqAu7+h6LpZn3dGIfvS+7kml7keIR+L3JPiMz+qT52E2BWzRg78MDIb0OPF5sAB/3XvpiKq4TIQhyrw0m3ovi+7DF4Kk+DK2tTflgpsMb35kiR+hqyIq87vENv8iN9QnE8iI1xMQ3sirpDBhz+dtjMVKZkG9J3lKiiA4LUspphiM8L2NrkF0ru9WET9BngpUKArmBzldfMw0t8SXDUPkiCH2p3R7LkI1bVEaF+Cw/WVlSVMxJrBs9dkRkdDbpMDNRkiEkFfd39NPCaKi5nWzhaLmHTo4nIzYnZ76E4LpaYQflCZdH8ECewbmU/zFXUVcp+iYOkfgAIAFq901KQ/z0aDsC+181hMCfwmaPyz4rWX6efqeZge2qw9bV8SOz7bom0jz2KAoCU1xyi0Ql9A2OXu/YU5pvVUlqBI5pyp+y/EwHeJ3imRzdAH+UHRxdb4nTDl0txFISiZCAG/wvGW/cisdfaFn3PblmbFWtKTN9NrmM/MCnb1w5hnKPhvNFZt1ch8Xi1Xpa5zghSiy7TT2gANlkhvpjYWETmMfwcZ71sf1gM3SngYH6IpJzCeGWN+E0N+SoyMviq9Kec=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYyNjEwNzI5NjMzNCwKICAicHJvZmlsZUlkIiA6ICJiMWMyNWQ0YjMwZDU0N2Y4YTk3NmZlYTllOGU1YzBjMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJvd29FbmRlciIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS85YjY4ODU0NWUzMjZlNGYxZmQxYzQwNTg3M2Q5YzViOGJiZTY5ZGQyNjg4OWNlM2RlYTEwYjRkOWUzZDdiNGRiIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(206, 43, -500, -130, 0);
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
