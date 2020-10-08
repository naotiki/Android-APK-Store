package jp.naotiki_apps.store

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button

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
val app1page= view.findViewById<Button>(R.id.app1)
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
