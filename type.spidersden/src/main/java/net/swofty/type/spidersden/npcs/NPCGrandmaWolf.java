package net.swofty.type.spidersden.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCGrandmaWolf extends HypixelNPC {
    public NPCGrandmaWolf() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{
                        "<c>Grandma Wolf",
                        "<e><l>CLICK"
                };
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "PX74SU2IXWdqcl0HOqT7dsD78wvGTZQxfirgsAw1lF2iN5XhEosX+1QlZL2OimwV7WsQIs3Slnwo6aG7a07OdnkLSnrkQSA6payjVMexS3aSk5QBqIVrVyxDXbcSp7ck4SOPwmFuzOTqtr0+jPmjiJlcrHUa4MnYd+taIyweiE1Vwu/j65Skx1roTbEAf1l3zXdmemVXUVKfXYbT96+do4dp1ycGA2pYGC5xWgFPzxdY+WU2OYVl7KPCnArfGyKvfoA9gpWNlmo/tDQcieHkSADuOZrKWjRNw97w3FOys2BGWgF95Qsf9GJqBBQsLJMIfSdHh5nlvvBiQnhnCaacZ5l5ySm1VQ6beAllMWs0W+UaTfNtwsXYsBnC65JzhC01w3ALt7GjO5sfSlRPjbDkfp14z6U4/hebL0yQrQT3K+ghQ6iEU7acpP+0pclwhT6GGNjqssO+wvIvYOfkgDQ+KJFIPOdEsnYG7DPFgJVmJWG+qMHC3inkCkT9Afb4fLprjDtZVbMgVYo2V8AWsjGE2IDtQ4sjOa0OAEBwqlPB62n8irts6JgNMWddE69f0LLkmwLkcLHiLalygSyxxuNmyGlAyOMkMS7AANvsYalqFbjHn3EyPhRR+Mzz3q0UK048ezFMs8BgStPq4OgPnCSIJggpemXThSdAIULn6BqTnRY=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYxNDg3MTAxNDQ5OCwKICAicHJvZmlsZUlkIiA6ICJmMTA0NzMxZjljYTU0NmI0OTkzNjM4NTlkZWY5N2NjNiIsCiAgInByb2ZpbGVOYW1lIiA6ICJ6aWFkODciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODM5MTkzYzA5YmNhZDlkMWY5MDFhZmVmNGUyYzBjYWQ4M2FhZTFhNTcwMzM1YzNlY2JjN2QwNWZkZTE5ODc5NSIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-281.5, 122, -190.5, 0, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {

    }
}
