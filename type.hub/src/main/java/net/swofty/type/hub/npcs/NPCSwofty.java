package net.swofty.type.hub.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.stream.Stream;

public class NPCSwofty extends HypixelNPC {

    public NPCSwofty() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"<c>Swofty", "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "hKuUWIcUX7jvLVHWVeY5zyWxGYVf09PR+eAu3RE8NcnM900GJ3npZTM/615g9M/shrKU3Fab1mazE6+hfNfGs5H+CJwEqzJE972ffidiT2iPJl1BgroJGkZAgCgGkc9xDaOkFTKc3E5w1iDiF0Y52BditPkdUP8oF14AEHVLfKnKdPPhnE4+ePtD0eGbsVmS3plg87MD0BpLmSWnIIjDamWIcQSrSl6h9yI78QuWbxzO3G5Z6AOpiiZwLLD7smm3P9MOWPwBDhbjC9G/3x7tI5uLyquqOL1eVIqc4/RK3B05prYR5gKZbiFmDoQSiMXs94iRcnofT3zJvAJ5LQdUMA7geFq5rZbu4+MkDu5eo0Yb+xW1Djewr7NnbKwpiaWxddUgLlWqbRtL602x/PHGin0Zh6qEYWtGZhSJ/9NjZKvRdiED3VitU5MKMyp4LaQEKVuaMfzf4m7oT71ZcPp0Sys68yMONOS/1Sd3UROLeaoWIVUZydgobxPUqYfH506LeW9nQKe5W7aggeRBnV6edbL9hYV1gIph2p3HLoBoDqiNO7LFlm9Ke9KRldrZ0LhrxEo7QWjXSBagEJ3IkEkUDGDs307NMX3ThgxImwG0KLFSToN44QxZfUMDtYll+btz0pcDMnTWEOafTnPoZJE8ui9DeatO+sl2akbPypGTe9o=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTU5OTQ1NTM4MzA0MCwKICAicHJvZmlsZUlkIiA6ICIyMWUzNjdkNzI1Y2Y0ZTNiYjI2OTJjNGEzMDBhNGRlYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJHZXlzZXJNQyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8xODYxNDI0MWI5ODAzMTljMDJmNWVlM2FlMWE3ZmM3ZWJmOGIzZmRkNTMwMWVkM2Q0ZTIxNTlhODBkYWUxZDJjIiwKICAgICAgIm1ldGFkYXRhIiA6IHsKICAgICAgICAibW9kZWwiIDogInNsaW0iCiAgICAgIH0KICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(0.5, 79, 20.5, 180, 0);
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
        setDialogue(e.player(), "hello");
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return Stream.of(
                DialogueSet.builder()
                        .key("hello").lines(
                                "Make sure to check out our discord server at https://discord.swofty.net/!",
                                "Feel free to create a pull request to help us :)"
                        ).build()
        ).toArray(DialogueSet[]::new);
    }
}
