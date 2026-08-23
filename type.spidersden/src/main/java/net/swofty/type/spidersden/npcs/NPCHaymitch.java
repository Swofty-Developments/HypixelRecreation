package net.swofty.type.spidersden.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCHaymitch extends HypixelNPC {
    public NPCHaymitch() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{
                        "<f>Haymitch",
                        "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "ZiQozDQddFh5n6/fx2kg3PNFpJdk8Vr063P1KfoYd+aNtwIr3hH2yUjzJgoZiV/75j0EsSn8Iq1hcvGg/KsAr9v1mJEX2/EJiUU86F++Hj+uGu3JZN7BixBD3GOSLsgfzgatuxMDPhrZumq6bg/qmBBG8+QyVVxjju24fCm4/K9pBPrJDh5khSTVOY+01oT1uxg1eW4Yv3Rmd0Wq2Pf12evJPPnj/eTGAdz5peSV/M5O4T/C99PVIZ5yTYfBsX9m7TJWZ980/ERmTFeZS3yJcn6NKwqhHenQjUFS2AnbS52MrX5h6GzvTow394lPMgxN9S0uNgswOh2XLuGdHVfhrhXYUs4jbGRMIxlcTtNI7vmG3wZOxUpKCq2ZuUKimv9zHrlQ+TWE9kjZ8wvlMNjyvWA2QZkSjhMAFB6QMB+inBW5r5BNIiHRu8AAy4I5mdoZe2PCrxt8FWf1kiJcWeFVj+4nso9Lbkx1+NXo8z82DONyxrIKj+Evi65xCOsPmQD0fOivycuryn5BtdnKIVY4xrL8VKJ2L6vAgSL2Y7KBGeCnXe8msdt5yxj+VcGI++OTR4neBrQ3onXd3EKKyEGHmD8x0hm+nkIdje86awaMpB0pmQMWusxxR233gSBTZ8kmHwHUK7yv6uS5MOZecxrE3BerVF7YUrh3+xjtRMN83lM=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1NTk2ODMzNDU1NTksInByb2ZpbGVJZCI6ImEyZjgzNDU5NWM4OTRhMjdhZGQzMDQ5NzE2Y2E5MTBjIiwicHJvZmlsZU5hbWUiOiJiUHVuY2giLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzg2YjlmNTZiODE3Njc2ZDUxMzU1NjU4NGIzMmJiOWM2YWEwZGRkNTEyZDFkMWFhZGZlNmE1OGEyOTA1MmZlZjgifX19";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-202.5, 83, -236.5, 0, 0);
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
