package mint.woman.female.workout.activity.ui.sounds;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import mint.woman.female.workout.R;
import mint.woman.female.workout.databinding.MusicItemLayoutBinding;
import mint.woman.female.workout.retrofit.MusicItem;

public class MusicAdapter extends RecyclerView.Adapter<MusicAdapter.MusicViewHolder>{
    List<MusicItem> musicItemList;

    MusicClickListener clickListener;
    private int selectPosition = -1;

    private int downloadStatus = 1;
    private String currentName;
    private boolean premiumLock = true;
    Context mContext;
    public MusicAdapter(MusicClickListener clickListener,String currentName,boolean premiumLock) {
        this.clickListener = clickListener;
        this.musicItemList = new ArrayList<>();
        this.currentName = currentName;
        this.premiumLock = premiumLock;
    }
    public void setMusicItemList(List<MusicItem> list){
        this.musicItemList.clear();
        this.musicItemList.addAll(list);
        for (int i = 0; i < musicItemList.size(); i++) {
            if (musicItemList.get(i).getName().equals(currentName)){
                selectPosition = i;
                break;
            }
        }
        notifyDataSetChanged();
    }

    public void setSelectPosition(int selectPosition) {
        this.selectPosition = selectPosition;
        notifyDataSetChanged();
    }


    public void setDownloadStatus(int downloadStatus) {
        this.downloadStatus = downloadStatus;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public MusicViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        mContext = viewGroup.getContext();
        LayoutInflater layoutInflater = LayoutInflater.from(viewGroup.getContext());
        MusicItemLayoutBinding binding = MusicItemLayoutBinding.inflate(layoutInflater, viewGroup, false);
        return new MusicViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull MusicViewHolder musicViewHolder, int i) {
        MusicItem musicItem = musicItemList.get(i);
        musicViewHolder.binding.tvMusicName.setText(musicItem.getName());
        musicViewHolder.binding.tvMusicDescription.setText(musicItem.getDuration());
        musicViewHolder.itemView.setOnClickListener(view -> {
           clickListener.onMusicClick(musicItem,i);
        });
        musicViewHolder.binding.imMusicSelect.setVisibility(View.VISIBLE);
        musicViewHolder.binding.prgDownloadMusic.setVisibility(View.INVISIBLE);
        if (!existsData(musicItem.getFile())){
            musicViewHolder.binding.imMusicSelect.setImageResource(R.drawable.ic_download);
        }else {
            musicViewHolder.binding.imMusicSelect.setImageResource(R.drawable.ic_unselected);
        }
        if (selectPosition == i){
            musicViewHolder.binding.ltMusic.setVisibility(View.VISIBLE);


            if (downloadStatus == 0){
                musicViewHolder.binding.imMusicSelect.setVisibility(View.INVISIBLE);
                musicViewHolder.binding.prgDownloadMusic.setVisibility(View.VISIBLE);
                musicViewHolder.binding.ltMusic.setAnimation("download.json");
                musicViewHolder.binding.ltMusic.playAnimation();
            }else{
                musicViewHolder.binding.imMusicSelect.setVisibility(View.VISIBLE);
                musicViewHolder.binding.imMusicSelect.setImageResource(R.drawable.ic_selected);
                musicViewHolder.binding.prgDownloadMusic.setVisibility(View.INVISIBLE);
                musicViewHolder.binding.ltMusic.setAnimation("music.json");
                musicViewHolder.binding.ltMusic.playAnimation();
            }
        }else{
            musicViewHolder.binding.ltMusic.setVisibility(View.INVISIBLE);
            musicViewHolder.binding.ltMusic.cancelAnimation();
        }
        if (premiumLock && musicItem.getPremium()==1){

            musicViewHolder.binding.llMusicPro.setVisibility(View.VISIBLE);
        }else{
            musicViewHolder.binding.llMusicPro.setVisibility(View.INVISIBLE);
        }

    }
    private boolean existsData(String fileName) {
        File file = new File(mContext.getFilesDir()+"/music/"+fileName);
        return file.exists();
    }

    @Override
    public int getItemCount() {
        return musicItemList.size();
    }

    class MusicViewHolder extends RecyclerView.ViewHolder{
        private final MusicItemLayoutBinding binding;
        public MusicViewHolder(MusicItemLayoutBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
