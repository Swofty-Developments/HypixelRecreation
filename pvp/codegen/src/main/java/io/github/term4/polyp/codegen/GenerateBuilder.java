package io.github.term4.polyp.codegen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Generates {@code <Config>BuilderBase} from the annotated config's {@code FieldValue} fields (the fields stay the
 * source of truth): package-private knob fields, the three public setter overloads + the package-private
 * {@code FieldValue} passthrough per knob, {@code copyKnobs}/{@code mergeKnobs}, and a protected copy
 * constructor. The handwritten {@code Builder extends <Config>BuilderBase<Builder>} adds {@code self()} plus any
 * non-knob members; a {@code Builder(Config c)} copy ctor MUST call {@code super(c)} (compile-enforced -
 * forgetting silently resets every knob to defaults). Adding a knob = declare the field, rebuild.
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface GenerateBuilder {
}
