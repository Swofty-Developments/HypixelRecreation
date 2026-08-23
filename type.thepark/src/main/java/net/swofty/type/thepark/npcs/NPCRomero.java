package net.swofty.type.thepark.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class NPCRomero extends HypixelNPC {

	public NPCRomero() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{" "};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "mJ2X8Afy14c9pFiAvCNGCKuZPLt3cinpBJKloxz5mwcN00BEon6o3XqEPWWX8ZqamCVSG7lJanxFD2xU7b9zgpZpubk/dnKyilFTmXLrjHEBIfWBJQZO/Ae9XyghfIJYeEF0eLLyNN3e0dddBxv4HmkiwrcHVg1n9Ped5t1Wt55W3YaTKQhkG/S+f4FqM5c1Wkmcwgz2291j9+j6Put7as18+JFHhD+bdOj7Tjqg5Mf4YN6jkjsLQtHMwsIUr0WZ35fovtkjb0s3FkJob1Zy4dR3+tv6gTne9WINO9p1M3U5oSdT1I3/3m7q1qBmaDIQgtD2NgANHCLOtKo3886Ay0ya4YkQ701MD9mHYoAQwWEpGDwCNxtqQNxbCl0ThLOY0xg//XBFSOPCxAJVrQ07IVyqzYOmHKDq1XvAqgLxTN7GsTCiPjVd6hpT9qY2d7MigX2drDFppSZzKbibtQoovK68U1oCAJiNQZJKdJxcWM1ryllYwAx6yNrXSf744rU5mAkXr/tPO0XxuouaZpQPLBGj5ZP8yzG8TRA3GOYcvhoFxhE4Toxp3OeQ4u0RKAx821LqN+LzbXlNz7Vsl4djA637cuNdJZyxAWGdKCxC+CNOd+Xb4V98wEgpRRNUh3XE3YJDXhbhcOdMIdwx9j6iZz5IyJD46k50qCYHrZyfxV8=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "eyJ0aW1lc3RhbXAiOjE1NzI4OTE3NzU3OTcsInByb2ZpbGVJZCI6IjQxZDNhYmMyZDc0OTQwMGM5MDkwZDU0MzRkMDM4MzFiIiwicHJvZmlsZU5hbWUiOiJNZWdha2xvb24iLCJzaWduYXR1cmVSZXF1aXJlZCI6dHJ1ZSwidGV4dHVyZXMiOnsiU0tJTiI6eyJ1cmwiOiJodHRwOi8vdGV4dHVyZXMubWluZWNyYWZ0Lm5ldC90ZXh0dXJlLzdlZGJkZTcxOGQ3NGJjNmU4MGMxN2JmMjdiZmZhNmQ4YjI3OTViZjIwNTY0ZmI5YjBkNTgzNGNhNTkyZTNjZTYifX19";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(-456.500, 100.000, 30.500, 65, 0);
			}

			@Override
			public boolean looking(HypixelPlayer player) {
				return true;
			}

			@Override
            public @NonNull String chatName(HypixelPlayer player) {
				return "Romero";
			}
		});
	}

	@Override
	public void onClick(NPCInteractEvent event) {
		SkyBlockPlayer player = (SkyBlockPlayer) event.getPlayer();
		if (isInDialogue(player)) return;

		setDialogue(player, "idle");
	}

	@Override
	protected DialogueSet[] dialogues(HypixelPlayer player) {
		return List.of(
				DialogueSet.builder().key("romero-bring-item").lines(
						"Bring the item I gave you to Juliette!"
				).build(),

				DialogueSet.builder().key("romero-bring-item-missing").lines(
						"Bring the item I gave you to Juliette!",
						"If you lost the item, we can fix that!",
						"Although, you will have to cover my expenses, considering the sentimental value..."
				).build(),

				// Quest Step 1 – Savanna Woodland
				DialogueSet.builder().key("r-q1-start").lines(
						"I'm... trying to find gold...",
						"but I can only find sand and stone here!",
						"I heard of a gold mine in the Savanna Woodland.",
						"I'd like to craft a gift for my beloved Juliette.",
						"Could you find some gold for me, please?"
				).build(),

				DialogueSet.builder().key("r-q1-complete").lines(
						"Thank you so much!",
						"Here, could you do me a favor and bring this gift back to Juliette?"
				).build(),

				// Quest Step 2 – Village
				DialogueSet.builder().key("r-q2-start").lines(
						"Groceries?",
						"Haha!",
						"I'm here to get a flower bouquet for Juliette.",
						"Could you help out and get some flowers?"
				).build(),

				DialogueSet.builder().key("r-q2-wrong-material").lines(
						"I was thinking of a bouquet of roses."
				).build(),

				DialogueSet.builder().key("r-q2-1-poppy").lines(
						"Just one? That's not much of a bouquet!"
				).build(),

				DialogueSet.builder().key("r-q2-2-7-poppies").lines(
						"Going to need a few more than that!"
				).build(),

				DialogueSet.builder().key("r-q2-8-11-poppies").lines(
						"I love roses! Can you get more?"
				).build(),

				DialogueSet.builder().key("r-q2-12-14-poppies").lines(
						"That's a lot of flowers! Maybe a few more..."
				).build(),

				DialogueSet.builder().key("r-q2-complete").lines(
						"Wow, they're beautiful!",
						"Please bring the bouquet to Juliette!"
				).build(),

				// Quest Step 3 – Graveyard
				DialogueSet.builder().key("r-q3-start").lines(
						"I love emeralds!",
						"There's emerald on everything I like!",
						"This one can't be dislodged!",
						"Could you bring me some emerald?"
				).build(),

				DialogueSet.builder().key("r-q3-insufficient").lines(
						"I'm going to need a full stack of those!"
				).build(),

				DialogueSet.builder().key("r-q3-enchanted-emerald").lines(
						"Very cool, but regular emeralds will do the trick."
				).build(),

				DialogueSet.builder().key("r-q3-enchanted-block").lines(
						"Although I adore this pristine piece of emerald, I don't need it yet."
				).build(),

				DialogueSet.builder().key("r-q3-block").lines(
						"I don't need a whole block!"
				).build(),

				DialogueSet.builder().key("r-q3-complete").lines(
						"Am",
						"-az-",
						"-ing!",
						"Now bring this gift to Juliette!"
				).build(),

				// Quest Step 4 – Crimson Isle
				DialogueSet.builder().key("r-q4-start").lines(
						"Hey, you again!",
						"I wanted mushrooms to concoct a stew,",
						"but these big red ones won't cut it.",
						"I need some decent mushrooms."
				).build(),

				DialogueSet.builder().key("r-q4-red-mushroom").lines(
						"I can't cook a gourmet stew with tiny mushrooms!",
						"I need bigger ones!"
				).build(),

				DialogueSet.builder().key("r-q4-brown-mushroom").lines(
						"That's not the color of mushroom I'm looking for."
				).build(),

				DialogueSet.builder().key("r-q4-red-block").lines(
						"Sweet! Just what I need!",
						"EXCEPT... Could you enchant it?",
						"I need the perfect stew for my beloved Juliette."
				).build(),

				DialogueSet.builder().key("r-q4-brown-block").lines(
						"That's a big mushroom that I don't need.",
						"It's well known that good stews aren't made from brown mushrooms."
				).build(),

				DialogueSet.builder().key("r-q4-enchanted-brown-block").lines(
						"I need red mushrooms."
				).build(),

				DialogueSet.builder().key("r-q4-complete").lines(
						"Fantastic!",
						"There's some stew for Juliette!",
						"Mmh! Tastes great!"
				).build(),

				// Quest Step 5 – Mountain
				DialogueSet.builder().key("r-q5-start").lines(
						"So cold! I wish I had coffee right now...",
						"I want to reach the moon for Juliette.",
						"Literally!",
						"I wish I could jump like a Rabbit and grab a chunk of it."
				).build(),

				DialogueSet.builder().key("r-q5-wrong-potion").lines(
						"A potion might do the trick, but not quite that one."
				).build(),

				DialogueSet.builder().key("r-q5-rabbit-not-6").lines(
						"Rabbit potion? Sounds great!",
						"Although... could you bring a more potent one?",
						"I don't want to take any chances!"
				).build(),

				DialogueSet.builder().key("r-q5-no-coffee").lines(
						"Awesome potion!",
						"Any chance you could have it taste like coffee?",
						"Come back with a caffeinated beverage, thanks!"
				).build(),

				DialogueSet.builder().key("r-q5-complete").lines(
						"Thanks!",
						"See ya later!",
						"Bring the chunk to Juliette!"
				).build(),

				// Quest Step 6 – Gold Mine
				DialogueSet.builder().key("r-q6-start").lines(
						"Hi friend!",
						"I want to make a secret gift for Juliette.",
						"I collected tons of gold...",
						"Now I just need enough heat to melt it!"
				).build(),

				DialogueSet.builder().key("r-q6-flint").lines(
						"That's not even close to hot!"
				).build(),

				DialogueSet.builder().key("r-q6-lava").lines(
						"*tastes it*",
						"This is the kind of lava I need...",
						"Except I need hotter."
				).build(),

				DialogueSet.builder().key("r-q6-magical-lava").lines(
						"This is too magical for my taste."
				).build(),

				DialogueSet.builder().key("r-q6-complete").lines(
						"Perfect!",
						"Thank you so much for helping with all my gift ideas.",
						"Can you bring this one to Juliette?"
				).build(),

				// Quest Step 7 – Wilderness
				DialogueSet.builder().key("r-q7-start").lines(
						"I'm trying to solve this Rubix Prism.",
						"Going to need someone wicked smart to solve it."
				).build(),

				DialogueSet.builder().key("r-q7-low-int").lines(
						"You're pretty smart.",
						"I bet you wish you were smarter, though!"
				).build(),

				DialogueSet.builder().key("r-q7-mid-int").lines(
						"While very astute,",
						"you don't have quite what it takes for the Prism!"
				).build(),

				DialogueSet.builder().key("r-q7-high-int").lines(
						"Can you somehow get even more intelligence?"
				).build(),

				DialogueSet.builder().key("r-q7-almost-genius").lines(
						"I can definitely see it, your brain barely fits your head.",
						"Just not quite genius enough to solve the Prism!"
				).build(),

				DialogueSet.builder().key("r-q7-threshold").lines(
						"If I was to quantify it, I'd say you need 1,291 Intelligence."
				).build(),

				DialogueSet.builder().key("r-q7-complete").lines(
						"Incredible!",
						"You solved the Prism!",
						"Please, bring it to Juliette and show her how smart I am."
				).build(),

				// Quest Step 8 – Colosseum
				DialogueSet.builder().key("r-q8-start").lines(
						"Juliette keeps talking to me about this other person.",
						"I don't know what's going on, they meet over and over!",
						"Our love is stronger than theirs!",
						"I'm waiting for this other person to show up.",
						"I'll need my favorite weapon, can you fetch it for me?"
				).build(),

				DialogueSet.builder().key("r-q8-jerry").lines(
						"Go away."
				).build(),

				DialogueSet.builder().key("r-q8-emerald").lines(
						"That's not even a weapon!",
						"I do like it though!"
				).build(),

				DialogueSet.builder().key("r-q8-weak-sword").lines(
						"That sword is weak!",
						"Besides, would you really give it to me?"
				).build(),

				DialogueSet.builder().key("r-q8-complete").lines(
						"This is exactly what I need!",
						"Now I just have to wait for the pretendent.",
						"Please bring this poem to Juliette.",
						"I hope she'll see the reason."
				).build(),

				// Quest Step 9 – Mushroom Desert
				DialogueSet.builder().key("r-q9-start").lines(
						"It's you, <player>!",
						"I need your help to win Juliette back.",
						"My plan is to assemble the greatest bouquet ever!",
						"I need a ridiculous amount of flowers."
				).build(),

				DialogueSet.builder().key("r-q9-complete").lines(
						"That's exquisite!!",
						"With this, I can get enough flowers to win Juliette back!"
				).build(),

				// Quest Step 10 – Jungle Island
				DialogueSet.builder().key("r-q10-before-suit").lines(
						"<player>! You're the best person to turn the day into something memorable!",
						"But first, you should wear something nice!"
				).build(),

				DialogueSet.builder().key("r-q10-after-suit").lines(
						"Looking slick!",
						"You really look stunning!",
						"Thank you so much for reuniting Juliette and I.",
						"Please, one last time, give this to Juliette."
				).build(),
				DialogueSet.builder().key("idle").lines(
						"I love Juliette!"
				).build()
		).toArray(DialogueSet[]::new);
	}

}
