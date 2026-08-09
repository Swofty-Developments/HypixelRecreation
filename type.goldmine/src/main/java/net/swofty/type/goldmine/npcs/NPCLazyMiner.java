package net.swofty.type.goldmine.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.missions.goldmine.lazyminer.MissionFindLazyMinerPickaxe;
import net.swofty.type.skyblockgeneric.mission.missions.goldmine.lazyminer.MissionTalkToLazyMiner;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.stream.Stream;

public class NPCLazyMiner extends HypixelNPC {

    public NPCLazyMiner() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Lazy Miner", "§e§lCLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "DEPpEYXbbm6SQmo0h40eZFei7e+qtBqzZXDcRrPI5L2Yb4AFHQtalJrqqR4YlOv9NtOuBg+MDIMna+KFBSldUA0fMklitAk3GRl7Y4mWpAQM9BuOGGuWnqf2wVci5hZSiMtL2pSC4ogln0urAdm5qsB224RfbfnpiEWrm8BTHFXfqaMkEZNah+dxeszTMl1TytWYePAFtolMw/MdyuTGqApSpgk9nWoPaUAgGVWEo0mlrflEdmX0DkR98BKpqeg1naN8C+SbleTBJe8ZYqKRNMqD5bIJWc6J6cjGEeb7TbR+ZMipk3cMRu43HrcRdPAlmxihezTlxeDi1RoeObDSZXNSGtMhDxEWU1bOBAgohT8qM2x9AChAGL2kphq3EJplMlw45a9KbZj+/R68QmcEPY+TNo0BPshFoj8sh+RxZPdrirjM76G6TyVYJNIScu0JQ74WqYh/hpjc75rOldORlS9Aat5IXsDnrjCYi4l37DH2bBB6C+gk5BgGqnce/1QF0VESfJWtmS9gHSPBNZHa9ave2rM0ou2hpEw1LgW27hGWqZfGg0sfU9deQvLYyiRFsAXww22L5xmNJnOmvn3L1lDdGNpT63qXknp9NvAABOCPpBV0zeSCmHlDYbAN19sEkthnnIGk1BwZhsaZizf09gGAlMdLgcGYQKzfJJTODWo=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1NjAxOTM2MzQxNTQsInByb2ZpbGVJZCI6ImEyZjgzNDU5NWM4OTRhMjdhZGQzMDQ5NzE2Y2E5MTBjIiwicHJvZmlsZU5hbWUiOiJiUHVuY2giLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Q5M2Q5NjM2YWE5NTU2YWNmNGJlMmNjNThkOTNmZjBiNTMyZTY4MDk4ZjgwYTdkYzE1MWEwZjFkMTZiOGU1ZjgifX19";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-11.50, 78.00, -337.50, 40, 0);
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
        if (isInDialogue(player)) return;
        MissionData data = player.getMissionData();

        // Check if mission is completed - show idle dialogue
        if (data.hasCompleted(MissionTalkToLazyMiner.class)) {
            if (player.getSkills().getCurrentLevel(SkillCategories.MINING) < 5) {
                setGardenDialogue(player, "not-reached-deep-caverns");
                return;
            }
            setGardenDialogue(player, "idle");
            return;
        }

        // Check if player needs to return the pickaxe (MissionTalkToLazyMiner is active)
        if (data.isCurrentlyActive(MissionTalkToLazyMiner.class)) {
            setGardenDialogue(player, "quest-complete").thenRun(() -> {
                data.endMission(MissionTalkToLazyMiner.class);
            });
            return;
        }

        // Check if player is looking for the pickaxe (MissionFindLazyMinerPickaxe is active)
        if (data.isCurrentlyActive(MissionFindLazyMinerPickaxe.class)) {
            setGardenDialogue(player, "no-pickaxe-found");
            return;
        }

        // First interaction - check special case where player already found pickaxe
        boolean hasFoundPickaxe = player.getMissionData().hasCompleted(MissionFindLazyMinerPickaxe.class);
        if (hasFoundPickaxe) {
            // Player found the pickaxe before talking to Lazy Miner - skip to talk mission
            setGardenDialogue(player, "found-pick-intro").thenRun(() -> {
                player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_LAZY_MINER, true);
                data.startMission(MissionTalkToLazyMiner.class);
            });
            return;
        }

        // Normal first interaction - start the quest
        setGardenDialogue(player, "first-interaction").thenRun(() -> {
            player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_LAZY_MINER, true);
            data.startMission(MissionFindLazyMinerPickaxe.class);
        });
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return Stream.of(
                DialogueSet.builder()
                        .key("first-interaction").lines(new String[]{
                                "Whoops! I lost my good pickaxe again in the mines! That's why I always come prepared with a backup!",
                                "It's probably down in the mine somewhere. Can you go find it?"
                        }).build(),
                DialogueSet.builder()
                        .key("no-pickaxe-found").lines(new String[]{
                                "Find my pickaxe in the Gold Mines! I'm not going back down there..."
                        }).build(),
                DialogueSet.builder()
                        .key("found-pick-intro").lines(new String[]{
                                "Oh! You already found my pickaxe! That's amazing!",
                                "Keep it, it has a special enchantment. Come talk to me when you're ready!"
                        }).build(),
                DialogueSet.builder()
                        .key("quest-complete").lines(new String[]{
                                "You found it! Thank you so much!",
                                "Keep it as a reward. That pickaxe has Smelting Touch - it automatically smelts ores into ingots!",
                                "It'll be very useful for collecting iron and gold."
                        }).build(),
                DialogueSet.builder()
                        .key("not-reached-deep-caverns").lines(new String[]{
                                "The Deep Caverns are full of strange creatures and expensive treasures.",
                                "Reach Mining Level V to gain access!",
                                "Have you spelunked the Deep Caverns?",
                                "They are full of bountiful treasures, but watch out for those Lapis Zombies!"
                        }).build(),
                DialogueSet.builder()
                        .key("idle").lines(new String[]{
                                SkyBlockCalendar.getMonthName() + " " + SkyBlockCalendar.getDay() + " is my resting day!",
                                "I'd go mining but I'm too lazy.",
                                "Maybe some day I'll go back down there."
                        }).build()
        ).toArray(DialogueSet[]::new);
    }

    private java.util.concurrent.CompletableFuture<Void> setGardenDialogue(HypixelPlayer player, String key) {
        return setDialogue(player, key).thenRun(() -> {
            if (player instanceof net.swofty.type.skyblockgeneric.user.SkyBlockPlayer skyBlockPlayer) {
                net.swofty.type.skyblockgeneric.garden.progression.GardenProgressionSupport.apply(
                    skyBlockPlayer,
                    net.swofty.type.skyblockgeneric.garden.progression.GardenProgressionReward.spokenNpc("LAZY_MINER")
                );
            }
        });
    }
}
