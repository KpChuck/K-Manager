package kpchuck.kklock.activities;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.FileProvider;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cat.ereza.customactivityoncrash.CustomActivityOnCrash;
import cat.ereza.customactivityoncrash.config.CaocConfig;
import kpchuck.kklock.BuildConfig;
import kpchuck.kklock.MainActivity;
import kpchuck.kklock.R;
import kpchuck.kklock.utils.FileHelper;
import kpchuck.kklock.utils.PrefUtils;

import static kpchuck.kklock.constants.PrefConstants.PREF_BLACK_THEME;

public class CustomCrashActivity extends AppCompatActivity {

    final String MAIL_TYPE = "message/rfc822";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_custom_crash);
        PrefUtils prefUtils = new PrefUtils(this);
        setTheme(prefUtils.getBool(PREF_BLACK_THEME) ? R.style.AppTheme_Dark : R.style.AppTheme);

        //Close/restart button logic:
        //If a class if set, use restart.
        //Else, use close and just finish the app.
        //It is recommended that you follow this logic if implementing a custom error activity.
        Button restartButton = findViewById(R.id.customactivityoncrash_error_activity_restart_button);

        final CaocConfig config = CustomActivityOnCrash.getConfigFromIntent(getIntent());

        if (config == null) {
            //This should never happen - Just finish the activity to avoid a recursive crash.
            finish();
            return;
        }

        if (config.isShowRestartButton() && config.getRestartActivityClass() != null) {
            restartButton.setText(R.string.customactivityoncrash_error_activity_restart_app);
            restartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CustomActivityOnCrash.restartApplication(CustomCrashActivity.this, config);
                }
            });
        } else {
            restartButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    CustomActivityOnCrash.closeApplication(CustomCrashActivity.this, config);
                }
            });
        }

        Button moreInfoButton = findViewById(R.id.customactivityoncrash_error_activity_more_info_button);

        if (config.isShowErrorDetails()) {
            moreInfoButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //We retrieve all the error data and show it

                    AlertDialog dialog = new AlertDialog.Builder(CustomCrashActivity.this, android.R.style.Theme_Material_Dialog_Alert)
                            .setTitle(R.string.customactivityoncrash_error_activity_error_details_title)
                            .setMessage(CustomActivityOnCrash.getAllErrorDetailsFromIntent(CustomCrashActivity.this, getIntent()))
                            .setPositiveButton(R.string.customactivityoncrash_error_activity_error_details_close, null)
                            .setNeutralButton(R.string.customactivityoncrash_error_activity_error_details_copy,
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            copyErrorToClipboard();
                                        }
                                    })
                            .show();
                    TextView textView = dialog.findViewById(android.R.id.message);
                    if (textView != null) {
                        textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.customactivityoncrash_error_activity_error_details_text_size));
                    }
                }
            });
        } else {
            moreInfoButton.setVisibility(View.GONE);
        }

        Integer defaultErrorActivityDrawableId = config.getErrorDrawable();
        ImageView errorImageView = findViewById(R.id.customactivityoncrash_error_activity_image);

        if (defaultErrorActivityDrawableId != null) {
            errorImageView.setImageDrawable(ResourcesCompat.getDrawable(getResources(), defaultErrorActivityDrawableId, getTheme()));
        }

        // Now for the stuff I'm adding
        findViewById(R.id.crash_telegram_group).setOnClickListener(view -> {
            copyErrorToClipboard();
            Toast.makeText(CustomCrashActivity.this, "Error details copied to clipboard.\nPaste them into the github issue", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://t.me/kklock"));
            startActivity(intent);
        });

        findViewById(R.id.crash_gmail).setOnClickListener(view -> {
            prepareAndShare();
        });

        findViewById(R.id.crash_github_issue).setOnClickListener(view -> {
            copyErrorToClipboard();
            Toast.makeText(CustomCrashActivity.this, "Error details copied to clipboard.\nPaste them into the github issue", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(CustomCrashActivity.this.getString(R.string.github_issues)));
            startActivity(intent);
        });

        findViewById(R.id.crash_clear_app_data).setOnClickListener(view -> {
            prefUtils.deleteAllPrefs();
            CustomActivityOnCrash.restartApplication(CustomCrashActivity.this, config);
        });
    }


    private void copyErrorToClipboard() {
        String errorInformation = CustomActivityOnCrash.getAllErrorDetailsFromIntent(CustomCrashActivity.this, getIntent());
        new FileHelper().copyToClipBoard(CustomCrashActivity.this, errorInformation);
    }

    private void  prepareAndShare(){

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType(MAIL_TYPE);
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"przestrzelski.com@gmail.com"});
        addShareFlags(i);
        try {
            startActivity(Intent.createChooser(i, " Send bug report using ... "));
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(CustomCrashActivity.this,
                    "An error occurred while sending this error",
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    private void addShareFlags(Intent i){
        File logFile = getErrorFile();

        i.putExtra(Intent.EXTRA_SUBJECT, "K-Manager Bug Report");
        i.putExtra(Intent.EXTRA_TEXT, "Hi,\nK-Manager crashed, can you help fix it?");
        if (logFile != null)
            i.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                    CustomCrashActivity.this,
                    BuildConfig.APPLICATION_ID + ".fileprovider",
                    logFile));
    }

    @Nullable
    private File getErrorFile(){
        File outputDir = new File(Environment.getExternalStorageDirectory() + "/K-Manager/");
        if (!outputDir.exists()) outputDir.mkdirs();
        String errorInformation = CustomActivityOnCrash.getAllErrorDetailsFromIntent(getApplicationContext(), getIntent());
        File logFile = new File(outputDir, "crash.log");
        try {
            IOUtils.write(errorInformation, new FileOutputStream(logFile), "utf-8");
            return logFile;
        }  catch (IOException e){
            String s = e.getMessage();
            return null;
        }
    }
}
