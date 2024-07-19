package mint.woman.female.workout.common.data;



import android.content.Context;

import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.model.PlanObject;
import mint.woman.female.workout.utils.JsonHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class DemoDataProvider extends AbstractDemoDataProvider {
    private List<PlanObject> mData;
    private List<PlanObject.ActionObject> actionObjectList;
    private int index;
    private int max;
    private JsonHelper mJsonHelper;
    private Context mContext;
    HashMap<Integer, ExerciseObject> hashMap;
    List<ExerciseObject> allExercises;
    int discover = 0;
    int version = 1001;

    public DemoDataProvider(Context context, int discover, int version) {
        this.mContext = context;
        this.mJsonHelper = JsonHelper.newInstance(mContext);
        this.version = version;
        hashMap = mJsonHelper.getHashMapExercise(discover, version);
        mData = new ArrayList<>();
        this.discover = discover;
    }

    public void setData(String fileName, int day) {
        mData = mJsonHelper.getListPlan(fileName, discover, version);
        index = day;
        actionObjectList = new ArrayList<>();
        if (day < mData.size()) {
            max = mData.get(index).mActionList.size();
            if (mData.get(day).getRule() != null) {
                allExercises = mJsonHelper.getAllExercise(discover, version);
                if (allExercises == null){
                    actionObjectList.addAll(mData.get(day).mActionList);
                }
                max = mData.get(day).getRule().getValue();
                boolean[] map = new boolean[1000];
                while (actionObjectList.size() < max) {
                    Random random = new Random();
                    int id = random.nextInt(mData.get(index).mActionList.size());
                    PlanObject.ActionObject actionObject = mData.get(index).mActionList.get(id);
                    ExerciseObject exerciseObject = hashMap.get(actionObject.actionId);
                    int a = 1;
                    if (!map[actionObject.actionId]) {
                        map[actionObject.actionId] = true;
                        if (exerciseObject.getImagePath().contains("left")) {
                            String rightName = exerciseObject.getImagePath().replace("left", "right");
                            ExerciseObject exerciseObjectRight = findPair(rightName);
                            actionObjectList.add(actionObject.clone());
                            if (exerciseObjectRight != null) {
                                map[exerciseObjectRight.getId()] = true;
                                PlanObject.ActionObject rightAction = actionObject.clone();
                                rightAction.actionId = exerciseObjectRight.getId();
                                actionObjectList.add(rightAction);
                            }
                        } else if (hashMap.get(actionObject.actionId).getImagePath().contains("right")) {
                            String leftName = exerciseObject.getImagePath().replace("right", "left");
                            ExerciseObject exerciseObjectLeft = findPair(leftName);
                            if (exerciseObjectLeft != null) {
                                map[exerciseObjectLeft.getId()] = true;
                                PlanObject.ActionObject leftAction = actionObject.clone();
                                leftAction.actionId = exerciseObjectLeft.getId();
                                actionObjectList.add(leftAction);
                            }
                            actionObjectList.add(actionObject.clone());
                        } else {
                            actionObjectList.add(actionObject.clone());
                        }
                    }

                }


            } else {
                for (PlanObject.ActionObject action : mData.get(index).mActionList
                ) {
                    actionObjectList.add(action.clone());
                }
            }
        }
    }

    private ExerciseObject findPair(String imageName) {
        for (ExerciseObject ex : allExercises
        ) {
            if (ex.getImagePath().equals(imageName)) {
                return ex;
            }
        }
        return null;
    }

    public void resetData(String fileName) {
       // mData = mJsonHelper.getResetPlan(fileName, discover, version);
        setData(fileName,index);
    }

    public String convertPlanToJsonString() {
        JSONArray jsPlanObjects = new JSONArray();
        try {
            for (int i = 0; i < mData.size(); i++) {
                PlanObject planObject = mData.get(i);
                JSONObject jsDayObject = new JSONObject();
                jsDayObject.put("name", "Day " + (i + 1));
                JSONArray jsActions = new JSONArray();
                for (PlanObject.ActionObject actionObject : planObject.mActionList) {
                    JSONObject jsAct = new JSONObject();
                    jsAct.put("actionId", actionObject.actionId);
                    jsAct.put("time", actionObject.time);
                    jsAct.put("actype", actionObject.actype);
                    jsAct.put("rest", actionObject.rest);
                    jsAct.put("unit", actionObject.unit);
                    jsActions.put(jsAct);
                }
                jsDayObject.put("exercise", jsActions);
                jsPlanObjects.put(jsDayObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }

        return jsPlanObjects.toString();
    }



    public int getTotalTime() {
        int totalTime = 0;
        if (index >= mData.size()) return 0;
        for (PlanObject.ActionObject action : actionObjectList
        ) {
            // ExerciseObject exerciseObject = getExerciseItem(action.actionId);
            if (action.unit.isEmpty()) {
                totalTime += action.rest + action.time * 2;
                //  totalTime += action.time * (exerciseObject.getTime() / 1000.0f);
            } else {
                totalTime += action.rest +action.time;
            }

        }
        return totalTime;
    }


    @Override
    public int getItemCount() {
        return actionObjectList.size();
    }

    @Override
    public PlanObject.ActionObject getItem(int position) {
        return actionObjectList.get(position);
    }

    @Override
    public ExerciseObject getExerciseItem(int actionId) {
        return hashMap.get(actionId);
    }

    @Override
    public void moveItem(int fromPosition, int toPosition) {
        if (fromPosition == toPosition) {
            return;
        }
        final PlanObject.ActionObject item = actionObjectList.remove(fromPosition);
        actionObjectList.add(toPosition, item);
    }

    @Override
    public void removeItem(int position) {
        actionObjectList.remove(position);
    }

    @Override
    public void addItem(int action) {
        actionObjectList.add(new PlanObject.ActionObject(action, 30, "s", 0, 10));
    }

    @Override
    public void updateItem(int position, PlanObject.ActionObject actionObject) {
        actionObjectList.set(position, actionObject);
    }

    @Override
    public void updateTime(int position, int time) {
        actionObjectList.get(position).time = time;
    }

    @Override
    public void replaceAction(int position, PlanObject.ActionObject actionObject) {
        actionObjectList.set(position, actionObject);
    }

    @Override
    public PlanObject getPlanObject() {
        if (index >= mData.size()) return null;
        return mData.get(index);
    }
    public List<PlanObject.ActionObject> getActionObjectList()
    {
        return actionObjectList;
    }

}
