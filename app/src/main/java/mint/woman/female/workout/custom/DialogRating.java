package mint.woman.female.workout.custom;

import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.google.firebase.analytics.FirebaseAnalytics;
import mint.woman.female.workout.R;
import mint.woman.female.workout.admodule.CustomRatingBar;
import mint.woman.female.workout.utils.MyPreference;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DialogRating extends DialogFragment {
    @BindView(R.id.img_rate)
    ImageView imgRating;
    //    @BindView(R.id.ratingBar)
//    CustomRatingBar mRatingBar;
    @BindView(R.id.txt_pre_rate_title)
    TextView mPreRateTitle;
    @BindView(R.id.txt_pre_rate_feedback)
    TextView mPreRateFeedback;
    int mRateValue = 0;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_rating, container, false);
        ButterKnife.bind(this, view);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // setStyle(DialogFragment.STYLE_NORMAL, R.style.rating_dialog);
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        Dialog dialog = super.onCreateDialog(savedInstanceState);
        dialog.getWindow().requestFeature(Window.FEATURE_NO_TITLE);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        return dialog;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FirebaseAnalytics.getInstance(getContext()).logEvent("scr_pre_rating",new Bundle());

        initView();

    }

    @BindView(R.id.star_layout)
    LinearLayout lyRate;

    private void initView() {
       // changeTextColorOfTextViews(lyRate, mRateValue);

//        mRatingBar.setStars(0.0f);
//        mRatingBar.setCanChange(true);
//        mRatingBar.setMax(5);
//        mRatingBar.setStep(true);
//        mPreRateFeedback.setText(getString(R.string.txt_pre_rate_feedback_default));
//        mRatingBar.setOnStarChangeListener(new CustomRatingBar.OnStarChangeListener() {
//            @Override
//            public void onStarChange(CustomRatingBar ratingBar, float star) {
//
//                int value = (int) star;
//                mRateValue = value;
//                switch (value) {
//                    case 3:
//                        mPreRateTitle.setText(getString(R.string.txt_pre_rate_title_unhappy));
//                        mPreRateFeedback.setText(getString(R.string.txt_feedback_unhappy));
//                        imgRating.setImageResource(R.drawable.ic_rate_three);
//                        break;
//                    case 4:
//                        mPreRateTitle.setText(getString(R.string.txt_pre_rate_title_happy));
//                        mPreRateFeedback.setText(getString(R.string.txt_some_feedback));
//                        imgRating.setImageResource(R.drawable.ic_rate_four);
//                        break;
//                    case 5:
//                        mPreRateTitle.setText(getString(R.string.txt_pre_rate_title_happy));
//                        mPreRateFeedback.setText(getString(R.string.txt_some_feedback));
//                        imgRating.setImageResource(R.drawable.ic_rate_five);
//                        break;
//                    case 2:
//                        mPreRateTitle.setText(getString(R.string.txt_pre_rate_title_unhappy));
//                        mPreRateFeedback.setText(getString(R.string.txt_feedback_unhappy));
//                        imgRating.setImageResource(R.drawable.ic_rate_two);
//                        break;
//                    default:
//                        mPreRateTitle.setText(getString(R.string.txt_pre_rate_title_unhappy));
//                        mPreRateFeedback.setText(getString(R.string.txt_feedback_unhappy));
//                        imgRating.setImageResource(R.drawable.ic_rate_one);
//                        break;
//                }
//            }
//        });
    }

    private void changeTextColorOfTextViews(ViewGroup viewGroup, int rate) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View child = viewGroup.getChildAt(i);
            if (child instanceof StarCheckedImageView) {
                StarCheckedImageView im = (StarCheckedImageView) child;

                if (i < rate) {
                    if (im.getTag() != null) {
                        im.setChecked(true);

                    }
                } else {
                    if (im.getTag() != null) {
                        im.setChecked(false);
                    }
                }
            }
        }
    }



    @OnClick({R.id.rate_star_1, R.id.rate_star_2, R.id.rate_star_3, R.id.rate_star_4, R.id.rate_star_5})
    void changeRate(View view) {
        switch (view.getId()) {
            case R.id.rate_star_1:
                if (mRateValue == 1) {
                    mRateValue = 0;
                } else {
                    mRateValue = 1;
                }
                mPreRateTitle.setText(getString(R.string.txt_pre_rate_title_unhappy));
                mPreRateFeedback.setText(getString(R.string.txt_feedback_unhappy));
                imgRating.setImageResource(R.drawable.ic_rate_one);
                break;
            case R.id.rate_star_2:
                if (mRateValue == 2) {
                    mRateValue = 1;
                } else {
                    mRateValue = 2;
                }
                mPreRateTitle.setText(getString(R.string.txt_pre_rate_title_unhappy));
                mPreRateFeedback.setText(getString(R.string.txt_feedback_unhappy));
                imgRating.setImageResource(R.drawable.ic_rate_two);
                break;
            case R.id.rate_star_3:
                if (mRateValue == 3) {
                    mRateValue = 2;
                } else {
                    mRateValue = 3;
                }
                mPreRateTitle.setText(getString(R.string.txt_pre_rate_title_unhappy));
                mPreRateFeedback.setText(getString(R.string.txt_feedback_unhappy));
                imgRating.setImageResource(R.drawable.ic_rate_three);
                break;
            case R.id.rate_star_4:
                if (mRateValue == 4) {
                    mRateValue = 3;
                } else {
                    mRateValue = 4;
                }
                mPreRateTitle.setText(getString(R.string.txt_pre_rate_title_happy));
                mPreRateFeedback.setText(getString(R.string.txt_some_feedback));
                imgRating.setImageResource(R.drawable.ic_rate_four);
                break;
            case R.id.rate_star_5:
                if (mRateValue == 5) {
                    mRateValue = 4;
                } else {
                    mRateValue = 5;
                }
                mPreRateTitle.setText(getString(R.string.txt_pre_rate_title_happy));
                mPreRateFeedback.setText(getString(R.string.txt_some_feedback));
                imgRating.setImageResource(R.drawable.ic_rate_five);
                break;
        }
        changeTextColorOfTextViews(lyRate, mRateValue);
    }


    @Override
    public void onDestroyView() {
        if (getDialog() != null && getRetainInstance()) {
            getDialog().setDismissMessage(null);
        }
        super.onDestroyView();
    }



    @OnClick({R.id.btn_close,R.id.btn_rate})
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_close:
                FirebaseAnalytics.getInstance(getContext()).logEvent("click_close_scr_pre_rating",new Bundle());
                dismiss();
                break;
            case R.id.btn_rate:
                Bundle bundleFire = new Bundle();
                bundleFire.putInt("Rate", mRateValue);
                FirebaseAnalytics.getInstance(getContext()).logEvent("click_rate_scr_pre_rating", bundleFire);
                if (mRateValue == 5 || mRateValue ==0) {
                    gotoRate();
                } else {
                    DialogFeedback dialogRating = new DialogFeedback();
                    dialogRating.show(getActivity().getSupportFragmentManager(), "rating");
                }
                dismiss();
                break;
        }
    }
    @Override
    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        if (dialog != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        }
    }

    @OnClick(R.id.img_close)
    void close(){
        dismiss();
    }

    public void gotoRate() {
        MyPreference myPreference = new MyPreference(getActivity());
        myPreference.setRated();
        Uri uri = Uri.parse("market://details?id=" + getActivity().getPackageName());
        Intent goToMarket = new Intent(Intent.ACTION_VIEW, uri);
        goToMarket.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY |
                Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
        try {
            startActivity(goToMarket);
        } catch (ActivityNotFoundException e) {
            startActivity(new Intent(Intent.ACTION_VIEW,
                    Uri.parse("http://play.google.com/store/apps/details?id=" + getActivity().getPackageName())));
        }


    }

}
