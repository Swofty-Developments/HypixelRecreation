package net.swofty.type.mainlobby.npcs.game;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.lobby.gui.GUIGameMenu;

public class NPCAllGames extends HypixelNPC {

    public NPCAllGames() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>All Games", "<e>? Playing"}; // TODO: network player count
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "fV2+IF6cbJxKASMhdfzekXzOyvJoyogCqinjD0LIUoy96K9B+FyXYbJTT9uc1CKDDiFiXYPAacU0otQYyQSbUsmHEbZjSEDoedHf+cri7/lrIQcALY5O/x3UOISUFaTvE0BWJ6m3kYZdHe6Jo4YRrFoeT7ydNdrl22daTLSVEbo6IPsZzvhtjB13wL1CLyT/KBRDd1EOTSo0Y6s5m6IlVQvrTHfDA7Cqk5vUIFCY3rgVAU2YIUMXHGusj8TGaFIUJq0AirV13BXBr0HmjJNIQ/kC1rZ2dCWyFowdcc858aDa5lvoMLXjfw/DuWMgYtbgO/p9ezVRoajOYPV4s8aeB5KZdxTfeqGaUod8ZSQeO/z4sRI+PwHU+G+fk6HcUCoNE94m3EpvGww5xDnDBdd13yv4p0Fo3xyjapXwdags4cUnFnDo0vV4Q/rwTOwbhyCwP/Dd6lpHIEVQSaJn4AgAwvbe2xcPZMX8+y+esV20zNrTuWdeXIY/eSNEydmEoNMBhOR5zhyFtIcnB1uiWYNzuM+z05ykWnMX86R0lyqoEEmTVk2aV2IIRghRKGVOtM7SrpKdxYozEDtzDgiijS3fcbtLcZ1ss/oNotOP53hQhhFVl4J+KXUs/NMXlJBALHHgHvDjZ8IZ2/RwZKhqpcTW/n4RIrR3Rg+fQ5nnsLG0rZQ=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1NDQzODMwNzg3MDksInByb2ZpbGVJZCI6ImY3Yzc3ZDk5OWYxNTRhNjZhODdkYzRhNTFlZjMwZDE5IiwicHJvZmlsZU5hbWUiOiJoeXBpeGVsIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8yNDdjMDU4ODQ1ZjNiNmQwMzQ4YWFlNTZiNmFhMWE2MWM0NjlkODRjYmFiYTA0ODE2NDA1MGExZDMzNWQ2ZWEwIn19fQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-29.5, 93, 18.5, 139, 0);
            }

            // TODO: compass in hand
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        event.player().openView(new GUIGameMenu());
    }
}
