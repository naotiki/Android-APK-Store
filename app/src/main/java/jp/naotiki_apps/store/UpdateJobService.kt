package jp.naotiki_apps.store


import android.app.PendingIntent
import android.app.job.JobParameters
import android.app.job.JobService
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.TaskStackBuilder
import org.json.JSONObject
import java.time.LocalDate

class UpdateJobService : JobService() {
    override fun onStopJob(params: JobParameters?): Boolean {

        jobFinished(params, false)
        return false
    }

    override fun onStartJob(params: JobParameters?): Boolean {

        Thread(
            Runnable {
                SendNoti("store"){GetHTTP().CheckStoreUpdate()}
                SendNoti("tps") {GetHTTP().CheckTPSUpdate(this)}
                Log.i(javaClass.name, "${LocalDate.now()}:スケジュールしたジョブで呼び出された処理")
                jobFinished(params, false)
            }
        ).start()



        return true

}
fun SendNoti(appName:String,UpdateUnit:()->Triple<Boolean?,String, JSONObject>){
    val resultJson = GetHTTP().getJsonAwait(RES_URL)
    // レスポンスボディを表示
    val (isNewVersion,newVersion,_)=UpdateUnit()
    Log.i("my_job", "${LocalDate.now()}:同期処理の結果：" + resultJson.toString().replace("\\", ""))

    if (isNewVersion!=null&&isNewVersion) {
        val intent = Intent(this, MainActivity::class.java)
        intent.putExtra("notiFlag", appName)
        val stackBuilder = TaskStackBuilder.create(this)
        stackBuilder.addParentStack(MainActivity::class.java)
        stackBuilder.addNextIntent(intent)
        val pendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
        val manager = NotificationManagerCompat.from(this)
        val notification = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationCompat.Builder(applicationContext, "update_$appName")
        } else {//API N_MR1 以下
            NotificationCompat.Builder(applicationContext)
        }.apply {

            val fullName= myAppname().FullName(appName)
            setContentTitle("${fullName}アプリの更新")
            setContentText("$newVersion が公開されました。")
            setSmallIcon(R.drawable.update)
            setContentIntent(pendingIntent)
            setAutoCancel(true)
        }.build()

        manager.notify(STORE, notification)
    }
}

    }
