package com.movtery.zalithlauncher.utils.string

import java.io.PrintWriter
import java.io.StringWriter

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
    }
}