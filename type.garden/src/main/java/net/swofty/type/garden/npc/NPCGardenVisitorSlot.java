package net.swofty.type.garden.npc;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.swofty.commons.StringUtility;
import net.swofty.type.garden.config.GardenConfigRegistry;
import net.swofty.type.garden.gui.GardenGuiSupport;
import net.swofty.type.garden.gui.GardenVisitorOfferView;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.garden.GardenData;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;
import java.util.Map;

public class NPCGardenVisitorSlot extends HypixelNPC {
    private static final String TEXTURE = "eyJ0aW1lc3RhbXAiOjE1NTA2Nzg1OTkwMDQsInByb2ZpbGVJZCI6ImEyZjgzNDU5NWM4OTRhMjdhZGQzMDQ5NzE2Y2E5MTBjIiwicHJvZmlsZU5hbWUiOiJiUHVuY2giLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2M4Y2NkNGZkZjU4YjMwYWE4MzAxN2NmYTVmZWQ5NzcxOTZjMDI0YzhkMWEyNzYwMDRlOTA2OGU4ZWNiYjBiNzkifX19";
    private static final String SIGNATURE = "EzyAHb6TAVKVuO3R6cTt6eNJYXdU6C1fpPByuOEL/FUIIHqW5QpUnQLP7s3EjLhhzRagDi/eU/xGe09Ucsb7s6tSavn1jzfqwnmVG7C2FJ30ELl35y3pYbNKwmBl8I2fDY9pQrmfJbWRVhv9Gw8W4h8YRZARnW5PfVdsL1ddbTTsssaxapU8YTfUc88h2egnTD/bEHaqYEgfLBzjyMAyK9pDUIqe0NDmBJLbjPZXIVImRbMKanwgLRxmUkjGLONerb0HE8Kx6QoJEumoLOBrOLA5BJF7Jwghrv2d1W9S6hr89Ul6R8CnxQwHFfBMejccm0hLZein4DrKbiFHC8c/hs4jCoC4JT4rvOd/Yp8zNr3Y/dtUk5uTOguk/gYExI+p+1xc8HwTK3sK75LiFl+Ryu4LlKv5GBEznsnRHv1Ufeia3NeuVXDLi/W3zR8VG95Hf0lmKHdwJ/R9E56TxNYRh7wpma37ZTfEpUpKE1o7Z2m5c3jmDxLRdQg8dK1ZYMjlul36Qa8SXYTM4T+bdB1577M/44Vyde1NFVepYK0vRXDDNRal2LoDRM9buoTuN2taeP3pmt5C+pL554r7tWgOdCHUz51E9hwsOA9VCVxIA5eS+bgzSBLkWbXZYNo+zi/0bVr9OGdP2hCTJDsd0x7YEL2P7qribidVjnRLWWP8a2k=";

    private final int slot;

    public NPCGardenVisitorSlot(int slot) {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                GardenData.GardenVisitorState visitor = currentVisitor(player, slot);
                if (visitor == null) {
                    return new String[]{" ", "§e§lCLICK"};
                }
                String displayName = displayName(player, visitor);
                return new String[]{
                    GardenGuiSupport.colorForRarity(visitor.getRarity()) + displayName,
                    "§e§lCLICK"
                };
            }

            @Override
            public Pos position(HypixelPlayer player) {
                if (!(player instanceof SkyBlockPlayer skyBlockPlayer)) {
                    return GardenNpcSupport.hiddenPosition();
                }
                return GardenNpcAnchorRegistry.getVisitorSlotAnchor(skyBlockPlayer, slot)
                    .map(GardenNpcAnchorRegistry.NpcAnchor::position)
                    .orElse(GardenNpcSupport.hiddenPosition());
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }

            @Override
            public boolean visible(HypixelPlayer player) {
                return player instanceof SkyBlockPlayer skyBlockPlayer
                    && GardenNpcSupport.isVisibleOnGarden(skyBlockPlayer)
                    && currentVisitor(player, slot) != null
                    && GardenNpcAnchorRegistry.getVisitorSlotAnchor(skyBlockPlayer, slot).isPresent();
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
            public Instance instance(HypixelPlayer player) {
                return GardenNpcSupport.instanceFor(player instanceof SkyBlockPlayer skyBlockPlayer ? skyBlockPlayer : null);
            }
        });
        this.slot = slot;
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        if (!(event.player() instanceof SkyBlockPlayer player)) {
            return;
        }
        GardenData.GardenVisitorState visitor = currentVisitor(player, slot);
        if (visitor != null) {
            player.openView(new GardenVisitorOfferView(visitor.getVisitorId()));
        }
    }

    private static GardenData.GardenVisitorState currentVisitor(HypixelPlayer player, int slot) {
        if (!(player instanceof SkyBlockPlayer skyBlockPlayer) || !skyBlockPlayer.isOnGarden()) {
            return null;
        }
        List<GardenData.GardenVisitorState> activeVisitors = GardenGuiSupport.visitors(skyBlockPlayer).getActiveVisitors();
        return slot <= 0 || slot > activeVisitors.size() ? null : activeVisitors.get(slot - 1);
    }

    private static String displayName(HypixelPlayer player, GardenData.GardenVisitorState visitor) {
        Map<String, Object> definition = net.swofty.type.garden.GardenServices.visitors().getVisitor(visitor.getVisitorId());
        return GardenConfigRegistry.getString(definition, "display_name", StringUtility.toNormalCase(visitor.getVisitorId()));
    }
}
