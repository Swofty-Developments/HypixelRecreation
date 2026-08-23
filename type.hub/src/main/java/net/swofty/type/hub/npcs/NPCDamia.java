package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCDamia extends HypixelNPC {

    public NPCDamia() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Damia", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "NGpDgIE8bidcr8W8BviMwQTRP5Pq4qXUHcqOFwWSJIFunoEQm3UqOmMTWCn3i5ZXjrwkxT9V8XRT7fVqURMfZRyqus6jRN6UKe1nyk/C+MICkJFmlcyKr/Uixd7MLDY8GIqUxs8BaY8Bou3kdPUP95gJm68ntKby2+P0kjRY3O8NpV+pJrcuXVmH9u8s7Leh8G9LuSGrIM6Cic+4oUVj2FR9mipbJDDbb0LiZ4FwPs4dKFSejQm1BqPw5PrPNe1Lj81cBb0WlSfpb8+f3TPB6s/DBHdZjFA7U3W5FQTzgp32JUtigMTl5WO7LfP+8rtITPpf1xBjMZltBNe0OYD91VOX8/gw8dFRuRnUbBaO/qvvtJrs3k79Qa4pKJvrCtSG8tToTk8fqAl0e8BofkXWdlFgBgMoYx67QAMSTfvI58dWnfLF0O3xoI4m6rxoa4xkQhPii8/WFOegBpJxqkg/738qozr6eFs6muJVnxRFSt0n72l+li7wr2pZAYqa50uRllHL7Ry920bH/NaQGdvctgEMEcDnuk754vszzKXJ+rqdaemGb2MVnSt+rq0RzXf27WGJVp7KRGZsVxV7cXk930qwHYF3XaqNXcUXF3RV/AGESFupUgWAcdGbVeWODJXQpLrniNcfsSzI7cep7t7t6G5Xw58fu59zYDaXPrsik7o=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYyNzQ0Mjk5OTI4OCwKICAicHJvZmlsZUlkIiA6ICIwYWFjMWRlZjUwZmI0N2RjODNmOGU2Njk3MTg1ODRkZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJ0aGVhcGlpc2JhZCIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS82NjMyYmRmMTE2MmI0YmZiMTU5ZGE5YzBkYzYxYjBkM2Y5OThhMGMxNTQwYzFiNDM1MTNkNmYwOWE2NDJlOGMiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-16.5, 55, -10.5, 90, 0);
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
