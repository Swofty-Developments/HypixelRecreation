package net.swofty.type.bedwarslobby.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;

public class NPCDoormanDave extends HypixelNPC {

	public NPCDoormanDave() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{
						"<e>Doorman Dave",
				};
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "ewogICJ0aW1lc3RhbXAiIDogMTY3ODg5NDAxNTk0NCwKICAicHJvZmlsZUlkIiA6ICIyMDZlMWZkYjI5Yzk0NGYxOTQ5OTg4NzAwNTQxMGQ2NyIsCiAgInByb2ZpbGVOYW1lIiA6ICJoNHlsMzMiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDAzYjk4NmM5MWI1YTBmNDFkOWMxZjYwZDJmZjBjMDg5MWY1NzQ0M2ZjNmE5ODE4MjhjNzk5MTdkMzYxYmNiZCIKICAgIH0KICB9Cn0=";
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "trxs9wZmM9LNrGHE8fPtg+GFBXGPAvgbFGsbXtG5q/Q6g+QWrhcFFiS02iHRzYSquI11EO4qxntebojwU19UbWkxdENOJFfoEeiEJ8SEhnVrBtx8jLKn30oXKGJqu5cEstwkYjSnIIZGaBO8f+frPOQSsVbNqI0oCiA5915x6bezJunLsyDkpzATNDM0BBMPfCxhJjV0I2PLnJaKq6aeCBt+KN5lnZDRA2gMT6vvvmJxk09UiK2oYRi+rN0LOyvmN5yjdx1mQ6umEV5kd+2H7rCi4I7K5rgH5KR6rj4sh/xZfp7sH94frSQcs0bcIvxEqC46jBYtih8Z0orbIHAQYKa3P0REEZsCIV0yMiclctl18KilgtE5UYbG2kjk9+uwriselDcLY0fJh8TFiIFevATyq/Fsx7oJLnJhQszAxdVujEE7yW/w8/Wi5Q9IYdq4850cxIjY/mz2NJPVHDCZfxXpX1UyNTCUWAvW35S2jmCmqLQ4jjgkkqd1ifpJpZdtpOrTd9iNGHXmAQOVQyLFhxh+A4cvhLIzuNYcD84c/u/Wnuoa9gUs1XmcAfXgTDFH4eFUaULyVXW/hUqWD2dsGB7E5bBuBTOm2JoQnFZTWJH4IrshMj/Wojp/Fx9RQjmz6N9IXV9X9WL4YgPKZ/BBVoZQcUE3KsV909K9QxpQDjE=";
			}

			// The position of this NPC changes after the player has talked to this NPC, but there might be a quest involved.
			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(21.5, 69, 1.5, 90, 0);
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return true;
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		event.player().notImplemented();
	}
}
