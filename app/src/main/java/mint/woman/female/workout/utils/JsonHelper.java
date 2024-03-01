package mint.woman.female.workout.utils;

import android.content.Context;
import android.content.res.AssetManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import mint.woman.female.workout.R;
import mint.woman.female.workout.model.DietObject;
import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.model.MealObject;
import mint.woman.female.workout.model.PlanObject;
import mint.woman.female.workout.model.ProductObject;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.model.Replace;

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
import java.util.List;
import java.util.Locale;

public class JsonHelper {
    public final static int VERSION_DAY_PLAN = 7;
    public static JsonHelper mHelper;
    Integer[] meals = new Integer[]{R.string.txt_breakfast, R.string.txt_snack, R.string.txt_lunch, R.string.txt_dinner};
    Integer[] levels = new Integer[]{R.string.txt_beginner, R.string.txt_intermediate, R.string.txt_advanced};
    Context mContext;
    MyPreference myPreference;
    Decode mDecode;

    public JsonHelper(Context context) {
        this.mContext = context;
        this.myPreference = new MyPreference(context);
    }

    public JsonHelper(Context context, String key) {
        this.mContext = context;
        this.myPreference = new MyPreference(context);
        this.mDecode = new Decode("by_MATICS_studio", key);
        // this.absExercise = new ArrayList<>();
    }

    public static JsonHelper newInstance(Context context, String key) {
        if (mHelper == null) {
            mHelper = new JsonHelper(context, key);
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
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
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

    public void copyPlanToData(String fileName) {
        writeFileToData(fileName, loadJSONFromAsset("data/" + fileName));
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


    public HashMap<Integer, ProgramObject> getAllCategoryHashMap() {
        HashMap<Integer, ProgramObject> hashMap = new HashMap<>();

        String langCode = getLocaleApp();
        String fileName;
        fileName = "programs/all_program_" + langCode + ".json";
        String jsonString = "";
        if (isAssetExists(fileName)) {
            jsonString = loadJSONFromAsset(fileName);
        } else {
            jsonString = loadJSONFromAsset("programs/all_program_en.json");
        }

        Gson gson = new Gson();
        List<ProgramObject> list = gson.fromJson(jsonString, new TypeToken<List<ProgramObject>>() {
        }.getType());
        for (ProgramObject pr : list
        ) {
            hashMap.put(pr.getId(), pr);
        }

        return hashMap;
    }

    public HashMap<Integer, ExerciseObject> getHashMapExercise(int discover) {
        HashMap<Integer, ExerciseObject> hashMap = new HashMap<>();

        String langCode = getLocaleApp();
        String fileName;
        String baseUrl;
        if (discover == 0) {
            baseUrl = "i/language/";
        } else {
            baseUrl = mContext.getFilesDir().getPath() + "/discover/" + discover + "/language/";
        }
        fileName = baseUrl + langCode;
        String jsonString = "";

//            String[] urls = mContext.getResources().getStringArray(R.array.arr_vi_di);
        //  String decryptJson = mDecode.decrypt(jsonString);

        try {
            if (discover == 0) {
                if (isAssetExists(fileName)) {
                    byte[] decode = VideoEncryptor.decryptGif(mContext.getAssets(), fileName, "DesignMinFitness");
                    jsonString = new String(decode, StandardCharsets.UTF_8);
                } else {
                    byte[] decode = VideoEncryptor.decryptGif(mContext.getAssets(), baseUrl + "en", "DesignMinFitness");
                    jsonString = new String(decode, StandardCharsets.UTF_8);
                }
            } else {
                if (existsData(fileName)) {
                    byte[] decode = VideoEncryptor.decryptInput(fileName, "DesignMinFitness");
                    jsonString = new String(decode, StandardCharsets.UTF_8);
                } else {
                    byte[] decode = VideoEncryptor.decryptInput(baseUrl + "en", "DesignMinFitness");
                    jsonString = new String(decode, StandardCharsets.UTF_8);
                }
            }
            Gson gson = new Gson();
            List<ExerciseObject> list = gson.fromJson(jsonString, new TypeToken<List<ExerciseObject>>() {
            }.getType());
            for (ExerciseObject exe : list
            ) {
                hashMap.put(exe.getId(), exe);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }
//        Gson gson = new Gson();
//        list = gson.fromJson(decryptJson, new TypeToken<List<ExerciseObject>>() {}.getType());

        return hashMap;
    }


    public List<ExerciseObject> getAllExercise(int discover) {
        List<ExerciseObject> list = new ArrayList<>();

        String langCode = getLocaleApp();
        String fileName;
        String baseUrl;
        if (discover == 0) {
            baseUrl = "i/language/";
        } else {
            baseUrl = "/discover/" + discover + "/language/";
        }
        fileName = baseUrl + langCode;
        String jsonString = "";
        if (discover == 0) {
            if (isAssetExists(fileName)) {
                byte[] decode = VideoEncryptor.decryptGif(mContext.getAssets(), fileName, "DesignMinFitness");
                jsonString = new String(decode, StandardCharsets.UTF_8);
            } else {
                byte[] decode = VideoEncryptor.decryptGif(mContext.getAssets(), baseUrl + "en", "DesignMinFitness");
                jsonString = new String(decode, StandardCharsets.UTF_8);
            }
        } else {
            if (existsData(fileName)) {
                byte[] decode = VideoEncryptor.decryptInput(fileName, "DesignMinFitness");
                jsonString = new String(decode, StandardCharsets.UTF_8);
            } else {
                byte[] decode = VideoEncryptor.decryptInput(baseUrl + "en", "DesignMinFitness");
                jsonString = new String(decode, StandardCharsets.UTF_8);
            }
        }


        Gson gson = new Gson();
        return gson.fromJson(jsonString, new TypeToken<List<ExerciseObject>>() {
        }.getType());
//        Gson gson = new Gson();
//        list = gson.fromJson(decryptJson, new TypeToken<List<ExerciseObject>>() {}.getType());

    }


    public List<Replace> getGroupList(int id) {
        List<Replace> list = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(loadJSONFromAsset("js/g_" + id + ".json"));
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                Replace replace = new Replace(jsonObject.getInt("id"), jsonObject.getInt("maxValue"), jsonObject.getInt("minValue"));
                list.add(replace);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<PlanObject> getListPlan(String fileName, int discover) {
        List<PlanObject> list = new ArrayList<>();
        try {
            JSONArray jsonArray;
            String baseUrl;
            if (discover == 0) {
                baseUrl = "i/" + fileName;
            } else {
                baseUrl = "/discover/" + discover + "/workout";
            }
            if (baseUrl.contains("custom_plan_")) {
                jsonArray = new JSONArray(readFileFromData(fileName));
            } else if (discover == 0) {
                if (!existsData(mContext.getFilesDir().getPath() + "/" + baseUrl) || myPreference.getVersionPlan(fileName) < VERSION_DAY_PLAN) {

                    byte[] decode = VideoEncryptor.decryptGif(mContext.getAssets(), baseUrl, "DesignMinFitness");
                    jsonArray = new JSONArray(new String(decode, StandardCharsets.UTF_8));

                } else {
                    byte[] decode = VideoEncryptor.decryptInput(mContext.getFilesDir() + "/" + baseUrl, "DesignMinFitness");
                    jsonArray = new JSONArray(new String(decode, StandardCharsets.UTF_8));
                }
            } else {
                byte[] decode = VideoEncryptor.decryptInput(mContext.getFilesDir() + baseUrl, "DesignMinFitness");
                jsonArray = new JSONArray(new String(decode, StandardCharsets.UTF_8));
            }
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                PlanObject planObject = new PlanObject();
                planObject.mNextActionId = i * 25;
                planObject.name = jsonObject.getString("name");
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
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    public List<PlanObject> getResetPlan(String fileName, int discover) {
        List<PlanObject> list = new ArrayList<>();
        try {
            String baseUrl;
            if (discover == 0) {
                baseUrl = "i/";
            } else {
                baseUrl = "d/" + discover + "/";
            }
            byte[] decode = VideoEncryptor.decryptGif(mContext.getAssets(), baseUrl + fileName, "DesignMinFitness");
            JSONArray jsonArray = new JSONArray(new String(decode, StandardCharsets.UTF_8));
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

}
