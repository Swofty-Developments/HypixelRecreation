package net.swofty.type.skyblockgeneric.item.handlers.pet.abstr;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface PetEventHandler {
    int order() default 0;
}
