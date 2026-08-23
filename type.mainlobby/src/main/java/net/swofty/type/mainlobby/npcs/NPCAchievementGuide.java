package net.swofty.type.mainlobby.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCAchievementGuide extends HypixelNPC {

    public NPCAchievementGuide() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>Achievement Guide", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "IOzgt3iLZEQUGMft+g4dhw3Gu5vWxt2qEmlAxY7sU6MRf/PD4h4ezzr0gsQe8slcQELRH7ecyI+qTRDLh/pvu0Djg8AdGY9+20Pock54L7BsL6cl37dJno7nn6RZqQn1KqSlxVCsM2rgnQ/Gr+91aVJ+VxR/THV7gztlos/Az+O0c4nSX9tNhczHWwDoUQFGBEBqOr9iVai+DtrXM31Q5UELSHyaeBOMg5+BEhmgZiWdD3iddvIi+t07aJhEC83iZD7G2k1NyRXSozbxb1D/sP6tsa/vLqVC7PBD+8lKAMlla21/K33q175wLd9MJNy2bp6AsIg3WB64TPkQ9LgDzhTa9a64XhoAe0gQvZGFznlrgRIrKQTXFiZdTauUFc37Ao5foYCsVWQ1HhGq1t2uTD5t39baDj17dvxCBl8sQ8oQtEmzg8dFSVXT8CNJO1GHBqJxzAX6QhadQSdcQI36C2LoUTFh4JPSN5rqW7eyPBQJdN3rNss2v3p08umCZaSSPJAW5d1axWFl7OfCDWy8EyGqEtMt45cEEiZhDkiKgJSAzlcxlOM3LE81r6vcJ+1xzhNn+nOwM4RUV26FhtAkWOpTCrXKYwSNPbUwVSMGf1tLjDh54Z3XS6K5zuksP18xsjhQZfpUp4vX22bSHyHH99k93YbrJYxVK7v9QMT6xnM=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1MDk1NTc2MTIxMTUsInByb2ZpbGVJZCI6IjQxZDNhYmMyZDc0OTQwMGM5MDkwZDU0MzRkMDM4MzFiIiwicHJvZmlsZU5hbWUiOiJNZWdha2xvb24iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzdkNDYwZjNlOTM5YTNkMWFmZmE2YWY4NmE4N2JlN2VhM2Q2NzczYWNlMzA3ZTExZmI0ZTVmNDU3NzYifX19";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-39.5, 87, 42.5, 129, 0);
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
