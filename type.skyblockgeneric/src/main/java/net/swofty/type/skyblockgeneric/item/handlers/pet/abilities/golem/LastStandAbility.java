package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.golem;

import net.minestom.server.MinecraftServer;
import net.minestom.server.timer.TaskSchedule;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.commons.skyblock.statistics.ItemStatistic;
import net.swofty.commons.skyblock.statistics.ItemStatistics;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;

import java.util.List;

@PetAbilityRegistration(pet = PetHandler.GOLEM, minimumRarity = Rarity.EPIC, order = 0)
public final class LastStandAbility implements PetAbility {
    private static final double HP_THRESHOLD = 0.20;
    private static final double INCOMING_DAMAGE_REDUCTION = 0.20;
    private static final double SHIELD_PERCENT = 0.40;
    private static final double DEALT_DAMAGE_BONUS = 0.40;
    private static final long WINDOW_MILLIS = 12_000;
    private static final long COOLDOWN_MILLIS = 60_000;

    private long buffUntil;
    private long lastProc;

    @Override
    public String getName() {
        return "Last Stand";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        return List.of(
                "<7>While at less than <a>20% HP<7>, reduce incoming",
                "<7>damage by <a>20%<7>. Additionally, gain a temporary",
                "<7>shield equal to <a>40% <7>of your maximum health and",
                "<7>deal <a>40% <7>more damage. <8>(Lasts 12s, 60s cooldown)<7>"
        );
    }

    @PetEventHandler
    public void onDamaged(PetEvent.Damaged event) {
        SkyBlockPlayer player = event.player();
        if (player.getHealth() / player.getMaxHealth() >= HP_THRESHOLD) return;

        event.damage(event.damage() * (1 - INCOMING_DAMAGE_REDUCTION));

        long now = System.currentTimeMillis();
        if (now - lastProc < COOLDOWN_MILLIS) return;

        lastProc = now;
        buffUntil = now + WINDOW_MILLIS;
        player.setAdditionalHearts((float) (player.getMaxHealth() * SHIELD_PERCENT));
        MinecraftServer.getSchedulerManager().scheduleTask(
                () -> {
                    if (player.isOnline()) player.setAdditionalHearts(0);
                },
                TaskSchedule.millis(WINDOW_MILLIS), TaskSchedule.stop());
    }

    @Override
    public ItemStatistics getStatistics(SkyBlockPlayer player, Rarity rarity, int level) {
        if (System.currentTimeMillis() >= buffUntil) return ItemStatistics.empty();
        return ItemStatistics.builder()
                .withMultiplicative(ItemStatistic.DAMAGE, 1 + DEALT_DAMAGE_BONUS)
                .build();
    }
}
