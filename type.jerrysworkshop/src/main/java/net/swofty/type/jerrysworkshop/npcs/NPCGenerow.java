package net.swofty.type.jerrysworkshop.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCGenerow extends HypixelNPC {

	public NPCGenerow() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{
						"<6>Generow",
						"<e><l>CLICK"
				};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "mV3RUDym+XkUrs4w9k7/pIFSXMD/ZZ+8L/cDQv3Mn6nSGbk+F/6GcyWx3GDX3uPXGuXzlP3k+WKaF1bvHaJcyYNQbCpPaMxGnD6jnCA4sJN5OJgUZRgXgiU7YyjjCOwYgU5FIPWBlOTNDy5LOShF01YCA1C5QIO3RhbvG3XuGU6q8fRFtRpVNieBWGRcf+cmnJi6lS/z9UVR5I/0fxo8yKAkZLWk7Uf8+BfQslZAN5aavoAZ5UaIpQIGwnF00bHPr4CTjmPe491rRY8jKtmqdSlu1QflQm84vwpVP2B2ZiNqtTR3rtSCwfm9w2RFdKUTXa6G43TA+I8DVJrcvdz0OegON9VPgN+wrSj182oV5FAzMXivclviuS0yPrvv/Kz4/LgC3nBa49bv4riDCFMwKOII9G3Ghy81XrUQreRpzZxwyCr9n3xHu+mGB9Zl6tcOyoMgOlxzukV5T7bKesz+3lQmK3xbpVs6KinURCoV29k36ITiLhBrRQavLcnRetrXx+tBlS6YeVjYkVKDUIjqK98FKPMdAYQnVrBdsGaxGOrQKmAH7A8BHNiLM5dn87v0nCMwfixnzfUPNyJ0WSHGddvX/Mj1XLtZajTnucatfNDT+80/a/Gv4REpZ4qIrM7NSHfOYW7QgDR1rYfEN8QqucXC3ZErmYh8jDf2bpkTyN8=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "ewogICJ0aW1lc3RhbXAiIDogMTY5OTk5ODY0MzYwOSwKICAicHJvZmlsZUlkIiA6ICI3NzUwYzFhNTM5M2Q0ZWQ0Yjc2NmQ4ZGUwOWY4MjU0NiIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZWVkcmVsIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2RhMDBkZGE1N2U5YjI2NTMxYjQwOTlhMGE4ZmM1MjZlZThjYjg1OTFlNmY5NWFjOTE1MWZiODRkZTRjZTJmOTIiCiAgICB9CiAgfQp9";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(-10.500, 76.000, 93.500, -45, 0);
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return true;
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent event) {

	}
}
