package net.swofty.type.thefarmingislands.npcs;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;

import net.swofty.type.generic.event.custom.NPCInteractEvent;

public class NPCHunterAva extends HypixelNPC {
    public NPCHunterAva() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Hunter Ava", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "qSFlVZAWOKyfTxPIS9SXOMyNuckdZhnxBRpZdlOWsBGir/O7TTymZsCB0qGsnfZ0ijjGN9CBciX0Z4GwmDL0Wh1NxCzdRO2MknuvPMb4b6iMwrd11SxVF7+rVndXDEAuwknyelBBGwgA5LpWOXZXW85RPVchNA2FeObj6jePW7oIuqOjOQ/Nl8aPWkZbst+KawJLGbvZ1qWi+oYWkAwTi6Uo1Qk/yAwma61VOwvZQVMk/2D++8xdpZuSqNPMydqm448lD9nB7xzyzyfu7Jj//bRwapR7tNzn6VhJ3nAs96PUFt4FU9SN6OqtX9yZ5ni/qkuPY0q2F4WWBQa9qPhg0lgfojAhcD8+XCHQHZCAdiS2KrzAPu3WlrFU4OWLiqOcoN7HynbxgbdZqupKiFKvuIdcBdY7UjTsPgRdAGA1fE4FXHFt6ovOwZQ5OaHJ9oyYwv1dWGtRF+mfj3pSBADuAFyyYbJNz9HJmEPgj8LiahgO8HWPcFtplQ9KnRr06ErxwmKA94MS0ZmTtXvWySE9uJAfSysK3RtEN9gpaib2oyeL2Q+hoWh3b0QID+PChAQW9ELWu86Ph8BRNgmUXEkD/5a8xAB+yUCrClU86mJ6kMBTPp57H8ltU57t9HhjWjywvDSbXSYsNR26p6fkgZWZtK9H5bgz/Nk2/Ldjeo9BsQs=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYxODc2MTk3MDM2NCwKICAicHJvZmlsZUlkIiA6ICIzOTg5OGFiODFmMjU0NmQxOGIyY2ExMTE1MDRkZGU1MCIsCiAgInByb2ZpbGVOYW1lIiA6ICJNeVV1aWRJcyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9hN2RjOTRmYjcxNjVmNzQ3MGZmMjU1N2JkM2JjZGE1Nzk4ZGQzZmFmYjk5OTE1MDRmY2NmYWQ3MjFjYzQyMjYyIgogICAgfQogIH0KfQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(319.5, 102, -470, 0, 60);
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
