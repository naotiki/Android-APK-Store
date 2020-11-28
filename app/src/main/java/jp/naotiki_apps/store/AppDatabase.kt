package jp.naotiki_apps.store

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [DownloadHistory::class], version = 1)
abstract class AppDatabase : RoomDatabase() {
    abstract fun downloadHistoryDao(): DownloadHistoryDao
}

