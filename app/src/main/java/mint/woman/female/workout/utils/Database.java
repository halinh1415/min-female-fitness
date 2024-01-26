package mint.woman.female.workout.utils;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.preference.PreferenceManager;
import android.util.Log;

import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.model.ReminderItem;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

@SuppressLint("Range")
public class Database {
    private static Database mainDb;
    MyPreference myPreference;
    private String pkgName;
    private String dbName;
    private int dbVersion;
    private String folderPath;
    private String filePath;
    private Context mContext;
    private SQLiteDatabase mDatabase;
    private SQLiteOpenHelper mOpenHelper;

    private Database(Context context, String dbName) {
        this.mContext = context;
        this.dbName = dbName;
        this.pkgName = this.mContext.getPackageName();
        this.folderPath = "data/data/" + this.pkgName + "/databases";
        this.filePath = this.folderPath + "/" + this.dbName;
        this.dbVersion = 1;
        copyDbFromAsset(this.mContext, this.dbName);
        this.mOpenHelper = new SQLiteOpenHelper(this.mContext, this.dbName, null, this.dbVersion) {
            @Override
            public void onCreate(SQLiteDatabase sqLiteDatabase) {
            }

            @Override
            public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
                Log.d("HAHA", "onUpgrade!!! old =" + oldVersion + "new =" + newVersion);
                switch (oldVersion) {
                    case 1:
                    case 2:
                    case 3:
                    case 20:
                    case 21:

                }
            }
        };
        this.myPreference = new MyPreference(context);
        open();
    }

    public static Database newInstance(Context context, String name) {
        if (mainDb == null)
            mainDb = new Database(context, name);
        return mainDb;
    }


    private void copyDbFromAsset(Context context, String name) {
        SharedPreferences localSPR = PreferenceManager.getDefaultSharedPreferences(context);
        if (!hasDatabase(name)) {
            copyDatabaseFromAsset();
            Log.d("HAHA", "copyDbFromAsset!!! ->to =" + dbVersion);
            SharedPreferences.Editor sprEdit = localSPR.edit();
            sprEdit.putInt("db_version", this.dbVersion);
            sprEdit.apply();
        }
    }

    private void open() {
        this.mDatabase = this.mOpenHelper.getWritableDatabase();
    }

    public void close() {
        if (this.mDatabase != null && this.mDatabase.isOpen()) {
            this.mDatabase.close();
        }
        this.mOpenHelper.close();
        mainDb = null;
    }

    private void copyDatabaseFromAsset() {
        try {
            InputStream myInput = mContext.getAssets().open(dbName);
            File f = new File(this.folderPath);
            if (!f.exists())
                f.mkdir();
            OutputStream myOutput = new FileOutputStream(filePath);
            byte[] buffer = new byte[1024];
            int length;
            while ((length = myInput.read(buffer)) > 0) {
                myOutput.write(buffer, 0, length);
            }
            myOutput.flush();
            myOutput.close();
            myInput.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private boolean hasDatabase(String dbName) {
        return new File("data/data/" + this.pkgName + "/databases/" + dbName).exists();
    }


    @SuppressLint("Range")
    public List<Integer> getMainPlan() {
        List<Integer> listProgram = new ArrayList<>();
        try {
            Cursor cursor = this.mDatabase.rawQuery("select * from main_program", null);
            boolean has = cursor.moveToFirst();
            if (has) {
                do {
                    listProgram.add(cursor.getInt(cursor.getColumnIndex("plan")));
                }
                while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return listProgram;
    }

    public void removePlan(int planId) {
        this.mDatabase.delete("main_program", "plan = " + planId, null);
    }

    public boolean hasPlanInMain(int planId) {
        Cursor cursor = this.mDatabase.rawQuery("select * from main_program where plan = " + planId, null);
        return cursor.moveToFirst();
    }

    public int insertPlan(int planId) {
        removePlan(planId);
        ContentValues contentValues = new ContentValues();
        contentValues.put("plan", planId);
        long r = this.mDatabase.insert("main_program", null, contentValues);
        return (int) r;

    }


    public List<ReminderItem> getReminder() {
        List<ReminderItem> list = new ArrayList<>();
        try {
            Cursor cursor = this.mDatabase.rawQuery("select * from reminder ", null);
            boolean has = cursor.moveToFirst();
            if (has) {
                do {
                    ReminderItem reminderItem = new ReminderItem();
                    reminderItem.id = cursor.getInt(cursor.getColumnIndex("id"));
                    reminderItem.time = cursor.getString(cursor.getColumnIndex("time"));
                    reminderItem.repeat = cursor.getInt(cursor.getColumnIndex("repeat"));
                    reminderItem.active = cursor.getInt(cursor.getColumnIndex("active"));
                    // reminderItem.active = myDB.getReminderStatus(reminderItem.id);
                    list.add(reminderItem);
                }
                while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }
    public String getReminderSet(){
        String reminderSet ="";
        try {
            Cursor cursor = this.mDatabase.rawQuery("select * from reminder ", null);
            boolean has = cursor.moveToFirst();
            if (has) {
                do {
                    ReminderItem reminderItem = new ReminderItem();
                    reminderItem.id = cursor.getInt(cursor.getColumnIndex("id"));
                    reminderItem.time = cursor.getString(cursor.getColumnIndex("time"));
                    reminderItem.repeat = cursor.getInt(cursor.getColumnIndex("repeat"));
                    reminderItem.active = cursor.getInt(cursor.getColumnIndex("active"));
                    // reminderItem.active = myDB.getReminderStatus(reminderItem.id);
                    reminderSet =reminderSet+ ", "+reminderItem.time;
                }
                while (cursor.moveToNext());
                cursor.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return reminderSet.replaceFirst(",","");
    }

    public void deleteReminder(int id) {
        this.mDatabase.delete("reminder", "id = " + id, null);
    }

    public int insertReminder(ReminderItem reminderItem) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("time", reminderItem.time);
        contentValues.put("active", reminderItem.active);
        contentValues.put("repeat", reminderItem.repeat);
        long r = this.mDatabase.insert("reminder", null, contentValues);
        return (int) r;

    }

    public void updateReminder(ReminderItem reminderItem) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("time", reminderItem.time);
        contentValues.put("active", reminderItem.active);
        contentValues.put("repeat", reminderItem.repeat);
        this.mDatabase.update("reminder", contentValues, "id = " + reminderItem.id, null);
    }

    public void updateActiveReminder(int id, int active) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("active", active);
        this.mDatabase.update("reminder", contentValues, "id = " + id, null);
    }


    public void deleteUserData() {
        this.mDatabase.execSQL("DELETE FROM user_status");
    }

    public void deleteAllWorkoutHistory() {
        this.mDatabase.execSQL("DELETE FROM workout");
    }

    @SuppressLint("Range")
    public List<ProgramObject> getMyWorkoutList() {
        List<ProgramObject> list = new ArrayList<>();
        try {
            Cursor cursor = this.mDatabase.rawQuery("select * from my_workout where type = 3", null);
            boolean has = cursor.moveToFirst();
            if (has) {
                do {
                    ProgramObject myWorkoutObject = new ProgramObject();
                    myWorkoutObject.setName(cursor.getString(cursor.getColumnIndex("name")));
                    myWorkoutObject.setPlan(cursor.getString(cursor.getColumnIndex("plan")));
                    myWorkoutObject.setId(cursor.getInt(cursor.getColumnIndex("id")));
                    myWorkoutObject.setTotal(cursor.getInt(cursor.getColumnIndex("total")));
                    myWorkoutObject.setImage(cursor.getString(cursor.getColumnIndex("image")));
                    myWorkoutObject.setType(3);
                    myWorkoutObject.setLevel(1);
                    myWorkoutObject.setPremium(0);
                    list.add(myWorkoutObject);
                }
                while (cursor.moveToNext());
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return list;
    }

    @SuppressLint("Range")
    public ProgramObject getMyWorkoutObject(int id) {
        ProgramObject myWorkoutObject = new ProgramObject();
        try {
            Cursor cursor = this.mDatabase.rawQuery("select * from my_workout where id =" + id, null);
            boolean has = cursor.moveToFirst();
            if (has) {
                myWorkoutObject.setName(cursor.getString(cursor.getColumnIndex("name")));
                myWorkoutObject.setPlan(cursor.getString(cursor.getColumnIndex("plan")));
                myWorkoutObject.setId(cursor.getInt(cursor.getColumnIndex("id")));
                myWorkoutObject.setTotal(cursor.getInt(cursor.getColumnIndex("total")));
                myWorkoutObject.setImage(cursor.getString(cursor.getColumnIndex("image")));
                myWorkoutObject.setType(3);
                myWorkoutObject.setLevel(1);
                myWorkoutObject.setPremium(0);
            }
            cursor.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return myWorkoutObject;
    }


    public void updateMyWorkout(ProgramObject myWorkoutObject) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("data", myWorkoutObject.getPlan());
        this.mDatabase.update("my_workout", contentValues, "id = " + myWorkoutObject.getId(), null);
    }
    public int getNewId(){
        int newId = 0;
        try {
            Cursor getNoteId = mDatabase.rawQuery("select id from my_workout",null);
            return getNoteId.getColumnIndex("id")+1;
        }catch (Exception e){
            e.printStackTrace();
        }
        return newId;
    }

    public int insertMyWorkout(ProgramObject myWorkoutObject) {
        ContentValues contentValues = new ContentValues();
        contentValues.put("name", myWorkoutObject.getName());
        contentValues.put("plan", myWorkoutObject.getPlan());
        contentValues.put("total", myWorkoutObject.getTotal());
        contentValues.put("image", myWorkoutObject.getImage());
        contentValues.put("type", 3);
        contentValues.put("level", 0);
        long newId = this.mDatabase.insert("my_workout", null, contentValues);
        return (int) newId;
    }

    public void deleteMyWorkout(ProgramObject myWorkoutObject) {
        this.mDatabase.delete("my_workout", "id = " + myWorkoutObject.getId(), null);
    }

    public void deleteMyWorkout(int id) {
        this.mDatabase.delete("my_workout", "id = " + id, null);
    }




}
