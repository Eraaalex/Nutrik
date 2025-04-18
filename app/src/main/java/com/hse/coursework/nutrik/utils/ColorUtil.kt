package com.hse.coursework.nutrik.utils

import androidx.compose.ui.graphics.Color

class ColorUtil {
    companion object {
        fun lightenColor(color: Color, factor: Float = 0.4f): Color {
            val r = color.red + (1f - color.red) * factor
            val g = color.green + (1f - color.green) * factor
            val b = color.blue + (1f - color.blue) * factor
            return Color(r, g, b, color.alpha)
        }
    }
}