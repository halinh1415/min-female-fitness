package mint.woman.female.workout.activity;

import android.os.Bundle;
import android.webkit.WebView;

import androidx.appcompat.app.AppCompatActivity;

import mint.woman.female.workout.R;

public class PolicyActivity extends AppCompatActivity {

    WebView webView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_policy);
        initView();
    }

    private void initView() {
        webView = findViewById(R.id.webView);
        webView.loadUrl("file:///android_asset/private_policy.html");
    }
}
