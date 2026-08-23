package net.swofty.type.hub.npcs;

import net.kyori.adventure.text.format.NamedTextColor;
import net.minestom.server.coordinate.Pos;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.NPCOption;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.hub.gui.fishing.GUIGFishingShip;
import net.swofty.type.skyblockgeneric.fishing.item.FishingItemSupport;
import net.swofty.type.skyblockgeneric.fishing.ship.FishingShipService;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.stream.Stream;

public class NPCCaptainBaha extends HypixelNPC {

    public NPCCaptainBaha() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<6>Captain Baha", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "A9Wh529RWV2HEMvVnPzQEPfvT7p8m2GU8IB5FowBVYRash8GUSC6OvO88v5eBXAsCJvAauOnCFkp0DrxNTHUTS6E8rcGpo5ieHTr+QYglXIlA8S+rgA5eGODgI3LEtOZucHJ6H64a23Bu41lNMpN2c+LzQbisqC9WBnfVBxYo6qrzgh5JBGsRDIg2h3UKmTnNgJPuhN2cwRDDlHG8/k+xES5ZqyEFvdjGn6O5HHL6xyMkCukjZN0E8s03NkpkKxZXEm1M/Eg8EWtwGqZIa3DHNmxchYok4mDPMst8iRy4pGRlJN+VBCmGLIV7pq4QZlGzuXWplrX/PAOb+B36Rg67SHvmIk23tpnu+7uvB3rw9NedWY1+xLp8W4gPKpynOobSCbKiJ6bX0mCQfURVh2svFT5nG/VnKCL0TE8CUiTxOuxJR8QWwWRI4BMRMJQfQxy0mofvPnR5g1XUnHzvGWr4m44dmooqyCgB4W9iysADAEgc9CVtizjroopAJLXCtfsxwIuioHaZsBKQU1NpvpH55bPqf//RI9FyJJwOXTgX7fbF49z0eAgjnRAyF9VE9VYI2hFZwa3BIFnvdGxlZhE63QPB+nmKQMT0WzTz15lm77lxvvpQsurkm2gKr6FlL9+SokbTUuQmisyzS84s2EocpRscgc9JF1Dv/NjK7T+3GU=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTc0MDQxOTU1NzI0NiwKICAicHJvZmlsZUlkIiA6ICIzY2I3YTA3YWY3ZjM0ZWZiYTlkNGI4ODQ3NDM4Mzc0ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJBUkJVWklLMTIwMTMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvMzcxYzQ0YWQ0MDdjMDIxNzM3YWQ3ODkwN2I1MDY4ZDdiY2MwYzY1OGIyYTJmYmFiZjAxNzA2NTYzYmQ5NDQ3ZCIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(162.5, 69, -65.5, 105, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent event) {
        SkyBlockPlayer player = (SkyBlockPlayer) event.player();
        if (isInDialogue(player)) return;

        if (player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_UNLOCKED_SHIP)) {
            new GUIGFishingShip().open(player);
            return;
        }

        if (!player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_CAPTAIN_BAHA)) {
            setDialogue(player, "first-interaction").thenRun(() -> {
                player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_CAPTAIN_BAHA, true);
            });
            return;
        }

        if (player.countItem(ItemType.RUSTY_SHIP_ENGINE) > 0) {
            setDialogue(player, "rust-ship-engine").thenRun(() ->
                NPCOption.sendOption(player, "captain_baha_engine", true, java.util.List.of(
                    NPCOption.Option.builder()
                        .key("yes")
                        .color(NamedTextColor.GREEN)
                        .bold(false)
                        .name("Yes!")
                        .action(ignored -> handleRustyShipEngine(player))
                        .build()
                )));
            return;
        }

        setDialogue(player, "first-interaction");
    }

    private void handleRustyShipEngine(SkyBlockPlayer player) {
        setDialogue(player, "dialogue-yes").thenRun(() -> {
            var definition = FishingItemSupport.getShipPart(ItemType.RUSTY_SHIP_ENGINE.name());
            if (definition != null) {
                FishingShipService.installPart(player, ItemType.RUSTY_SHIP_ENGINE.name(), definition);
            }
            player.takeItem(ItemType.RUSTY_SHIP_ENGINE, 1);
            FishingShipService.unlockDestination(player, "BACKWATER_BAYOU");
            player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_UNLOCKED_SHIP, true);
            new GUIGFishingShip().open(player);
        });
    }

    @Override
    protected DialogueSet[] dialogues(HypixelPlayer player) {
        return Stream.of(
                DialogueSet.builder()
                        .key("first-interaction").lines(
                                "I was about to set sail, but this <6>Ship <f>is missing its <c>engine<f>!",
                                "Maybe <3>Fisherman <f>Gerald knows where it is?"
                        ).build(),
                DialogueSet.builder()
                        .key("rust-ship-engine").lines(
                                "Ahoy, is that the <c>Rusty Ship Engine<f>?"
                        ).build(),
                DialogueSet.builder()
                        .key("dialogue-yes").lines(
                                "Excellent! I'm the captain of this <6>Ship<f>, which means I oversee everything from repairs to navigation.",
                                "Apply that <c>Rusty Ship Engine <f>in the <6>Engine <f>slot by clicking the engine in your inventory!"
                        ).build()
        ).toArray(DialogueSet[]::new);
    }
}
