package mint.woman.female.workout.activity.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import mint.woman.female.workout.R
import mint.woman.female.workout.activity.ui.settings.UserUpdateCallback
import mint.woman.female.workout.databinding.HealthDataDialogBinding
import mint.woman.female.workout.utils.MyPreference

class HealthDataDialog : BottomSheetDialogFragment() {
    lateinit var binding: HealthDataDialogBinding
    var callback: UserSyncCallBack? = null
    lateinit var myPreference:MyPreference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.BottomSheetDialog)
        myPreference = MyPreference(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = HealthDataDialogBinding.inflate(
            layoutInflater
        )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding.imClose.setOnClickListener { view1: View? ->
            dismiss()
        }
        binding.swSyncHealthConnect.isChecked = myPreference.isSyncGoogleFit
        binding.swSyncHealthConnect.setOnCheckedChangeListener { compoundButton: CompoundButton?, b: Boolean ->
            if (b) {
                binding.swSyncHealthConnect.isChecked = false
                if (callback != null) {
                    callback!!.onSync()
                }
            } else {
                myPreference.isSyncGoogleFit = false
                if (callback != null) {
                    callback!!.disableHealthConnect()
                }

            }
        }
        binding.tvManagerDataAndAccess.setOnClickListener { view1: View? ->
            openHealthConnectAppManager()
        }
    }

    fun updateValue(value: Boolean) {
        Log.d("HAHA", "updateValue$value")
        binding.swSyncHealthConnect.isChecked = value
    }

    fun openHealthConnectAppManager() {
        val healthConnectPackageName = "com.google.android.apps.healthdata"

        val packageManager = context?.packageManager
        val intent = packageManager?.getLaunchIntentForPackage(healthConnectPackageName)

        try {
            if (intent != null) {
                // Health Connect is installed, open the app
                startActivity(intent)
            } else {
                // Health Connect is not installed, redirect to Play Store
                try {
                    val playStoreIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("market://details?id=$healthConnectPackageName")
                    )
                    startActivity(playStoreIntent)
                } catch (e: Exception) {
                    // Fallback to browser if Play Store is unavailable
                    val browserIntent = Intent(
                        Intent.ACTION_VIEW,
                        Uri.parse("https://play.google.com/store/apps/details?id=$healthConnectPackageName")
                    )
                    startActivity(browserIntent)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is UserUpdateCallback) {
            callback = context as UserSyncCallBack
        } else {
            throw RuntimeException(
                context.toString()
                        + " must implement UserUpdateCallback"
            )
        }
    }

    override fun onDetach() {
        super.onDetach()
        callback = null
    }
}
