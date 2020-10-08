package jp.naotiki_apps.store


import android.app.job.JobInfo
import android.app.job.JobScheduler
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat


class user_parameters :PreferenceFragmentCompat(), SharedPreferences.OnSharedPreferenceChangeListener {
   // var listener:SharedPreferences.OnSharedPreferenceChangeListener?=null;
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        // res/xml/preferences.xml ファイルに従って設定画面を構成
        setPreferencesFromResource(R.xml.parameters_seting, rootKey)

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
       /* listener=SharedPreferences.OnSharedPreferenceChangeListener { sharedPreferences, s ->
            MainActivity().conf(sharedPreferences,s,AppCompatActivity().getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler)
        }

*/    val activityPreference=findPreference<Preference>("activity")!!
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,Uri.parse("package:" + BuildConfig.APPLICATION_ID))

        intent.addCategory(Intent.CATEGORY_DEFAULT)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS)
        activityPreference.intent = intent

    }


    override fun onResume() {
        super.onResume()
        preferenceManager.sharedPreferences.registerOnSharedPreferenceChangeListener(this)
    }


    override fun onPause() {
        super.onPause()
        preferenceManager.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this)
    }


    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        Log.i("n_runi","RUNNNNNNNN!!!!!!")
        val notification= sharedPreferences?.getBoolean("notification",true);
        val scheduler = activity?.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler;
        if (notification!!){
            Log.i("job_noti",scheduler.getPendingJob(1).toString())
            Log.i("job_noti",scheduler.allPendingJobs.toString())
            if(scheduler.getPendingJob(1)==null){//ジョブなし
                val componentName = ComponentName(activity!!,UpdateJobService::class.java)
                val jobInfo = JobInfo.Builder(1, componentName)
                    .apply {
                        setBackoffCriteria(10000, JobInfo.BACKOFF_POLICY_LINEAR);
                        setPersisted(true)
                        setPeriodic(5000)
                        setRequiredNetworkType(JobInfo.NETWORK_TYPE_NONE)
                        setRequiresCharging(false)
                    }.build()
                scheduler.schedule(jobInfo)
            }
            Log.i("job_noti",scheduler.getPendingJob(1).toString())
            Log.i("job_noti",scheduler.allPendingJobs.toString())
        }else{
            scheduler.cancel(1);
        }
    }
}


