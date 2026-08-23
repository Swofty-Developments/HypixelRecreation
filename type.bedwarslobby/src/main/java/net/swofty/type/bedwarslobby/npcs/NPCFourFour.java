package net.swofty.type.bedwarslobby.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.ServerType;
import net.swofty.commons.StringUtility;
import net.swofty.commons.VersionConst;
import net.swofty.commons.bedwars.BedWarsGameType;
import net.swofty.type.bedwarslobby.gui.GUIPlay;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.utility.GameCountCache;

public class NPCFourFour extends HypixelNPC {
    public NPCFourFour() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                int amountOnline = GameCountCache.getPlayerCount(
                    ServerType.BEDWARS_GAME,
                    BedWarsGameType.TWO_FOUR.name()
                );

                String commaified = StringUtility.commaify(amountOnline);
                return new String[]{
                    "<e><l>CLICK TO PLAY",
                    "<b>4v4 <7>[v" + VersionConst.BED_WARS_VERSION + "]",
                    "<e><l>" + commaified + " Players",
                };
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "NYN80kE4rhej50fuuWnlv0SsxFpTPzLkZ8LqtN2VLwltVNijjA5tH2LR9iD0CFp7nszVxoXCull59qzzGdjV/QoCFcvPpYmAQH223/7NzCAPBqr0nZpQFWYX5jO/eZC64yGVHZL8k0P9J+JQmfHQuDYw9bpaWKmxWrNsEmulgyk1QRbxBxMlrIQbB931hNDe+Wb5jhwaNHe642i9Q3VV2YMxNqmE+mt1UZPEv0a7S8dVveUIO1CkfqXtuMEN5WUb7qBZDIS+Gyioggloiz+pwOdVrrc/AnCYXD31fHejVuDIi5N4fas/JDDlYMGriIxeMQ2d1EDkRKT0/e0s3eMnItBCF6W3zQYVshghuc+lJEwgnTjqzK+0AEnBpSgNwCFfB+PSFirBOVCOT/4SCS4/IMuhybfXHgiGOulaj8B7G4V1RydzMskBhdtGNTGMes813oAGhZ4H8Ja4iqcoM27utIw+T9YiL81ECLXWaPqDaSFGcTT/0PILKwArARATXMmvRndTbL8XtyrfUwnHMgWVtnRs8mNeGDZVcGSGQoWZqaWySnsMMI5/BKLTdtOqz10UiN+x4JfNAZWRc/WvdzrcP15M4GYLuJGnNxitiQk3AX24BMzM9cymPJOW9HF4S8bhnIFZsCOdrCSxtLTgLK2oIIkuEKy69swLJ/feFII/6x4=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTc1NTE4MjU4ODY1OSwKICAicHJvZmlsZUlkIiA6ICJmNTBjOGRkN2FiN2Y0ZmUyYWI4ZGI1M2NjYzRiYWQxMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJtYWNoYWRvVF9UIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzVkZDc2OWQ3M2UyOTY2Y2Y1ZGY3Nzc2ZmE0NWM2ZThhMWMwZTBlMWM5OWExNzQzNDJkZTEwNzVjYWU2ZjM0ZGIiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-0.5, 68, 5.5, 90, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return false;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        e.player().openView(new GUIPlay(BedWarsGameType.TWO_FOUR));
    }
}
