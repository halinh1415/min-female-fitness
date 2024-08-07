package mint.woman.female.workout.custom;

import android.content.Context;
import android.net.Uri;
import android.util.AttributeSet;
import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import mint.woman.female.workout.R;
import mint.woman.female.workout.activity.ui.settings.PreferenceClickListener;

public class CustomPreference extends Preference {
    Uri imageUrl;
    boolean isSync = false;
    PreferenceClickListener clickListener;
    private Animation rotateAnimation;
    ImageView imSync;

    public CustomPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setLayoutResource(R.layout.custom_preference);
        init(context);
    }

    private void init(Context context) {
        rotateAnimation = AnimationUtils.loadAnimation(context, R.anim.rotate);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        // Access and modify the views in the custom layout
        TextView title = (TextView) holder.findViewById(R.id.custom_preference_title);
        TextView summary = (TextView) holder.findViewById(R.id.custom_preference_summary);
        ImageView icon = (ImageView) holder.findViewById(R.id.im_icon);

        ImageView imSync = (ImageView) holder.findViewById(R.id.img_sync);
        View llTilte = holder.findViewById(R.id.ll_title);
        // Set custom values or perform any additional setup
        title.setText(getTitle());
        summary.setText(getSummary());


        if (isSync){
            icon.setVisibility(View.VISIBLE);
            if (imageUrl!= null) {
                Glide.with(getContext()).load(imageUrl).apply(RequestOptions.circleCropTransform()).into(icon);
            }
            llTilte.setOnClickListener(view -> {
                if (clickListener != null){
                    clickListener.onTitleClick();
                }
            });
            imSync.startAnimation(rotateAnimation);
        }else{
            icon.setVisibility(View.INVISIBLE);
            clickListener = null;
        }



    }
    public void setUrl(Uri url,boolean isSync){
      this.imageUrl = url;
      this.isSync = isSync;
      notifyChanged();
    }
    public void setClickListener(PreferenceClickListener clickListener){
        this.clickListener = clickListener;
    }

}
