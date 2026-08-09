package net.swofty.type.skyblockgeneric.gui.inventories.sbmenu.questlog;

import net.kyori.adventure.text.Component;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.commons.StringUtility;
import net.swofty.type.generic.gui.inventory.ItemStackCreator;
import net.swofty.type.generic.gui.inventory.TranslatableItemStackCreator;
import net.swofty.type.generic.gui.v2.Components;
import net.swofty.type.generic.gui.v2.DefaultState;
import net.swofty.type.generic.gui.v2.StatelessView;
import net.swofty.type.generic.gui.v2.ViewConfiguration;
import net.swofty.type.generic.gui.v2.ViewLayout;
import net.swofty.type.generic.gui.v2.context.ViewContext;
import net.swofty.type.generic.i18n.I18n;
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
import java.util.Locale;
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
        return ViewConfiguration.withString(
            (state, ctx) -> I18n.string("gui_sbmenu.questlog.title", ctx.player().getLocale(), Component.text(showCompleted ? "(Completed)" : "")),
                InventoryType.CHEST_6_ROW);
    }

    @Override
    public void layout(ViewLayout<DefaultState> layout, DefaultState state, ViewContext ctx) {
        Components.fill(layout);
        Components.close(layout, 49);
        Components.back(layout, 48, ctx);

        layout.slot(4, (s, c) -> {
            Locale l = c.player().getLocale();
            return ItemStackCreator.getStack(I18n.string("gui_sbmenu.questlog.info", l, Component.text(showCompleted ? "(Completed)" : "")),
                Material.WRITABLE_BOOK, 1, I18n.iterable("gui_sbmenu.questlog.info.lore"));
        });

        // Fairy Souls
        layout.slot(10, (s, c) -> {
            SkyBlockPlayer player = (SkyBlockPlayer) c.player();
            return TranslatableItemStackCreator.getStackHead("gui_sbmenu.questlog.fairy_souls",
                    "b96923ad247310007f6ae5d326d847ad53864cf16c3565a181dc8e6b20be2387", 1,
                "gui_sbmenu.questlog.fairy_souls.lore", Component.text(String.valueOf(player.getFairySoulHandler().getTotalFoundFairySouls())), Component.text(String.valueOf(FairySoul.getFairySouls().size())));
        }, (_, c) -> {
            c.push(new GUIFairySoulsGuide());
        });

        // Toggle completed/ongoing
        if (showCompleted) {
            layout.slot(50, (s, c) -> TranslatableItemStackCreator.getStack("gui_sbmenu.questlog.ongoing_quests", Material.BOOK, 1,
                            "gui_sbmenu.questlog.ongoing_quests.lore"),
                    (click, c) -> c.replace(new GUIMissionLog(false)));
        } else {
            layout.slot(50, (s, c) -> {
                SkyBlockPlayer player = (SkyBlockPlayer) c.player();
                return TranslatableItemStackCreator.getStack("gui_sbmenu.questlog.completed_quests", Material.BOOK, 1,
                    "gui_sbmenu.questlog.completed_quests.lore", Component.text(String.valueOf(player.getMissionData().getCompletedMissions().size())));
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
                Locale l = p.getLocale();
                MissionData data = p.getMissionData();
                List<String> lore = new ArrayList<>(List.of("§7 "));

                Arrays.stream(missionSet.getMissions()).forEach(mission -> {
                    Map.Entry<MissionData.ActiveMission, Boolean> activeMission = data.getMission(mission);

                    if (activeMission == null) {
                        try {
                            lore.add(" §c✖§e " + mission.newInstance().getName() + ".");
                        } catch (InstantiationException | IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                        return;
                    }

                    SkyBlockMission skyBlockMission = MissionData.getMissionClass(activeMission.getKey().getMissionID());
                    SkyBlockProgressMission progressMission = data.getAsProgressMission(skyBlockMission.getID());

                    lore.add(" " + (activeMission.getValue() ? "§a✔§f " : "§c✖§e ") + skyBlockMission.getName()
                            + (progressMission != null ? ". §7(§b" + activeMission.getKey().getMissionProgress()
                            + "§7/§b" + progressMission.getMaxProgress() + ")" : "."));
                });

                lore.add("§7 ");
                Map.Entry<MissionData.ActiveMission, Boolean> firstMissionInSetEntry = data.getMission(missionSet.getMissions()[0]);
                if (firstMissionInSetEntry != null) {
                    MissionData.ActiveMission firstMissionInSet = firstMissionInSetEntry.getKey();

                    lore.add(I18n.string("gui_sbmenu.questlog.started", l));
                    lore.add("§f  " + SkyBlockCalendar.getMonthName(
                            SkyBlockCalendar.getMonth(firstMissionInSet.getMissionStarted()))
                            + " " + StringUtility.ntify(SkyBlockCalendar.getDay(firstMissionInSet.getMissionStarted())));
                    lore.add("§7  " + SkyBlockCalendar.getDisplay(firstMissionInSet.getMissionStarted()));

                    if (showCompleted) {
                        lore.add("§7 ");
                        lore.add(I18n.string("gui_sbmenu.questlog.completed", l));
                        lore.add("§f  " + SkyBlockCalendar.getMonthName(
                                SkyBlockCalendar.getMonth(firstMissionInSet.getMissionEnded()))
                                + " " + StringUtility.ntify(SkyBlockCalendar.getDay(firstMissionInSet.getMissionEnded())));
                        lore.add("§7  " + SkyBlockCalendar.getDisplay(firstMissionInSet.getMissionEnded()));
                    }
                } else {
                    lore.add(I18n.string("gui_sbmenu.questlog.not_started", l));
                }

                return ItemStackCreator.enchant(ItemStackCreator.getStack("§a" + StringUtility.toNormalCase(missionSet.name()),
                        Material.PAPER, 1, lore));
            });
        }
    }
}
