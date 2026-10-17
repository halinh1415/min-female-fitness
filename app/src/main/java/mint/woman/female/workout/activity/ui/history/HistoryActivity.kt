package mint.woman.female.workout.activity.ui.history

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.preference.PreferenceManager
import mint.woman.female.workout.FitnessApplication
import mint.woman.female.workout.R
import mint.woman.female.workout.activity.ui.compose.MinTheme
import mint.woman.female.workout.activity.ui.compose.imageBase
import mint.woman.female.workout.activity.ui.preview.PreviewActivity
import mint.woman.female.workout.model.HistoryItem
import mint.woman.female.workout.utils.LanguageHelper
import mint.woman.female.workout.utils.MyPreference
import java.util.Calendar
import java.util.Locale

/** Lịch sử tập luyện: lịch tháng + danh sách theo ngày, giao diện Jetpack Compose. */
class HistoryActivity : AppCompatActivity(), HistoryActions {

    private lateinit var viewModel: HistoryViewModel
    private val state = HistoryUiState()
    private var monthData: LiveData<List<HistoryItem>>? = null
    private val monthObserver = Observer<List<HistoryItem>> { items ->
        state.items = items.orEmpty()
        state.loading = false
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this)[HistoryViewModel::class.java]
        state.firstDayOfWeek = MyPreference(this).firstDayOfWeek
        state.monthStart = Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0); set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0); set(Calendar.MILLISECOND, 0)
        }.timeInMillis
        loadMonth()
        setContent { MinTheme { HistoryScreen(state, this) } }
    }

    private fun loadMonth() {
        val end = Calendar.getInstance().apply {
            timeInMillis = state.monthStart
            add(Calendar.MONTH, 1)
        }.timeInMillis
        state.loading = true
        state.selectedDay = 0L
        monthData?.removeObserver(monthObserver)
        monthData = viewModel.findFromTo(state.monthStart, end).also { it.observe(this, monthObserver) }
    }

    private fun shiftMonth(delta: Int) {
        state.monthStart = Calendar.getInstance().apply {
            timeInMillis = state.monthStart
            add(Calendar.MONTH, delta)
        }.timeInMillis
        loadMonth()
    }

    override fun onPrevMonth() = shiftMonth(-1)

    override fun onNextMonth() = shiftMonth(1)

    override fun onItemClick(item: HistoryItem) {
        if (FitnessApplication.getApplication().mJsonHelper.allCategoryHashMap[item.programId] == null) return
        startActivity(
            Intent(this, PreviewActivity::class.java).putExtras(Bundle().apply {
                putInt("PROGRAM_ID", item.programId)
                putInt("DAY_NUMBER", item.dayIndex)
            })
        )
    }

    override fun imageUrl(item: HistoryItem): String {
        // Lịch sử cũ không lưu tên ảnh (image = null) nên trước đây URL chỉ còn thư mục → không hiện ảnh.
        // Ưu tiên ảnh hiện tại của chương trình, sau đó tên ảnh đã lưu, cuối cùng là ảnh mặc định.
        val program = FitnessApplication.getApplication().getProgram(item.programId)
        if (program != null && !program.image.isNullOrEmpty()) return program.imageBase() + program.image
        val saved = item.image
        return if (!saved.isNullOrEmpty()) ASSET_IMAGES + saved else HISTORY_IMAGE_FALLBACK
    }

    override fun onBack() {
        onBackPressedDispatcher.onBackPressed()
    }

    override fun onReset() {
        AlertDialog.Builder(this)
            .setTitle(getString(R.string.txt_alert_reset))
            .setNegativeButton(getString(android.R.string.cancel), null)
            .setPositiveButton(getString(android.R.string.ok)) { _, _ -> viewModel.deleteAllHistory() }
            .show()
    }

    override fun attachBaseContext(newBase: Context) {
        val saved = PreferenceManager.getDefaultSharedPreferences(newBase).getString("ST_LANGUAGE", "").orEmpty()
        val lang = if (saved.isNotEmpty()) saved.substring(0, 2) else Locale.getDefault().language
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
}

private const val ASSET_IMAGES = "file:///android_asset/images/"

/** Ảnh dùng khi chương trình đã bị xoá hoặc ảnh không tải được. */
internal const val HISTORY_IMAGE_FALLBACK = ASSET_IMAGES + "custom_workout.webp"
