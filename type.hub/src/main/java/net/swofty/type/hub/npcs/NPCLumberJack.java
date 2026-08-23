package net.swofty.type.hub.npcs;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.NPCOption;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.gui.inventories.ClaimRewardView;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.missions.lumber.MissionBreakOaklog;
import net.swofty.type.skyblockgeneric.mission.missions.lumber.MissionTalkToLumberjack;
import net.swofty.type.skyblockgeneric.mission.missions.lumber.MissionTalkToLumberjackAgain;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class NPCLumberJack extends HypixelNPC {

    public NPCLumberJack() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<2>Lumber Jack", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "cxEDP/ZmKhHdhmmHUgbqSxSgoD8w/wQk3IysGKDggfZUR3tsd1BFqpwsE37pv1LUsUkHr+mF0R6aAC0ZWQzT5kMT1546s5JAyRPFNa4P9iScE+aWlPCK1iqw2tMDqD73hYRtlSKuDRlxjbKXbHBltAsZYF1KEHh7uy+WJn+kUTuZI9Z96AUDkZlx/zxNO7Biq4pxZyilU6f2UId58fAkJYbMOYzxwCCOP+4NsH4vZazZ5YRwjpBnxrgI6epBe4KczoEqQZOo5lCEYrDMeBBjtcO9ktRnR+ddhTkPwZFHMqJaONOaeUWPar4/G977gwDwOkC0y5yw1hCABU66OiY1OsmshE9s5r8MOHYVFdwacp8M1f6xMmydmkl/VB8tCDsUMQIFlXHfF1YyNJ6UZTOhxyqI2ntDfVxcN5EMEC4c0lsycp8k5wg41aTlksvzwSQ1H+QkRByo1xG7WIZNr90mkJzwUG7Ggr3q+DZZeo8gQKMRTYIa5EBtiSTugMPcv4pw3xqv23E2kB5PvmumHGg4jh0VwE7CgFPD3f3SIZsT6bCKxrQ1pq1XQlAKsJbu341l2j01UkPG1GThjTGBTwnwV+vZUA+hG/2c/wyfbfqquJBH5PqaM7ZvzvyGEVMmh8wU663dBNkXQHoW3/Y/yg1eLPxwm9aSDffWMJVaGfXFuq4=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1NTA2Nzg1MjAzNzMsInByb2ZpbGVJZCI6ImEyZjgzNDU5NWM4OTRhMjdhZGQzMDQ5NzE2Y2E5MTBjIiwicHJvZmlsZU5hbWUiOiJiUHVuY2giLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzY4MjUwMzBlNzc1MmZhZjdmODE2YWMyYTIwYjExNDMyYWY0YjJjZGU0OTMyZGMwZTkyMDgzMWU2MjE4MDhlZTgifX19";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-123.5, 73, -29.5, -89, 0);
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

        if (data.isCurrentlyActive(MissionTalkToLumberjack.class)) {
            setDialogue(player, "initial-hello").thenRun(() -> {
                NPCOption.sendOption(player, "lumber_jack", true, List.of(
                        new NPCOption.Option(
                                "r_2_1",
                                NamedTextColor.GREEN,
								false,
                                "Sure",
                                (p) -> {
                                    setDialogue(p, "option-accept").thenRun(() -> {
										data.endMission(MissionTalkToLumberjack.class);
                                    });
                                }
                        ),
                        new NPCOption.Option(
                                "r_2_2",
                                NamedTextColor.RED,
								false,
                                "Nah, I'm good",
                                (p) -> {
                                    setDialogue(player, "option-nah");
                                }
                        )
                ));
            });
            return;
        }
        if (!data.hasCompleted(MissionBreakOaklog.class)) {
            sendNPCMessage(player, "Please collect and then bring to me <a>20 Oak Logs<f>! You can get 'em from the <b>Forest <f>just off the trail.");
            return;
        }
        if (!data.hasCompleted(MissionTalkToLumberjackAgain.class)) {
            setDialogue(player, "spoke-again").thenRun(() -> {
				player.openView(new ClaimRewardView(), new ClaimRewardView.State(ItemType.PROMISING_AXE, () -> {
					data.endMission(MissionTalkToLumberjackAgain.class);
				}));
            });
            return;
        }
		setDialogue(player, "idle-" + (1 + (int)(Math.random() * 3)));
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return List.of(
				DialogueSet.builder()
						.key("initial-hello").lines(
								"Timber!",
								"My woodcutting assistant has fallen quite ill! Do you think you could take over for him?",
								"I just need you to chop down some Logs. If you do, I'll even give you his old axe as a reward!",
								"Do you think you could take over for him?"
						).build(),
				DialogueSet.builder().key("option-nah").lines(
						"Oh okay... maybe come back later!"
				).build(),
				DialogueSet.builder().key("option-accept").lines(
						"Cheers!, Let me tell ya a little about what we're going to be doing.",
						"We will be <a>Foraging<f>! As one of SkyBlock's core skills, Foraging involves cutting down lots of different types of trees!",
						"As you do this, you'll unlock various crafts and useful stat boots that will help in other areas of your adventure.",
						"Here, let me unlock the <a>Foraging skill <f>for you, and then you can get started!",
						"To get you started, please bring me <a>20 Oak Logs<f>! You can break the trees just off the trail to get 'em",
						"<f>Use your fists for now - I'll give you my previous assistant's axe if you can prove your worth!"
				).build(),
				DialogueSet.builder()
						.key("spoke-again").lines(
								"Nice job! In return for those logs, I'll give ya this <a>Promising Axe<f>.",
								"It's far less painful than using your fists to punch trees, I'll tell ya that much!",
								"If you feel like Foraging is the thing for you, continue down the path and head towards <a>The Park<f>.",
								"My friend <e>Charlie <f>can be found there. I'm sure he'll have some work for you!"
						).build(),
				DialogueSet.builder()
						.key("idl-1").lines(
								"My mate <d>Charlie <f>has a <5>Treecapitator<f>! With it, he can break down entire trees in a single swipe!"
						).build(),
				DialogueSet.builder()
						.key("idle-2").lines(
								"Have you spoken to my mate <e>Charlie <f>over in <a>The Park <f>yet? He could probably use your help."
						).build(),
				DialogueSet.builder()
						.key("idle-3").lines(
								"Have you enchanted your axe with <a>Efficiency V<f>? It'll help ya break stuff a lot faster."
						).build()
		).toArray(DialogueSet[]::new);
    }
}
