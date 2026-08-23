package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCJacobus extends HypixelNPC {

    public NPCJacobus() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Jacobus", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "UyawAzr+K7HV66yViJLuJXuCAUBBnoxEtlfszgLIe1waFxfCL/YBnQsdXSKIaM7W5QTu+nD9H6Mp0C3bCWsC4zDcWXEq+UW38AT15llzr8Y9e48DJ+LtNXGWdkgK3k2FsbCMziz6+KH/3JmA2Ux3UcqLXCNdD47/T+SORJSW8cQIvxTvUrH1dOoVcJVPB2H30i8GC22xdYgffX2X79hlUIermyCg+y02asJWW1iwt2aTmBYQecyS1yyLvLpS1IUmGWOR+4E8gqm8Q7C/gUAk+H174mTkCobVvZYmu1OnJql4hvm0FaTgUKlR/yIyTWitF6hjl94s8//ETmtTqy4gig8SeHBrNWmZ/4CLyg/2Qmqyy24JtJpCmi4bVacZFkIMVaF06kUbLQZy+Y0Ybnc4iqFklf4nBW7BFCJc0BrA2e4pmK5bOO6DScw8PWzLh0bYbibbhezk4G+SwVNHK9hEtpXsHz4LCSSaRF8HVX3uKJS5kBki0MQYoWASPWY24GgPUikVXd/hYOhArEQfWcGt7C9m6irrDRjzJq6GPco1AOWDpT1uj4kHYhF5enA49Dilqs9hVfsFuh3XvyC0U73FAjm1pTI6iSagd+lnGbzISofgXn4YTtl9EHDtX8JTZuFMGL/tfd2UAqHOxFxY+godkXii+MlramNilHXCGr+zyx0=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYyNTg2MjUzNTA5OSwKICAicHJvZmlsZUlkIiA6ICIyYzEwNjRmY2Q5MTc0MjgyODRlM2JmN2ZhYTdlM2UxYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJOYWVtZSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS81YjFhNjg0ZTI3NGY2MWJkMjUwZDhkZGNlNTcxYzgxNjY2YzMwNTZjY2YyZjNkZjVkMGEzZDYwMjc0YmQzZThjIgogICAgfQogIH0KfQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-49.5, 70, -60.5, 90, 0);
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
