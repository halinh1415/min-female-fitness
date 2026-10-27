package mint.woman.female.workout.activity.ui.settings

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.media.MediaPlayer
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.DrawableRes
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import androidx.preference.PreferenceManager
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequest
import androidx.work.WorkInfo
import androidx.work.WorkManager
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import mint.woman.female.workout.R
import mint.woman.female.workout.retrofit.ApiUtils
import mint.woman.female.workout.utils.AppEncrypt
import mint.woman.female.workout.utils.DownloadVoiceWorker
import mint.woman.female.workout.FitnessApplication
import mint.woman.female.workout.utils.LanguageHelper
import mint.woman.female.workout.utils.MyPreference
import mint.woman.female.workout.utils.TtsExecutor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.util.Locale

/**
 * Màn chọn giọng huấn luyện viên (Jetpack Compose, viết lại 26/10/2026 như app Women Workout, thay bản XML cũ).
 *
 * - Giọng: "Mặc định" (TTS máy) + các giọng thu sẵn có cho ngôn ngữ app (Remote Config `enable_voice` + `voice`).
 * - "Đã tải" = có câu giới thiệu HLV ([isReady]). Bản cũ kiểm câu "Ready to go" nên khi Preview đã tải sẵn vài câu
 *   (giọng mặc định) thì bấm tải không làm gì mà nghe thử cũng không có câu giới thiệu.
 * - Tải bằng [DownloadVoiceWorker] (unique work theo ngôn ngữ + giọng): rời màn vẫn tải tiếp; quay lại màn thì theo dõi
 *   tiếp tiến độ. Bấm giọng chưa tải là chọn luôn giọng đó (câu chưa có file thì AudioAppManager đọc TTS).
 * - Tips và tên bài của bài tải về: PreviewActivity tải theo buổi.
 */
class CoachOptionActivity : AppCompatActivity() {

    data class Voice(val value: String, val name: String, @DrawableRes val avatar: Int)

    sealed interface VoiceState {
        data object NotDownloaded : VoiceState
        data class Downloading(val done: Int, val total: Int) : VoiceState
        data object Ready : VoiceState
        data object Failed : VoiceState
    }

    private lateinit var myPreference: MyPreference
    private lateinit var language: String
    private var voices by mutableStateOf<List<Voice>>(emptyList())
    private val states = mutableStateMapOf<String, VoiceState>()
    private var selected by mutableStateOf("default")
    private var playing by mutableStateOf("")
    private var ttsEngine by mutableStateOf("")
    private var ttsLocale by mutableStateOf("")

    @Volatile private var tts: TextToSpeech? = null
    private var mediaPlayer: MediaPlayer? = null

    /** md5 câu chung (theo giọng: câu giới thiệu có tên HLV). Tính một lần trên luồng phụ. */
    private var coreFiles: Map<String, List<String>> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        myPreference = MyPreference.newInstance(this)
        language = myPreference.language
        voices = availableVoices()
        selected = myPreference.getVoiceModel(language)
        if (voices.none { it.value == selected }) selected = DEFAULT
        initTts()
        setContent {
            MaterialTheme {
                CoachScreen(
                    voices = voices,
                    states = states,
                    selected = selected,
                    playing = playing,
                    ttsEngine = ttsEngine,
                    ttsLocale = ttsLocale,
                    onBack = ::finish,
                    onDefault = ::chooseDefault,
                    onVoice = ::chooseVoice,
                    onPlay = ::play,
                    onOpenTtsSettings = ::openTtsSettings,
                    onInstallTtsData = ::installTtsData,
                )
            }
        }
        lifecycleScope.launch {
            coreFiles = withContext(Dispatchers.IO) { voices.associate { it.value to coreTexts(it).map(AppEncrypt::getName) } }
            refreshStates()
            voices.forEach(::observeDownload)
        }
    }

    // ------------------------------------------------------------ dữ liệu giọng

    private fun availableVoices(): List<Voice> {
        val config = FirebaseRemoteConfig.getInstance()
        if (!config.getBoolean("enable_voice")) return emptyList()
        val json = try {
            JSONObject(config.getString("voice"))
        } catch (e: Exception) {
            Log.w(TAG, "Remote Config voice is not JSON")
            return emptyList()
        }
        fun has(key: String) = ("@" + json.optString(key, "") + "@").contains("@$language@")
        return buildList {
            if (has("woman")) add(Voice("woman", getString(R.string.name_female), R.drawable.ic_coach_woman))
            if (has("man")) add(Voice("man", getString(R.string.name_male), R.drawable.ic_coach_male))
            if (has("Bella")) add(Voice("Bella", "Jennifer", R.drawable.ic_coach_jennifer))
            if (has("Adam")) add(Voice("Adam", "Adam", R.drawable.ic_coach_adam))
        }
    }

    private fun introText(voice: Voice) = String.format(getString(R.string.tts_test_des), voice.name)

    /** Câu chung + câu giới thiệu + tên bài trong app + số 1–99 (như bản XML cũ); tips tải theo buổi ở PreviewActivity. */
    private fun coreTexts(voice: Voice): List<String> {
        val texts = LinkedHashSet<String>()
        texts += introText(voice)
        CORE_PHRASES.forEach { texts += getString(it) }
        FitnessApplication.getApplication().mJsonHelper.getAllExercise(0, 0).orEmpty().forEach { e ->
            texts += e.name ?: return@forEach
        }
        (1..99).forEach { texts += "$it" }
        return texts.filter { it.isNotBlank() }
    }

    private fun voiceDir(value: String) = File(filesDir, "voice/$language/$value")

    /**
     * Có câu giới thiệu HLV (tải sau cùng một lượt với câu chung) thì coi là đã tải. Không đòi đủ mọi câu chung: server
     * có thể thiếu câu (vd chữ trong app đổi sau khi thu giọng) – câu thiếu lúc tập đọc TTS, không chặn việc chọn giọng.
     */
    private fun isReady(voice: Voice): Boolean = File(voiceDir(voice.value), AppEncrypt.getName(introText(voice))).exists()

    private fun refreshStates() {
        voices.forEach { v ->
            if (states[v.value] !is VoiceState.Downloading) {
                states[v.value] = if (isReady(v)) VoiceState.Ready else (states[v.value] as? VoiceState.Failed ?: VoiceState.NotDownloaded)
            }
        }
    }

    // ------------------------------------------------------------ tải

    private fun workName(value: String) = "coach_voice_${language}_$value"

    private fun startDownload(voice: Voice) {
        val files = coreFiles[voice.value] ?: return
        val dir = voiceDir(voice.value)
        val missing = files.filterNot { File(dir, it).exists() }
        if (missing.isEmpty()) {
            states[voice.value] = VoiceState.Ready
            return
        }
        if (!dir.exists() && !dir.mkdirs()) return
        val list = File(dir, "file_list_${System.currentTimeMillis()}.json")
        list.writeText(JSONArray(missing).toString())
        val request = OneTimeWorkRequest.Builder(DownloadVoiceWorker::class.java)
            .setInputData(
                Data.Builder()
                    .putString("FILE_LIST_PATH", list.path)
                    .putString("BASE_PATH", dir.path + "/")
                    .putString("BASE_URL", ApiUtils.BASE_APP_URL + "voice/$language/${voice.value}/")
                    .build()
            )
            .setConstraints(Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
            .build()
        states[voice.value] = VoiceState.Downloading(0, missing.size)
        WorkManager.getInstance(applicationContext).enqueueUniqueWork(workName(voice.value), ExistingWorkPolicy.KEEP, request)
    }

    /** Theo dõi lượt tải của giọng (kể cả lượt bắt đầu trước khi mở màn). */
    private fun observeDownload(voice: Voice) {
        WorkManager.getInstance(applicationContext).getWorkInfosForUniqueWorkLiveData(workName(voice.value))
            .observe(this) { infos ->
                val info = infos?.lastOrNull() ?: return@observe
                when (info.state) {
                    WorkInfo.State.ENQUEUED, WorkInfo.State.RUNNING, WorkInfo.State.BLOCKED -> {
                        val done = info.progress.getInt("PROGRESS", 0)
                        val total = info.progress.getInt("TOTAL", (states[voice.value] as? VoiceState.Downloading)?.total ?: 0)
                        states[voice.value] = VoiceState.Downloading(done, total)
                    }
                    WorkInfo.State.SUCCEEDED -> {
                        val wasDownloading = states[voice.value] is VoiceState.Downloading
                        states[voice.value] = if (isReady(voice)) VoiceState.Ready else VoiceState.Failed
                        if (wasDownloading && selected == voice.value && states[voice.value] == VoiceState.Ready) play(voice.value)
                    }
                    WorkInfo.State.FAILED, WorkInfo.State.CANCELLED -> {
                        if (states[voice.value] is VoiceState.Downloading) states[voice.value] = VoiceState.Failed
                    }
                }
            }
    }

    // ------------------------------------------------------------ chọn / nghe thử

    private fun select(value: String, name: String) {
        selected = value
        myPreference.setVoiceModel(language, value)
        myPreference.setVoiceModelName(language, name)
    }

    private fun chooseDefault() {
        select(DEFAULT, "Default")
        play(DEFAULT)
    }

    private fun chooseVoice(voice: Voice) {
        // Chọn ngay cả khi chưa tải xong: rời màn giữa chừng thì giọng vẫn được dùng (câu thiếu file đọc TTS).
        select(voice.value, voice.name)
        when (states[voice.value]) {
            VoiceState.Ready -> play(voice.value)
            is VoiceState.Downloading -> Unit
            else -> startDownload(voice)
        }
    }

    private fun play(value: String) {
        stopPlayback()
        if (value == DEFAULT) {
            val engine = tts ?: return
            playing = DEFAULT
            val text = getString(R.string.test_voice)
            TtsExecutor.run { engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "test") }
            window.decorView.postDelayed({ if (playing == DEFAULT) playing = "" }, 2000)
            return
        }
        val voice = voices.firstOrNull { it.value == value } ?: return
        if (states[value] != VoiceState.Ready) {
            chooseVoice(voice)
            return
        }
        val file = File(voiceDir(value), AppEncrypt.getName(introText(voice)))
        lifecycleScope.launch {
            val temp = withContext(Dispatchers.IO) {
                runCatching {
                    File(cacheDir, "coach_intro.mp3").also { out ->
                        out.writeBytes(file.readBytes().map { (it.toInt() xor 0xAB).toByte() }.toByteArray())
                    }
                }.getOrNull()
            } ?: return@launch
            runCatching {
                mediaPlayer = MediaPlayer().apply {
                    setDataSource(temp.path)
                    setOnCompletionListener { playing = "" }
                    prepare()
                    start()
                }
                playing = value
            }.onFailure { Log.w(TAG, "Cannot play intro: ${it.message}") }
        }
    }

    private fun stopPlayback() {
        mediaPlayer?.runCatching { stop(); release() }
        mediaPlayer = null
        playing = ""
    }

    // ------------------------------------------------------------ TTS máy

    private fun initTts() {
        val appContext = applicationContext
        TtsExecutor.run {
            val holder = arrayOfNulls<TextToSpeech>(1)
            holder[0] = TextToSpeech(appContext) { status ->
                TtsExecutor.run {
                    val engine = holder[0] ?: return@run
                    if (status != TextToSpeech.SUCCESS || engine !== tts) return@run
                    val parts = language.split("_")
                    engine.language = if (parts.size > 1) Locale(parts[0], parts[1]) else Locale(parts[0])
                    val name = engine.defaultEngine.orEmpty()
                    val locale = engine.voice?.locale?.displayLanguage ?: engine.defaultVoice?.locale?.displayLanguage.orEmpty()
                    runOnUiThread {
                        ttsEngine = name
                        ttsLocale = locale
                    }
                }
            }
            if (isDestroyed) holder[0]?.shutdown() else tts = holder[0]
        }
    }

    private fun openTtsSettings() {
        runCatching {
            startActivity(Intent("com.android.settings.TTS_SETTINGS").addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
        }.onFailure { Log.w(TAG, "No TTS settings screen") }
    }

    private fun installTtsData() {
        try {
            startActivity(
                Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA)
                    .setPackage("com.google.android.tts")
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            )
        } catch (e: ActivityNotFoundException) {
            openTtsSettings()
        }
    }

    override fun onResume() {
        super.onResume()
        if (coreFiles.isNotEmpty()) refreshStates()
    }

    override fun onDestroy() {
        stopPlayback()
        val engine = tts
        tts = null
        if (engine != null) TtsExecutor.run { engine.stop(); engine.shutdown() }
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

    companion object {
        private const val TAG = "CoachOption"
        private const val DEFAULT = "default"

        /** Câu chung DoWorkoutActivity đọc mỗi buổi. */
        private val CORE_PHRASES = listOf(
            R.string.wp_start, R.string.v_start_with, R.string.wp_ready_to_go, R.string.wp_v_half_time,
            R.string.wp_seconds, R.string.wp_each_side, R.string.wp_the_next, R.string.wp_have_a_rest,
        )
    }
}

// ---------------------------------------------------------------- giao diện

@Composable
private fun CoachScreen(
    voices: List<CoachOptionActivity.Voice>,
    states: Map<String, CoachOptionActivity.VoiceState>,
    selected: String,
    playing: String,
    ttsEngine: String,
    ttsLocale: String,
    onBack: () -> Unit,
    onDefault: () -> Unit,
    onVoice: (CoachOptionActivity.Voice) -> Unit,
    onPlay: (String) -> Unit,
    onOpenTtsSettings: () -> Unit,
    onInstallTtsData: () -> Unit,
) {
    val c = CoachColors
    Column(
        Modifier
            .fillMaxSize()
            .background(c.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Row(Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 4.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier.size(48.dp).clip(CircleShape).clickable(onClick = onBack),
                contentAlignment = Alignment.Center
            ) {
                Image(painterResource(R.drawable.ic_baseline_arrow_back_24), contentDescription = null, modifier = Modifier.size(24.dp),
                    colorFilter = androidx.compose.ui.graphics.ColorFilter.tint(c.title))
            }
            Text(stringResource(R.string.coach_voice), color = c.title, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        }
        Column(
            Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(stringResource(R.string.tap_to_listen_and_choose), color = c.muted, fontSize = 14.sp)
            DefaultCard(
                selected = selected == "default",
                playing = playing == "default",
                ttsEngine = ttsEngine,
                ttsLocale = ttsLocale,
                onSelect = onDefault,
                onPlay = { onPlay("default") },
                onOpenTtsSettings = onOpenTtsSettings,
                onInstallTtsData = onInstallTtsData,
            )
            voices.forEach { voice ->
                VoiceCard(
                    voice = voice,
                    state = states[voice.value] ?: CoachOptionActivity.VoiceState.NotDownloaded,
                    selected = selected == voice.value,
                    playing = playing == voice.value,
                    onSelect = { onVoice(voice) },
                    onPlay = { onPlay(voice.value) },
                )
            }
            Spacer(Modifier.height(16.dp))
        }
    }
}

@Composable
private fun CardFrame(selected: Boolean, onClick: () -> Unit, content: @Composable () -> Unit) {
    val c = CoachColors
    Box(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(c.surface)
            .border(BorderStroke(if (selected) 2.dp else 1.dp, if (selected) c.primary else c.border), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) { content() }
}

@Composable
private fun SelectMark(selected: Boolean) {
    val c = CoachColors
    Box(
        Modifier
            .size(24.dp)
            .clip(CircleShape)
            .border(2.dp, if (selected) c.primary else c.border, CircleShape)
            .padding(5.dp)
    ) {
        if (selected) Box(Modifier.fillMaxSize().clip(CircleShape).background(c.primary))
    }
}

@Composable
private fun PlayButton(playing: Boolean, onPlay: () -> Unit) {
    val c = CoachColors
    Box(
        Modifier.size(40.dp).clip(CircleShape).background(c.primaryContainer).clickable(onClick = onPlay),
        contentAlignment = Alignment.Center
    ) {
        if (playing) {
            // Đang phát: sóng nhạc Lottie như màn chọn giọng cũ.
            androidx.compose.ui.viewinterop.AndroidView(
                factory = { ctx ->
                    com.airbnb.lottie.LottieAnimationView(ctx).apply {
                        setAnimation(R.raw.voice_play)
                        repeatCount = com.airbnb.lottie.LottieDrawable.INFINITE
                        playAnimation()
                    }
                },
                modifier = Modifier.size(40.dp)
            )
        } else {
            Text("▶", color = c.primary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        }
    }
}

@Composable
private fun DefaultCard(
    selected: Boolean,
    playing: Boolean,
    ttsEngine: String,
    ttsLocale: String,
    onSelect: () -> Unit,
    onPlay: () -> Unit,
    onOpenTtsSettings: () -> Unit,
    onInstallTtsData: () -> Unit,
) {
    val c = CoachColors
    CardFrame(selected, onSelect) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(painterResource(R.drawable.ic_coach_default), contentDescription = null, modifier = Modifier.size(52.dp))
                Spacer(Modifier.width(12.dp))
                Text(stringResource(R.string.default_text), color = c.title, fontSize = 17.sp, fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f))
                PlayButton(playing, onPlay)
                Spacer(Modifier.width(12.dp))
                SelectMark(selected)
            }
            if (selected) {
                Spacer(Modifier.height(8.dp))
                SettingLine(stringResource(R.string.select_tts), ttsEngine, onOpenTtsSettings)
                SettingLine(stringResource(R.string.tts_name), ttsLocale, onOpenTtsSettings)
                SettingLine(stringResource(R.string.tts_data), null, onInstallTtsData)
            }
        }
    }
}

@Composable
private fun SettingLine(title: String, value: String?, onClick: () -> Unit) {
    val c = CoachColors
    Column(Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 10.dp)) {
        Box(Modifier.fillMaxWidth().height(1.dp).background(c.border))
        Spacer(Modifier.height(10.dp))
        Text(title, color = c.title, fontSize = 15.sp)
        if (!value.isNullOrEmpty()) Text(value, color = c.muted, fontSize = 13.sp)
    }
}

@Composable
private fun VoiceCard(
    voice: CoachOptionActivity.Voice,
    state: CoachOptionActivity.VoiceState,
    selected: Boolean,
    playing: Boolean,
    onSelect: () -> Unit,
    onPlay: () -> Unit,
) {
    val c = CoachColors
    CardFrame(selected, onSelect) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painterResource(voice.avatar), contentDescription = null, contentScale = ContentScale.Crop,
                    modifier = Modifier.size(52.dp).clip(CircleShape)
                )
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(voice.name, color = c.title, fontSize = 17.sp, fontWeight = FontWeight.Bold)
                    val sub = when (state) {
                        is CoachOptionActivity.VoiceState.Downloading ->
                            if (state.total > 0) "${state.done * 100 / state.total}%" else "…"
                        CoachOptionActivity.VoiceState.Failed -> stringResource(R.string.oops_network_error)
                        CoachOptionActivity.VoiceState.NotDownloaded -> stringResource(R.string.download)
                        CoachOptionActivity.VoiceState.Ready -> null
                    }
                    if (sub != null) {
                        Text(sub, color = if (state == CoachOptionActivity.VoiceState.Failed) c.bad else c.muted, fontSize = 13.sp)
                    }
                }
                when (state) {
                    CoachOptionActivity.VoiceState.Ready -> {
                        PlayButton(playing, onPlay)
                        Spacer(Modifier.width(12.dp))
                        SelectMark(selected)
                    }
                    is CoachOptionActivity.VoiceState.Downloading ->
                        CircularProgressIndicator(Modifier.size(28.dp), color = c.primary, strokeWidth = 3.dp)
                    else -> Box(
                        Modifier.size(40.dp).clip(CircleShape).background(c.primary).clickable(onClick = onSelect),
                        contentAlignment = Alignment.Center
                    ) { Text("↓", color = c.onPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold) }
                }
            }
            if (state is CoachOptionActivity.VoiceState.Downloading && state.total > 0) {
                Spacer(Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { state.done.toFloat() / state.total },
                    modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                    color = c.primary,
                    trackColor = c.primaryContainer,
                )
            }
        }
    }
}

/** Min Female Fitness chưa có theme Compose: màu theo colorPrimary (#E91E63) của app. */
private object CoachColors {
    val background = androidx.compose.ui.graphics.Color(0xFFF7F4F6)
    val surface = androidx.compose.ui.graphics.Color.White
    val primary = androidx.compose.ui.graphics.Color(0xFFE91E63)
    val onPrimary = androidx.compose.ui.graphics.Color.White
    val primaryContainer = androidx.compose.ui.graphics.Color(0xFFFCE4EC)
    val title = androidx.compose.ui.graphics.Color(0xFF1F1A1D)
    val muted = androidx.compose.ui.graphics.Color(0xFF7A6F75)
    val border = androidx.compose.ui.graphics.Color(0xFFE6DDE2)
    val bad = androidx.compose.ui.graphics.Color(0xFFD32F2F)
}
