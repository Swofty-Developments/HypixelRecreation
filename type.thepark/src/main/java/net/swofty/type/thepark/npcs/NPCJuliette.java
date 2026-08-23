package net.swofty.type.thepark.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

public class NPCJuliette extends HypixelNPC {

	public NPCJuliette() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"Juliette", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "KfBsGKY8f6EkYsyjmC8Gcneb4/kmLK9Ui2imeRb43xlF1U+gatchbLsTfywMQlcD9hBxg/zU83oyfqtz1sQl9e/x2k9lU3ANl3zLfJ4sZrwmeKGnsBRdHRF2mC6skTIVYj1QF1AfAhHNdzuYKnx3A+oh559rXcPk8PAMYjo1FqOUA3nbYRzzG87w+oBVh5jP6h/p1o5b7viEab0ASfokEPFSCSCu/aq9vw7bxYYXzmCyHztDFaAHeVlwD8uK3zlrUYDiUsE1Ajw8w99Is1W8iWTWRV2iQ6YvJzsQH4LQ+eDjBoYHp5Nf8sgfaAlEf3Q3IvRCxfyt6PmFqCzBgHHvDWmw6xpF8rO00hHEB/Yda2i6zLwsyvsuXdYlQQp16Pq8EUSQAB41pK2fvr0vcdiieVkaGViQEkAYf3Ku2Wp6/iViq3HSVEPi7krPiZroaoVx05DIaZkuneRdXAtOGqJf6MGK2oLlIOgt/Pa/8BWGUTziNWvsdNkjfMZnyNDz/rN2Ky7LSeiXEr1YXBriRF0rYhArSS+VMVn2fH1rKilrwhfDW0GB+E483th1skxvqxHg4PdN1uW6FY90k9q49sPUYbOxqXIC8zO7XGyF9TMSpwzo67ZS2ux0sFXGJfe5Z4k/3YOhWaQTQ52ZfzJhJKN0G74Fj717JPeZdzATEt+sFrs=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "eyJ0aW1lc3RhbXAiOjE1NzI4NzgzNDY2MDIsInByb2ZpbGVJZCI6IjQxZDNhYmMyZDc0OTQwMGM5MDkwZDU0MzRkMDM4MzFiIiwicHJvZmlsZU5hbWUiOiJNZWdha2xvb24iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzYyMWFhYmVkM2JhOGI4OGI4ZGUzNDNiMjY3YmM0OWIyZGYwNDY4YzBiYTRmMjBmZWI5ODUyYjc0YzIyZjMwMjAifX19";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(-415.5, 130, -121.5, 14, 0);
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return true;
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
		if (isInDialogue(player)) return;

		setDialogue(player, "q1-start");
	}

	@Override
	protected DialogueSet[] dialogues(HypixelPlayer player) {
		return List.of(

				DialogueSet.builder().key("completing-step-not-holding").lines(
								"You found <e>Romero<f>?",
								"Did he have anything for me?"
						).build(),

				// Quest Step 1 – Savanna Woodland
				DialogueSet.builder().key("q1-start").lines(
						"Nice to meet you!",
						"Could you find my dear <e>Romero<f>?",
						"He's in a cave somewhere under the <a>Savanna Woodland<f>.",
						"...",
						"Oh!",
						"He wears glasses and blends well in a crowd!"
				).build(),

				DialogueSet.builder().key("q1-complete").lines(
						"You found Romero? That's weird, I just saw him!",
						"He wanted me to have this? So sweet!",
						"...",
						"A yellow rock? What's the point of that?",
						"Here, you keep it!"
				).build(),

				// Quest Step 2 – Flower House
				DialogueSet.builder().key("q2-start").lines(
						"Since you're here, can you find Romero again?",
						"He said he was getting groceries at the downtown markets."
				).build(),

				DialogueSet.builder().key("q2-complete").lines(
						"Romero? He just came over with the groceries.",
						"These flowers are for me?",
						"That's so kind of you!",
						"You know I'm with Romero, right?"
				).build(),

				// Quest Step 3 – Graveyard
				DialogueSet.builder().key("q3-start").lines(
						"Dear, sorry for the inconvenience...",
						"Can you please find Romero again?",
						"He's at some sort of emerald altar..."
				).build(),

				DialogueSet.builder().key("q3-complete").lines(
						"Oof! Why did you bring this all the way here?",
						"How is that a gift?",
						"Just keep it! I don't want this!"
				).build(),

				// Quest Step 4 – Crimson Isle
				DialogueSet.builder().key("q4-start").lines(
						"What I want is Romero!",
						"For some reason, he's now looking for...",
						"The largest, most evil patch of fungus ever!"
				).build(),

				DialogueSet.builder().key("q4-complete").lines(
						"Stew? I love stew!",
						"...",
						"!!!",
						"Yuck!!!",
						"Yuck!!!",
						"Yuck!!!",
						"Yuck!!!",
						"Yuck!!!",
						"DISGUSTING!",
						"Is that a stew from hell?",
						"Maybe Romero should give you cooking lessons!"
				).build(),

				// Quest Step 5 – Mountain
				DialogueSet.builder().key("q5-start").lines(
						"Oh! I love Romero! He has so many skills.",
						"Did you know he likes photography?",
						"Just now, he went to take pictures of the old castle.",
						"Go find him!"
				).build(),

				DialogueSet.builder().key("q5-complete").lines(
						"You really believe that this is from the Moon?",
						"How? Someone flew up there?",
						"Thanks, I guess.",
						"Actually, I heard Romero flew close to the Sun.",
						"Sounds more like a metaphor, like..."
				).build(),

				// Quest Step 6 – Gold Mine
				DialogueSet.builder().key("q6-start").lines(
						"He went to where gold is smelted.",
						"Please find him, wherever that is!"
				).build(),

				DialogueSet.builder().key("q6-complete").lines(
						"This is a wonderful gift!",
						"I will keep this one for myself.",
						"Sometimes, some things have to remain a mystery."
				).build(),

				// Quest Step 7 – Wilderness
				DialogueSet.builder().key("q7-start").lines(
						"About something not so secret, can you get in touch with Romero?",
						"He told me he's in his home."
				).build(),

				DialogueSet.builder().key("q7-complete").lines(
						"Uh? Romero solved his cube?",
						"That's unbelievable.",
						"No, really, I don't believe it at all."
				).build(),

				// Quest Step 8 – Colosseum
				DialogueSet.builder().key("q8-start").lines(
						"I don't know why Romero is trying to be so unlike himself lately.",
						"I like him just the way he is!",
						"He says he's getting equipment for a duel.",
						"Can you find Romero before he hurts himself?"
				).build(),

				DialogueSet.builder().key("q8-complete").lines(
						"This is a wonderful poem.",
						"But, does he really think there's someone else?",
						"I can't possibly imagine why he'd think that."
				).build(),

				// Quest Step 9 – Mushroom Desert
				DialogueSet.builder().key("q9-start").lines(
						"Romero went to meditate at his retreat.",
						"It's the smallest house in SkyBlock.",
						"Please find him, I'm worried!"
				).build(),

				DialogueSet.builder().key("q9-complete").lines(
						"Another gift from Romero?",
						"Thank you for delivering it.",
						"I don't know how Romero has such hard a time understanding that I LOVE HIM.",
						"I like him just the way he is!",
						"I don't need tons of gifts to know that."
				).build(),

				// Quest Step 10 – Jungle Island
				DialogueSet.builder().key("q10-before-suit").lines(
						"It's decided, this is the day!",
						"We're making it official!"
				).build(),

				DialogueSet.builder().key("q10-after-suit").lines(
						"We can't thank you enough for bringing us together!",
						"You're the best, <player>!"
				).build()

		).toArray(DialogueSet[]::new);
	}

}
