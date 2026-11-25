package net.swofty.type.theend.dragon;

import net.swofty.type.generic.entity.DragonEntity;

public class EnderDragonEntity extends DragonEntity {
    private final EndDragonVariant variant;

    public EnderDragonEntity(EndDragonVariant variant) {
        super();
        this.variant = variant;
    }

    public EndDragonVariant getVariant() {
        return variant;
    }
}
