package jp.naotiki_apps.store


import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import android.widget.*
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.content.FileProvider
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.jama.carouselview.CarouselView
import com.jama.carouselview.enums.IndicatorAnimationType
import com.jama.carouselview.enums.OffsetType
import java.io.File


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM = "app_id"
private const val ARG_PARAM2 = "update_flag"


class ApppageFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var appId: String? = null
    private var updateflag:Boolean?=null
    private val APK_REQ_ID = listOf(111, 112)
    var store = arrayListOf<Any>()// 内部ID アプリ名 説明文 インストール関数
    var tps = arrayListOf<Any>()// 内部ID アプリ名
    val GetHTTP=GetHTTP()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            appId = it.getString(ARG_PARAM)
            updateflag =it.getBoolean(ARG_PARAM2)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_apppage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.findViewById<LinearLayout>(R.id.progressLayout).visibility=View.GONE
        view.findViewById<LinearLayout>(R.id.details).visibility=View.GONE
         store = arrayListOf("store", "naotiki App Store", R.string.store)// 内部ID アプリ名 説明文 インストール関数
         tps = arrayListOf("tps", "TPS", R.string.store)// 内部ID アプリ名
        val Card1 =view.findViewById<CardView>(R.id.card1)
        CardInitialize(Card1)

        val array : ArrayList<Any> = when(appId){
            "store" -> {
                store
            }
            "tps" -> {
                tps
            }
            else -> {
                arrayListOf()
            }
        }

        var json= GetHTTP.getJsonAwait(RES_URL).getJSONObject(array[0].toString())
        val appTitle=view.findViewById<TextView>(R.id.appTitle)
        val versionText =view.findViewById<TextView>(R.id.Versioninfo)
        val updateText =view.findViewById<TextView>(R.id.update_text)
        val appText=view.findViewById<TextView>(R.id.app_info)
        val installBtn =view.findViewById<Button>(R.id.installBtn)
val swipeUpdate=view.findViewById<SwipeRefreshLayout>(R.id.swipeupdate)




        var newversion=json.getString("version")

GetHTTP.GetImage(context!!, json.getJSONObject("imageurl").getString("icon"), view.findViewById(R.id.imageView2))
swipeUpdate.setOnRefreshListener {
   json= GetHTTP.getJsonAwait(RES_URL).getJSONObject(array[0].toString())
    newversion=json.getString("version")
    versionText.text="V$newversion"
    updateText.text=json.getString("comment")


    when(appId){// アプリごとの初期化処理
        "store" -> {
            installBtn.visibility = View.VISIBLE
            val (isNewversion, newVersion, _) = GetHTTP.CheckStoreUpdate()
            if (isNewversion!!) {
                Toast.makeText(context, "更新があります", Toast.LENGTH_LONG).show()
                installBtn.text = "更新"
                if (updateflag!!) {
                    StoreUpdateDialog()
                }
            } else {
                installBtn.visibility = View.GONE
            }

        }
        "tps" -> {
            installBtn.visibility = View.VISIBLE
            var isInstall = true
            try {
                context!!.packageManager.getPackageInfo(PackageName.TPS, 0)
            } catch (e: PackageManager.NameNotFoundException) {
                isInstall = false
                e.printStackTrace()
            }
            if (isInstall) {
                val (isNewversion, newVersion, _) = GetHTTP.CheckTPSUpdate(context!!)
                if (isNewversion != null && isNewversion) {
                    Toast.makeText(context, "更新があります", Toast.LENGTH_LONG).show()
                    installBtn.text = "更新"
                    if (updateflag!!) {
                        TPSUpdateDialog()
                    }
                } else {
                    installBtn.visibility = View.GONE
                }
            } else {
                installBtn.text = "インストール"

            }

        }
        else -> {

        }
    }
    swipeUpdate.isRefreshing=false
}
        appTitle.text= array[1].toString()
        versionText.text="V$newversion"
        updateText.text=json.getString("comment")
        appText.text=getString(array[2] as Int)
        installBtn.setOnClickListener {
            when(appId){
                "store" -> {
                    StoreUpdateDialog()
                }
                "tps" -> {

                }
                else -> {
                }
            }

        }
//スクリーンショット設定
        val jsonArray= json.getJSONObject("imageurl").getJSONArray("screenshot")
        val urlArrayList= arrayListOf<String>()
        for (i in 0 until jsonArray.length()) {
            if (jsonArray[i].toString()!="url"){
                urlArrayList.add(jsonArray[i].toString())
            }

        }
        val carouselView = view.findViewById<CarouselView>(R.id.carouselView)
        carouselView.apply {

            size = urlArrayList.size
            resource = R.layout.center_carousel_item
            autoPlay = false

            indicatorAnimationType = IndicatorAnimationType.THIN_WORM
            carouselOffset = OffsetType.CENTER
            setCarouselViewListener { view, position ->
                // Example here is setting up a full image carousel
                val imageView = view.findViewById<ImageView>(R.id.imageView)
                imageView.scaleType=ImageView.ScaleType.FIT_CENTER
                GetHTTP.GetImage(context!!, urlArrayList[position], imageView)

            }
            // After you finish setting up, show the CarouselView
            show()
        }

        when(appId){// アプリごとの初期化処理
            "store" -> {
                val (isNewversion, newVersion, _) = GetHTTP.CheckStoreUpdate()
                if (isNewversion!!) {
                    installBtn.text = "更新"
                    if (updateflag!!) {
                        StoreUpdateDialog()
                    }
                } else {
                    installBtn.visibility = View.GONE
                }

            }
            "tps" -> {
                var isInstall = true
                try {
                    val packageInfo = context!!.packageManager.getPackageInfo(PackageName.TPS, 0)
                } catch (e: PackageManager.NameNotFoundException) {
                    isInstall = false
                    e.printStackTrace()
                }
                if (isInstall) {
                    val (isNewversion, newVersion, _) = GetHTTP.CheckTPSUpdate(context!!)
                    if (isNewversion != null && isNewversion) {
                        installBtn.text = "更新"
                        if (updateflag!!) {
                            //TODO 個別で作る
                            TPSUpdateDialog()
                        }
                    } else {
                        installBtn.visibility = View.GONE
                    }
                } else {
                    installBtn.text = "インストール"
                }

            }
            else -> {

            }
        }
view.findViewById<TextView>(R.id.weblink).text=json.getString("infourl")
    }
    /**
     * @param view 初期化するCardView
     * **/

    fun CardInitialize(view: CardView){
        val info =(view.getChildAt(0)as LinearLayout).getChildAt(0) as LinearLayout
        val details= (view.getChildAt(0)as LinearLayout).getChildAt(1) as LinearLayout
val icon =((view.getChildAt(0)as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(1) as ImageView
        info.setOnClickListener {
            val inAnimation = AnimationUtils.loadAnimation(context, R.anim.in_animation)
            val openAnimation = RotateAnimation(
                0.0f, 180.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            ).apply {
                duration = 500
                fillAfter = true
            }

            val closeAnimation = RotateAnimation(
                180.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            ).apply {
                duration = 500
                fillAfter = true
            }
            if (details.visibility==View.GONE){//非表示

                icon.startAnimation(openAnimation)
                details.startAnimation(inAnimation);
                details.visibility=View.VISIBLE //表示する
            }else if (details.visibility==View.VISIBLE){//表示
                //   details.startAnimation(outAnimation);
                icon.startAnimation(closeAnimation)
                details.visibility=View.GONE //非表示にする
            }
        }
    }
    fun StoreUpdateDialog() {//TODO 外部からの呼び出しでクラッシュ 要確認！！

        val (isNewversion, newVersion, _) = GetHTTP.CheckStoreUpdate()
        if (isNewversion!!) {
            // BuilderからAlertDialogを作成
            val dialog = AlertDialog.Builder(context!!)
                .setTitle("Storeの更新があります") // タイトル
                .setMessage("バージョン $newVersion があります\n更新しますか?") // メッセージ
                .setPositiveButton("はい") { dialog, which -> // OK
                    //apkPermission(111)
                    callInstallProcess(APK_REQ_ID[0])
                }
                .setNegativeButton("いいえ") { _, _ -> }
                .create()
            // AlertDialogを表示
            dialog.show()
        }
    }
    fun TPSUpdateDialog() {//TODO 外部からの呼び出しでクラッシュ 要確認！！

        val (isNewversion, newVersion, _) = GetHTTP.CheckStoreUpdate()
        if (isNewversion!=null&&isNewversion) {
            // BuilderからAlertDialogを作成
            val dialog = AlertDialog.Builder(context!!)
                .setTitle("TPSの更新があります") // タイトル
                .setMessage("バージョン $newVersion があります\n更新しますか?") // メッセージ
                .setPositiveButton("はい") { dialog, which -> // OK
                    //apkPermission(111)
                    callInstallProcess(APK_REQ_ID[1])
                }
                .setNegativeButton("いいえ") { _, _ -> }
                .create()
            // AlertDialogを表示
            dialog.show()
        }else if(isNewversion==null){
            val dialog = AlertDialog.Builder(context!!)
                .setTitle("TPSのインストール") // タイトル
                .setMessage("バージョン $newVersion があります\nインストールしますか?") // メッセージ
                .setPositiveButton("はい") { dialog, which -> // OK
                    //apkPermission(111)
                    callInstallProcess(APK_REQ_ID[1])
                }
                .setNegativeButton("いいえ") { _, _ -> }
                .create()
            // AlertDialogを表示
            dialog.show()
        }
    }
    private fun apkPermission(installID: Int) {

        val havePermission = context!!.packageManager.canRequestPackageInstalls()
        if (!havePermission) {

            val dialog = AlertDialog.Builder(context!!)
                .setTitle("更新には権限が必要です") // タイトル
                .setMessage("naotiki App Storeを次の画面で許可してください") // メッセージ
                .setPositiveButton("はい") { dialog, which -> // OK
                    startActivityForResult(
                        Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES)
                            .setData(Uri.parse(String.format("package:%s", context!!.packageName))), installID
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
            if (context!!.packageManager.canRequestPackageInstalls()) {
                callInstallProcess(requestCode)
            } else {
                apkPermission(requestCode)
            }
        } else if (APK_REQ_ID.contains(requestCode)) {
            apkPermission(requestCode)
        }
    }
    private fun callInstallProcess(reqid: Int) {
        var lastUpdate = 0L
        var downLoad:com.github.kittinunf.fuel.core.requests.DownloadRequest?=null
        var filename:String=""
        var pass:String=""
        when (reqid) {
            111 -> {// Store
                Toast.makeText(context!!, "Storeをダウンロードしています。", Toast.LENGTH_LONG).show()
                val (_, _, json) = GetHTTP.CheckStoreUpdate()
                pass = json.getJSONObject("store").getString("pass")
                filename = "Store" + ".apk"
                if (File(context!!.filesDir, filename).exists()) {
                    File(context!!.filesDir, filename).delete()
                }

// TODO 次回!　114514話 プログレスバーでダウンロード進捗情報を視覚化する！　お楽しみに！


            }
            112 -> {// TPS
                Toast.makeText(context!!, "TPSをダウンロードしています。", Toast.LENGTH_LONG).show()
                val (_, _, json) = GetHTTP.CheckTPSUpdate(context!!)
                pass = json.getJSONObject("tps").getString("pass")
                filename = "TPS" + ".apk"
                if (File(context!!.filesDir, filename).exists()) {
                    File(context!!.filesDir, filename).delete()
                }
            }
        }
        downLoad=  GetHTTP.DownloadAPK(pass, filename, context!!)
        val progressLayout=view!!.findViewById<LinearLayout>(R.id.progressLayout)
        progressLayout.visibility=View.VISIBLE
        val progressBar=view!!.findViewById<ProgressBar>(R.id.progressBar)
        val persentageTextView= view!!.findViewById<TextView>(R.id.persentageText)
        val downloadBytes=view!!.findViewById<TextView>(R.id.byteText)
        var totalbyte:Long?=null
        downLoad.responseProgress{ readBytes, totalBytes ->
            if (totalbyte == null) {
                totalbyte=totalBytes
            }
            if (System.currentTimeMillis() - lastUpdate > 500) {
                lastUpdate = System.currentTimeMillis()
                progressBar.progress=(readBytes.toFloat() / totalBytes.toFloat()*100).toInt()
                persentageTextView.text =progressBar.progress.toString()+"%"
                downloadBytes.text=(readBytes.toFloat()/1000000).toString()+"MB"+" / "+(totalBytes.toFloat()/1000000).toString()+"MB"
            }
        }

        downLoad.response { request, response, result ->
           val apk= File(context!!.filesDir, filename)
            Log.i("my_log", "apk:${apk.path}")
            val apkUri =
                FileProvider.getUriForFile(
                    context!!,
                    BuildConfig.APPLICATION_ID + ".fileprovider", apk
                )  // 1
            progressBar.progress=100
            persentageTextView.text="100%"
            downloadBytes.text=(totalbyte!!.toFloat()/1000000).toString()+"MB"+" / "+(totalbyte!!.toFloat()/1000000).toString()+"MB"


            val intent = Intent(Intent.ACTION_VIEW)   // 2
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)  // 3
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive")  // 4
            context!!.startActivity(intent)  // 6

        }




    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param app_id Application ID.
         * @param update_flag すぐにアップデート確認処理に行くかのフラグ。
         * @return 一つの新しいApppageFragmentフラグメントのインスタンスを返す(直訳)
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(app_id: String, update_flag: Boolean = false) =
            ApppageFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM, app_id)
                    putBoolean(ARG_PARAM2, update_flag)
                }
            }
    }
}
