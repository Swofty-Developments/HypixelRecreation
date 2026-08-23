package net.swofty.type.thefarmingislands.npcs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;

import net.swofty.type.generic.event.custom.NPCInteractEvent;

public class NPCMason extends HypixelNPC {
    public NPCMason() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Mason", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "FCikfFZ7XLICYw8VXpvxpjPOmbXLPd72AWnRWxapE8fMVuCFETMPmW8IDRljHdiwUfenjc32enJ5qUTtOu2YySBPkOozpJl1MfGGfvfeiGLcRTnlszR9v8QDCDHKPhxI+G3qpKefgT+pqkF1AHW093jkanJN/325bhP3jfSUuXH3xdGWOb8LMWCi1CE/zJvF1QeAGZZJE6ha6SAMIKaEoCQMUiotarZuKf3Z1GHqzeUBj+5qPt/809e2IndlEMwutIgRRQKVJj4VCNmXb1LG7QFzpvSan5ITepj5QIkXgPlCDdhC/QIl5jS7516z38Pxkn0sWFNg+F4QPTa/1sn+Qkwdu623VLJVpWT0CahYxoOKcti1TzJD9jJI3DUgZ3DlvBzvYdzCmnCVFwAkVnMvel441Ija1Mf6orm8nvmn9HUkk4OVFRts47PGzoqFrTuqTwbPiSGVmYu5kTGwkh3MN3WgoHcb+GEUtQ2opAEuPcPvVuhpavGDHCxkwk1jYQ3ihamSBjGVmVuzQdOcvAEwuW6SKH6pdqnqHO7qZ2fF17eGQYzyym7JY3XS8p61ty2bQtD37PCCKZ3s/kVAQjiM5qMTaMLjQgZQnJP7lKp/0EbLoONgo1RGMSl4rWtk4dRciw1i4I+9aBD+hapOZRhF/Vl8hqEwoCfYk2lQUa9FXLg=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYxNzg5OTY1NjAyMiwKICAicHJvZmlsZUlkIiA6ICJjNjc3MGJjZWMzZjE0ODA3ODc4MTU0NWRhMGFmMDI1NCIsCiAgInByb2ZpbGVOYW1lIiA6ICJDVUNGTDE2IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzNmY2I0Mjk2Mjg1ZTMzMWE5MzYwMzAwNzJiZTcxNmZjYjRiN2JhM2UzZjU0NDc3OTMyOTRjMDNlZWUxYzkyN2MiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(177.5, 77, -356, 150, 0);
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
