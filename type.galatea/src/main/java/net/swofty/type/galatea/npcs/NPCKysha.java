package net.swofty.type.galatea.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCKysha extends HypixelNPC {

    public NPCKysha() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>Kysha", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "VVCsJ2fmOv6SXYbHEIlR0iOr+1O2tKqXtKyVkowX3xRuUWenOxly8M6ooyxON5e6UEbkKCg9qV0UOJCB0pbFSSvKJL17WMey9XG/VUzf1BQzrEcm2KANm1Ij/gZNBz53NkXsip8FDmYyaeutq8fvtZ6jFpsKxIpcjGCt2b9wXTszSjf7/3WRTDz0rBk0kncn3aLgdEi9El0KVhVBzTqxw07VcDMY5SXUyT5ZmB+HhVOODof9HcUKIuOEziteSkq2NdbwQBZlgtjDUVcAlClzfDyw6zZMetRLfRrIT3CXQNMacKsqGT7uOMuJSHp5dyOMLcDVHUFuYVYDQmwk8DFODFuBCZQbl9UTBTj5AcAlbGl4gBWEvJ9FcO0oIIox/PM0oPpIpuYHMyknB3mPsDD2u0Lue87bw4/tG65vGXyIEGUycALejg3+oMUpIeStxoVqkri96S+n9KhbakdnrtcW8S1hSeNuymrM+psh6JEUEbiirSJhDSQWyQUVOqZJNWxhE61bZx2KVbCb00RSu+OusO7SBWwRAQuddXjeGrptABSMPg8r0ztR5uAxl9kkAXVlDXNnXz6OcNH2RjwKXgppwHjNf5SFoT0KKMEm6t1nxWrBK4EMsedpQDJi8oD/S6kNFXFE5lZ15DbGljm6ik9tTLw73FIaZ2GrX4WyyMll5ws=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTcxOTUyNzQ2MjA3NSwKICAicHJvZmlsZUlkIiA6ICJmODJmNTQ1MDIzZDA0MTFkYmVlYzU4YWI4Y2JlMTNjNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZXNwb25kZW50cyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS82MTMzMjhjMGE0NmY5N2YxZTdhZjg2YzQ1OTM0YWNhODcwMDg2MTI3MjFiNTFiM2VmZWFlNzVhNWY0MGM4MGZkIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-601.5, 115, -12, -180, 0);
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
