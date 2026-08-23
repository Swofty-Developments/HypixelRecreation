package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.gui.inventories.centauri.GUICentauri;

public class NPCCentauri extends HypixelNPC {

    public NPCCentauri() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Centauri", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "IgsJrr1HNui6CIIaIiQRvLIUACp3xYnvCkZdMqlKhZ9567qt3px70+YvVoh+BQWI8wiDtSwNN3102qFQp+OLNJOSeN1IImBMSu+1mW270JzhHpQO+ymjnhZG3jWaQ1XUXL5t9/BiNR7RSldedwwvTjCHtR35AjMwfb8AsLmwaXk9xb6n1J+F1m9oQ5hw9FsODmQR10APfz+Up+iFI9N056xjxb6YXCtT4H799KASsrMELHqfFZIaRZ/Zf/pPRMOpfOSFWkPc7t04z21yGeFopRU2RpDTRWi4Fbe1E6fxTt/4XjXp+oZ5bCinY3pPmi8cls3Y/nMFQjPxAE3PzTcxv9v+oKT0j5g7m8e7A+qd6UEYdfPLYg20kVA59PFbU0OxKj5cBrqH9ooftNQm5+absHuqKBm5S5Uqiu0Bdw7Rzk6np4OL3pFSU+wO6uU/XXr3/OrGGwcAjjeRvg1kDYgWziKX5ORL0G18zcuyT4/6C1KoKnm+t4VuMTPBiNMyDHTEjuFrKaEdlykiDxQ5377JjA8BKD0R9aaMppDH08mQ/QhBir6HNVTs4oiXlAcdDLegoRq3WQV7jcSIqmdjW0pKBSnM7X3VNbG1yvFTDclls2cWQtzMMh56lW+clpOJknJxa5LKQ9eSE7XxiJP6DNEltnMmkUeEAYEQIX+DrPquz2M=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYxNTIzMDI1OTA5NywKICAicHJvZmlsZUlkIiA6ICJiN2ZkYmU2N2NkMDA0NjgzYjlmYTllM2UxNzczODI1NCIsCiAgInByb2ZpbGVOYW1lIiA6ICJDVUNGTDE0IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzRkNDdkYjcyMTkyNzQxNDk1ZTE1N2Y0YWJmYzJhZTEyMDJkMjkyNTBlYTAwNDdhNTJmYzgyMDQwODllMThlMTAiCiAgICB9CiAgfQp9";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(2, 77, -5);
            }

            @Override
            public boolean visible(HypixelPlayer player) {
                return player.getRank().isEqualOrHigherThan(Rank.STAFF);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        e.player().openView(new GUICentauri(), new GUICentauri.State());
    }
}
