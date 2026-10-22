package mint.woman.female.workout.activity.ui.workout

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.widget.NumberPicker
import android.widget.Toast
import androidx.activity.compose.setContent
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import androidx.preference.PreferenceManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import mint.woman.female.workout.BuildConfig
import mint.woman.female.workout.FitnessApplication
import mint.woman.female.workout.R
import mint.woman.female.workout.activity.ui.compose.MinTheme
import mint.woman.female.workout.activity.ui.custom.CustomMyWorkoutActivity
import mint.woman.female.workout.activity.ui.preview.PreviewActivity
import mint.woman.female.workout.activity.ui.workout.compose.ChallengeActions
import mint.woman.female.workout.activity.ui.workout.compose.ChallengeScreen
import mint.woman.female.workout.activity.ui.workout.compose.ChallengeState
import mint.woman.female.workout.model.ProgramObject
import mint.woman.female.workout.utils.Database
import mint.woman.female.workout.utils.EdgeToEdgeHelper
import mint.woman.female.workout.utils.LanguageHelper
import mint.woman.female.workout.utils.MyPreference
import java.util.Locale

/**
 * Thử thách nhiều ngày (vd 7x4), giao diện Compose ([ChallengeScreen]) giống app WomenWorkout.
 * Tiến độ đọc lại ở mỗi onResume nên tập xong một ngày quay về là thấy ngay.
 */
class ChallengeActivity : AppCompatActivity(), EdgeToEdgeHelper.HandlesInsets {

    private lateinit var myPreference: MyPreference
    private var programId = 0
    private var program: ProgramObject? = null
    private var state by mutableStateOf(ChallengeState())
    private var adView: AdView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        // Header là ảnh tối: icon status bar màu trắng.
        enableEdgeToEdge(statusBarStyle = SystemBarStyle.dark(android.graphics.Color.TRANSPARENT))
        super.onCreate(savedInstanceState)
        myPreference = MyPreference(this)
        programId = intent.extras?.getInt("PROGRAM_ID", 0) ?: 0
        program = FitnessApplication.getApplication().getProgram(programId)
        if (program == null) {
            Toast.makeText(this, R.string.ch_removed, Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        setContent {
            MinTheme {
                ChallengeScreen(
                    state = state,
                    actions = ChallengeActions(
                        onBack = ::finish,
                        onDay = ::goDay,
                        onReset = ::restartProgress,
                        onSetProgress = ::showDialogSetProgress,
                        onEdit = ::openEdit,
                    ),
                    banner = { AdBanner() },
                )
            }
        }
    }

    override fun onResume() {
        super.onResume()
        refresh()
    }

    private fun refresh() {
        val p = program ?: return
        state = ChallengeState(
            program = p,
            progress = myPreference.getPlanProgress(p.id),
            showEdit = BuildConfig.DEBUG,
        )
    }

    private fun goDay(day: Int) {
        val p = program ?: return
        val progress = myPreference.getPlanProgress(p.id)
        when {
            day >= p.total -> restartProgress()
            day > progress -> Toast.makeText(
                this, getString(R.string.ch_finish_previous, progress + 1), Toast.LENGTH_SHORT
            ).show()
            // Bài tự tạo: ngày chưa thêm động tác thì mở màn sửa, không vào Preview trống.
            isEmptyCustomDay(p, day) -> {
                Toast.makeText(this, R.string.my_workout_day_empty, Toast.LENGTH_SHORT).show()
                openEdit()
            }
            else -> startActivity(
                Intent(this, PreviewActivity::class.java).putExtras(
                    Bundle().apply {
                        putInt("PROGRAM_ID", programId)
                        putInt("DAY_NUMBER", day)
                    },
                ),
            )
        }
    }

    private fun isEmptyCustomDay(p: ProgramObject, day: Int): Boolean {
        if (p.id <= Database.MY_WORKOUT_ID_BASE) return false
        val days = FitnessApplication.getApplication().mJsonHelper.getListPlan(p.plan, p.discover, p.version)
        return days == null || day >= days.size || days[day].mActionList.isNullOrEmpty()
    }

    private fun restartProgress() {
        val p = program ?: return
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.txt_restart_progress) + " " + p.name)
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                myPreference.setPlanProgress(p.id, 0)
                refresh()
            }
            .show()
    }

    private fun showDialogSetProgress() {
        val p = program ?: return
        val picker = NumberPicker(this).apply {
            minValue = 0
            maxValue = p.total - 1
            displayedValues = Array(p.total) { getString(R.string.txt_day) + " " + (it + 1) }
            value = myPreference.getPlanProgress(p.id).coerceIn(0, p.total - 1)
        }
        AlertDialog.Builder(this)
            .setTitle(R.string.txt_start_day)
            .setMessage(R.string.txt_choose_day)
            .setView(picker)
            .setPositiveButton(android.R.string.ok) { _, _ ->
                myPreference.setPlanProgress(p.id, picker.value)
                refresh()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .show()
    }

    private fun openEdit() {
        startActivity(
            Intent(this, CustomMyWorkoutActivity::class.java)
                .putExtras(Bundle().apply { putInt("PROGRAM_ID", programId) })
        )
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

    override fun onDestroy() {
        adView?.destroy()
        super.onDestroy()
    }

    override fun attachBaseContext(newBase: Context) {
        val lang = PreferenceManager.getDefaultSharedPreferences(newBase).getString("ST_LANGUAGE", "").orEmpty()
        super.attachBaseContext(
            LanguageHelper.setLanguage(newBase, if (lang.isNotEmpty()) lang.substring(0, 2) else Locale.getDefault().language),
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
