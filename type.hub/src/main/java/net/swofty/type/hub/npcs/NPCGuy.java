package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCGuy extends HypixelNPC {

    public NPCGuy() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Guy", "§e§lCLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "Wt6So7YJ+kxD3vUeu5scgT4SjDb1SwK90lD02L8+PlKyr7v0af8H0DRMt3aF9EEJBZdwz64jEzDEVrG0ZakyM8DuA554pwRp4G3jjQ+uYAxmYK7oaLPuhF46IIhhu5efNRgS9e5NaR+frPMFP+UuR+MthVL3G2BHfwLpUthAx/lEV0CyVYNLvs8cUnGnnl/nm91UOzQjk054RRdDjUS3O8dt7BWo2yykACo/cKgyzhNvy4b/cJg8aB3f/VMQYPi4x/Yuza7OqQWWa2TPFvYk8l4eOqz87qwkAE0yQoBcNPgyUT6R/uJpHAwWonMUkkRzFf+jO1HT79ltrxD1ptghiYmpULZjIIaPL46aqZ78c3N3e4/YezFPl02hKq61GQ7QjwgV7gBBijZHGhygUrv9QiYw6Lxgz3jIF1vE0pYRALAMJHw14EkS/3Oduh0J/ttJahBH+GkhJe0XhLKfvjcEvhqVFBJItbb6cJFmueMWT+G5aYWL0Yq9YvrQVA2KGDaaZ2XLXqNy5BFvJ4NFWkbHVMOFy0PE7jQ7bSr53Bhg8kwXJrcIHBNNNtYk+oD9EiKLgWiyV7//GA9m9nH8HIF6841YZR4MGjJ7POav85Wlx2nXR6TRbTgduImkYZTa5lgvmQL2gF+m/e3cLq3AR4GB35YMVapXgqWXnYKdQoOO5+k=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1NzUzMDMzNzQ3NjcsInByb2ZpbGVJZCI6IjQxZDNhYmMyZDc0OTQwMGM5MDkwZDU0MzRkMDM4MzFiIiwicHJvZmlsZU5hbWUiOiJNZWdha2xvb24iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2U5ZTIzYmU3ZjA0NTU2ZmEzMzM1MWE0Yzc3MWEzZjA1ZjRhNmQyN2RlNDEzYTM2ZDAyMzBjNjFmNzE2OTg3OTkifX19";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(51.5, 78, 20.5, -180, 0);
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
