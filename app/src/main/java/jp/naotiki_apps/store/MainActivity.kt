package jp.naotiki_apps.store

import android.app.Activity
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.MenuItem
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.navigation.NavigationView
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.header.*
import java.io.File


class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private var viewsArray: ArrayList<Any> = arrayListOf()
    private val jobId = 1;

    private var installContext:Context?=null

    @RequiresApi(Build.VERSION_CODES.N)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        this.setToolbar()
        this.setDrawerLayout()
        this.ConfigCheck()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.notificationConfig()
        }

        val (isNewVersion,_,_)=GetHTTP().CheckStoreUpdate()
        val (isNewVersion2,_,_)=GetHTTP().CheckTPSUpdate(this)
        var c = 0
        if (isNewVersion!!) {//アップデートあり
            c += 1

        }
        if (isNewVersion2!=null&&isNewVersion2){
            c+=1
        }
        if (c>0){
            Toast.makeText(this,"$c 件のアップデート",Toast.LENGTH_LONG).show()
        }

        val ft = supportFragmentManager.beginTransaction()

        when (intent.getStringExtra("notiFlag")) {// 通知から遷移
            "store" -> {
                ft.replace(R.id.frame_contents, ApppageFragment.newInstance("store", true))
                ft.commit()
            }
            "tps" -> {
                ft.replace(R.id.frame_contents, ApppageFragment.newInstance("tps", true))
                ft.commit()
            }
            else->{
                ft.replace(R.id.frame_contents, app_fragment.newInstance())
                ft.commit()
            }
        }
    }

    fun StoreUpdateDialog(_context: Context,views:ArrayList<Any>) {//TODO 外部からの呼び出しでクラッシュ 要確認！！
        installContext=_context
        viewsArray=views
        val (isNewversion, newVersion, _) = GetHTTP().CheckStoreUpdate()
        if (isNewversion!!) {
            // BuilderからAlertDialogを作成
            val dialog = AlertDialog.Builder(installContext!!)
                .setTitle("Storeの更新があります") // タイトル
                .setMessage("バージョン $newVersion があります\n更新しますか?") // メッセージ
                .setPositiveButton("はい") { dialog, which -> // OK
                    //apkPermission(111)
                    callInstallProcess(111)
                }
                .setNegativeButton("いいえ") { _, _ -> }
                .create()
            // AlertDialogを表示
            dialog.show()
        }
    }
    fun apkPermission(installID: Int) {

        val havePermission = installContext!!.packageManager.canRequestPackageInstalls()
        if (!havePermission) {

            val dialog = AlertDialog.Builder(installContext!!)
                .setTitle("更新には権限が必要です") // タイトル
                .setMessage("naotiki App Storeを次の画面で許可してください") // メッセージ
                .setPositiveButton("はい") { dialog, which -> // OK
                    ApppageFragment().startActivityForResult(
                        Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                            .setData(Uri.parse(String.format("package:%s", installContext!!.packageName)))
                        , installID
                    )

                }
                .setNegativeButton("キャンセル") { _, _ -> }
                .create()
            // AlertDialogを表示
            dialog.show()


        } else {
            callInstallProcess(installID)
        }
    }



    @RequiresApi(api = Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (APK_REQ_ID.contains(requestCode) && resultCode == Activity.RESULT_OK) {
            if (packageManager.canRequestPackageInstalls()) {
                callInstallProcess(requestCode)
            } else {
                apkPermission(requestCode)
            }
        } else if (APK_REQ_ID.contains(requestCode)) {
            apkPermission(requestCode)
        }
    }

    fun callInstallProcess(reqid: Int) {
        var lastUpdate = 0L
        var downLoad:com.github.kittinunf.fuel.core.requests.DownloadRequest?=null
        var filename:String=""
        when (reqid) {
            111 -> {// Store
                Toast.makeText(installContext!!, "Storeをダウンロードしています。", Toast.LENGTH_LONG).show()
                val (_, _, json) = GetHTTP().CheckStoreUpdate()
                val pass = json.getJSONObject("store").getString("pass")
                filename = "Store"+".apk"
                downLoad=  GetHTTP().DownloadAPK(pass, filename,installContext!!)//getApkAwait(URL(pass),filename)
// TODO 次回!　114514話 プログレスバーでダウンロード進捗情報を視覚化する！　お楽しみに！


            }
            112 -> {// TPS

            }
        }
        val progressBar=viewsArray[0] as ProgressBar
        val persentageTextView= viewsArray[1] as TextView
        val downloadBytes=viewsArray[2] as TextView
        downLoad!!.progress{readBytes, totalBytes ->
            if (System.currentTimeMillis() - lastUpdate > 500) {
                lastUpdate = System.currentTimeMillis()
                progressBar.progress=(readBytes.toFloat() / totalBytes.toFloat()*100).toInt()
             //   persentageTextView.text =progressBar.progress.toString()

                downloadBytes.text=(readBytes.toFloat()/1000000).toString()+"/"+(totalBytes.toFloat()/1000000).toString()

            }
        }
            downLoad.response { request, response, result ->
                val apk= File(installContext!!.filesDir,filename)
                Log.i("my_log","apk:${apk.path}")
                val apkUri =
                    FileProvider.getUriForFile(installContext!!,
                        BuildConfig.APPLICATION_ID + ".fileprovider", apk)  // 1
                progressBar.progress=100
                val intent = Intent(Intent.ACTION_VIEW)   // 2
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)  // 3
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive")  // 4
                installContext!!.startActivity(intent)  // 6
            }



    }

    private fun notificationConfig() {

        var channelId = "update_store"
        var channelName = "Storeの通知"
        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (manager.getNotificationChannel(channelId) == null) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            channel.apply {
                description = "Storeアプリのアップデートを通知します"
            }
            manager.createNotificationChannel(channel)
        }
        channelId = "update_tps"
        channelName = "tuyoponの通知"
        if (manager.getNotificationChannel(channelId) == null) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            channel.apply {
                description = "シューティングアプリのアップデートを通知します"
            }
            manager.createNotificationChannel(channel)
        }
    }

    /**
     * @see onSharedPreferenceChanged 設定の変更時にトリガーする。
     * **/
    private fun ConfigCheck() {
        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val notification = sharedPreferences.getBoolean("notification", true);
        val scheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler
        if (notification) {
            Log.i("job_noti", scheduler.getPendingJob(jobId).toString())
            Log.i("job_noti", scheduler.allPendingJobs.toString())
            if (scheduler.getPendingJob(jobId) == null) {//ジョブなし
                val componentName = ComponentName(
                    this,
                    UpdateJobService::class.java
                )

                val jobInfo = JobInfo.Builder(jobId, componentName)
                    .apply {
                        setBackoffCriteria(10000, JobInfo.BACKOFF_POLICY_LINEAR);
                        setPersisted(true)
                        setPeriodic(5000)
                        setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                        setRequiresCharging(false)
                        
                    }.build()
                scheduler.schedule(jobInfo)
            }
        } else {
            scheduler.cancel(jobId);
        }
    }

    private fun setToolbar() {
        setSupportActionBar(toolbar)
        supportActionBar!!.setDisplayShowHomeEnabled(false)
    }

    private fun setDrawerLayout() {
        val toggle = ActionBarDrawerToggle(Activity(), drawer_layout, toolbar, R.string.nav_open, R.string.nav_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()
        nav_view.setNavigationItemSelectedListener(this)
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        var fragment: Fragment? = null
        var tag:String?=null
        when (item.itemId) {
            R.id.nav_item_category_1 -> {
                fragment = app_fragment.newInstance()
            }
            R.id.nav_item_category_2 -> {
                fragment = user_parameters()
            }
            R.id.nav_sub_item_1 -> {
                if (supportFragmentManager.findFragmentByTag("Settings")==null){
                    fragment = ApppageFragment.newInstance("store",true)
                    tag="Settings"
                }else if (!supportFragmentManager.findFragmentByTag("Settings")!!.isVisible){
                    fragment = ApppageFragment.newInstance("store",true)
                    tag="Settings"
                    }


                //StoreUpdateDialog(this)
            }
            R.id.nav_sub_item_2->{

            }
        }
        // Replace the fragment.
        if (fragment != null) {

            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.frame_contents, fragment,tag)
            ft.addToBackStack(null)

            ft.commit()

        }
        // Close the Navigation Drawer.
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    fun conf(sharedPreferences: SharedPreferences, key: String, scheduler: JobScheduler) {
        Log.i("n_runi", "RUNNNNNNNN!!!!!!")
        //val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(MainActivity())
        val notification = sharedPreferences.getBoolean("notification", true);
        //val scheduler = getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler;
        if (notification) {
            Log.i("job_noti", scheduler.getPendingJob(jobId).toString())
            Log.i("job_noti", scheduler.allPendingJobs.toString())
            if (scheduler.getPendingJob(jobId) == null) {//ジョブなし
                val componentName = ComponentName(MainActivity(), UpdateJobService::class.java)

                val jobInfo = JobInfo.Builder(jobId, componentName)
                    .apply {
                        setBackoffCriteria(10000, JobInfo.BACKOFF_POLICY_LINEAR);
                        setPersisted(true)
                        setPeriodic(5000)
                        setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
                        setRequiresCharging(false)
                    }.build()
                scheduler.schedule(jobInfo)
            }
            Log.i("job_noti", scheduler.getPendingJob(jobId).toString())
            Log.i("job_noti", scheduler.allPendingJobs.toString())
        } else {
            scheduler.cancel(jobId);
        }
    }
}
