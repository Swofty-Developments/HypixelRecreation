package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCZarina extends HypixelNPC {

    public NPCZarina() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Zarina", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "mCZ+I++tLR3JDFxnFfnONbwimOcGwbnl2L6miDS4QrXZoKylNc9AjrdfKro3UBZphn4u5+XASjW5dAK6iAqfewf9TlSjX77qBhfFsBt0kVgSi0hMJzhUvmDUxcyx7AZOWo73IxFMEgd7T/j26Rxz4Djnx7UF/ZVSYNXV6CSTIpHK2FcjakJlJiGJ0Jcw21SFEH+pEOX4Jee6upXGfxbtuFnmXLbI/lTKCGD69/rB6M/zk40KMLlV7k0b+RjRMYZbWcsTeCyQONyNuzSiOkafZi3EHeArUgHSKPTycKiCbHxWv7LIb1coSwznl2x9OcsVtTG0eLrQj/S+gegJjiTnTe3/AUBu73ZvQSOZJgiXvbTeDwzmjCSEXXLZMJGfpkUgG8YctEhTZr/PhsRcK/q/10BmlX+p9VlI8OPadWGd8JssBuAyKovu2//RimiEBrY1vg7MMD7yI8SBuy+F0A2S1QC3Mgd3H0KISrTBDX2W64vwPsyJpO6I9l1zgEj+xioB7B+0ao1I5NNQbu+eMbCB3fUPMWFaaZ8IQAy7kM3OBAWIDvOwuDRjhnwj9kFxDu2YTtnblTcSmTe4LVcy8/EmguMgn2jhfDNzhYGLS7YdPgaBmbZg8ipZkHqjH+4UNIjQBBTjYHn9aP/SA4qCWSnTfETo4U/X6dRl6SM8KRpqu2A=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTcxMDg0OTU1MDkyMywKICAicHJvZmlsZUlkIiA6ICJmNzcxMDI1NGMzYWY0YjA5YmRjY2NiNDRjNjg1NjFiMCIsCiAgInByb2ZpbGVOYW1lIiA6ICJDZXJ1c1YyIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2FmODg0OTBlMjBiYTM0NzJlNDJiYTk0NjM2NmRjMWNkZDFkYTUyNzY0MTQ3MGM4NzZiMTk3NGI4NjZiZTM3OGUiCiAgICB9CiAgfQp9";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-32.5, 70, -75.5, 165, 0);
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
