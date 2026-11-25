package net.swofty.type.garden.npc;

import net.swofty.type.garden.gui.GUIAnita;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCAnita extends AbstractGardenNpc {
    private static final String TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTYwNDQxODU1MDQ5MCwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNGQ0ZTlkMmE4Yzg4ZDBlZmQ3NTBlODUyNzMyYmEwNWZhM2YzMDJlMjA0ZTJiZGM4OGRhZDYwNWUyNTU3ZTJhNiIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9";
    private static final String SIGNATURE = "kgTWKoHcjvKRS1D7bWAlG6yIYm+5y6xLnq4j1q8Q1UyeNyQkjaDBis+AiHGyQQQ+iyKC2iu3XTv1nXkxV1oYRwWFqg/aabA3S/FIEwppEZBrkDl+2zWNqFmwfsppPZ5zbsJHBvDm/c9RSlaKLEzeSO66KEyJDllRfcJWIGTg1xk5FxCf3hsgZ0QPynp3v/m0Pv9bmUtd88iuHus7kC76G41DDIQYm4xUOXY6E3i7AyqNX+fhl0EaLGg8DGm4mpfdFx0HVvOf5njXauhkTKCKMg7+WLQcLEHtPnnL8wSHOiNuzk8+tYbah2KzKJHjXSulWE4o5BGLgbbowPnLB3Nknzi2fwNnjqKNaoU1EZj3YpgPgpL4W6+fx7rScrt3gsGEso/7bHJwBBJLoYNdUL3XzJwI/z7sbFFukB28tL4kJ4Bc9eOduVopuaueioNcAHhPfxVp5wSrvNPq6r/c+yDBNHgOgcd3vn5iwWRh7Ls6tzY3bwUDqM7RUjIEhGb4shqDdMUSaS90eLlieZG9jpBVstMwHh5K2LXjIDeGH9sD9hFQaZ7G0OPvtlErRyoEXxnS1DxLs6Zcn/A4sxjFJbs4aoXweM7xpO2DmhdxCGYvMlAcj9KcCPkcYkwN5EM9Ws3EQWURIV37QNOWcd51vDmdH7f3GI6PVjbalS3esM9vgX8=";

    public NPCAnita() {
        super("anita", "Anita", TEXTURE, SIGNATURE);
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) {
            return;
        }
        if (!hasSpoken(player)) {
            setDialogue(player, "hello").thenRun(() -> markSpoken(player));
            return;
        }
        player.openView(new GUIAnita());
    }

    @Override
    protected DialogueSet[] dialogues(net.swofty.type.generic.user.HypixelPlayer player) {
        return configuredDialogues();
    }
}
