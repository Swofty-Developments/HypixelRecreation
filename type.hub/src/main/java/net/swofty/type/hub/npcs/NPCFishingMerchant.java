package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.hub.gui.GUIShopFishMerchant;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCFishingMerchant extends HypixelNPC {

    public NPCFishingMerchant() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<b>Fishing Merchant", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "piUjhfqpIwGQ/JbUxauA4SOGkH/2Li6Sb5y4ErzIob6Nv1Ai/BFyKac5aEroxh53M9a9Qdqz13tXnS03nlykCGXtlhFhJ/H/pEgIKGEFRvRmgF3L9R7u4ZYqZT7R89m6yruGCr+37ZUUk1rSF7idx7TtC0IFJt9k+QFOyQqtk2vjka8POPGQ2nXPqLnDaKi8TlAvlCIEWR3z0hSN4pLOupXFMF99a49GX0CWj7ewB6vJl+sEinNSYyviYPK7ncVYfJcn4gSN9EtsI1PQzAT2Tb3/EntzoBk88RLxpJNKjcySTzs2yALlRyUAtsn4QkOyoIBOkSEhixDY7BHd46ALllo5HwdzKjExc5JK6HUL/KdsRXIfK3ZEJ8QodhJUiwn7JkiMtMmLTpebIZwVWDQIB+1fz9KQ7bUq01OkQx4bGeIfg6H0nMRY/gPAVWSvAZbJwZkeJ811RbNbgHozfuvo2/Bq74KTiobxkD+M4MbfXfTi1XjawDZg+W/Jzb313cDL1EbgyU+DIDQUA4aPtoVcGFu3l9BoM8Yb+mD0XiMxwxw9cJGLS6f+xg5sG4IgK5RMpYjMuq9Mrt2ebu7nxx5JqjygJcxqy1/YQt4k4BL/D0tfZbmSjsQ45RZ7uMjP0FtALhFusLfykALCP78hYm8q4to8pDmIhvdrjssUVu0C9Uk=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1NTkxMzU3NjQ4NDMsInByb2ZpbGVJZCI6IjkxZjA0ZmU5MGYzNjQzYjU4ZjIwZTMzNzVmODZkMzllIiwicHJvZmlsZU5hbWUiOiJTdG9ybVN0b3JteSIsInNpZ25hdHVyZVJlcXVpcmVkIjp0cnVlLCJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTJjNGVlMDQ3OWI5MDFmMTc3YzI4Njg5NWRhMzEwYTgwNmRmZTg5N2M5YTg2NzhiOWRlODhhYmJiMWJmZjRiMSJ9fX0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(112.500, 71.000, -44.500, 0, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        if (isInDialogue(player)) return;
        boolean hasSpokenBefore = player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_FISH_MERCHANT);

        if (!hasSpokenBefore) {
            setDialogue(player, "hello").thenRun(() -> {
                player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_FISH_MERCHANT, true);
            });
            return;
        }

        player.openView(new GUIShopFishMerchant());
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[]{
            DialogueSet.builder()
                .key("hello").lines(
                    "Fishing is my trade.",
                    "I buy and sell any fish, rod, or treasure you can find!",
                    "Click me again to open the Fisherman Shop!"
                ).build(),
        };
    }
}
