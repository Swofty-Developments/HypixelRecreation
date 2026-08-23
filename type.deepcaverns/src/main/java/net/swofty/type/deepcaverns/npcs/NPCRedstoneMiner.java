package net.swofty.type.deepcaverns.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCRedstoneMiner extends HypixelNPC {

	public NPCRedstoneMiner() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"Redstone Miner", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "wJTZ9MCUjX/mtBuzZcey1VVlywKqV91n8hWUGyrR/37p+gCntLgGzjssYYWqKRWgVosrIPjtye5E23fYMv3lW+SEPdu3o14kjYl+ZPzXLJ/A3Kt8aJO+8TJthGzXGm2fKb3PTmvQVIvAxaoOzDwP9Q8JjXgZcUCoLtY1IGlcAlr4xWqUQNV3UsbNeWG/1W9otKgqrvX9oKBn+ScSQMPItIasG6VKRj/22S/kSlzgkS7ZdewpM6Yzk/sT2srvkKQuM561ZCX1AMq7xhr+OdJRg3zEX3G6PTVDBWk8pWPvTYvCVpkreEfh47WiFRGweWmB4lvTWITDj9dyOHwG1M08bR4w1oVOUkN+RzA7X1/4vSlwOIJIf0s/1JH+hmM1bIvGWl46ZqxitKUyq6/NfSFtWVnlXNshz1+5/eZ8DYGfKUtKiJQvxTU86mZ0kdeUiys97WB8Qs/x/nVoxgz61+LAAp9lQ3EetVgnfyUvJc8aegTnIuJwt18SoPB3w9RTZU40BE7EcXjUE/tcQ7aD/f7MTGhqNokfNmsGatLxOpi8Jg7Z9dKkXpYUIqRP2wm/5ahHfMfQYAamDVTEblpgfJcaEwMUlPlwOjHcwrlTi5a1DjA6s9V9d3SalYEtn+a+YrP51Ib4NhGMXVyE4jOQstMdzaG4JKpJYiNHmwNwU/4QY58=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "ewogICJ0aW1lc3RhbXAiIDogMTY5MzkzMDkxNTIyMiwKICAicHJvZmlsZUlkIiA6ICJiMGQ0YjI4YmMxZDc0ODg5YWYwZTg2NjFjZWU5NmFhYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNaW5lU2tpbl9vcmciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvOTUwM2MwMDg5MDMyNjYwNTA2N2ZmMTZmZmZiYzJkMDUwMjI1MWIzNjgwYzFhY2MxZDY4Y2QzZDA2NGQwNTc3IgogICAgfQogIH0KfQ==";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(25, 104, 16.5, -66, 0);
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
        player.notImplemented();
	}
}