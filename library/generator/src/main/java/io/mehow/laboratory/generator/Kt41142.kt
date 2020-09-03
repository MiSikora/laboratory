package io.mehow.laboratory.generator

import kotlin.annotation.AnnotationRetention.SOURCE
import kotlin.annotation.AnnotationTarget.EXPRESSION

// TODO: https://youtrack.jetbrains.com/issue/KT-41142, use method references
@Retention(SOURCE)
@Target(EXPRESSION)
@Suppress("ClassNaming")
internal annotation class Kt41142
