package mint.woman.female.workout.common.data;


import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.model.PlanObject;
import mint.woman.female.workout.utils.JsonHelper;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DemoDataProvider extends AbstractDemoDataProvider {
    private List<PlanObject> mData;
    private List<PlanObject.ActionObject> actionObjectList;
    private int index;
    private JsonHelper mJsonHelper;
    HashMap<Integer,ExerciseObject> hashMap;
    int discover =0;

    public DemoDataProvider(JsonHelper jsonHelper,int discover) {
        this.mJsonHelper = jsonHelper;
        hashMap = mJsonHelper.getHashMapExercise(discover);
        mData = new ArrayList<>();
        this.discover = discover;
    }

    public void setData(String fileName, int day) {
        mData = mJsonHelper.getListPlan(fileName,discover);
        index = day;
        actionObjectList = new ArrayList<>();
        if (index < mData.size()) {
            for (PlanObject.ActionObject action : mData.get(index).mActionList
            ) {
                actionObjectList.add(action.clone());
            }
        }
    }

    public void x2Workout() {
        mData.get(index).mActionList.clear();
        for (int i = 0; i < actionObjectList.size(); i++) {
            PlanObject.ActionObject action = actionObjectList.get(i).clone();
            action.time *= 2;
            mData.get(index).addNewAction(action);
        }
    }

    public List<PlanObject.ActionObject> getActionObjectList() {
        return mData.get(index).mActionList;
    }

    public void x1Workout() {
        mData.get(index).mActionList.clear();
        mData.get(index).mActionList.addAll(actionObjectList);
    }



    public void resetData(String fileName) {
        mData = mJsonHelper.getResetPlan(fileName,discover);
    }

    public String convertPlanToJsonString() {
        JSONArray jsPlanObjects = new JSONArray();
        try {
            for (int i = 0; i < mData.size(); i++) {
                PlanObject planObject = mData.get(i);
                JSONObject jsDayObject = new JSONObject();
                jsDayObject.put("name", "Day " + (i + 1));
                // jsDayObject.put("focus", planObject.focus);
                //  jsDayObject.put("actionCount", planObject.mActionList.size());
                JSONArray jsActions = new JSONArray();
                for (PlanObject.ActionObject actionObject : planObject.mActionList) {
                    JSONObject jsAct = new JSONObject();
                    jsAct.put("actionId", actionObject.actionId);
                    jsAct.put("time", actionObject.time);
                    jsAct.put("actype",actionObject.actype);
                    jsAct.put("rest",actionObject.rest);
                    jsAct.put("unit",actionObject.unit);
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

    public int getTotalTimeByIndex(int i) {
        int totalTime = 0;
        for (PlanObject.ActionObject action : mData.get(i).mActionList
        ) {
            ExerciseObject exerciseObject = getExerciseItem(action.actionId);
            if (exerciseObject.getUnit().isEmpty()) {
                //  totalTime += action.time * (exerciseObject.getTime() / 1000.0f);
            } else {
                totalTime += 20 + action.time;
            }

        }
        return totalTime;
    }

    public int getTotalTime() {
        int totalTime = 0;
        if (index >= mData.size()) return 0;
        for (PlanObject.ActionObject action : mData.get(index).mActionList
        ) {
           // ExerciseObject exerciseObject = getExerciseItem(action.actionId);
            if (action.unit.isEmpty()) {
                totalTime +=  action.time*3;
                //  totalTime += action.time * (exerciseObject.getTime() / 1000.0f);
            } else {
                totalTime +=  action.time;
            }

        }
        return totalTime;
    }


    @Override
    public int getItemCount() {
        if (index >= mData.size()) return 0;
        return mData.get(index).mActionList.size();
    }

    @Override
    public PlanObject.ActionObject getItem(int position) {
        return mData.get(index).mActionList.get(position);
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
        final PlanObject.ActionObject item = mData.get(index).mActionList.remove(fromPosition);
        mData.get(index).mActionList.add(toPosition, item);
    }

    @Override
    public void removeItem(int position) {
        mData.get(index).mActionList.remove(position);
    }

    @Override
    public void addItem(int action) {
        mData.get(index).addNewAction(new PlanObject.ActionObject(action, 30, "s", 0, 10));
    }

    @Override
    public void updateItem(int position, PlanObject.ActionObject actionObject) {
        mData.get(index).mActionList.set(position, actionObject);
    }

    @Override
    public void updateTime(int position, int time) {
        mData.get(index).mActionList.get(position).time = time;
    }

    @Override
    public void replaceAction(int position, PlanObject.ActionObject actionObject) {
        mData.get(index).mActionList.set(position,actionObject);
    }

    @Override
    public PlanObject getPlanObject() {
        if (index >= mData.size()) return null;
        return mData.get(index);
    }

}
