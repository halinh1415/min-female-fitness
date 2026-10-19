package mint.woman.female.workout.activity.ui.workout

import mint.woman.female.workout.utils.DifficultyStore
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.play.core.review.ReviewInfo
import com.google.android.play.core.review.ReviewManager
import com.google.android.play.core.review.ReviewManagerFactory
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import mint.woman.female.workout.FitnessApplication
import mint.woman.female.workout.R
import mint.woman.female.workout.activity.ui.badge.BadgeActivity
import mint.woman.female.workout.activity.ui.badge.BadgeUnlockedDialog
import mint.woman.female.workout.activity.ui.compose.MinTheme
import mint.woman.female.workout.activity.ui.history.HistoryViewModel
import mint.woman.female.workout.activity.ui.reminder.ReminderActivity
import mint.woman.female.workout.activity.ui.workout.compose.FinishedActions
import mint.woman.female.workout.activity.ui.workout.compose.FinishedScreen
import mint.woman.female.workout.activity.ui.workout.compose.FinishedState
import mint.woman.female.workout.admodule.MaticAd
import mint.woman.female.workout.admodule.MaticAdCallBack
import mint.woman.female.workout.badge.BadgeRepository
import mint.woman.female.workout.custom.DialogRating
import mint.woman.female.workout.custom.DialogWeight
import mint.woman.female.workout.custom.SelectReminderEveryDayDialog
import mint.woman.female.workout.model.HistoryItem
import mint.woman.female.workout.model.ProgramObject
import mint.woman.female.workout.notification.NotificationPermissionDialog
import mint.woman.female.workout.notification.PracticeStatsStore
import mint.woman.female.workout.activity.ui.preview.PreviewActivity
import mint.woman.female.workout.activity.ui.workout.compose.FinishedNext
import mint.woman.female.workout.activity.ui.workout.compose.FinishedWeekDay
import mint.woman.female.workout.utils.DateIdUtils
import mint.woman.female.workout.utils.EdgeToEdgeHelper
import mint.woman.female.workout.utils.ProgramRecommenderV3
import java.text.SimpleDateFormat
import java.util.Calendar
import mint.woman.female.workout.utils.HistoryDatabase
import mint.woman.female.workout.utils.LanguageHelper
import mint.woman.female.workout.utils.MyPreference
import mint.woman.female.workout.utils.StreakCalculator
import mint.woman.female.workout.utils.StreakMessageType
import java.time.Instant
import java.time.ZoneOffset
import java.util.Locale

/**
 * Màn kết thúc buổi tập, giao diện Compose ([FinishedScreen]) giống app WomenWorkout:
 * hero ăn mừng, 3 chỉ số, streak, đánh giá cảm nhận, cân nặng/BMI, giờ nhắc, Chia sẻ / Xong.
 * Logic cũ giữ nguyên: âm thanh chúc mừng, Health Connect, dialog đánh giá/nhắc tập, huy hiệu, quảng cáo.
 */
class FinishedActivity : AppCompatActivity(), EdgeToEdgeHelper.HandlesInsets {
    var mProgramObject: ProgramObject? = null
    private val basePercent by lazy { DifficultyStore.percent(this, mProgramObject?.id ?: 0) }
    lateinit var mHistoryItem: HistoryItem
    var mCongratulationPlayer: MediaPlayer? = null
    lateinit var myPreference: MyPreference
    lateinit var reviewManager: ReviewManager
    private lateinit var historyViewModel: HistoryViewModel
    private var state by mutableStateOf(FinishedState())
    private var adView: AdView? = null

    /** Chỉ ăn mừng một lần cho một lần mở màn hình, dù có nhiều huy hiệu cùng mở khóa. */
    private var badgeCelebrationShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        // Hero là nền hồng đậm: icon status bar màu trắng.
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        super.onCreate(savedInstanceState)
        historyViewModel = ViewModelProvider(this)[HistoryViewModel::class.java]
        myPreference = MyPreference(this)
        setContent {
            MinTheme {
                FinishedScreen(
                    state = state,
                    actions = FinishedActions(
                        onFeel = ::onFeel,
                        onUpdateWeight = { DialogWeight().show(supportFragmentManager, "enter_weight") },
                        onEditReminder = { startActivity(Intent(this, ReminderActivity::class.java)) },
                        onShare = ::share,
                        onDone = ::closeWithAd,
                        onClose = ::closeWithAd,
                        onNext = ::startNext,
                    ),
                    banner = { AdBanner() },
                )
            }
        }
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() = closeWithAd()
        })
        // Cân nặng vừa cập nhật qua DialogWeight -> làm mới thẻ cân nặng.
        historyViewModel.weightChanged.observe(this) { refreshWeight() }
        initView()
        observeBadgeUnlocks()
        showStreak()
        showWeekAndNext()
    }

    /** Thẻ "Tuần này" và "Buổi tiếp theo" (đọc lịch sử / chấm điểm bài ở luồng nền). */
    private fun showWeekAndNext() {
        lifecycleScope.launch {
            val (week, next) = withContext(Dispatchers.IO) {
                runCatching { loadWeek() }.getOrDefault(emptyList()) to runCatching { loadNext() }.getOrNull()
            }
            state = state.copy(week = week, weekGoal = myPreference.goalOfWeek.coerceAtLeast(1), next = next)
            val record = withContext(Dispatchers.IO) { runCatching { isLongestWorkout() }.getOrDefault(false) }
            if (record) state = state.copy(recordText = getString(R.string.fin_new_record))
        }
    }

    /** 7 ngày của tuần hiện tại như thẻ mục tiêu tuần ở Home; hôm nay luôn tính là đã tập (buổi vừa xong). */
    private fun loadWeek(): List<FinishedWeekDay> {
        val cal = Calendar.getInstance().apply {
            firstDayOfWeek = myPreference.firstDayOfWeek
            set(Calendar.DAY_OF_WEEK, myPreference.firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }
        val today = DateIdUtils.todayDateId()
        if (cal.timeInMillis > today) cal.add(Calendar.DAY_OF_MONTH, -7)
        val trained = HistoryDatabase.getDatabase(this).historyDao().allPassedDateIds.orEmpty().toSet()
        val labelFormat = SimpleDateFormat("EEEEE", Locale.getDefault())
        return (0 until 7).map {
            val dateId = DateIdUtils.toDateId(cal.timeInMillis)
            val day = FinishedWeekDay(labelFormat.format(cal.time), dateId in trained || dateId == today, dateId == today)
            cal.add(Calendar.DAY_OF_MONTH, 1)
            day
        }
    }

    /** Thử thách nhiều ngày: ngày kế tiếp của chính thử thách đó. Còn lại: bài gợi ý cho ngày mai (khác bài vừa tập). */
    private fun loadNext(): FinishedNext? {
        val program = mProgramObject ?: return null
        val day = intent.extras?.getInt("DAY_NUMBER", 0) ?: 0
        if (program.type == 1) {
            if (day + 1 >= program.total.coerceAtLeast(1)) return null
            return FinishedNext(program, program.name.orEmpty(), getString(R.string.txt_day) + " " + (day + 2))
        }
        val candidates = FitnessApplication.getApplication().mJsonHelper.allCategory.orEmpty().filter { it.id != program.id }
        val hour = java.time.LocalTime.now().hour
        val timeOfDay = when (hour) {
            in 5..11 -> ProgramRecommenderV3.TIME_MORNING
            in 12..16 -> ProgramRecommenderV3.TIME_AFTERNOON
            else -> ProgramRecommenderV3.TIME_EVENING
        }
        val next = ProgramRecommenderV3().suggest(
            candidates, 1, myPreference.isPremiumMember, timeOfDay, setOf(program.id), 1, 0,
            myPreference.userTargetFocus, false
        ) ?: return null
        return FinishedNext(next, next.name.orEmpty(), getString(R.string.x_minutes, next.total))
    }

    /** Mở buổi tiếp theo như Home (thử thách -> màn thử thách, bài lẻ -> Preview), không chen quảng cáo. */
    private fun startNext() {
        val program = state.next?.program ?: return
        FirebaseAnalytics.getInstance(this).logEvent("finish_next_start", Bundle().apply { putInt("program_id", program.id) })
        val target = if (program.type == 1) ChallengeActivity::class.java else PreviewActivity::class.java
        startActivity(Intent(this, target).putExtra("PROGRAM_ID", program.id).putExtra("DAY_NUMBER", 0))
        finish()
    }

    override fun onResume() {
        super.onResume()
        // Quay lại từ màn Nhắc nhở: hiện giờ nhắc mới.
        state = state.copy(reminderText = reminderText())
    }

    /**
     * Lịch sử vừa được ghi (không đồng bộ) trước khi màn hình này mở, nên không thể tính huy
     * hiệu ngay. Bám vào LiveData của bảng history để chắc chắn bản ghi mới đã có trước khi tính.
     */
    private fun observeBadgeUnlocks() {
        historyViewModel.getAllHistory().observe(this) {
            if (badgeCelebrationShown) return@observe
            BadgeRepository.load(this) { board ->
                if (badgeCelebrationShown || board.newlyUnlocked.isEmpty()) return@load
                if (isFinishing || isDestroyed) return@load
                badgeCelebrationShown = true
                BadgeUnlockedDialog.show(this, board.newlyUnlocked) {
                    startActivity(BadgeActivity.newIntent(this))
                }
            }
        }
    }

    /** Streak sau buổi tập; nếu buổi vừa xong chưa kịp được tính thì cộng thêm hôm nay. */
    private fun showStreak() {
        lifecycleScope.launch {
            val result = withContext(Dispatchers.IO) {
                runCatching {
                    StreakCalculator(HistoryDatabase.getDatabase(this@FinishedActivity).historyDao())
                        .getUserStreakResult()
                }.getOrNull()
            } ?: return@launch
            val days = if (result.didPracticeToday) result.streakDays else result.streakDays + 1
            state = state.copy(
                streakText = when {
                    days <= 1 -> getString(R.string.streak_finished_first)
                    result.messageType == StreakMessageType.MILESTONE_ACHIEVED ->
                        getString(R.string.streak_finished_milestone, days)
                    else -> getString(R.string.streak_finished_title, days)
                }
            )
        }
    }

    private fun initView() {
        reviewManager = ReviewManagerFactory.create(this)
        val mBundle = intent.extras

        val stats = PracticeStatsStore.get(this)
        stats.isDidCompleteToday = true
        stats.missedDays = 0
        stats.streakCount = stats.streakCount + 1
        stats.consecutiveActiveDays = stats.consecutiveActiveDays + 1
        stats.lastCompletedAt = System.currentTimeMillis()
        PracticeStatsStore.save(this, stats)

        if (mBundle != null) {
            val id = mBundle.getInt("PROGRAM_ID", 0)
            val size = mBundle.getInt("SIZE")
            mProgramObject = FitnessApplication.getApplication().getProgram(id)
            @Suppress("DEPRECATION")
            mHistoryItem = mBundle.getParcelable<HistoryItem>("HISTORY") as HistoryItem
            val time = mHistoryItem.realDuration
            state = state.copy(
                programName = mHistoryItem.name.orEmpty(),
                durationText = String.format(Locale.US, "%d:%02d", time / 60, time % 60),
                kcal = String.format(Locale.US, "%.0f", mHistoryItem.calories.toFloat()),
                exercises = size,
            )
        }
        refreshWeight()
        state = state.copy(reminderText = reminderText())

        if (mCongratulationPlayer != null) mCongratulationPlayer!!.release()
        if (myPreference.isRingOn) {
            mCongratulationPlayer = MediaPlayer.create(this, R.raw.cheer)
            if (mCongratulationPlayer != null) {
                mCongratulationPlayer!!.isLooping = false
                mCongratulationPlayer!!.start()
            }
        }
        val openCount = myPreference.openCount()
        if (!myPreference.isRated && myPreference.showRate && openCount % 3 == 1) {
            DialogRating().show(supportFragmentManager, "rate")
        } else if (openCount > 1 && openCount % 10 == 1) {
            requestReview()
        }
        requestNotificationPermissionIfNeeded()
        if (!myPreference.isSetReminder) {
            SelectReminderEveryDayDialog().show(supportFragmentManager, "SelectReminder")
        }
        if (myPreference.isSyncGoogleFit && Build.VERSION.SDK_INT >= 26 && ::mHistoryItem.isInitialized) {
            lifecycleScope.launch {
                insertHealthData()
            }
        }
    }

    private fun refreshWeight() {
        val unit = myPreference.weightUnit
        val heightM = myPreference.currentHeightM
        val bmi = if (heightM > 0f) myPreference.currentWeightKG / (heightM * heightM) else 0f
        state = state.copy(
            weightText = String.format(Locale.US, "%.1f %s", myPreference.currentWeight, unit),
            bmiText = if (bmi > 0f) String.format(Locale.US, "BMI %.1f", bmi) else "",
        )
    }

    private fun reminderText(): String =
        FitnessApplication.getApplication().mDatabase.getReminderSet().orEmpty().ifBlank { "--:--" }

    /** Buổi vừa xong dài hơn mọi buổi trước (cần ít nhất 1 buổi trước đó để không báo kỷ lục ở buổi đầu tiên). */
    private fun isLongestWorkout(): Boolean {
        if (!::mHistoryItem.isInitialized) return false
        val current = mHistoryItem.duration
        if (current < 60) return false
        val previous = HistoryDatabase.getDatabase(this).historyDao().all.orEmpty().filterNotNull()
            .filter { it.startTime != mHistoryItem.startTime }
        return previous.isNotEmpty() && current > previous.maxOf { it.duration }
    }

    /** Cảm nhận độ khó (0 = quá khó .. 4 = quá dễ): thống kê + DifficultyStore chỉnh reps buổi sau. */
    private fun onFeel(level: Int) {
        state = state.copy(feel = level)
        // Buổi sau tự tăng / giảm reps theo đánh giá; tính từ hệ số lúc mở màn để chọn lại không cộng dồn.
        val programId = mProgramObject?.id ?: 0
        DifficultyStore.applyFeel(this, programId, basePercent, level)
        FirebaseAnalytics.getInstance(this).logEvent("workout_feel", Bundle().apply {
            putInt("feel", level)
            putInt("program_id", mProgramObject?.id ?: 0)
        })
    }

    private fun share() {
        try {
            val sharingIntent = Intent(Intent.ACTION_SEND)
            sharingIntent.setType("text/plain")
            sharingIntent.putExtra(
                Intent.EXTRA_TEXT,
                "https://play.google.com/store/apps/details?id=$packageName"
            )
            startActivity(Intent.createChooser(sharingIntent, "Share via"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun closeWithAd() {
        if (MaticAd.getInstance().isAvailableInter) {
            MaticAd.getInstance().showPriorityInterstitialAd(this, object : MaticAdCallBack() {
                override fun onNextAction() {
                    super.onNextAction()
                    finish()
                }
            })
        } else {
            finish()
        }
    }

    @Composable
    private fun AdBanner() {
        if (!(myPreference.isShowAds && myPreference.ok)) return
        AndroidView(
            factory = { ctx ->
                AdView(ctx).apply {
                    setAdSize(AdSize.BANNER)
                    adUnitId = getString(R.string.ad_banner_unit_id)
                    loadAd(AdRequest.Builder().build())
                    adView = this
                }
            },
            modifier = Modifier.fillMaxWidth(),
        )
    }

    private fun requestReview() {
        try {
            if (FirebaseRemoteConfig.getInstance().getBoolean("enable_review")) {
                reviewManager.requestReviewFlow()
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val reviewInfo: ReviewInfo = task.result
                            reviewManager.launchReviewFlow(this@FinishedActivity, reviewInfo)
                        }
                    }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun insertHealthData() {
        try {
            val availabilityStatus = HealthConnectClient.getSdkStatus(this)
            if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE) {
                Toast.makeText(this, "sdk UNAVAILABLE", Toast.LENGTH_SHORT).show()
                return
            }
            if (availabilityStatus == HealthConnectClient.SDK_UNAVAILABLE_PROVIDER_UPDATE_REQUIRED) {
                val uriString =
                    "market://details?id=com.google.android.apps.healthdata&url=healthconnect%3A%2F%2Fonboarding"
                val intent = Intent(Intent.ACTION_VIEW)
                intent.setPackage("com.android.vending")
                intent.setData(Uri.parse(uriString))
                intent.putExtra("overlay", true)
                intent.putExtra("callerId", packageName)
                startActivity(intent)
                return
            }
            val healthConnectClient = HealthConnectClient.getOrCreate(this)
            val workoutSession = ExerciseSessionRecord(
                startTime = Instant.ofEpochMilli(mHistoryItem.startTime),
                startZoneOffset = ZoneOffset.UTC,
                endTime = Instant.now(),
                endZoneOffset = ZoneOffset.UTC,
                exerciseType = ExerciseSessionRecord.EXERCISE_TYPE_GYMNASTICS,
                title = mHistoryItem.name,
            )
            healthConnectClient.insertRecords(listOf(workoutSession))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    /**
     * Xin quyền thông báo khi người dùng đã tập vài lần (openCount >= 2) và chưa đặt nhắc nhở —
     * lúc họ tích cực nhất.
     */
    private fun requestNotificationPermissionIfNeeded() {
        val openCount = myPreference.openCount()
        if (!myPreference.isSetReminder && openCount >= 2) {
            Log.d("NotificationRequest", "Requesting notification permission at openCount: $openCount")
            NotificationPermissionDialog.showPracticeReminderDialog(this)
        }
    }

    override fun onDestroy() {
        mCongratulationPlayer?.release()
        mCongratulationPlayer = null
        adView?.destroy()
        super.onDestroy()
    }

    override fun attachBaseContext(newBase: Context) {
        val lang = PreferenceManager.getDefaultSharedPreferences(newBase).getString("ST_LANGUAGE", "").orEmpty()
        super.attachBaseContext(
            LanguageHelper.setLanguage(newBase, if (lang.isNotEmpty()) lang.substring(0, 2) else Locale.getDefault().language)
        )
    }

    override fun applyOverrideConfiguration(overrideConfiguration: Configuration?) {
        if (overrideConfiguration != null) {
            val uiMode = overrideConfiguration.uiMode
            overrideConfiguration.setTo(baseContext.resources.configuration)
            overrideConfiguration.uiMode = uiMode
        }
        super.applyOverrideConfiguration(overrideConfiguration)
    }
}
