package com.shelfapp.naol.util

import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter

/**
 * Makes the date and time text stored in the Shelf database.
 *
 * Every date is UTC text in one fixed shape, for example 2026-11-19T10:30:00.123Z.
 * The shape never changes length, so sorting the text also sorts by time. That
 * matters for "latest change wins" in sync.
 */
object DateUtil {

    private val formatter: DateTimeFormatter =
        DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
            .withZone(ZoneOffset.UTC)

    // The current moment as database text.
    fun nowUtc(): String {
        return formatter.format(Instant.now())
    }
}

