package mint.woman.female.workout.utils;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.LocaleList;

import androidx.annotation.RequiresApi;
import androidx.preference.PreferenceManager;

import java.util.Locale;

public class LanguageHelper {

    private static final String SELECTED_LANGUAGE = "Language.Helper.Selected.Language";

    /** Mã ngôn ngữ app có dữ liệu bài tập (giống mã ở màn chọn ngôn ngữ). */
    public static final String[] SUPPORTED_LANGUAGES = {"en", "de", "fr", "es", "pt", "zh_CN", "zh_TW", "it", "pl", "nl", "ja", "ko", "tr", "ar", "in_ID", "ru", "vi"};

    /**
     * Khớp ngôn ngữ máy (theo thứ tự trong Cài đặt máy) với mã ngôn ngữ app, dùng khi người dùng chưa chọn
     * ngôn ngữ (bỏ qua màn ngôn ngữ): zh-Hant/zh-TW/zh-HK → zh_TW, zh → zh_CN, id/in → in_ID, pt-BR → pt…
     * Trả null nếu máy không có ngôn ngữ nào app hỗ trợ.
     */
    public static String matchSystemLanguage() {
        LocaleList locales = Resources.getSystem().getConfiguration().getLocales();
        for (int i = 0; i < locales.size(); i++) {
            Locale l = locales.get(i);
            String lang = l.getLanguage();
            if ("id".equals(lang)) lang = "in";
            String region = l.getCountry();
            if ("zh".equals(lang)) {
                boolean hant = "Hant".equals(l.getScript()) || "TW".equals(region) || "HK".equals(region) || "MO".equals(region);
                region = hant ? "TW" : "CN";
            }
            String full = lang + "_" + region;
            for (String code : SUPPORTED_LANGUAGES) {
                if (code.equalsIgnoreCase(full)) return code;
            }
            for (String code : SUPPORTED_LANGUAGES) {
                if (code.split("_")[0].equalsIgnoreCase(lang)) return code;
            }
        }
        return null;
    }

    public static Context onAttach(Context context) {
        String lang = getPersistedData(context, Locale.getDefault().getLanguage());
        return setLanguage(context, lang);
    }

    public static Context onAttach(Context context, String defaultLanguage) {
        String lang = getPersistedData(context, defaultLanguage);
        return setLanguage(context, lang);
    }

    public static String getLanguage(Context context) {
        return getPersistedData(context, Locale.getDefault().getLanguage());
    }

    public static Context setLanguage(Context context, String language) {
        if ("zh".equalsIgnoreCase(language)) {
            // attachBaseContext cắt mã còn "zh" (mất vùng) → giao diện luôn giản thể; giữ zh_CN/zh_TW cho khớp dữ liệu bài tập.
            String stored = PreferenceManager.getDefaultSharedPreferences(context).getString("ST_LANGUAGE", "");
            String full = stored.startsWith("zh_") ? stored : matchSystemLanguage();
            if (full != null && full.startsWith("zh_")) language = full;
        }
        persist(context, language);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return updateResources(context, language);
        }


        return updateResourcesLegacy(context, language);
    }

    public static String getPersistedData(Context context, String defaultLanguage) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        return preferences.getString(SELECTED_LANGUAGE, defaultLanguage);
    }

    private static void persist(Context context, String language) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(SELECTED_LANGUAGE, language);
        editor.apply();
    }

    @RequiresApi(api = Build.VERSION_CODES.N)
    private static Context updateResources(Context context, String language) {

        String[] loc = language.split("[_-]");
        Locale locale;
        if (loc.length > 1) {
            locale = new Locale(loc[0], loc[1]);
        } else {
            locale = new Locale(loc[0]);
        }

        Configuration configuration = new Configuration();

        LocaleList localeList = new LocaleList(locale);
        LocaleList.setDefault(localeList);
        configuration.setLayoutDirection(locale);
        configuration.setLocales(localeList);

        return context.createConfigurationContext(configuration);
    }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    private static Context updateResourcesLocale(Context context, Locale locale) {
        Configuration configuration = new Configuration(context.getResources().getConfiguration());
        configuration.setLocale(locale);
        return context.createConfigurationContext(configuration);
    }

    private static Context updateResourcesLegacy(Context context, String language) {

        String[] loc = language.split("[_-]");
        Locale locale;
        if (loc.length > 1) {
            locale = new Locale(loc[0], loc[1]);
        } else {
            locale = new Locale(loc[0]);
        }
        Locale.setDefault(locale);

        Resources resources = context.getResources();

        Configuration configuration = new Configuration();
        configuration.locale = locale;

        resources.updateConfiguration(configuration, resources.getDisplayMetrics());

        return context;
    }
}