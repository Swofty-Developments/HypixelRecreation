package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.gui.inventories.auction.GUIAuctionHouse;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.mission.missions.MissionTalkToAuctionMaster;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCAuctionMaster extends HypixelNPC {
    public NPCAuctionMaster() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<6>Auction Master", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "IHbiM8PpwXBFKB/x6Ue2msaxAg5uuAn7/4U8D1WIWFOy6vlTz7//aBAunwOpIAHwjI6wS+wMP+awFfcAQtL3CNnaQ6WWaUhPi+Vm2yfNDl7xOXxLKYy/soIBlPEHNteyaV7KEa22zG0a8H7cZ8UKBAdNvzWSMGeZmabBdDpboQAn3kuznmaqJh1Kij6HOvDo4fR5h87ihHy76+ljzGi62vl8ejKi37lwu2pOV+NmhEY37KSZbAtIN4s/UiYCqrwqJ8yP3lMMO7+iIjk8uyT5DJVgoc23bsw+sdDNJzNZ9OZNLvMhy/QvdE4UldIxY6Ikw2ZjP6k1Wb+oBgGDW25bAusvUKf2liPgvJtbcS2TpSanegSzreLfR9XThY9L1SHjja9CQbGeoRD4kmS6Vqi/oFZKDKhuGkWHgyJTcWm2+BGFrC183+ZfMt9JTu4g7GJfSJwL/5PrFpzBm2rbLNMmZP/zq5o0YZUSD0izdffVFoyaQ58oueE3DvZ1rnLuiuhBxGd+Ptc1xKM/sSmcdXIeAn+POCJvK3zb3I7adRCFAy432LzqRnLnLGzuufqvuyn506DdEOEgRaq4yc0VDR1IgmMAgdO/zE/pNdoR/p8LrVFRO5WQmxcXfCNwP888YbRt6t5a7/ExdSN39VYhtovnkPfO+SEsAVofw3wfBO0/FvE=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTU5NzMwNTI3MzM5MiwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzE5NzFkNzk2ZGM0ZGM5YWYzOGNjOTcwNjc5NjNkZTRlZDJiZjk5OTIyNDI1NzQxNmRmYjg0MTQ4OGZkOWUwZCIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-39.500, 73.000, -12.500, -90, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[] {
            DialogueSet.ofTranslation("quest-hello", "npcs_hub.auction_master.dialogue.quest_hello")
        };
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        if (isInDialogue(e.player())) return;
        MissionData missionData = ((SkyBlockPlayer) e.player()).getMissionData();

        if (missionData.isCurrentlyActive(MissionTalkToAuctionMaster.class)) {
            setDialogue(e.player(), "quest-hello").thenRun(() -> {
                missionData.endMission(MissionTalkToAuctionMaster.class);
            });
            return;
        }

        new GUIAuctionHouse().open(e.player());
    }
}
