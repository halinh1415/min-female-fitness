package mint.woman.female.workout.badge;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.LruCache;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Anh huy hieu do nguoi dung tu bo vao {@code assets/badges/icons/}.
 *
 * <p>Quy uoc dat ten: file ten theo id cua huy hieu thi tu dong duoc nhan, khong can khai bao
 * gi trong JSON. Muon dat ten khac thi ghi ro vao truong {@code icon} trong badges.json.</p>
 *
 * <p>Khong tim thay file nao thi huy hieu ve bang glyph mac dinh.</p>
 */
public final class BadgeIconAssets {

    private static final String[] EXTENSIONS = {"webp", "png", "jpg", "jpeg"};

    /**
     * Ten file trong thu muc icons: khoa la ten viet thuong (de so sanh khong phan biet hoa
     * thuong), gia tri la ten that de mo file. Doc mot lan thay vi mo thu tung file.
     */
    private static volatile Map<String, String> fileNames;

    /** Anh da giai ma, khoa theo ten file + kich thuoc yeu cau. */
    private static final LruCache<String, Bitmap> CACHE = new LruCache<>(48);

    private BadgeIconAssets() {
    }

    /**
     * Tim file anh cho mot huy hieu.
     *
     * @return ten file trong thu muc icons, hoac null neu khong co -> dung glyph mac dinh.
     */
    public static String resolve(Context context, String badgeId, String declaredIcon) {
        Map<String, String> available = fileNames(context);
        if (available.isEmpty()) return null;

        // 1. Ten file ghi ro trong badges.json.
        if (declaredIcon != null && !declaredIcon.trim().isEmpty()) {
            return available.get(declaredIcon.trim().toLowerCase(Locale.US));
        }

        // 2. Quy uoc theo id: ic_<id>.<ext> roi den <id>.<ext>.
        if (badgeId == null || badgeId.trim().isEmpty()) return null;
        String id = badgeId.trim().toLowerCase(Locale.US);

        for (String prefix : new String[]{"ic_", ""}) {
            for (String extension : EXTENSIONS) {
                String match = available.get(prefix + id + "." + extension);
                if (match != null) {
                    return match;
                }
            }
        }
        return null;
    }

    /**
     * Doc anh, thu nho ve dung co can hien thi de khong giu anh 1024px trong bo nho
     * chi de ve mot huy hieu 64dp.
     *
     * @param targetPx canh dai mong muon, tinh bang pixel
     */
    public static Bitmap load(Context context, String fileName, int targetPx) {
        if (fileName == null || fileName.isEmpty()) return null;

        int bucket = Math.max(0, targetPx);
        String key = fileName + "@" + bucket;

        Bitmap cached = CACHE.get(key);
        if (cached != null) return cached;

        String path = BadgeCatalogLoader.ICON_DIR + "/" + fileName;

        BitmapFactory.Options bounds = new BitmapFactory.Options();
        bounds.inJustDecodeBounds = true;
        try (InputStream input = context.getAssets().open(path)) {
            BitmapFactory.decodeStream(input, null, bounds);
        } catch (IOException e) {
            return null;
        }

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inSampleSize = sampleSize(bounds.outWidth, bounds.outHeight, bucket);

        try (InputStream input = context.getAssets().open(path)) {
            Bitmap bitmap = BitmapFactory.decodeStream(input, null, options);
            if (bitmap != null) {
                CACHE.put(key, bitmap);
            }
            return bitmap;
        } catch (IOException e) {
            return null;
        }
    }

    static int sampleSize(int width, int height, int targetPx) {
        int longest = Math.max(width, height);
        // Khong biet kich thuoc anh hoac kich thuoc can ve -> giu nguyen anh, dung doan mo.
        if (longest <= 0 || targetPx <= 0) return 1;

        int sample = 1;
        while (longest / (sample * 2) >= targetPx) {
            sample *= 2;
        }
        return sample;
    }

    /** Doc lai thu muc icons o lan dung tiep theo. */
    public static void invalidate() {
        synchronized (BadgeIconAssets.class) {
            fileNames = null;
            CACHE.evictAll();
        }
    }

    private static Map<String, String> fileNames(Context context) {
        Map<String, String> local = fileNames;
        if (local != null) return local;

        synchronized (BadgeIconAssets.class) {
            if (fileNames != null) return fileNames;

            Map<String, String> names = new HashMap<>();
            try {
                String[] listed = context.getAssets().list(BadgeCatalogLoader.ICON_DIR);
                if (listed != null) {
                    for (String name : listed) {
                        names.put(name.toLowerCase(Locale.US), name);
                    }
                }
            } catch (IOException ignored) {
                // Thu muc icons chua ton tai - binh thuong khi chua co anh nao.
            }

            fileNames = names;
            return fileNames;
        }
    }
}
