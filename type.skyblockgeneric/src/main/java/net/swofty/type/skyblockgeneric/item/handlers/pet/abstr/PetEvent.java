package net.swofty.type.skyblockgeneric.item.handlers.pet.abstr;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import net.minestom.server.item.Material;
import net.swofty.type.skyblockgeneric.entity.mob.SkyBlockMob;
import net.swofty.type.skyblockgeneric.fishing.catches.CatchPayload;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.Nullable;

public sealed interface PetEvent {
    SkyBlockPlayer player();

    record Kill(SkyBlockPlayer player, SkyBlockItem pet, SkyBlockMob mob) implements PetEvent {
    }

    record DamageMob(SkyBlockPlayer player, SkyBlockItem pet, SkyBlockMob mob) implements PetEvent {
    }

    @Getter
    @Accessors(fluent = true)
    final class DamagedByMob implements PetEvent {
        private final SkyBlockPlayer player;
        private final SkyBlockItem pet;
        private final SkyBlockMob mob;
        @Setter
        private double damage;

        public DamagedByMob(SkyBlockPlayer player, SkyBlockItem pet, SkyBlockMob mob, double damage) {
            this.player = player;
            this.pet = pet;
            this.mob = mob;
            this.damage = damage;
        }
    }

    @Getter
    @Accessors(fluent = true)
    final class FallDamage implements PetEvent {
        private final SkyBlockPlayer player;
        private final SkyBlockItem pet;
        @Setter
        private double damage;

        public FallDamage(SkyBlockPlayer player, SkyBlockItem pet, double damage) {
            this.player = player;
            this.pet = pet;
            this.damage = damage;
        }
    }

    @Getter
    @Accessors(fluent = true)
    final class XpGain implements PetEvent {
        private final SkyBlockPlayer player;
        private final SkyBlockItem pet;
        private final XpType type;
        @Nullable
        private final SkyBlockMob mob;
        @Setter
        private double amount;

        public XpGain(SkyBlockPlayer player, SkyBlockItem pet, XpType type, @Nullable SkyBlockMob mob, double amount) {
            this.player = player;
            this.pet = pet;
            this.type = type;
            this.mob = mob;
            this.amount = amount;
        }
    }

    enum XpType {
        SKILL, SLAYER, HOTM, HOTF
    }

    record Jump(SkyBlockPlayer player, SkyBlockItem pet) implements PetEvent {
    }

    record PetInteract(SkyBlockPlayer player, SkyBlockItem pet) implements PetEvent {
    }

    @Getter
    @Accessors(fluent = true)
    final class FishCaught implements PetEvent {
        private final SkyBlockPlayer player;
        private final SkyBlockItem pet;
        @Setter
        private CatchPayload payload;
        @Nullable
        private final String regionId;

        public FishCaught(SkyBlockPlayer player, SkyBlockItem pet, CatchPayload payload, @Nullable String regionId) {
            this.player = player;
            this.pet = pet;
            this.payload = payload;
            this.regionId = regionId;
        }
    }

    @Getter
    @Accessors(fluent = true)
    final class CropHarvested implements PetEvent {
        private final SkyBlockPlayer player;
        private final SkyBlockItem pet;
        private final Material material;
        @Setter
        private int crops;

        public CropHarvested(SkyBlockPlayer player, SkyBlockItem pet, Material material, int crops) {
            this.player = player;
            this.pet = pet;
            this.material = material;
            this.crops = crops;
        }
    }
}
