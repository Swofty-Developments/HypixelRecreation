package net.swofty.type.mainlobby.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCHypixelStore extends HypixelNPC {

    public NPCHypixelStore() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>Hypixel Store", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "B/HLg4Vl1m7QfP+Ay3j1DWaizkOYgGXi8usPcHfR5varnqN3mGwM+DtMO15KAliwyZd1LUwAulHAp08VNdVfittNmB8+Mh60XtMUCCXgnCuxcFLkhy+aOxX4O+n4gYhzBZqqHMBvSfpt64flYKS/5IZX3xH2G1BcrJo2Vqd94c6NAej9voL/J6UM89+0q+8xb/jNF32JTaqUcaCBBsnR22i+MzIJsbWSHLb+f343He15UoNBP6SqfnfOtOvmz0YF051uc8A/TRlFJdnj87ChHhvb/WasPxsAUZ3eu2y0YHiUBs5TeAv7NW6YF9l7xgc9HAqrKvDF9gHIg6fIVCRqeOv17BIR9Lf5sCGe3ASkwFTZdJppr3mZAaUCMWrPR443Jp3FzHo2TfLR5uuba+JvpgEZ5Sq7KI7UN+VvwIYBhLXZiZuBVPNlXU8d8ZNmOKWs2WWk455/HW75afIC2bNZBJja8i9JNg1GYxvMBsl6im7jRJ+UItBX9DEgYiX656Ue3gI3N0sxNFg0IGdBgmo8dYSI2Ol0giy61dQbdYUCLP5dCKCudQHRFBsNWwncuAd8+t5SYAjIgNu741o0Dh1YJK5W1Bki03DrDerPsIuly1dgzzzcGImHPVNLHDnRUn7bfDSLRQcp82cJJM61pnjQslW0aYv8MfvEpgNtg56IVZ8=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTU4Nzc0MTg3NjY0NywKICAicHJvZmlsZUlkIiA6ICIyZGM3N2FlNzk0NjM0ODAyOTQyODBjODQyMjc0YjU2NyIsCiAgInByb2ZpbGVOYW1lIiA6ICJzYWR5MDYxMCIsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS84YzFiZDNmZjM0YmYxMmE0MDYxN2VmZjMxZDhlMDliMGNhOTAwNzdjNGIwMjhkMWI2ZTA4NWY4YzZjMGRjZDljIgogICAgfQogIH0KfQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-44.5, 92, 25.5, -180, 0);
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        event.player().notImplemented();
    }
}
