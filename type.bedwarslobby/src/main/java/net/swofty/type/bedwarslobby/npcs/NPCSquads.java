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

public class NPCSquads extends HypixelNPC {
    public NPCSquads() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                int amountOnline = GameCountCache.getPlayerCount(
                    ServerType.BEDWARS_GAME,
                    BedWarsGameType.FOUR_FOUR.name()
                );

                String commaified = StringUtility.commaify(amountOnline);
                return new String[]{
                    "<e><l>CLICK TO PLAY",
                    "<b>4v4v4v4 <7>[v" + VersionConst.BED_WARS_VERSION + "]",
                    "<e><l>" + commaified + " Players",
                };
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "tQBE08RCcC7KXz758VAUxJRwHRmITEHre3VmUcyxtmluwNzpAYhm/NcJXwhPrw3W+sY4gnOWbWc5deoc8tAiTT43tJWbqsOMUEZ2ECS1mBfpJVCpO8A/fboJF0T8WWkW3Mi3j607kY5sfzrVmn1jw9BoCit0SXXXpM9RLuglWkoXQ6i55pTJCcwyXx5FjjoucdvBwlmTTZ/HYCLaf75KeyPZrDmu7UfqWpE1aUmXVv3cvRmB80dCIQHh8WZX8UFHt9X/gOOwolczt8bT+AuOQMoJ4wZ415Iw07dOI19ZgPsn8csrKmmScub/4r1LbeKr3MnyHYdF4PSbCERWaDNZfX6CwDYqxQJufimnXBRaVl1oT7hmhB8ywy2/49LW5nqw4r7QT03XfqkcCEfEVyTnU5FfzukPTshtZWui6Z5Kvd8hF5OHM07SWf4xkioPPZJ1P40I80xVx88rnrsDWSWo8MiAJKsjvRus+wQPhysdR4P6ZjMql4kFhzmWagnGRAPlsKR77b3hdlmCv2S530nIgkN0FzyOlA4/XB9QFwESPL+j4HaAtzcJx5xvhpF69LYsdd8yvf5e6CS2SiZJLv+HnX6gA5DUXnVi4LbRxYu3zbrBUAVFJmsIX8G/IfB6YVRheDkOWNBmhN29bxjfjCDSVjfgoBIIZl00VhrjDq9kXWg=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYxNjg2ODIzODA4MSwKICAicHJvZmlsZUlkIiA6ICJkZTU3MWExMDJjYjg0ODgwOGZlN2M5ZjQ0OTZlY2RhZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfTWluZXNraW4iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMjUxMmFlYWZmMjJkYzYyODZiMDkxOGIyYmI4OGMzNWM4ZjcxZTk0MTMyMzIxOWI3NTZiODE2OGI3OWZjNDcyMCIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-0.5, 68, -3.5, 90, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return false;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        e.player().openView(new GUIPlay(BedWarsGameType.FOUR_FOUR));
    }
}
