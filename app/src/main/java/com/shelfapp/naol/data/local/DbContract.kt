package com.shelfapp.naol.data.local

/**
 * One place for every table name, column name, and fixed value in the Shelf database.
 *
 * Why this file exists:
 * - A name is typed once here and used everywhere else, so a typo becomes a red
 *   underline in Android Studio instead of a crash while the app runs.
 * - The repositories and the sync code will reuse these same names.
 *
 * How to read it:
 * - "object" means a single shared bundle. You never create it, you just use it.
 * - "const val" means a fixed text value that never changes.
 * - Usage example: DbContract.LibraryItems.STATUS gives the text "status".
 */
object DbContract {

    // 7.1 users: the profile of each registered user (no password here)
    object Users {
        const val TABLE = "users"
        const val USER_ID = "user_id"
        const val USERNAME = "username"
        const val ROLE = "role"
        const val IS_ACTIVE = "is_active"
        const val CREATED_AT = "created_at"
        const val UPDATED_AT = "updated_at"
        const val NEEDS_SYNC = "needs_sync"
    }

    // 7.2 media: shared metadata for movies, TV shows, books, and albums
    object Media {
        const val TABLE = "media"
        const val MEDIA_ID = "media_id"
        const val MEDIA_TYPE = "media_type"
        const val EXTERNAL_SOURCE = "external_source"
        const val EXTERNAL_ID = "external_id"
        const val TITLE = "title"
        const val DESCRIPTION = "description"
        const val CREATOR = "creator"
        const val RELEASE_YEAR = "release_year"
        const val RUNTIME_MINUTES = "runtime_minutes"
        const val TOTAL_SEASONS = "total_seasons"
        const val TOTAL_EPISODES = "total_episodes"
        const val PAGE_COUNT = "page_count"
        const val TRACK_COUNT = "track_count"
        const val COVER_IMAGE_URL = "cover_image_url"
        const val COVER_LOCAL_PATH = "cover_local_path" // local only, never synced
        const val CREATED_AT = "created_at"
    }

    // 7.3 library_items: connects a user with a media item in their library
    object LibraryItems {
        const val TABLE = "library_items"
        const val LIBRARY_ITEM_ID = "library_item_id"
        const val USER_ID = "user_id"
        const val MEDIA_ID = "media_id"
        const val STATUS = "status"
        const val IS_FAVORITE = "is_favorite"
        const val RATING = "rating"
        const val REVIEW_TEXT = "review_text"
        const val PERSONAL_NOTES = "personal_notes"
        const val DATE_ADDED = "date_added"
        const val DATE_COMPLETED = "date_completed"
        const val UPDATED_AT = "updated_at"
        const val IS_DELETED = "is_deleted"
        const val NEEDS_SYNC = "needs_sync"
    }

    // 7.4 media_progress: page, season, and episode progress, one row per library item
    object MediaProgress {
        const val TABLE = "media_progress"
        const val LIBRARY_ITEM_ID = "library_item_id"
        const val CURRENT_PAGE = "current_page"
        const val TOTAL_PAGES = "total_pages"
        const val CURRENT_SEASON = "current_season"
        const val CURRENT_EPISODE = "current_episode"
        const val UPDATED_AT = "updated_at"
        const val NEEDS_SYNC = "needs_sync"
    }

    // 7.5 collections: user-created collections
    object Collections {
        const val TABLE = "collections"
        const val COLLECTION_ID = "collection_id"
        const val USER_ID = "user_id"
        const val NAME = "name"
        const val DESCRIPTION = "description"
        const val COVER_IMAGE_PATH = "cover_image_path" // local only, never synced
        const val CREATED_AT = "created_at"
        const val UPDATED_AT = "updated_at"
        const val IS_DELETED = "is_deleted"
        const val NEEDS_SYNC = "needs_sync"
    }

    // 7.6 collection_items: which library items sit in which collection
    object CollectionItems {
        const val TABLE = "collection_items"
        const val COLLECTION_ID = "collection_id"
        const val LIBRARY_ITEM_ID = "library_item_id"
        const val ADDED_AT = "added_at"
        const val UPDATED_AT = "updated_at"
        const val IS_DELETED = "is_deleted"
        const val NEEDS_SYNC = "needs_sync"
    }

    // 7.7 genres: shared list of genres
    object Genres {
        const val TABLE = "genres"
        const val GENRE_ID = "genre_id"
        const val NAME = "name"
    }

    // 7.8 media_genres: links media to genres (many to many)
    object MediaGenres {
        const val TABLE = "media_genres"
        const val MEDIA_ID = "media_id"
        const val GENRE_ID = "genre_id"
    }

    // 7.9 progress_history: append-only log of status and progress changes
    object ProgressHistory {
        const val TABLE = "progress_history"
        const val HISTORY_ID = "history_id"
        const val LIBRARY_ITEM_ID = "library_item_id"
        const val USER_ID = "user_id"
        const val OLD_STATUS = "old_status"
        const val NEW_STATUS = "new_status"
        const val CURRENT_PAGE = "current_page"
        const val CURRENT_SEASON = "current_season"
        const val CURRENT_EPISODE = "current_episode"
        const val CHANGED_AT = "changed_at"
        const val NEEDS_SYNC = "needs_sync"
    }

    // ---- Fixed values stored in the columns above ----

    // Stored in library_items.status and progress_history.old_status / new_status.
    // The screens show friendly names such as "Want to Watch" for planned.
    object Status {
        const val PLANNED = "planned"
        const val IN_PROGRESS = "in_progress"
        const val COMPLETED = "completed"
        const val ON_HOLD = "on_hold"
        const val DROPPED = "dropped"
    }

    // Stored in media.media_type
    object MediaType {
        const val MOVIE = "movie"
        const val TV = "tv"
        const val BOOK = "book"
        const val ALBUM = "album"
    }

    // Stored in users.role
    object Role {
        const val USER = "user"
        const val ADMIN = "admin"
    }
}