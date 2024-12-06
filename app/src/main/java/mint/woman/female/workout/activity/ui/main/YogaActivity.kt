package mint.woman.female.workout.activity.ui.main

import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.res.Configuration
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI.setupActionBarWithNavController
import androidx.navigation.ui.NavigationUI.setupWithNavController
import androidx.preference.PreferenceManager
import com.facebook.FacebookSdk.sdkInitialize
import com.facebook.appevents.AppEventsConstants.EVENT_NAME_COMPLETED_REGISTRATION
import com.facebook.appevents.AppEventsLogger
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.AuthUI.IdpConfig.GoogleBuilder
import com.firebase.ui.auth.FirebaseAuthUIActivityResultContract
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.launch
import mint.woman.female.workout.FitnessApplication
import mint.woman.female.workout.R
import mint.woman.female.workout.activity.SplashActivity
import mint.woman.female.workout.activity.premium.PremiumActivity
import mint.woman.female.workout.activity.premium.PremiumViewModel
import mint.woman.female.workout.activity.ui.custom.MyWorkoutActivity
import mint.woman.female.workout.activity.ui.history.HistoryViewModel
import mint.woman.female.workout.activity.ui.home.HomeFragment.OnHomeFragmentInteractionListener
import mint.woman.female.workout.activity.ui.preview.PreviewActivity
import mint.woman.female.workout.activity.ui.settings.SettingFragment
import mint.woman.female.workout.activity.ui.settings.SettingFragment.OnSettingsListener
import mint.woman.female.workout.activity.ui.settings.UserUpdateCallback
import mint.woman.female.workout.activity.ui.workout.ChallengeActivity
import mint.woman.female.workout.custom.DialogRating
import mint.woman.female.workout.databinding.ActivityYogaBinding
import mint.woman.female.workout.model.HistoryItem
import mint.woman.female.workout.model.ProgramObject
import mint.woman.female.workout.receiver.AlarmReceiver
import mint.woman.female.workout.utils.FileUtil
import mint.woman.female.workout.utils.LanguageHelper
import mint.woman.female.workout.utils.MyPreference
import mint.woman.female.workout.utils.RemoteConfigKeys
import java.io.File
import java.util.Arrays
import java.util.Locale


class YogaActivity : AppCompatActivity(), OnHomeFragmentInteractionListener, OnSettingsListener,UserSyncCallBack,
    UserUpdateCallback {
    var binding: ActivityYogaBinding? = null
    var mCurrentCategory: ProgramObject? = null
    lateinit var myPreference: MyPreference
    var flagAds: Int = 0

    var tts: TextToSpeech? = null
    var premiumViewModel: PremiumViewModel? = null
    var historyViewModel: HistoryViewModel? = null

    //   GoogleSignInClient mGoogleSignInClient;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityYogaBinding.inflate(layoutInflater)
        setContentView(binding!!.root)


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration: AppBarConfiguration = AppBarConfiguration.Builder(
            R.id.nav_home,
            R.id.nav_report2,
            R.id.nav_diet_fragment,
            R.id.nav_setting
        ).build()
        val navController = findNavController(this, R.id.nav_host_fragment)
        setupActionBarWithNavController(this, navController, appBarConfiguration)
        setupWithNavController(binding!!.navView, navController)
        initView()
        val file = File(filesDir, "music")
        if (!file.exists()) {
            file.mkdirs()
            FileUtil.copyAssetToInternalStorage(
                this@YogaActivity,
                "music/Default.mp3",
                "music/Default.mp3"
            )
        }
        sdkInitialize(applicationContext)
        AppEventsLogger.activateApp(FitnessApplication.getApplication())
        val logger = AppEventsLogger.newLogger(this)
        //        logger.logEvent("App Launched");
        logger.logEvent(EVENT_NAME_COMPLETED_REGISTRATION)
    }

    private fun initView() {
        premiumViewModel = ViewModelProvider(this).get(
            PremiumViewModel::class.java
        )
        historyViewModel = ViewModelProvider(this).get(
            HistoryViewModel::class.java
        )
        flagAds = 0
        myPreference = MyPreference.newInstance(this)
        myPreference.increaseOpenCount()
        if (myPreference.getPlanProgress(20) > 0) {
            myPreference.setPlanProgress(879, myPreference.getPlanProgress(20))
            myPreference.setPlanProgress(20, 0)
        }
        if (myPreference.getPlanProgress(21) > 0) {
            myPreference.setPlanProgress(881, myPreference.getPlanProgress(21))
            myPreference.setPlanProgress(21, 0)
        }
        if (myPreference.getPlanProgress(22) > 0) {
            myPreference.setPlanProgress(880, myPreference.getPlanProgress(22))
            myPreference.setPlanProgress(22, 0)
        }
        if (myPreference.getPlanProgress(23) > 0) {
            myPreference.setPlanProgress(882, myPreference.getPlanProgress(23))
            myPreference.setPlanProgress(23, 0)
        }

        premiumViewModel!!.isPremium.observe(
            this
        ) { isPremium: Boolean? ->
            myPreference.setPremiumMember(
                isPremium!!
            )
        }
        if (isFirst) {
            isFirst = false
            val openCount = myPreference.openCount()
            if (openCount > 2 && openCount % 5 == 0 && !myPreference.isPremiumMember()) {
                startActivity(Intent(this@YogaActivity, PremiumActivity::class.java))
            }
        }
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null && FirebaseRemoteConfig.getInstance()
                .getBoolean(RemoteConfigKeys.ENABLE_SYNC)
        ) {
            retrieveUserProgress(user.uid)
        }

        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(
                    this@YogaActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@YogaActivity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
                return
            }
            if (ContextCompat.checkSelfPermission(
                    this@YogaActivity,
                    Manifest.permission.SCHEDULE_EXACT_ALARM
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@YogaActivity,
                    arrayOf(Manifest.permission.SCHEDULE_EXACT_ALARM),
                    111
                )
                return
            }
        }
        if (myPreference.isRemind()) {
            createDailyNotify()
        }


        //checkAccount();
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 101) {
            if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                AlarmReceiver.createNotificationChannels(this@YogaActivity)
                AlarmReceiver.scheduleLockscreenWidget(
                    this@YogaActivity,
                    myPreference.timeRemindDaily
                )
            }
        }
    }

    fun createDailyNotify() {
        if (Build.VERSION.SDK_INT >= 33) {
            if (ContextCompat.checkSelfPermission(
                    this@YogaActivity,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this@YogaActivity,
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    101
                )
                return
            }
        }
        myPreference.isRemind = false
        AlarmReceiver.createNotificationChannels(this@YogaActivity)
        AlarmReceiver.scheduleLockscreenWidget(this@YogaActivity, myPreference.timeRemindDaily)
    }


    override fun attachBaseContext(newBase: Context) {
        val preferences = PreferenceManager.getDefaultSharedPreferences(newBase)
        var lang = preferences.getString("ST_LANGUAGE", "")!!
        lang = if (!lang.isEmpty()) {
            lang.substring(0, 2)
        } else {
            Locale.getDefault().language
        }
        super.attachBaseContext(LanguageHelper.setLanguage(newBase, lang))
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        if (overrideConfiguration != null) {
            val uiMode = overrideConfiguration.uiMode
            overrideConfiguration.setTo(baseContext.resources.configuration)
            overrideConfiguration.uiMode = uiMode
        }
        super.applyOverrideConfiguration(overrideConfiguration)
    }


    override fun onBackPressed() {
        if (myPreference.openCount() >= 2 && !myPreference.isRated && myPreference.showRate) {
            myPreference.showRate = false
            val mDialogRate = DialogRating()
            mDialogRate.show(supportFragmentManager, "Rate")
            return
        }
        if (myPreference.openCount() == 20 && myPreference.showRate) {
            myPreference.showRate = false
            val mDialogRate = DialogRating()
            mDialogRate.show(supportFragmentManager, "Rate")
            return
        }

        super.onBackPressed()
    }

    override fun onPause() {
        super.onPause()
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
    }

    override fun onLanguageChange() {
        val intent = Intent(this, SplashActivity::class.java)
        intent.addFlags((Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        startActivity(intent)
    }
    private val PERMISSIONS = setOf(
        HealthPermission.getWritePermission(ExerciseSessionRecord::class)
    )

    // Permission launcher

    val requestPermissionActivityContract =
        PermissionController.createRequestPermissionResultContract()

    val requestPermissions = registerForActivityResult(requestPermissionActivityContract) { granted ->
        if (granted.containsAll(PERMISSIONS)) {
            Toast.makeText(
                this,
                "granted", Toast.LENGTH_SHORT
            ).show()
            myPreference.isSyncGoogleFit = true
            accessGoogleFit()


        } else {
            myPreference.isSyncGoogleFit = false
            try {
                val navHostFragment =
                    supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
                val settingFragment =
                    navHostFragment!!.childFragmentManager.primaryNavigationFragment as SettingFragment?
                settingFragment?.initValue(false)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    suspend fun checkPermissionsAndRun(healthConnectClient: HealthConnectClient) {
        try {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            if (granted.containsAll(PERMISSIONS)) {
                myPreference.isSyncGoogleFit = true
                accessGoogleFit()
                // Proceed with inserting or reading data
            } else {
                requestPermissions.launch(PERMISSIONS)
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }
    // Permission launcher
    override fun onGetGoogleFit() {
        try{
            val availabilityStatus = HealthConnectClient.getSdkStatus(this)
            myPreference = MyPreference.newInstance(this)
            if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE) {
                Toast.makeText(
                    this@YogaActivity,
                    getString(R.string.oops_sync_failed),
                    Toast.LENGTH_SHORT
                ).show()
                return  // early return as there is no viable integration
            }
            if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
                Toast.makeText(
                    this@YogaActivity,
                    getString(R.string.update_health_connect_gpt),
                    Toast.LENGTH_SHORT
                ).show()
                val providerPackageName = "com.google.android.apps.healthdata"
                // Optionally redirect to package installer to find a provider, for example:
                val uriString =
                    "market://details?id=$providerPackageName&url=healthconnect%3A%2F%2Fonboarding"
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setPackage("com.android.vending")
                intent.setData(Uri.parse(uriString))
                intent.putExtra("overlay", true)
                intent.putExtra("callerId", packageName)
                startActivity(intent)
                return
            }
            val healthConnectClient = HealthConnectClient.getOrCreate(this)
            lifecycleScope.launch {
                checkPermissionsAndRun(healthConnectClient)
            }
        }catch (e:Exception){
            e.printStackTrace()
        }


    }


    private val signInLauncher = registerForActivityResult(
        FirebaseAuthUIActivityResultContract()
    ) { result: FirebaseAuthUIAuthenticationResult -> onSignInResult(result) }

    private fun onSignInResult(result: FirebaseAuthUIAuthenticationResult) {
        val response = result.idpResponse
        if (result.resultCode == RESULT_OK) {
            // Successfully signed in
            val user = FirebaseAuth.getInstance().currentUser
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
            val settingFragment =
                navHostFragment!!.childFragmentManager.primaryNavigationFragment as SettingFragment?
            if (settingFragment != null) {
                settingFragment.updateAccount(user)
                retrieveUserProgress(user!!.uid)
            }
        } else {
            Toast.makeText(
                this@YogaActivity,
                getString(R.string.oops_sync_failed),
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    fun retrieveUserProgress(userId: String) {
        // Get a reference to the database
        val database = FirebaseDatabase.getInstance()
        val myRef = database.getReference("users").child(userId).child("programs")
        val userDiet = database.getReference("users").child(userId).child("diet")
        val userHistory = database.getReference("users").child(userId).child("history")

        // Attach a listener to read the data at our reference
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for (programSnapshot in dataSnapshot.children) {
                    val progress = programSnapshot.getValue(Int::class.java)
                    val programId = programSnapshot.key!!.toInt()
                    if (progress != null) {
                        myPreference.setPlanProgress(programId, progress)
                    }
                }

                // Do something with the retrieved progress list, e.g., update UI
            }

            override fun onCancelled(databaseError: DatabaseError) {
                // Getting UserProgress failed, log a message
                Log.w("HAHA", "loadUserProgress:onCancelled", databaseError.toException())
            }
        })
        userDiet.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (dietSnapshot in snapshot.children) {
                    val isDone = dietSnapshot.getValue(Boolean::class.java)
                    if (isDone != null) {
                        val day = dietSnapshot.key!!.toInt()
                        myPreference.setDietDone(day, isDone)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })

        userHistory.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (hisDataSnapshot in snapshot.children) {
                    val historyItem = hisDataSnapshot.getValue(
                        HistoryItem::class.java
                    )
                    if (historyItem != null) {
                        historyViewModel!!.insertHistory(historyItem)
                        Log.d("HAHA", "retry " + historyItem.name)
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {
            }
        })
    }


    override fun onSigOut() {
        FirebaseAuth.getInstance().signOut()
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
        val settingFragment =
            navHostFragment!!.childFragmentManager.primaryNavigationFragment as SettingFragment?
        settingFragment!!.updateAccount(null)
    }

    override fun onContinueSync() {
        val providers = Arrays.asList(GoogleBuilder().build())
        // Create and launch sign-in intent
        val signInIntent =
            AuthUI.getInstance().createSignInIntentBuilder().setAvailableProviders(providers)
                .build()
        signInLauncher.launch(signInIntent)
    }

    override fun syncData() {
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null && FirebaseRemoteConfig.getInstance()
                .getBoolean(RemoteConfigKeys.ENABLE_SYNC)
        ) {
            val userId = user.uid
            val database = FirebaseDatabase.getInstance()
            val userRef = database.getReference("users").child(userId).child("programs")
            val userDiet = database.getReference("users").child(userId).child("diet")
            val preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(this)
            val allEntries = preferences.all
            for ((key, value) in allEntries) {
                if (key.startsWith("PLAN_PROGRESS_")) {
                    val programId = key.replace("PLAN_PROGRESS_", "").toInt()
                    val progress = value as Int
                    if (programId == 1 || programId == 879 || programId == 880 || programId == 881 || programId == 882 || programId == 1178) {
                        userRef.child(programId.toString()).setValue(progress)
                            .addOnSuccessListener { aVoid: Void? ->
                                Log.d("HAHA", "write progress success!")
                            }.addOnFailureListener { e: Exception ->
                                Log.d(
                                    "HAHA",
                                    "write progress fail!$e"
                                )
                            }
                    }
                }
                if (key.startsWith("IS_DONE_DIET")) {
                    val day = key.replace("IS_DONE_DIET", "").toInt()
                    val isDone = value as Boolean
                    userDiet.child(day.toString()).setValue(isDone)
                        .addOnSuccessListener { aVoid: Void? ->
                            Log.d("HAHA", "write diet progress success!")
                        }.addOnFailureListener { e: Exception ->
                            Log.d(
                                "HAHA",
                                "write diet progress fail!$e"
                            )
                        }
                }
            }

            val userHistory = database.getReference("users").child(userId).child("history")
            historyViewModel!!.allHistory.observe(
                this
            ) { historyItems: List<HistoryItem> ->
                for (history in historyItems) {
                    userHistory.child(history.id.toString())
                        .addValueEventListener(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                if (!snapshot.exists()) {
                                    userHistory.child(history.id.toString()).setValue(history)
                                        .addOnSuccessListener { aVoid: Void? ->
                                            Log.d("HAHA", "write history success!")
                                        }.addOnFailureListener { e: Exception ->
                                            Log.d(
                                                "HAHA",
                                                "write history fail!$e"
                                            )
                                        }
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }
                        })
                }
            }
        }
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_ACCESS_FITNESS) {
            if (resultCode == RESULT_OK) {
                accessGoogleFit()
            } else {
                myPreference.isSyncGoogleFit = false
                try {
                    val navHostFragment =
                        supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
                    val settingFragment =
                        navHostFragment!!.childFragmentManager.primaryNavigationFragment as SettingFragment?
                    settingFragment?.initValue(false)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    private fun accessGoogleFit() {
        Toast.makeText(this, getString(R.string.tip_connect_hc_success), Toast.LENGTH_SHORT).show()
        myPreference.isSyncGoogleFit = true
        try {
            val navHostFragment =
                supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment?
            val settingFragment =
                navHostFragment!!.childFragmentManager.primaryNavigationFragment as SettingFragment?
            val dialogSync = supportFragmentManager.findFragmentByTag("health_connect") as HealthDataDialog?
            dialogSync?.updateValue(true)
            settingFragment?.initValue(true)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onResume() {
        super.onResume()
        if (flagAds == 1) {
            flagAds = 0
            gotoPlan()
        }
    }

    override fun onGotoPlan(programObject: ProgramObject) {
        mCurrentCategory = programObject
        //myDB.setCurrentId(ca);
        gotoPlan()
    }

    fun gotoPlan() {
        flagAds = 2
        if (mCurrentCategory == null) return

        if (mCurrentCategory!!.type == 1) {
            val intent = Intent(this@YogaActivity, ChallengeActivity::class.java)
            val bundle = Bundle()
            //bundle.putParcelable("PLAN", mCurrentCategory);
            bundle.putInt("PROGRAM_ID", mCurrentCategory!!.id)
            intent.putExtras(bundle)
            startActivity(intent)
        } else if (mCurrentCategory!!.type == 2) {
            val intent2 = Intent(this@YogaActivity, PreviewActivity::class.java)
            val bundle = Bundle()
            //bundle.putParcelable("PLAN", mCurrentCategory);
            bundle.putInt("PROGRAM_ID", mCurrentCategory!!.id)
            bundle.putInt("DAY_NUMBER", 0)
            intent2.putExtras(bundle)
            startActivity(intent2)
        }
    }



    private fun showUpdateApp() {
        val restart = AlertDialog.Builder(this)
        restart.setTitle("Update New Version!")
        restart.setNegativeButton(getString(android.R.string.cancel), null)
        restart.setPositiveButton(
            getString(android.R.string.ok)
        ) { dialogInterface, i -> gotoRate() }
        restart.show()
    }

    fun gotoRate() {
        myPreference.setRated()
        val uri = Uri.parse("market://details?id=$packageName")
        val goToMarket = Intent(Intent.ACTION_VIEW, uri)
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY or Intent.FLAG_ACTIVITY_MULTIPLE_TASK)
        try {
            startActivity(goToMarket)
        } catch (e: ActivityNotFoundException) {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=$packageName")
                )
            )
        }
    }

    override fun onRateShow() {
        val dialogPreRating = DialogRating()
        dialogPreRating.show(supportFragmentManager, "rate")
    }

    override fun onDestroy() {
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }


        super.onDestroy()
    }

    override fun onTestVoice(locale: String) {
        tts = TextToSpeech(this) { i: Int ->
            if (i == TextToSpeech.SUCCESS) {
                tts!!.setLanguage(Locale(locale))
                tts!!.speak("This is test voice!", TextToSpeech.QUEUE_ADD, null)
            }
        }
    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.fitness, menu)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }

            R.id.action_my_workout -> {
                startActivity(Intent(this@YogaActivity, MyWorkoutActivity::class.java))
                return true
            }

            R.id.action_premium -> {
                startActivity(Intent(this@YogaActivity, PremiumActivity::class.java))
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        private const val TAG = "YogaActivity"
        var isFirst: Boolean = true
        const val RC_SIGN_IN: Int = 1201
        const val RC_ACCESS_FITNESS: Int = 1211
    }


    override fun onSync() {
        try{
            val availabilityStatus = HealthConnectClient.getSdkStatus(this)
            myPreference = MyPreference.newInstance(this)
            if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE) {
                Toast.makeText(
                    this@YogaActivity,
                    getString(R.string.oops_sync_failed),
                    Toast.LENGTH_SHORT
                ).show()

                return  // early return as there is no viable integration
            }
            if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
                Toast.makeText(
                    this@YogaActivity,
                    getString(R.string.update_health_connect_gpt),
                    Toast.LENGTH_SHORT
                ).show()
                val providerPackageName = "com.google.android.apps.healthdata"
                // Optionally redirect to package installer to find a provider, for example:
                val uriString =
                    "market://details?id=$providerPackageName&url=healthconnect%3A%2F%2Fonboarding"
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setPackage("com.android.vending")
                intent.setData(Uri.parse(uriString))
                intent.putExtra("overlay", true)
                intent.putExtra("callerId", packageName)
                startActivity(intent)
                return
            }
            val healthConnectClient = HealthConnectClient.getOrCreate(this)
            lifecycleScope.launch {
                checkPermissionsAndRun(healthConnectClient)
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
    }

    override fun disableHealthConnect() {
        try{
            val availabilityStatus = HealthConnectClient.getSdkStatus(this)
            myPreference = MyPreference.newInstance(this)
            if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE) {
                Toast.makeText(
                    this@YogaActivity,
                    getString(R.string.oops_sync_failed),
                    Toast.LENGTH_SHORT
                ).show()

                return  // early return as there is no viable integration
            }

            val healthConnectClient = HealthConnectClient.getOrCreate(this)
            lifecycleScope.launch {
                healthConnectClient.permissionController.revokeAllPermissions()
            }

        }catch (e:Exception){
            e.printStackTrace()
        }

    }
}