package jp.naotiki_apps.store

import android.content.Context
import android.content.pm.PackageManager.NameNotFoundException
import android.graphics.BitmapFactory
import android.widget.ImageView
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitByteArrayResponseResult
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.json.responseJson
import jp.naotiki_apps.store.Apps.Companion.AppsDataLoad
import jp.naotiki_apps.store.Apps.Companion.GetAppInfoFromAppKey
import jp.naotiki_apps.store.Apps.Companion.appKeysList
import jp.naotiki_apps.store.Apps.Companion.apps
import kotlinx.coroutines.*
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.json.JSONObject
import java.io.*


class GetHTTP {
    companion object {
    fun GetImage(context: Context, url: String, imageView: ImageView) = GlobalScope.launch(Dispatchers.Main) {

        async(Dispatchers.Default) { url.httpGet().awaitByteArrayResponseResult().third }.await().let { it1 ->
            val data = it1.fold({ res -> res }, { _ ->
                null

            })
            if (data != null) {//ここでダウンロードした画像をBitmap形式に変換する。
                val bitmap = BitmapFactory.decodeByteArray(data, 0, data.size)!!

                imageView.setImageBitmap(bitmap)
                //高さ bitmap.height
                //幅 bitmap.width
                /*width= if(bitmap.height >bitmap.width){//縦画像
                    context.resources.getDimension(R.dimen.VerticalImage).toInt()
                }else{//横画像
                    context.resources.getDimension(R.dimen.HorizontalImage).toInt()
                }*/
            }
        }

    }

    //非同期を使う
    fun DownloadAPK(url: String, filename: String, context: Context) = runBlocking {

        Fuel.download(url).fileDestination { response, request ->
            File(context.cacheDir, filename)
        }


    }

     fun GetAppsData() = runBlocking {
        var json: String = ""
        // async/async でもなく withContextってなに？ IntelliJ idea　ナイスすぎ!!
        json = withContext(Dispatchers.Default) {
            RES_URL.httpGet().responseJson().third.get().obj().toString()
        }
        return@runBlocking Json.decodeFromString<AppsData>(json)
    }
    /*fun CheckStoreUpdate():Triple<Boolean?,String,JSONObject>{
        val resultJson = getJsonAwait(RES_URL)
        Log.i("my_job","${LocalDate.now()}:同期処理の結果：" + resultJson.getJSONObject("store").get("version").toString().replace("\\",""))
        val newVersion:String=resultJson.getJSONObject("store").get("version").toString()
        val nowVersion : Int = BuildConfig.VERSION_NAME.replace(".","").toInt()
        val isNewVersion:Boolean=newVersion.replace(".","").toInt()>nowVersion
        return Triple(isNewVersion,newVersion,resultJson)
    }*/
    /**
     *
     * */
    /*fun CheckTPSUpdate(context: Context):Triple<Boolean?,String,JSONObject>{
        val resultJson = getJsonAwait(RES_URL)
        Log.i("my_job","${LocalDate.now()}:同期処理の結果：" + resultJson.getJSONObject("tps").get("version").toString().replace("\\",""))
        val newVersion:String=resultJson.getJSONObject("tps").get("version").toString()
        val appVersion= getAppVersion(context,PackageName.TPS)
        var isNewVersion:Boolean?=null
        if (appVersion!=null){
            val nowVersion : Int = appVersion.replace(".","").toInt()
            isNewVersion=newVersion.replace(".","").toInt()>nowVersion
        }


        return Triple(isNewVersion,newVersion,resultJson)
    }*/
    fun CheckAppState(context: Context, appKey: String): Triple<AppState, String, AppsData> {

        AppsDataLoad()
        val newVersion: String = GetAppInfoFromAppKey(appKey).version
        val packageName = GetAppInfoFromAppKey(appKey).packageName
        val appVersion = if (packageName == BuildConfig.APPLICATION_ID) BuildConfig.VERSION_NAME else getAppVersion(
            context,
            packageName
        )
        var isNewVersion: Boolean? = null
        if (appVersion != null) {
            val nowVersion: Int = appVersion.replace(".", "").toInt()
            isNewVersion = GetAppInfoFromAppKey(appKey).VersionInt() > nowVersion
        }
        val appState = when (isNewVersion) {
            true -> AppState.HaveUpdate
            false -> AppState.NotUpdate
            else -> AppState.NotInstall
        }

        return Triple(appState, newVersion, apps)
    }

    /**
     * @return 個数,idのリスト,JSONのリスト
     * */
    fun CheckUpdate(context: Context): Triple<Int, ArrayList<String>, ArrayList<AppInfo>> {
        val resultJson = GetAppsData()

        var UpdateAppCount = 0
        val UpdateIDList = arrayListOf<String>()
        val UpdateJsonList = arrayListOf<AppInfo>()


        repeat(resultJson.Apps.size) {
            val app = resultJson.Apps[it]

            val newVersion: String = app.version
            val packageName = app.packageName
            val appVersion = if (packageName == BuildConfig.APPLICATION_ID) BuildConfig.VERSION_NAME else getAppVersion(
                context,
                packageName
            )
            var isNewVersion = false
            if (appVersion != null) {
                val nowVersion: Int = appVersion.replace(".", "").toInt()
                isNewVersion = newVersion.replace(".", "").toInt() > nowVersion
                if (isNewVersion) {
                    UpdateAppCount++
                    UpdateIDList.add(app.id)
                    UpdateJsonList.add(app)
                }
            }
        }



        return Triple(UpdateAppCount, UpdateIDList, UpdateJsonList)
    }

    fun getAppVersion(context: Context, packageName: String): String? {
        val pm = context.packageManager
        var versionName: String? = null
        try {
            val packageInfo = pm.getPackageInfo(packageName, 0)
            versionName = packageInfo.versionName
        } catch (e: NameNotFoundException) {
            e.printStackTrace()
        }
        return versionName
    }
}
}