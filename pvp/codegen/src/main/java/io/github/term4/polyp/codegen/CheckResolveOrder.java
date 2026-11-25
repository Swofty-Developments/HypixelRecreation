package io.github.term4.polyp.codegen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Compile-checks every {@code new <Record>(...)} in the annotated resolver: an argument reading {@code cfg.<knob>}
 * must sit at the record component of the same name. Catches transposed positional args (same-typed neighbours slip
 * past the compiler) when a knob is added or removed.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface CheckResolveOrder {
}
