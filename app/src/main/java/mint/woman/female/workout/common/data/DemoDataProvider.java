package mint.woman.female.workout.common.data;



import android.content.Context;

import mint.woman.female.workout.FitnessApplication;
import mint.woman.female.workout.model.ExerciseObject;
import mint.woman.female.workout.model.PlanObject;
import mint.woman.female.workout.model.ProgramObject;
import mint.woman.female.workout.utils.JsonHelper;
import mint.woman.female.workout.utils.DifficultyStore;
import mint.woman.female.workout.utils.ExerciseFeedbackStore;
import mint.woman.female.workout.utils.PlanEditStore;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
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
    int discover = 0;
    int version = 1003;

    public DemoDataProvider(Context context, int discover, int version) {
        this.mContext = context;
        this.mJsonHelper = JsonHelper.newInstance(mContext);
        this.version = version;
        hashMap = mJsonHelper.getHashMapExercise(discover, version);
        mData = new ArrayList<>();
        this.discover = discover;
    }

    public void setData(String fileName, int day) {
        planFile = fileName;
        appliedPercent = 100;
        mData = mJsonHelper.getListPlan(fileName, discover, version);
        index = day;
        actionObjectList = new ArrayList<>();
        if (day < mData.size()) {
            max = mData.get(index).mActionList.size();
            if (mData.get(day).getRule() != null) {
                // Duyệt danh sách đã xáo trộn đúng 1 lượt. Trước đây bốc ngẫu nhiên trong vòng while
                // đến khi đủ rule.value: bài không đủ động tác hợp lệ (thiếu trong file ngôn ngữ /
                // trùng id) thì lặp vô hạn trên main thread -> ANR ở PreviewActivity.
                max = mData.get(day).getRule().getValue();
                Set<Integer> used = new HashSet<>();
                List<PlanObject.ActionObject> candidates = new ArrayList<>(mData.get(index).mActionList);
                Collections.shuffle(candidates, new Random());
                for (PlanObject.ActionObject actionObject : candidates) {
                    if (actionObjectList.size() >= max) break;
                    ExerciseObject exerciseObject = hashMap.get(actionObject.actionId);
                    if (exerciseObject == null || !used.add(actionObject.actionId)) continue;
                    List<Integer> list = exerciseObject.getGroupactions();
                    if (list != null && !list.isEmpty()) {
                        PlanObject.ActionObject leftAction = actionObject.clone();
                        leftAction.actionId = list.get(0);
                        used.add(leftAction.actionId);
                        if (hashMap.get(leftAction.actionId) != null) {
                            actionObjectList.add(leftAction);
                        }
                    }
                    actionObjectList.add(actionObject.clone());
                }
            } else {
                for (PlanObject.ActionObject action : mData.get(index).mActionList
                ) {
                    actionObjectList.add(action.clone());
                }
            }
            // Ngày này người dùng đã chỉnh và bấm Lưu ở màn Preview: dùng bản đó thay danh sách gốc.
            List<PlanObject.ActionObject> edited = PlanEditStore.load(mContext, discover, fileName, day);
            if (edited != null) {
                actionObjectList = edited;
            } else {
                if (discover == 0) {
                    ExerciseFeedbackStore.swapDisliked(mContext, discover, actionObjectList, hashMap);
                }
                if (extrasProgram != null) {
                    DifficultyStore.scale(mContext, extrasProgram.getId(), actionObjectList, hashMap);
                    appliedPercent = DifficultyStore.percent(mContext, extrasProgram.getId());
                }
            }
            if (extrasProgram != null) {
                WorkoutExtras.apply(actionObjectList, hashMap, extrasProgram, extrasWarmUp, extrasNoJump);
            }
        }
    }

    private String planFile;
    private int appliedPercent = 100;

    /** Hệ số độ khó (%) đã áp vào danh sách lần nạp gần nhất; 100 = không chỉnh. */
    public int getAppliedPercent() {
        return appliedPercent;
    }

    /** Ghi danh sách đang chỉnh (động tác chính) cho ngày đang mở; lần nạp sau dùng bản này. */
    public void saveEdits() {
        if (planFile != null) PlanEditStore.save(mContext, discover, planFile, index, actionObjectList);
    }

    /** Bỏ bản đã chỉnh của ngày đang mở để Đặt lại về bài gốc. */
    public void clearEdits() {
        if (planFile != null) PlanEditStore.clear(mContext, discover, planFile, index);
    }

    private ProgramObject extrasProgram;
    private boolean extrasWarmUp, extrasNoJump;

    /** Bật khởi động / giãn cơ và đổi động tác nhảy cho các lần setData / resetData sau (xem WorkoutExtras). */
    public void setExtras(ProgramObject program, boolean warmUpCoolDown, boolean noJump) {
        extrasProgram = program;
        extrasWarmUp = warmUpCoolDown;
        extrasNoJump = noJump;
    }

    public boolean supportsWarmUp() {
        PlanObject planObject = getPlanObject();
        return extrasProgram != null && planObject != null
                && WorkoutExtras.supportsWarmUp(planObject.mActionList, hashMap, extrasProgram);
    }

//    private ExerciseObject findPair(String imageName) {
//        for (ExerciseObject ex : allExercises
//        ) {
//            if (ex.getImagePath().equals(imageName)) {
//                return ex;
//            }
//        }
//        return null;
//    }

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
                    jsAct.put("unit", actionObject.getUnit());
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
            if (action.getUnit().isEmpty()) {
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
