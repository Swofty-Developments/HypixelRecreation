package net.swofty.type.deepcaverns.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.deepcaverns.gui.GUIShopWalter;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.entity.npc.trait.NPCAbiphoneTrait;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.utility.ScheduleUtility;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.stream.Stream;

public class NPCWalter extends HypixelNPC implements NPCAbiphoneTrait {

	public NPCWalter() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"Walter", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "pYSyVXmvIReEOZA6g7/m7dA1WCdC/GE9yj/78LN/cEwVwUf7OTYwadV9nv4DHaQSvw9qICXff25/ewa2ayer/HHhl6/9UX7nso/CZdc4nlyDkA9v6ywKu5yChgWHdpJKbYoDolIdOoYj5TFSAlN0rewNlLivEK2B+8BCugeNJbEiIcgmnq7sdovh7M2r6VH3ZXG7mvC6nq37aMsKxrT0mjkzRnTAMGgYv3Tdnu8kzcAQTBJWejdT4MsBBytcFPquQXdlt36+ng7n3ERGzB3F8nIdX2smAjft19hCfPbrbQQL4WRzQi2fyrACnYIaNACRnlhnu55+UGFYlUrdGeCEHmjLz0aFsipnmmq5xFAOyx3Vr/EAbyy5hSBN316yMeklDCgiGIqPgvjMTdpSMqglNEzgqxT/s2NCDbjpmdcTP8j44MfWd522ehAA/W+g9BIto2ERmF3AFm8NqA6LEjs1jyNOEAuL0rG7KoJ+p1bEvGW6V/2Z7zEUnF/KYAQpTmTsOWBssUFvFHUAytzoTABBuWFIv39Jb9uaLljMhWGYknwIPMmmnkw2UYzqvjbZ3VpHBoEN2FlGXGVe3K/pmSK/wrfR61vq+vQ2w7eOfmyx7GXqPTYRGdDJbuuTs0fpUDkx243Gjjt1p58zSl4l2tTAEji3yjWf4lT11y+hmBbYQqs=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "ewogICJ0aW1lc3RhbXAiIDogMTU5MzgyNTM2OTAzOSwKICAicHJvZmlsZUlkIiA6ICI4YzUzN2M3YWEzYjI0NWFmOGQ0ODY2YmIxZjhjMGU5YSIsCiAgInByb2ZpbGVOYW1lIiA6ICJCb0F0TmVlR2EiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDNhYzIxYjY1M2EyNzYzMmRiYzgzNzNjNmU2ZmJhNWI4Yzk3YjdlY2JmZWYyZDc5MzYzMGQxNDliMTE2YmE4MSIsCiAgICAgICJtZXRhZGF0YSIgOiB7CiAgICAgICAgIm1vZGVsIiA6ICJzbGltIgogICAgICB9CiAgICB9CiAgfQp9";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(19, 156, -36, -25, 0);
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return true;
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		SkyBlockPlayer player = (SkyBlockPlayer) event.player();
		if (isInDialogue(player)) return;

		setDialogue(player, "none").thenRun(() -> {
			ScheduleUtility.delay(() -> player.openView(new GUIShopWalter()), 20);
		});
	}

	@Override
	public DialogueSet[] dialogues(HypixelPlayer player) {
		return Stream.of(
				DialogueSet.builder()
						.key("none").lines(
								"With the right tools, you can get through anything!"
						).build()
		).toArray(DialogueSet[]::new);
	}

	@Override
	public String getAbiphoneKey() {
		return "walter";
	}
}