package net.swofty.type.bedwarslobby.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.bedwarslobby.gui.cosmetics.GUIBedWarsMenuShop;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCShopkeeper extends HypixelNPC {

    public NPCShopkeeper() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<e><l>CLICK TO OPEN", "<b>Shopkeeper"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "Vglicj+k7wgYrbXx3Pr/IDbOwEUMybnVBPDC/Nln0uSVrHcWlB/gu+FyZcLC1zWv8ETe3GVpfbV0Mly2tMO4KyzIVSj3A3tK0V2R7a5s9M8O/HN0Iac8PvTbUeI+6Hrxgt017wzppvjgziNNql2iOzDbdc3l3zO8io9539XYFNLNSVhcn2EyGFklQrKwwuWCENJ60KX2HB+tjxdS1omzqgM6K2ABFxHfN/tJhq+DTDwi6Hj1O1bK+wb8Cj9c3XcFWr6Y/j+/EdRst4Sg7SjlsUunFffwoRojTZXYiqSIyfSBkAC7yR2axE7MNBrBw3r+/KqnXDrU2WD6hZW6d7QQTOLGkcx7uvclh9MRLauww0r+L852bt4k0h+0/YG45veGSXUNPxpqzZdpA1vQuKG1yWwx0yIsPhTE5vjGu/xIR6QkJZ43Wx6+Hx3SGsSMDqp3MfuMlqSUv/YBJ6A2Ya+jNK/mb4VlUwu9osGW+yfbicOsXG+I3LNdNUXogXvja7qizza0rrCsPbi9u3A8gTvYpVuM58vqxdtvx1AhoyHDx6RKWDd+IDw9xFHd+hsrTTlY8qvJrTHdEmc8qJyMfrE8b2mAlGH+EiMdGhKifhJ0XYmhfswp8hXKdGzA8Ff2QXiWNX7nHQow4alQtNZp5RMU4ianSl/lDCqtEiS0xyjhgyk=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE0ODA5MDA4MzI1MDIsInByb2ZpbGVJZCI6IjQ4YzQ5MTI2NGY5ZTQ5ODA5NjI0M2RmNTE3NGQzYTQ0IiwicHJvZmlsZU5hbWUiOiJWbGFkVG9CZUhlcmUiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Y0YjM1NjcyNmNjMDkxY2ZmNmM1ODg5NjNhODJjNmY2ZmNlZjUyNWQyNjkxNWY4NzhmNjVkNjMzZjhkYTQifX19";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(4.5, 67, 0.5, 90, 0);
            }

            // TODO: is holding a paper
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        event.player().openView(new GUIBedWarsMenuShop());
    }
}
