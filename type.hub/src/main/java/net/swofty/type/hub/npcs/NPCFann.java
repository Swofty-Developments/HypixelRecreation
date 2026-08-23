package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCFann extends HypixelNPC {

    public NPCFann() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Fann", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "iXkXE5kR/LAhlH/Hqhd4rytl9bCEIs7RgdCBKMMKqhoKXPmymS3f27nSqaRF2IeNSQxag5MAojkKhTp5Md63TcgU+8sD/9Hf/cpOjU137plYIqWkv2YQodY5I5+WIyVtxeNHdgturHgHF3WAtGF9byzzFUeyhXMAx3bgKhbN0DrPWYzx2E757q+JSTf20XT5S+Ry6C721cGfJExkMK7uR/XYNukwRI8NupZmSCHqV7ULTBSEtk42wCRopi4VJ693da3U36bxp2D9u+765vC5z6j1TJUKOpBJMvyns+oaGZTXEuhUZRN0eIxZaRd/TxzwYqV8Q5xP1pE3XEkJB8S9dZbN0Q9c5++0VfhVUseGfDePEsD/gE4uMaTxiBMtymLidK7gw8l5LYL5WqdCUE2i/xfjpLyWVJVp+SUBvhgHwl+7E8KJcE4rJMBj5WD8qnpAcX0BPowAzkHoneKQJ8pZzcwa4rf7/ZrbSREIf5xLE+H/MkWtBE2GO2vXD6BHCYwFmZKzAX/r+5CvfbVe4QVnQZKz76tqiUDzAcPMjs6NHDkp8o1EY5QTjbyO4vTHbSHIfWGOMn5k18WA7xBhU3R6yiTGLcvFmIaqPVeKWUfb3DbEz/sPFiA+5zxzKdQ1EGWdWUKtB1asl12EBAeC7d18oStEiqrFN1JF4oH+jQjapw4=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTcwMjA4NDQ2NjQ3OCwKICAicHJvZmlsZUlkIiA6ICIxNmJhNWU4MDJhMmU0ZDJhYjEwZmZiYWJiYmQ1MDdlZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJzbGlua3l1c2VyMzMxNSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9iYzEzNTRkNTI1Y2ExNTBlYTI3YWEwNmY4ZDhkYjQ5OGRjYzBiNTljM2YyNzc2ZGVkZDgzNzQyZjUzOWRmYTBmIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(9.5, 73, -62.5, -90, 0);
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
