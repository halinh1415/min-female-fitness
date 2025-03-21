package mint.woman.female.workout.activity.ui.locknotify;

import android.app.KeyguardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Locale;

import mint.woman.female.workout.activity.SplashActivity;
import mint.woman.female.workout.databinding.ActivityLockScreenBinding;

public class LockScreenActivity extends AppCompatActivity {
    ActivityLockScreenBinding binding;
    private final SimpleDateFormat mFormatTIME = new SimpleDateFormat("HH:mm", Locale.getDefault());
    private final SimpleDateFormat mFormatDate = new SimpleDateFormat("EEEE, MMMM dd", Locale.getDefault());

    Handler mHandler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLockScreenBinding.inflate(getLayoutInflater());
        showOnLockscreen();
        setContentView(binding.getRoot());
        initView();

        binding.imgClose.setOnClickListener(view -> finish());
        binding.btnStart.setOnClickListener(view -> goApp());
        binding.lnLockWidget.setOnClickListener(view -> goApp());

    }

    private void initView() {

        mHandler = new Handler();
        mHandler.post(new Runnable() {
            @Override
            public void run() {

                long timeMillis = System.currentTimeMillis();
                binding.txtTime.setText(mFormatTIME.format(timeMillis));
                binding.txtDate.setText(mFormatDate.format(timeMillis));
                Log.d("HAHA","Time update "+binding.txtTime.getText());
                mHandler.postDelayed(this,1000);
            }
        });

    }
    void dismissKeyguard(){
        KeyguardManager km = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            km.requestDismissKeyguard(LockScreenActivity.this,null);
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);
            mHandler = null;
        }
    }


    void goApp(){
        Intent intent = new Intent(LockScreenActivity.this, SplashActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK
                | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        finish();
        dismissKeyguard();
        startActivity(intent);
    }
    private void showOnLockscreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
        } else {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
        }
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            hideSystemUI();
        }
    }

    private void hideSystemUI() {
        // Enables regular immersive mode.
        // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
        // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
        View decorView = getWindow().getDecorView();

        decorView.setSystemUiVisibility(
                View.SYSTEM_UI_FLAG_IMMERSIVE
                        // Set the content to appear under the system bars so that the
                        // content doesn't resize when the system bars hide and show.

                        | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                        | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                // Hide the nav bar and status bar4
        );
    }
}