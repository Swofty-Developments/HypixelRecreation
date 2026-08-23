package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.questlog;

import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.gui.inventory.ItemStacks;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.MissionSet;
import net.swofty.type.skyblockgeneric.mission.SkyBlockMission;
import net.swofty.type.skyblockgeneric.mission.SkyBlockProgressMission;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.user.fairysouls.FairySoul;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public class GUIMissionLog extends StatelessView {
    private static final int[] MISSION_SLOTS = {
            11, 12, 13, 14, 15, 16,
            19, 20, 21, 22, 23, 24, 25,
            28, 29, 30, 31, 32, 33, 34,
            37, 38, 39, 40, 41, 42, 43
    };

    private final boolean showCompleted;

    public GUIMissionLog() {
        this(false);
    }

    public GUIMissionLog(boolean showCompleted) {
        this.showCompleted = showCompleted;
    }

    @Override
    public ViewConfiguration<DefaultState> configuration() {
        return new ViewConfiguration<>(Text.key("gui_sbmenu.questlog.title", suffix()), InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        layout.slot(4, (s, c) -> ItemStacks.item(Material.WRITABLE_BOOK, 1,
                Text.key("gui_sbmenu.questlog.info", suffix()),
                Text.keyLines("gui_sbmenu.questlog.info.lore")));

        // Fairy Souls
        layout.slot(10, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            return ItemStacks.head("b96923ad247310007f6ae5d326d847ad53864cf16c3565a181dc8e6b20be2387",
                    Text.key("gui_sbmenu.questlog.fairy_souls"),
                    Text.keyLines("gui_sbmenu.questlog.fairy_souls.lore",
                            player.getFairySoulHandler().getTotalFoundFairySouls(),
                            FairySoul.getFairySouls().size()));
        }, (_, c) -> {
            c.push(new GUIFairySoulsGuide());
        });

        // Toggle completed/ongoing
        if (showCompleted) {
            layout.slot(50, (s, c) -> ItemStacks.item(Material.BOOK, 1,
                            Text.key("gui_sbmenu.questlog.ongoing_quests"),
                            Text.keyLines("gui_sbmenu.questlog.ongoing_quests.lore")),
                    (click, c) -> c.replace(new GUIMissionLog(false)));
        } else {
            layout.slot(50, (s, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                return ItemStacks.item(Material.BOOK, 1,
                        Text.key("gui_sbmenu.questlog.completed_quests"),
                        Text.keyLines("gui_sbmenu.questlog.completed_quests.lore",
                                player.getMissionData().getCompletedMissions().size()));
            }, (_, c) -> c.replace(new GUIMissionLog(true)));
        }

        SkyBlockPlayer player = (SkyBlockPlayer) ctx.player();
        MissionData missionData = player.getMissionData();

        List<MissionSet> completedMissions = new ArrayList<>();
        List<MissionSet> activeMissions = new ArrayList<>();

        for (MissionSet set : MissionSet.values()) {
            boolean completedSet = true;
            for (Class<? extends SkyBlockMission> mission : set.getMissions()) {
                if (missionData.getMission(mission) == null || !missionData.getMission(mission).getValue()) {
                    completedSet = false;
                    break;
                }
            }
            if (completedSet) {
                completedMissions.add(set);
            } else {
                activeMissions.add(set);
            }
        }

        List<MissionSet> toShow = showCompleted ? completedMissions : activeMissions;

        // Clear mission slots
        for (int missionSlot : MISSION_SLOTS) {
            layout.slot(missionSlot, ItemStack.AIR.builder());
        }

        for (int i = 0; i < toShow.size() && i < MISSION_SLOTS.length; i++) {
            MissionSet missionSet = toShow.get(i);
            int slot = MISSION_SLOTS[i];

            layout.slot(slot, (s, c) -> {
                SkyBlockPlayer p = (SkyBlockPlayer) c.player();
                MissionData data = p.getMissionData();
                List<Text> lore = new ArrayList<>();
                lore.add(Text.of("<7> "));

                Arrays.stream(missionSet.getMissions()).forEach(mission -> {
                    Map.Entry<MissionData.ActiveMission, Boolean> activeMission = data.getMission(mission);

                    if (activeMission == null) {
                        try {
                            lore.add(Text.of(" <c>✖<e> {}.", mission.newInstance().getName()));
                        } catch (InstantiationException | IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                        return;
                    }

                    SkyBlockMission skyBlockMission = MissionData.getMissionClass(activeMission.getKey().getMissionID());
                    SkyBlockProgressMission progressMission = data.getAsProgressMission(skyBlockMission.getID());

                    if (progressMission == null) {
                        lore.add(Text.of(activeMission.getValue() ? " <a>✔<f> {}." : " <c>✖<e> {}.",
                                skyBlockMission.getName()));
                    } else {
                        lore.add(Text.of(activeMission.getValue()
                                        ? " <a>✔<f> {}. <7>(<b>{}<7>/<b>{})"
                                        : " <c>✖<e> {}. <7>(<b>{}<7>/<b>{})",
                                skyBlockMission.getName(),
                                activeMission.getKey().getMissionProgress(),
                                progressMission.getMaxProgress()));
                    }
                });

                lore.add(Text.of("<7> "));
                Map.Entry<MissionData.ActiveMission, Boolean> firstMissionInSetEntry = data.getMission(missionSet.getMissions()[0]);
                if (firstMissionInSetEntry != null) {
                    MissionData.ActiveMission firstMissionInSet = firstMissionInSetEntry.getKey();

                    lore.add(Text.key("gui_sbmenu.questlog.started"));
                    lore.add(Text.of("<f>  {} {}",
                            SkyBlockCalendar.getMonthName(SkyBlockCalendar.getMonth(firstMissionInSet.getMissionStarted())),
                            StringUtility.ntify(SkyBlockCalendar.getDay(firstMissionInSet.getMissionStarted()))));
                    lore.add(Text.of("<7>  {}", SkyBlockCalendar.getDisplay(firstMissionInSet.getMissionStarted())));

                    if (showCompleted) {
                        lore.add(Text.of("<7> "));
                        lore.add(Text.key("gui_sbmenu.questlog.completed"));
                        lore.add(Text.of("<f>  {} {}",
                                SkyBlockCalendar.getMonthName(SkyBlockCalendar.getMonth(firstMissionInSet.getMissionEnded())),
                                StringUtility.ntify(SkyBlockCalendar.getDay(firstMissionInSet.getMissionEnded()))));
                        lore.add(Text.of("<7>  {}", SkyBlockCalendar.getDisplay(firstMissionInSet.getMissionEnded())));
                    }
                } else {
                    lore.add(Text.key("gui_sbmenu.questlog.not_started"));
                }

                return ItemStacks.enchanted(ItemStacks.item(Material.PAPER, 1,
                        Text.of("<a>{}", StringUtility.toNormalCase(missionSet.name())), lore));
            });
        }
    }

    private String suffix() {
        return showCompleted ? "(Completed)" : "";
    }
}
