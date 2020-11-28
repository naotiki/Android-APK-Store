package jp.naotiki_apps.store

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlin.reflect.typeOf

class Apps{
    companion object {
        var appKeysList=ArrayList<String>()
        lateinit var apps:AppsData
        fun AppsDataLoad(){//TODO 参照渡しでAPPINfo初期化
            appKeysList.clear()
            apps = GetHTTP.GetAppsData()
            repeat(apps.Apps.size) {
                appKeysList.add(apps.Apps[it].id)
            }

        }
        fun AppsDataLoad(appID: Int):AppInfo{//TODO 参照渡しでAPPINfo初期化
            appKeysList.clear()
            apps = GetHTTP.GetAppsData()
            repeat(apps.Apps.size) {
                appKeysList.add(apps.Apps[it].id)
            }
            return apps.Apps[appID]
        }

        fun GetAppInfoFromAppKey(appKey:String):AppInfo {
            return apps.Apps[appKeysList.indexOf(appKey)]
        }
        fun GetAppIDFromAppKey(appKey: String):Int{
           return appKeysList.indexOf(appKey)
        }
    }


}


@Serializable
data class AppsData(
    @SerialName("apps")
    var Apps:List<AppInfo>
)

@Serializable
public data class AppInfo(
    @SerialName("id") var id:String,
    @SerialName("name") var name:String,
    @SerialName("version") var version:String,
    @SerialName("package") var packageName: String,
    @SerialName("pass") var apkUrl: String,
    @SerialName("description") var description:String,
    @SerialName("comment") var comment:String,
    @SerialName("imageurl") var imageUrls:Image,
    @SerialName("infourl") var infoUrl:String
){
    fun VersionInt():Int{
        return version.replace(".","").toInt()
    }
}

@Serializable
public data class Image(
    @SerialName("icon")var iconUrl:String,
    @SerialName("screenshot") var screenShotUrls:List<String>
)