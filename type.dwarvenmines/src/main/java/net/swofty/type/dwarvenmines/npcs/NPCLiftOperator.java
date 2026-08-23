package net.swofty.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCLiftOperator extends HypixelNPC {

	public NPCLiftOperator() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"Lift Operator", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "W8e6ip++dGgqglbYiPyLxCEHUXpOMioWrf6bCJwMNPHmEFTJux+M0H4GZaDjs1+d7FcSHjg6rEbMWS1sZAL4TUh7Q9vCtEYlx0PfnkY42l1Qqo/tIpJvgY5J6RHZ1j1cvtfrgXfKU8AKpjZVDNNyAqc5iJWcgqAm1gj3SPH6SoVvfzZgx9avAKo3z440CRsIhLYwgwEtq8/sbkqi0y6cuwlCZ+reo7yy2Ohe5AwG0Sx7Tkkv0DIVC4wO2RYoP+4xw2MYi1SRWk9yv3ZKqjwhTP8ugB/xqK/R480vVrr7MLhCpLrbUzpuLiaAbfruF9/TmvBV2hXFSCrlqypo4EmLk1E3WSuJX5ls11+juht0M12MIUlmmYcsFjgnAux5tgJ+fQq3KWhrbYedEY4CG7swavIG71/ZZI/ugRCXz/KONOq7bXKn939CyIpPdfBBAo2RZhjk2QXG/bWODeTfJ6z5VlIivNzo65FCAdwJ54VQzmUcP86ID3/jrWQ5fE5fhPkGhpYtOlVn5S4xKdFtu6RfYYUX6cEPD6MRPcOzXB4ZvCzgD7QhKxIQDBc1S9XY35c6+ZDYHPokQa87iD053Yfft4PH/pZA21ovOcf7+Xa+AFu72wnsjbynkaZUqSrFz3mCdOl+TFynZe89SwgDX0t1mIPtGtJmtK5jbZpwFA4bqDE=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "eyJ0aW1lc3RhbXAiOjE1NTQ1NzE2OTkxOTAsInByb2ZpbGVJZCI6IjNmYzdmZGY5Mzk2MzRjNDE5MTE5OWJhM2Y3Y2MzZmVkIiwicHJvZmlsZU5hbWUiOiJZZWxlaGEiLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzg2NTJhMDZmN2I5OWU0ODIyYjQ5NWJhOWI2MDg5ZDRkNTE1MWU4M2JlZDk1NmE3Y2NjYjg0N2VhNDZhMjU3MjUifX19";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(-79.5, 200, -123.5, -59, 0);
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