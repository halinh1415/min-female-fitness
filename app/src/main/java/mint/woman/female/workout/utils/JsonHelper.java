package mint.woman.female.workout.utils;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;

import com.google.gson.Gson;
import mint.woman.female.workout.activity.ui.plan.PersonalPlanStore;
import com.google.gson.reflect.TypeToken;

import mint.woman.female.workout.R;
import mint.woman.female.workout.model.DietObject;
import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.model.MealObject;
import mint.woman.female.workout.model.PlanObject;
import mint.woman.female.workout.model.ProductObject;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.model.Replace;
import mint.woman.female.workout.model.Rule;

import com.minfitness.decode.Decoder;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class JsonHelper {
    public final static int VERSION_DAY_PLAN = 7;
    public static JsonHelper mHelper;
    /**
     * Danh sách động tác đã giải mã, theo (discover, version, ngôn ngữ). Giữ vài bộ: trước đây chỉ giữ 1 bộ,
     * các màn gọi với version khác nhau (0 / 1003 / version bài) hoặc xen bài Discover với bài có sẵn làm
     * giải mã + parse lại ~200KB trên main thread mỗi lần (ANR khi mở màn tập).
     */
    private final LinkedHashMap<String, HashMap<Integer, ExerciseObject>> exerciseCache =
            new LinkedHashMap<String, HashMap<Integer, ExerciseObject>>(4, 0.75f, true) {
                @Override
                protected boolean removeEldestEntry(Map.Entry<String, HashMap<Integer, ExerciseObject>> eldest) {
                    return size() > 3;
                }
            };
    private HashMap<Integer, ProgramObject> programHashMap;
    private String key = "aaBALAXIbuaa9988";
    Integer[] meals = new Integer[]{R.string.recipes_breakfast, R.string.recipes_snack, R.string.recipes_lunch, R.string.recipes_dinner};
    Integer[] levels = new Integer[]{R.string.beginner, R.string.intermediate, R.string.advanced};
    Context mContext;
    MyPreference myPreference;
    Decode mDecode;



    private JsonHelper(Context context) {
        this.mContext = context;
        this.myPreference = new MyPreference(context);
        this.mDecode = new Decode("by_MATICS_studio", key);
        // this.absExercise = new ArrayList<>();
    }

    public static synchronized JsonHelper  newInstance(Context context) {
        if (mHelper == null) {
            mHelper = new JsonHelper(context);
        }
        return mHelper;
    }

    private String loadJSONFromAsset(String name) {
        String json = null;
        try {
            InputStream is = mContext.getAssets().open(name);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    public String readFileFromData(String fileName) {
        String json = null;
        try {
            InputStream is = mContext.openFileInput(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return json;
    }

    public void writeFileToData(String fileName, String data) {
        myPreference.setVersionPlan(fileName, VERSION_DAY_PLAN);
        try {
            File file = new File(mContext.getFilesDir() + "/" + fileName);

            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter writer = new FileWriter(file);
            writer.append(data);
            writer.flush();
            writer.close();
        } catch (IOException e) {
        }
    }




    public String getLocaleApp() {
        String[] loc = myPreference.getLanguage().split("_");
        Locale locale;
        if (loc.length > 1) {
            locale = new Locale(loc[0], loc[1]);
        } else {
            locale = new Locale(loc[0]);
        }
        if (locale.getLanguage().contains("zh") || locale.getLanguage().contains("in")) {
            return locale.getLanguage() + "_" + locale.getCountry();
        }
        String language = locale.getLanguage();
        return language.substring(0, 2);
    }

    /**
     * Danh sách chương trình chỉ giải mã + parse JSON một lần cho mỗi ngôn ngữ (như Tai Chi).
     * Trước đây getAllCategory() giải mã lại cả file mỗi lần gọi (Home gọi mỗi lần cập nhật chuỗi ngày),
     * và getAllCategoryHashMap() gán map rỗng trước rồi mới đổ dữ liệu, không khoá: luồng nền (Home) và
     * main thread gọi cùng lúc thì giải mã hai lần, main có thể nhận map đang đổ dở / rỗng.
     */
    private final Object programLock = new Object();
    private List<ProgramObject> programList;
    private String programListLang;
    private List<ProgramObject> programHashMapSource;

    private List<ProgramObject> loadProgramList() {
        String langCode = getLocaleApp();
        synchronized (programLock) {
            if (programList != null && langCode.equals(programListLang)) return programList;
            long startNs = System.nanoTime();
            String fileName = "programs/all_program_" + langCode;
            if (!isAssetExists(fileName)) fileName = "programs/all_program_en";
            byte[] decode = Decoder.decryptFromAssets(mContext.getAssets(), fileName);
            String jsonString = new String(decode, StandardCharsets.UTF_8);
            List<ProgramObject> list = new Gson().fromJson(jsonString,
                    TypeToken.getParameterized(List.class, ProgramObject.class).getType());
            programList = list == null ? new ArrayList<>() : list;
            programListLang = langCode;
            android.util.Log.d("JsonHelper", "program catalog decoded in "
                    + (System.nanoTime() - startNs) / 1_000_000 + " ms on " + Thread.currentThread().getName());
            return programList;
        }
    }

    /** Gọi ở luồng nền lúc mở app để Home / Mục tiêu tuần không phải chờ giải mã danh sách bài. */
    public void prewarmPrograms() {
        if (Decoder.isSuccess) getAllCategoryHashMap();
    }

    public List<ProgramObject> getAllCategory() {
        if (!Decoder.isSuccess){
            return new ArrayList<>();
        }
        // Bản sao danh sách để nơi gọi sắp xếp / lọc không ảnh hưởng cache.
        return new ArrayList<>(loadProgramList());
    }

    /** Bỏ cache danh sách chương trình (vd sau khi tạo lại plan cá nhân). */
    public void invalidatePrograms() {
        synchronized (programLock) {
            programHashMap = null;
        }
    }

    public HashMap<Integer, ProgramObject> getAllCategoryHashMap() {
        if (!Decoder.isSuccess){
            return new HashMap<>();
        }
        synchronized (programLock) {
            List<ProgramObject> list = loadProgramList();
            // Map theo cùng ngôn ngữ với danh sách: lần mở đầu có thể đã giải mã sẵn trước khi người dùng chọn ngôn ngữ.
            if (programHashMap != null && list == programHashMapSource) return programHashMap;
            HashMap<Integer, ProgramObject> map = new HashMap<>();
            for (ProgramObject pr : list) {
                map.put(pr.getId(), pr);
            }
            // Plan cá nhân (nếu người dùng đã tạo) được dùng như một thử thách 28 ngày.
            ProgramObject personal = PersonalPlanStore.program(mContext);
            if (personal != null) {
                map.put(personal.getId(), personal);
            }
            programHashMap = map;
            programHashMapSource = list;
            return map;
        }
    }

    public synchronized HashMap<Integer, ExerciseObject> getHashMapExercise(int discover,int version) {
        if (!Decoder.isSuccess){
            return new HashMap<>();
        }
        String langCode = getLocaleApp();
        // Bài có sẵn (discover 0) đọc từ assets, không phụ thuộc version.
        String cacheKey = (discover == 0 ? "0" : discover + "/" + version) + "/" + langCode;
        HashMap<Integer, ExerciseObject> hashMapExercises = exerciseCache.get(cacheKey);
        if (hashMapExercises == null) {
            HashMap<Integer, ExerciseObject> loadedExercises = new HashMap<>();
            String fileName;
            String baseUrl;
            if (discover == 0) {
                baseUrl = "i/language/";
            } else {
                baseUrl = mContext.getFilesDir().getPath() + "/discover/" + discover+"/"+version+"/" + "/language/";
            }
            fileName = baseUrl + langCode;
            String jsonString = "";

            try {
                if (discover == 0) {
                    if (isAssetExists(fileName)) {
                        byte[] decode = Decoder.decryptFromAssets(mContext.getAssets(), fileName);
                        jsonString = new String(decode, StandardCharsets.UTF_8);
                    } else {
                        byte[] decode = Decoder.decryptFromAssets(mContext.getAssets(), baseUrl + "en");
                        jsonString = new String(decode, StandardCharsets.UTF_8);
                    }
                } else {
                    if (existsData(fileName)) {
                        byte[] decode = Decoder.decryptInput(fileName);
                        jsonString = new String(decode, StandardCharsets.UTF_8);
                    } else {
                        byte[] decode = Decoder.decryptInput(baseUrl + "en");
                        jsonString = new String(decode, StandardCharsets.UTF_8);
                    }
                }
                Gson gson = new Gson();
                List<ExerciseObject> list = gson.fromJson(jsonString, TypeToken.getParameterized(List.class,ExerciseObject.class).getType());
                if (list == null || list.isEmpty()) {
                    return new HashMap<>();
                }
                for (ExerciseObject exe : list) {
                    if (exe != null) loadedExercises.put(exe.getId(), exe);
                }
                if (loadedExercises.isEmpty()) return loadedExercises;
                // Publish only a successfully loaded catalog; failed loads must remain retryable.
                hashMapExercises = loadedExercises;
                exerciseCache.put(cacheKey, loadedExercises);
            } catch (Exception e) {
                e.printStackTrace();
                return new HashMap<>();
            }
        }

//        Gson gson = new Gson();
//        list = gson.fromJson(decryptJson, new TypeToken<List<ExerciseObject>>() {}.getType());

        return hashMapExercises;
    }


    public List<ExerciseObject> getAllExercise(int discover,int version) {
        List<ExerciseObject> list = new ArrayList<>();
        if (!Decoder.isSuccess){
            return new ArrayList<>();
        }
        String langCode = getLocaleApp();
        String fileName;
        String baseUrl;
        if (discover == 0) {
            baseUrl = "i/language/";
        } else {
            baseUrl = mContext.getFilesDir().getPath() + "/discover/" + discover+"/"+version+"/" + "/language/";
        }
        fileName = baseUrl + langCode;
        String jsonString = "";
        if (discover == 0) {
            if (isAssetExists(fileName)) {
                byte[] decode = Decoder.decryptFromAssets(mContext.getAssets(), fileName);
                //  jsonString = Decoder.getAllData(mContext.getAssets(), fileName);
                jsonString = new String(decode, StandardCharsets.UTF_8);
            } else {
                byte[] decode = Decoder.decryptFromAssets(mContext.getAssets(), baseUrl + "en");
                jsonString = new String(decode, StandardCharsets.UTF_8);
            }
        } else {
            if (existsData(fileName)) {
                byte[] decode = Decoder.decryptInput(fileName);
                jsonString = new String(decode, StandardCharsets.UTF_8);
            } else if (existsData(baseUrl + "en")){
                byte[] decode = Decoder.decryptInput(baseUrl + "en");
                jsonString = new String(decode, StandardCharsets.UTF_8);
            }else{
                return null;
            }
        }


        Gson gson = new Gson();
        return gson.fromJson(jsonString, TypeToken.getParameterized(List.class,ExerciseObject.class).getType());
//        Gson gson = new Gson();
//        list = gson.fromJson(decryptJson, new TypeToken<List<ExerciseObject>>() {}.getType());

    }




    public List<PlanObject> getListPlan(String fileName, int discover,int version) {
        if (!Decoder.isSuccess){
            return new ArrayList<>();
        }
        List<PlanObject> list = new ArrayList<>();
        try {
            JSONArray jsonArray;
            String baseUrl;
            if (discover == 0) {
                baseUrl = "i/" + fileName;
            } else {
                baseUrl = mContext.getFilesDir().getPath() + "/discover/" + discover+"/"+version+"/workout";
            }
            // Lịch người dùng tự tạo / đã sửa (filesDir/custom, mã hoá) được ưu tiên hơn bản trong assets.
            String saved = discover == 0 && !baseUrl.contains("custom_plan_new_")
                    ? CustomPlanFiles.read(mContext, fileName) : null;
            if (baseUrl.contains("custom_plan_new_")) {
                jsonArray = new JSONArray(readFileFromData(fileName));
            } else if (saved != null) {
                jsonArray = new JSONArray(saved);
            } else if (CustomPlanFiles.isUserPlan(fileName)) {
                // Bài tự tạo chưa có file (lỗi ghi cũ): coi như lịch trống thay vì tìm trong assets.
                jsonArray = new JSONArray();
            } else if (discover == 0) {
                if (!existsData(baseUrl) || myPreference.getVersionPlan(fileName) < VERSION_DAY_PLAN) {
                    byte[] decode = Decoder.decryptFromAssets(mContext.getAssets(), baseUrl);
                    String json = new String(decode, StandardCharsets.UTF_8);
                    jsonArray = new JSONArray(json);

                } else {
                    byte[] decode = Decoder.decryptInput(baseUrl);
                    String json = new String(decode, StandardCharsets.UTF_8);
                    jsonArray = new JSONArray(json);
                }
            } else {
                byte[] decode = Decoder.decryptInput(baseUrl);
                String json = new String(decode, StandardCharsets.UTF_8);
                jsonArray = new JSONArray(json);
            }
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                PlanObject planObject = new PlanObject();
                planObject.mNextActionId = i * 25;
                planObject.name = jsonObject.getString("name");

                if (jsonObject.has("rule")) {
                    int type = jsonObject.getJSONObject("rule").getInt("type");
                    int value = jsonObject.getJSONObject("rule").getInt("value");
                    planObject.setRule( new Rule(type,value));
                }
                JSONArray subArr = jsonObject.getJSONArray("exercise");
                for (int j = 0; j < subArr.length(); j++) {
                    JSONObject subObj = subArr.getJSONObject(j);
                    int id = subObj.getInt("actionId");
                    int time = subObj.getInt("time");
                    int rest = 10;
                    if (subObj.has("rest")) {
                         rest = subObj.getInt("rest");
                    }
                    String unit ="";
                    if (subObj.has("unit")) {
                         unit = subObj.getString("unit");
                    }
                    int actype = 0;
                    if (subObj.has("actype")) {
                        actype = subObj.getInt("actype");
                    }

                    planObject.addNewAction(new PlanObject.ActionObject(id, time, unit, actype, rest));
                }
                list.add(planObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<PlanObject> getResetPlan(String fileName, int discover,int version) {
        List<PlanObject> list = new ArrayList<>();
        try {
            String baseUrl;
            if (discover == 0) {
                baseUrl = "i/" + fileName;
            } else {
                baseUrl = mContext.getFilesDir().getPath() + "/discover/" + discover+"/"+version+"/workout";
            }
            String json = "";
            byte[] decode;
            if (CustomPlanFiles.isUserPlan(fileName)) {
                // Bài tự tạo không có bản gốc trong assets: "đặt lại" = bản đã lưu.
                String saved = CustomPlanFiles.read(mContext, fileName);
                json = saved == null ? "[]" : saved;
            } else {
                if (discover == 0) {
                    decode = Decoder.decryptFromAssets(mContext.getAssets(), baseUrl);
                } else {
                    decode = Decoder.decryptInput( baseUrl);
                }
                json = new String(decode, StandardCharsets.UTF_8);
            }
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                PlanObject planObject = new PlanObject();
                planObject.name = jsonObject.getString("name");
                planObject.mNextActionId = i * 25;
//                if (jsonObject.has("focus")) {
//                    planObject.focus = jsonObject.getString("focus");
//                }
//                planObject.actionCount = jsonObject.getInt("actionCount");
                //  planObject.duration = jsonObject.getInt("duration");
                JSONArray subArr = jsonObject.getJSONArray("exercise");
                for (int j = 0; j < subArr.length(); j++) {
                    JSONObject subObj = subArr.getJSONObject(j);
                    int id = subObj.getInt("actionId");
                    int time = subObj.getInt("time");
                    int rest= 10;
                    if (subObj.has("rest")) {
                        rest = subObj.getInt("rest");
                    }
                    String unit ="";
                    if (subObj.has("unit")) {
                        unit = subObj.getString("unit");
                    }
                    int actype = 0;
                    if (subObj.has("actype")) {
                        actype = subObj.getInt("actype");
                    }
                    planObject.addNewAction(new PlanObject.ActionObject(id, time, unit, actype, rest));
                }
                list.add(planObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<DietObject> getListDiet() {
        List<DietObject> dietObjectList = new ArrayList<>();

        try {
            String langCode = myPreference.getLanguage();
            String fileName = "";
            String jsonString = "";
            if (myPreference.isStandardDiet()) {
                if (langCode.length() > 2) {
                    fileName = "foods/diet_standard_" + langCode.substring(0, 2) + ".json";
                } else {
                    fileName = "foods/diet_standard_" + Locale.getDefault().getLanguage().toLowerCase() + ".json";
                }
                if (isAssetExists(fileName)) {
                    jsonString = loadJSONFromAsset(fileName);
                } else {
                    jsonString = loadJSONFromAsset("foods/diet_standard_en.json");
                }
            } else {
                if (langCode.length() > 2) {
                    fileName = "foods/diet_vegetarian_" + langCode.substring(0, 2) + ".json";
                } else {
                    fileName = "foods/diet_vegetarian_" + Locale.getDefault().getLanguage().toLowerCase() + ".json";
                }
                if (isAssetExists(fileName)) {
                    jsonString = loadJSONFromAsset(fileName);
                } else {
                    jsonString = loadJSONFromAsset("foods/diet_vegetarian_en.json");
                }
            }

            String decryptJson = mDecode.decrypt(jsonString);
            JSONArray jsonArray = new JSONArray(decryptJson);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                DietObject dietObject = new DietObject();
                dietObject.day = mContext.getString(R.string.txt_day) + " " + (i + 1);
                JSONArray jsonMeals = jsonObject.getJSONArray("meals");


                for (int j = 0; j < jsonMeals.length(); j++) {
                    JSONObject jsMealObject = jsonMeals.getJSONObject(j);
                    MealObject mealObject = new MealObject();
                    mealObject.name = mContext.getString(meals[j]);
                    JSONArray jsRecipes = jsMealObject.getJSONArray("recipes");
                    for (int k = 0; k < jsRecipes.length(); k++) {
                        String recipe = jsRecipes.getString(k);
                        mealObject.recipes.add(recipe);
                    }
                    dietObject.meals.add(mealObject);
                }
                dietObjectList.add(dietObject);
            }

        } catch (JSONException e) {
            e.printStackTrace();
        }
        return dietObjectList;
    }

    private boolean isAssetExists(String pathInAssetsDir) {
        AssetManager assetManager = mContext.getResources().getAssets();
        InputStream inputStream = null;
        try {
            inputStream = assetManager.open(pathInAssetsDir);
            if (null != inputStream) {
                inputStream.close();
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean existsData(String fileName) {
        File file = new File(fileName);
        return file.exists();

    }


    public List<ProductObject> getListProducts(boolean typeProduct) {
        List<ProductObject> productObjects = new ArrayList<>();
        String langCode = myPreference.getLanguage();
        String fileName;
        String jsonString;
        int id = 0;
        if (typeProduct) {
            if (langCode.length() > 2) {
                fileName = "foods/product_list_standard" + langCode.substring(0, 2) + ".json";
            } else {
                fileName = "foods/product_list_standard" + Locale.getDefault().getLanguage().toLowerCase() + ".json";
            }
            if (isAssetExists(fileName)) {
                jsonString = loadJSONFromAsset(fileName);
            } else {
                jsonString = loadJSONFromAsset("foods/product_list_standard_en.json");
            }
        } else {
            id = 10;
            if (langCode.length() > 2) {
                fileName = "foods/product_list_vegetarian_" + langCode.substring(0, 2) + ".json";
            } else {
                fileName = "foods/product_list_vegetarian_" + Locale.getDefault().getLanguage().toLowerCase() + ".json";
            }
            if (isAssetExists(fileName)) {
                jsonString = loadJSONFromAsset(fileName);
            } else {
                jsonString = loadJSONFromAsset("foods/product_list_vegetarian_en.json");
            }
        }
        try {
            JSONArray productArr = new JSONArray(mDecode.decrypt(jsonString));
            for (int i = 0; i < productArr.length(); i++) {
                JSONObject jsPrObject = productArr.getJSONObject(i);
                JSONArray jsonArray = jsPrObject.getJSONArray("products");
                ProductObject prdObject = new ProductObject();
                prdObject.id = id;
                id++;
                for (int j = 0; j < jsonArray.length(); j++) {
                    prdObject.products.add(jsonArray.getString(j));
                    id++;
                }
                productObjects.add(prdObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return productObjects;
    }
    public void resetData() {
        synchronized (this) {
            exerciseCache.clear();
        }
        synchronized (programLock) {
            programHashMap = null;
            programList = null;
        }
    }

}
