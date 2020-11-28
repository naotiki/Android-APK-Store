package jp.naotiki_apps.store

import android.os.Parcel
import android.os.Parcelable
import android.os.Parcelable.Creator
import kotlinx.android.parcel.Parcelize
import java.io.File
import kotlinx.serialization.*

@Parcelize
data  class FTPData(
     var ServerAddress: String,
     var UserName: String,
     var Password: String,
     var APKPath: String,
     var Local: File
): Parcelable


@Parcelize
data class ProgressData(
     var persent:Int,
     var MBCount:String,
     var MBTotal:String
): Parcelable
