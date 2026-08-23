package net.swofty.type.mainlobby.npcs.game;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.ServerType;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCBedWars extends HypixelNPC {

    public NPCBedWars() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b><l>DREAMFEAST UPDATE!", "<b>BedWars", "<e>? Playing"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "bsYrsYh6viAUp53oSYM1DoUleZgh4wwJi+WF698LKB/IaacgqMfrifel+YROVQ50MqiJzI9bXzI+pO7M2AUN1h+cZ1tApHVs69pcX4sRq7N0CFfHzvHGZvEXVOzquWZeLVX0MKk3MPjwCbGoByu8lWyS1zOlXeeUiJxfUAeU+OZmjH+WfqFyLvjH+jXiUx463CjP9ivCoqJ0LWLMkq7SpO7N3Vl11IS3ip8WdQr9JF7Mqxs6g3OD7e2OKhn9t1W5H0OjjU58hsaRGAbOld50u5p1BI7KfUq2spGE3NSFqYmf0beUgWRpYC8YTUyZFzVThGxSyUPjxdWz1UOjJXCTqZAY6oSRyDCG9B49SFWU4de9bC3e+nIsl4Fk59V1HELRZROML6jtwXrFLAcm7Aa9407qSc31d3E1lCEyGiWHEGX3n2FEx/oCEUM3z50Y+Wwufj4a1Ex/vOJbwAaThyTPP6NwOhAXxQaxUB/7OfKWWTzNLQrOhUiwklUWOfke1XszzX3MSiro3iR8SC3YB7w9Q0eVr9dDtyldofm0FKMom1bfU0OBfiWkxoYy0BIuYT2yLfWlJqiJ7c3LLCeCarwWDOwIg4dQ4C0sI4sHuvWhw1byGibsXmR0glS4ZCq2htUZ7dYrAklT8d91/4AlmdVfgSiPIYeYADpe6Ty2PgD2yTM=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYxNjg2ODIxNjczNywKICAicHJvZmlsZUlkIiA6ICIwYWFjMWRlZjUwZmI0N2RjODNmOGU2Njk3MTg1ODRkZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJQZW50YWdyaWQiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZTUwZmYwMDcyNTE2ODBjNjNjYTJkM2E2YjY1ZWM1N2YzMWQ4ZjgzZGYxMjc0MWZiMGEzMzlhMWZlMmIwOTRhOSIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-25.5, 93, 2.5, 96, 0);
            }

            // TODO: bed in hand
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        event.player().sendTo(ServerType.BEDWARS_LOBBY);
    }
}
