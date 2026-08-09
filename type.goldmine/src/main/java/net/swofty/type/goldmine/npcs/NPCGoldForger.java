package net.swofty.type.goldmine.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.goldmine.gui.GUIShopGoldForger;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCGoldForger extends HypixelNPC {

	public NPCGoldForger() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"Gold Forger", "§e§lCLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "LDlv2TZxU3LQIvO122hiA5sgcOr6q1uMlzpFY4g3l29+O6sjGJdJlL+IWqpSz5raBg3duW+kky8YhJgBKPsc5cLZPj9EqbSgF82URLIXaCdCEtouZNnqVBn6hb4ToserxU6O19LjyLDtO7IqzbfL07srAjWtV729awUUkhjcQa6QNeJW6d6EE37stRIlPcatkDuISB0IR7oZTmDtS3YNqNjSj7aqFPCuqKUJnExZ5OO1Mlgu0urP6AvMuS+6Uwr22ZwnW83yd55XCRMxVxN7Ehwal99wsF+EIOPcoUa0yG1hvlgoJuzjQwKpjLFQ0l9TnoAP6Gey4bn1fKyacFLARMZbuzzyn6ByUwCbuiqAbNJEx9MIOohJVQ1UR76StHpyVmywtx4q7CWucrMuwwh/315qIYB+wzxiycWLLBXVX4xdc1q6mn/K1loC787hAAconAI5s8jmK0tgfYXAnRON+oc2VWRwNtf1D2bJWTZVrsNpwCDEcAd7FuSlN2dQASEdByfF+aAc5T5zlXjZ4dtf5LZnQLqZYOznRFsaFIsWOcWbZn/qwvw975CXwY1a5GIPug29ocyiAEvQWd6jn0DhdxTW9myxFhLlhtwBXcWXM8HDhixJuy2YV99tQk32gk0HPA3sy+2SZDQZPRhDQFCf9fqdbQX/44lTnvtH0vt804g=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "eyJ0aW1lc3RhbXAiOjE1NTk1OTMyMDkzMzIsInByb2ZpbGVJZCI6ImEyZjgzNDU5NWM4OTRhMjdhZGQzMDQ5NzE2Y2E5MTBjIiwicHJvZmlsZU5hbWUiOiJiUHVuY2giLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzM3Y2EwZmYxNzkzNmJlMzNlYWEzMTc2MjRkYjQ3NDRmYzNmMjkyZTYxMjM1Nzg1ODQ1MzI1OGU0ZjllN2NhNzkifX19";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(-27.5, 74, -294.5, -90, 0);
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return true;
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent e) {
		SkyBlockPlayer player = (SkyBlockPlayer) e.player();
		if (isInDialogue(player)) return;
		boolean hasSpokenBefore = player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_GOLD_FORGER);

		if (!hasSpokenBefore) {
			setGardenDialogue(player, "hello").thenRun(() -> {
				player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_GOLD_FORGER, true);
			});
			return;
		}

		player.openView(new GUIShopGoldForger());
	}

	@Override
	public DialogueSet[] dialogues(HypixelPlayer player) {
		return new DialogueSet[] {
				DialogueSet.builder()
						.key("hello").lines(new String[]{
						"I love goooold!",
						"Click me again to open the Gold Forger Shop!"
						}).build(),
		};
	}

    private java.util.concurrent.CompletableFuture<Void> setGardenDialogue(HypixelPlayer player, String key) {
        return setDialogue(player, key).thenRun(() -> {
            if (player instanceof net.swofty.type.skyblockgeneric.user.SkyBlockPlayer skyBlockPlayer) {
                net.swofty.type.skyblockgeneric.garden.progression.GardenProgressionSupport.apply(
                    skyBlockPlayer,
                    net.swofty.type.skyblockgeneric.garden.progression.GardenProgressionReward.spokenNpc("GOLD_FORGER")
                );
            }
        });
    }
}
