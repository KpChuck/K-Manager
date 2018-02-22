package kpchuck.kklock.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import kpchuck.kklock.MainActivity;
import kpchuck.kklock.R;
import kpchuck.kklock.utils.PrefUtils;

import static kpchuck.kklock.constants.PrefConstants.PREF_BLACK_THEME;

public class InformationWebViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information_web_view);
        PrefUtils prefUtils = new PrefUtils(getApplicationContext());
        setTheme(prefUtils.getBool(PREF_BLACK_THEME) ? R.style.AppTheme_Dark : R.style.AppTheme);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        // this.webPath = getApplicationContext().getFilesDir().getPath() + "html";
        //this.webPath = Environment.getExternalStorageDirectory() + "/K-Manager/html";

        int data = 0;
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if (bundle != null) {
            data = bundle.getInt("value");
        }
        WebView aboutWebView = findViewById(R.id.webViewInfo);
        aboutWebView.setBackgroundColor(Color.WHITE);
        aboutWebView.setWebViewClient(new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
            }
        });

        switch (data) {
            case 1:
                aboutWebView.loadUrl("file:///android_asset/html/index.html");
                break;
            case 2:
                aboutWebView.loadUrl("file:///android_asset/html/qsbg.html");
                break;
            case 3:
                aboutWebView.loadUrl("file:///android_asset/html/left.html");
                break;
            case 4:
                aboutWebView.loadUrl("file:///android_asset/html/otherroms.html");
                break;
            default:
                finish();
                Intent i = new Intent(this, MainActivity.class);
                startActivity(i);
        }
    }

    public void shortToast (String message){
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // handle arrow click here
        if (item.getItemId() == android.R.id.home) {
            finish(); // close this activity and return to preview activity (if there is any)
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }


}
