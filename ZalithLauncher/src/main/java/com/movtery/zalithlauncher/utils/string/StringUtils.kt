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

        fun compareChar(s1: String, s2: String): Int {
            val regex = "\\d+".toRegex()

            val parts1 = regex.findAll(s1).map { it.value }.toList()
            val parts2 = regex.findAll(s2).map { it.value }.toList()

            val minSize = minOf(parts1.size, parts2.size)

            for (i in 0 until minSize) {
                val num1 = parts1[i].toIntOrNull()
                val num2 = parts2[i].toIntOrNull()

                if (num1 != null && num2 != null) {
                    if (num1 != num2) return num1.compareTo(num2)
                } else {
                    val strCompare = parts1[i].compareTo(parts2[i], ignoreCase = true)
                    if (strCompare != 0) return strCompare
                }
            }

            return s1.compareTo(s2, ignoreCase = true)
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

        /**
         * @return 检查字符串是否为null，如果是那么则返回""，如果不是，则返回字符串本身
         */
        fun getStringNotNull(string: String?): String = string ?: ""

        fun compareClassVersions(thisName: String, otherName: String): Int {
            val parts1 = thisName.split('.').map { it.toIntOrNull() ?: 0 }
            val parts2 = otherName.split('.').map { it.toIntOrNull() ?: 0 }
            val maxLength = maxOf(parts1.size, parts2.size)
            for (i in 0 until maxLength) {
                val p1 = parts1.getOrElse(i) { 0 }
                val p2 = parts2.getOrElse(i) { 0 }
                if (p1 != p2) return p1.compareTo(p2)
            }
            return 0
        }

        /**
         * [Modified from PojavLauncher](https://github.com/PojavLauncherTeam/PojavLauncher/blob/84aca2e/app_pojavlauncher/src/main/java/net/kdt/pojavlaunch/Tools.java#L1032-L1039)
         */
        fun String.extractUntilCharacter(whatFor: String, terminator: Char): String? {
            var whatForStart = indexOf(whatFor)
            if (whatForStart == -1) return null
            whatForStart += whatFor.length
            val terminatorIndex = indexOf(terminator, whatForStart)
            if (terminatorIndex == -1) return null
            return substring(whatForStart, terminatorIndex)
        }

        /**
         * 获取字符串指定行的内容
         */
        fun String.getLine(line: Int): String? {
            val lines = this.trimIndent().split("\n")
            return if (line in 1..lines.size) lines[line - 1] else null
        }

        fun insertJSONValueList(args: Array<String>, keyValueMap: Map<String, String>) =
            args.map { it.insertSingleJSONValue(keyValueMap) }.toTypedArray()

        fun String.insertSingleJSONValue(keyValueMap: Map<String, String>): String =
            keyValueMap.entries.fold(this) { acc, (k, v) ->
                acc.replace("\${$k}", v ?: "")
            }

        fun String.splitPreservingQuotes(delimiter: Char = ' '): List<String> {
            val result = mutableListOf<String>()
            val currentPart = StringBuilder()
            var inQuotes = false

            for ((index, c) in withIndex()) {
                when {
                    c == '"' && (index == 0 || this[index - 1] != '\\') -> {
                        // 切换引号状态（忽略转义引号）
                        inQuotes = !inQuotes
                    }
                    c == delimiter && !inQuotes -> {
                        // 如果不在引号内且遇到空格，则结束当前部分并添加到结果中
                        if (currentPart.isNotEmpty()) {
                            result.add(currentPart.toString())
                            currentPart.clear() // 清空当前部分
                        }
                    }
                    else -> {
                        // 将字符添加到当前部分
                        currentPart.append(c)
                    }
                }
            }

            // 添加最后一部分（如果有的话）
            if (currentPart.isNotEmpty()) {
                result.add(currentPart.toString())
            }

            return result
        }

        fun String.isSurrounded(prefix: String, suffix: String): Boolean = this.startsWith(prefix) && this.endsWith(suffix)
    }
}