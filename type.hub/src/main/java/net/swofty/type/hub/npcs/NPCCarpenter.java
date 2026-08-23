package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.missions.MissionGiveWoolToCarpenter;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.stream.Stream;

public class NPCCarpenter extends HypixelNPC {

    public NPCCarpenter() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Carpenter", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "XM0830BVKJKAtHvafFdC6JikuXLGj5A9alrAuF6s3IYw1zidmOI4EMsIaCEZpwuEApoGEOgorc1H5r5vo0YSeeXZNNPBTN6s7pG1mn8PqMxXGOEN4yXVnq13YEohZdNPFLA9lbDGnnGcWIncBb6ggzphtDfNDFA7hrqVEwzDcnej9RxfxmMtvH0k1fS8TKAsspclyjlparSQu3EdWQRFlW5RjFxGtrvTttvOJGUuJmMnIFbRZWfdwi1kPgrYa6/H34fRra57ktOOnCIz2iCs98KGi2kqQpOSozCwo7ykHdVHFRu2iBLE0gp2UmKp9aou6Kx/GyXAOd3HQJ5wMuJBfFurZAc47zvJDz1OP4lZtjNZjdxvlZAc14aWI7Guc/a/neeQH9B2XYJa/EdOcritdHLRh02AXbjKOkoBHs487YaWNXjMdx9eEJ64DxfEWFNa91g9776giQrkeKIove4A1HsiFl/5raST/N5u1/x1OF8z7WG7bzbQznMQVmTKYQKCz/f09EeEC0yP7nQFBevIkZ2CRC77TdN9EMOgQb8whe6MxzS9c1oaSA4lPjho/O1aVV5h0UVz7dHpZF0bsISdVfuZVc35xLOzUoGaYiegoo18GjmUtRxyfcQl1CVyhpZDt8JLau4562F1G7cPlxrW0mAt4WvENHCg6TFVx0O3RxM=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1NjM4MjIyNTY3OTQsInByb2ZpbGVJZCI6ImEyZjgzNDU5NWM4OTRhMjdhZGQzMDQ5NzE2Y2E5MTBjIiwicHJvZmlsZU5hbWUiOiJiUHVuY2giLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2NjOGQyYTVhZjk3NWM1M2NmMDgxYjUzMTU2NjE0OGJhZTM2NjE0MTMxNGIzNWFhNTcyNmU5N2NjOTdlZjExOGIifX19";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-137.5, 74, -41.5, -11, 0);
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
        MissionData data = player.getMissionData();

        if (!data.isCurrentlyActive(MissionGiveWoolToCarpenter.class) && !data.hasCompleted(MissionGiveWoolToCarpenter.class)) {
            setDialogue(player, "initial-hello").thenRun(() -> {
                data.startMission(MissionGiveWoolToCarpenter.class);
            });
            return;
        }
        if (!data.hasCompleted(MissionGiveWoolToCarpenter.class) && data.isCurrentlyActive(MissionGiveWoolToCarpenter.class)) {
            if (player.getAmountInInventory(ItemType.WHITE_WOOL) >= 64) {
                player.takeItem(ItemType.WHITE_WOOL, 64);
                setDialogue(player, "completed-quest").thenRun(() -> {
                    data.endMission(MissionGiveWoolToCarpenter.class);
                });
            } else {
                player.sendMessage("Come back with a stack of White Wool!");
            }
            return;
        }
        if (data.hasCompleted(MissionGiveWoolToCarpenter.class)) {
            setDialogue(player, "spoke-again");
        }
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return Stream.of(
                DialogueSet.builder()
                        .key("initial-hello").line("Hi, {}! Welcome to the <a>Furniture Shop<f>.", player.getUsername())
                                .line("Sales are too good right now, I can't keep up with the demand!")
                                .line("Could you bring a <a>stack of White Wool<f> to help replenish my stock?")
                                .line("Sheep over in The Barn drop wool, but you can also purchase it from the <d>Wool Weaver<f>.")
                                .line("She lives in a house not far from here - it's over by the water fountain.").build(),
                DialogueSet.builder()
                        .key("completed-quest").lines(
                                "Wow, thanks so much for the help!",
                                "Carpentry is my passion, I always love to teach others.",
                                "Here's the recipe for the <a>Carpentry Table<f>. You can place it in your world and craft furniture that you've unlocked!",
                                "You can now gain Carpentry XP by crafting items. Leveling your <a>Carpentry Skill<f> unlocks new furniture recipes!",
                                "Some furniture is available exclusively in the Furniture Shop downstairs. Check it out!"
                        ).build(),
                DialogueSet.builder()
                        .key("spoke-again").lines(
                                "Check out the Furniture Shop downstairs!",
                                "The Furniture Shop is downstairs. Purchase cool furniture down there!"
                        ).build()
        ).toArray(DialogueSet[]::new);
    }
}
