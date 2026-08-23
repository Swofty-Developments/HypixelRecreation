package net.swofty.type.spidersden.npcs;

import net.minestom.server.coordinate.Pos;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.type.generic.data.datapoints.DatapointToggles;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

public class NPCBramassBeastslayer extends HypixelNPC {

    public NPCBramassBeastslayer() {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{
                        "<3>Bestiary",
                        "<c>Bramass Beastslayer",
                        "<e><l>CLICK"};
            }

            @Override
            public String signature(HypixelPlayer player) {
                return "rXqHJm9SDJV3cxXim1J6MDT0WUfVa7nacz70F8ARcxUg91unuKr7CO/gdwngzQNy6Qa45GfG0VZmCkNB/DXBcMv3fAOOXzvbtN1HyBWGLZ5STXL/g8JaIideD0NNRB2HHJriokpnROInuy+FYdFKtclVXcRpbV0dhtbD6k4VXnsVg1fUbrfojwfazTKAUTKT6E9rfJLPFTzS6y8yoWZvOHCiuMPO7YVblhH/T3c+4HaijHXTkYKo8k97Y/BKuTs54adEmInItbfldwqUDqc+yQ/AdSW4Qr319M8FL4SnSC42CuJiFvnZ/lhSvx2tlm/PU8E36iSi0GYc7qfJuI/xoz0aT4ZKG9GYvBrCAG34LeYVcgqZGS77IBk9637ghLRr7t+9wn8JRWrvYByWdCjFOi2Ia3yXx3be6zGjBIAwWwSXnneKEgDXIt7lEJ2bA1HT04EO/ja5JIDoFVJbK4LAnZZwRczDJt4UeS3iP9/tRcMRIm4tLIOGP0Ygnh86Jb9o1nZ72kodwC5z3aI1BGYbkSaH88eIhrxhbhU7PexqATzgFoNYhSEpuBoyO9kbQ7sBAFnH0d2TORro7bnlC6B3r84vOMJ69P9/KfjNkKcSq2epB4I//kD4+BQC5HeMmPszckwqfFGUbTHC7MZbpJMO+OWwhP5C10gK/gy4F3+hSJM=";
            }

            @Override
            public String texture(HypixelPlayer player) {
                return "ewogICJ0aW1lc3RhbXAiIDogMTYxMzQ4Nzk3NTIzMywKICAicHJvZmlsZUlkIiA6ICJhMjk1ODZmYmU1ZDk0Nzk2OWZjOGQ4ZGE0NzlhNDNlZSIsCiAgInByb2ZpbGVOYW1lIiA6ICJWaWVydGVsdG9hc3RpaWUiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYWFlZjUxNzk5OGI5ZDkzZjYyZDAwNTY3Y2Y2YmM2ODg1MWEzNGJkOTlmMzcwNjk4ZjhiNmQ2MmQ5ZGU0YzNlNCIKICAgIH0KICB9Cn0=";
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-271.5, 113, -196.5, -87, 0);
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
        if (!player.getToggles().get(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_BRAMASS_BEASTSLAYER)) {
            setDialogue(player, "initial-hello").thenRun(() -> {
                player.getToggles().set(DatapointToggles.Toggles.ToggleType.HAS_SPOKEN_TO_BRAMASS_BEASTSLAYER, true);
            });
        }

        setDialogue(player, "idle-" + (1 + (int)(Math.random() * 7)));
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return new DialogueSet[] {
                DialogueSet.builder()
                        .key("initial-hello").lines(
                                "Hello, adventurer! I am <c>Bramass Beastslayer<f>! I've slain beasts of all sorts across SkyBlock!",
                                "I sure wish there was a record of all of my accomplishments in one place!",
                                "Oh wait...there is!",
                                "Your <3>Bestiary<f> is a compendium of all of the mobs in SkyBlock!",
                                "View your mob stats, unlock rewards, and more!",
                                "You can find the Bestiary in your <a>Combat Skill<f> menu!"
                        ).build(),
                DialogueSet.builder()
                        .key("idle-1").line("Killing mobs in a Family enough times rewards you with {}<f> towards that mob!",
                                        ItemStatistic.MAGIC_FIND.getCompleteDisplayName()).build(),
                DialogueSet.builder()
                        .key("idle-2").lines(
                                "This increases your chance to find rare loot when killing this mob!"
                        ).build(),
                DialogueSet.builder()
                        .key("idle-3").lines(
                                "Killing enough mobs in a given <a>Family<f> unlocks <a>rewards<f>."
                        ).build(),
                DialogueSet.builder()
                        .key("idle-4").line("You can unlock {}<f>, {}<f> bonuses, and loot drop information for that Family!",
                                        ItemStatistic.MAGIC_FIND.getCompleteDisplayName(),
                                        ItemStatistic.STRENGTH.getCompleteDisplayName()).build(),
                DialogueSet.builder()
                        .key("idle-5").lines(
                                "You can always view your Bestiary in your <a>Combat Skill<f> menu!"
                        ).build(),
                DialogueSet.builder()
                        .key("idle-6").lines(
                                "Reach <a>Milestones<f> in your Bestiary by unlocking unique Family tiers."
                        ).build(),
                DialogueSet.builder()
                        .key("idle-7").line("Reaching Milestones rewards {}<f>, <3>Combat Exp<f>, and more!",
                                        ItemStatistic.HEALTH.getCompleteDisplayName()).build(),
        };
    }
}
