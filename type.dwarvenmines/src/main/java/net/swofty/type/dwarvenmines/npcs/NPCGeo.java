package net.swofty.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.stream.Stream;

public class NPCGeo extends HypixelNPC {
    public NPCGeo() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{
                        "<a>Geo",
                        "<e><l>CLICK"
                };
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "wZW+jncgoCCDcyFMat52uadTINXURF5E0HrBrFPwpn09gd3R5hmp7fFO+P2lxre4/JKzdW0iu9psP4D01Ri79K4/VIO21OV4I2VBL/bpicM7M3xQepI/dOa9XbtLju+0gPEF9nRWbOmDGY71++YOnLRMGXGfInB/3BhoFoTexj+XRhQv7lWofLlJeEb7uvRqOc4Zie/JdDgWSubdHwnC04WodnJ7jUbLj/u7GbJ/1ytJ9Gp85MDs0jAsAhHeSdjKrDMA2pEl/vXaoDnP6GkYyVqpszZC18Y7Wz8HCyzcU8JsKrmjkcOpiXj+nKBD5MdHJO/j+NQkopRAlton/uoNTYZECZDHtH67sWHFp7vivJmBy0CKP3fpShE9s3K3q1Se8+8DkFkVOsbiHWRl8/vkFIE6IfkOnppX+tbEHLJnnQ7xKz0IA/+1J+0CwZAdjQQHg7fN4IfDJq4Zaqbg/HHue4G/+NDbLp4J3CzWz24YlzMZatbRFEKjKMAvE9DAo/Asj5ZwyenoBTZgkJa0MIFZLRb0oZzh1uj0xhQyQyRuNnOP4DeDjmeZslEuKxrhFBHBldzCWZKT8ddUWOoLP5BL/6uTFjs/RMMqBtYYz+4mxJ76+qxblxuimDU8g92GgSKuF1TQPD2Z4qcnAahTaCA8FEBZx5/8mtTEktVgEhEqn1c=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYyNTY3OTY1NzAwMCwKICAicHJvZmlsZUlkIiA6ICIzYTNmNzhkZmExZjQ0OTllYjE5NjlmYzlkOTEwZGYwYyIsCiAgInByb2ZpbGVOYW1lIiA6ICJOb19jcmVyYXIiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGZjOTNlZWIyNThiNDdjYjdjYjQ5OWI2ODFlM2Q2NjAzNWE0NDNjMDc2Mjg0ZmYzZmY1MGIxOTZhNDY2YTU4YyIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(87.5, 199, -115.5, -225, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
        if (isInDialogue(player)) return;

        if (!player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_GEO)) {
            setDialogue(player, "initial-hello").thenRun(() -> {
                player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_GEO, true);
            });
            return;
        }

        setDialogue(player, "idle-" + (1 + (int)(Math.random() * 3)));
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return Stream.of(
                DialogueSet.builder()
                        .key("initial-hello")
                        .lines(
                                "<a>Gemstones <r>have magical properties and can be applied to some special items.",
                                "There are <a>12 <r>different Gemstones, all of them having their own unique properties!",
                                "For example, adding <c>Ruby <r>to armor will increase its <c>❤ Health<r>, while <d>Jasper <r>increases <c>❁ Strength<r>.",
                                "The more pure the Gemstone - the stronger the effect!",
                                "Only some items can have Gemstones applied, and you can remove them any time.",
                                "Use the <d>Gemstone Grinder <r>next to me to apply Gemstones to items!"
                        )
                        .build(),
                DialogueSet.builder()
                        .key("idle-1")
                        .lines(
                                "The more pure the Gemstone - the stronger the effect!",
                                "There are several qualities of Gemstone, including <f>Rough<r>, <a>Flawed<r>, and <9>Fine<r>.",
                                "The highest quality of Gemstone - <6>Perfect <r>- will increase your stats the most!"
                        )
                        .build(),
                DialogueSet.builder()
                        .key("idle-2")
                        .lines(
                                "Not all items can have Gemstones applied to them.",
                                "Check out the <d>Gemstone Grinder <r>menu to check which items work with it!"
                        )
                        .build(),
                DialogueSet.builder()
                        .key("idle-3")
                        .lines(
                                "Use the <d>Gemstone Grinder <r>next to me to apply Gemstones to items!"
                        )
                        .build()
        ).toArray(DialogueSet[]::new);
    }
}