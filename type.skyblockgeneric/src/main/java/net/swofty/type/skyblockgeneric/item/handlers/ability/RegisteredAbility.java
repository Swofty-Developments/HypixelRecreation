package net.swofty.type.skyblockgeneric.item.handlers.ability;

import lombok.Getter;
import lombok.NonNull;
import net.minestom.server.coordinate.Point;
import net.minestom.server.instance.block.BlockFace;
import net.swofty.type.generic.data.datapoints.DatapointInteger;
import net.swofty.type.skyblockgeneric.data.SkyBlockDataHandler;
import net.swofty.type.skyblockgeneric.item.SkyBlockItem;
import net.swofty.type.skyblockgeneric.item.set.ArmorSetEffectDispatcher;
import net.swofty.type.skyblockgeneric.user.SkyBlockActionBar;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

@Getter
public class RegisteredAbility {
    private final String id;
    private final String name;
    private final BiFunction<SkyBlockPlayer, SkyBlockItem, String> description;
    private final AbilityActivation activation;
    private final int cooldownTicks;
    private final AbilityCost cost;
    private final AbilityAction action;

    public RegisteredAbility(String id, String name, BiFunction<SkyBlockPlayer, SkyBlockItem, String> description,
                             AbilityActivation activation, int cooldownTicks,
                             AbilityCost cost, AbilityAction action) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.activation = activation;
        this.cooldownTicks = cooldownTicks;
        this.cost = cost;
        this.action = action;
    }

    public boolean execute(SkyBlockPlayer player, SkyBlockItem item) {
        return execute(player, item, null, null);
    }

    public boolean execute(SkyBlockPlayer player, SkyBlockItem item, Point targetedBlock, BlockFace blockFace) {
        if (!cost.canUse(player)) {
            cost.onFail(player);
            return false;
        }
        if (!action.execute(player, item, targetedBlock, blockFace)){
            return false;
        }
        cost.onUse(player, this);
        return true;
    }

    @FunctionalInterface
    public interface Description {
        void execute(SkyBlockPlayer player, SkyBlockItem item, Point targetedBlock, BlockFace blockFace);
    }

    @FunctionalInterface
    public interface AbilityAction {
        boolean execute(SkyBlockPlayer player, SkyBlockItem item, Point targetedBlock, BlockFace blockFace);
    }

    @Getter
    public enum AbilityActivation {
        RIGHT_CLICK("RIGHT CLICK"),
        LEFT_CLICK("LEFT CLICK"),
        LEFT_CLICK_BLOCK("LEFT CLICK"),
        RIGHT_CLICK_BLOCK("RIGHT CLICK"),
        SNEAK_RIGHT_CLICK("SNEAK RIGHT CLICK"),
        SNEAK_LEFT_CLICK("SNEAK LEFT CLICK"),
        NEVER("")
        ;

        private final @NonNull String display;

        AbilityActivation(@NotNull String display) {
            this.display = display;
        }
    }

    public static abstract class AbilityCost {
        public abstract boolean canUse(@NonNull SkyBlockPlayer player);
        public abstract void onUse(@NonNull SkyBlockPlayer player, @NonNull RegisteredAbility ability);
        public abstract void onFail(@NonNull SkyBlockPlayer player);
        public abstract @Nullable String getLoreDisplay();
    }

    public static class AbilityManaCost extends AbilityCost {
        private final int cost;

        public AbilityManaCost(int cost) {
            this.cost = cost;
        }

        @Override
        public boolean canUse(@NotNull SkyBlockPlayer player) {
            return player.getMana() >= effectiveCost(player);
        }

        @Override
        public void onUse(@NonNull SkyBlockPlayer player, @NonNull RegisteredAbility ability) {
            int effectiveCost = effectiveCost(player);
            SkyBlockActionBar.getFor(player).addReplacement(
                    SkyBlockActionBar.BarSection.MANA,
                    new SkyBlockActionBar.DisplayReplacement(
                            "§b-" + effectiveCost + " (§6" + ability.getName() + "§b)",
                            20,
                            2
                    )
            );
            player.setMana(player.getMana() - effectiveCost);
        }

        @Override
        public void onFail(@NonNull SkyBlockPlayer player) {
            SkyBlockActionBar.getFor(player).addReplacement(
                    SkyBlockActionBar.BarSection.MANA,
                    new SkyBlockActionBar.DisplayReplacement(
                            "§c§lNOT ENOUGH MANA",
                            20 * 2,
                            2
                    )
            );
        }

        @Override
        public String getLoreDisplay() {
            return "§8Mana Cost: §3" + cost;
        }

        private int effectiveCost(SkyBlockPlayer player) {
            return ArmorSetEffectDispatcher.modifyManaCost(player, cost);
        }
    }

    public static class AbilityManaSoulflowCost extends AbilityCost {
        private final int cost;
        private final int soulflow;

        public AbilityManaSoulflowCost(int cost, int soulflow) {
            this.cost = cost;
            this.soulflow = soulflow;
        }

        @Override
        public boolean canUse(@NotNull SkyBlockPlayer player) {
            return (player.getMana() >= effectiveCost(player)) && (player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.SOULFLOW, DatapointInteger.class).getValue() >= soulflow);
        }

        @Override
        public void onUse(@NonNull SkyBlockPlayer player, @NonNull RegisteredAbility ability) {
            int effectiveCost = effectiveCost(player);
            SkyBlockActionBar.getFor(player).addReplacement(
                    SkyBlockActionBar.BarSection.MANA,
                    new SkyBlockActionBar.DisplayReplacement(
                            "§b-" + effectiveCost + " (§6" + ability.getName() + "§b)",
                            20,
                            2
                    )
            );
            player.setMana(player.getMana() - effectiveCost);
            player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.SOULFLOW, DatapointInteger.class).setValue(
                    player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.SOULFLOW, DatapointInteger.class).getValue() - soulflow
            );
        }

        @Override
        public void onFail(@NonNull SkyBlockPlayer player) {
            if (player.getSkyblockDataHandler().get(SkyBlockDataHandler.Data.SOULFLOW, DatapointInteger.class).getValue() < soulflow) {
                SkyBlockActionBar.getFor(player).addReplacement(
                        SkyBlockActionBar.BarSection.MANA,
                        new SkyBlockActionBar.DisplayReplacement(
                                "§c§lNOT ENOUGH SOULFLOW",
                                20 * 2,
                                2
                        )
                );
                return;
            }
            SkyBlockActionBar.getFor(player).addReplacement(
                    SkyBlockActionBar.BarSection.MANA,
                    new SkyBlockActionBar.DisplayReplacement(
                            "§c§lNOT ENOUGH MANA",
                            20 * 2,
                            2
                    )
            );
        }

        @Override
        public String getLoreDisplay() {
            return "§8Soulflow Cost: §3" + soulflow + "\n§8Mana Cost: §3" + cost;
        }

        private int effectiveCost(SkyBlockPlayer player) {
            return ArmorSetEffectDispatcher.modifyManaCost(player, cost);
        }
    }

    public static class NoAbilityCost extends AbilityCost {
        @Override
        public boolean canUse(@NotNull SkyBlockPlayer player) {
            return true;
        }

        @Override
        public void onUse(@NonNull SkyBlockPlayer player, @NonNull RegisteredAbility ability) {}

        @Override
        public void onFail(@NonNull SkyBlockPlayer player) {}

        @Override
        public String getLoreDisplay() {
            return null;
        }
    }
}
