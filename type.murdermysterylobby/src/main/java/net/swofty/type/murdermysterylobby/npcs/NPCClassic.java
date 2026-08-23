package net.swofty.type.murdermysterylobby.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.ServerType;
import net.swofty.commons.murdermystery.MurderMysteryGameType;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.utility.GameCountCache;
import net.swofty.type.murdermysterylobby.gui.GUIPlayMurderMystery;

public class NPCClassic extends HypixelNPC {
    public NPCClassic() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                // Count players in CLASSIC and DOUBLE_UP modes
                int classicPlayers = GameCountCache.getPlayerCount(
                        ServerType.MURDER_MYSTERY_GAME,
                        MurderMysteryGameType.CLASSIC.name()
                );
                int doubleUpPlayers = GameCountCache.getPlayerCount(
                        ServerType.MURDER_MYSTERY_GAME,
                        MurderMysteryGameType.DOUBLE_UP.name()
                );
                int amountOnline = classicPlayers + doubleUpPlayers;

                return new String[]{
                        "<e><l>CLICK TO PLAY",
                        "<b>Classic / Double Up",
                        Text.of("<e>{:,} Players", amountOnline).serialize()
                };
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "w60TwsZJWTY2BpmqxNF6CntYiYchRzXu1QaBHZhgmtjNvsSOrndyF6F2iDr7yOp+cF81H5dCc11Uw42zUpuX11PnfFe3yEhjsbgPLI0+8UvBHxrh/We4eLBjx059Cg7PhLPSXTkocu8XLdbp97yA3p1ENm65ScFQMQduWHLdWanD9Q2SoB7OeypfEoNg8Y7MCCClgExqlojH6VMtMoHH+/DPrAPH7BhRwoQGal4g7EHHyBjbXT/DR1TCu4NoDEISyhexUtpfQsqSfLn45cYlqmmPnLddN48kJAruLBvwQN6vIaYi7tptkcBJjJgqOtYiPF1/X/GXOf8VhT7QIZapQkZq+t48f+K/4jxt/v46J4KaERY+HxP6hUHP7OCUtBXuwYgTHNoM7aJZtpeol0PqnQmA+5lMBHx9qZIulVc4Z3J/T2Sq6sTx/DJgaLjLFxdOnpQGY/NJBDecEgUyKR0tZrPrJC7fYLGwukTnKn2vLRtdyPveOU2hnAZdg4TKft0vipdfoIPTS+nATcX+QAiM7LgIcdCeFUQM/7PkhFSPaH5GCBdih99JGj4ss8hYRhQdu3mwjVAa1vbk5yPcpEyf7711c11VJydJP3QznLRO/fjoMMIsMJWaX15amO7TRAOujkgRkyb+1fkIwSqUT+cPOQKSgkVWuCBGlYr2sbSYT/0=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTc2NzEwNTY1MjI2MSwKICAicHJvZmlsZUlkIiA6ICIyYjcyZWYyYWUzMmQ0Zjc1OGEyMThlMDI4MTViYmNjZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJ2b2xrb2RhZl82MyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS84YjI3NjQ5NzM1NTk3MmI5YmMyMGQyMTVjMjcxMGNkYTQzNGU1MDI2N2ZmYzhmMGExYWRhZWI0NDgyMDkyZGNiIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(23.5, 69, 0.5, 90, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return false;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        new GUIPlayMurderMystery().open(e.player());
    }
}
