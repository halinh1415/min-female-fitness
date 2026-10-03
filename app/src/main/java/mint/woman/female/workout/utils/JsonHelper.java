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
import java.util.List;
import java.util.Locale;

public class JsonHelper {
    public final static int VERSION_DAY_PLAN = 7;
    public static JsonHelper mHelper;
    private HashMap<Integer, ExerciseObject> hashMapExercises;
    private HashMap<Integer, ProgramObject> programHashMap;
    private String key = "aaBALAXIbuaa9988";
    Integer[] meals = new Integer[]{R.string.recipes_breakfast, R.string.recipes_snack, R.string.recipes_lunch, R.string.recipes_dinner};
    Integer[] levels = new Integer[]{R.string.beginner, R.string.intermediate, R.string.advanced};
    Context mContext;
    MyPreference myPreference;
    Decode mDecode;
    private int currentDiscover = -1;



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

    public List<ProgramObject> getAllCategory() {
        if (!Decoder.isSuccess){
            return new ArrayList<>();
        }
        String langCode = getLocaleApp();
        String fileName;
        fileName = "programs/all_program_" + langCode ;
        String jsonString = "";
        if (isAssetExists(fileName)) {
            //jsonString = loadJSONFromAsset(fileName);
            byte[] decode = Decoder.decryptFromAssets(mContext.getAssets(), fileName);
            //  jsonString = Decoder.getAllData(mContext.getAssets(), fileName);
            jsonString = new String(decode, StandardCharsets.UTF_8);

        } else {
            // jsonString = loadJSONFromAsset("programs/all_program_en.json");
            byte[] decode = Decoder.decryptFromAssets(mContext.getAssets(), "programs/all_program_en");
            //  jsonString = Decoder.getAllData(mContext.getAssets(), fileName);
            jsonString = new String(decode, StandardCharsets.UTF_8);
        }

        Gson gson = new Gson();
        List<ProgramObject> list = gson.fromJson(jsonString, TypeToken.getParameterized(List.class,ProgramObject.class).getType());
        return list;
    }

    /** Bỏ cache danh sách chương trình (vd sau khi tạo lại plan cá nhân). */
    public void invalidatePrograms() {
        programHashMap = null;
    }

    public HashMap<Integer, ProgramObject> getAllCategoryHashMap() {
        if (!Decoder.isSuccess){
            return new HashMap<>();
        }
        if (programHashMap == null){
            programHashMap = new HashMap<>();
            String langCode = getLocaleApp();
            String fileName;
            fileName = "programs/all_program_" + langCode ;
            String jsonString = "";
            if (isAssetExists(fileName)) {
                //jsonString = loadJSONFromAsset(fileName);
                byte[] decode = Decoder.decryptFromAssets(mContext.getAssets(), fileName);
                //  jsonString = Decoder.getAllData(mContext.getAssets(), fileName);
                jsonString = new String(decode, StandardCharsets.UTF_8);

            } else {
                // jsonString = loadJSONFromAsset("programs/all_program_en.json");
                byte[] decode = Decoder.decryptFromAssets(mContext.getAssets(), "programs/all_program_en");
                //  jsonString = Decoder.getAllData(mContext.getAssets(), fileName);
                jsonString = new String(decode, StandardCharsets.UTF_8);
            }

            Gson gson = new Gson();
            List<ProgramObject> list = gson.fromJson(jsonString, TypeToken.getParameterized(List.class,ProgramObject.class).getType());
            for (ProgramObject pr : list
            ) {
                programHashMap.put(pr.getId(), pr);
            }
            // Plan cá nhân (nếu người dùng đã tạo) được dùng như một thử thách 28 ngày.
            ProgramObject personal = PersonalPlanStore.program(mContext);
            if (personal != null) {
                programHashMap.put(personal.getId(), personal);
            }
        }

        return programHashMap;
    }

    public HashMap<Integer, ExerciseObject> getHashMapExercise(int discover,int version) {
        if (!Decoder.isSuccess){
            return new HashMap<>();
        }
        if (hashMapExercises == null || discover != currentDiscover) {
            hashMapExercises = new HashMap<>();

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
                for (ExerciseObject exe : list
                ) {
                    hashMapExercises.put(exe.getId(), exe);
                }


            } catch (Exception e) {
                e.printStackTrace();
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
            if (baseUrl.contains("custom_plan_new_")) {
                jsonArray = new JSONArray(readFileFromData(fileName));
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
            if (discover == 0) {
                decode = Decoder.decryptFromAssets(mContext.getAssets(), baseUrl);
            } else {
                decode = Decoder.decryptInput( baseUrl);
            }
            json = new String(decode, StandardCharsets.UTF_8);
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
        if (hashMapExercises != null) {
            hashMapExercises.clear();
            hashMapExercises = null;

        }
        if (programHashMap != null) {
            programHashMap.clear();
            programHashMap = null;
        }
    }

}
