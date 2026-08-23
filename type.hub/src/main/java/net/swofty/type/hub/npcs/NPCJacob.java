package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCJacob extends HypixelNPC {

    public NPCJacob() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Jacob", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "pEkQ4zTK7/clvbGWw7JthccvquN+7wwY1rAIqUULhF6PLc8FWRwcVnwtd4K/j8xaTbe3ol8wayk8hraHtPPx6gxtLUbT/r/th9gsfJIBlbvcYJmrLnOI5yUHHYtxPjC2O9OTcxspyv2Ha0kF5Ua/RzOEjU4JN9HM5ni1g6c/U2mFgUnnj3SrNjr5pkIO0wZ27INHcswdVJuD8pC+yaflaSgKPV4CnoPFPp9uBvnVG9Knsz6oVNLno3ZUMojMYU5O3Nnkg8naNb/G2H1FBbwtgVxsjaprKbsqhrQHvFF9oia/Fz9I8IqGukLM0J9Dc3UANjORxeTZcqKkTnDM1wic7s52B5cuZZCQ2by5Di+km/hL2m8dJzVe7TbfS+FrK0galtQHD7jb7yXapQ7LqbutHTuElVCijrp0FlUE0rC+dUeBTbVFcXqo+V/cy2OVuv6DBJM87/6yogqlxyD3ZIetmJx9go42rIXwK97IgTtHt4nO/l14/kc46FL2ESe+P7Nqe/bJRmVDqEy5kuTSB7P4po1O2UHYv9Fydt0daxDrnm/CekKAg5EWh/m30RSKcXu84E85Y2FPW0U4jnmd2fcHaR3MCIY9os44GUEMm56xDKDNtqpRohTEhfo9ncLHTnC3+urN4QudtTbwjha31fvewzIDZKFsHAKakhyEG+OHehk=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYwNDM3MDQyNDk4NiwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2I4YmIxYjQ4ZjRiYWJjNjdjZTM5NTQ3MjA4ZmRiZWQ3MjJjYTU5OGNkZjMwNjgxZTM2N2M2MjQ3Y2FiMTkxOCIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(46.5, 73, -128.5, -37, 0);
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
