package jp.naotiki_apps.store

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import jp.naotiki_apps.store.MyApplication.Companion.database


class HistoryFragment : Fragment() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {

        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_history, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val histories=database.downloadHistoryDao().GetAllHistory()
        histories.forEach{
            //ここにViewの処理
        }
    }
    companion object {


        public fun newInstance() =
            HistoryFragment().apply {
                arguments = Bundle().apply {

                }
            }
    }
}