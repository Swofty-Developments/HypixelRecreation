package net.swofty.type.skyblockgeneric.item.handlers.pet.abilities.guardian;

import net.minestom.server.coordinate.Pos;
import net.minestom.server.instance.Instance;
import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.generic.utility.BlockProps;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbility;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetAbilityRegistration;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEvent;
import net.swofty.type.skyblockgeneric.item.handlers.pet.abstr.PetEventHandler;
import net.swofty.type.skyblockgeneric.user.SkyBlockPlayer;
import net.swofty.type.skyblockgeneric.utility.RarityValue;

import java.util.List;

import static net.swofty.commons.StringUtility.decimalify;

@PetAbilityRegistration(pet = PetHandler.GUARDIAN, minimumRarity = Rarity.LEGENDARY)
public final class ManaPoolAbility implements PetAbility {
    private static final int MAX_RADIUS = 3;
    private static final RarityValue<Double> REGEN_PER_LEVEL =
            new RarityValue<>(0.0, 0.0, 0.0, 0.0, 0.3, 0.3, 0.0);

    @Override
    public String getName() {
        return "Mana Pool";
    }

    @Override
    public List<String> getDescription(Rarity rarity, int level) {
        String regen = decimalify(REGEN_PER_LEVEL.getForRarity(rarity) * level, 2);

        return List.of(
                "<7>Regenerate <b>" + regen + "% <7>extra mana,",
                "<7>doubled when near or in water."
        );
    }

    @PetEventHandler
    public void onManaRegen(PetEvent.ManaRegen event) {
        Rarity rarity = event.pet().getAttributeHandler().getRarity();
        int level = event.pet().getAttributeHandler().getPetData().getAsLevel(rarity);
        double percent = REGEN_PER_LEVEL.getForRarity(rarity) * level;

        double multiplier = isNearWater(event.player()) ? 2 : 1;
        event.amount(event.amount() * (1 + percent / 100 * multiplier));
    }

    private static boolean isNearWater(SkyBlockPlayer player) {
        Instance instance = player.getInstance();
        if (instance == null) return false;
        Pos pos = player.getPosition();
        for (int x = pos.blockX() - MAX_RADIUS; x <= pos.blockX() + MAX_RADIUS; x++) {
            for (int y = pos.blockY() - MAX_RADIUS; y <= pos.blockY() + MAX_RADIUS; y++) {
                for (int z = pos.blockZ() - MAX_RADIUS; z <= pos.blockZ() + MAX_RADIUS; z++) {
                    if (BlockProps.isWater(instance.getBlock(x, y, z))) return true;
                }
            }
        }
        return false;
    }
}
