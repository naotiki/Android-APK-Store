package jp.naotiki_apps.store


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.RotateAnimation
import android.widget.*
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.jama.carouselview.CarouselView
import com.jama.carouselview.enums.IndicatorAnimationType
import com.jama.carouselview.enums.OffsetType
import kotlinx.android.synthetic.main.fragment_apppage.*


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM = "app_id"
private const val ARG_PARAM2 = "update_flag"


class ApppageFragment : Fragment() {
    // TODO: Rename and change types of parameters
    private var appId: Int? = null
    private var updateflag: Boolean? = null
    private val APK_REQ_ID = listOf(111, 112)
    var store = arrayListOf<Any>()// 内部ID アプリ名 説明文 インストール関数
    var tps = arrayListOf<Any>()// 内部ID アプリ名
lateinit var appInfo :AppInfo
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            appId = it.getInt(ARG_PARAM)
            updateflag = it.getBoolean(ARG_PARAM2)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_apppage, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        super.onViewCreated(view, savedInstanceState)
        view.findViewById<LinearLayout>(R.id.progressLayout).visibility = View.GONE
        view.findViewById<LinearLayout>(R.id.details).visibility = View.GONE

        val Card1 = view.findViewById<CardView>(R.id.card1)
        CardInitialize(Card1)


         appInfo = Apps.AppsDataLoad(appId!!)
        val appTitle = view.findViewById<TextView>(R.id.appTitle)
        val versionText = view.findViewById<TextView>(R.id.Versioninfo)
        val updateText = view.findViewById<TextView>(R.id.update_text)
        val appText = view.findViewById<TextView>(R.id.app_info)
        val installBtn = view.findViewById<Button>(R.id.installBtn)
        val swipeUpdate = view.findViewById<SwipeRefreshLayout>(R.id.swipeupdate)


        var newversion = appInfo.version
        GetHTTP.GetImage(
            context!!,
            appInfo.imageUrls.iconUrl,
            view.findViewById(R.id.imageView2)
        )
        swipeUpdate.setOnRefreshListener {
           appInfo= Apps.AppsDataLoad(appId!!)

            newversion = appInfo.version
            versionText.text = "V$newversion"
            updateText.text = appInfo.comment



            installBtn.visibility = View.VISIBLE
           InitFromNet()



            swipeUpdate.isRefreshing = false
        }
        appTitle.text = appInfo.name
        versionText.text = "V$newversion"
        updateText.text = appInfo.comment
        appText.text = appInfo.description
        installBtn.setOnClickListener {
            (activity as MainActivity).AppInstallDialog(appInfo,)

        }
//スクリーンショット設定
        val screenShots = appInfo.imageUrls.screenShotUrls
        val urlArrayList = arrayListOf<String>()
        for (i in screenShots.indices) {
            if (screenShots[i].toString() != "url") {
                urlArrayList.add(screenShots[i])
            }

        }

        val carouselView = view.findViewById<CarouselView>(R.id.carouselView)
        carouselView.apply {

            size = urlArrayList.size
            resource = R.layout.center_carousel_item
            autoPlay = false

            indicatorAnimationType = IndicatorAnimationType.THIN_WORM
            carouselOffset = OffsetType.CENTER
            setCarouselViewListener { view, position ->
                // Example here is setting up a full image carousel
                val imageView = view.findViewById<ImageView>(R.id.imageView)
                imageView.scaleType = ImageView.ScaleType.FIT_CENTER
                GetHTTP.GetImage(context!!, urlArrayList[position], imageView)

            }
            // After you finish setting up, show the CarouselView
            show()
        }



        InitFromNet()

        view.findViewById<TextView>(R.id.weblink).text = appInfo.infoUrl
        (activity as MainActivity).toolBarMode = ToolBarMode.Share
        (activity as MainActivity).shareText = "${appInfo.name}\n${appInfo.infoUrl}"

        (activity as MainActivity).invalidateOptionsMenu()


    }


    /**
     * @param view 初期化するCardView
     * **/

    fun InitFromNet(){
        installBtn.visibility = View.VISIBLE
        val (state,_,_)= GetHTTP.CheckAppState(context!!,appInfo.id)
        when(state){
            AppState.NotInstall->{
                installBtn.text = "インストール"
            }
            AppState.HaveUpdate->{
                installBtn.text = "更新"
            }
            AppState.NotUpdate->{
                installBtn.visibility = View.GONE
            }
        }
    }

    fun CardInitialize(view: CardView) {
        val info = (view.getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout
        val details = (view.getChildAt(0) as LinearLayout).getChildAt(1) as LinearLayout
        val icon = ((view.getChildAt(0) as LinearLayout).getChildAt(0) as LinearLayout).getChildAt(1) as ImageView
        info.setOnClickListener {
            val inAnimation = AnimationUtils.loadAnimation(context, R.anim.in_animation)
            val openAnimation = RotateAnimation(
                0.0f, 180.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            ).apply {
                duration = 500
                fillAfter = true
            }

            val closeAnimation = RotateAnimation(
                180.0f, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.5f,
                Animation.RELATIVE_TO_SELF, 0.5f
            ).apply {
                duration = 500
                fillAfter = true
            }
            if (details.visibility == View.GONE) {//非表示

                icon.startAnimation(openAnimation)
                details.startAnimation(inAnimation)
                details.visibility = View.VISIBLE //表示する
            } else if (details.visibility == View.VISIBLE) {//表示
                //   details.startAnimation(outAnimation);
                icon.startAnimation(closeAnimation)
                details.visibility = View.GONE //非表示にする
            }
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param app_id Application ID.
         * @param update_flag すぐにアップデート確認処理に行くかのフラグ。
         * @return 一つの新しいApppageFragmentフラグメントのインスタンスを返す(直訳)
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(app_id: Int, update_flag: Boolean = false) =
            ApppageFragment().apply {
                arguments = Bundle().apply {
                    putInt(ARG_PARAM, app_id)
                    putBoolean(ARG_PARAM2, update_flag)
                }
            }
    }
}
