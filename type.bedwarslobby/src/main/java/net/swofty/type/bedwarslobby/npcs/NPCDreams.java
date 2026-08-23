package net.swofty.type.bedwarslobby.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.ServerType;
import net.swofty.commons.StringUtility;
import net.swofty.commons.UnderstandableProxyServer;
import net.swofty.commons.VersionConst;
import net.swofty.commons.bedwars.BedWarsDreamRotation;
import net.swofty.proxyapi.ProxyInformation;
import net.swofty.type.bedwarslobby.gui.GUIPlayDreamBedWars;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.generic.utility.GameCountCache;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

public class NPCDreams extends HypixelNPC {
	private static final List<UnderstandableProxyServer> cacheServers = new ArrayList<>();
	private static long lastCacheTime = 0;

	public NPCDreams() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				if (System.currentTimeMillis() - lastCacheTime > 5000) {
					ProxyInformation information = new ProxyInformation();
					List<UnderstandableProxyServer> servers = information.getAllServersInformation().join();
					cacheServers.clear();
					cacheServers.addAll(servers);
					lastCacheTime = System.currentTimeMillis();
				}

				BedWarsDreamRotation.RotationEntry current = BedWarsDreamRotation.current(LocalDate.now());

				int amountOnline = GameCountCache.getPlayerCount(
					ServerType.BEDWARS_GAME,
					current.mode().doublesType().name()
				);

				amountOnline += current.mode().foursType() != null ? GameCountCache.getPlayerCount(
					ServerType.BEDWARS_GAME,
					current.mode().foursType().name()
				) : 0;

				String commaified = StringUtility.commaify(amountOnline);
				long days = Math.max(0, ChronoUnit.DAYS.between(LocalDate.now(), current.endsAt()));
				return new String[]{
					"<7>Changes in " + days + " days!",
						"<e><l>CLICK TO PLAY",
					"<b>" + current.mode().displayName() + " <7>[v" + VersionConst.BED_WARS_VERSION + "]",
					"<e><l>" + commaified + " Players",
				};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "eKuYfkdZAn7TXoBm469fOoVR9Z7NANyxSiVa0DEekI0RSTrJp2Y3MOBb3WweqlUhQ4+ad70k2PLYwPdyDJfyfNQ6ygXREwer2xvRiuKfXw9vewM0lT4cxpaBWs4TJS9Xy9mOjp+1+VjBrbTrPBObW5adpFT9i/x+Sb5950JyMAcuOtytFgObKbzabyDGlJusm/9bzOHyKwrPHaiNlbb6FdhPQomFLgQYBuMeJGwreeRLiMA7DY6OJcbGRFoT2fupSBJpBT8bQ0mB5bPI4KhBAB891HjQjqBiQVolRjmDwNAtLaRwdUvFgKbxEixFU5B+2KowKwzV24eeL01vOqeeSSmu2PQc85zxEAlfHLIN2KtvyKPJHOr1dyOrgsx6bFLUmtJHeTGKoLUVDcAHVrovhWnSl/QK7lLFQJsOa5fxp38qk4Z+OpMFet2CuTRLr15ttTKjomhSjrdaBaNQYYxMNlhLJDfUiROuPfn/l/CSxtzBMe5VyK/IGx6k1VLyq0igoKFprlAlOh5ZjOeMVZLurT17uISEpChY+fAJpgOgGMA7+1HH4diX0VFwAy6Fx35JRqhC9qNDgao/tmUBnvpOvQpu5TJV7XRxI4J378zo1zFEKEB9f0l0gqYCrpkLKTibJVWivrgm3NXmUnh8pWzJv6a0q9eTTHVYENv5zrtG+Mo=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "ewogICJ0aW1lc3RhbXAiIDogMTYxNjg2ODEwNzY2OCwKICAicHJvZmlsZUlkIiA6ICI5MWYwNGZlOTBmMzY0M2I1OGYyMGUzMzc1Zjg2ZDM5ZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJTdG9ybVN0b3JteSIsCiAgInNpZ25hdHVyZVJlcXVpcmVkIiA6IHRydWUsCiAgInRleHR1cmVzIiA6IHsKICAgICJTS0lOIiA6IHsKICAgICAgInVybCIgOiAiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS85NmYzMWJhNmI2OGEwZmQyMjNiM2UzMTg4NTEwOWJlYmRkNDNhMGI0ZjQwNzE1YjAwZGNlMWEwNjM3NmI0MjRhIgogICAgfQogIH0KfQ==";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(-3.5, 68, 13.5, 90, 0);
			}

		});
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		event.player().openView(new GUIPlayDreamBedWars());
	}
}
