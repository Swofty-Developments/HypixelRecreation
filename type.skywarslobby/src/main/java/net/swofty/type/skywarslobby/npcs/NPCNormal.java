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

public class NPCNormal extends HypixelNPC {
    private static final String TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTYyMTg4ODg1MzgxMiwKICAicHJvZmlsZUlkIiA6ICJkZTU3MWExMDJjYjg0ODgwOGZlN2M5ZjQ0OTZlY2RhZCIsCiAgInByb2ZpbGVOYW1lIiA6ICJNSEZfTWluZXNraW4iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMTZlNDNjNGE2NTI0ZTlhZTgyN2MwYjYwZDM5MjdkNTZhNWNjMzE2MTczZGUzYjA4YTBiYjQ4NjIxNWY0YzA4NiIKICAgIH0KICB9Cn0=";
    private static final String SIGNATURE = "T5ox79ZY+LE+ZcKywXlkv/enpo2OtI3JkoLX/mbt38/WgwKPGyU87bpbNdAjfX2oF8rGlnGYWAz0WZqdGHSZ7Lia7gp8SwUlswXBZCT6q9WIltR/qI1p/sDKUVzWboVj4dE1MsruQL8wx9GKy5imFXMVO5C+Vy6iGJGdDalePthni9LjbQpwsQoyBrgLTHXCQKY0luBNhqtwTJDSsazMN2Iigw5TVxOu0LplUdXX0/raBEHR/U/nCT21plKY83Y+Q+BqAhooUFouK8vTeE7rqeNyJSCqUxTojf+reBWr30fp3fhK9zbz7xc8wukqGECLe2Sb51UeMhTW2jnBHIAU8zZiYlZqMGEu/ziD1sInFQ3KiCvegGIMWkKdL8iBylCQr2QoLMuykN7K4DCIxXcjt3UsPW50nZ19kb1Yb6JZlmLKaF1kBsLI3hgzyvmuokCamQ+pZTAq1tVvAMSM+SXRn/1a57lDKZ9Kyi9drJVIatp+2nThxkAur4+34aiU25VNeentFA/LkFWDj4uWwkdSZRPynJVNu/9CKdoXBnkcpy7eigNrYfAtKSc2NyCt6fKumowdy34ER+GompKmNbIlUvRCPmLJGdJhDUp4Qod3OK/02gb9KfZe+OfxB6ZaAxnKusQuXK8lgmS6ZwSuzoCq3LUcAA/4FdCojqkkT541y0g=";

    public NPCNormal() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                int soloPlayers = GameCountCache.getPlayerCount(
                        ServerType.SKYWARS_GAME,
                        SkywarsGameType.SOLO_NORMAL.name()
                );
                int doublePlayers = GameCountCache.getPlayerCount(
                        ServerType.SKYWARS_GAME,
                        SkywarsGameType.DOUBLES_NORMAL.name()
                );
                String playerCount = StringUtility.commaify(soloPlayers + doublePlayers);

                return new String[]{
                        "<e><l>CLICK TO PLAY",
                        "<b>Normal SkyWars <7>[Solo/Doubles]",
                        Text.of("<e><l>{} Players", playerCount).serialize(),
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
                return new Pos(21.5, 64, -10.5, 90, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return false;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        new GUIPlaySkywars(SkywarsGameType.SOLO_NORMAL, true).open(event.player());
    }
}
