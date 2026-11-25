package net.swofty.type.garden.npc;

import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCPamela extends AbstractGardenNpc {
    private static final String TEXTURE = "ewogICJ0aW1lc3RhbXAiIDogMTY5NzU1NzEwODk1NywKICAicHJvZmlsZUlkIiA6ICJhNWZlYWViNDdhYjA0ZDZiYTk2ZjMyOGJjMDQ3MDZjMyIsCiAgInByb2ZpbGVOYW1lIiA6ICJYeW5kcmEyIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzJhNjA0NmNmODQ2MjgxMzU1NjNlMmM0OWVlOWUxZmJiZDExNWY5MTZjOTZhN2E4NWVhOGU5NzM4NzY1ZGExNWIiCiAgICB9CiAgfQp9";
    private static final String SIGNATURE = "BRvZNsc8BauvahIbhZB48WNhH9is7pqpfQOib87m2uBk6UylprDpOd9Dt/1jNweJS4Fn/34EVNqQZIpn5cmbV5viHf+/VO+z66Dy5Jb1cYi2721q+sDXCKvdzuIQDrDtD7frrNTD9n9goJSk396rL8vjIkwt7D6rLLfdo0rkzhrTOm4OEVWsvPqdQNvwaeJ1RJFxiOj4Fs6CjwlB3ndZlI0h05cgC54EOrmvIJ5sMIAiPE+rXtvCI5WviTcZRpOGfhuP7OF9gUxns05hbT3jHXTiX6CmvXUL8lW7mo9MJlJ7qRwwvCC4ybGWTyde2d5RRXINKR/IWul4oWv8zsmmB482L368fsdI3A7vXIuxxijv1A9zNj8eydeAP/V/VOx/tQl9XD+Sk1FOwkjNsB0wSLjNoRH7kswejxh4Gjh5ZjIDsF8xdIDb438PQcJ6eK2WQd48dnNJ7yNGCLyAQ6YrkHvn6l+Q9nok4CDJeLSidIO+6aFpVCLeG0dGO3wSDm633wxo6a6rtawgpubKOR8gEXWXbmSENk6I5SWXAuGtk6FcLBXwhZRyN3jbbASyBBtJMzlLml+sOeLKfu7cDz1fSYVJBgamVwUAGgwDquoaHuwX2wEeCiI2cRoe/n8jMzuCvUCRlvb2AZmjSpaKFG6YNWbVxxlbZKvwmDHrgqsj3BI=";

    public NPCPamela() {
        super("pamela", "§6Pamela", TEXTURE, SIGNATURE);
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
        sendNPCMessage(player, "Anita handles medals. I keep an eye on the contests.");
    }

    @Override
    protected DialogueSet[] dialogues(net.swofty.type.generic.user.HypixelPlayer player) {
        return configuredDialogues();
    }
}
