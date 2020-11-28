package jp.naotiki_apps.store

import androidx.room.*

@Dao
interface DownloadHistoryDao {//DAO☆
    @Insert(onConflict = OnConflictStrategy.REPLACE)//既存であるなら置換
    fun insert(downloadHistory : DownloadHistory)

    @Update
    fun update(downloadHistory : DownloadHistory)

    @Delete
    fun delete(downloadHistory : DownloadHistory)

    @Query("SELECT * FROM download_history")
    fun GetAllHistory(): List<DownloadHistory>


}