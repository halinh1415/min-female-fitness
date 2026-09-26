package mint.woman.female.workout.activity.ui.settings;

import androidx.recyclerview.widget.RecyclerView;

import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import mint.woman.female.workout.R;
import mint.woman.female.workout.utils.AppEncrypt;

public class VoiceAdapter extends RecyclerView.Adapter<VoiceAdapter.VoiceViewHolder> {

    private List<VoiceItem> voiceList;
    private OnItemClickListener onItemClickListener;
    String defaultEngine;
    Locale defaultLocale;
    String voicePlay ="";
    String voiceSelected = "default";
    String voiceDownloading = "";
    String basePath ="";
    Context context;


    public interface OnItemClickListener {
        void onItemClick(VoiceItem voiceItem);
        void onItemChoose(VoiceItem voiceItem);
        void onItemPlay(VoiceItem voiceItem);
    }

    public void setVoiceDownloading(String voiceDownloading) {
        this.voiceDownloading = voiceDownloading;
        notifyDataSetChanged();
    }

    public void setDefaultTTs(String defaultEngine, Locale defaultLocale){
        this.defaultEngine = defaultEngine;
        this.defaultLocale = defaultLocale;
        notifyDataSetChanged();
    }
    public void setVoicePlay(String voicePlay){
        this.voicePlay = voicePlay;
        notifyDataSetChanged();
    }
    public VoiceAdapter(Context context,String basePath, OnItemClickListener listener) {
        this.voiceList = new ArrayList<>();
        this.basePath = basePath;
        this.context = context;
        this.onItemClickListener = listener;
    }
    public void setVoiceList(List<VoiceItem> voiceList){
        this.voiceList.clear();
        this.voiceList.addAll(voiceList);
        notifyDataSetChanged();
    }
    public void setSelectVoice(String value){
        this.voiceSelected = value;

        notifyDataSetChanged();
    }

    boolean isExist(String name){
       return new File(basePath, name).exists();
    }

    @Override
    public int getItemViewType(int position) {
        return position ==0? 0:1;
    }

    @NonNull
    @Override
    public VoiceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == 0){
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.voice_item_default, parent, false);
            return new VoiceViewHolder(view);
        }
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.voice_item, parent, false);
        return new VoiceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull VoiceViewHolder holder, int position) {
        VoiceItem item = voiceList.get(position);
        holder.imgVoiceIcon.setImageResource(item.getIconRes());
        holder.tvVoiceName.setText(item.getName());
        holder.imgChooseVoice.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemChoose(item);
            }
        });
        holder.imgMediaPlay.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemPlay(item);
            }
        });

        int selectionRes = item.getValue().equals(voiceSelected) ? R.drawable.ic_selected : R.drawable.ic_unselected;
        holder.imgChooseVoice.setImageResource(selectionRes);

        if (position ==0) {
            if ( item.getValue().equals(voiceSelected)) {
                holder.llDefaultChild.setVisibility(View.VISIBLE);
            } else {
                holder.llDefaultChild.setVisibility(View.GONE);
            }
            if (defaultEngine != null && defaultLocale != null){
                holder.tvEngine.setText(defaultEngine);
                holder.tvLocale.setText(defaultLocale.getDisplayLanguage());
            }
            if (holder.itemView.findViewById(R.id.tvSelectTTS) != null) {
                holder.itemView.findViewById(R.id.tvSelectTTS).setOnClickListener(view -> {
                    openTTSSettings();
                });
            }
            if (holder.itemView.findViewById(R.id.tvInstallVoice) != null) {
                holder.itemView.findViewById(R.id.tvInstallVoice).setOnClickListener(view -> {
                    installVoiceData();
                });
            }
            if (voicePlay.equals(item.getName())){
                holder.imgMediaPlay.setVisibility(View.GONE);
                holder.lottieAnimationView.setVisibility(View.VISIBLE);
            }else{
                holder.imgMediaPlay.setVisibility(View.VISIBLE);
                holder.lottieAnimationView.setVisibility(View.GONE);
            }
        }else{

            String coachName = AppEncrypt.getName(String.format(context.getString(R.string.tts_test_des),item.getName()));
            if (isExist(item.getValue()+"/"+coachName)){

                if (voicePlay.equals(item.getName())){
                    holder.imgMediaPlay.setVisibility(View.GONE);
                    holder.lottieAnimationView.setVisibility(View.VISIBLE);
                }else{
                    holder.imgMediaPlay.setVisibility(View.VISIBLE);
                    holder.lottieAnimationView.setVisibility(View.GONE);
                }
            }else{
                holder.imgMediaPlay.setVisibility(View.GONE);
                holder.lottieAnimationView.setVisibility(View.GONE);
                holder.imgChooseVoice.setImageResource(R.drawable.ic_download);
            }
            if (item.getValue().equals(voiceDownloading)){
                holder.imgChooseVoice.setVisibility(View.INVISIBLE);
                holder.prgDownloading.setVisibility(View.VISIBLE);
            }else {
                holder.imgChooseVoice.setVisibility(View.VISIBLE);
                holder.prgDownloading.setVisibility(View.GONE);
            }
        }



    }
    private void installVoiceData() {
        Intent intent = new Intent(TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage("com.google.android.tts"/*replace with the package name of the target TTS engine*/);
        try {
            context.startActivity(intent);
        } catch (ActivityNotFoundException ex) {
            Log.e("HAHA", "Failed to install TTS data, no acitivty found for " + intent + ")");
        }
    }

    private void openTTSSettings() {
        try {
            //Open Android Text-To-Speech Settings
            if (Build.VERSION.SDK_INT >= 15) {
                Intent intent = new Intent();
                intent.setAction("com.android.settings.TTS_SETTINGS");
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            } else {
                Intent intent = new Intent();
                intent.addCategory(Intent.CATEGORY_LAUNCHER);
                intent.setComponent(new ComponentName("com.android.settings", "com.android.settings.TextToSpeechSettings"));
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public int getItemCount() {
        return voiceList.size();
    }

    public void updateItems(List<VoiceItem> newItems) {
        this.voiceList = newItems;
        notifyDataSetChanged();
    }

    static class VoiceViewHolder extends RecyclerView.ViewHolder {
        ImageView imgVoiceIcon, imgChooseVoice,imgMediaPlay;
        TextView tvVoiceName;
        LinearLayout llDefaultChild;
        TextView tvEngine,tvLocale;
        LottieAnimationView lottieAnimationView;

        ProgressBar prgDownloading;

        VoiceViewHolder(View itemView) {
            super(itemView);
            imgVoiceIcon = itemView.findViewById(R.id.imgVoiceIcon);
            tvVoiceName = itemView.findViewById(R.id.tvVoiceName);
            imgChooseVoice = itemView.findViewById(R.id.imgChooseVoice);
            llDefaultChild = itemView.findViewById(R.id.llDefaultChild);
            imgMediaPlay = itemView.findViewById(R.id.imgVoicePlay);

            tvEngine = itemView.findViewById(R.id.tvEngine);
            tvLocale = itemView.findViewById(R.id.tvLocale);
            lottieAnimationView = itemView.findViewById(R.id.ltVoicePlay);
            prgDownloading = itemView.findViewById(R.id.prg_download);
        }
    }
}
