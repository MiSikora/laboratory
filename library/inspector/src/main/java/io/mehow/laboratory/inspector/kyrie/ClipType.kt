package io.mehow.laboratory.inspector.kyrie

/** Determines the clipping strategy of a [ClipPathNode]. */
internal enum class ClipType {
    /** Only the pixels drawn inside the bounds of the clip path will be displayed.  */
    INTERSECT,
    /** Only the pixels drawn outside the bounds of the clip path will be displayed.  */
    SUBTRACT
}
