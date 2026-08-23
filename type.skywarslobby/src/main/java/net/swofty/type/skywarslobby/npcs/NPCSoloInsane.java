package net.swofty.type.skywarslobby.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.ServerType;
import net.swofty.commons.StringUtility;
import net.swofty.commons.skywars.SkywarsGameType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.utility.GameCountCache;
import net.swofty.type.skywarslobby.gui.GUIPlaySkywars;

public class NPCSoloInsane extends HypixelNPC {
    private static final String TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTYyMTQ3NTk1NDcwMCwKICAicHJvZmlsZUlkIiA6ICJmMTA0NzMxZjljYTU0NmI0OTkzNjM4NTlkZWY5N2NjNiIsCiAgInByb2ZpbGVOYW1lIiA6ICJ6aWFkODciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOGFlOWYzMGU2NzYxMjJiODdmN2E5MjkxOWNjOTFhNWE3YzBlYjgxNjhkZTVkNmFhZGM2M2ZiNWI2MWJkYzJiMSIKICAgIH0KICB9Cn0=";
    private static final String SIGNATURE = "DKbvqc84PB9P7gyQqHAeSaURn0zqDR7sDgBgOUAXzHoNsQB3s4zywXFPy8EybZtJr4KZgDj2Ibmps3hOjKIyOL9VrkV1qnxEFQJRaVBnETHwW45msYhr+wuoiRbW4uSBVzZ6ViHmTcdM1bDYv9n5Kxck3dpgxY3+b5+bE+DwdBYqY/7zkLs8Gf/2cWYQBMn/OF9iFxnwMGGupl2qKZ+p/Lj/xRjzIrZ5i7BUMyDCAhj+h+vEMWrZprG4ZBrs2T4tIQySEVKlypm8cNBjaoO9k5SKRW6jVXp7izVOvImFoSBMmzQFsC7/S3jwvcQManoZuGAFcBAi2SKbTxziujEwZkOY732/soArH+k5sJgrBh/9NgIiCk58w1+6+Z8D5KHsTNfMtFT5ROHUp72UBIyHzhNIn9MS5Hb8sZKMDD5JxT3GrfV9M1qYITE7cj16aZ8zG5dKpGow7TMbn0zL+TWUM64r/5SciuebDLlvupAccrTWAT7AeT+fRfeZojPB+mkrasKym7fJuC2ZXY/Wv4BmGvrbIVu35SFlIwsO0RL4I/tKrXTrk+UZyCLQV17doYJAMoVNdMWFfPBRxwPkMQYWp3JCgOcqFLYWXJUc5DIRfEen8MYwk4zPqsNGHYTHPbjdk4e60gyUmC8Y+tf6fm1o/ApJZ9NDeMQ3B8b3akdGueA=";

    public NPCSoloInsane() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                int insanePlayers = GameCountCache.getPlayerCount(
                        ServerType.SKYWARS_GAME,
                        SkywarsGameType.SOLO_INSANE.name()
                );

                String commaified = StringUtility.commaify(insanePlayers);

                return new String[]{
                        "<e><l>CLICK TO PLAY",
                        "<b>Insane SkyWars <7>[Solo]",
                        Text.of("<e><l>{} Players", commaified).serialize(),
                };
            }

            @Override
            public String texture(HypixelPlayer player) {
                return TEXTURE;
            }

            @Override
            public String signature(HypixelPlayer player) {
                return SIGNATURE;
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(22.5, 64, -7.5, 90 ,0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return false;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        new GUIPlaySkywars(SkywarsGameType.SOLO_INSANE, false).open(event.player());
    }
}
