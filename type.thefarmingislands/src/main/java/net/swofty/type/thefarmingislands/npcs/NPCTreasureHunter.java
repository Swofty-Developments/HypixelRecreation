package net.swofty.type.thefarmingislands.npcs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;

import net.swofty.type.generic.event.custom.NPCInteractEvent;

public class NPCTreasureHunter extends HypixelNPC {
    public NPCTreasureHunter() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Treasure Hunter", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "WJ2wuOWVJxm0DEAl9dymzo2eC8TjjxjvS0c7BuCDepoCQhv/KIZcl0FXD+ybQGeNp5NDq7NsZ0NNFoKZRcOJq/5PUuOgDQx/0JGCERxeiNRjztMxJb160H+RHPaP9qmo9fXyl4TsmjBOL33Jol88U/D2O7eUDN7694cj5MZMw4pnZ1Nj18O3aZu/zVppultNEDCJtPTAmKZcWeou32ITrXn/NJPm82kscK2gMjzqgExM6pud7aWCdOkaPYPvX8LQIL66ZIxufS2E4bqGBRR4QARDLSn55JtWhQel8alAXoh9e4cOe3i+82SjXTbrJCuuyiHSQBPIhkPfX3x5N5eu9+GtuPURotjGYfR36CiuKotmJFhNw1EDHH0U6RoXwFM4NZ+WbJJ6xE+VQirTGv4pUKp9hf6Vg6Newqf/f6gzv3DPxwGgTHt+XiUWXu4d9n5UhLocp69m5FJQHuTDpJyzVBR1vxSjsr0S4Si1zk0yDyKITovY6/i84WAnk6bl3SPWWz8Nd7B3K+RnmqCLo4TBSJZeAzbqLDNmGPGlxsEmXwp52MoGJZcMpAHqmnDzNQf6IsM/eWRDlt9s34ZE/DkzWWGmqR9z4Koe1eD+gcofKAoJNFHyR4+4St1+Evn1mxkY79mdFWNC2oLGwBTBD17WOi9Wp79Z5H9CRrR3ZbTqGx4=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYxMTg3OTY1ODUxNSwKICAicHJvZmlsZUlkIiA6ICJkZGVkNTZlMWVmOGI0MGZlOGFkMTYyOTIwZjdhZWNkYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJEaXNjb3JkQXBwIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2VlYWY0M2M3NDc0OTdlMjBmZTA2NjdkNjc4MTBhYTFmM2NiMGFlYjk2ZmNjOTRiN2NlMThmNWQxNjVjODI4NGQiCiAgICB9CiAgfQp9";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(200.5, 92, -437, 0, 0);
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
