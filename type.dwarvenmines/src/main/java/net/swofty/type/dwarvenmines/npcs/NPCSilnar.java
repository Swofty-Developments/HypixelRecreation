package net.swofty.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.stream.Stream;

public class NPCSilnar extends HypixelNPC {

	public NPCSilnar() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"<f>Silnar", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "jJzThnQ6wdSSb2ETJ6emtJhXx5w2oLRXi0loH8BRtlxDQ4MsFfdB77srnHhniwBx0tuHDLVowIvtz+013I4MeM17ZavqNrmkvBAAnums31MadYreoEIzcdnqI/mgChZ3GVUzUTP/FIBNmRYqkfIMsk2yN5BTOFi7hzHGDcMoWoBw20kz6ph0sDG2RWigKxFX2YLQyJuay4Ufsp4p8GR6hmCx016csMI22F9s70knFmO1ZimwxF74EjgWy3eSdRWrUbGxiRWRBLqjYKW648YjYvsaFLx1OLG3+PEmlTT3rXxGdWnQxcm53fygWuPGXUsLCpf7QV8dXwZi2iqOaqIrRKhznwBBCCEjz4GZRLdgltTFhaMtTaIExB33ejJZ7vfaWmJNa49S3awco04xCEHTQyWZRWl44mjqYBcuBfCVVJRuHIL8nUD8xX5KcTrlQavL/I2FZzQwBboXo7vESQWBsCe/SoJESTGKu9Uo8cFWzLZ8DyI+iNDXgLcsdCibEkBhwdm/lpUKuWGBO8FNZYIRJ77bIVsDvF1nxrOuP6+COlNJj03ysSjrQIzkeo++OS3Ecqjmg14Q064doaj0tbjofkzQgKfjOpqb/WFo91fn41PuCMUkpRUkzqsNx0VEF1LlagrfmwSyyKgCk3E1hyIaV6lWl5iSwlO/UHd1qXAlD/I=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "ewogICJ0aW1lc3RhbXAiIDogMTYwODMxMzQwOTk1NSwKICAicHJvZmlsZUlkIiA6ICI0MWQzYWJjMmQ3NDk0MDBjOTA5MGQ1NDM0ZDAzODMxYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNZWdha2xvb24iLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNTdiMzU1MmM1YWYyYWIyZGM0ZTFmODRkNzhjYTRjNGQ2NzZkZWMwNjgxNTcyMjVhM2MyNjc0ZTU1NzRkMjM0OCIKICAgIH0KICB9Cn0=";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(53.063, 141.5, 19.5, 45, 0);
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
		setDialogue(player, "idle");
	}

	@Override
	protected DialogueSet[] dialogues(HypixelPlayer player) {
		return Stream.of(
				DialogueSet.builder()
						.key("idle")
						.lines(
								"<f>See that giant cliff with the <d>giant crystal<f>?",
								"<b>At night<f>, my friends host a <5>secret meeting <f> in a nearby <5>secret cave<f>.",
								"<f>Drop by some time, our <5>secret club <f>is open!",
								"<f>If my friends approve of you, they'll give you a special item.",
								"<f>Come back to me then and show it to me!"
						)
						.build(),
				DialogueSet.builder()
						.key("wearing-fallen-star-helmet")
						.lines(
								"<f>Hurrah! A fellow member of the <5>Cult of the Fallen Star<f>!",
								"<f>Hey, I've crafted new tech to let us locate <5>Fallen Stars <f>easily.",
								"<f>Check it out!"
						)
						.build(),
				DialogueSet.builder()
						.key("after-claiming-fallen-star-lozenge")
						.lines(
								"<f>With the <5>Fallen Star Lozenge<f>, you can find <5>Fallen Stars <f>much easier!",
								"<f>Now go find some stars, but don't forget about the next meeting of the <5>Cult<f>!",
								"<f>I expect to see you there!"
						)
						.build()
		).toArray(DialogueSet[]::new);
	}
}