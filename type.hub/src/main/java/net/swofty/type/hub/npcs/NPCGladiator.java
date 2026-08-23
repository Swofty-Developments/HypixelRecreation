package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.stream.Stream;

public class NPCGladiator extends HypixelNPC {

    public NPCGladiator() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>Gladiator", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "Hait/P/7IFMJudb1Ixdx3OdKlTP6D11idZAvDjQATno2D1vEe2ZfacpR1dEENqaBrkoRi0cbGWFmTiqM79zPPknRlfuDGDdaPB4m1fqKZkyTWBfDeHl9ugPxfiEZrOPboNzcOnU8K5kr/FDmbczEwIGARM0p4SmA0defqBEnNhBclsYIzpCM/Xn3G4PkDa/+f0QPmkd8EOhDljPwPd62z3wTNt8W0ofSjCfWXTnIk0rvRymNjhmXZZ/UGDjzvlKdDgb4TyqOBOgbrg9DWTCapIJfFIsZLTbEkit906pzJph2jTLmvGNoFR+011LHNAZRnOXhJ0pEhOslv3BoAvQEyB8+LWGo3/1eOayDHV3aygoV6j+e4vMXzZIcODyYX5pGYbC4DSnNBHcFeTIxJLRxP4/zcH8RwUU9Kj0W5MEmLIJruwqATdWc9liK7zMDk7ZtJoCXx9/YUehw+1tv/DV3ZZvwsfnTxC9GB+xM6yFpWwA1ulvu2mqw0oa6TvV2UwxzJ4Pq+XXeEnnnuobauV33qiKaxqHkmVwjEWnjt1bZhitqrnqupLVo9XcCV0kDsjo2T7vqVphbGHh1vjNoK6VtNdkFV7JzJ/MOl2VieWqVuKjZf1aYqop+Nz1E9GQzKY65ypCGOl+09Rr9SfRXaw1sDOMwhlZn/D1ekyj5IavhpEk=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1NjA4NzMzMzg3MTQsInByb2ZpbGVJZCI6ImEyZjgzNDU5NWM4OTRhMjdhZGQzMDQ5NzE2Y2E5MTBjIiwicHJvZmlsZU5hbWUiOiJiUHVuY2giLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2Y3MWNhYWM2YmVjNjQwNjYwNDJjZjViZmJkOWRmZjJkZGRlZWFiMWM2OTU2ZjM1MWUxOGQ4MjcxNGI1NmIxMmIifX19";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(123.5, 79, 165.5, 135, 14);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        if (isInDialogue(e.player())) return;
        setDialogue(e.player(), "hello");
    }


    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return Stream.of(
                DialogueSet.builder()
                        .key("hello").lines(
                                "Welcome to the <b>Colosseum<f>!",
                                "Oh...wait. Nevermind.",
                                "Here you can join me in drinking away your sorrows."
                        ).build()
        ).toArray(DialogueSet[]::new);
    }
}
