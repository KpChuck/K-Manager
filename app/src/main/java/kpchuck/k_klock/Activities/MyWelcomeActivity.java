package kpchuck.k_klock.Activities;

import android.os.Bundle;

import com.stephentuso.welcome.BasicPage;
import com.stephentuso.welcome.TitlePage;
import com.stephentuso.welcome.WelcomeActivity;
import com.stephentuso.welcome.WelcomeConfiguration;

import kpchuck.k_klock.R;

public class MyWelcomeActivity extends WelcomeActivity {

    @Override
    protected WelcomeConfiguration configuration() {
        return new WelcomeConfiguration.Builder(this)
                .defaultBackgroundColor(R.color.colorPrimaryDark)
                .page(new TitlePage(R.drawable.ic_watch_black_24dp,
                        "K-Klock")
                )
                .page(new BasicPage(R.drawable.ic_watch_black_24dp,
                        "How to use",
                        "Select your Rom and any other settings in K-Manager. " +
                                "Then tap the yellow build button to get a K-Klock theme with that configuration! "
                                )

                )
                .page(new BasicPage(R.drawable.ic_watch_black_24dp,
                        "What the Clock Style option means in Substratum",
                        "\t\tThe first part of the clock style option specifies how the clock should behave.\n" +
                                "You can choose whether the clock should appear on the lockscreen or not. \n\n" +
                                "\t\tStock Clock or Dynamic Clock are hidden on the lockscreen without needing to select anything. \n" +
                                "These two are different because they make the clock dynamic, ie. it changes color against white backgrounds. ")
                ).page(new BasicPage(R.drawable.ic_watch_black_24dp,
                        "Help and Support",
                        "If you\'re still confused about anything check the navdrawer! "))
                .swipeToDismiss(true)
                .build();
    }
}
