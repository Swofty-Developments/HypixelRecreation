package net.swofty.type.thefarmingislands.npcs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;

import net.swofty.type.generic.event.custom.NPCInteractEvent;

public class NPCTony extends HypixelNPC {
    public NPCTony() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Tony", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "q+WCbao7pfdeqzeoM228bAO5L1zhmYrlwxTz/7pY+Y+YsOPhKrKYX3pDO2XLsWqg2Zzh55wXsa2fHRWCd8p0GnYfUkVWWHBQWHy600iVH48RnSZLECnCure/bbn7auNbs7fjdtkXk0lmyn5z2lxnQFpFHy4gOODPgkpXNsLSJAnaNnOnebS1UBu4p/HVlS0jK/DLngtCK9NV/4bo4BpDXVDvZQOShSJoWSvnzVPriTla1zpVq86WKGHioFdUeCM/tTSSw5jtM32/HiQaC388Rvt1yuvKFoI5VoDAbjY5R0Kh8q8PsYKrKwUsN8SFREsCqXE1kjyG3zTkyrz8tXO5vBh9J5fPavQBdvGr9H6Vs1JgAngS5vuNl3sqfqYgTd5p2JE1JSxOJX/v4v7iTj6TKtrGA+jZ7xCDX1pexi110iEnZMm0bvJRgkwlSxXYKpzO5dZjyHqRNvl0sbQ5O7Pa0H0fSPrM1Fo/2WxY76GtYHeRucIKHgX8H264TlLtQ3QrALP9gS68Xcm4/mfYrT/YZAossLbUP2PhWvWL4xnGjRWPsu9LVHyY4oI6Z0aRBQPFBTc/v7IaucUcGmSAh4MKV4V62hMQ74sxK94vOJYYCWsI52GLyz6EWAQj3TQR8IIgGfIhKnn7UeIDbOntyisFkdsJU0iOHF1uXGYkmpayzSc=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYwOTYwOTQwMjU0MywKICAicHJvZmlsZUlkIiA6ICJmMTA0NzMxZjljYTU0NmI0OTkzNjM4NTlkZWY5N2NjNiIsCiAgInByb2ZpbGVOYW1lIiA6ICJ6aWFkODciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNjVmMzFjMWMyMTVhNTdlOTM3ZmQ3NWFiMzU3ODJmODVlYzI0MmExYjFmOTUwYTI2YTQyYmI1ZTBhYTVjYmVkYSIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(278.5, 104, -544.5, -160, 0);
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
