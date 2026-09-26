package mint.woman.female.workout.badge;

import android.content.Context;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Doc noi dung huy hieu tu thu muc assets.
 *
 * <p>Cau truc huy hieu (metric, target, tier, thu tu) luon lay tu {@code badges/badges.json}.
 * Cac file {@code badges/badges-<ngon-ngu>.json} chi chua phan chu va duoc ghep de len tren,
 * nen doi nguong cua mot huy hieu chi phai sua o mot cho.</p>
 *
 * <p>Ket qua duoc cache trong bo nho de khong doc lai file moi lan mo man hinh.</p>
 */
public final class BadgeCatalogLoader {

    public static final String BADGE_DIR = "badges";
    public static final String ICON_DIR = BADGE_DIR + "/icons";

    private static final String TAG = "BadgeCatalog";
    private static final String BASE_FILE = BADGE_DIR + "/badges.json";

    private static volatile BadgeCatalog cached;
    private static volatile String cachedKey;

    private BadgeCatalogLoader() {
    }

    public static BadgeCatalog load(Context context) {
        Locale locale = Locale.getDefault();
        String key = locale.getLanguage() + "-" + locale.getCountry();

        BadgeCatalog local = cached;
        if (local != null && key.equals(cachedKey)) {
            return local;
        }

        synchronized (BadgeCatalogLoader.class) {
            if (cached != null && key.equals(cachedKey)) {
                return cached;
            }

            BadgeCatalog base = read(BASE_FILE, context);
            if (base == null) {
                base = new BadgeCatalog();
            }
            BadgeCatalog catalog = base.normalized();
            catalog.applyTranslation(readTranslation(context, locale));

            cached = catalog;
            cachedKey = key;
            return cached;
        }
    }

    /** Buoc app doc lai file assets o lan {@link #load(Context)} tiep theo. */
    public static void invalidate() {
        synchronized (BadgeCatalogLoader.class) {
            cached = null;
            cachedKey = null;
        }
        BadgeIconAssets.invalidate();
    }

    private static BadgeCatalog readTranslation(Context context, Locale locale) {
        for (String path : translationCandidates(locale)) {
            BadgeCatalog translation = read(path, context);
            if (translation != null) {
                return translation;
            }
        }
        return null;
    }

    /**
     * Cac ten file ban dich se thu, theo thu tu uu tien.
     */
    static List<String> translationCandidates(Locale locale) {
        List<String> candidates = new ArrayList<>();
        String language = locale.getLanguage();
        if (language == null || language.isEmpty()) return candidates;

        language = language.toLowerCase(Locale.US);
        String country = locale.getCountry();

        if (country != null && !country.isEmpty()) {
            candidates.add(fileFor(language + "-" + country.toLowerCase(Locale.US)));
        }
        candidates.add(fileFor(language));

        int separator = Math.max(language.indexOf('_'), language.indexOf('-'));
        if (separator > 0) {
            candidates.add(fileFor(language.substring(0, separator)));
        }
        return candidates;
    }

    private static String fileFor(String tag) {
        return BADGE_DIR + "/badges-" + tag + ".json";
    }

    private static BadgeCatalog read(String path, Context context) {
        try (InputStream input = context.getAssets().open(path)) {
            return new Gson().fromJson(readText(input), BadgeCatalog.class);
        } catch (IOException ignored) {
            // File khong ton tai - binh thuong voi ban dich theo ngon ngu.
            return null;
        } catch (JsonSyntaxException e) {
            Log.e(TAG, "Badge file is not valid JSON: " + path, e);
            return null;
        }
    }

    private static String readText(InputStream input) throws IOException {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int read;
        while ((read = input.read(buffer)) != -1) {
            output.write(buffer, 0, read);
        }
        return output.toString(StandardCharsets.UTF_8.name());
    }
}
