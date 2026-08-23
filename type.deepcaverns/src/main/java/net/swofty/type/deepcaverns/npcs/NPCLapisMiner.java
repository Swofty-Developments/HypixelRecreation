package net.swofty.type.deepcaverns.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCLapisMiner extends HypixelNPC {

	public NPCLapisMiner() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"Lapis Miner", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "SissRoKQWTHkTCNRnTMHimwaoqOA3OcENRzAEOAU+WZ+1NKJXqehyd6ntO3HyCzit9FlJSBigpCFKYEykR/yAhaFbyb9sbVv+HvO2OvttHkme+Nx9upGVOz7LeB+1tSiQtGEZgGqhOUxCppFsYWrVKUmGMSWf6TCnAv5TJHx1yjy0OgY5NpTaUOKW95egGKZEzbBUAvC7bi8n8Bfpv42yuYnAoMPQAQ2RvxRqNFNmTPsxy10y8x7v/GqxOIeC9gbBsklUE/UKEdyBE0I+fCn7MP80b94N0aAl5qIHH84/yzf1WMna4XRmywYoAf1MmH5Scl51RLeRwuCuwKobgGXTyViHtFkX+ELGrLOvw9SFRp98cvdr1ghoC7oyn3yaxXYC20Bz+OjOcq7LBn+Bq8NJk1yTZ4ePYVtNUZewV8kQpJL+zs6WvEiEh7raVF57BTaU+dkZ1BIW/BNv2xk3VZHR73lYxucI5oyT58mSUGZjUrsl6p9jirEXrc+iU9/pdtObV/CzWr53rMNNPZTLfMCrDYW+1a0YmRXPhYQ4KCOiZhH3utr1M58o8xNiw1Kf061ZCluA1LKV1CYyM0EffL2HADooDggpaq17Q6W6yScNqnROShL+sU6Aq3H+Sj2pa3KIni/OLRpBH+HodV/gZyh+MfTj73+uSZ9JCiC7dZkMkY=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "ewogICJ0aW1lc3RhbXAiIDogMTY5MzkzMDgxMjc2NCwKICAicHJvZmlsZUlkIiA6ICJmZDYwZjM2ZjU4NjE0ZjEyYjNjZDQ3YzJkODU1Mjk5YSIsCiAgInByb2ZpbGVOYW1lIiA6ICJSZWFkIiwKICAic2lnbmF0dXJlUmVxdWlyZWQiIDogdHJ1ZSwKICAidGV4dHVyZXMiIDogewogICAgIlNLSU4iIDogewogICAgICAidXJsIiA6ICJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlL2JmYWFmNWRlMGQyNWFlMjZiMjg3YTJiYTUxOTYxYjk2OGEzYWRkOGJiYzI0NzJlOTdhNzZmZGM2OGNlNDFmNzgiCiAgICB9CiAgfQp9";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(-10.5, 120, 35.5, -115, 0);
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