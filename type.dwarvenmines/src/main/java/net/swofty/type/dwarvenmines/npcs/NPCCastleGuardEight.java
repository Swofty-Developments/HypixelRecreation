package net.swofty.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.stream.Stream;

import net.swofty.type.generic.event.custom.NPCInteractEvent;

public class NPCCastleGuardEight extends HypixelNPC {

	public NPCCastleGuardEight() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"<6>Castle Guard", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "TH8OMYtsYSNU/tXKaAYjcI5Uc0DbFJFQQjwBodV2B6G1YKfoIEq7zYXP1OYDo2XWEOVnAbFjxCNu402jsw366wQOaaWpi6S/WMXX5QwnG0CReeM5ss3xCiJWGKzpXVvlz87/k0qTMfNba1EpTlaNT+AnreDmTQKxqC2dkdk+7a/+x6wr3wQPZwQKJ1kRE6mcZ7eJGrUBaGVfpNf4050Vc6FFcG8K+IlbuGdQYB1ARZ+/mY95aGidnh+Z/Tl6BXPGl4UdF+e4LMwLThxJtJViM6/6h2t9cKnQ7udscegYSRKJ8cA+qLyMUDvez9zRZl9PvqBYFyOiXwKa2veACpWcwXWl8dMVPUmb9/i4ZvzjBisfOnqefBl8IYN78RL8cVerZ3ejIgylHD6vuQAKIo+or/TI0kHDYHNiDbOn7hrl6VZl5NgTk91Bkb5ANTOp/zr3bERS/KOsNNj1G2bP/P4UtTa48K5Bn/iwMKgV5suESM69L8J5q6cfxYHNPl2P7BgaN7LcJsbre8UrdDkeyFrCkqP0jXTAS4NhWwsFCau3Un2B8gyofPElPVW3Vcic93MSG9n/pRzbBaLdAjeRcQDp03wmWZaGB5FWm1WMItoIEXZwxC3O/F+AgbfukCJgjeZ6T9xVgscK0rmC51Q4fa9yrgms7CwOL9PMSQDYy/bNa9c=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "ewogICJ0aW1lc3RhbXAiIDogMTYxMDQ3NTQzMzc3NywKICAicHJvZmlsZUlkIiA6ICJkZjMxY2M2NTE4M2I0YmUzYmZmZmNjMDVhMDFkYjBmNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ3aWxsaWFtZ2J1cm5zIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzY0NTI5ZWQyZmE1YmJkZjJjZjYyZTA2NWUwNGI4ODU2MTcwMGRhYjdiZmU5ZWNkNjBmMDIwODgyNzk1YWNhMzMiCiAgICB9CiAgfQp9";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(108.5, 202, 253, -180, 0);
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return false;
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		SkyBlockPlayer player = (SkyBlockPlayer) event.player();
		if (isInDialogue(player)) return;
		setDialogue(player, "idle");
	}

	@Override
	protected DialogueSet[] dialogues(HypixelPlayer player) {
		return Stream.of(
				DialogueSet.builder()
						.key("idle")
						.lines(
								"<f>I am guarding nothing."
						)
						.build()
		).toArray(DialogueSet[]::new);
	}
}