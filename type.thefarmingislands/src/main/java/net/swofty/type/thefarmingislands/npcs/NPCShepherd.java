package net.swofty.type.thefarmingislands.npcs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;

import net.swofty.type.generic.event.custom.NPCInteractEvent;

public class NPCShepherd extends HypixelNPC {
    public NPCShepherd() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Shepherd", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "dw2AFZUXrsepyojNvl1qRVi2pUA0/Oed5KjdbZFSN4UC3c1aXuhHH+PjbhYhsOeN+ZvJdfYbBLJR3KDUVKc03P9cckgcrKqd5cLuixVj7TZInmQTyylQ7/S/EBQGqGyKM+P+5ZK+cX13hPmZaR2tk2yfgBr5MySitHbA4nklJvEJ8MeqPO2kqJD/+WXgxSCcnWOEX/kt4lqdWej4JtEStBCc+SO0OwEKhT0Lux4S9Hr8enwarY90nBUIGNACfHAiooyTiogj4hUws6t0AkctuylG7wq9b1NPFny7K1PWJlZClsvXxXR0AC/sMp5J1x6pX8hQVF66QPJqMw3Oc60hQWkJkihyb/op9Ql1WXzq35YjNN7T9ctq23L3csjtTkIxODUBKEh5NpiafSbiluBp8hWBxjjsp7pbbzALOZj77jxPF6Vbj9i0/2+N6qjH2+6gZI0lJpVD2ut2o1qVchfrDvTlOAjL1HeEQbjyNJLLTHB0NKqpv2MV51+BAqjW6vWHTIL3K3biA+b9h+misKwzX+OX0yy+FWEuU1yot2BHHvv1OwfShrpWsZPHqUvXT+xt3U6RnXgZuA2PTTOPR1deaRI22zLv3ZeZjg1l4hYEk7bRhiIe6NkaHb1PQc9sT3uZ4pD6CLrVCjcV0eeytRVwEDhiKYXDAXRHQSf7mJYI3AE=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYxMjAzODQ5MzY2NSwKICAicHJvZmlsZUlkIiA6ICIyODA1YmUzMzY5NGQ0NjUwODljMjQ3YWZhMDIwY2U3YyIsCiAgInByb2ZpbGVOYW1lIiA6ICJkb2RvbmVsbGkiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvN2E4YWIyMjY3YWU0NDNlODNiMDVlM2E4ZDY3YjhiNzYwYWRmYWFkMzM1YzkwNDczMzhjMGUxNzc0YTY1YzM3MiIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(388.5, 85, -372.5, 90, 0);
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
