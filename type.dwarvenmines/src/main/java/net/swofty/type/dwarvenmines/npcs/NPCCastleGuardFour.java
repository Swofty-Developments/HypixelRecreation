package net.swofty.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.stream.Stream;

import net.swofty.type.generic.event.custom.NPCInteractEvent;

public class NPCCastleGuardFour extends HypixelNPC {

	public NPCCastleGuardFour() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"<6>Castle Guard", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "VjQdC7UTHxhLZhbY+hLJ4z2JkQ7zajwlgn/JUqaIGRMGLUTUfx9DUzLTALl4D/L4EpQPwfaOJx/lXDy3U88oNhaTEG/62VEYidKmI//jw5uc8UDbR5soYqAuoINcnqNU2iuNLJ/5doCHDtV+oKqs/IQlXMy5jXy5I31yeNB2Ke6LOaDdKRYRetUA5eABmVFfAdU37bZbRXafNUSGS5XXzP38dpRwtdNFKUvsj5mFM4reIVR/zIIzWRpbXvXrQLS+71VdNngsV9YlexxPAqcDRjAN27FQiamim6KF7JQhtNROuI3Bwj4V4QnK0P8xFr2E+rG6P8ZFiQN33U8xEJ8x8fVQp4LXttw5aq4as/9vu5b2fY7npEMlDveYjDZ4U5qFI2Cnc6q3lWU5FHUblv+hxACZ7YmlwXpG8ZcaUi5dKbDa8IfXZft8eaoXO/Tm//qOC2yx4byNU5xhpRZ9SA81sFaQAZQfNbbWE+uuF1pS1wymQ3nU3mpqIXpyPJ1tzNNFgDMfDsSiTW2WXJmF2Bi8PNXsOyKD22ZKE4qumK6W4OMHPsKH/SGnvm8fjpVBIP+z8MCkvuUF3jkfHAFfIG3xUSLDkEB2Mx6uMru5hBzNa1lX4rdhz0z2va4WHwnLoav5TAXJo8mYg2I4fQOFOsoI7JLJGjxMoahcb4n34dxxgbM=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "ewogICJ0aW1lc3RhbXAiIDogMTYxMDQ3NTI2NzU1NywKICAicHJvZmlsZUlkIiA6ICJkZjMxY2M2NTE4M2I0YmUzYmZmZmNjMDVhMDFkYjBmNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ3aWxsaWFtZ2J1cm5zIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2E3NjU5NmFhNjRlZWYwYzEzNTQzMzc1MWNkNzEyMzBjZmViMWE4ODFiYTNmMTEwYTAyYWE1OGM3MTYwYzBmYzgiCiAgICB9CiAgfQp9";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(122, 192, 148, -180, 0);
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
								"<f>I'm guarding the whole kingdom."
						)
						.build()
		).toArray(DialogueSet[]::new);
	}
}