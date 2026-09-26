package mint.woman.female.workout.common.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import mint.woman.female.workout.fragment.VideoDemoFragment;
import mint.woman.female.workout.fragment.VideoYtFragment;

public class VideoDemoAdapter extends FragmentPagerAdapter {


    String video, url;

    public VideoDemoAdapter(@NonNull FragmentManager fm, String video, String url) {
        super(fm);
        this.video = video;
        this.url = url;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        if (position == 1) return VideoYtFragment.newInstance(url);
        else return VideoDemoFragment.newInstance(video);
    }

    @Override
    public int getCount() {
        return 2;
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if (position == 0) return "Animation";
        else return "Video";
    }
}
