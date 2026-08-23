package net.swofty.type.murdermysterylobby.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCInfection extends HypixelNPC {
    public NPCInfection() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                // Infection mode doesn't exist yet, so just show 0 players
                return new String[]{
                        "<e><l>CLICK TO PLAY",
                        "<b>Infection",
                        "<e>0 Players"
                };
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "T/Y084GMHU/eWI2ACVEJFj3OtFaglkJI7znNfBFtdeaJfGyvd+/6He7NVUP1OJVfv85PtNCzRGibh5N0P0n5e540sbzRVQoyFEzZj8k2cadZB3Yii548PtBmshObRQWndmxLsQ+zfoEHXr7xFkbiNqTB3q3o/F8KiO6jY7B1wOxoAKPWCIfXPrqz1iydVw2V5QD7q/Pqaj56p+qcC4OacJPXMxWB+LD/kAq5DdK1en+AxaxMVxwsusA7+IUboQlnAZWUzrLfol0LZ52XmYOWfQkpz6yv2kVwqlWpXZG0dQmju7e3AK6/MMz/BnYk4A/4qYYkSjVMERi9hgbnIW8z9ExwkemcUttgOZZHv4BcpM14xWZZ2LcuLuzSt8VKAKh2LZgbrLaK/PpeBAtO4B8N3LjDC+Hs/1Si/yMkiisYXgmv0CkYK79wGxxM4awSHlMqllCTuRsBmdjRbA+zorfeTRvkoTGTCv0QNOceMYfcLWNgR3SJhIyc02brznr+FaqmRspK/T3OaNbpOraRk0PTRpbAy3qvYZhrBegZOTo+PoABWjzXJYBw7pyn9iHg47OARU93zCTBkyUsoMvvF5comFiIlMF4rRPPRQZOdMcsIyuONMbbDlXQX9CLjpBQ5vv5OMJgVnXbuZe7ohs1BIOkFk5PE+HRK5FRMMMh5vBGcqw=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTc0NzcxMDQzNDk4NSwKICAicHJvZmlsZUlkIiA6ICIyMDZlMWZkYjI5Yzk0NGYxOTQ5OTg4NzAwNTQxMGQ2NyIsCiAgInByb2ZpbGVOYW1lIiA6ICJoNHlsMzMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODUzNjZiZjc3YWM5YTk3ODJmMWY5YWQ3ZmE5MzZkMmYxYTNlMTc0ODk5ZWRjMWRkMzU4ZWVlZmU3NjRhYzExMyIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(24.5, 69, 3.5, 90, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return false;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        event.player().sendMessage("<7>Infection is coming soon...");
        event.player().notImplemented();
    }
}
