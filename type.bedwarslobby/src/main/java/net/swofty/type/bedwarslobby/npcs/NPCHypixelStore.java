package net.swofty.type.bedwarslobby.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCHypixelStore extends HypixelNPC {

	public NPCHypixelStore() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"<b>Hypixel Store", "<e><l>RIGHT CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "JL8Cj2xLlvL1aehfBhAsQzJWeAfxpaXbEWpdr1spMxAWLsCXXv7YdQ5kHpUYP3XUaeqsUMFhjRcbZJriXdkh7hDenYZuPEjXLcyvIPI42nrpxBybGxB8HmAu3pfWfuo04Y2uTi3xpF/oqOPWWH4zuMTYhNff1dTcJ7KQYkg51xfwJ4uIhdhNfvbcwKldH4rtspx9rUZE4PPfUdoKeBs4s6NanwVLZgiQlJ7WJXwMpxMeSjqrIuysUuKblVjUDAedujw4/v8gcljqIqrz+/1/6heTufyk973HwgF+VjRAl8A3ioNX6btkafvdOa5fMRKFpzYGYsGckEa+85l1M5CXTSdgh0X7HL3mNcYnjbVHNxhX5Rmy1u5FzoOualTbD/jU1uo6FVbJxVKdlwwRNO3QfXYSP4ENIzdGCAw6a8LLWvxUjmlZr6XuFXlNVzz0bz7Smzl/brsydD594crY4K1vxjdKBO5cBi+PNJcqZAQIffiGC+91hkhL8+cba/vSTKdzYCldK+0/MUy/n2h2vcqY2VwAXwkUCElnTCc55l8k2qWENmEB8QZ3siPJfo2XjTg+wUaDZdsnGm/bikjf/e1xdCxPF1//pYwr3C90sX/dusq/5h+buSvHxt5bGVCTr5AkpmSlzmy5c9TQrYItTBhU+iIOYWjHAwZYhBfHrwVDSjk=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "ewogICJ0aW1lc3RhbXAiIDogMTYwNjY4MTkyMzQ3OSwKICAicHJvZmlsZUlkIiA6ICJjZGM5MzQ0NDAzODM0ZDdkYmRmOWUyMmVjZmM5MzBiZiIsCiAgInByb2ZpbGVOYW1lIiA6ICJSYXdMb2JzdGVycyIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS81YWEzZjVhNWFlMmM2ODkzZTE4MDRlY2YzZmFlZjQ5MmU3ODExZWU5NTcwOWNjMzlhYTczYThlYjMwMTIwMDhmIgogICAgfQogIH0KfQ==";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(11.5, 69, 20.5, 90, 0);
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return false;
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent event) {
        event.player().notImplemented();
	}
}
