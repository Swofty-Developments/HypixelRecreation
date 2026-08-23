package net.swofty.type.galatea.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCNina extends HypixelNPC {

    public NPCNina() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>Hina", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "fndDfeYzJYIgb2JokwBAwxam0HWE+MDkOXUK/A0XGHT3trwsDjfinAnyqpnA53F/4BCiMQci2ZPeQKxpZTciY/VsY1ic64cj7yW8eUd9zkmYXB1crzpRKWIJCJohHsz0mWRhzemKr+TQ1PTQXkrwMD87js6KrZHFqvryJQyjOj5AAjBbNUZ6MYj/diKoMVigpcU6ow7qIw1AzUodi5kGnXSZrpjyN+M+tfDVblr82IGI4UoO7JmG8TSxmis8uD8cK7oHGFA7gLgkxuPaTIis0OKY1wG7PLq3k3CDjzfXcbJmSAkSZLsSLp3JAs0E+/AG+yQ8+QGETRvPXWTJUOF5+E12Gme+hRZ4NKp2g3dNpvZRcEoHPNfFNTlJoaJ5VMFiduDfmmTGWtv791XTu8o/QCoy5wM1peth+cjtrcdkPOhyn/O9MdtMJc/V52w/6BzBvpiPL2s6GOHLR897Q6gaHTtyZcukqDeUK1kS0sgX/NLUZ/BptjqY1oA7/wVqM115Ou2ViRPWlY/HqYV9aF6xqe7bk8fvrdGD7ROVe6zXrBtmjw5SjLu+/EoIQckXqYLtrqgitzfWM+P2UuSCGV+C4VT6m2cbbedWrm8GZqdQTCfttRkiEre5SNYt9f3f/N/d2CdXy3upY7VY8jGQWuohLi1Kz2HJBIJvqj2TLSpMrd0=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1ODgwNzg2NDMxOTAsInByb2ZpbGVJZCI6IjdkYTJhYjNhOTNjYTQ4ZWU4MzA0OGFmYzNiODBlNjhlIiwicHJvZmlsZU5hbWUiOiJHb2xkYXBmZWwiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzUxM2JlNGQzN2EzNTg0MDAyNGIyNmY4ZDYyM2ZmMTQxMTllNzYxNzg0NTkyZjFhMmNjNGIwMWZjYThmYjdiMiJ9fX0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-553.5, 110, -16.5, -141, 0);
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
