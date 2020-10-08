package jp.naotiki_apps.store

import android.content.Context
import android.content.pm.PackageManager.NameNotFoundException
import android.graphics.BitmapFactory
import android.util.Log
import android.widget.ImageView
import com.github.kittinunf.fuel.Fuel
import com.github.kittinunf.fuel.coroutines.awaitByteArrayResponseResult
import com.github.kittinunf.fuel.httpGet
import com.github.kittinunf.fuel.json.responseJson
import kotlinx.coroutines.*
import org.json.JSONObject
import java.io.*
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate


class GetHTTP {
    fun GetImage(context: Context,url: String,imageView:ImageView)= GlobalScope.launch(Dispatchers.Main) {

        async(Dispatchers.Default) {  url.httpGet().awaitByteArrayResponseResult().third}.await().let { it1 ->
            val data = it1.fold({res-> res}, { _ ->
                null

            })
            if (data!=null) {//ここでダウンロードした画像をBitmap形式に変換する。
                val bitmap= BitmapFactory.decodeByteArray(data, 0, data.size)!!

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
    fun DownloadAPK(url:String, filename:String, context: Context)= runBlocking {

             Fuel.download(url).fileDestination { response, request ->
                 File(context.filesDir,filename)
             }



    }
    fun getApkAwait(url:URL,filename:String)= runBlocking {
   return@runBlocking withContext(Dispatchers.Default){
        var con: HttpURLConnection? = null

    try {
        con = url.openConnection() as HttpURLConnection
       con.connect()
       val status=con.responseCode
         if (status == HttpURLConnection.HTTP_OK) {
            // 通信に成功し

            val input = con.inputStream
            val dataInput =  DataInputStream(input);
            val fileOutput =  FileOutputStream(filename);
            val dataOut =  DataOutputStream(fileOutput);
            val buffer =ByteArray(4096)
            var readByte=0
            do{
                readByte = dataInput.read(buffer)
                dataOut.write(buffer, 0, readByte);
            } while(readByte != -1)

            dataInput.close();
            fileOutput.close();
            dataInput.close();
            input.close();
             return@withContext true
        }else{
             return@withContext false
         }
   } catch (e: IOException) {
e.printStackTrace()
        return@withContext false
   } finally {
       con?.disconnect()
   }
    }
    }
    fun getJsonAwait(url:String) = runBlocking {
        var json: JSONObject = JSONObject().put("message", "NULL")
        //　フルスピードで走るのが俺の人生さ
        // async/async でもなく withContextってなに？ IntelliJ idea　ナイスすぎ!!
        json = withContext(Dispatchers.Default) {
            url.httpGet().responseJson().third.get().obj()
        }
        json
    }
    fun CheckStoreUpdate():Triple<Boolean?,String,JSONObject>{
        val resultJson = GetHTTP().getJsonAwait(RES_URL)
        Log.i("my_job","${LocalDate.now()}:同期処理の結果：" + resultJson.getJSONObject("store").get("version").toString().replace("\\",""))
        val newVersion:String=resultJson.getJSONObject("store").get("version").toString()
        val nowVersion : Int = BuildConfig.VERSION_NAME.replace(".","").toInt()
        val isNewVersion:Boolean=newVersion.replace(".","").toInt()>nowVersion
        return Triple(isNewVersion,newVersion,resultJson)
    }
    /**
     * @return Booleanがtrueで新しいバージョンあり falseでなし NULLでインストールされていない
     * */
    fun CheckTPSUpdate(context: Context):Triple<Boolean?,String,JSONObject>{
        val resultJson = GetHTTP().getJsonAwait(RES_URL)
        Log.i("my_job","${LocalDate.now()}:同期処理の結果：" + resultJson.getJSONObject("tps").get("version").toString().replace("\\",""))
        val newVersion:String=resultJson.getJSONObject("tps").get("version").toString()
        val appVersion= getAppVersion(context,PackageName.TPS)
        var isNewVersion:Boolean?=null
        if (appVersion!=null){
            val nowVersion : Int = appVersion.replace(".","").toInt()
            isNewVersion=newVersion.replace(".","").toInt()>nowVersion
        }


        return Triple(isNewVersion,newVersion,resultJson)
    }
    fun getAppVersion(context: Context,packageName:String):String?{
val pm=context.packageManager;
        var versionName :String?= null
        try {
            val packageInfo = pm.getPackageInfo(packageName, 0)
            versionName = packageInfo.versionName
        } catch (e: NameNotFoundException) {
            e.printStackTrace()
        }
        return versionName
    }

}