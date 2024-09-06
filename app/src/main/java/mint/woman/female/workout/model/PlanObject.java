package mint.woman.female.workout.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class PlanObject implements Parcelable, Cloneable {

    public List<ActionObject> mActionList;
    public String name;

    private Rule rule;
    public int mNextActionId;

    public PlanObject() {
        mActionList = new ArrayList<>();
        name = "";
        mNextActionId = 0;
    }

    protected PlanObject(Parcel in) {
        mActionList = in.createTypedArrayList(ActionObject.CREATOR);
        name = in.readString();
        mNextActionId = in.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(mActionList);
        dest.writeString(name);
        dest.writeInt(mNextActionId);
    }

    public Rule getRule() {
        return rule;
    }

    public void setRule(Rule rule) {
        this.rule = rule;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<PlanObject> CREATOR = new Creator<PlanObject>() {
        @Override
        public PlanObject createFromParcel(Parcel in) {
            return new PlanObject(in);
        }

        @Override
        public PlanObject[] newArray(int size) {
            return new PlanObject[size];
        }
    };

    public int generateNewActionId() {
        int id = mNextActionId;
        mNextActionId += 1;
        return id;
    }

    public void addNewAction(ActionObject actionObject) {
        actionObject.mId = generateNewActionId();
        mActionList.add(actionObject);
    }

    public List<ActionObject> getActionList() {
        return mActionList;
    }

    public void setActionList(List<ActionObject> mActionList) {
        this.mActionList = mActionList;
    }

    public int getTotalTime() {
        int totalTime = 0;
        for (ActionObject action : mActionList
        ) {
            if (action.unit.isEmpty()) {
                totalTime += 20 +action.time*4;
            } else {
                totalTime += 20 + action.time;
            }

        }
        return totalTime;
    }
    public int getNetTotalTime() {
        int totalTime = 0;
        for (ActionObject action : mActionList
        ) {
            if (action.unit.isEmpty()) {
                totalTime += action.time*4;
            } else {
                totalTime += action.time;
            }

        }
        return totalTime;
    }


    public static class ActionObject implements Parcelable, Cloneable {


        public int actionId, time, actype, rest;
        public int mId;
        private String unit;

        public boolean isSelected = false;

        public ActionObject() {
            unit ="";
        }

        public ActionObject(int actionId, int time, String unit, int actype, int rest) {
            this.actionId = actionId;
            this.time = time;
            this.actype = actype;
            this.rest = rest;
            this.unit = unit;
        }

        protected ActionObject(Parcel in) {
            actionId = in.readInt();
            time = in.readInt();
            actype = in.readInt();
            rest = in.readInt();
            mId = in.readInt();
            unit = in.readString();
            isSelected = in.readByte() != 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeInt(actionId);
            dest.writeInt(time);
            dest.writeInt(actype);
            dest.writeInt(rest);
            dest.writeInt(mId);
            dest.writeString(unit);
            dest.writeByte((byte) (isSelected ? 1 : 0));
        }

        @Override
        public int describeContents() {
            return 0;
        }

        public static final Creator<ActionObject> CREATOR = new Creator<ActionObject>() {
            @Override
            public ActionObject createFromParcel(Parcel in) {
                return new ActionObject(in);
            }

            @Override
            public ActionObject[] newArray(int size) {
                return new ActionObject[size];
            }
        };

        public ActionObject clone() {
            ActionObject actionObject = new ActionObject();
            actionObject.actionId = actionId;
            actionObject.time = time;
            actionObject.unit = unit;
            actionObject.actype = actype;
            actionObject.rest = rest;
            return actionObject;
        }

        public String getUnit(){
            if (unit!= null) return unit;
            return "";
        }

    }
}
