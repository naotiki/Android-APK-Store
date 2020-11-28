package jp.naotiki_apps.store

import androidx.room.Entity
import androidx.room.PrimaryKey

/***
 *
 * @param id AppsData.Appsのindex
 * @param fileID DownloadManagerのファイルID
 * @param localApkPath File.path
 * @param downloadedTime いつDLしたか System.currentTimeMillis()で計測
 * @param installed すでにAPKをインストールしたか
 */
@Entity(tableName = "download_history")
data class DownloadHistory(
    @PrimaryKey(autoGenerate = false) val id: Int,//JSON選択用
    val fileID: Long,//fileID
    val localApkPath:String,//Uri作成用
    val downloadedTime:Long,//いつDLしたか System.currentTimeMillis()
    val installed:Boolean//ダウンロード済みか否か
)
