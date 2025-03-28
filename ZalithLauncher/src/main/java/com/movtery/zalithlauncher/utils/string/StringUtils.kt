package com.movtery.zalithlauncher.utils.string

import android.util.Base64
import java.io.PrintWriter
import java.io.StringWriter
import java.nio.charset.StandardCharsets

class StringUtils {
    companion object {
        fun shiftString(input: String, direction: ShiftDirection, shiftCount: Int): String {
            if (input.isEmpty()) {
                return input
            }

            //确保位移个数在字符串长度范围内
            val length = input.length
            val shiftCount1 = shiftCount % length
            if (shiftCount1 == 0) {
                return input
            }

            return when (direction) {
                ShiftDirection.LEFT -> input.substring(shiftCount1) + input.substring(0, shiftCount1)
                ShiftDirection.RIGHT -> input.substring(length - shiftCount1) + input.substring(0, length - shiftCount1)
            }
        }

        fun throwableToString(throwable: Throwable): String {
            val stringWriter = StringWriter()
            PrintWriter(stringWriter).use {
                throwable.printStackTrace(it)
            }
            return stringWriter.toString()
        }

        fun Throwable.getMessageOrToString(): String {
            return message ?: throwableToString(this)
        }

        fun decodeBase64(rawValue: String): String {
            val decodedBytes = Base64.decode(rawValue, Base64.DEFAULT)
            return String(decodedBytes, StandardCharsets.UTF_8)
        }

        fun decodeUnicode(input: String): String {
            val regex = """\\u([0-9a-fA-F]{4})""".toRegex()
            var result = input
            regex.findAll(input).forEach { match ->
                val unicode = match.groupValues[1]
                val char = Character.toChars(unicode.toInt(16))[0]
                result = result.replace(match.value, char.toString())
            }
            return result
        }
    }
}