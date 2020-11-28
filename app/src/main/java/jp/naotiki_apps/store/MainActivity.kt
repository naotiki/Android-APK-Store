package jp.naotiki_apps.store

import android.app.Activity
import android.app.DownloadManager
import android.app.DownloadManager.Query
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.*
import android.database.Cursor
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ShareCompat
import androidx.core.content.FileProvider
import androidx.core.view.GravityCompat
import androidx.core.view.get
import androidx.fragment.app.Fragment
import androidx.preference.PreferenceManager
import com.google.android.material.navigation.NavigationView
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.header.*
import org.json.JSONObject
import java.io.File
import java.text.DecimalFormat
import java.util.*


enum class ToolBarMode {
    Normal,
    Share
}

enum class AppState {
    NotInstall,
    HaveUpdate,
    NotUpdate,
}

// Broadcast を受け取る BroadcastReceiver

class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {


    private lateinit var firebaseAnalytics: FirebaseAnalytics
    private lateinit var storage: FirebaseStorage
    private var viewsArray: ArrayList<Any> = arrayListOf()
    private val jobId = 1

    companion object {
        var fileId = -1L
    }

    private var installContext: Context? = null

    var toolBarMode: ToolBarMode = ToolBarMode.Normal
    var shareText: String = ""


    var appKeys = arrayListOf<String>()
    lateinit var appsData: AppsData




    /* var mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
         override fun onReceive(context: Context?, intent: Intent?) {

             val action = intent!!.action

             if (action == DownloadManager.ACTION_DOWNLOAD_COMPLETE) {
                 val id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)
                 if (id == fileId) {
                     val query = Query()
                     query.setFilterByStatus(DownloadManager.STATUS_FAILED)
                     query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL)
                     query.setFilterById(id)
                     val manager = context!!.getSystemService(AppCompatActivity.DOWNLOAD_SERVICE) as DownloadManager
                     val cursor = manager.query(query)
                     if (cursor.moveToFirst()) {
                         val status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS))
                         val url = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_URI))

                         if (status == DownloadManager.STATUS_SUCCESSFUL) {

                             val apkUri = FileProvider.getUriForFile(
                                 application,
                                 BuildConfig.APPLICATION_ID + ".fileprovider", File(Uri.parse(url).path!!)
                             )
                             install(applicationContext,apkUri)

                         }
                         //  エラーのときはよしなに
                     }
                     cursor.close()
                     //  WebViewの片付けが必要ならここのタイミング
                 }
                 fileId=-1
             }
 //-----------------------------------------------FTP用----------------------------------------------
             /*  Log.d("DataReceiver", "onReceive")
               if (intent==null)return
               val id = intent.getStringExtra("id")!!
               val data = intent.getParcelableExtra<ProgressData>("data")!!
               ProgressUIUpdate(id, data)*/
         }
     }

 */

    fun ProgressUIUpdate(id: String, data: ProgressData) {
        val list = findViewById<LinearLayout>(R.id.appList) ?: return
        val card = (list[appKeys.indexOf(id) - 1] as CardView)[0] as LinearLayout
        val progressBar = card[2] as ProgressBar
        val progressTextView = card[3] as TextView
        progressBar.progress = data.persent
        progressTextView.text = "${data.persent}% ${data.MBCount}MB / ${data.MBTotal}MB"
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        val u = Uri.Builder()

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        storage = Firebase.storage("gs://naotikiapps-apks")
        Apps.AppsDataLoad()
        /* val messageFilter =   IntentFilter().apply {
             addAction(DownloadManager.ACTION_DOWNLOAD_COMPLETE)
         }
             LocalBroadcastManager.getInstance(applicationContext)
             .registerReceiver(DownloadReceiver(), messageFilter)*/
        this.setToolbar()
        this.setDrawerLayout()
        this.ConfigCheck()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            this.notificationConfig()
        }

        val (updateCount, _, _) = GetHTTP.CheckUpdate(this)
        if (updateCount > 0) {
            Toast.makeText(this, "$updateCount 件のアップデート", Toast.LENGTH_LONG).show()
        }

        val ft = supportFragmentManager.beginTransaction()

        when (intent.getStringExtra("notiFlag")) {// 通知から遷移

            else -> {
                ft.replace(R.id.frame_contents, app_fragment.newInstance())
                ft.commit()
            }
        }
    }

    /* fun StoreUpdateDialog(_context: Context, views: ArrayList<Any>) {//TODO 外部からの呼び出しでクラッシュ 要確認！！
         installContext=_context
         viewsArray=views
         val (isNewversion, newVersion, _) = httpGet.CheckStoreUpdate()
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
     }*/
    /*
    * */
    fun AppInstallDialog(target: AppInfo) {
        val (appState, _, _) = GetHTTP.CheckAppState(this, target.id)
        when (appState) {
            AppState.NotUpdate -> {
                Toast.makeText(this, "アップデートはありません", Toast.LENGTH_LONG).show()
                //END
            }
            AppState.HaveUpdate -> {
                Toast.makeText(this, "アップデートが見つかりました", Toast.LENGTH_LONG).show()
                CallInstallbyDM(target)
                val params = Bundle().apply { putString("app_name", target.id) }
                firebaseAnalytics.logEvent("update_from_store", params)
            }
            AppState.NotInstall -> {
                Toast.makeText(this, "インスト―ルを開始します", Toast.LENGTH_LONG).show()
                CallInstallbyDM(target)
                val params = Bundle().apply { putString("app_name", target.id) }
                firebaseAnalytics.logEvent("install_from_store", params)
            }
        }
    }

    /*fun apkPermission(installID: Int) {

        val havePermission = installContext!!.packageManager.canRequestPackageInstalls()
        if (!havePermission) {

            val dialog = AlertDialog.Builder(installContext!!)
                .setTitle("更新には権限が必要です") // タイトル
                .setMessage("naotiki App Storeを次の画面で許可してください") // メッセージ
                .setPositiveButton("はい") { dialog, which -> // OK
                    ApppageFragment().startActivityForResult(
                        Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                            .setData(Uri.parse(String.format("package:%s", installContext!!.packageName))), installID
                    )

                }
                .setNegativeButton("キャンセル") { _, _ -> }
                .create()
            // AlertDialogを表示
            dialog.show()


        } else {
            callInstallProcess(installID)
        }
    }*/


    /*  @RequiresApi(api = Build.VERSION_CODES.O)
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
      }*/

  /*  fun callInstallProcess(app: JSONObject, progressViews: List<View>) {
        var lastUpdate = 0L
        var downLoad: com.github.kittinunf.fuel.core.requests.DownloadRequest? = null
        Toast.makeText(this, "${app.getString("name")}をダウンロードしています。", Toast.LENGTH_LONG).show()

        val pass = app.getString("pass")
        val filename = app.getString("id") + ".apk"
        if (File(cacheDir, filename).exists()) {
            File(cacheDir, filename).delete()
        }
        downLoad = httpGet.DownloadAPK(pass, filename, this)
        val progressBar = progressViews[0] as ProgressBar
        val downloadBytes = progressViews[1] as TextView
        var totalbyte: Long? = null
        downLoad.responseProgress { readBytes, totalBytes ->
            if (totalbyte == null) {
                totalbyte = totalBytes
            }
            if (System.currentTimeMillis() - lastUpdate > 500) {
                lastUpdate = System.currentTimeMillis()
                progressBar.progress = (readBytes.toFloat() / totalBytes.toFloat() * 100).toInt()
                //persentageTextView.text =progressBar.progress.toString()+"%"
                downloadBytes.text =
                    (readBytes.toFloat() / 1000000).toString() + "MB" + " / " + (totalBytes.toFloat() / 1000000).toString() + "MB"
            }
        }

        downLoad.response { request, response, result ->
            val apk = File(cacheDir, filename)
            Log.i("my_log", "apk:${apk.path}")
            val apkUri =
                FileProvider.getUriForFile(
                    this,
                    BuildConfig.APPLICATION_ID + ".fileprovider", apk
                )  // 1
            progressBar.progress = 100
            downloadBytes.text =
                (totalbyte!!.toFloat() / 1000000).toString() + "MB" + " / " + (totalbyte!!.toFloat() / 1000000).toString() + "MB"


            val intent = Intent(Intent.ACTION_VIEW)   // 2
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)  // 3
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive")  // 4
            this.startActivity(intent)  // 6

        }
    }*/

    fun CallInstallFromFTP(app: JSONObject, progressViews: List<View>) {
        val data = FTPData(
            "naotiki-apps.xyz",
            "naotiki-apps.xyz",
            "naokiti2468",
            "/server.naotiki-apps.xyz/shooting/download/v1.0.1.apk",
            File(cacheDir, "shooting.apk")
        )
        val intent = Intent(application, FileDownloadService::class.java)
        intent.putExtra("data", data)
        intent.putExtra("id", app.getString("id"))
        intent.putExtra("name", app.getString("name"))
        startService(intent)

        /* val ftp= UpdateAppByFTP()
         ftp.setContext(this)
         ftp.setProgressViews(progressViews)

         ftp.execute(data)*/
    }

    fun CallInstallbyDM(app: AppInfo) {
        val manager = applicationContext.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        val url = app.apkUrl
        Log.i("ns_install", url)
        val mUri = Uri.parse(url)
        val request: DownloadManager.Request = DownloadManager.Request(mUri)
        request.apply {
            setDestinationUri(Uri.fromFile(File(externalCacheDir, "${app.id}.apk").OverWrite()))
            setTitle(app.name)
            setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI)
            setDescription("ダウンロードしています")
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
            }
        }

        fileId = manager.enqueue(request)
        //
        DownloadReceiver.fileIdPair[fileId] = Pair(appsData.Apps.indexOf(app), app.name)
        Thread {

            val format = DecimalFormat("#.#").apply {
                minimumFractionDigits = 2
                maximumFractionDigits = 2
            }
            while (fileId != -1L) {
                val q = Query()
                q.setFilterById(fileId)
                val cursor: Cursor = manager.query(q)
                cursor.moveToFirst()
                val bytesDownloaded = cursor.getInt(
                    cursor
                        .getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR)
                ).toDouble() / 1000000
                val bytesTotal =
                    cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES))
                        .toDouble() / 1000000


                runOnUiThread {
                    UpdateProgress(bytesDownloaded, bytesTotal, this)
                }


                cursor.close()
            }
        }.start()
    }

    /* fun CallInstallFromFirebase(app: JSONObject, progressViews: List<View>){
         var lastUpdate = 0L
         val storageRef = storage.reference
         val pathReference = storageRef.child("${app.getString("id")}/debug.apk")
         Toast.makeText(this, "${app.getString("name")}をダウンロードしています。", Toast.LENGTH_LONG).show()


         val pass = app.getString("pass")
         val filename = app.getString("id") + ".apk"
         if (File(cacheDir, filename).exists()) {
             File(cacheDir, filename).delete()
         }
         val progressBar=progressViews[0] as ProgressBar
         val downloadBytes=progressViews[1] as TextView
         val localFile = File.createTempFile(filename, null, cacheDir)
         val intent=Intent(application,FSDownloadService::class.java)
         intent.putExtra("file",localFile)
         intent.putExtra("pathReference",pathReference)

     }*/
    private fun File.OverWrite(): File {
        if (this.isFile && this.exists()) {
            this.delete()
        }
        return this
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

        channelId = "download_progress"
        channelName = "ダウンロード進捗通知"
        if (manager.getNotificationChannel(channelId) == null) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            channel.apply {
                description = "ファイルダウンロードの進捗を通知します"
                setSound(null, null)
            }
            manager.createNotificationChannel(channel)
        }

        channelId = "install_ready"
        channelName = "インストール準備完了"
        if (manager.getNotificationChannel(channelId) == null) {
            val channel = NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_DEFAULT)
            channel.apply {
                description = "ファイルのダウンロードが終了したら通知します"
            }

            manager.createNotificationChannel(channel)
        }
    }

    /**
     * @see onSharedPreferenceChanged 設定の変更時にトリガーする。
     * **/
    private fun ConfigCheck() {
        val sharedPreferences: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(this)
        val notification = sharedPreferences.getBoolean("notification", true)
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
                        setBackoffCriteria(10000, JobInfo.BACKOFF_POLICY_LINEAR)
                        setPersisted(true)
                        setPeriodic(5000)
                        setRequiredNetworkType(JobInfo.NETWORK_TYPE_ANY)
                        setRequiresCharging(false)

                    }.build()
                scheduler.schedule(jobInfo)
            }
        } else {
            scheduler.cancel(jobId)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.shereButton -> {
                if (shareText != "") {
                    openChooserToShareThisApp()
                }
                return true
            }
        }
        return false
    }

    /// このアプリをSNSシェアできるIntentを起動する
    fun openChooserToShareThisApp() {
        val builder = ShareCompat.IntentBuilder.from(this)
        val subject = "おすすめのNaotiki Apps アプリ"
        val bodyText = shareText
        builder.setSubject(subject) /// 件名
            .setText(bodyText)  /// 本文
            .setType("text/plain")
        val intent = builder.createChooserIntent()

        /// 結果を受け取らずに起動
        builder.startChooser()
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {


        when (toolBarMode) {
            ToolBarMode.Normal -> {
                menu?.findItem(R.menu.header_menu)?.isVisible = false

            }
            ToolBarMode.Share -> {
                if (menu?.findItem(R.menu.header_menu) == null) {
                    menuInflater.inflate(R.menu.header_menu, menu)
                }

                menu?.findItem(R.menu.header_menu)?.isVisible = true

            }
            else -> {
            }
        }
        return super.onCreateOptionsMenu(menu)
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
        var tag: String? = null
        when (item.itemId) {
            R.id.nav_item_category_1 -> {
                fragment = app_fragment.newInstance()
            }
            R.id.nav_item_category_2 -> {
                fragment = user_parameters()
            }
            R.id.nav_sub_item_1 -> {
                if (supportFragmentManager.findFragmentByTag("Settings") == null) {
                    fragment = ApppageFragment.newInstance(STORE_INDEX, true)
                    tag = "Settings"
                } else if (!supportFragmentManager.findFragmentByTag("Settings")!!.isVisible) {
                    fragment = ApppageFragment.newInstance(STORE_INDEX, true)
                    tag = "Settings"
                }


                //StoreUpdateDialog(this)
            }
            R.id.nav_sub_item_2 -> {

            }
        }
        // Replace the fragment.
        if (fragment != null) {

            val ft = supportFragmentManager.beginTransaction()
            ft.replace(R.id.frame_contents, fragment, tag)
            ft.addToBackStack(null)

            ft.commit()

        }
        // Close the Navigation Drawer.
        drawer_layout.closeDrawer(GravityCompat.START)
        return true
    }

    /* fun conf(sharedPreferences: SharedPreferences, key: String, scheduler: JobScheduler) {
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
     }*/
}


