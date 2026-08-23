package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.EquipmentSlot;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import org.jspecify.annotations.NonNull;

import java.util.Map;

public class NPCLotusAtoll extends HypixelNPC {
    public NPCLotusAtoll() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<6><l>NEW UPDATE!", "<2>Lotus Atoll", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "J6ay+WTyBtlt1HbVqp1Vm2waUTZ/V8uV6gNhpjBLtSZOVWhIhzl0eHN+5WlBOXEM/YXE4Nk8MR+eB9Reo+eUyEEVoXfPE2yHreob7PjERYTF5wUO4r8qdEL1a/bKzDfMNqCuMHnahdvP1Tx8xTfXdB8CIjleCYrC1/mOve8zlyYRsKi78v14dVZMBY0p8s6Qn2FqB5Fy5w07XHORVfETQmiMLIkpV62/btin/sAN4nfhRoGNXzjGdUHTXJI87JSlLgHxnuHscR+o6BhbWSoYghiy0rxO+O9EY1T1TAb1LL5k1s4NvkxRw8KIRs6naSKnlkH8gRnjjIewM/2LVE+vPXWOYQJRmFCwONUtQdija/es9uhK/4f5ZkTkjdaOvtGJmVP97WIk4kEZ6vTSCdDJ8hsVUv6pDuYDJx2CUR8suNwbXcDflb2ZSOtdYiyEQfm//GOady41vXX9Wgz8z+OD1psFGlqeDb3EocS7Snt0GPs+/Oeb5j+tBFh+v3nPU0XksAx5Fq8jWpvKLUgz0Vx/pIZYFykzr4Gf3pfTe4G4abix4HNxW0EIKtgf+snS6JItVZ85h6O+cYS/tttZ56qbfRpkxd1RS3D4X+B9ilQfTxe/J7ZZLOvelyj9slbvTurBUhAUcaL72oxHPjwpahTSijDC/a/2CDxUvSnzMvPAB94=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTc0NjEyODYzMDA1NCwKICAicHJvZmlsZUlkIiA6ICIzNmU5MTE1YzBjYzc0ZjhkOTdmOGFjNjA1ZGMxNGVkYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJEYXJnaVYiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzIwMzQ1YzE4M2E4MGZkYThiZTNlZjBiMmVlNjRmYTgxMTVmN2FjZGQ2MTA3MWI5ZWRhOGM5YmFjYmIyYmZmZCIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(29.5, 70.094, -21.5, 39, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }

            @Override
            public @NonNull Map<EquipmentSlot, ItemStack> equipment(HypixelPlayer player) {
                return Map.of(
                    EquipmentSlot.MAIN_HAND, ItemStack.of(Material.FISHING_ROD)
                );
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        e.player().notImplemented();
    }
}
