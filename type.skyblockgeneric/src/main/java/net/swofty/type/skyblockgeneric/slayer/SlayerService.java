package net.swofty.type.skyblockgeneric.slayer;

import java.util.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.swofty.commons.text.Text;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.data.datapoints.DatapointSlayer;
import net.swofty.type.skyblockgeneric.elections.ElectionManager;
import net.swofty.type.skyblockgeneric.elections.SkyBlockMayor;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.slayer.SlayerBossMob;
import net.swofty.type.skyblockgeneric.entity.mob.mobs.slayer.SlayerBossProfile;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SlayerService {
    public static StartResult startQuest(SkyBlockPlayer player, SlayerType type, SlayerTier tier) {
        DatapointSlayer.SlayerData data = data(player);
        if (data.getActiveQuest() != null) {
            return StartResult.alreadyActive(data.getActiveQuest());
        }

        SlayerDefinition definition = SlayerRegistry.get(type);
        if (definition == null || !definition.enabled()) {
            return StartResult.unavailable(type);
        }

        SlayerTierDefinition tierDefinition = definition.tiers().get(tier);
        if (tierDefinition == null) {
            return StartResult.missingTier(type, tier);
        }

        if (definition.unlockRequirement().isPresent()) {
            SlayerUnlockRequirement requirement = definition.unlockRequirement().get();
            if (data.progress(requirement.type()).completions(requirement.tier()) <= 0) {
                return StartResult.locked(requirement);
            }
        }

        int cost = questCost(tierDefinition);
        if (player.getCoins() < cost) {
            return StartResult.notEnoughCoins(cost);
        }

        player.removeCoins(cost);
        SlayerQuest quest = new SlayerQuest(type, tier, System.currentTimeMillis(), 0, false, null);
        data.setActiveQuest(quest);
        save(player, data);
        player.sendMessage("<5><l>SLAYER QUEST STARTED!");
        player.sendMessage("<7>Slay {} mobs to summon <c>{}<7>.", type.categoryName(), tierDefinition.displayName(type));
        return StartResult.started(quest);
    }

    public static void cancelQuest(SkyBlockPlayer player) {
        DatapointSlayer.SlayerData data = data(player);
        if (data.getActiveQuest() == null) {
            player.sendMessage("<c>You do not have an active Slayer quest.");
            return;
        }
        data.setActiveQuest(null);
        save(player, data);
        player.sendMessage("<c>Slayer quest cancelled!");
    }

    public static void handleMobKill(SkyBlockPlayer player, SkyBlockMob mob) {
        DatapointSlayer.SlayerData data = data(player);
        SlayerQuest quest = data.getActiveQuest();
        if (quest == null || mob == null) {
            return;
        }

        if (mob instanceof SlayerBossMob bossMob) {
            completeBoss(player, data, quest, bossMob);
            return;
        }

        if (quest.bossSpawned()) {
            return;
        }

        SlayerDefinition definition = SlayerRegistry.get(quest.type());
        if (definition == null || !definition.accepts(mob.getMobTypes())) {
            return;
        }

        SlayerTierDefinition tierDefinition = definition.tiers().get(quest.tier());
        if (tierDefinition == null) {
            return;
        }

        int gainedXp = Math.max(1, (int) mob.getOtherLoot().getSkillXPAmount());
        SlayerQuest progressedQuest = quest.addCombatXp(gainedXp, tierDefinition.requiredCombatXp());
        if (progressedQuest.combatXp() >= tierDefinition.requiredCombatXp()) {
            SlayerBossMob boss = spawnBoss(player, definition, tierDefinition, mob.getPosition());
            if (boss != null) {
                progressedQuest = progressedQuest.markBossSpawned(boss.getUuid());
                player.sendMessage("<c><l>SLAYER BOSS SPAWNED!");
                player.sendMessage("<7>Kill <c>{}<7> to complete your quest.", tierDefinition.displayName(quest.type()));
            }
        } else {
            int remaining = tierDefinition.requiredCombatXp() - progressedQuest.combatXp();
            player.sendMessage("<5>Slayer Quest <7>{}<8>/<7>{} Combat XP <8>(<e>{} left<8>)",
                progressedQuest.combatXp(), tierDefinition.requiredCombatXp(), remaining);
        }

        data.setActiveQuest(progressedQuest);
        save(player, data);
    }

    public static QuestStatus status(SkyBlockPlayer player) {
        DatapointSlayer.SlayerData data = data(player);
        SlayerQuest quest = data.getActiveQuest();
        if (quest == null) {
            return QuestStatus.none();
        }

        SlayerDefinition definition = SlayerRegistry.get(quest.type());
        SlayerTierDefinition tier = definition == null ? null : definition.tiers().get(quest.tier());
        return new QuestStatus(quest, definition, tier);
    }

    private static void completeBoss(SkyBlockPlayer player, DatapointSlayer.SlayerData data, SlayerQuest quest, SlayerBossMob bossMob) {
        if (!bossMob.getOwnerUuid().equals(player.getUuid())) {
            return;
        }

        SlayerDefinition definition = SlayerRegistry.get(quest.type());
        SlayerTierDefinition tierDefinition = definition == null ? null : definition.tiers().get(quest.tier());
        if (definition == null || tierDefinition == null) {
            return;
        }

        DatapointSlayer.SlayerProgress progress = data.progress(quest.type());
        int slayerXp = slayerXpReward(tierDefinition);
        progress.setXp(progress.getXp() + slayerXp);
        progress.addCompletion(quest.tier());
        data.setActiveQuest(null);
        save(player, data);

        int level = definition.levelForXp(progress.getXp());
        player.sendMessage("<a><l>SLAYER QUEST COMPLETE!");
        player.sendMessage("<7>You gained <d>{} {} Slayer XP<7>.", slayerXp, quest.type().categoryName());
        player.sendMessage("<7>{} Slayer Level: <e>{} <8>(<d>{} XP<8>)",
            quest.type().categoryName(), level, progress.getXp());
    }

    private static int questCost(SlayerTierDefinition tierDefinition) {
        if (ElectionManager.isPerkActive(SkyBlockMayor.Perk.SLASHED_PRICING)) {
            return Math.max(1, tierDefinition.cost() / 2);
        }
        return tierDefinition.cost();
    }

    private static int slayerXpReward(SlayerTierDefinition tierDefinition) {
        if (ElectionManager.isPerkActive(SkyBlockMayor.Perk.SLAYER_XP_BUFF)) {
            return (int) Math.round(tierDefinition.slayerXp() * 1.25D);
        }
        return tierDefinition.slayerXp();
    }

    private static @Nullable SlayerBossMob spawnBoss(SkyBlockPlayer player, SlayerDefinition definition, SlayerTierDefinition tierDefinition, Pos position) {
        Instance instance = player.getInstance();
        if (instance == null) {
            return null;
        }

        SlayerBossProfile profile = new SlayerBossProfile(
            definition.type(),
            tierDefinition,
            definition.targetMobTypes(),
            definition.type().entityType()
        );
        SlayerBossMob mob = SlayerBossMob.create(player.getUuid(), profile);
        mob.setInstance(instance, position);
        broadcastSpawn(player, tierDefinition.displayName(definition.type()), instance, position);
        return mob;
    }

    private static void broadcastSpawn(SkyBlockPlayer player, String bossName, Instance instance, Pos position) {
        Text message = Text.of("<c><l>SLAYER BOSS! </l><7>{} spawned <c>{}<7>!",
            player.getUsername(), bossName);
        instance.getPlayers().stream()
            .filter(nearby -> nearby.getPosition().distance(position) <= 32)
            .forEach(nearby -> nearby.sendMessage(message));
    }

    private static DatapointSlayer.SlayerData data(SkyBlockPlayer player) {
        return player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.SLAYER, DatapointSlayer.class).getValue();
    }

    private static void save(SkyBlockPlayer player, DatapointSlayer.SlayerData data) {
        player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.SLAYER, DatapointSlayer.class).setValue(data);
    }

    public record QuestStatus(
        @Nullable SlayerQuest quest,
        @Nullable SlayerDefinition definition,
        @Nullable SlayerTierDefinition tier
    ) {
        public static QuestStatus none() {
            return new QuestStatus(null, null, null);
        }

        public boolean active() {
            return quest != null;
        }
    }

    public record StartResult(boolean success, String message, Optional<SlayerQuest> quest) {
        public static StartResult started(SlayerQuest quest) {
            return new StartResult(true, "Started", Optional.of(quest));
        }

        public static StartResult alreadyActive(SlayerQuest quest) {
            return new StartResult(false, "You already have an active Slayer quest.", Optional.of(quest));
        }

        public static StartResult unavailable(SlayerType type) {
            return new StartResult(false, type.displayName() + " is not available here.", Optional.empty());
        }

        public static StartResult missingTier(SlayerType type, SlayerTier tier) {
            return new StartResult(false, type.displayName() + " " + tier.numeral() + " is not configured.", Optional.empty());
        }

        public static StartResult notEnoughCoins(int cost) {
            return new StartResult(false, "You need " + cost + " coins to start this quest.", Optional.empty());
        }

        public static StartResult locked(SlayerUnlockRequirement requirement) {
            return new StartResult(false, "You must slay " + requirement.type().displayName() + " "
                + requirement.tier().numeral() + " first.", Optional.empty());
        }
    }
}
