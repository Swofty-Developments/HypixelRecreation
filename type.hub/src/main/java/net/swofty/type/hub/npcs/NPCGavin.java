package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.stream.Stream;

public class NPCGavin extends HypixelNPC {

    public NPCGavin() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>Gavin", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "qwNSHj1b7UwXoeP6/Vs/1EGb0pyfO+DGOVUQ9DJ7DY0ZPn6VwQv1Ej1W39wANDGJI1p8eQKRqMLKH0Xj4WkSwnMnj0e7DZU2VROO9xi3th5IhJg/7SzpMt2vMvYUN3u6HI1EzhYHuLL1oHF1eGK/5lZZp6xvb2X4ZuOvX8oASvrgQFPcxR2WMn4nwS4bKRp5CDFGg+fCHCMuHHQAjZpJAHqbfxD2DWXq7CbuyOQJlIybhGDY30syWVKH1aZsp5Nmm8fOeSysqyZL+F49zBbxPUBaX/gmxsBR8cRyU37gBzT39aTeybFsZrQSOz3raFX7H4pRC8xf9dKQzTYxCvi14ljjp8q+IH1AWYSZJSZkxA6k03gxVH6Oxbs8XjHWxYQIu4uPhTV3LRShkSRz1WTLYHqu+I/fhmAsAN7YJQHYNFJGkLSBVYRCkSPCQ3efj7TDX455KlojZ23waaqEvH0d9gUMEScRc3Qpq1Tf3X63CNvd5BBO6apiN9Vfq3TZ3YKdhGLQLXBsu3QewCH2qcf6jbU37in6FltyncRsrd84pFsl2ryquOYhwj2slnVNIyhG7zAguRM9p7zvZOPYlgSplcMgW8sQ3Isv25PBGZ+B6Qhwdl4yYpUnc/n7EyxMuthGyID/nhwTBq4f51L3+RNDYnFz9ED26E3IcW/dQdgxPNA=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTc0MDA3MTUzNTE3NCwKICAicHJvZmlsZUlkIiA6ICI1ODc5MjNlNDkxMzM0ZDMzYWE4ZjQ3ZWJkZTljOTc3MiIsCiAgInByb2ZpbGVOYW1lIiA6ICJFbGV2ZW5mb3VyMTAiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDZlMzc1NmQ4Y2ZhYzU0MDU1YmNkNTUyNWQyYTNhYmVmZTZjZWY3NzkyNGY4YTk5ODQ2YmVhMmZlZmY3NjExNCIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(161.5, 69.900, 43.5, 203, 17);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        HypixelPlayer player = event.player();
        if (isInDialogue(player)) return;

        if(!player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_GAVIN)) {
            setDialogue(player, "first-interaction").thenRun(() -> {
                player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_GAVIN, true);
            });
            return;
        }

        setDialogue(player, "idle-" + (int)(Math.random() * 2 + 1));
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return Stream.of(
                DialogueSet.builder()
                        .key("first-interaction").lines(
                                "At the end of each year I bake cakes for everyone in town to celebrate the year.",
                                "I made one especially for you, here you go.",
                                "I've recently added a <d>New Year Cake Bag <f>to my inventory. Sadly, it's not free! Click me again to open my shop!"
                        ).build(),
                DialogueSet.builder()
                        .key("idle-1").lines(
                                "You can open your <a>Sea Creature Guide <f>through your <a>Fishing Skill <f>menu, or with <d>/scg<f>!"
                        ).build(),
                DialogueSet.builder()
                        .key("idle-2").lines(
                                "I'd rather be out there fishing for <6>Treasure<f>.",
                                "But mum insists I finish my studies first."
                        ).build()
        ).toArray(DialogueSet[]::new);
    }
}
