package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCMaddoxTheSlayer extends HypixelNPC {

    public NPCMaddoxTheSlayer() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<5>Maddox the Slayer", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "enmXRpBElHQ2aBCDawMdi5hG0pm9oLwOerSYQHhSO/fDZwy0eZRJwJzo3QgnYcESf9EEGyxZ1sVpn3Apw1UAPh8h0j5rf0I6l1xXmlj5cyqTaPZWPETMCvOSgX75L0hUm87X3eROyyU9pQq1HY6JaMC/raNwwg/PQuxt28UmTYQczzCg4cStVfLfO1sAIzYZK5+gBY3fnKkW3N0t1gcJfJJXUKOKnfTTy31C3rtKqU8FKg4VqRH3JbYSdGjV1+uUtTv59s9NnJJIjgTvj+uyzaqgy189xh9c8apUUftwyUhapymG9XyMBVF8SclTicPSA+Bgqx7OpVGcgE0fW8a3H0hLO6ZbDhODmAIfPV+gxXb8rPR12i4JvzuK2AuInsLeIrpUpeujoO0VcZl4a/h3TQLaUgTjVQzkoLYEi+b7VXuaJBdSiHXjjfD2op+iGIzUaNmNHjIcNpBvTds2Uu1bZdygYFciANJEPtUHnXH8I6Vv/hnJ9mQ7py6UOSRlxN9ncOuTtwvSqudZnaTueHZ2Jbclu4worL0wCNIXQyOxIqlC4sMfYm7H56JCrLlpZCH1rFuNa88M051PVXZei4Z4X+eIUhB3xiW/gpwwZW32ax0W8Vlgdy928BUmczRksjxKJ2hHCO14QCJnH9kgdQtVQPxnobYnz+56ijNsByWdBMg=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTY0NjY1MjUwNjMxMCwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzM4YzY4N2RkODg4N2M1MDlkNTU4NjVlYmI2MmFiNjA4N2VmNzQzZTk0MjVjZDVkYzZiNGM3YjkzYjc0N2ZmMiIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-83.5, 68, -129.5, -135, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        e.player().notImplemented();
    }

}
