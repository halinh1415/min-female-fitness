package mint.woman.female.workout.custom;

import android.app.Dialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import mint.woman.female.workout.R;
import mint.woman.female.workout.model.ReminderItem;
import mint.woman.female.workout.receiver.AlarmReceiver;
import mint.woman.female.workout.utils.Database;
import mint.woman.female.workout.utils.MyPreference;

import java.util.ArrayList;
import java.util.List;

public class SelectReminderEveryDayDialog extends DialogFragment implements View.OnClickListener {

    String[] mSelectTime = new String[]{"5:00", "6:00", "7:00", "8:00", "9:00", "10:00", "11:00", "12:00", "13:00", "14:00", "15:00", "16:00", "17:00", "18:00", "19:00", "20:00", "21:00", "22:00", "23:00", "00:00", "I don't want to remind"};
    //Boolean[] mFlagSelect = new Boolean[]{false,false,false,true,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false,false};
    RecyclerView mSelectRm;
    List<SelectTimeItem> mSelectTimeItems;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.select_reminder_everyday, container, false);
        initView(view);
        return view;
    }

    private void initView(View view) {
        mSelectTimeItems = new ArrayList<>();
        for (int i = 0; i < mSelectTime.length; i++) {
            mSelectTimeItems.add(new SelectTimeItem(mSelectTime[i], false));
        }
        mSelectTimeItems.get(15).isSelect = true;
        mSelectRm = view.findViewById(R.id.rc_select_rm);
        mSelectRm.setLayoutManager(new GridLayoutManager(getActivity(), 1));
        mSelectRm.setAdapter(new SelectRMAdapter());
        view.findViewById(R.id.txt_ok_select).setOnClickListener(this);
        view.findViewById(R.id.txt_select_cancel).setOnClickListener(this);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        return dialog;

    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }

    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.txt_ok_select:
                for (int i = 0; i < mSelectTimeItems.size() - 1; i++) {
                    if (mSelectTimeItems.get(i).isSelect) {
                        ReminderItem reminderItem = new ReminderItem();
                        reminderItem.active = 1;
                        reminderItem.repeat = 127;
                        reminderItem.time = mSelectTimeItems.get(i).time;
                        reminderItem.id = Database.newInstance(getActivity(), "workout.db").insertReminder(reminderItem);
                        AlarmReceiver.setAlarm(getActivity(), reminderItem);
                    }
                }
                MyPreference myPreference = new MyPreference(getActivity());
                myPreference.setReminder(true);
                dismiss();
                break;
            case R.id.txt_select_cancel:
                dismiss();
                break;
        }
    }

    //    @Override
//    public void onStart() {
//        super.onStart();
//        Dialog dialog = getDialog();
//        if (dialog != null) {
//            dialog.getWindow().setLayout(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
//        }
//    }
    class SelectRMAdapter extends RecyclerView.Adapter<SelectRMAdapter.SRMViewHolder> {
        @NonNull
        @Override
        public SRMViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.select_reminder_item, null);
            return new SRMViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull SRMViewHolder holder, int position) {
            SelectTimeItem item = mSelectTimeItems.get(position);
            holder.mCBReminder.setText(item.time);
            holder.mCBReminder.setChecked(item.isSelect);
        }

        @Override
        public int getItemCount() {
            return mSelectTimeItems.size();
        }

        class SRMViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            CheckBox mCBReminder;

            public SRMViewHolder(View itemView) {
                super(itemView);
                mCBReminder = itemView.findViewById(R.id.cb_select_rm);
                mCBReminder.setOnClickListener(this);
            }

            @Override
            public void onClick(View view) {
                if (view.getId() == R.id.cb_select_rm) {
                    if (getAdapterPosition() < mSelectTimeItems.size() - 1) {
                        mSelectTimeItems.get(getAdapterPosition()).isSelect = mCBReminder.isChecked();
                    } else if (getAdapterPosition() == mSelectTimeItems.size() - 1) {
                        mSelectTimeItems.get(mSelectTimeItems.size() - 1).isSelect = mCBReminder.isChecked();
                        if (mSelectTimeItems.get(mSelectTimeItems.size() - 1).isSelect) {
                            for (int i = 0; i < mSelectTimeItems.size() - 1; i++) {
                                mSelectTimeItems.get(i).isSelect = false;
                                notifyDataSetChanged();
                            }
                        }

                    }
                }
            }
        }
    }

    class SelectTimeItem {
        public String time;
        public Boolean isSelect;

        public SelectTimeItem(String time, Boolean isSelect) {
            this.time = time;
            this.isSelect = isSelect;
        }
    }
}
