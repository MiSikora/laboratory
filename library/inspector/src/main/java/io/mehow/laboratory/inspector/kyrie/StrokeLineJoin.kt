package io.mehow.laboratory.inspector.kyrie

/** Stroke line join determines the shape that should be used at the ends of a stroked sub-path. */
internal enum class StrokeLineJoin {
    /** A miter stroke line join. */
    MITER,
    /** A round stroke line join. */
    ROUND,
    /** A bevel stroke line join. */
    BEVEL
}
