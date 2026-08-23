package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.hub.gui.GUIMaxwell;

public class NPCMaxwell extends HypixelNPC {

    public NPCMaxwell() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<6>Thaumaturgist", "<f>Maxwell", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "CALy0nHX/3+BBjSHm3DEfAUEkDJKHU+jgvi3iS9Z3SCxwTGUaP9DiL0Izyc2MJth3B4Qxe9CwYzJ2l5KFBpHu52KTGWyv/cwyJn4GxZ+rDeOkCqeK1aWN7hzOQsZH1kYQ5Xf/KgeF0oIhOgCpexYtIUsU+bERMcl4QCVeogZ6Ewzvyux3tdLTw/TjYDpuAgW+594QtcjKYKE1lFb51DrHxl/c38nddaAPi8Ss4rPe/O3gUp5NVLNnaJ3+hZ8rsOhkEk3YwFCEBLMZ2xlAFm3s8yyILCzT4huZ8lqmZDj3xUM5bFKwl+iGdjfvZyrZUDNr/+zf2ZwhGllzIfttWm6y8xwzwv6JIqu/XTo+taohn7MPZGmJVt0nkrpmrXNBeZAK/g6pvJzNvqiNugXIaokcLsNYzTSKsV++/qbz7ZIaWqsImB7efH6/7fcz30JUga+wdnOMauISONzjvsQOzcGKHTHpaNP402IBjVa3hu0lIDxLwfhm4JYa/RfNGdQwJV8L3G7Vu3EdGMmu28MhfRtmge8CTI5cCf5Jt40HSr4rlTPM+PG2uq6iruOCoK1vtNKbqJubcuNO0AYWfXInlHB/NzrwZ9blemPe4/Lmack8skjrsTxzoM/X6Ze3cBIvOCLtUN8s+v0V4RAkyiA38eR4Z6cLNqVLiGbc3zCTBic34s=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTY0OTM1MTIyNzU1NywKICAicHJvZmlsZUlkIiA6ICIwNjNhMTc2Y2RkMTU0ODRiYjU1MjRhNjQyMGM1YjdhNCIsCiAgInByb2ZpbGVOYW1lIiA6ICJkYXZpcGF0dXJ5IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzQ0Y2RmNGE4NDJjMWRjZTNhZDJjZmFlNDAzNjZlNTM5ZTlmNGYwNTdhMzliZGI3NzdlM2MxOWYyZWE2ODZlODIiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-66.5, 70, -66.5, -90, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        new GUIMaxwell().open(e.player());
    }

}
