package mint.woman.female.workout.activity.ui.main

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import mint.woman.female.workout.activity.ui.settings.UserUpdateCallback
import mint.woman.female.workout.databinding.HealthConnectUpdateFragmentBinding

class HealthConnectUpdateDialog : DialogFragment() {
    var binding: HealthConnectUpdateFragmentBinding? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = HealthConnectUpdateFragmentBinding.inflate(
            layoutInflater
        )
        return binding!!.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding!!.btnCancel.setOnClickListener { view1: View? ->
            dismiss()
        }
        binding!!.btnUpdate.setOnClickListener { view1: View? ->
            openHealthConnectAppManager()
            dismiss()
        }
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

}
