package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.user.categories.Rank;
import net.swofty.type.skyblockgeneric.bazaar.BazaarCategories;
import net.swofty.type.skyblockgeneric.gui.inventories.bazaar.GUIBazaar;
import net.swofty.type.skyblockgeneric.gui.inventories.bazaar.GUISpecialBazaar;
import net.swofty.type.skyblockgeneric.levels.SkyBlockLevelRequirement;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.stream.Stream;

public class NPCBazaar extends HypixelNPC {
    public NPCBazaar() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<6>Bazaar", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "FDFrRosEo3GRN1QUX2XDIxxB9cWznwE1Nt6zoDCUL9Ya0sudiOMey3r0wL+qgKNItbDgeflDpTwlpA1JBWbfQWWVCRRQhsN6HWPAyTqFMXyy8skaR8UMgr6My8Xz6kcWIfv3g6toUe1sowoKDBXt9z3hn4j6qiARxMOb1nSSy1Cp19di4rYOIFa7Ibu5DNNKAo0bafPYA3Mexy1DYpkJ9FFO6wyW/3U30jPCTnbysZp6XJN0scnXQcoLeBw5wy0V/NI/C7TNJKhr7YWlZKqVKW8r1kyrGgkTvC1u1AWBj3PFV3KuIlhX+G7VUD8iCvz8hvwJVRJBPlsMT6CQ5sP0eCHs38YoN9kiHtO+gHElHzp0JctQXX/7eYXV1FCMGJ8ov+u9f9V/Xu9HEdjCxwdjrRS7I/FSy5/GuBOHY+G2YIVKzMsCTkOM+F52WWF+O6/mGTo6NAdgvJb0Wvvif6/edHbUucOp2OtH67XGD61p/ktg/DmHNoXvjDCD0ld1HLO24fZrdm/cuC85/VYrEb6m9NvFZZVIoLbjbwSFuZD7AyGvHiFVdBWa9Ps3IpxiKi8lroyW8D4VLEQteN/BoB2DHTvu+jEMFJK4W+X7MG0pPAQz5F+1JAaWufR6ZH6Jrx/r4+1gjZlWzV6tmv4OXQHtDnaY0HCRvB+srNfQ/c1UZt8=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1NzMyMjM2NDc4NDcsInByb2ZpbGVJZCI6IjQxZDNhYmMyZDc0OTQwMGM5MDkwZDU0MzRkMDM4MzFiIiwicHJvZmlsZU5hbWUiOiJNZWdha2xvb24iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2MyMzJlMzgyMDg5NzQyOTE1NzYxOWIwZWUwOTlmZWMwNjI4ZjYwMmZmZjEyYjY5NWRlNTRhZWYxMWQ5MjNhZDcifX19";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-33.5, 73, -22.5, -180, 0);
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
        SkyBlockLevelRequirement lvl = player.getSkyBlockExperience().getLevel();
        if (lvl.asInt() >= 7 || player.getRank().isEqualOrHigherThan(Rank.STAFF)) {
            if (player.isIronman()) player.openView(new GUISpecialBazaar());
            else new GUIBazaar(BazaarCategories.FARMING).open(player);
            return;
        }
        setDialogue(player, "hello");
    }
    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return Stream.of(
                DialogueSet.builder()
                        .key("hello").lines(
                                "<c>You need SkyBlock Level 7 to access this feature!"
                        ).build()
        ).toArray(DialogueSet[]::new);
    }
}
