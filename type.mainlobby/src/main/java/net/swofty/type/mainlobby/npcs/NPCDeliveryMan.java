package net.swofty.type.mainlobby.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.lobby.gui.GUITheDeliveryMan;

public class NPCDeliveryMan extends HypixelNPC {

    public NPCDeliveryMan() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{
                    "<a>The Delivery Man",
                    "<e><l>RIGHT CLICK",
                    "<a>0 Daily Reward Tokens!" // this changes colors
                    // shows "Free Daily Reward" (<6>, <b>, <5>) if not claimed, and "1 Delivery!" (<c>, <f>) if not claimed.
                };
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1MTIxMzc5MzY5MjYsInByb2ZpbGVJZCI6IjQxZDNhYmMyZDc0OTQwMGM5MDkwZDU0MzRkMDM4MzFiIiwicHJvZmlsZU5hbWUiOiJNZWdha2xvb24iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2U1OGZlYTVlYzQ2MDMyNTZkMzUxNTYzMmMwOTliOTUzNjgxN2I3YThiYTE0YjFkZTkzM2M1ODNjZDRhZGQyIn19fQ==";
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "dlW8HRSHGMmFTNYQLyg0yjM3svh30uJX/+uSnBxJg3r5/hkkPKkfLLRg6uzdwh/yRgU5suz6F5QraNSo0iJrCBYmbbsNpIEK2WCkyHFEfDcBJS6AUM/oFM+edtn7EqQyH4X6mD2fKWLAp06sDRQmM31rGinIndylbCIZN2OLzkkL73Et95kzmbnaNqh9YpROraM6PH0cIzlfCF4HjcBzLNYsKe0l2LlqsW2+V/+W3JJW42KyCBB57P6HNFMY8kNVWv1wdqNdDrbzVrHKF31WconDFHm1eW6BKRD5uzXjxD5l9z1O15CaHRC6cAheEAtE0QwMop+z7U3aydpCSFi3d5aWcqUxgIEZNQDmDvunPIyMvLyrl3EvbyO2U6rxP97Aae4rURk/OzEtjpoEkqXoyCUjDSBxY96h5XqVxoBUXxxmRWuiuNmuRYt8wFPHDJDuMdOMY5954dz9fWuroirySecxGikHq9/srebQpk8jpgmD1765gxti0C+RbqKcPRyDoE/rMIxhSeF2JMXLS3Ub7u5a51rv39ESuvVRtqWVefgi/47LL1iNO9HYMB0C20I5d47LgCihzu2RHDlSAUVYtTPaQzkmkdYuYfGaz8z/tS2YKUxwAfR//k8olpTJvpxqUxtpxhorK8S7uHCLkR1m61UJAdUfbJICJMxaqpsIe2o=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-24.5, 67, -5.5, 85, 0);
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        event.player().openView(new GUITheDeliveryMan());
    }
}
