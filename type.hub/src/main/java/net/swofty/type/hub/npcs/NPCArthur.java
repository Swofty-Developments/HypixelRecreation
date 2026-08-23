package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCArthur extends HypixelNPC {

    public NPCArthur() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"Arthur", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "ph8zOGN/cLhHpRWWG6YOFEC4SyDqgCi5WjutZUXWdP1bBSUZLhYBMp8oMOS/wlnt5MIc8RW7Gy3feu26BpiFthpwYXEytK7pqRbszY+TvjlhA496oiEtWliu+MepMQPe4fODyNaRwfis5N2VvbzoKQU2DJ0PZvijrQufe6z7Rai2LesFQeZj3LC4UMqQkyJWkZ8VAMSvvldbMdvZ7osZSGgOVBYi97KyRvgAixxADBNlKpdGmJCqJ3GFEA02enI6VisrsT4Qe1lcSURUu9gUt4PwCGZBN3o/1AJV4R8/QUHIWz+3cx1oWy2MEmLBWIPO6SLqWdyXb+g3IPlQpZGaEINNXHhVsubGscB5fpQzqJCYgX/dHAGffCZLgihjB1eodSvQHV4l+ByQRzp1pf8hHMNBwPXwNleAPYaULeHrEsgpcc4KRzsYWxDn49foTDsUnwmjinZrU7w9G1a/lpMb2HrMDX0gu5P8hYHzfOLgMgm7lNOw9KLqoWVH32vK7aBrlCrEVBXHo2Iv5KR5esvjdNE5prdXj+JoGtLPod2KM5LFsUWLu9D3eRVN3FkxmKWAL7YVVxZNcpZmPRzZllx4H6rNAy9dUrwNkclkyJcACKrs0AOehRjWNfSfhd376MaTC/HegeTPvPQ7tppj/VOb0EbmNcXqL0+Oqp+9SoROZ8U=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "eyJ0aW1lc3RhbXAiOjE1MzY3NTkzMzY3NjQsInByb2ZpbGVJZCI6IjQ2N2NmOTRkY2UyYjQ1ZTY4YmRhNTJlNTUwMmU3M2U4IiwicHJvZmlsZU5hbWUiOiJMaWxpeWFfIiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS9jYzJlODE5OTA2ZWIxNzk0MzliOGRkNTU1MTEzMmU1NGViNDcxNzNlMGY0NTg4NjFhZDJhOGM5Mzc5MTgzODkxIn19fQ==";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(53.5, 72, -111.5, 55, 0);
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

        setDialogue(e.player(), "dialogue-" + (int) (Math.random() * 7 + 1));
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[] {
            DialogueSet.ofTranslation("dialogue-1", "npcs_hub.arthur.dialogue.dialogue_1"),
            DialogueSet.ofTranslation("dialogue-2", "npcs_hub.arthur.dialogue.dialogue_2"),
            DialogueSet.ofTranslation("dialogue-3", "npcs_hub.arthur.dialogue.dialogue_3"),
            DialogueSet.ofTranslation("dialogue-4", "npcs_hub.arthur.dialogue.dialogue_4"),
            DialogueSet.ofTranslation("dialogue-5", "npcs_hub.arthur.dialogue.dialogue_5"),
            DialogueSet.ofTranslation("dialogue-6", "npcs_hub.arthur.dialogue.dialogue_6"),
            DialogueSet.ofTranslation("dialogue-7", "npcs_hub.arthur.dialogue.dialogue_7")
        };
    }
}
