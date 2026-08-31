package net.swofty.type.skyblockgeneric.item.handlers.pet.abstr;

import net.swofty.commons.skyblock.item.Rarity;
import net.swofty.type.skyblockgeneric.item.handlers.pet.PetHandler;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PetAbilityRegistration {
    PetHandler pet();

    Rarity minimumRarity();

    Rarity maximumRarity() default Rarity.MYTHIC;

    int order() default 0;

    boolean implemented() default true;

    String notImplementedReason() default "";
}
