package net.swofty.type.galatea.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCBankerBonsai extends HypixelNPC {

    public NPCBankerBonsai() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Banker Bonsai", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "s+PmKKWQkmtdmuJ10xcGJttkmzxGBSnGcWI1S4T9G5x81kOHqrICXEZHjyvqDcpYbSAliW4aJH45+1fiD40rcbnLtocKGNgju36W581+Zsi67PRhQgpz4i9RUR0juvvC6sm8B7ZA1YrZ2CfXRFi2REhMNher8TNBD02N6f0VGEQYJXwIpRM2XpSCeu0XRPi8umpwETUbUgt0EHAvL7gCjy8Dra/gTSdCcnx9pARr76ZGpvFsiAf3AxYktUhUz3PD1MsY5x61GS6+2SM/ywInBHAFZLfqFGUvtSuFKD48VafGakoVGw3U+Wo30YeusFKhiDUBhi+3pAJOTNi0QQAYmE2Jkqcmgp1QlDZFxRRMKgzFUE3Rj2CLegPb48eDGNOg8WKU6bmzMz8/4qswIFG/zkntATKB6x6EPJOKpVGv1S1V4rQQeuAP1EgYqFizrJZpBBuhw1KRalaMQ9JUbHnesoivPFu21mzJ0srULdA1OEputr+38/iSThr01DODv0v0zptdIpPVv8n9ELLAn2UoAJyYCkyGkxfx3bcBHebdvNcjMAN9d3ZfvGPr8Kq2IuUljHms4jtM2YFg1XLZu4MKuSKfjQfyF7nmzuSB+LMrGqAdYli90/cqmRxJh6RlttRmUVisUCK6neY//gOJGBb/+DDcGE9IN6T1Z3VebyciiIo=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTcxODkxODMyODc4NywKICAicHJvZmlsZUlkIiA6ICIyNGFjYWZhMmVlNDA0ZGMwYmRjMjViNWVjNjgxZmRjZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJXaWxsZXJtaW5hRDM0dGgiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWM3OGI5NDU5ZjhjNjRjOWQ2MmNjMjI3MjI0YzZkYjdmYTMxY2IzMWNiZTE0NDkwN2UxMGJiNzBlYjE3YjYwZiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-598.719, 116, -37.312, 6, 0);
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
