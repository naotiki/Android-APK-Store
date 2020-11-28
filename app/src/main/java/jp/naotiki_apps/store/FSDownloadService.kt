
package jp.naotiki_apps.store
/*
import android.app.IntentService
import android.content.Intent
import android.content.Context
import android.util.Log
import androidx.core.content.FileProvider
import java.io.File

class FSDownloadService : IntentService("FSDownloadService") {

    override fun onHandleIntent(intent: Intent?) {
        val file= intent!!.getSerializableExtra("file")!!
         pathReference= intent!!.getSerializableExtra("pathReference")!!
        pathReference.getFile(file).apply {
            addOnSuccessListener {
                val apk= File(this@MainActivity.cacheDir, filename)
                Log.i("my_log", "apk:${apk.path}")

                progressBar.progress=100
                val apkUri =
                    FileProvider.getUriForFile(
                        this@MainActivity,
                        BuildConfig.APPLICATION_ID + ".fileprovider", apk
                    )  // 1
                val intent = Intent(Intent.ACTION_VIEW)   // 2
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)  // 3
                intent.setDataAndType(apkUri, "application/vnd.android.package-archive")  // 4
                this@MainActivity.startActivity(intent)  // 6
            }
            addOnFailureListener {
                it.printStackTrace()
            }
            addOnProgressListener {
                if (System.currentTimeMillis() - lastUpdate > 500) {
                    lastUpdate = System.currentTimeMillis()
                    progressBar.progress=(it.bytesTransferred / it.totalByteCount).toInt()*100


                    downloadBytes.text=(it.bytesTransferred).toString()+"/"+(it.totalByteCount).toString()

                }
            }
        }
    }




}
*/