package net.swofty.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.dwarvenmines.gui.GUIShopLumina;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCLumina extends HypixelNPC {
    public NPCLumina() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{
                        "<e>Lumina",
                        "<e><l>CLICK"
                };
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "EtTogfRrMFuYvMm04HNNbCgpXKBqI7yBJXPds3DD30SrSzG8BV/J67DPYL00a7cVpunqKzDItUaTMjbwZqriKufDYEwES6PZB7h9iqEpEjO4aTPd6hGr49v49ssB7fJES6rNsMfqbrfzOcRY8HMysTiRY2QDsRIzFq/HpZyt2B0WxrygdXokpYvN3zc5dIqR5ULvPO6BGGaQ3yn+92xPO8ymyJLpIllRUwxmFdb0yDlPFAp/VZBIZeF9zpZSX2TfP7A/09U580wb7dWqJZ+7LX4VZ1EeyDZKpQoG4I2I/cZNlLR4sEp1XTCD/dgitRu0a+KYr8LheX3smQYFNHKl7ucivFRAv8gNLXKz0jD5tfXJmyc968ercV9YiJyoBnSY3a6coSh6UfAXkYiC1iorwLDBV20MY8NOxsESBZNtLxR+u2D/8kbdZnsnDlsBeppOmZjSfjb4iXCicDvp7On2eas8edcD5bCUhTMp3S3RZNtr2Yro6fShfWaq5Kjp7buXB2TpIFYcnE9xH542kFuBwETM/njhR2BrjcBk00WnAqaB7ui1mFJ2S52TeEzdgqjMbbQ++6iLaMUw1wqgHIs+gkiikxLaO12/xyToBBREabvZrsKGQiByCqA0wWFueVTobK9rFxM8mBRmFtRcdxeoy5zkixyb6zDMtqRauP61fzE=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYxNTI0MjQ5OTc0NywKICAicHJvZmlsZUlkIiA6ICI3ZGEyYWIzYTkzY2E0OGVlODMwNDhhZmMzYjgwZTY4ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJHb2xkYXBmZWwiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvY2RhMzA4Y2FlMTc3MGY2YjcwOWIxZjA4MDA0Y2EyZTljMjBhZjZmZjhlYzUzODFkMTE0YzQ0MDZkZjZiODBiYyIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(77.5, 199, -110.5, 0, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();

        player.openView(new GUIShopLumina());
    }
}
