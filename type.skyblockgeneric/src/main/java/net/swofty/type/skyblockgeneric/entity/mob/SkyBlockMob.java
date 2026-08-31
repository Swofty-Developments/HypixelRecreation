package net.swofty.type.skyblockgeneric.entity.mob;

import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import net.minestom.server.component.DataComponents;
import net.minestom.server.coordinate.Pos;
import net.minestom.server.coordinate.Vec;
import net.minestom.server.entity.*;
import net.minestom.server.entity.ai.GoalSelector;
import net.minestom.server.entity.ai.TargetSelector;
import net.minestom.server.entity.attribute.Attribute;
import net.minestom.server.entity.damage.Damage;
import net.minestom.server.entity.metadata.display.TextDisplayMeta;
import net.minestom.server.instance.Instance;
import net.minestom.server.network.packet.server.play.EntityHeadLookPacket;
import net.minestom.server.network.packet.server.play.EntityMetaDataPacket;
import net.minestom.server.network.packet.server.play.PlayerInfoUpdatePacket;
import net.minestom.server.network.packet.server.play.SpawnEntityPacket;
import net.minestom.server.timer.Scheduler;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.skyblock.item.ItemType;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.commons.text.Text;
import net.swofty.type.generic.entity.drop.ItemDrops;
import net.swofty.type.generic.entity.ai.vanilla.VanillaNavigator;
import net.swofty.type.generic.event.HypixelEventHandler;
import net.swofty.type.generic.HypixelConst;
import net.swofty.type.generic.utility.MathUtility;
import net.swofty.type.skyblockgeneric.SkyBlockGenericLoader;
import net.swofty.type.skyblockgeneric.entity.DroppedItemEntityImpl;
import net.swofty.type.skyblockgeneric.entity.TextDisplayEntity;
import net.swofty.type.skyblockgeneric.entity.mob.impl.MobPlayerSkin;
import net.swofty.type.skyblockgeneric.entity.mob.impl.RegionPopulator;
import net.swofty.type.skyblockgeneric.event.custom.PlayerKilledSkyBlockMobEvent;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.components.ArmorComponent;
import net.swofty.type.skyblockgeneric.item.components.PetComponent;
import net.swofty.type.skyblockgeneric.loottable.LootAffector;
import net.swofty.type.skyblockgeneric.loottable.OtherLoot;
import net.swofty.type.skyblockgeneric.loottable.SkyBlockLootTable;
import net.swofty.type.skyblockgeneric.region.RegionType;
import net.swofty.type.skyblockgeneric.region.SkyBlockRegion;
import net.swofty.type.skyblockgeneric.skill.SkillCategories;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Setter
public abstract class SkyBlockMob extends EntityCreature {
    @Getter
    private static List<SkyBlockMob> mobs = new ArrayList<>();
    @Getter
    private long lastAttack = System.currentTimeMillis();
    @Getter
    private boolean hasBeenDamaged = false;
    private final Map<UUID, Double> damageContributions = new LinkedHashMap<>();

    private Text customName;
    private TextDisplayEntity nameDisplayEntity;

    // Fully self-contained vanilla navigator (own A* + own follow loop + vanilla physics). Every mob
    // movement goal paths through this instead of Minestom's Navigator.
    private final VanillaNavigator vanillaNavigator = new VanillaNavigator(this);

    public VanillaNavigator getVanillaNavigator() {
        return vanillaNavigator;
    }

    public SkyBlockMob(EntityType entityType) {
        this(entityType, true);
    }

    protected SkyBlockMob(EntityType entityType, boolean initialize) {
        super(entityType);
        if (initialize) {
            init();
        }
    }

    protected final void initializeMob() {
        init();
    }

    private void init() {
        this.setCustomNameVisible(true);

        this.getAttribute(Attribute.MAX_HEALTH)
                .setBaseValue(getBaseStatistics().getOverall(ItemStatistic.HEALTH).floatValue());
        this.getAttribute(Attribute.MOVEMENT_SPEED)
                .setBaseValue((float) ((getBaseStatistics().getOverall(ItemStatistic.SPEED).floatValue() / 1000) * 2.5));
        this.setHealth(getBaseStatistics().getOverall(ItemStatistic.HEALTH).floatValue());

        this.customName = Text.of("<8>[<7>Lv{}<8>] <c>{}<c> {} <a>{}<f>/<a>{}",
                getLevel(),
                getMobTypes().getFirst().getColoredSymbol(),
                getDisplayName(),
                Math.round(getHealth()),
                Math.round(getBaseStatistics().getOverall(ItemStatistic.HEALTH).floatValue())
        );
        this.set(DataComponents.CUSTOM_NAME, customName.asComponent());
        nameDisplayEntity = new TextDisplayEntity(customName, meta -> meta.setTranslation(new Pos(0, getNameDisplayHeightOffset(), 0)));

        setAutoViewable(true);
        setAutoViewEntities(true);
        this.addAIGroup(getGoalSelectors(), getTargetSelectors());

        onInit();
    }

    @Override
    public void updateNewViewer(@NotNull Player player) {
        super.updateNewViewer(player);
        if (this instanceof MobPlayerSkin skin) {
            List<PlayerInfoUpdatePacket.Property> properties = new ArrayList<>();
            if (skin.getSkinTexture() != null && skin.getSkinSignature() != null) {
                properties.add(new PlayerInfoUpdatePacket.Property("textures", skin.getSkinTexture(), skin.getSkinSignature()));
            }
            player.sendPackets(
                    new PlayerInfoUpdatePacket(PlayerInfoUpdatePacket.Action.ADD_PLAYER,
                            new PlayerInfoUpdatePacket.Entry(
                                    getUuid(),
                                    "SB_MOB_" + getEntityId(),
                                    properties,
                                    false,
                                    0,
                                    GameMode.SURVIVAL,
                                    customName.asComponent(), // ARI - this is only used in tab for some reason
                                    null,
                                    1, true)),
                    new SpawnEntityPacket(this.getEntityId(), this.getUuid(), EntityType.PLAYER,
                            getPosition(),
                            (float) 0,
                            0,
                            Vec.ZERO),
                    new EntityHeadLookPacket(getEntityId(), 0),
                    new EntityMetaDataPacket(getEntityId(), Map.of(
                            MetadataDef.Avatar.DISPLAYED_MODEL_PARTS_FLAGS.index(),
                            Metadata.Byte((byte) 127) // 127 is all parts
                    ))
            );
        }
    }

    @Override
    public void spawn() {
        super.spawn();
        mobs.add(this);
        addPassenger(nameDisplayEntity);
        updateCustomName(customName);
        onSpawn();
    }

    public void onInit() {
        // override this
    }

    public void onSpawn() {
        // override this
    }

    public float getNameDisplayHeightOffset() {
        return 0.2f;
    }

    public abstract String getDisplayName();
    public abstract Integer getLevel();
    public abstract List<GoalSelector> getGoalSelectors();
    public abstract List<TargetSelector> getTargetSelectors();
    public abstract ItemStatistics getBaseStatistics();
    public abstract @Nullable SkyBlockLootTable getLootTable();

    public @Nullable SkyBlockLootTable getLootShareTable() {
        return null;
    }
    public abstract SkillCategories getSkillCategory();
    public abstract long damageCooldown();
    public abstract OtherLoot getOtherLoot();
    public abstract List<MobType> getMobTypes();

    /**
     * Whether this mob should collide with other mobs.
     * Override and return false to disable collision for specific mob types.
     */
    public boolean hasCollision() {
        return true;
    }

    public ItemStatistics getStatistics() {
        ItemStatistics statistics = getBaseStatistics().clone();
        ItemStatistics toSubtract = ItemStatistics.builder()
            .withBase(ItemStatistic.HEALTH, (double) getHealth())
            .build();

        return statistics.sub(toSubtract);
    }

    @Override
    public boolean damage(@NotNull Damage damage) {
        float healthBefore = getHealth();
        boolean toReturn = super.damage(damage);

        if (toReturn && damage.getAttacker() instanceof SkyBlockPlayer player) {
            double dealt = Math.max(0, healthBefore - getHealth());
            if (dealt > 0) damageContributions.merge(player.getUuid(), dealt, Double::sum);
        }

        setHasBeenDamaged(true);

        Entity sourcePoint = damage.getSource();
        if (sourcePoint != null) {
            takeKnockback(0.4f,
                    Math.sin(sourcePoint.getPosition().yaw() * Math.PI / 180),
                    -Math.cos(sourcePoint.getPosition().yaw() * Math.PI / 180));
        }

        updateCustomName("<8>[<7>Lv{}<8>] <c>{} <a>{}<f>/<a>{}",
                getLevel(),
                getDisplayName(),
                Math.round(getHealth()),
                Math.round(this.getAttributeValue(Attribute.MAX_HEALTH))
        );

        return toReturn;
    }

    @Override
    public void kill() {
        super.kill();
        mobs.remove(this);
        nameDisplayEntity.remove();

        if (getLastDamageSource() == null
                || !(getLastDamageSource().getAttacker() instanceof SkyBlockPlayer player)) return;

        HypixelEventHandler.callCustomEvent(new PlayerKilledSkyBlockMobEvent(player, this));

        player.getSkills().increase(player, getSkillCategory(), (double) getOtherLoot().getSkillXPAmount());
        player.playSound(Sound.sound(Key.key("entity." + getEntityType().name().toLowerCase().replace("minecraft:", "") + ".death"), Sound.Source.PLAYER, 1f, 1f), Sound.Emitter.self());

        awardDrops(player, getLootTable(), false);
        awardLootShares(player);
    }

    private void awardLootShares(SkyBlockPlayer killer) {
        if (net.swofty.type.generic.HypixelConst.isIslandServer()) return;
        String serverType = net.swofty.type.generic.HypixelConst.getTypeLoader().getType().name();
        if (serverType.contains("DUNGEON")) return;

        double requiredDamage = getAttributeValue(Attribute.MAX_HEALTH)
                * (this instanceof net.swofty.type.skyblockgeneric.entity.mob.mobs.slayer.SlayerBossMob ? 0.10 : 0.01);
        damageContributions.entrySet().stream()
                .filter(entry -> !entry.getKey().equals(killer.getUuid()))
                .filter(entry -> entry.getValue() >= requiredDamage)
                .map(entry -> getInstance() == null ? null : getInstance().getPlayerByUuid(entry.getKey()))
                .filter(SkyBlockPlayer.class::isInstance)
                .map(SkyBlockPlayer.class::cast)
                .filter(assistant -> assistant.getPosition().distance(getPosition()) <= 30)
                .sorted(Comparator.comparingDouble((SkyBlockPlayer assistant) ->
                        damageContributions.getOrDefault(assistant.getUuid(), 0D)).reversed())
                .limit(5)
                .forEach(assistant -> {
                    SkyBlockLootTable table = getLootShareTable() == null ? getLootTable() : getLootShareTable();
                    awardDrops(assistant, table, true);
                    assistant.sendMessage("<6><l>LOOT SHARE <e>You received loot for assisting "
                            + killer.getUsername() + "!");
                });
    }

    private void awardDrops(SkyBlockPlayer player, @Nullable SkyBlockLootTable table, boolean shared) {
        if (table == null) return;
        List<SkyBlockLootTable.LootRecord> drops = table.roll(player, this, record -> {
            SkyBlockItem item = new SkyBlockItem(record.getItemType());
            List<LootAffector> affectors = new ArrayList<>();
            affectors.add(LootAffector.MAGIC_FIND);
            if (item.hasComponent(PetComponent.class)) {
                affectors.add(LootAffector.PET_LUCK);
            } else if (!shared && item.hasComponent(ArmorComponent.class)) {
                affectors.add(LootAffector.ENCHANTMENT_LUCK);
            } else if (!shared) {
                affectors.add(LootAffector.ENCHANTMENT_LOOTING);
            }
            return affectors;
        });

        for (SkyBlockLootTable.LootRecord record : drops) {
            if (SkyBlockLootTable.LootRecord.isNone(record)) continue;
            SkyBlockItem item = new SkyBlockItem(record.getItemType(), record.getAmount());
            ItemType droppedItemLinker = item.getAttributeHandler().getPotentialType();

            if (player.canInsertItemIntoSacks(droppedItemLinker, record.getAmount())) {
                player.getSackItems().increase(droppedItemLinker, record.getAmount());
            } else if (player.getSkyBlockExperience().getLevel().asInt() >= 6) {
                player.addAndUpdateItem(item);
            } else {
                ItemDrops.dropAt(new DroppedItemEntityImpl(item, player),
                        getInstance(), getPosition().add(0, 0.5, 0));
            }
            record.getRarity().announcement().announce(player, record.getItemType().getDisplayName());
        }
    }

    public void updateCustomName(Text newName) {
        this.customName = newName;
        this.set(DataComponents.CUSTOM_NAME, customName.asComponent());
        if (nameDisplayEntity != null) {
            nameDisplayEntity.editEntityMeta(TextDisplayMeta.class, meta -> {
                meta.setTranslation(new Pos(0, getNameDisplayHeightOffset(), 0));
                meta.setText(newName.asComponent());
            });
        }
    }

    public void updateCustomName(String markup, Object... arguments) {
        updateCustomName(Text.of(markup, arguments));
    }

    public static void runRegionPopulators(Scheduler scheduler) {
        scheduler.submitTask(() -> {
            if (SkyBlockGenericLoader.getLoadedPlayers().isEmpty()) return TaskSchedule.seconds(10);

            MobRegistry.getMobsToRegionPopulate().forEach(mobRegistry -> {
                RegionPopulator regionPopulator = (RegionPopulator) mobRegistry.getMobCache();
                if (!regionPopulator.canPopulate(HypixelConst.getInstanceContainer())) return;

                regionPopulator.getPopulators().forEach(populator -> {
                    RegionType regionType = populator.regionType();
                    int minimumAmountToPopulate = populator.minimumAmountToPopulate();

                    int amountInRegion = 0;

                    for (SkyBlockMob mob : SkyBlockRegion.getMobsInRegion(regionType)) {
                        if (!mobRegistry.equals(MobRegistry.getFromMob(mob))) {
                            continue;
                        }

                        amountInRegion++;
                    }

                    if (amountInRegion < minimumAmountToPopulate) {
                        for (int i = 0; i < minimumAmountToPopulate - amountInRegion; i++)
                            RegionPopulator.populateRegion(mobRegistry, populator);
                    }
                });
            });

            return TaskSchedule.seconds(5);
        });
    }

    @Override
    public void tick(long time) {
        Instance instance = getInstance();
        Pos position = getPosition();

        if (instance == null) {
            return;
        }

        if (!instance.isChunkLoaded(position)) {
            instance.loadChunk(position).join();
        }

        // Entity collision handling
        if (hasCollision()) {
            applyEntityCollision();
        }

        // Drive our own vanilla navigator's path-follow loop.
        this.vanillaNavigator.tick();

        try {
            super.tick(time);
        } catch (Exception e) {
            // Suppress odd warnings
        }
    }

    private void applyEntityCollision() {
        for (SkyBlockMob other : mobs) {
            if (other == this || !other.hasCollision()) continue;
            if (other.getInstance() != getInstance()) continue;

            double dx = getPosition().x() - other.getPosition().x();
            double dz = getPosition().z() - other.getPosition().z();
            double distanceSquared = dx * dx + dz * dz;

            // Check if within collision range - using larger hitbox multiplier
            double combinedWidth = (getBoundingBox().width() + other.getBoundingBox().width()) / 2;
            double minDistance = combinedWidth * 1.5;

            if (distanceSquared < minDistance * minDistance && distanceSquared > 0.0001) {
                double distance = Math.sqrt(distanceSquared);
                double overlap = minDistance - distance;

                // Normalize direction
                double nx = dx / distance;
                double nz = dz / distance;

                // Aggressive push to prevent overlap while in motion
                double baseStrength = overlap * 1.2;

                // Extra push if either entity is moving (prevents clipping during movement)
                double thisSpeed = getVelocity().length();
                double otherSpeed = other.getVelocity().length();
                double motionMultiplier = 1.0 + Math.max(thisSpeed, otherSpeed) * 2.0;

                double pushStrength = Math.min(baseStrength * motionMultiplier, 0.8);

                // Apply velocity to both entities (equal and opposite)
                setVelocity(getVelocity().add(nx * pushStrength, 0, nz * pushStrength));
                other.setVelocity(other.getVelocity().add(-nx * pushStrength, 0, -nz * pushStrength));
            }
        }
    }

    public static @NonNull List<SkyBlockMob> getMobFromFuzzyPosition(Pos position) {
        List<SkyBlockMob> mobs = new ArrayList<>();
        for (SkyBlockMob mob : getMobs()) {
            if (MathUtility.isWithinSameBlock(mob.getPosition(), position)) {
                mobs.add(mob);
            }
        }
        return mobs;
    }
}
