package kpchuck.kklock.activities;

import com.stephentuso.welcome.BasicPage;
import com.stephentuso.welcome.TitlePage;
import com.stephentuso.welcome.WelcomeActivity;
import com.stephentuso.welcome.WelcomeConfiguration;

import kpchuck.kklock.R;

public class MyWelcomeActivity extends WelcomeActivity {

    @Override
    protected WelcomeConfiguration configuration() {
        return new WelcomeConfiguration.Builder(this)
                .defaultBackgroundColor(R.color.colorPrimaryDark)
                .page(new TitlePage(R.drawable.ic_watch_black_24dp,
                        "K-Klock")
                )
                .page(new BasicPage(R.drawable.transparent,
                        getString(R.string.how_to_use),
                        getString(R.string.screen1_1) + "\n" +
                                getString(R.string.screen1_2)
                                )

                )
                .page(new BasicPage(R.drawable.transparent,
                        getString(R.string.hscreen2_title),
                        "\t\t" + getString(R.string.hscreen2_1) + "\n" +
                                getString(R.string.hscreen2_2) + "\n\n" +
                                "\t\t" + getString(R.string.hscreen2_3))
                ).page(new BasicPage(R.drawable.transparent,
                        getString(R.string.hscreen3_title),
                        getString(R.string.hscreen3_1) + "\n" +
                                getString(R.string.hscreen3_2)))
                .swipeToDismiss(true)

                .build();
    }
}
