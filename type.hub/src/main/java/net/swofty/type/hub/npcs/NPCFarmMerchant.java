package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.hub.gui.GUIShopFarmMerchant;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCFarmMerchant extends HypixelNPC {

    public NPCFarmMerchant() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Farm Merchant", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "JzFsBS2+uF9KejwuhN9Y/ri1D7GckhODpugutLbPBdtCr4me3NURwE6owdr/rY3EUa2S36bbqRucuTsEM/HU3Tti4MXUqNJv9RnsZsr/o528EtpA4LfDMBO+MZKBXKvspe5rLYl1lb6ZVgWCJ1hTm7s6yhA9bBqZ5mtcRsgwMJV5fY9uzydiOuUe15vtLzDYrXfmuu8USoR6EIQOeSQe1Ui0RLE3MmueURwXBXOzdtYnLwdsdOKofv4haRe7W74f1Xab6hxRW3oyhtm4pKune0sOyxGsNA8/DQ0vgAWKF712F+DoRGEvVGiyhXIqMl9xEYi+oKcMIOWdkj9vwvIuJBuXKsagPMIMVKveKu/agP2OjFf71KY4ob/nreRKhLR2aNU5qpzC+KXbMa88gkiZUvEAmzzIECY/RgwI0DFxkL43GXetKQQ2tfElPvkgeqhnQkArOYFXIPlRQI01E8dku9ekKuSkLiNClwN4Knaw7OOYpPSe65viYUhREj3UWYzj3Al40ut7MEP7565pj4OKxKQ2ToOcfT9Zm/2FJ4X9PPN+ubKVAq1la1cZuh/O78lV3k5v/6UlhXXMTuKPVep1U86rd0Fdp1E/nYReKoJHX1NIg7xynSk2qy8dol9OZbLlKdxo00rrinckpoAY/GeABu8FR+ZhFgCqDdOaVo1Z+wQ=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1NjAxOTM1MzI5MjIsInByb2ZpbGVJZCI6ImEyZjgzNDU5NWM4OTRhMjdhZGQzMDQ5NzE2Y2E5MTBjIiwicHJvZmlsZU5hbWUiOiJiUHVuY2giLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzgzOTAzODQ0ODczOTk4ZmRjYThmOTVjMjNiMDZlMTA5NTQ5NGZkZmYyY2YzMzgyNGE1YzU2MGU0NjU0Mjc0ZmMifX19";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(63.5, 72, -113.5, 45, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        if (isInDialogue(player)) return;
        boolean hasSpokenBefore = player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_FARM_MERCHANT);

        if (!hasSpokenBefore) {
            setDialogue(player, "hello").thenRun(() -> {
                player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_FARM_MERCHANT, true);
            });
            return;
        }

        player.openView(new GUIShopFarmMerchant());
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[] {
                DialogueSet.builder()
                        .key("hello").lines(
                                "You can buy and sell harvested crops with me!",
                                "Wheat, carrots, potatoes, and melon are my specialties!",
                                "Click me again to open the Farmer Shop!"
                        ).build(),
        };
    }

}
