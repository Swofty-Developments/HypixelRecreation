package net.swofty.type.bedwarslobby.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.bedwarslobby.gui.GUISlumberLocations;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.user.HypixelPlayer;

import java.util.stream.Stream;

import net.swofty.type.generic.event.custom.NPCInteractEvent;

public class NPCSlumberTourGuide extends HypixelNPC {

	public NPCSlumberTourGuide() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{
						"<e>Slumber Tour Guide",
				};
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "ewogICJ0aW1lc3RhbXAiIDogMTY5OTMyNTc1OTQ1MywKICAicHJvZmlsZUlkIiA6ICJiZTExMTBlY2NhN2Y0NzhmOTg1MzVmYWQwODQxYzlkOCIsCiAgInByb2ZpbGVOYW1lIiA6ICJpbnZpdGVybG9sIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzQwZjliMGYzYzQ0ZDkwY2Q0Nzc3YTI0YTNhYjE5Mjg5YTE2NGI3OGU0ZDE5Y2FhYWIyZTZiN2JjMWZjZTc5YmMiCiAgICB9CiAgfQp9";
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "W3uZAVjAJ/DxAgpaWXTw+Dfn0cvzFlgOF+l1zxz6rm9RvuYqSm59+HkeLhCV6yKmJozta7Ki29cK+oeL0Ex29x9CwhwFhMu/jHg9+Pa7Q+ZFr8/vqHlfmPuqYw0wJCnK6IEOp3w+42z15XKWdo6XG9Jlwkq4+FBHnLHH42PMdjo2ue8yVmyYsDKXBPtrXkjobhYBHAJmbu/IWr18Y17k9NCAwU0diVfZRVkpjo3+HA4LlaItrqhzonXrki4cy5dtqSMvLLK13K+NH08DxX6eicUmFjzCYOMhXH42pkn4ctJ26nfOQa+/ccuHk/mwBF0nfhmGJLqq2ArP+JA+XSJYkAO8ssHrqyrERz5kShZg0oURKDOIUlX2PyKycbnL7iSHUGUS1yf/4QQp1EyRnMR/xVSZlyYEZToS2aO6L1/Ztl+5WmtmKT7ksIqGn45Pj/pG5TmMq4EmeeL0DylAVi2IGH0HEralptlyzM4hYELBFLXhM+FgOyBuah2a6Vwjc552JYf4kXJIqcrtDwIrMI652PqJReG60o9Bg4384Pd2CkAxxCUw/E1XJOZSIV6e7HN09Iu2+M+TQonPOJKATibioUrBIVEq6rJ8Z7Qsyx9WAws2afNa+kbFuSa7ywvUfL14urxvhVg+6CkUTx1php1CwlPloB7ZF/75WTvNGaGxpvE=";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(-28.5, 68, -8.5, 0, 0);
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return false;
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		new GUISlumberLocations().open(event.player());
		setDialogue(
				event.player(),
				"" + (int) (Math.random() * 4 + 1)
		);
	}

	@Override
	protected DialogueSet[] dialogues(HypixelPlayer player) {
		return Stream.of(
				DialogueSet.builder().key("1").lines("Where to?").build(),
				DialogueSet.builder().key("2").lines("Top o' the mornin te ya.").build(),
				DialogueSet.builder().key("3").lines("Where can I take you to today?").build(),
				DialogueSet.builder().key("4").lines("Tips are appreciated!").build()
		).toArray(DialogueSet[]::new);
	}
}
