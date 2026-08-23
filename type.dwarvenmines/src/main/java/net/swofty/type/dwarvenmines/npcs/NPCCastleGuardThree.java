package net.swofty.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.stream.Stream;

import net.swofty.type.generic.event.custom.NPCInteractEvent;

public class NPCCastleGuardThree extends HypixelNPC {

	public NPCCastleGuardThree() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"<6>Castle Guard", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "a+pcnn8iWlcx5YYiy84rytjMnF5x6IukLezE8pXN03YqCozbLh84KX+fBION2VrPsr38GU92XlYaHQoeHhYVXrhB6WopJgnTxsw3Jl8QSbH5K63Z2pDJDu44qqsbeVhzTzT1UcHVi7blN7BYsb3kr/zwTj3KewbRUJDt+1An7+jjzqWHONVa9c/L5kZ+UYf4A6IgyOmnf8YtiPl9td4hqyxGGBYXDcMPqnjqlJEo2CfrLpRmho/2R9FgYB3I+a9ULAci8jP3Z5AsbAC6chQeb1Wawv8+Y9NAJ3M+k7JymHu1V06Cgc1BDxl2wwNVgOgDXPjsEM2HybPinZbdRzUjZWgseZOlQWcFLSKUZVLsowI9mqJOFJND7vYMvfcKeWFoxKhPND6ZKh8rm1ttxxdy0/yZ9gY2evXCiKVKoEW8M9KAQmQjNNejXAOYrBxtxXQ4cq+ZSvTRAo5v1n3PVQI3UYiMbIcpSNlx8k8v/Eyl3stY7f12NCuDJstPjna0zv5Ksp6NyPeUcymEsXWaMMvzUcvIQNsDLBXLiIPTGev9UYokiH+WQLrsnvvxycb4WwNrdXdW/xssC1pSPKHHJMtjfcCd2BNERQifhgzvZlXH3CuNt/7snlKyfV1olRun0OA3bI19sKQgzumMgsH5SLhLStTB+l4xbfqQfyc7OyMZSVg=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "ewogICJ0aW1lc3RhbXAiIDogMTYxMDQ3NTM3MjY1MCwKICAicHJvZmlsZUlkIiA6ICJkZjMxY2M2NTE4M2I0YmUzYmZmZmNjMDVhMDFkYjBmNyIsCiAgInByb2ZpbGVOYW1lIiA6ICJ3aWxsaWFtZ2J1cm5zIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzdiMGFkMmU1ZTkxYzI3ZGI1NWQ1OTE5MWNjYzc2YjVhYmZkODg2YTMwMzViMWE4ODViODEwYzFkZjQ4NzcwMDEiCiAgICB9CiAgfQp9";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(137, 192.0, 148, -180, 0);
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