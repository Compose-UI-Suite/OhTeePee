package com.composeuisuite.ohteepee.configuration

import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

data class OhTeePeeCellConfiguration(
    val shape: Shape,
    val cellBackground: OhTeePeeCellBackground,
    val borderColor: Color,
    val borderWidth: Dp,
    val textStyle: TextStyle,
    val placeHolderTextStyle: TextStyle,
) {

    companion object {
        val BORDER_WIDTH = 1.dp

        @Composable
        fun withDefaults(
            shape: Shape = MaterialTheme.shapes.medium,
            cellBackground: OhTeePeeCellBackground = OhTeePeeCellBackground.Solid(MaterialTheme.colors.surface),
            borderColor: Color = MaterialTheme.colors.primary,
            borderWidth: Dp = BORDER_WIDTH,
            textStyle: TextStyle = TextStyle(),
            placeHolderTextStyle: TextStyle = textStyle,
        ) = OhTeePeeCellConfiguration(
            shape = shape,
            cellBackground = cellBackground,
            borderColor = borderColor,
            borderWidth = borderWidth,
            textStyle = textStyle,
            placeHolderTextStyle = placeHolderTextStyle,
        )
    }
}
