package net.swofty.type.spidersden.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCArchaelogist extends HypixelNPC {
    public NPCArchaelogist() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{
                        "<b>Archaeologist",
                        "<e><l>CLICK"
                };
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "T7z9Er9geL6Y9+z7o0u7BUc5kTlbrWw+w6jNDYecdJeTX+u7XfB7C0bgYD4e2MQawQxvI8otTTA8GWrvlgjtfP/xiQD70baSd1u4/bVRKDcquG6Knn7ALmLz34WDlIjZT7N3yVPVuAnauFeYKySCe77pphoaBJCySOfDjr78zeYvTsoWNmq+AJ2BlpsMfErhzKgiuZbRFNEIrjVyUcm/OxWFiqi8nODA85St4Ka4dG9q7cUAjZbI9mfz0cUWPyGe9k1S6Zv2MXiWh/9jNGXXPzjAIPK+uXynm5ihhxJPUJfvB0HE/UX2y3clHwqLlSiG93ju/TE8EBwCXxbPkzlFny2K1aAYlTfhxoTu4J5S6vDleFBhIBTxslneEyALgsYarpS37e0cta6cNx4dwvENaZfR/vekqLfAR6G5FV4P+6q9FbfSsI5rhfpnbgsluY9hkt1J5GCzPVyH1+Lfxse8310yhQBQ0HCDvX7SG5cioXxzFs1Sr/25/bC3/BpfG1Z6PjrdKuTymz5DPbu/pkjXkbti+1yGYmBDbSFz+zurgKxerkzWSumtApuIGGX7nc43XyKBoklfq/5sEZ4IuIijlIlD1pDWtxrpxkSdhFRFibY71PAV/18bez+ABFpr1lbydPCvo8O8yiq0NTAAc8hw66kL3z6cpagvq7SiQOWdPf0=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYxMjgwOTU0ODAzMCwKICAicHJvZmlsZUlkIiA6ICIzOTg5OGFiODFmMjU0NmQxOGIyY2ExMTE1MDRkZGU1MCIsCiAgInByb2ZpbGVOYW1lIiA6ICJNeVV1aWRJcyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS82ODVlODNhMDk5NTFhMmMyZGI3ODM2MGJjOTU0NmJiMGQ0NTQ5NzViNWU1YTM0NGNhMWRhZmY0MDhhNjdlOWYxIgogICAgfQogIH0KfQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-360.5, 111, -290, -41, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {

    }
}
