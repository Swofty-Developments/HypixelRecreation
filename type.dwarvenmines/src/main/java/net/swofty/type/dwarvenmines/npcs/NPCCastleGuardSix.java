package net.swofty.type.dwarvenmines.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.stream.Stream;

import net.swofty.type.generic.event.custom.NPCInteractEvent;

public class NPCCastleGuardSix extends HypixelNPC {

	public NPCCastleGuardSix() {
		super(new HumanConfiguration() {
			@Override
			public String[] holograms(HypixelPlayer player) {
				return new String[]{"<6>Castle Guard", "<e><l>CLICK"};
			}

			@Override
			public String signature(HypixelPlayer player) {
				return "Ic93JjFO3ZL10gtcVoaGbsaHXjsUvq6dWirUPihZYzieAHVSyCSzrjMYDmMVvuIJGnVE8MCgcGQuYxh+J9TbmqdFugU9vVZvJ6urPTKJwyLrHtdz3tzUW4FF7+HkDn2nOBEm3lu3Pcszis49TFstpp4xfBUsvIpdivyzfeHH0KoWPTK/NE5IDdy7JqP3NS5IJg4L4Rt/zWuA17F/Pfm5+s5Ol9l7SAvSWhS/ed/lB7AzdHFGBx9SVSR1oGr1Sx9YxRKyFT4KRjc5czTUhiNUmiaZ/dwwn4cVYqjXbRG3sPQuMffuIO/QKo4ohWLU1My1SThC0qC4b52xMdzYQg8aV4rmJwe+zNkbpvylmpHNgIysTK9GO4clG3sBc+McdNM9NFKyLtxHdpGwu/ZHOPnPO5uu2vbCqnB+sxQ6I12esos+V1HPiFV4YZnCK2YxYB4MCkMjoWTJRkZhShAIcE8IgcQ/pHi5kwlqyy4kH/4Vfwd0YFK0oPFo7zfVOjj8wZaVnPmKdcAkMbO9kpOOUMPVt5/jfLyfiroRgJJQQJITVStXpmQFIieze6+aigsKhoiL5Mej4ggVrwniP8gc3iiK3p4HMeObKK0CfoiVPPP+sN9uEs443EGZmVrpuphD51xA0Qow4UtD6YhIRWPYiLzT1SV55dJ4gYy7Qr0rbDUbm+k=";
			}

			@Override
			public String texture(HypixelPlayer player) {
				return "ewogICJ0aW1lc3RhbXAiIDogMTYxMDU3NzM3NDY3NCwKICAicHJvZmlsZUlkIiA6ICJiMGQ0YjI4YmMxZDc0ODg5YWYwZTg2NjFjZWU5NmFhYiIsCiAgInByb2ZpbGVOYW1lIiA6ICJNaW5lU2tpbl9vcmciLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzk0MWM5YzZhMzhiMjU0OWMyOTViMzQxOWI5MWMwM2YzMTAzNThmOTllMWViOWVkOGI5YjU5NGJlZWRlZjEwOCIKICAgIH0KICB9Cn0=";
			}

			@Override
			public Pos position(HypixelPlayer player) {
				return new Pos(196, 204, 181, 90, 0);
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
								"<f>I, Murdohr, son of Murdohr the Third, Commander of the Phoenix Forge, and Warden of the Holy Pickaxe, am guarding this wall."
						)
						.build()
		).toArray(DialogueSet[]::new);
	}
}