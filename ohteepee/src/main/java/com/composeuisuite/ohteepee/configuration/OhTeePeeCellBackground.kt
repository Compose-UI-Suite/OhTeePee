package com.composeuisuite.ohteepee.configuration

import androidx.annotation.FloatRange
import androidx.compose.foundation.background
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

sealed class OhTeePeeCellBackground {

    data class Gradient(
        val brush: Brush,
        @FloatRange(from = 0.0, to = 1.0)
        val alpha: Float = 1f,
    ) : OhTeePeeCellBackground()

    data class Solid(
        val color: Color,
    ) : OhTeePeeCellBackground()
}

internal fun Modifier.cellBackground(
    background: OhTeePeeCellBackground,
): Modifier {
    val backgroundModifier = when (background) {
        is OhTeePeeCellBackground.Gradient -> {
            this.background(brush = background.brush, alpha = background.alpha)
        }

        is OhTeePeeCellBackground.Solid -> {
            this.background(color = background.color)
        }
    }

    return backgroundModifier.then(this)
}
