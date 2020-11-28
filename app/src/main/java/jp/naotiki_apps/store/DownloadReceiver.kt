package jp.naotiki_apps.store

import android.app.DownloadManager
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

import android.net.Uri
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import jp.naotiki_apps.store.MainActivity.Companion.fileId
import jp.naotiki_apps.store.MyApplication.Companion.database
import java.io.File


class DownloadReceiver : BroadcastReceiver() {

    companion object{
// <DownloadManagerのファイルID,Pair<appID,displayName>>
        public var fileIdPair = mutableMapOf<Long, Pair<Int, String>>()
    }
    override fun onReceive(context: Context?, intent: Intent?) {

        val action = intent!!.action

        if (action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
            val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
            if (id == fileId) {
                val query = DownloadManager.Query()
                query.setFilterByStatus(DownloadManager.STATUS_FAILED)
                query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL)

                query.setFilterById(id)
                val manager =
                    context!!.applicationContext.getSystemService(AppCompatActivity.DOWNLOAD_SERVICE) as DownloadManager
                val cursor = manager.query(query)
                if (cursor.moveToFirst()) {
                    val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                    val url = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))

                    if (status == DownloadManager.STATUS_SUCCESSFUL) {



                                val dao =database.downloadHistoryDao()
                                dao.insert(
                                    DownloadHistory(
                                        fileIdPair[fileId]!!.first,
                                        fileId,
                                        Uri.parse(url).path!!,
                                        System.currentTimeMillis(),
                                        false)
                                )






                        val apkUri = FileProvider.getUriForFile(
                            context.applicationContext,
                            BuildConfig.APPLICATION_ID + ".fileprovider", File(Uri.parse(url).path!!)
                        )
                        val apkIntent = Intent(Intent.ACTION_VIEW) // 2
                        apkIntent.setDataAndType(apkUri, "application/vnd.android.package-archive") // 4
                        apkIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // 3
                        apkIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        val notifyPendingIntent = PendingIntent.getActivity(
                            context, 0, apkIntent, PendingIntent.FLAG_UPDATE_CURRENT
                        )
                        val mBuilder: NotificationCompat.Builder = NotificationCompat.Builder(context, "install_ready").apply {
                            setSmallIcon(R.drawable.update)
                            setContentTitle(fileIdPair[id]!!.second+"のインストール準備完了")
                            setContentText("開始するにはここをタップ")
                            setContentIntent(notifyPendingIntent)
                        //折りたたまれるから却下.addAction(R.drawable.about,"インストール",notifyPendingIntent)
                        }


                        val mNotificationManager =
                            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                        //重複防止
                        mNotificationManager.notify(fileIdPair[id]!!.first, mBuilder.build())
                        install(context, apkUri)
                        //  ここでインストーラ呼び出し


                    }
                    //  エラーのときはよしなに
                }
                cursor.close()
                //  WebViewの片付けが必要ならここのタイミング
            }
            fileIdPair.remove(fileId)
            fileId = -1
        }else if (action==Intent.ACTION_PACKAGE_ADDED){//アプリがインストールされた
            val installPackage=intent.`package`//パッケージ名
            var installApp:AppInfo?=null
            for ( i in Apps.apps.Apps.indices){
                if (Apps.apps.Apps[i].packageName==installPackage) {
                    installApp=Apps.apps.Apps[i]
                    break
                }
            }
            if (installApp != null) {//もしアプリがNaotiki Appsなら

                val id=  Apps.GetAppIDFromAppKey(installApp.id)//アプリのID:Intを取得
                val datas= database.downloadHistoryDao().GetAllHistory()
                var downloadHistory:DownloadHistory?=null
                datas.forEach {
                    if (it.id==id){
                        downloadHistory=it
                    }
                }
                if (downloadHistory != null) {//nullCheck
                    database.downloadHistoryDao().update(
                        downloadHistory!!.copy(installed = true)//インストール済みにする
                    )
                }

            }
        }


//-----------------------------------------------FTP用----------------------------------------------
        /*  Log.d("DataReceiver", "onReceive")
          if (intent==null)return
          val id = intent.getStringExtra("id")!!
          val data = intent.getParcelableExtra<ProgressData>("data")!!
          ProgressUIUpdate(id, data)*/
    }


    fun install(context: Context, apkUri: Uri) {
        Log.i("debug", "Start Install")


        Log.i("debug", "apk:$apkUri")
        val apkIntent = Intent(Intent.ACTION_VIEW) // 2
        apkIntent.setDataAndType(apkUri, "application/vnd.android.package-archive") // 4
        apkIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // 3
        apkIntent.addFlags(Intent.FLAG_ACTIVITY_TASK_ON_HOME)
        apkIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        Log.i("debug", "起動前")
        context.startActivity(apkIntent)
    }
}
