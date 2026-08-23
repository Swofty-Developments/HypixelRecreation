package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCRichard extends HypixelNPC {

    public NPCRichard() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<6><l>STONKS AUCTION", "<f>Richard", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "NmqeCuF5tgqe1V2Flrc9I5pKVu0UYsW665Jw61VSB5roccOc2E9rzfMep8EtU54i96Df8BLqAVfGlPoha7/Rp+wQOQ5/rL52aGugkkiPSMKQEHg1BcR2CsMijZOTKXtHm0z6cwE9n67bj9U2jJYTKPqhTwG7CqLUJ6o0KfaA08etawFQRF0qCX3xgfXvSN3odOUvfKLJY6NApBAdnGjrCoPxe5jMtWj1MbPqSqIjuHxNAq5MeHjglIDRiykHpTZl70NNFcUYb54+Y5iFCfRKV9AGy7hGSPelP+5pR/6jXmg175V7a0svLW4ylQ9iCDbMN1csd25wrtmJ3XkIj1cHlxmVwMZHZwSJniGPZz1KEzr8i6J/JqrBoR6eEVecbt/7wcx+0j7HEqC++eN0GwrLXGB8/8xkD5PeTWs8HAYZpBDSr0jzTLaE7L6XNkW/gSo4PeX134pcXKcn04n79w0R8R3Cceq5az38pYZvXJyllqnyN0A8xkSFj5xDIfTvgAvTcfpW9VnXq5P//dDTSzCUKr1LZGinpymXM50FY71Pe73YhQS/wESL8TXyPC/z4R8n52cL+VpDDbea+Fz91oJtt6KGsXISVVgDG+QBXVYjfmCohdT5Qpy4e/o1b9k8keQgIgH0ODcJKmPh3pIr3I3IScmLPis5JDgzZn40AhvdV/w=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTcxNTkwODE4ODMzMywKICAicHJvZmlsZUlkIiA6ICJmNzcxMDI1NGMzYWY0YjA5YmRjY2NiNDRjNjg1NjFiMCIsCiAgInByb2ZpbGVOYW1lIiA6ICJDZXJ1c1YyIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzMyOWJkYzA3NDcxOTg5NWEyMzllNmU1OWZjMDE5ZWYyODI1NmNmNzRhMzMzNDJlMWI4M2RkODYyZmE5NjMwOTAiCiAgICB9CiAgfQp9";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-32.5, 70, -80.5, 90, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {

    }
}
