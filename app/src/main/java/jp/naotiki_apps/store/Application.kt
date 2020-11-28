package jp.naotiki_apps.store

import android.app.Application
import androidx.room.Room

class MyApplication: Application() {
    companion object {
        lateinit var database: AppDatabase
    }

    override fun onCreate() {
        super.onCreate()

         database =
            Room.databaseBuilder(
                applicationContext,
                AppDatabase::class.java,
                "download_history"
            ).build()
    }
}