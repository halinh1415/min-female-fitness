package mint.woman.female.workout.activity.ui.workout

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.bumptech.glide.Glide
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.android.play.core.review.model.ReviewErrorCode
import kotlinx.coroutines.launch
import mint.woman.female.workout.FitnessApplication
import mint.woman.female.workout.R
import mint.woman.female.workout.activity.ui.reminder.ReminderActivity
import mint.woman.female.workout.admodule.MaticAd
import mint.woman.female.workout.admodule.MaticAdCallBack
import mint.woman.female.workout.custom.DialogRating
import mint.woman.female.workout.custom.SelectReminderEveryDayDialog
import mint.woman.female.workout.databinding.ActivityFinishedBinding
import mint.woman.female.workout.model.HistoryItem
import mint.woman.female.workout.model.ProgramObject
import mint.woman.female.workout.utils.LanguageHelper
import mint.woman.female.workout.utils.MyPreference
import java.time.Instant
import java.time.ZoneOffset
import java.util.Locale


class FinishedActivity : AppCompatActivity() {
    lateinit var binding: ActivityFinishedBinding
    var mProgramObject: ProgramObject? = null
    lateinit var mHistoryItem: HistoryItem
    var mCongratulationPlayer: MediaPlayer? = null
    lateinit var myPreference: MyPreference
    lateinit var reviewManager: ReviewManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFinishedBinding.inflate(
            layoutInflater
        )
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
        initView()
        requestNewAdBanner()
    }


    private fun initView() {
        reviewManager = ReviewManagerFactory.create(this);
        myPreference = MyPreference(this)
        binding.swGoogleFit.isChecked = myPreference!!.isSyncGoogleFit
        binding.swGoogleFit.visibility = View.GONE

        val mBundle = intent.extras
        binding.lottieView.setAnimation("success.json")
        binding.lottieView.playAnimation()

        Glide.with(this).load(Uri.parse("file:///android_asset/images/img_done.webp")).into(
            binding.imgFinishedBanner
        )
        // Glide.with(this).load(R.drawable.finished_banner).into(mBanner);
        if (mBundle != null) {
            //mPlanObject = mBundle.getParcelable("PLAN_OBJECT");
            val id = mBundle.getInt("PROGRAM_ID", 0)
            val size = mBundle.getInt("SIZE")
            mProgramObject = FitnessApplication.getApplication().getProgram(id)
            mHistoryItem = mBundle.getParcelable<HistoryItem>("HISTORY") as HistoryItem

            binding.contentFinished.txtReminderSet.text =
                FitnessApplication.getApplication().mDatabase.getReminderSet()
            var kCalories = 0f
            if (mHistoryItem != null) {
                kCalories = mHistoryItem!!.calories.toFloat()
                binding.txtKcalCount.text = String.format("%.0f", kCalories)
                binding.tvCompleted.text = """
                    ${mHistoryItem!!.name}
                    ${getString(R.string.txt_completed)}
                    """.trimIndent()
                binding.txtExerciseCount.text = "" + size

                val time = mHistoryItem!!.realDuration
                val minutes = time / 60
                val seconds = time % 60
                binding.txtTimeCount.text = String.format("%2d:%02d", minutes, seconds)
            }
        }

        if (mCongratulationPlayer != null) mCongratulationPlayer!!.release()
        if (myPreference.isRingOn) {
            mCongratulationPlayer = MediaPlayer.create(this, R.raw.cheer)
            if (mCongratulationPlayer != null) {
                mCongratulationPlayer!!.isLooping = false
                mCongratulationPlayer!!.start()
            }
        }
        val openCount = myPreference.openCount()
        if (!myPreference.isRated && myPreference!!.showRate && openCount % 3 == 1) {
            val dialogPreRating = DialogRating()
            dialogPreRating.show(supportFragmentManager, "rate")
        } else {
            if (openCount >1 && openCount % 10 == 1) {
                requestReview()
            }
        }
        if (!myPreference.isSetReminder) {
            val selectReminderEveryDayDialog = SelectReminderEveryDayDialog()
            selectReminderEveryDayDialog.show(supportFragmentManager, "SelectReminder")
        }
        if (myPreference.isSyncGoogleFit) {
            lifecycleScope.launch {
                insertHealthData()
            }

        }
        binding.contentFinished.btnFinish.setOnClickListener { view: View ->
            this.onClick(
                view
            )
        }
        binding.contentFinished.btnShare.setOnClickListener { view: View ->
            this.onClick(
                view
            )
        }
        binding.contentFinished.txtEditReminder.setOnClickListener { view: View ->
            this.onClick(
                view
            )
        }
        binding.contentFinished.btnFinish.setOnClickListener { view: View ->
            this.onClick(
                view
            )
        }
    }

    private fun requestReview() {
        try{
            reviewManager.requestReviewFlow()
                .addOnCompleteListener { task ->
                    if (task.isSuccessful()) {
                        // get ReviewInfo
                        val reviewInfo: ReviewInfo = task.getResult()
                        // show dialog review
                        reviewManager.launchReviewFlow(this@FinishedActivity, reviewInfo)
                            .addOnCompleteListener { reviewTask ->

                                if (!reviewTask.isSuccessful()) {

                                }
                            }
                    }
                }
        }catch (e:Exception){
            e.printStackTrace();
        }

    }


    suspend fun insertHealthData() {
        Log.d("HAHA", "add fit")
        val availabilityStatus = HealthConnectClient.getSdkStatus(this)

        if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE) {
            Toast.makeText(this, "sdk UNAVAILABLE", Toast.LENGTH_SHORT).show()
            return  // early return as there is no viable integration
        }
        if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
            val providerPackageName = "com.google.android.apps.healthdata"
            // Optionally redirect to package installer to find a provider, for example:
            val uriString =
                "market://details?id=\$providerPackageName&url=healthconnect%3A%2F%2Fonboarding"
            val intent = Intent(Intent.ACTION_VIEW)
            intent.setPackage("com.android.vending")
            intent.setData(Uri.parse(uriString))
            intent.putExtra("overlay", true)
            intent.putExtra("callerId", packageName)
            startActivity(intent)
            return
        }
        val healthConnectClient = HealthConnectClient.getOrCreate(this)


        // Create a WorkoutSessionRecord
        val workoutSession = ExerciseSessionRecord(
            startTime = Instant.ofEpochMilli(mHistoryItem.startTime),
            startZoneOffset = ZoneOffset.UTC,
            endTime = Instant.now(),
            endZoneOffset = ZoneOffset.UTC,
            exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_GYMNASTICS,
            title = mHistoryItem.name,
//                metadata = Metadata(UUID.randomUUID().toString())
        )

        healthConnectClient.insertRecords(listOf(workoutSession))

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

    fun requestNewAdBanner() {
        binding.adView.visibility = View.GONE
        if (myPreference!!.isShowAds && myPreference!!.ok) {
            val adRequest = AdRequest.Builder().build()
            binding.adView.loadAd(adRequest)
            binding.adView.adListener = object : AdListener() {
                override fun onAdLoaded() {
                    super.onAdLoaded()
                    binding.adView.visibility = View.VISIBLE
                }
            }
        }
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                if (MaticAd.getInstance().isAvailableInter) {
                    MaticAd.getInstance().showPriorityInterstitialAd(
                        this@FinishedActivity,
                        object : MaticAdCallBack() {
                            override fun onNextAction() {
                                super.onNextAction()
                                finish()
                            }
                        })
                } else {
                    finish()
                }
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onResume() {
        super.onResume()
    }


    fun onClick(view: View) {
        when (view.id) {
            R.id.btn_share -> try {
                val sharingIntent = Intent(Intent.ACTION_SEND)
                sharingIntent.setType("text/plain")
                val shareBody = "https://play.google.com/store/apps/details?id=$packageName"
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareBody)
                startActivity(Intent.createChooser(sharingIntent, "Share via"))
            } catch (e: Exception) {
                e.printStackTrace()
            }

            R.id.txt_feedback -> sendEmail()
            R.id.txt_edit_reminder -> startActivity(
                Intent(
                    this@FinishedActivity,
                    ReminderActivity::class.java
                )
            )

            R.id.btn_finish -> if (MaticAd.getInstance().isAvailableInter) {
                MaticAd.getInstance()
                    .showPriorityInterstitialAd(this@FinishedActivity, object : MaticAdCallBack() {
                        override fun onNextAction() {
                            super.onNextAction()
                            finish()
                        }
                    })
            } else {
                finish()
            }
        }
    }

    fun sendEmail() {
        val emailIntent = Intent(Intent.ACTION_SENDTO)
        emailIntent.setData(Uri.parse("mailto: minfitfeedback@gmail.com"))
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "[Feedback] Female Workout")
        emailIntent.putExtra(Intent.EXTRA_TEXT, "")
        startActivity(Intent.createChooser(emailIntent, "Send Email"))
    }

    override fun onBackPressed() {
        if (MaticAd.getInstance().isAvailableInter) {
            MaticAd.getInstance()
                .showPriorityInterstitialAd(this@FinishedActivity, object : MaticAdCallBack() {
                    override fun onNextAction() {
                        super.onNextAction()
                        finish()
                    }
                })
        } else {
            super.onBackPressed()
        }
    }

    private fun goAgain() {
        finish()
    }


    //    private void goHome() {
    //        Intent intent = new Intent(this, FitnessActivity.class);
    //        startActivity(intent);
    //        finish();
    //    }
    override fun onDestroy() {
        super.onDestroy()
    }
}
