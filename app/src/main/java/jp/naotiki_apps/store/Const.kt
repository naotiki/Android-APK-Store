package jp.naotiki_apps.store


val APK_REQ_ID = listOf(111, 112)// Store TPS
const val STORE=111
const val TPS=112
const val URL ="http://nuserver.starfree.jp/"
const val RES_URL= URL+"response.php"
 class  myAppname{
    fun FullName(appKey:String):String = when(appKey){
        "store"->{
            "Store"
        }
        "tps"->{
            "TPS"
        }
        else->{
            ""
        }
    }
}
