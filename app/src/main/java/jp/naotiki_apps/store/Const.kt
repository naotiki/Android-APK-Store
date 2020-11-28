package jp.naotiki_apps.store

import android.content.Context
import android.util.TypedValue


val APK_REQ_ID = (111..199).toList()// Store TPS
const val STORE=111
const val TPS=112
const val STORE_INDEX=0
const val URL ="http://server.naotiki-apps.xyz/"
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
fun getDP(f:Float,context:Context):Float{
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, f, context.resources.displayMetrics)
}
fun getDP(i:Int,context:Context):Int{
    return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, i.toFloat(), context.resources.displayMetrics).toInt()
}


