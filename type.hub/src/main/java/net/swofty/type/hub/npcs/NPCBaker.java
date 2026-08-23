package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.data.datapoints.DatapointInteger;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.hub.gui.GUIBakerShop;
import net.swofty.type.skyblockgeneric.calendar.CalendarEvent;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.gui.inventories.ClaimRewardView;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class NPCBaker extends HypixelNPC {

    public NPCBaker() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                List<CalendarEvent> events = SkyBlockCalendar.getCurrentEvents();
                if (!events.contains(CalendarEvent.NEW_YEAR)) {
                    return new String[]{""};
                }
                return new String[]{"<f>Baker", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "lu9SuKpv/U8XqaZTkleKzPDg8S1pcqA7LSWiWimR9x0BnkpK5CkyLwkWA1AMKCibQZSMPjoFFySNMVRcIhylv3yN0V6/Y6moJi1/SmRIeJJL/FovCUykzTSvbWsqJXfRyoi+5mUt6REj6bvJQruNtCedIHQD5a0Mrw3d8LbvZ0OlGPUbaAv1O7dW1O2uxmxCDSWMOL8PN+6fb/zYgA/XeJvSj97LafK4YAeb1YV362CeMkhmMP0uE5wj11+BnexEN+WaBzbRIUlBuSMB+Pw+7RoS4Nk7kxxKSNAR/pzlSqFHLkTlL88ljrLeyEooccpETSuqLh55/wsWSdesEDpSNjmfRYVX9EXOk783VRz3Btb+MItjiqmos5Mgmjelnx34utIPkAFbLyn/AUvWaNImxhWw/iDFYod+C/QNbUqR/H9ahIHzZXun4+6tKhVBgaCfLqaqF+V9Js8miapUpW16EEnElTNJ843+/HFgqex18q2vCTUX0tixtzHrFmwhhbBnT02DSvbvIxm9ucyNMwTpYhJ33I433pB67i1iQxiNBxaTTVSn2bGs4AKLgOjkTg3TsixEix02fCOzFl8bau/JlZMDmk7/2SAI74VRnreBVTEHjIAb7SRRXNy+zOxQJLzyMB+TwpGBBIUbNpCgjKu0aqu+Ld/FOO37dvBke8bv7Uw=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTU5OTQ1NzU3MjI1NCwKICAicHJvZmlsZUlkIiA6ICI2MTI4MTA4MjU5M2Q0OGQ2OWIzMmI3YjlkMzIxMGUxMiIsCiAgInByb2ZpbGVOYW1lIiA6ICJuaWNyb25pYzcyMTk2IiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2U5ZTIzYmU3ZjA0NTU2ZmEzMzM1MWE0Yzc3MWEzZjA1ZjRhNmQyN2RlNDEzYTM2ZDAyMzBjNjFmNzE2OTg3OTkiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                List<CalendarEvent> events = SkyBlockCalendar.getCurrentEvents();
                if (!events.contains(CalendarEvent.NEW_YEAR)) {
                    return new Pos(-6.5, 0, -47.5, 180, 0);
                }
                return new Pos(-6.5, 70, -47.5, 180, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        List<CalendarEvent> events = SkyBlockCalendar.getCurrentEvents();
        if (!events.contains(CalendarEvent.NEW_YEAR)) return;

        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;
        SkyBlockDataHandler dataHandler = player.getSkyblockDataHandler();
        if (dataHandler.get(SkyBlockDataHandler.Data.LATEST_NEW_YEAR_CAKE_YEAR, DatapointInteger.class).getValue() >= SkyBlockCalendar.getYear()) {
            player.openView(new GUIBakerShop());
            return;
        }
        setDialogue(player, "initial-hello").thenRun(() -> {
            SkyBlockItem item = new SkyBlockItem(ItemType.NEW_YEAR_CAKE);
            item.getAttributeHandler().setNewYearCakeYear(SkyBlockCalendar.getYear());
            player.openView(new ClaimRewardView(), new ClaimRewardView.State(
                    item, () -> SkyBlockCalendar.getCurrentEvents().contains(CalendarEvent.NEW_YEAR) && (dataHandler.get(SkyBlockDataHandler.Data.LATEST_NEW_YEAR_CAKE_YEAR, DatapointInteger.class).getValue() < SkyBlockCalendar.getYear()), () -> {
                dataHandler.get(SkyBlockDataHandler.Data.LATEST_NEW_YEAR_CAKE_YEAR, DatapointInteger.class).setValue(SkyBlockCalendar.getYear());
                player.getAchievementHandler().completeAchievement("skyblock.happy_new_year");
            }
            ));
        });
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[] {
            DialogueSet.ofTranslation("initial-hello", "npcs_hub.baker.dialogue.initial_hello")
        };
    }
}
