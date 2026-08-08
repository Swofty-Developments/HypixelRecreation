package io.github.term4.polyp.mechanics.explosion;

import net.minestom.server.coordinate.Vec;
import org.jetbrains.annotations.Nullable;

/**
 * Per-direction scaling of the radial explosion base ({@link ExplosionConfig#baseKnockback} × these factors). Each axis
 * resolves specific ({@link #plusX} …) → {@link #horizontal} (X/Z only) → {@link #base} (default 1.0 = isotropic).
 */
public record RadialScale(double base, @Nullable Double horizontal,
                          @Nullable Double up, @Nullable Double down,
                          @Nullable Double plusX, @Nullable Double minusX,
                          @Nullable Double plusZ, @Nullable Double minusZ) {

    public static final RadialScale ISOTROPIC = builder().build();

    /** {@code magnitude ×} the per-axis directional factor {@code ×} the unit component. */
    public Vec apply(double magnitude, Vec unit) {
        return new Vec(magnitude * horizontal(plusX, minusX, unit.x()) * unit.x(),
                magnitude * or(unit.y() >= 0 ? up : down) * unit.y(),
                magnitude * horizontal(plusZ, minusZ, unit.z()) * unit.z());
    }

    private double horizontal(@Nullable Double plus, @Nullable Double minus, double c) {
        Double axis = c >= 0 ? plus : minus;
        return axis != null ? axis : or(horizontal);
    }

    private double or(@Nullable Double v) { return v != null ? v : base; }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private double base = 1.0;
        private Double horizontal, up, down, plusX, minusX, plusZ, minusZ;

        public Builder base(double v) { base = v; return this; }
        public Builder horizontal(double v) { horizontal = v; return this; }
        public Builder up(double v) { up = v; return this; }
        public Builder down(double v) { down = v; return this; }
        public Builder plusX(double v) { plusX = v; return this; }
        public Builder minusX(double v) { minusX = v; return this; }
        public Builder plusZ(double v) { plusZ = v; return this; }
        public Builder minusZ(double v) { minusZ = v; return this; }

        public RadialScale build() { return new RadialScale(base, horizontal, up, down, plusX, minusX, plusZ, minusZ); }
    }
}
