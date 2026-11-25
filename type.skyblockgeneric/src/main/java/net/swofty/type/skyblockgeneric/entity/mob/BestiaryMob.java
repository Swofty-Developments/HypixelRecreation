package net.swofty.type.skyblockgeneric.entity.mob;

import net.minestom.server.entity.EntityType;
import net.swofty.type.generic.gui.inventory.item.GUIMaterial;

public abstract class BestiaryMob extends SkyBlockMob {
    public BestiaryMob(EntityType entityType) {
        super(entityType);
    }

    protected BestiaryMob(EntityType entityType, boolean initialize) {
        super(entityType, initialize);
    }

    public abstract int getMaxBestiaryTier();
    public abstract int getBestiaryBracket();
    public abstract String getMobID();
    public abstract GUIMaterial getGuiMaterial();
}
