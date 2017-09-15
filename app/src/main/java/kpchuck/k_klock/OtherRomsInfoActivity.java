package kpchuck.k_klock;

import android.content.Intent;
import android.net.Uri;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import com.chabbal.slidingdotsplash.SlidingSplashView;

public class OtherRomsInfoActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getSupportActionBar().hide();

        setContentView(R.layout.activity_other_roms_info);

        SlidingSplashView splashView = (SlidingSplashView) findViewById(R.id.splashView);
        final Button button = (Button) findViewById(R.id.openInPlayStore);
        final TextView header = (TextView) findViewById(R.id.splashHeaderText);
        final TextView substitle = (TextView) findViewById(R.id.splashSubtitleText);
        splashView.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch(position){
                    case 0:
                        header.setText(getString(R.string.guide_0));
                        substitle.setText(getResources().getString(R.string.splash_subtitle_text));
                        button.setVisibility(View.GONE);
                        header.setVisibility(View.VISIBLE);
                        substitle.setVisibility(View.VISIBLE);
                        break;

                    case 1:
                        header.setText(getString(R.string.guide_1));
                        substitle.setVisibility(View.GONE);
                        button.setVisibility(View.VISIBLE);
                        button.setText(getString(R.string.button_1));
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://play.google.com/store/apps/details?id=com.gmail.heagoo.apkeditor&hl=en"));
                                startActivity(browserIntent);
                            }
                        });
                        break;

                    case 2:
                        button.setVisibility(View.GONE);
                        header.setText(getString(R.string.guide_2));
                        break;
                    case 3:
                        header.setText(getString(R.string.guide_3));
                        break;
                    case 4:
                        header.setText(getString(R.string.guide_4));
                        break;
                    case 5:
                        header.setText(getString(R.string.guide_5));
                        break;
                    case 6:
                        header.setText(getString(R.string.guide_6));
                        break;
                    case 7:
                        header.setText(getString(R.string.guide_7));
                        break;
                    case 8:
                        header.setText(getString(R.string.guide_8));
                        break;
                    case 9:
                        header.setText(getString(R.string.guide_9));
                        break;
                    case 10:
                        header.setText(getString(R.string.guide_10));
                        break;
                    case 11:
                        header.setText(getString(R.string.guide_11));
                        button.setText(getString(R.string.button_11));
                        button.setVisibility(View.VISIBLE);
                        button.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent i = new Intent(getApplicationContext(), MainActivity.class);
                                startActivity(i);
                                finish();
                            }
                        });
                        break;
                }

            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(this,MainActivity.class);
        startActivity(intent);
        finish();
    }
}
