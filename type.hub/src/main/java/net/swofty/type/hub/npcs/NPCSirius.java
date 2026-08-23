package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.skyblock.auctions.DarkAuctionPhase;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.hub.gui.GUIDarkAuction;
import net.swofty.type.skyblockgeneric.darkauction.DarkAuctionHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.stream.Stream;

public class NPCSirius extends HypixelNPC {
    private static final long MINIMUM_COINS = 400_000;

    public NPCSirius() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{
                        "Sirius",
                        "<e><l>CLICK"
                };
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "wxO8NN0XVV66aolUjL9qhturPPfas7lvoxTGCZ9jkV+ojvREtBS2fYuFTNaF5kB+SZdnZLphOrLBAHlyMIsGS7o+MhBmtlQblJBafdNHN5CsA5LpsbaHwj+WZjqSnzCV2qVVj2gY7w7HLlLV1PEPg97TcRS/SjIZND08257kJOkrCRxJBLGfQ8ZhQWl0p4JF5S4bn+kP754L1fyUx0kYWGstIZGmMcyQkY0aOI251bRapVa3rs+rc0QtOkRyy38OputzTaCREQ3m3fpCZ5lxG0IvR4NScXc3V1EJKwvsEpXUYsy1ElrrbYle+2sZvHdPS4JGYN8RLcVBiOHcncZ2xSSlep2O9rui/jtiTZAIvUGQX6Fn4RSRbeI9qinOr8qhGOnKiPsBXx18BNgCRNcj1rCmwAqmHGbP0eMZ/KYMoVlHZWlXBd1DMN/7duEoy5MV59+GK2sOO2Qkz1C+TeTKRV2gLe2tkTJMukUYS58m4BBZPavdY8RZAnzn0EP6pwWTrb1ms9ZA+CovfFTdGLh4fGuHjrhQBvdldQf00gbbDGcWnOoiiEfsZ/azW1VmB08XpIvRlLDTTgjmI1avwrd0BMe+oRNt9wOLFnUbPZ+weIKPTT/iyrhthG3dialYr+GgTJGJ/+QHKJY9gWkDIcDrl3Iei3Va1tuI2VvEtf9wcy8=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYxMjI5MDEyMjIxNSwKICAicHJvZmlsZUlkIiA6ICJkZGVkNTZlMWVmOGI0MGZlOGFkMTYyOTIwZjdhZWNkYSIsCiAgInByb2ZpbGVOYW1lIiA6ICJEaXNjb3JkQXBwIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzY3YWUzZTI1MzlhNDQyNTE4MTMwMzcwZDNiMmQzYjE4MzdhNDkxZWUyM2Q1YWZmMjBmMDE1YWUyODRhMjhjOTAiLAogICAgICAibWV0YWRhdGEiIDogewogICAgICAgICJtb2RlbCIgOiAic2xpbSIKICAgICAgfQogICAgfQogIH0KfQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                if (DarkAuctionHandler.getLocalState() == null)
                    return new Pos(0, 3, 0, 0, 0);


                switch (DarkAuctionHandler.getLocalState().getPhase()) {
                    case QUEUE -> {
                        return new Pos(91, 75, 176, 180, 0);
                    }
                    default -> {
                        return new Pos(88.5, 50, 145.5, 0, 0);
                    }
                }
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

        SkyBlockPlayer player = (SkyBlockPlayer) e.player();

        // Check if auction is active
        if (!DarkAuctionHandler.isAuctionActive()) {
            setDialogue(e.player(), "closed");
            return;
        }

        // Check if player already in auction
        if (DarkAuctionHandler.getLocalState().getPhase() == DarkAuctionPhase.QUEUE) {
            boolean isAlreadyInQueue = DarkAuctionHandler.isPlayerInAuction(player.getUuid());
            if (isAlreadyInQueue) {
                return;
            }

            setDialogue(e.player(), "adding_to_auction").join();
            player.sendMessage("<7>Signing up to the Dark Auction...");

            if (player.getCoins() < MINIMUM_COINS) {
                setDialogue(e.player(), "minimum_coins");
                return;
            }

            // Add player to auction
            DarkAuctionHandler.addPlayerToAuction(player);
            setDialogue(e.player(), "added_to_auction");
            return;
        }

        if (DarkAuctionHandler.isPlayerInAuction(player.getUuid())) {
            new GUIDarkAuction().open(player);
        }
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return Stream.of(
                DialogueSet.builder()
                        .key("closed").lines(
                                "You shouldn't have of had been able to click me!"
                        ).build(),
                DialogueSet.builder()
                        .key("adding_to_auction").lines(
                                "Are you here for the Auction? Only the richest players can enter."
                        ).build(),
                DialogueSet.builder()
                        .key("minimum_coins").line("You need at least <6>{:,} coins <f>to enter the Dark Auction!", MINIMUM_COINS).build(),
                DialogueSet.builder()
                        .key("added_to_auction").lines(
                                "You signed up for the Auction!",
                                "You'll be warped once it starts..."
                        ).build()
        ).toArray(DialogueSet[]::new);
    }
}
