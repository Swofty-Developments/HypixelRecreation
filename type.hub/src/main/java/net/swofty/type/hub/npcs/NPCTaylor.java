package net.swofty.type.hub.npcs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCTaylor extends HypixelNPC {

    public NPCTaylor() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>Taylor", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "tOgjpsAATSBO9ocrq+5sLS9mPbma67wiFGNYXipzTl2BSJQQL0Ab55SW+ls1LiLL3ZeMEzLf0BCixHSmQ5Ewk+GQNsabvxSul3ILHVPZPNl5rdg3y5KTw/+tP5LjJh1kw4HpS6Gt468H8EuP26McbnmlD/l9BgnJaTN8HhuxnpaVugvt8nXYy0/IreIi4oKEL+c279hF3yopaQ/y/dhPshnXlgy8qh0SRbWodV5238DRDj7RuH53ZwSh4CH+3pmaIljcbnU5hjsZw5x69SzXjlRbcceaTgJJRg361IH25ig10b5XpMMWv7qMmAQmHtGztJT/XWyBIYd4KbLEC10bPl4eSPbd9Ke6RhG0XnlY9bIBPSLHakWkUYIm4Y2fsYrLxKFUDCg7tk0kJ+e1FejgJzLslRW8rj4HAGt7w6Px7dOW5q3fzUd4nzqCSyquNlluaKqbMyICvXoBLHqnTg0X4E5RRoZSXVUw37Bd5EGzkNck6VKDTBaMjFsguWY5zRqYGfKq8jQSKjIr9MuG+PYS8n2lqvEeHKHrHfIqgcKGP33dnOqmKTb7pwtdWkPpH/4DzzKNqVIlUQZdYolEy2wTDCKnWHK+Vj3C3IPyYjpUKttNuRH8QcUf6flNmpVDJBg9eJfjSpzo5xv/MxN4gzt4gWhyMPO5qCxjR8YTjar0a5A=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1NzMwNTQyNjQwODgsInByb2ZpbGVJZCI6IjQxZDNhYmMyZDc0OTQwMGM5MDkwZDU0MzRkMDM4MzFiIiwicHJvZmlsZU5hbWUiOiJNZWdha2xvb24iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzE4Y2EwYzg5YmVmOGM0ZTdlNTIxYjExMzgyMWZmZGYwMGU3YjVlYjU1MmEzNzhmOTAzN2NiZWM2NTVkZDI3YzgiLCJtZXRhZGF0YSI6eyJtb2RlbCI6InNsaW0ifX19fQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(37.5, 74, -39.5, 135, 0);
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
