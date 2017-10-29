package kpchuck.k_klock;

import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.StringRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.webkit.WebView;

public class InformationWebViewActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_information_web_view);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        int data = 0;
        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();

        if(bundle != null){
            data = bundle.getInt("value");
        }
        WebView aboutWebView = (WebView) findViewById(R.id.webViewInfo);
        aboutWebView.setBackgroundColor(Color.WHITE);

        if (data== 1) {
            aboutWebView.loadData(styleHtml(this, R.string.general_faq), "text/html", "UTF-8");
        }else if(data== 2){
            aboutWebView.loadData(styleHtml(this, R.string.qs_bg), "text/html", "UTF-8");
        }else if(data == 3) {
            aboutWebView.loadData(styleHtml(this, R.string.left_network_indicators), "text/html", "UTF-8");
        }else if(data == 4){
            aboutWebView.loadData(styleHtml(this, R.string.about_other_roms), "text/html", "UTF-8");
        }else{
            finish();
            Intent i = new Intent(this, MainActivity.class);
            startActivity(i);
        }


    }

    public static String styleHtml(Context context, @StringRes int resourceId) {
        TypedArray ta = context.obtainStyledAttributes(new int[] {
                android.R.attr.textColorPrimary,
                android.R.attr.colorAccent,
               }
        );
        String textColorPrimary = String.format("#%06X", (0xFFFFFF & ta.getColor(0, Color.BLACK)));
        String accentColor = String.format("#%06X", (0xFFFFFF & ta.getColor(1, Color.CYAN)));

        ta.recycle();
        String html = context.getString(resourceId);
        html = html.replaceAll("\\?android:attr/textColorPrimary", textColorPrimary);
        html = html.replaceAll("\\?android:attr/colorAccent", accentColor);

        return html;
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
