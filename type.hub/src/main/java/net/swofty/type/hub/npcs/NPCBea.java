package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.hub.gui.GUIShopBea;

import java.util.stream.Stream;

public class NPCBea extends HypixelNPC {

    public NPCBea() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Bea", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "KKbZH4W3Qmx57D3vaF30MdHzxB2hryzgSNffNssfSo2lo1VBjLWM6bpyY6BYGbKM5Yi+6C0RA33IKAHGNeARhIr/tF52pUEzMc4Jp93CGxY7tUd9yQyW5EKw87aWzoriIJFVlcN8DuC4ZOujKFlPSVAPGXQE74PchoYBhi5VvBuO2GvGc0JZt4Z9aEVJIpG5yMv/ZhcxKKBVai8w4sZnj60lVblXcyznJ7lQvRgavyCesp8pcUtMMwqzblcdN3tJKN2VMMxqOI4+F/JxBKP5LmqF7/XgqGjf6avjQGxYsUvfMa2NjDz7lg/TqV91GOPBPDsdCHt6v+ybfdveohYrmAkdgKKeZOtFD8SZsAtiSjeWMdCTvw9ix9aCk/MTI1uNqhLM/cydXEVVOO3HrfweL//A+agXe2/tYDBxBB2VvyRIN5j+s7HZ36+UIdzl+WgzqrgUmOVf8AyGRUDYDt8kFZRQeLlm+49UJgs12DG5PhNEa+x9iwfODHG/RBcys/0NP8q7KyEAFH0bv+xmSpgcGp0HxIKeRujSkHvBQpJ+kI/HT1l3vlEce6fIE0XIF1UOwJV/0cuaBqDlso7PXq8LJyOg99TosoalUg4/IGh1fqQgkURfbmtzUFtWJqzwQIypVYLPlhsU8rxa0GBZrk7NrXT+JqrbGrXrRif7UPHjJiE=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1ODIzMDAzNTg3ODYsInByb2ZpbGVJZCI6IjQxZDNhYmMyZDc0OTQwMGM5MDkwZDU0MzRkMDM4MzFiIiwicHJvZmlsZU5hbWUiOiJNZWdha2xvb24iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2UzYzM0YjllNTFmMzUyZDZjNjljODIxYTFmYzI5ZjUwMDUwOTgwM2ExZWVjMDI0YzM4NzAxNWI1YzQzYzc3M2EiLCJtZXRhZGF0YSI6eyJtb2RlbCI6InNsaW0ifX19fQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(11.5, 72, -58.5, -76, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        if (isInDialogue(e.player())) return;
        DatapointToggles.Toggles toggle = e.player().getToggles();

        if (!toggle.get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_BEA)) {
            setDialogue(e.player(), "hello");
            toggle.set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_BEA, true);
            return;
        }

        e.player().openView(new GUIShopBea());
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return Stream.of(
                DialogueSet.builder()
                        .key("hello").lines(
                                "Hello! Do you have a pet?",
                                "Pets are little companions for your adventures in SkyBlock!",
                                "Personally, I prefer the <e>bee <f>pet!"
                        ).build()
        ).toArray(DialogueSet[]::new);
    }
}
