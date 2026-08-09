package net.swofty.type.garden.npc;

import net.minestom.server.coordinate.Pos;
import net.swofty.type.garden.config.GardenConfigRegistry;
import net.swofty.type.garden.gui.GardenGuiSupport;
import net.swofty.type.generic.entity.npc.HypixelNPC;
import net.swofty.type.generic.entity.npc.configuration.HumanConfiguration;
import net.swofty.type.generic.user.HypixelPlayer;
import net.swofty.type.skyblockgeneric.garden.progression.GardenProgressionSupport;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public abstract class AbstractGardenNpc extends HypixelNPC {
    private final String npcId;

    protected AbstractGardenNpc(String npcId, String displayName, String texture, String signature) {
        super(new HumanConfiguration() {
            @Override
            public String[] holograms(HypixelPlayer player) {
                return new String[]{displayName, "§e§lCLICK"};
            }

            @Override
            public Pos position(HypixelPlayer player) {
                if (!(player instanceof SkyBlockPlayer skyBlockPlayer)) {
                    return GardenNpcSupport.hiddenPosition();
                }
                return GardenNpcAnchorRegistry.getNpcAnchor(skyBlockPlayer, npcId).map(GardenNpcAnchorRegistry.NpcAnchor::position).orElse(GardenNpcSupport.hiddenPosition());
            }

            @Override
            public boolean looking(HypixelPlayer player) {
                return true;
            }

            @Override
            public boolean visible(HypixelPlayer player) {
                return player instanceof SkyBlockPlayer skyBlockPlayer && GardenNpcSupport.isVisibleOnGarden(skyBlockPlayer) && GardenNpcAnchorRegistry.getNpcAnchor(skyBlockPlayer, npcId).isPresent();
            }

            @Override
            public String texture(HypixelPlayer player) {
                return texture;
            }

            @Override
            public String signature(HypixelPlayer player) {
                return signature;
            }

            @Override
            public net.minestom.server.instance.Instance instance(HypixelPlayer player) {
                return GardenNpcSupport.instanceFor(player instanceof SkyBlockPlayer skyBlockPlayer ? skyBlockPlayer : null);
            }
        });
        this.npcId = npcId;
    }

    protected boolean hasSpoken(SkyBlockPlayer player) {
        return GardenGuiSupport.personal(player).getSpokenNpcFlags().contains(GardenProgressionSupport.normalizeSpokenKey(npcId));
    }

    protected void markSpoken(SkyBlockPlayer player) {
        GardenProgressionSupport.apply(player, net.swofty.type.skyblockgeneric.garden.progression.GardenProgressionReward.spokenNpc(npcId));
    }

    protected DialogueSet[] configuredDialogues() {
        Map<String, Object> config = GardenConfigRegistry.getConfig("visitor_dialogue.yml");
        Map<String, Object> npcConfig = GardenConfigRegistry.getSection(
            GardenConfigRegistry.getSection(config, "npcs"),
            npcId
        );
        List<DialogueSet> dialogueSets = new ArrayList<>();
        for (Map<String, Object> entry : GardenConfigRegistry.getMapList(npcConfig, "dialogue_sets")) {
            List<Object> lines = GardenConfigRegistry.getList(entry, "lines");
            if (lines.isEmpty()) {
                continue;
            }
            dialogueSets.add(DialogueSet.builder()
                .key(GardenConfigRegistry.getString(entry, "key", "hello"))
                .lines(lines.stream().map(String::valueOf).toArray(String[]::new))
                .build());
        }
        return dialogueSets.toArray(DialogueSet[]::new);
    }
}
