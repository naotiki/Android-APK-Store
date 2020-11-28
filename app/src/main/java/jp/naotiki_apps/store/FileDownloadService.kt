package jp.naotiki_apps.store

import android.annotation.SuppressLint
import android.app.IntentService
import android.app.Notification
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.util.Log
import android.view.View
import androidx.core.app.NotificationCompat
import androidx.core.content.FileProvider
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import org.apache.commons.io.output.CountingOutputStream
import org.apache.commons.net.ftp.FTPClient
import org.apache.commons.net.ftp.FTPConnectionClosedException
import org.apache.commons.net.ftp.FTPReply
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.math.BigDecimal
import java.text.DecimalFormat


class FileDownloadService : IntentService("FileDownloadIntentService") {
    val notificationId = 1;
    private  var  context: Context=this
    private  var progressViews: List<View>?=null
    lateinit var manager :NotificationManager
    override fun onCreate() {
        super.onCreate()
      manager=  getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }



    override fun onBind(intent: Intent): IBinder {
        TODO("Return the communication channel to the service.")
    }

    override fun onHandleIntent(_intent: Intent?) {
        val data=_intent!!.getParcelableExtra<FTPData>("data")!!
        val id=_intent.getStringExtra("id")!!
        val name=_intent.getStringExtra("name")!!
        try {
            val (serverName, userName, password, serverFilePath, localFile) = data
            if (getFileByFTP(serverName, userName, password, serverFilePath, localFile, name, id)) {
                /*   Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(new File(localFilePath)), "application/vnd.android.package-archive");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK); // without this flag android returned a intent error!
                context.startActivity(intent);*/
            } else {
                //Do nothing could not download
            }
            Log.i("debug", "Start Install")
            val apkUri = FileProvider.getUriForFile(
                application,
                BuildConfig.APPLICATION_ID + ".fileprovider", localFile
            ) // 1\

            Log.i("debug", "apk:$apkUri")
            val intent = Intent(Intent.ACTION_VIEW) // 2
            intent.setDataAndType(apkUri, "application/vnd.android.package-archive") // 4
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION) // 3


            application.startActivity(intent) // 6
            Log.i("debug", "end")
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }


    override fun onDestroy() {
        Log.d("Debug TEST", "onDestroy")
        super.onDestroy()
        manager.cancel(notificationId)
    }

    //Below code to download using FTP
    @Throws(Exception::class)
     fun getFileByFTP(
        serverName: String, userName: String,
        password: String, serverFilePath: String, localFile: File, name: String, id: String
    ): Boolean {
        val ftp = FTPClient()
        try {
            ftp.connect(serverName)
            val reply = ftp.replyCode
            if (!FTPReply.isPositiveCompletion(reply)) {
                ftp.disconnect()
                return false
            }
        } catch (e: IOException) {
            if (ftp.isConnected) {
                try {
                    ftp.disconnect()
                } catch (f: IOException) {
                    throw f
                }
            }
            throw e
        } catch (e: Exception) {
            throw e
        }
            return try {
            if (!ftp.login(userName, password)) {
                ftp.logout()
            }
            ftp.setFileType(FTPClient.BINARY_FILE_TYPE)
            ftp.enterLocalPassiveMode()

            val builder = NotificationCompat.Builder(
                context,
                "download_progress"
            ).apply {
                setContentTitle("${name}のダウンロード中")
                setContentText("0% 0MB/?MB")
                setSmallIcon(R.drawable.update)
                setProgress(100, 0, false)

            }
            val noti = builder.build()
            noti.flags = Notification.FLAG_NO_CLEAR;

            manager.notify(notificationId, noti)
                val format = DecimalFormat("#.#").apply {
                    minimumFractionDigits = 2
                    maximumFractionDigits = 2
                }
            val lenghtOfFile = getFileSize(ftp, serverFilePath).toInt()
            val output: OutputStream = FileOutputStream(localFile)
            val cos: CountingOutputStream = object : CountingOutputStream(output) {
                @SuppressLint("SetTextI18n")
                override fun beforeWrite(n: Int) {
                    super.beforeWrite(n)
                    Log.i("debug", "Writing")
                    val countMB = count.toDouble() / 1000000
                    val allMB = lenghtOfFile.toDouble() / 1000000
                    val percent = (countMB / allMB * 100).toInt()
                    /*  Log.i("FTP_DOWNLOAD", "bytesTransferred /downloaded" + lenghtOfFile.toFloat() / 1000000)
                    Log.i(
                        "FTP_DOWNLOAD",
                        "Downloaded " + count.toFloat() / 1000000 + "/" + lenghtOfFile.toFloat() / 1000000
                    )*/
                    val number = BigDecimal(Math.round(countMB * 100.0) / 100.0)
                    val number2 = BigDecimal(Math.round(allMB * 100.0) / 100.0)

                    builder.setProgress(100, percent, false)
                    builder.setContentText("$percent% ${format.format(number)}MB / ${format.format(number2)}MB")
                    val notif = builder.build()
                    notif.flags = Notification.FLAG_NO_CLEAR;
                    manager.notify(notificationId, notif)


                    // Local Broadcast で発信する
                    val messageIntent = Intent("DownloadEvent")
                    messageIntent.putExtra(
                        "data", ProgressData(
                            percent,
                            format.format(number),
                            format.format(number2)
                        )
                    )
                    messageIntent.putExtra("id", id)
                    LocalBroadcastManager.getInstance(context).sendBroadcast(messageIntent)

                }
            }
            ftp.bufferSize = 2024 * 2048 //To increase the  download speed
            ftp.retrieveFile(serverFilePath, cos)
            output.close()
            ftp.noop() // check that control connection is working OK
            ftp.logout()
            true
        } catch (e: FTPConnectionClosedException) {
            Log.d("FTP_DOWNLOAD", "ERROR FTPConnectionClosedException:$e")
            throw e
        } catch (e: IOException) {
            Log.d("FTP_DOWNLOAD", "ERROR IOException:$e")
            throw e
        } catch (e: Exception) {
            Log.d("FTP_DOWNLOAD", "ERROR Exception:$e")
            throw e
        } finally {
            if (ftp.isConnected) {
                try {
                    ftp.disconnect()
                } catch (f: IOException) {
                    throw f
                }
            }
        }
    }


    @Throws(Exception::class)
    private fun getFileSize(ftp: FTPClient, filePath: String): Long {
        var fileSize: Long = 0
        val files = ftp.listFiles(filePath)
        if (files.size == 1 && files[0].isFile) {
            fileSize = files[0].size
        }
        Log.d("FTP_DOWNLOAD", "File size = $fileSize")
        return fileSize
    }
}
