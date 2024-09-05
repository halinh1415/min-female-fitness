package mint.woman.female.workout.common.data;

import mint.woman.female.workout.model.PlanObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class CustomWorkoutDataProvider extends AbstractCustomWorkoutDataProvider {
    List<PlanObject> mData;

    public CustomWorkoutDataProvider() {
        mData = new ArrayList<>();
    }

    @Override
    public int getGroupCount() {
        return mData.size();
    }

    public void setData(List<PlanObject> list) {
        this.mData.clear();
        this.mData.addAll(list);
    }

    public void generateDefaultPlan(int dayNumber) {
        mData = new ArrayList<>();
        for (int i = 0; i < dayNumber; i++) {
            PlanObject planObject = new PlanObject();
            //planObject.focus = "Custom";
            mData.add(planObject);
        }
        if (dayNumber > 0) {

        }
        if (dayNumber > 1) {

        }


    }

    @Override
    public int getChildCount(int groupPosition) {
        return mData.get(groupPosition).mActionList.size();
    }

    @Override
    public PlanObject getGroupItem(int groupPosition) {
        return mData.get(groupPosition);
    }

    @Override
    public PlanObject.ActionObject getChildItem(int groupPosition, int childPosition) {
        return mData.get(groupPosition).mActionList.get(childPosition);
    }


    @Override
    public void moveChildItem(int fromGroupPosition, int fromChildPosition, int toGroupPosition, int toChildPosition) {
        if ((fromGroupPosition == toGroupPosition) && (fromChildPosition == toChildPosition)) {
            return;
        }
        PlanObject fromGroup = mData.get(fromGroupPosition);
        PlanObject toGroup = mData.get(toGroupPosition);
        final PlanObject.ActionObject actionRemove = fromGroup.mActionList.remove(fromChildPosition);
        if (toGroupPosition != fromGroupPosition) {
            final int newId = toGroup.generateNewActionId();
            actionRemove.mId = newId;
        }
        toGroup.mActionList.add(toChildPosition, actionRemove);
    }


    @Override
    public void removeChildItem(int groupPosition, int childPosition) {
        mData.get(groupPosition).mActionList.remove(childPosition);
    }

    @Override
    public void updateItem(int groupPosition, int childPosition, PlanObject.ActionObject actionObject) {
        mData.get(groupPosition).mActionList.set(childPosition, actionObject);
    }

    @Override
    public void addChildItem(int groupPosition, PlanObject.ActionObject childData) {
        int newId = mData.get(groupPosition).mActionList.size();
        childData.mId = newId;
        mData.get(groupPosition).mActionList.add(newId, childData);
    }

    @Override
    public int copyToAll() {
        for (int i = 1; i < mData.size(); i++) {
            mData.get(i).mActionList.clear();
            for (PlanObject.ActionObject a : mData.get(0).mActionList
            ) {
                mData.get(i).addNewAction(a.clone());
            }
        }
        return 0;
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
                    jsAct.put("actype",actionObject.actype);
                    jsAct.put("rest",actionObject.rest);
                    jsAct.put("unit",actionObject.getUnit());

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
}
