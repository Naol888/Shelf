package com.shelfapp.naol.util

import java.util.UUID

/**
 * Makes the IDs used as primary keys in the Shelf database.
 *
 * Two kinds of ID:
 * - Random: for rows that belong to one user (library items, collections, history).
 * - Derived: for shared rows (media, genres). The same input always gives the same ID,
 *   so every phone gets the same ID for the same movie or genre.
 */
object IdGenerator {

    // A brand new random ID, for example "3f2b8c1e-5a4d-4e0a-9d1b-7c6e2a9f0b11".
    fun newId(): String {
        return UUID.randomUUID().toString()
    }

    // Derived ID for a media item. Use the same two values you store in the media table.
    // Example: mediaId("tmdb_movie", "550") gives the same ID on every phone.
    // A media item with no external ID should use newId() instead.
    fun mediaId(externalSource: String, externalId: String): String {
        return derivedId("$externalSource:$externalId")
    }

    // Derived ID for a genre. "Sci-Fi", "sci-fi", and " sci-fi " all give the same ID.
    fun genreId(genreName: String): String {
        return derivedId(genreName.trim().lowercase())
    }

    // Turns any text into a UUID. The same text always gives the same UUID.
    private fun derivedId(text: String): String {
        return UUID.nameUUIDFromBytes(text.toByteArray(Charsets.UTF_8)).toString()
    }
}