package com.movtery.layer_controller.utils

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter

class ColorTypeAdapter : TypeAdapter<Color>() {
    override fun write(out: JsonWriter, value: Color) {
        out.value(value.toArgb())
    }

    override fun read(reader: JsonReader): Color {
        val colorValue = reader.nextLong()
        return Color(colorValue)
    }
}
