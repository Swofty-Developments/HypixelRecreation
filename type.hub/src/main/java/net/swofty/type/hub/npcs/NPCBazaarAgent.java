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

public class NPCBazaarAgent extends HypixelNPC {
    public NPCBazaarAgent() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<6>Bazaar Agent", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "Uw6+Tq5MobvryAxcWBW3NTVjq/GShE7gSTmg2BPlIJVJohDfKx2ZoLf4TnSTeG5GKwX/EjdiwkqS8RmS/8XkxVIDVGbNFHeeOBc6b7cs0nkwo82t0Fr/GNJq2B/kCffLBnVNAUNS0G6A83voThbRVqeFzL5mldFZatPO80aaHZtnoQfYlzpSBA4QW6+D79FFyRrbiNyJTvH+24nrJLG62rgiLs5wcBBeStDOIpal0X8mAnBrI68lOsvHXHmwSBxkQdOm39ppO/L5C7m3IFbvmaELUepSqVOzaqOz+uBNSSkPWUW7vvEuU5spYeSQmcfkX8I161j9ouIWCSejDTRNt9DxIsAgYSX+py0LMqYXn/l004yyVs7/67Bg7bqVtfrincfZqKbn3Azvq+6tgYrKkyF4hNqNpTqb9cAAXuSvoHGMzdqIh7LCH2pxwwz+koe0kbRzNyMCFf/BuRgl6litdUZD+atDOzjWOuQIP4EctL8QqIFobBE0cXQfsuSbL246PiQHSq+vneVdkUGfIzTmuPGTnJUOESpeiQs3HyT1s0gckmsJg8qg+/SMo1lEOFITkY6GIvhF+Sc2a9w5RfCYBkONm1KDaj211nKAswwKgAChpdP4tpKyDrkcrwkAwS/rkZPjy5xLXKdRXsf24DA/VJkKzg/Y6SoAr9A+OcyVxhQ=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTU5NzMwNjQwOTk1NSwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNmM5NDUzYjU4OWUxNzc2NzBkODZmM2U1ZDRmMTcxMzEzM2FkNzkyYmY4YzU0ZmE2MWQwN2I1MDQ0ZDU3ZGI5OSIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-35.5, 73, -31.5, 0, 0);
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
