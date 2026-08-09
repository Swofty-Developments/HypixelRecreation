package net.swofty.type.hub.npcs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCWizard extends HypixelNPC {

    public NPCWizard() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"§dWizard", "§e§lCLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "Ubs3ogFzpCcDZZQiNcIrTQTykcnJsac0Fm+mXYMFVh7CNuTdmGlcJcqdb8fXmBJFOfO9/8zAxKVyn3YVehkzzFdHJyMiPqK++arqf0eBNElbON9LUlPvOGca7JCBYBJdZWbpTS3lbzL9VZRoba9GmM0XZfvunZyLTF671JrKT0adDREe4B7HJaqmLAPOXZlzQokxeyN0oqXAPThkQPcR2UzKNQpwAAPiQhrPc7dTvikw9sHOVPX9GsTsqUAjh1vQOZl4DeALMZwG5Clzc+SDWAa5hgNn15SVCWlbPcsumGziXo1EPhYrx5XbGkDS/9OE+y7/7qMQE1YAUoVw4umREJ5YoqWfv5Elko1dOkF6HfVLuyglFIoJenPJ6/jy7C1tNpHbCG/xOKBeMAGR1+YNCIm3JWzgZbFDMpKjFhfgQEOaLPsQPLPxAYA+RjNRpQpXSke8Zfi64j2j85G4gCcOy8zxlYjHdKgxiZNSIs2cyLSU87BdJFaVBKkQvnB5z9AsJu6u1OoC0A4NakRDQyhjzn93RO8i6LoF2MQshEbBn/2q1ja/KfIw4ZjsK11G7xTP+qnVdkXzgC0eGwnfAqkltbfbmwnGVShe0+cVF3FNs+C/j2BtjptUm7Fhw7nu/I40J+u+jTA/NdZl4JWGCIDIzsawACfO14Xt5DjrmtcY24U=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1NTkxNDEyMjMzNjIsInByb2ZpbGVJZCI6ImEyZjgzNDU5NWM4OTRhMjdhZGQzMDQ5NzE2Y2E5MTBjIiwicHJvZmlsZU5hbWUiOiJiUHVuY2giLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzJmNjlhYjJmNGU3MzQxYWMyNjI1OTcwODNjYTM4YWQwMWJmYmJmODczMGZiZmJkZDEwY2E5MGFlZTMzN2EyZGQifX19";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(48.5, 119, 99.5, -231, 0);
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
