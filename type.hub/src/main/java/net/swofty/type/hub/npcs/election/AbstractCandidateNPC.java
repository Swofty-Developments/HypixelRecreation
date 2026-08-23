package net.swofty.type.hub.npcs.election;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.elections.ElectionData;
import net.swofty.type.skyblockgeneric.elections.ElectionManager;
import net.swofty.type.skyblockgeneric.elections.SkyBlockMayor;
import org.jspecify.annotations.NonNull;

import java.util.List;

public abstract class AbstractCandidateNPC extends HypixelNPC {
    private final int index;

    private static ElectionData.CandidateData getCandidateData(int index) {
        ElectionData data = ElectionManager.getElectionData();
        if (!data.isElectionOpen()) return null;
        List<ElectionData.CandidateData> candidates = data.getCandidates();
        if (candidates.size() <= index) return null;
        return candidates.get(index);
    }

    protected AbstractCandidateNPC(int index, Pos candidatePosition) {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                ElectionData.CandidateData candidateData = getCandidateData(index);
                if (candidateData == null) return new String[]{
                    "<key:'npcs_hub.election.mayor_unknown'>",
                    "<key:'npcs_hub.election.candidate_label'>",
                    "<key:'npcs_hub.election.click'>"
                };
                String color = Text.colorTag(candidateData.getColor());
                SkyBlockMayor mayor = candidateData.getMayorEnum();
                String name = mayor != null ? mayor.getDisplayName() : candidateData.getMayorName();
                return new String[]{
                    color + name,
                    color + "<key:'npcs_hub.election.candidate_label'>",
                    "<key:'npcs_hub.election.click'>"
                };
            }

            @Override
            public String signature(HypixelPlayer player) {
                ElectionData.CandidateData candidateData = getCandidateData(index);
                if (candidateData == null) return "";
                SkyBlockMayor mayor = candidateData.getMayorEnum();
                return mayor != null ? mayor.getSignature() : "";
            }

            @Override
            public boolean visible(HypixelPlayer player) {
                return getCandidateData(index) != null;
            }

            @Override
            public String texture(HypixelPlayer player) {
                ElectionData.CandidateData candidateData = getCandidateData(index);
                if (candidateData == null) return "";
                SkyBlockMayor mayor = candidateData.getMayorEnum();
                return mayor != null ? mayor.getTexture() : "";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return candidatePosition;
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }

            @Override
            public @NonNull String chatName(HypixelPlayer player) {
                ElectionData.CandidateData candidateData = getCandidateData(index);
                if (candidateData == null) return "Mayor ???";
                String color = Text.colorTag(candidateData.getColor());
                SkyBlockMayor mayor = candidateData.getMayorEnum();
                String name = mayor != null ? mayor.getDisplayName() : candidateData.getMayorName();
                return color + "Mayor " + name;
            }
        });
        this.index = index;
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        if (getCandidateData(index) == null) return;
        sendNPCMessage(event.player(), "<key:'npcs_hub.election.running'>");
    }
}
