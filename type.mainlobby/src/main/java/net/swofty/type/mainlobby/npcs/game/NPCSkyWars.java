package net.swofty.type.mainlobby.npcs.game;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.ServerType;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCSkyWars extends HypixelNPC {

    public NPCSkyWars() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<e><l>CLICK TO PLAY", "<b>SkyWars", "<e>? Playing"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "Gxzmv2Nve29AQqkQv/td/H/MN1QLk9/cHlzvX48BXGmHm0LktLeDiFFf1bQfPgMf0kcWS18TkidzBAyJlVr5d1XQhzO5YNmIhMdiKN9toNx0GJwA8WIOm9W95l2DoJdS82YAElQ4SqWCtPttAdm0CJpandAWrxJNOkYhSvXfRxKGXDjh8TUGQwppkzVoF/zMOpK164rM2EfCgmof9BQ5yXXlAwah0eS4aV1iMU/TUBDyBJXSStIxBgMQnf+mhQCgJqM7Eapyg6Qa6AWnnwUwQokDw4i+0rtaJTk9v/5Yro+AWXm2EDE3FIMD8UuZhPTt/KWXKFxUy/g91otea2r8sNpN6sVsE2PrSShFmlIUzYzQJNlYlnqYvaUmHw99jQ4KlpZEcb2jv451x5T59bIBoFpFB3UcqJCDC5PzcieZsEJ40meHSEf2OXkp46qg4XfFN3XlV9qzqVNR3Wks7ugGH0gpa9Q00eXHSamEEhwpTVq/BEKxLmoKlmxYhWjLBmH/MQQrJ+lZMqDBIaZG265uGt5ZymPbXIX48d3qcFn/MuDAsmjxCq3w6SJjZSs46M5wlySgiljuAlHgH2lgiE4gWj2Tpec2rp/LfDw5ePOq/hv6qp5JIPF4bB07R7YD54qNHjPwgjeL0dQ2tb9/E/h9LTfYt+ahD1L/H2vNu1dhfb8=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE0Mzg5MzgzODUyMTIsInByb2ZpbGVJZCI6ImZmM2EzMGY5YjdiYTQyMjM4ZTc5NjU4ZThiNmE1MjI5IiwicHJvZmlsZU5hbWUiOiJDcnlwdGtlZXBlciIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzkzNzNjZGY1NmMzOTZjNmRjYzc2OTRlYTRmM2Q4OGJlZmQ0YzI0ZTQ4M2E0YzJkYWFmNjZiMjllMmE3ZWU4In19fQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-26.5, 93, 10.5, 118, 0);
            }

            // TODO: holding item
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        event.player().sendTo(ServerType.SKYWARS_LOBBY);
    }
}
