package com.shelfapp.naol.data.local

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.shelfapp.naol.data.local.DbContract.CollectionItems
import com.shelfapp.naol.data.local.DbContract.Collections
import com.shelfapp.naol.data.local.DbContract.Genres
import com.shelfapp.naol.data.local.DbContract.LibraryItems
import com.shelfapp.naol.data.local.DbContract.Media
import com.shelfapp.naol.data.local.DbContract.MediaGenres
import com.shelfapp.naol.data.local.DbContract.MediaProgress
import com.shelfapp.naol.data.local.DbContract.MediaType
import com.shelfapp.naol.data.local.DbContract.ProgressHistory
import com.shelfapp.naol.data.local.DbContract.Role
import com.shelfapp.naol.data.local.DbContract.Status
import com.shelfapp.naol.data.local.DbContract.Users

/**
 * Opens and creates the local Shelf database (SQLite).
 *
 * Creates all nine tables (users, media, library_items, media_progress,
 * collections, collection_items, genres, media_genres, progress_history)
 * and the indexes.
 * The rest of the app never creates this class directly. It asks for the one
 * shared copy with DatabaseHelper.getInstance(context).
 */
class DatabaseHelper private constructor(context: Context) :
    SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    // Runs every time the database is opened, before anything else.
    // SQLite ignores foreign keys unless we turn them on each time.
    override fun onConfigure(db: SQLiteDatabase) {
        super.onConfigure(db)
        db.setForeignKeyConstraintsEnabled(true)
    }

    // Runs once, the first time the database file is created on the phone.
    // Android already wraps this in one transaction, so if any table fails,
    // nothing is kept. Parent tables come first because child tables point to them.
    override fun onCreate(db: SQLiteDatabase) {
        createUsersTable(db)
        createMediaTable(db)
        createLibraryItemsTable(db)
        createMediaProgressTable(db)
        createCollectionsTable(db)
        createCollectionItemsTable(db)
        createGenresTable(db)
        createMediaGenresTable(db)
        createProgressHistoryTable(db)
        createIndexes(db)
    }

    // Runs only if DATABASE_VERSION goes up. The schema will not change during
    // development, so this stays empty. If a table ever changes, uninstall the
    // app from the phone and run it again.
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
    }

    // 7.1 users
    private fun createUsersTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE ${Users.TABLE} (
                ${Users.USER_ID} TEXT PRIMARY KEY,
                ${Users.USERNAME} TEXT NOT NULL UNIQUE COLLATE NOCASE,
                ${Users.ROLE} TEXT NOT NULL DEFAULT '${Role.USER}'
                    CHECK (${Users.ROLE} IN ('${Role.USER}', '${Role.ADMIN}')),
                ${Users.IS_ACTIVE} INTEGER NOT NULL DEFAULT 1
                    CHECK (${Users.IS_ACTIVE} IN (0, 1)),
                ${Users.CREATED_AT} TEXT NOT NULL,
                ${Users.UPDATED_AT} TEXT NOT NULL,
                ${Users.NEEDS_SYNC} INTEGER NOT NULL DEFAULT 0
                    CHECK (${Users.NEEDS_SYNC} IN (0, 1))
            )
            """.trimIndent()
        )
    }

    // 7.2 media
    private fun createMediaTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE ${Media.TABLE} (
                ${Media.MEDIA_ID} TEXT PRIMARY KEY,
                ${Media.MEDIA_TYPE} TEXT NOT NULL
                    CHECK (${Media.MEDIA_TYPE} IN (
                        '${MediaType.MOVIE}', '${MediaType.TV}',
                        '${MediaType.BOOK}', '${MediaType.ALBUM}')),
                ${Media.EXTERNAL_SOURCE} TEXT,
                ${Media.EXTERNAL_ID} TEXT,
                ${Media.TITLE} TEXT NOT NULL,
                ${Media.DESCRIPTION} TEXT,
                ${Media.CREATOR} TEXT,
                ${Media.RELEASE_YEAR} INTEGER,
                ${Media.RUNTIME_MINUTES} INTEGER,
                ${Media.TOTAL_SEASONS} INTEGER,
                ${Media.TOTAL_EPISODES} INTEGER,
                ${Media.PAGE_COUNT} INTEGER,
                ${Media.TRACK_COUNT} INTEGER,
                ${Media.COVER_IMAGE_URL} TEXT,
                ${Media.COVER_LOCAL_PATH} TEXT,
                ${Media.CREATED_AT} TEXT NOT NULL,
                UNIQUE (${Media.EXTERNAL_SOURCE}, ${Media.EXTERNAL_ID})
            )
            """.trimIndent()
        )
    }

    // 7.3 library_items
    private fun createLibraryItemsTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE ${LibraryItems.TABLE} (
                ${LibraryItems.LIBRARY_ITEM_ID} TEXT PRIMARY KEY,
                ${LibraryItems.USER_ID} TEXT NOT NULL,
                ${LibraryItems.MEDIA_ID} TEXT NOT NULL,
                ${LibraryItems.STATUS} TEXT NOT NULL DEFAULT '${Status.PLANNED}'
                    CHECK (${LibraryItems.STATUS} IN (
                        '${Status.PLANNED}', '${Status.IN_PROGRESS}', '${Status.COMPLETED}',
                        '${Status.ON_HOLD}', '${Status.DROPPED}')),
                ${LibraryItems.IS_FAVORITE} INTEGER NOT NULL DEFAULT 0
                    CHECK (${LibraryItems.IS_FAVORITE} IN (0, 1)),
                ${LibraryItems.RATING} INTEGER
                    CHECK (${LibraryItems.RATING} IS NULL
                        OR ${LibraryItems.RATING} BETWEEN 1 AND 10),
                ${LibraryItems.REVIEW_TEXT} TEXT,
                ${LibraryItems.PERSONAL_NOTES} TEXT,
                ${LibraryItems.DATE_ADDED} TEXT NOT NULL,
                ${LibraryItems.DATE_COMPLETED} TEXT,
                ${LibraryItems.UPDATED_AT} TEXT NOT NULL,
                ${LibraryItems.IS_DELETED} INTEGER NOT NULL DEFAULT 0
                    CHECK (${LibraryItems.IS_DELETED} IN (0, 1)),
                ${LibraryItems.NEEDS_SYNC} INTEGER NOT NULL DEFAULT 0
                    CHECK (${LibraryItems.NEEDS_SYNC} IN (0, 1)),
                UNIQUE (${LibraryItems.USER_ID}, ${LibraryItems.MEDIA_ID}),
                FOREIGN KEY (${LibraryItems.USER_ID})
                    REFERENCES ${Users.TABLE}(${Users.USER_ID}) ON DELETE CASCADE,
                FOREIGN KEY (${LibraryItems.MEDIA_ID})
                    REFERENCES ${Media.TABLE}(${Media.MEDIA_ID}) ON DELETE RESTRICT
            )
            """.trimIndent()
        )
    }

    // 7.4 media_progress (one row per library item)
    private fun createMediaProgressTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE ${MediaProgress.TABLE} (
                ${MediaProgress.LIBRARY_ITEM_ID} TEXT PRIMARY KEY,
                ${MediaProgress.CURRENT_PAGE} INTEGER
                    CHECK (${MediaProgress.CURRENT_PAGE} IS NULL
                        OR ${MediaProgress.CURRENT_PAGE} >= 0),
                ${MediaProgress.TOTAL_PAGES} INTEGER
                    CHECK (${MediaProgress.TOTAL_PAGES} IS NULL
                        OR ${MediaProgress.TOTAL_PAGES} >= 0),
                ${MediaProgress.CURRENT_SEASON} INTEGER
                    CHECK (${MediaProgress.CURRENT_SEASON} IS NULL
                        OR ${MediaProgress.CURRENT_SEASON} >= 0),
                ${MediaProgress.CURRENT_EPISODE} INTEGER
                    CHECK (${MediaProgress.CURRENT_EPISODE} IS NULL
                        OR ${MediaProgress.CURRENT_EPISODE} >= 0),
                ${MediaProgress.UPDATED_AT} TEXT NOT NULL,
                ${MediaProgress.NEEDS_SYNC} INTEGER NOT NULL DEFAULT 0
                    CHECK (${MediaProgress.NEEDS_SYNC} IN (0, 1)),
                CHECK (${MediaProgress.CURRENT_PAGE} IS NULL
                    OR ${MediaProgress.TOTAL_PAGES} IS NULL
                    OR ${MediaProgress.CURRENT_PAGE} <= ${MediaProgress.TOTAL_PAGES}),
                FOREIGN KEY (${MediaProgress.LIBRARY_ITEM_ID})
                    REFERENCES ${LibraryItems.TABLE}(${LibraryItems.LIBRARY_ITEM_ID})
                    ON DELETE CASCADE
            )
            """.trimIndent()
        )
    }

    // 7.5 collections
    private fun createCollectionsTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE ${Collections.TABLE} (
                ${Collections.COLLECTION_ID} TEXT PRIMARY KEY,
                ${Collections.USER_ID} TEXT NOT NULL,
                ${Collections.NAME} TEXT NOT NULL
                    CHECK (length(trim(${Collections.NAME})) > 0),
                ${Collections.DESCRIPTION} TEXT,
                ${Collections.COVER_IMAGE_PATH} TEXT,
                ${Collections.CREATED_AT} TEXT NOT NULL,
                ${Collections.UPDATED_AT} TEXT NOT NULL,
                ${Collections.IS_DELETED} INTEGER NOT NULL DEFAULT 0
                    CHECK (${Collections.IS_DELETED} IN (0, 1)),
                ${Collections.NEEDS_SYNC} INTEGER NOT NULL DEFAULT 0
                    CHECK (${Collections.NEEDS_SYNC} IN (0, 1)),
                FOREIGN KEY (${Collections.USER_ID})
                    REFERENCES ${Users.TABLE}(${Users.USER_ID}) ON DELETE CASCADE
            )
            """.trimIndent()
        )
    }

    // 7.6 collection_items (which library items sit in which collection)
    private fun createCollectionItemsTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE ${CollectionItems.TABLE} (
                ${CollectionItems.COLLECTION_ID} TEXT NOT NULL,
                ${CollectionItems.LIBRARY_ITEM_ID} TEXT NOT NULL,
                ${CollectionItems.ADDED_AT} TEXT NOT NULL,
                ${CollectionItems.UPDATED_AT} TEXT NOT NULL,
                ${CollectionItems.IS_DELETED} INTEGER NOT NULL DEFAULT 0
                    CHECK (${CollectionItems.IS_DELETED} IN (0, 1)),
                ${CollectionItems.NEEDS_SYNC} INTEGER NOT NULL DEFAULT 0
                    CHECK (${CollectionItems.NEEDS_SYNC} IN (0, 1)),
                PRIMARY KEY (${CollectionItems.COLLECTION_ID}, ${CollectionItems.LIBRARY_ITEM_ID}),
                FOREIGN KEY (${CollectionItems.COLLECTION_ID})
                    REFERENCES ${Collections.TABLE}(${Collections.COLLECTION_ID})
                    ON DELETE CASCADE,
                FOREIGN KEY (${CollectionItems.LIBRARY_ITEM_ID})
                    REFERENCES ${LibraryItems.TABLE}(${LibraryItems.LIBRARY_ITEM_ID})
                    ON DELETE CASCADE
            )
            """.trimIndent()
        )
    }

    // 7.7 genres (shared list of genres)
    private fun createGenresTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE ${Genres.TABLE} (
                ${Genres.GENRE_ID} TEXT PRIMARY KEY,
                ${Genres.NAME} TEXT NOT NULL UNIQUE COLLATE NOCASE
                    CHECK (length(trim(${Genres.NAME})) > 0)
            )
            """.trimIndent()
        )
    }

    // 7.8 media_genres (links media to genres, many to many)
    private fun createMediaGenresTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE ${MediaGenres.TABLE} (
                ${MediaGenres.MEDIA_ID} TEXT NOT NULL,
                ${MediaGenres.GENRE_ID} TEXT NOT NULL,
                PRIMARY KEY (${MediaGenres.MEDIA_ID}, ${MediaGenres.GENRE_ID}),
                FOREIGN KEY (${MediaGenres.MEDIA_ID})
                    REFERENCES ${Media.TABLE}(${Media.MEDIA_ID}) ON DELETE CASCADE,
                FOREIGN KEY (${MediaGenres.GENRE_ID})
                    REFERENCES ${Genres.TABLE}(${Genres.GENRE_ID}) ON DELETE CASCADE
            )
            """.trimIndent()
        )
    }

    // 7.9 progress_history (append-only log of status and progress changes)
    private fun createProgressHistoryTable(db: SQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE ${ProgressHistory.TABLE} (
                ${ProgressHistory.HISTORY_ID} TEXT PRIMARY KEY,
                ${ProgressHistory.LIBRARY_ITEM_ID} TEXT NOT NULL,
                ${ProgressHistory.USER_ID} TEXT NOT NULL,
                ${ProgressHistory.OLD_STATUS} TEXT
                    CHECK (${ProgressHistory.OLD_STATUS} IS NULL
                        OR ${ProgressHistory.OLD_STATUS} IN (
                            '${Status.PLANNED}', '${Status.IN_PROGRESS}', '${Status.COMPLETED}',
                            '${Status.ON_HOLD}', '${Status.DROPPED}')),
                ${ProgressHistory.NEW_STATUS} TEXT NOT NULL
                    CHECK (${ProgressHistory.NEW_STATUS} IN (
                        '${Status.PLANNED}', '${Status.IN_PROGRESS}', '${Status.COMPLETED}',
                        '${Status.ON_HOLD}', '${Status.DROPPED}')),
                ${ProgressHistory.CURRENT_PAGE} INTEGER
                    CHECK (${ProgressHistory.CURRENT_PAGE} IS NULL
                        OR ${ProgressHistory.CURRENT_PAGE} >= 0),
                ${ProgressHistory.CURRENT_SEASON} INTEGER
                    CHECK (${ProgressHistory.CURRENT_SEASON} IS NULL
                        OR ${ProgressHistory.CURRENT_SEASON} >= 0),
                ${ProgressHistory.CURRENT_EPISODE} INTEGER
                    CHECK (${ProgressHistory.CURRENT_EPISODE} IS NULL
                        OR ${ProgressHistory.CURRENT_EPISODE} >= 0),
                ${ProgressHistory.CHANGED_AT} TEXT NOT NULL,
                ${ProgressHistory.NEEDS_SYNC} INTEGER NOT NULL DEFAULT 0
                    CHECK (${ProgressHistory.NEEDS_SYNC} IN (0, 1)),
                FOREIGN KEY (${ProgressHistory.LIBRARY_ITEM_ID})
                    REFERENCES ${LibraryItems.TABLE}(${LibraryItems.LIBRARY_ITEM_ID})
                    ON DELETE CASCADE,
                FOREIGN KEY (${ProgressHistory.USER_ID})
                    REFERENCES ${Users.TABLE}(${Users.USER_ID}) ON DELETE CASCADE
            )
            """.trimIndent()
        )
    }

    // Indexes make the common lookups, filters, and sorts fast.
    // Primary keys and UNIQUE rules already get an index automatically.
    private fun createIndexes(db: SQLiteDatabase) {
        // Library filters and sorts (always per user)
        db.execSQL(
            "CREATE INDEX idx_library_user_status " +
                    "ON ${LibraryItems.TABLE}(${LibraryItems.USER_ID}, ${LibraryItems.STATUS})"
        )
        db.execSQL(
            "CREATE INDEX idx_library_user_favorite " +
                    "ON ${LibraryItems.TABLE}(${LibraryItems.USER_ID}, ${LibraryItems.IS_FAVORITE})"
        )
        db.execSQL(
            "CREATE INDEX idx_library_user_date_added " +
                    "ON ${LibraryItems.TABLE}(${LibraryItems.USER_ID}, ${LibraryItems.DATE_ADDED})"
        )

        // Sorting media by title
        db.execSQL(
            "CREATE INDEX idx_media_title ON ${Media.TABLE}(${Media.TITLE})"
        )

        // Find the collections that contain a library item
        db.execSQL(
            "CREATE INDEX idx_collection_items_library_item " +
                    "ON ${CollectionItems.TABLE}(${CollectionItems.LIBRARY_ITEM_ID})"
        )

        // Find the media that belong to a genre
        db.execSQL(
            "CREATE INDEX idx_media_genres_genre " +
                    "ON ${MediaGenres.TABLE}(${MediaGenres.GENRE_ID})"
        )

        // Read the history of one library item in time order
        db.execSQL(
            "CREATE INDEX idx_history_library_item_changed " +
                    "ON ${ProgressHistory.TABLE}(${ProgressHistory.LIBRARY_ITEM_ID}, ${ProgressHistory.CHANGED_AT})"
        )

        // A user cannot have two live collections with the same name (ignoring case).
        // The WHERE part means deleted collections do not count, so a name can be reused.
        db.execSQL(
            "CREATE UNIQUE INDEX idx_collections_user_name " +
                    "ON ${Collections.TABLE}(${Collections.USER_ID}, ${Collections.NAME} COLLATE NOCASE) " +
                    "WHERE ${Collections.IS_DELETED} = 0"
        )
    }

    // "companion object" holds things that belong to the class itself,
    // not to one copy of it (like static members in other languages).
    companion object {
        private const val DATABASE_NAME = "shelf.db"
        private const val DATABASE_VERSION = 1

        // The single shared copy. It starts empty (null).
        private var instance: DatabaseHelper? = null

        // Gives back the shared copy and creates it the first time it is asked for.
        // @Synchronized makes sure two parts of the app cannot create it at the same time.
        @Synchronized
        fun getInstance(context: Context): DatabaseHelper {
            val current = instance
            if (current != null) {
                return current
            }
            // applicationContext lives as long as the app, so we never hold on to a closed screen.
            val created = DatabaseHelper(context.applicationContext)
            instance = created
            return created
        }
    }
}