package jp.naotiki_apps.store

import android.app.Activity
import android.content.Context
import android.widget.ProgressBar
import android.widget.TextView
import java.math.BigDecimal

//　プログレスUIのID はわざと重複させる

    fun UpdateProgress(downloadedByte: Double, totalByte: Double,activity: Activity) {

        val percent = (downloadedByte / totalByte * 100).toInt()
        val downloadedMB = BigDecimal(Math.round(downloadedByte * 100.0) / 100.0)
        val totalMB =BigDecimal(Math.round(totalByte * 100.0) / 100.0)
        val progressBar= activity.findViewById<ProgressBar>(R.id.ProgressBar)
        val textView= activity.findViewById<TextView>(R.id.ProgressText)
        if (progressBar != null&&textView!=null) {
            progressBar.progress=percent
            textView.text="${percent}% ${downloadedMB}MB / ${totalMB}MB"
        }
    }
