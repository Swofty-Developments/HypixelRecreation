package net.swofty.type.jerrysworkshop.present;

import net.kyori.adventure.nbt.BinaryTagTypes;
import net.kyori.adventure.nbt.CompoundBinaryTag;
import net.kyori.adventure.nbt.ListBinaryTag;
import net.minestom.server.coordinate.BlockVec;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.Instance;
import net.minestom.server.instance.block.Block;
import net.minestom.server.tag.Tag;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.data.datapoints.DatapointPresentYear;
import net.swofty.type.generic.entity.BlockDisplayEntity;
import net.swofty.type.generic.entity.InteractionEntity;
import net.swofty.type.skyblockgeneric.calendar.SkyBlockCalendar;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.tinylog.Logger;

import java.util.ArrayList;
import java.util.List;

public class Present {

	static List<BlockVec> positions = List.of(
			new BlockVec(-1, 76, 81),
			new BlockVec(-9, 76, 104),
			new BlockVec(-1, 79, 113),
			new BlockVec(-34, 76, 81),
			new BlockVec(-25, 76, 69),
			new BlockVec(16, 95, 96),
			new BlockVec(26, 86, 43),
			new BlockVec(-17, 82, 30),
			new BlockVec(-12, 76, 9),
			new BlockVec(9, 77, 9)
	);

	public static void spawnAll() {
		for (int i = 0; i < positions.size(); i++) {
			spawn(i + 1, positions.get(i));
		}
	}

	public static void spawn(int index, Point position) {
		Instance instance = HypixelConst.getInstanceContainer();

		Block playerHead = Block.PLAYER_HEAD.withTag(
				Tag.NBT("profile"),
				CompoundBinaryTag.builder().put(
						"properties",
						ListBinaryTag.builder(BinaryTagTypes.COMPOUND).add(
								CompoundBinaryTag.builder()
										.putString("name", "textures")
										.putString("value", "eyJ0aW1lc3RhbXAiOjE1MTQzMjM1NTc0ODYsInByb2ZpbGVJZCI6IjcwOTU2NDU0NTJkOTRiYTI5YzcwZDFmYTY3YjhkYTQyIiwicHJvZmlsZU5hbWUiOiJIaWRkdXMiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzEwZjUzOTg1MTBiMWEwNWFmYzViMjAxZWFkOGJmYzU4M2U1N2Q3MjAyZjUxOTNiMGI3NjFmY2JkMGFlMiJ9fX0=")
										.putString("signature", "biaiICm45OenpyzFGCo1Fn/jkrLGEXhfheW1Spi2NyQZddvnqtFmJPaPvVY0pPAkY5vrDDWxkCs2X+/Vt2wdVfctNg6jWDBLuElmk/nmzUwt18+dwKacPEv1K9BMqQ56AfdoRTvRps2F8OYdMJaUrBavjIc8f5Bc3cosGH+o/Ux4sFdoLX0EWWyyymb02SXFEebXZ3Mw0q343XJCzS0D/gb9xO92Zu+RtGAej/NFzvFT7T06dTAimOTM4lfGqYkVl0CjoBFi+8OpmtNWKrcbiNoLY8LOnmzI+cEPpnEVZCxSVeVg3GOKr43Qbf3bKrhFz5vPMONMeJIJkm9yoDq5OOd0Khkhz5Bg5Yope4bdNXMZvzS+r4OS6lxVuSxz+/F7RopW4ZwEzyFy+NOy4BCMT+HrGQbtxKJuJNAx4nqW5FBQkBS/FNdk1eY4IT8aTpWXfvCrWu0Qp1NBPNp2TgpSOuwPCBENGcpjUA+gR5jPKGWGAXsYqglRB0KwQaglWtqUZlw0BPIwiUWrzoYruxJ3B6Lo1KGFO6aJ0av1RXF8/HGJn8rSQUbNevvWUsXys/BykX6uCKbRELGcIJKkAe8xjYzD3CSa26eg1vYV/uHIX2jlcP24gaZhGdQowi4oEBOAn0n+t61yVm9Zoo9TgeHyNH/SJOppfkWKB6Z2TZ5D2fE=")
										.build()
						).build()
				).build());

		Logger.info(playerHead.getTag(Tag.NBT("profile")).toString());

		BlockDisplayEntity displayEntity = new BlockDisplayEntity(playerHead, _ -> {});
		displayEntity.setInstance(instance, position);
		InteractionEntity entity = new InteractionEntity(0.5f, 0.5f, (player, interactEvent) -> {
			SkyBlockDataHandler dataHandler = ((SkyBlockPlayer) player).getSkyblockDataHandler();
			DatapointPresentYear.YearData presents = dataHandler.get(SkyBlockDataHandler.Data.LATEST_YEAR_PRESENT_PICKUP, DatapointPresentYear.class).getValue();
			int year = presents.year();
			int currentYear = SkyBlockCalendar.getYear();
			if (year != currentYear) {
				dataHandler.get(SkyBlockDataHandler.Data.LATEST_YEAR_PRESENT_PICKUP,
						DatapointPresentYear.class
				).setValue(new DatapointPresentYear.YearData(currentYear, List.of(index)));
				player.sendMessage("<2><l>GIFT! </l><a>You found a <f>White Gift<a>! <7>(<f>1<7>/<a>20<7>)");
				((SkyBlockPlayer) player).addAndUpdateItem(ItemType.WHITE_GIFT);
				return;
			}
			List<Integer> value = new ArrayList<>(presents.value());

			if (value.contains(index)) {
				player.sendMessage("<c>You have already found this Gift this year!");
			} else {
				value.add(index);
				dataHandler.get(SkyBlockDataHandler.Data.LATEST_YEAR_PRESENT_PICKUP,
						DatapointPresentYear.class
				).setValue(new DatapointPresentYear.YearData(currentYear, value));
				player.sendMessage("<2><l>GIFT! </l><a>You found a <f>White Gift<a>! <7>(<f>{}<7>/<a>20<7>)", value.size());
				((SkyBlockPlayer) player).addAndUpdateItem(ItemType.WHITE_GIFT);
			}
		});
		entity.setInstance(instance, position.add(0.5, 0, 0.5));
	}

}
