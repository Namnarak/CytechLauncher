package com.movtery.zalithlauncher.utils.string

import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeFormatterBuilder
import java.time.format.DateTimeParseException
import java.time.format.FormatStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

private val EN_US_FORMAT: DateTimeFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.MEDIUM)
    .withLocale(Locale.US)
    .withZone(ZoneId.systemDefault())

private val ISO_DATE_TIME: DateTimeFormatter = DateTimeFormatterBuilder()
    .append(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    .optionalStart().appendOffset("+HH:MM", "+00:00").optionalEnd()
    .optionalStart().appendOffset("+HHMM", "+0000").optionalEnd()
    .optionalStart().appendOffset("+HH", "Z").optionalEnd()
    .optionalStart().appendOffsetId().optionalEnd()
    .toFormatter()

/**
 * 将字符串解析为 Instant
 * @param string 日期时间字符串
 * @return 解析后的 Instant 对象
 * @throws IllegalArgumentException 当字符串无法被任何支持的格式解析时抛出
 */
fun parseInstant(string: String): Instant {
    val parsers = listOf<(String) -> Instant>(
        { ZonedDateTime.parse(it, EN_US_FORMAT).toInstant() },
        { ZonedDateTime.parse(it, ISO_DATE_TIME).toInstant() },
        { LocalDateTime.parse(it, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
            .atZone(ZoneId.systemDefault())
            .toInstant() }
    )

    return parsers.firstNotNullOfOrNull { parser ->
        try {
            parser(string)
        } catch (_: DateTimeParseException) {
            null
        }
    } ?: throw IllegalArgumentException("Invalid instant format: $string. Supported formats: EN_US localized, ISO with offset, ISO local date time")
}

/**
 * 将 Instant 序列化为字符串
 * @param instant 要转换的 Instant 对象
 * @param zone 时区，默认为系统默认时区
 * @return 格式化的日期时间字符串
 */
fun formatInstant(instant: Instant, zone: ZoneId = ZoneId.systemDefault()): String {
    return DateTimeFormatter.ISO_OFFSET_DATE_TIME.format(
        ZonedDateTime.ofInstant(instant, zone).truncatedTo(ChronoUnit.SECONDS)
    )
}

/**
 * 将 Instant 序列化为字符串（使用指定时区ID）
 * @param instant 要转换的 Instant 对象
 * @param zoneId 时区ID字符串，如 "Asia/Shanghai"
 * @return 格式化的日期时间字符串
 */
fun formatInstant(instant: Instant, zoneId: String): String {
    return formatInstant(instant, ZoneId.of(zoneId))
}

/**
 * 安全解析 Instant，解析失败时返回 null
 * @param string 日期时间字符串
 * @return 解析后的 Instant 对象，解析失败时返回 null
 */
fun parseInstantOrNull(string: String): Instant? {
    return try {
        parseInstant(string)
    } catch (_: IllegalArgumentException) {
        null
    }
}