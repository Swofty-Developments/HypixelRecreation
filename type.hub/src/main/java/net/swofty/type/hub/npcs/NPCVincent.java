package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.item.Material;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.garden.progression.GardenProgressionReward;
import net.swofty.type.skyblockgeneric.garden.progression.GardenProgressionSupport;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCVincent extends HypixelNPC {

    public NPCVincent() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"§bVincent", "§e§lCLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "l9pYvAwAs6liRRBKySCecGDU8zERatdYPFOpx2s/kc9daZ67hwUe6MIHojxyu0+dtyhwAPf18pBEvR58HvZjpD1OWfkDQtPrVJscyzAiJdZVfze7kxsUHOIA9I9x6Dd3mUSukyZLDpmH6nNVetKn2W82nwOPBxT8wXpVIYizZGSGFrl6AXW1UUiWr6sJI0+7yGhph5q+QtKFGrfp+FNd7IwahSOAuk41xfdu4cze5CLk35QMWvKW3V2ItX7KOkJv1kooAnzYXWfMoiM0FT+liY+OOWUI8eAgPngCMmQN7+W6W7gu5Aea2TM2mDKufG/52H7CiagrSDKk/jlxlP9/1QeR09uchAjbOibSFxU1OFqGmcIyfUN1cq19WV0C5YQd+6AbyC3Hblde+QzjsyNLe7R3KvMpB5zlYWyb84uoRi2ogWuifE6rswoxBWJgtUCMBf/MKu5XzW6eoOpwNGLt5qHtr9QyxeqPp9XbBgn6syfAlTwqHx7utf+gu3SSBZbNe6cf2OE/ngxsPxDri3Tt3M9NTdS485Ja4Ic9JZkpz6I7GsaG+3nOClRWX16eseGTDgXGpcJ3OUZz25obuHWEQScqq7oI1XH8KnVENPhzy8VZ4vhZVUZBPgjnsVcE7K5q9Trbk75C1zJAaplM+XrQs2Cr1Ig2HnUn3Vy2ixWkRGY=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTcxNzYyMDA2OTUyOSwKICAicHJvZmlsZUlkIiA6ICIwMzQ2N2E0Yzc5ZGU0ZGM5YTQ4NzU5MGY5NmEwODFmMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJTaGFrYVlhbWkiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOWJhM2M0ZjU4MmZjODc0OWUzZDhlMzVlZjFlNjMyODBjNDc1MmU0YWM4M2FhYjY5ZjY3NGRhYzI0YTM3NTgwMyIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(79.5, 74, 53.5, 0, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        SkyBlockItem heldItem = new SkyBlockItem(player.getItemInMainHand());
        ItemType heldType = heldItem.getAttributeHandler().getPotentialType();
        boolean dyeItem = heldType != null
            ? heldType.name().endsWith("_DYE")
            : heldItem.getMaterial().name().endsWith("_DYE") || heldItem.getMaterial() == Material.INK_SAC;
        if (dyeItem) {
            if (heldType != null) {
                player.takeItem(heldType, 1);
            }
            GardenProgressionSupport.apply(player, GardenProgressionReward.donatedItem("DYE"));
            sendNPCMessage(player, "A lovely color choice. I'll remember your donation.");
            return;
        }
        sendNPCMessage(player, "Bring me any dye if you want to trade colors with the Garden.");
    }
}
