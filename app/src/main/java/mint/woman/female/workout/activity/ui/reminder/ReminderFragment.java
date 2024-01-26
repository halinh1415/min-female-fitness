package mint.woman.female.workout.activity.ui.reminder;

import android.app.TimePickerDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import mint.woman.female.workout.R;
import mint.woman.female.workout.model.ReminderItem;
import mint.woman.female.workout.receiver.AlarmReceiver;
import mint.woman.female.workout.utils.Database;
import mint.woman.female.workout.utils.MyPreference;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import butterknife.BindArray;
import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class ReminderFragment extends Fragment {

    int[] day_repeats = new int[]{R.id.cb1, R.id.cb2, R.id.cb3, R.id.cb4, R.id.cb5, R.id.cb6, R.id.cb7};

    @BindArray(R.array.week_simple)
    String[] weekSimple;

    SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
    AlertDialog.Builder mRepeatDialog;
    int repeat = 127;
    Database mDatabase;

    @BindView(R.id.rc_reminder)
    RecyclerView mReminderRc;

    ReminderAdapter mAdapter;
    MyPreference myPreference;
    Context mContext;

    public ReminderFragment() {
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle(R.string.txt_reminder);

        mDatabase = Database.newInstance(getContext(), "workout.db");
        mContext = getContext();
        myPreference = new MyPreference(mContext);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_reminder, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable final Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mReminderRc.setLayoutManager(new GridLayoutManager(mContext, 1));
        mAdapter = new ReminderAdapter();
        mReminderRc.setAdapter(mAdapter);


        List<ReminderItem> list = mDatabase.getReminder();
        mAdapter.setData(list);
    }

    @OnClick(R.id.fb_add)
    public void addReminder() {

        TimePickerDialog timePicker = new TimePickerDialog(getActivity(), (tp, i, i1) -> {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, i);
            calendar.set(Calendar.MINUTE, i1);
            ReminderItem reminderItem = new ReminderItem();
            reminderItem.time = sdf.format(calendar.getTime());
            reminderItem.repeat = 127;
            reminderItem.active = 1;
            reminderItem.id = -1;
            initDialog(reminderItem);
        }, Calendar.getInstance().get(Calendar.HOUR_OF_DAY), Calendar.getInstance().get(Calendar.MINUTE), true);
        timePicker.show();
    }

    private void initDialog(final ReminderItem reminderItem) {
        repeat = reminderItem.repeat;
        mRepeatDialog = new AlertDialog.Builder(getActivity());
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_reminder, null);
        mRepeatDialog.setView(view);
        for (int i = 0; i < day_repeats.length; i++) {
            final int finalI = i;
            int bit = getBit(repeat, i);
            ((CheckBox) view.findViewById(day_repeats[i])).setChecked(bit == 1);
            ((CheckBox) view.findViewById(day_repeats[i])).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean value) {
                    if (value) {
                        repeat = repeat | 1 << finalI;
                    } else {
                        repeat = repeat & ~(1 << finalI);
                    }
                }
            });
        }
        mRepeatDialog.setNegativeButton(android.R.string.cancel, null);
        mRepeatDialog.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                try {
                    reminderItem.repeat = repeat;
                    if (reminderItem.id == -1) {
                        reminderItem.id = mDatabase.insertReminder(reminderItem);
                    } else {
                        mDatabase.updateReminder(reminderItem);
                    }
                    AlarmReceiver.setAlarm(mContext, reminderItem);
                    mAdapter.setData(mDatabase.getReminder());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        mRepeatDialog.show();

    }

    void confirmDelete(ReminderItem reminderItem) {
        AlertDialog.Builder deleteDialog = new AlertDialog.Builder(getContext());
        // deleteDialog.setTitle(getString(R.string.txt_tips));
        deleteDialog.setTitle(getString(R.string.txt_delete_confirm));
        deleteDialog.setNegativeButton(getString(android.R.string.cancel), null);
        deleteDialog.setPositiveButton(getString(android.R.string.ok), (dialogInterface, i) -> {
            AlarmReceiver.cancelAlarm(mContext, reminderItem);
            mDatabase.deleteReminder(reminderItem.id);
            mAdapter.setData(mDatabase.getReminder());
        });
        deleteDialog.show();
    }

    public int getBit(int n, int k) {
        return (n >> k) & 1;
    }

    class ReminderAdapter extends RecyclerView.Adapter<ReminderAdapter.ReminderVH> {
        List<ReminderItem> reminderItemList;

        ReminderAdapter() {
            reminderItemList = new ArrayList<>();
        }

        public void setData(List<ReminderItem> list) {
            this.reminderItemList.clear();
            this.reminderItemList.addAll(list);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ReminderVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.reminder_item_layout, null);
            return new ReminderVH(view);
        }

        @Override
        public void onBindViewHolder(@NonNull final ReminderVH holder, final int position) {
            final ReminderItem reminderItem = reminderItemList.get(position);
            holder.mTime.setText(reminderItem.time);
            holder.mActiveSw.setChecked(reminderItem.active == 1);
            StringBuilder strDayRepeat = new StringBuilder();
            for (int i = 0; i < 7; i++) {
                int bit = getBit(reminderItem.repeat, i);
                if (bit == 1) {
                    strDayRepeat.append(weekSimple[i]).append(", ");
                }
            }
            holder.mDayRepeat.setText(strDayRepeat.toString());
            if (reminderItem.repeat == 127) {
                holder.mDayRepeat.setText(getString(R.string.txt_rm_everyday));
            }
            holder.mRepeat.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    initDialog(reminderItem);
                }
            });
            holder.mActiveSw.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                @Override
                public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                    if (b) {
                        AlarmReceiver.setAlarm(mContext, reminderItem);
                        mDatabase.updateActiveReminder(reminderItem.id, 1);
                    } else {
                        AlarmReceiver.cancelAlarm(mContext, reminderItem);
                        mDatabase.updateActiveReminder(reminderItem.id, 0);
                    }
                }
            });
            holder.mDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    confirmDelete(reminderItem);
                }
            });

        }

        @Override
        public int getItemCount() {
            return reminderItemList.size();
        }

        class ReminderVH extends RecyclerView.ViewHolder {
            TextView mTime, mDayRepeat, mRepeat;
            Switch mActiveSw;
            ImageView mDelete;

            ReminderVH(View itemView) {
                super(itemView);
                mTime = itemView.findViewById(R.id.txt_reminder_time);
                mDayRepeat = itemView.findViewById(R.id.tv_day_unit);
                mRepeat = itemView.findViewById(R.id.tv_repeat);
                mActiveSw = itemView.findViewById(R.id.sw_active);
                mDelete = itemView.findViewById(R.id.img_delete);
            }
        }
    }
}
