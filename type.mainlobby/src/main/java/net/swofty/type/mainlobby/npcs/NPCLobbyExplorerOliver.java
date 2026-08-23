package net.swofty.type.mainlobby.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCLobbyExplorerOliver extends HypixelNPC {

    public NPCLobbyExplorerOliver() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<a>Lobby Explorer Oliver", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "Etu06UnHFKpOr+LoGJal/maT7xqEn24WTPldN3O1mwmVsBs/aQqroHOR84PJO1AtdhhBJOC0R3Gb5KKQOMqE+TD2/LqvHQYsWtticbunu/2oKYfk0A1C55rhGrwYTVdzz9vv6VSheXgHiYYFUANmqHj5KUnXXntZexqBZzZmWUYz3HJV8rvoZ88P4b5DxRIFIEdrWkEVmCOeft7m/DTpelx0LhPCTUS2ilamzboqRPwCWF+mT18XRUNH3vBHa4gBG1AbbSd7GSvbVGoIqmXR9KY974Wz3Va75ArNMkBRgjX78hCPJ5xecUDv9ZvkoMjjPXJaf87ncM8epkjssWEd0E7MovxQk2TbUTbKsMEUkUEUkExT7mp/NgmJSeS4cb6hMyjB/gPd7VRpXbK+3MpiZe4HwlhaS8nt0SOjH4lz+dmGXQvNIHaWXnk8Xp6aEnorBcllHUzi1y4cygck0COIEf9s1o7N5BfNLfxjHrMRk/XOl/vo7bEUeMB1YLlGUMnGGr0wumdx8w7WxDbPW7PXImMx84mWKYEPSzpzdYJ2ksysyZoyBVVPfZfy0b1/CvelrKOh4zv2wwiyANVc+V1oApdfbmoxCoOmywIG3nxJsDG75Hipf8UAND65Q3EQuHjAdH3C2iS889vf7tS0ih5jIcB0M6aZSeS2F5Ht5xS1ZjM=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTY0NDI1ODk5NjE3MiwKICAicHJvZmlsZUlkIiA6ICJkMWY2OTc0YzE2ZmI0ZjdhYjI1NjU4NzExNjM3M2U2NSIsCiAgInByb2ZpbGVOYW1lIiA6ICJGaW9saWVzdGEiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZWM3NWFmMGUxYzQ3ZjE4YzJkZmJlNjU3ZmQzYjFlZjM1MGQ4ZWYzZGIwYjE0NTYwNzMxMzE5NDY4YjY4MGIzMCIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-52.5, 92, 28.5, -150, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        event.player().notImplemented();
    }
}
