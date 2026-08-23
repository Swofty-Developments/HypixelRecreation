package net.swofty.type.galatea.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCSawyer extends HypixelNPC {

    public NPCSawyer() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>Sawyer", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "kJkYx94vfepT2xlKYGiolKgZZ8ozqiK7SRtRYkfl5qYCBN4tE0WHV/X08smjr4ohSp3/ytStjb735EiiGPSGQFX01ltNXEwpPlIVk0M85hrad0wsTb9/P8v1YGKxmEjIE5CPoC49IM8sDF6h3lTvv3Rinu0Td/FMLp8i3m7Y9tIhEpAy7pu2ZY15/GTuBUNWMHSde9qVXoej0qI7qHHHU2Cz4oapRePUIHjzXIiOHsHoTjKG6TKQFd/pZnOQ6EYRf7dAB8N/B+9ydCm4TzjvjjElJtVt0GSl2Qh0JWPxUcpN/XIxFDYwEknzIVISrx8uF+bCVrWzqGX1SBcsfo0JDhqkv4fAeJAlFCQaf2eUmfBG2V9PpDgzgDsX8MR1j1sv/5EPZk6QbSIJ7jLgrEmn4YaAHUO2o0YJ2/nn9O/2i7Yxhd6KaUoTsccsO1a4/3NCOKcoXrXvngsPzac3E4RvPTRCWv0oEA6OPfXBtpDrRGBRF57mYDQ5GPjxqXUspgkpFnuJoHFzy6BlcuvHCaxmZ1gp7wj6+PUe7P8hWyDtjUWtCrAdL5wQ9zP3Pm39Z4/QI9ISRk9PKmU6DpOMrWo+D9tiic3hIfz9svMzMe99cDXjDwY/akmd8rv7YUjsTa4sjRl3PdM0FVSe0YKjr7deNicWaxA5sIXsu9wgjtx6xWc=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTc0MTE5ODk5MzA3OSwKICAicHJvZmlsZUlkIiA6ICJhNzdkNmQ2YmFjOWE0NzY3YTFhNzU1NjYxOTllYmY5MiIsCiAgInByb2ZpbGVOYW1lIiA6ICIwOEJFRDUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDUwYWUzNDgyZDgzODk4ZTBlNTVjMTUxNmIxZWFkMWE3ZDFjZDU0Y2NmNGMyNDM0Mzg2OWEyOGJkNTYyMWYwNCIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-691.5, 121, 81.5, 135, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        event.player().notImplemented();
    }
}
