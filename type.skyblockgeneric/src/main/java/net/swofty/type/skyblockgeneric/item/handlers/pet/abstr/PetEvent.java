package net.swofty.type.skyblockgeneric.item.handlers.pet.abstr;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minestom.server.coordinate.Point;
import net.minestom.server.entity.damage.DamageType;
import net.minestom.server.item.Material;
import net.minestom.server.registry.RegistryKey;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.fishing.catches.CatchPayload;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.handlers.ability.RegisteredAbility;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

@Getter
@Accessors(fluent = true)
public abstract sealed class PetEvent {
    private final SkyBlockPlayer player;
    private SkyBlockItem pet;

    protected PetEvent(SkyBlockPlayer player, SkyBlockItem pet) {
        this.player = player;
        this.pet = pet;
    }

    public PetEvent pet(SkyBlockItem pet) {
        this.pet = pet;
        return this;
    }

    /**
     * when the mob is killed
     */
    @Getter
    @Accessors(fluent = true)
    public static final class KilledMob extends PetEvent {
        private final SkyBlockMob mob;

        public KilledMob(SkyBlockPlayer player, SkyBlockItem pet, SkyBlockMob mob) {
            super(player, pet);
            this.mob = mob;
        }
    }

    /**
     * when the player jumps
     */
    @Getter
    @Accessors(fluent = true)
    public static final class Jump extends PetEvent {
        public Jump(SkyBlockPlayer player, SkyBlockItem pet) {
            super(player, pet);
        }
    }

    /**
     * after the ability is cast
     */
    @Getter
    @Accessors(fluent = true)
    public static final class AbilityCast extends PetEvent {
        public AbilityCast(SkyBlockPlayer player, SkyBlockItem pet) {
            super(player, pet);
        }
    }

    /**
     * when the block is being mined
     */
    @Getter
    @Accessors(fluent = true)
    public static final class BlockMining extends PetEvent {
        public BlockMining(SkyBlockPlayer player, SkyBlockItem pet) {
            super(player, pet);
        }
    }

    /**
     * before the mana is consumed
     */
    @Getter
    @Accessors(fluent = true)
    public static final class ManaCost extends PetEvent {
        private final RegisteredAbility ability;
        @Setter
        private double cost;
        @Setter
        private boolean free;

        public ManaCost(SkyBlockPlayer player, SkyBlockItem pet, RegisteredAbility ability, double cost) {
            super(player, pet);
            this.ability = ability;
            this.cost = cost;
        }
    }

    /**
     * when the player regenerates mana
     */
    @Getter
    @Accessors(fluent = true)
    public static final class ManaRegen extends PetEvent {
        @Setter
        private double amount;

        public ManaRegen(SkyBlockPlayer player, SkyBlockItem pet, double amount) {
            super(player, pet);
            this.amount = amount;
        }
    }

    /**
     * when the ability is on cooldown
     */
    @Getter
    @Accessors(fluent = true)
    public static final class AbilityCooldown extends PetEvent {
        private final SkyBlockItem item;
        @Setter
        private double cooldown;  // millis, modified by handlers

        public AbilityCooldown(SkyBlockPlayer player, SkyBlockItem pet, SkyBlockItem item, double cooldown) {
            super(player, pet);
            this.item = item;
            this.cooldown = cooldown;
        }
    }

    /**
     * when the player is taking damage (any source: mob, fall)
     */
    @Getter
    @Accessors(fluent = true)
    public static non-sealed abstract class Damaged extends PetEvent {
        @Nullable
        private final RegistryKey<@NotNull DamageType> type;
        @Setter
        private double damage;

        public Damaged(SkyBlockPlayer player, SkyBlockItem pet, @Nullable RegistryKey<@NotNull DamageType> type, double damage) {
            super(player, pet);
            this.type = type;
            this.damage = damage;
        }
    }

    @Getter
    @Accessors(fluent = true)
    public static final class DamagedByMob extends Damaged {
        private final SkyBlockMob mob;

        public DamagedByMob(SkyBlockPlayer player, SkyBlockItem pet, SkyBlockMob mob, double damage) {
            super(player, pet, DamageType.MOB_ATTACK, damage);
            this.mob = mob;
        }
    }

    /**
     * when the player is taking falling damage
     */
    @Getter
    @Accessors(fluent = true)
    public static final class FallDamage extends Damaged {
        private final int fallHeight;

        public FallDamage(SkyBlockPlayer player, SkyBlockItem pet, double damage, int fallHeight) {
            super(player, pet, DamageType.FALL, damage);
            this.fallHeight = fallHeight;
        }
    }

    /**
     * when the player deals damage (any source: melee, ranged, magic)
     */
    @Getter
    @Accessors(fluent = true)
    public static non-sealed abstract class DamageDealt extends PetEvent {
        private final SkyBlockMob mob;
        @Nullable
        private final SkyBlockItem weapon;
        private final double damage;

        public DamageDealt(SkyBlockPlayer player, SkyBlockItem pet, SkyBlockMob mob,
                           @Nullable SkyBlockItem weapon, double damage) {
            super(player, pet);
            this.mob = mob;
            this.weapon = weapon;
            this.damage = damage;
        }
    }

    /**
     * when the player deals melee damage
     */
    @Getter
    @Accessors(fluent = true)
    public static final class MeleeDamageDealt extends DamageDealt {
        public MeleeDamageDealt(SkyBlockPlayer player, SkyBlockItem pet, SkyBlockMob mob,
                                @Nullable SkyBlockItem weapon, double damage) {
            super(player, pet, mob, weapon, damage);
        }
    }

    /**
     * when the player deals ranged (arrow) damage
     */
    @Getter
    @Accessors(fluent = true)
    public static final class RangedDamageDealt extends DamageDealt {
        public RangedDamageDealt(SkyBlockPlayer player, SkyBlockItem pet, SkyBlockMob mob,
                                 @Nullable SkyBlockItem weapon, double damage) {
            super(player, pet, mob, weapon, damage);
        }
    }

    @Getter
    @Accessors(fluent = true)
    public static final class FishCaught extends PetEvent {
        @Setter
        private CatchPayload payload;
        @Nullable
        private final String regionId;

        public FishCaught(SkyBlockPlayer player, SkyBlockItem pet, CatchPayload payload, @Nullable String regionId) {
            super(player, pet);
            this.payload = payload;
            this.regionId = regionId;
        }
    }

    @Getter
    @Accessors(fluent = true)
    public static final class CropHarvested extends PetEvent {
        private final Material material;
        @Setter
        private int crops;

        public CropHarvested(SkyBlockPlayer player, SkyBlockItem pet, Material material, int crops) {
            super(player, pet);
            this.material = material;
            this.crops = crops;
        }
    }

    /**
     * after the block is mined
     */
    @Getter
    @Accessors(fluent = true)
    public static final class BlockMined extends PetEvent {
        private final Material material;
        private final Point point;
        private final SkyBlockItem heldItem;
        @Setter
        private List<SkyBlockItem> drops;

        public BlockMined(SkyBlockPlayer player, SkyBlockItem pet, Material material, Point point,
                          SkyBlockItem heldItem, List<SkyBlockItem> drops) {
            super(player, pet);
            this.material = material;
            this.point = point;
            this.heldItem = heldItem;
            this.drops = drops;
        }
    }
}
