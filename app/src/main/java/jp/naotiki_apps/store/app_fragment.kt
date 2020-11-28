package jp.naotiki_apps.store

import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import android.widget.LinearLayout.LayoutParams
import androidx.appcompat.view.ContextThemeWrapper
import androidx.cardview.widget.CardView

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [app_fragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class app_fragment : Fragment() {
lateinit var mainActivity:MainActivity
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_app, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity=(activity as MainActivity)
        mainActivity.toolBarMode=ToolBarMode.Normal;
        mainActivity.invalidateOptionsMenu();
/*val app1page= view.findViewById<Button>(R.id.app1)
        app1page.setOnClickListener {
            val ft = parentFragmentManager.beginTransaction()
            ft.replace(R.id.frame_contents, ApppageFragment.newInstance("tps"))
            ft.addToBackStack(null)
            ft.commit()
        }
        val installBtn1=view.findViewById<Button>(R.id.install_btn1)
        installBtn1.setOnClickListener {
            val ft = parentFragmentManager.beginTransaction()
            ft.replace(R.id.frame_contents, ApppageFragment.newInstance("tps",true))
            ft.addToBackStack(null)
            ft.commit()
        }
        val siritori =view.findViewById<Button>(R.id.button4)
        siritori.setOnClickListener {
            val uri = Uri.parse("https://unity709.github.io/Wiki_siritori/");
            val i = Intent(Intent.ACTION_VIEW,uri)
            startActivity(i)
        }*/
        val appsData=mainActivity.appsData
val appList =view.findViewById<LinearLayout>(R.id.appList)
        for (i in appsData.Apps.indices){//forじゃないとcontinue使えないZE☆
            val app=appsData.Apps[i]

            if (app.id=="store") continue//これ使いたかった

           val card= CardView(context!!).apply {
                radius=getDP(10f,context)
                cardElevation=getDP(50f,context)
                appList.addView(this)
                val layout=layoutParams as LayoutParams
                layout.apply {
                    leftMargin=getDP(10,context)
                    rightMargin=getDP(10,context)
                    topMargin=getDP(5,context)
                    width= LayoutParams.MATCH_PARENT
                    height= LayoutParams.MATCH_PARENT
                }
                layoutParams=layout
            };
           val linearLayout= LinearLayout(context).apply {
               orientation=LinearLayout.VERTICAL
                card.addView(this)
                val layout=layoutParams as FrameLayout.LayoutParams
                layout.apply {

                    width= LayoutParams.MATCH_PARENT
                    height= LayoutParams.MATCH_PARENT
                }
                layoutParams=layout
            };
            ImageView(context).apply {
                GetHTTP.GetImage(context,app.imageUrls.iconUrl,this)
                linearLayout.addView(this)
                val layout=layoutParams as LayoutParams
                layout.apply {

                    width= LayoutParams.MATCH_PARENT
                    height= getDP(300,context)
                }
                layoutParams=layout
            };
            TextView(context).apply {
                text=app.name
                textSize=36f
                setTextColor(resources.getColor(R.color.colorText,null))
                textAlignment=TextView.TEXT_ALIGNMENT_TEXT_START
                typeface = Typeface.DEFAULT_BOLD
                setAutoSizeTextTypeWithDefaults(TextView.AUTO_SIZE_TEXT_TYPE_UNIFORM)
                setLines(1)
                linearLayout.addView(this)
                val layout=layoutParams as LayoutParams
                layout.apply {
                    width= LayoutParams.MATCH_PARENT
                    height= LayoutParams.WRAP_CONTENT
                    setPadding(getDP(10,context),getDP(10,context),0,getDP(10,context))
                }
                layoutParams=layout
            };

            ProgressBar(ContextThemeWrapper(activity, android.R.style.Widget_DeviceDefault_ProgressBar_Horizontal ), null, 0).apply {
                id=R.id.ProgressBar
                isIndeterminate=false
                max=100
                progress=50

                linearLayout.addView(this)
                val layout=layoutParams as LayoutParams
                layout.apply {
                    width= LayoutParams.MATCH_PARENT
                    height= LayoutParams.MATCH_PARENT
                    weight=1F
                }
                layoutParams=layout
            }

            TextView(context).apply {
                id=R.id.ProgressText
                textSize=16f
                setTextColor(resources.getColor(R.color.colorText,null))
                textAlignment=TextView.TEXT_ALIGNMENT_TEXT_END
                                linearLayout.addView(this)
                val layout=layoutParams as LayoutParams
                layout.apply {
                    width= LayoutParams.MATCH_PARENT
                    height= LayoutParams.WRAP_CONTENT
                    setPadding(0,0, getDP(10,context),0)
                }
                layoutParams=layout
            }

            val linearLayout2= LinearLayout(context).apply {
                orientation=LinearLayout.HORIZONTAL
                linearLayout.addView(this)
                val layout=layoutParams as LayoutParams
                layout.apply {

                    width= LayoutParams.MATCH_PARENT
                    height= LayoutParams.MATCH_PARENT
                }
                layoutParams=layout
            };
            Button(context).apply {
                text="詳細"
                background=resources.getDrawable(R.drawable.button_rounded_normal,null)
                linearLayout2.addView(this)
                setTextColor(resources.getColor(R.color.colorText,null))
                val layout=layoutParams as LayoutParams
                layout.apply {

                    width= LayoutParams.WRAP_CONTENT
                    height= LayoutParams.MATCH_PARENT
                    weight= 1f
                }
                layoutParams=layout

            }
            Button(context).apply {
                text="インストール"
                setTextColor(resources.getColor(R.color.colorText,null))
                typeface = Typeface.DEFAULT_BOLD
                background=resources.getDrawable(R.drawable.button_rounded_green,null)
                linearLayout2.addView(this)
                val layout=layoutParams as LayoutParams
                layout.apply {

                    width= LayoutParams.WRAP_CONTENT
                    height= LayoutParams.MATCH_PARENT
                    weight= 1f
                }
                layoutParams=layout
                setOnClickListener {
                    mainActivity.AppInstallDialog(app)
                }
            }

/*

 TextView(context).apply {


                appList.addView(this)
                val layout=layoutParams as LayoutParams
                layout.apply {
                    leftMargin=getDP(10,context)
                    rightMargin=getDP(10,context)
                    topMargin=getDP(5,context)
                    width= LayoutParams.MATCH_PARENT
                    height= LayoutParams.MATCH_PARENT
                }
                layoutParams=layout
            };
            */
        }

    }
    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment app_fragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance() =
            app_fragment()
    }
}
