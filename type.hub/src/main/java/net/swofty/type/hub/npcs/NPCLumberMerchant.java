package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.hub.gui.GUIShopLumberMerchant;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCLumberMerchant extends HypixelNPC {
    public NPCLumberMerchant() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<2>Lumber Merchant", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "rK6rdM1BcPwjg+cHMT5MJVLaztD/3lH4GZLfzVbD9Rzof9Y4ET9zu/qpgI3XkBYMn0lCcpd/QHfpi8yhlfxpuvdd6hcnzLnKASAikYEVb2yP1HzE/9ScvDNUbnyqBX1DBItwCHdpQyeOxYxOV1yN5x92t3C+7aAG5XORcZhcdYEhwwT1/c+8LSby0diJ7+QFEi1qJNtHzABJmlSMJ7pEH0w7GVBGhY2knPz2cP07CipsrpRz+luypRbMsIubRg2Mx/0sydeNK3QmQVzWJaQjBTI/5VfAgGQwu6H0wEONNXOMmeSqM4SxIqh/KrnUKbifFVCLy3bqR0nRdSa15clRteD6P2LFl1QM8zB9eCR/6269VQPNocPCCImPQR/P2tS5uHpsyoxkfkrMR8nR01f4Wlu1t9k9nNo9rifnSOOFl5qWjxodwKUE6jcNL0eEblFKJxAOUkP/4dwmGYxTffYLzv/RbwTCJvUKRzSw4C60lsmLabVfqokGAPsfo7uF11AqaJ2fJPKDSURxtADNJrsk9PpfH9jJS/ro/bujgA3r3G9xPASknohbi6hAwuLxW7PTEGff8PBAEMVNuq3Bfj0oZlIaDDvZy+lIhzHeuX7NZ451KYjzbk1KOzGKW0B1gMd6P7DqZdPlK7+1/vcqvY+hoDY/Wjciuu29HZsdDR86krc=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1NjAxOTQ0MjU4MDEsInByb2ZpbGVJZCI6ImEyZjgzNDU5NWM4OTRhMjdhZGQzMDQ5NzE2Y2E5MTBjIiwicHJvZmlsZU5hbWUiOiJiUHVuY2giLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzdjNGYxYmVmYTAyYjA5ZmEzNDlmMDI5ODZmYjRmNGI1MjBjYjgxYTBjNTJmZmJiNWJkZGQwNzQwYjBmMDMwZjUifX19";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-125, 73, -42.5, 0, 0);
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
        boolean hasSpokenBefore = player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_LUMBER_MERCHANT);

        if (!hasSpokenBefore) {
            setDialogue(player, "hello").thenRun(() -> {
                player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_LUMBER_MERCHANT, true);
            });
            return;
        }

        player.openView(new GUIShopLumberMerchant());
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
            DialogueSet.builder()
                .key("hello").lines(
                    "Buy and sell wood and axes with me!",
                    "Click me again to open the Lumberjack Shop!"
                ).build(),
        };
    }
}
