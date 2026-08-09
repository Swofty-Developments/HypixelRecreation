package net.swofty.type.hub.npcs.villagers;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.entity.VillagerProfession;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.VillagerConfiguration;
import net.swofty.type.generic.event.custom.NPCInteractEvent;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.hub.gui.GUIShopLibrarian;
import net.swofty.type.skyblockgeneric.mission.MissionData;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.stream.Stream;

public class VillagerLibrarian extends HypixelNPC {
    public VillagerLibrarian() {
        super(new VillagerConfiguration(){
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{"&fLibrarian", "§e§lCLICK"};
            }

            @Override
            public Pos position(HypixelPlayer player) {
                return new Pos(-68.5, 70, -79.5, -45, 0);
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }

            @Override
            public VillagerProfession profession() {
                return VillagerProfession.LIBRARIAN;
            }
        });
    }

    @Override
    public void onClick(NPCInteractEvent e) {
        SkyBlockPlayer player = (SkyBlockPlayer) e.player();
        if (isInDialogue(player)) return;

        MissionData data = player.getMissionData();

        if (data.isCurrentlyActive("speak_to_librarian")) {
            setGardenDialogue(player, "quest-hello").thenRun(() -> {
                data.endMission("speak_to_librarian");
            });
            return;
        }

        player.openView(new GUIShopLibrarian());
    }

    @Override
    public DialogueSet[] dialogues(HypixelPlayer player) {
        return Stream.of(
                DialogueSet.builder()
                        .key("quest-hello").lines(new String[]{
                                "Greetings! Welcome to the §bLibrary§f!",
                                "The §bLibrary §fis your one-stop shop for all things enchanting. Enchant items, purchase §aEnchanted Books§f, and more!",
                                "You can enchant items by clicking any §aEnchanting Table§f. Enchanting costs §3experience levels §f- the more levels you spend, the better enchantments you will receive.",
                                "Use the §aEnchanting Table §fto enchant an item!"
                        }).build()
        ).toArray(DialogueSet[]::new);
    }

    private java.util.concurrent.CompletableFuture<Void> setGardenDialogue(HypixelPlayer player, String key) {
        return setDialogue(player, key).thenRun(() -> {
            if (player instanceof net.swofty.type.skyblockgeneric.user.SkyBlockPlayer skyBlockPlayer) {
                net.swofty.type.skyblockgeneric.garden.progression.GardenProgressionSupport.apply(
                    skyBlockPlayer,
                    net.swofty.type.skyblockgeneric.garden.progression.GardenProgressionReward.spokenNpc("LIBRARIAN")
                );
            }
        });
    }
}
