package mint.woman.female.workout.activity.ui.workout

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.text.Layout
import android.text.StaticLayout
import android.text.TextPaint
import android.text.TextUtils
import androidx.core.content.FileProvider
import mint.woman.female.workout.R
import java.io.File
import java.text.DateFormat
import java.util.Date

/**
 * Ảnh chia sẻ sau buổi tập (1080x1350, vừa story và feed): ảnh bìa bài tập, tiêu đề, 3 ô số liệu,
 * chuỗi ngày và tên app. Vẽ thẳng bằng Canvas để không phụ thuộc màn hình đang hiện.
 */
object ShareCard {
    private const val W = 1080
    private const val H = 1350
    private val GRADIENT = intArrayOf(Color.parseColor("#FF5C8A"), Color.parseColor("#C2459F"))

    data class Data(
        val programName: String,
        val duration: String,
        val kcal: String,
        val exercises: Int,
        val streakText: String?,
        /** Tên file ảnh bìa trong assets/images (có thể không có). */
        val image: String?,
    )

    fun render(context: Context, data: Data): Bitmap {
        val bmp = Bitmap.createBitmap(W, H, Bitmap.Config.ARGB_8888)
        val c = Canvas(bmp)
        val bg = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            shader = LinearGradient(0f, 0f, W.toFloat(), H.toFloat(), GRADIENT[0], GRADIENT[1], Shader.TileMode.CLAMP)
        }
        c.drawRect(0f, 0f, W.toFloat(), H.toFloat(), bg)

        // Ảnh bìa nửa trên, mờ dần xuống nền gradient.
        val heroH = 620f
        loadAsset(context, data.image)?.let { hero ->
            val scale = maxOf(W / hero.width.toFloat(), heroH / hero.height)
            val sw = (W / scale).toInt()
            val sh = (heroH / scale).toInt()
            val sx = (hero.width - sw) / 2
            // Làm ảnh trong suốt dần (DST_IN) thay vì phủ một màu, để không lộ vệt nối với nền chéo.
            val layer = c.saveLayer(0f, 0f, W.toFloat(), heroH, null)
            c.drawBitmap(hero, Rect(sx, 0, sx + sw, sh), RectF(0f, 0f, W.toFloat(), heroH), Paint(Paint.FILTER_BITMAP_FLAG))
            hero.recycle()
            val fade = Paint().apply {
                shader = LinearGradient(0f, heroH * 0.35f, 0f, heroH, Color.BLACK, Color.TRANSPARENT, Shader.TileMode.CLAMP)
                xfermode = PorterDuffXfermode(PorterDuff.Mode.DST_IN)
            }
            c.drawRect(0f, 0f, W.toFloat(), heroH, fade)
            c.restoreToCount(layer)
        }

        val white = Color.WHITE
        val soft = Color.argb(215, 255, 255, 255)
        val bold = Typeface.create("sans-serif-black", Typeface.NORMAL)
        val medium = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        val pad = 72f

        // Tiêu đề + tên bài.
        var y = heroH - 40f
        val title = text(context.getString(R.string.fin_title), 76f, white, bold)
        y = drawWrapped(c, title, pad, y, W - 2 * pad, 2)
        y += 12f
        val name = text(data.programName, 48f, soft, medium)
        y = drawWrapped(c, name, pad, y, W - 2 * pad, 2)
        y += 48f

        // 3 ô số liệu.
        val gap = 24f
        val boxW = (W - 2 * pad - 2 * gap) / 3
        val boxH = 190f
        val boxPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.argb(46, 255, 255, 255) }
        val stats = listOf(
            data.duration to context.getString(R.string.fin_duration),
            data.kcal to "kcal",
            data.exercises.toString() to context.getString(R.string.fin_exercises),
        )
        stats.forEachIndexed { i, (value, label) ->
            val left = pad + i * (boxW + gap)
            c.drawRoundRect(RectF(left, y, left + boxW, y + boxH), 36f, 36f, boxPaint)
            val v = text(value, 64f, white, bold).apply { textAlign = Paint.Align.CENTER }
            c.drawText(fit(value, v, boxW - 24), left + boxW / 2, y + 100f, v)
            val l = text(label, 32f, soft, medium).apply { textAlign = Paint.Align.CENTER }
            c.drawText(fit(label, l, boxW - 24), left + boxW / 2, y + 152f, l)
        }
        y += boxH + 56f

        data.streakText?.let {
            val s = text(it, 44f, white, medium)
            y = drawWrapped(c, s, pad, y, W - 2 * pad, 2)
        }

        // Chân: ngày tập + tên app.
        val footer = text(DateFormat.getDateInstance(DateFormat.MEDIUM).format(Date()), 34f, soft, medium)
        c.drawText(footer.text, pad, H - 80f, footer)
        val app = text(context.getString(R.string.app_name), 38f, white, bold).apply { textAlign = Paint.Align.RIGHT }
        c.drawText(app.text, W - pad, H - 80f, app)
        return bmp
    }

    /** Vẽ ảnh, lưu vào cache/share và mở hộp chia sẻ (kèm chữ + link Store). */
    fun share(context: Context, data: Data, text: String, chooserTitle: String) {
        val dir = File(context.cacheDir, "share").apply { mkdirs() }
        val file = File(dir, "workout.png")
        val bmp = render(context, data)
        file.outputStream().use { bmp.compress(Bitmap.CompressFormat.PNG, 100, it) }
        bmp.recycle()
        val uri = FileProvider.getUriForFile(context, context.packageName + ".fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND)
            .setType("image/png")
            .putExtra(Intent.EXTRA_STREAM, uri)
            .putExtra(Intent.EXTRA_TEXT, text)
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        context.startActivity(Intent.createChooser(intent, chooserTitle))
    }

    private fun loadAsset(context: Context, name: String?): Bitmap? {
        if (name.isNullOrBlank()) return null
        return runCatching { context.assets.open("images/$name").use { BitmapFactory.decodeStream(it) } }.getOrNull()
    }

    private class Txt(val text: String, size: Float, color: Int, face: Typeface) : TextPaint(Paint.ANTI_ALIAS_FLAG) {
        init {
            textSize = size
            this.color = color
            typeface = face
        }
    }

    private fun text(s: String, size: Float, color: Int, face: Typeface) = Txt(s, size, color, face)

    private fun fit(s: String, p: Paint, max: Float): String =
        TextUtils.ellipsize(s, p as TextPaint, max, TextUtils.TruncateAt.END).toString()

    /** Vẽ chữ xuống dòng (tối đa [maxLines]); trả về y ngay dưới đoạn chữ. */
    private fun drawWrapped(c: Canvas, t: Txt, x: Float, y: Float, width: Float, maxLines: Int): Float {
        val layout = StaticLayout.Builder.obtain(t.text, 0, t.text.length, t, width.toInt())
            .setAlignment(Layout.Alignment.ALIGN_NORMAL)
            .setMaxLines(maxLines)
            .setEllipsize(TextUtils.TruncateAt.END)
            .build()
        c.save()
        c.translate(x, y)
        layout.draw(c)
        c.restore()
        return y + layout.height
    }
}
