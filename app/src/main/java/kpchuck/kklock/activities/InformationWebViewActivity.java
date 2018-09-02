package kpchuck.kklock.activities;

import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Toast;

import java.io.IOException;
import java.util.Arrays;

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

        String base = "file:///android_asset/html/" + getFolderLocale();


        switch (data) {
            case 1:
                aboutWebView.loadUrl(base + "index.html");
                break;
            case 2:
                aboutWebView.loadUrl(base + "qsbg.html");
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

    private String getFolderLocale(){
        String locale = "";
        String current_locale = getResources().getConfiguration().getLocales().get(0).getLanguage();
        if (!current_locale.equals("en") && folderExistsHtml(current_locale)){
            locale = current_locale + "/";
        }
        return locale;

    }


    private boolean folderExistsHtml(String folder){
        try {
            AssetManager assetManager = getAssets();
            String[] files = assetManager.list("html/");
            return Arrays.asList(files).contains(folder);
        }catch (IOException e){
            Log.e("klock", e.getMessage());
            return false;
        }

    }


}
