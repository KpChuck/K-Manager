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
import org.eclipse.egit.github.core.Issue;
import org.eclipse.egit.github.core.client.GitHubClient;
import org.eclipse.egit.github.core.client.RequestException;
import org.eclipse.egit.github.core.service.IssueService;
import org.json.JSONException;
import org.json.JSONObject;
import org.zeroturnaround.zip.ZipUtil;

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
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

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
        Button sendDetailsButton = findViewById(R.id.crash_send_details);
        sendDetailsButton.setOnClickListener(view -> {
            AlertDialog.Builder dialog = new AlertDialog.Builder(CustomCrashActivity.this, android.R.style.Theme_Material_Dialog);
            // Init custom view
            View textInput = getLayoutInflater().inflate(R.layout.input_menu_dialog, null);
            dialog.setView(textInput);
            textInput.findViewById(R.id.value).setVisibility(View.GONE);
            ((TextView) textInput.findViewById(R.id.title)).setText("Additional Details");
            EditText input = textInput.findViewById(R.id.name);
            input.setHint("");
            // Init dialog
            dialog.setPositiveButton(R.string.send, (dialogInterface, i) -> {
                String dets = input.getText().toString();
                prepareAndShare(dets.equals("") ? "K-Manager crashed, here's some files." : dets);
            });
            dialog.show();
        });

        Button githubIssueButton = findViewById(R.id.crash_github_issue);
        githubIssueButton.setOnClickListener(view -> {
            showDialog();
        });
    }

    private void showDialog(){
        AlertDialog.Builder dialog = new AlertDialog.Builder(CustomCrashActivity.this, android.R.style.Theme_Material_Dialog);
        // Init custom view
        View v = getLayoutInflater().inflate(R.layout.crash_github_issue, null);
        dialog.setView(v);
        RadioGroup choice = v.findViewById(R.id.github_radio_group);
        TextInputEditText title = v.findViewById(R.id.input_title);
        TextInputEditText description = v.findViewById(R.id.input_description);
        TextInputEditText username = v.findViewById(R.id.input_username);
        TextInputEditText password = v.findViewById(R.id.input_password);
        TextInputLayout usernameLayout = v.findViewById(R.id.input_username_layout);
        TextInputLayout passwordLayout = v.findViewById(R.id.input_password_layout);

        // Init dialog
        dialog.setPositiveButton(R.string.send, null);
        AlertDialog alertDialog = dialog.create();
        alertDialog.setOnShowListener(dialogInterface ->
                ((AlertDialog) dialogInterface).getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                    if (choice.getCheckedRadioButtonId() == R.id.github_browser_radio) {
                        openGithubBrowser();
                        dialogInterface.dismiss();
                    }
                    else {
                        if (validateInputs(title, description, username, password, usernameLayout, passwordLayout)) {
                            reportIssue(title, description, username, password);
                            dialogInterface.dismiss();
                        }
                    }
        }));
        alertDialog.show();
    }

    private void reportIssue(TextInputEditText title, TextInputEditText description, TextInputEditText username, TextInputEditText password){

        IssueModel model = new IssueModel(title.getText().toString(), description.getText().toString(),
                username.getText().toString(), password.getText().toString());
        new ReportGithubIssue(model).execute();
    }

    private boolean validateInputs(TextInputEditText title, TextInputEditText description,
                                   TextInputEditText username, TextInputEditText password,
                                   TextInputLayout usernameLayout, TextInputLayout passwordLayout){
        if (title.getText().toString().equals(""))
            title.setText("K-Manager Crashing Bug Report");
        if (description.getText().toString().equals(""))
            description.setText("It crashes.");
        if (username.getText().toString().equals("")){
            usernameLayout.setError("Your username cannot be blank.");
            return false;
        } else {
            usernameLayout.setError("");
        }
        if (password.getText().toString().equals("")){
            passwordLayout.setError("Your password cannot be blank");
            return false;
        } else {
            passwordLayout.setError("");
        }
        return true;
    }

    private void openGithubBrowser(){
        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/KpChuck/K-Manager/issues/new?template=bug_report.md"));
        startActivity(i);
    }

    private void copyErrorToClipboard() {
        String errorInformation = CustomActivityOnCrash.getAllErrorDetailsFromIntent(CustomCrashActivity.this, getIntent());
        new FileHelper().copyToClipBoard(CustomCrashActivity.this, errorInformation);
    }

    private void  prepareAndShare(String emailBody){
        // Prepare log file and Apk Zip
        List<File> sendFiles = prepareFiles();

        Intent i = new Intent(Intent.ACTION_SEND);
        i.setType(MAIL_TYPE);
        i.putExtra(Intent.EXTRA_EMAIL, new String[]{"przestrzelski.com@gmail.com"});
        i.putExtra(Intent.EXTRA_SUBJECT, "K-Manager Bug Report");
        i.putExtra(Intent.EXTRA_TEXT, emailBody);
        for (File f : sendFiles)
            i.putExtra(Intent.EXTRA_STREAM, FileProvider.getUriForFile(
                    CustomCrashActivity.this,
                    BuildConfig.APPLICATION_ID + ".fileprovider",
                    f));
        try {
            startActivity(Intent.createChooser(i, " Send bug report using ... "));
        } catch (ActivityNotFoundException ex) {
            Toast.makeText(CustomCrashActivity.this,
                    "An error occurred while sending this error",
                    Toast.LENGTH_LONG)
                    .show();
        }
    }

    private List<File> prepareFiles(){
        List<File> files = new ArrayList<>();
        File outputDir = new File(Environment.getExternalStorageDirectory() + "/K-Manager/");
        if (!outputDir.exists()) outputDir.mkdirs();
        String errorInformation = CustomActivityOnCrash.getAllErrorDetailsFromIntent(getApplicationContext(), getIntent());
        File kklock = new File(Environment.getExternalStorageDirectory() +"/K-Klock/K-Klock.apk");
        File sysui = new File(Environment.getExternalStorageDirectory() + "/K-Klock/userInput/SystemUI.apk");
        File logFile = new File(outputDir, "crash.log");
        File zipApks = new File(outputDir, "CrashFiles.zip");
        try {
            for (File f : new File[]{zipApks, logFile}){
                if (f.exists() && f.isFile()) f.delete();
            }
            IOUtils.write(errorInformation, new FileOutputStream(logFile), "utf-8");
            files.add(logFile);
            ZipUtil.packEntry(logFile, zipApks);
            files.add(zipApks);
            if (sysui.exists()){
                ZipUtil.addEntry(zipApks, sysui.getName(), sysui);
            }
            if (kklock.exists()){
                ZipUtil.addEntry(zipApks, kklock.getName(), kklock);
            }

        }  catch (Exception e){
            String s = e.getMessage();
        }
        return files;
    }

    class ReportGithubIssue extends AsyncTask<Void, String, String>{

        private IssueModel issueModel;
        private static final int STATUS_BAD_CREDENTIALS = 401;
        private static final int STATUS_ISSUES_NOT_ENABLED = 410;
        private String fileURL = "";
        private Snackbar bar;
        private ProgressBar progressBar;

        public ReportGithubIssue(IssueModel model){
            this.issueModel=model;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            bar = Snackbar.make(findViewById(R.id.root_activity_crash), "Creating Issue", Snackbar.LENGTH_INDEFINITE);
            ViewGroup contentLay = (ViewGroup) bar.getView().findViewById(android.support.design.R.id.snackbar_text).getParent();
            progressBar = new ProgressBar(getApplicationContext());
            contentLay.addView(progressBar,0);
            bar.show();
            progressBar.setProgress(0, true);
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            progressBar.setProgress(100, true);
            bar.setText(result);
            bar.setAction("Restart", view -> {
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED);
                finish();
                startActivity(intent);
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(10);
            });
        }

        @Override
        protected void onProgressUpdate(String... values) {
            super.onProgressUpdate(values);
            progressBar.setProgress(Integer.valueOf(values[0]), true);
            bar.setText(values[1]);
        }

        private boolean uploadCrashFiles() throws JSONException,  IOException{
            List<File> files = prepareFiles();
            File file = null;
            for (File f:files){
                if (f.getName().equals("CrashFiles.zip"))
                    file = f;
            }
            if (file == null) return true;

            OkHttpClient client = new OkHttpClient();
            String url = "http://karol.leelah.netsoc.co:8080/uploadKManager";

            RequestBody formBody = new MultipartBody.Builder()
                    .setType(MultipartBody.FORM)
                    .addFormDataPart("fileupload", file.getName(),
                            RequestBody.create(MediaType.parse("application/zip"), file))
                    .build();
            Request request = new Request.Builder().url(url).post(formBody).build();
            Response response = client.newCall(request).execute();

            if (!response.isSuccessful()) throw new IOException("Unexpected code " + response);

            String jsonString = response.body().string();

            JSONObject jsonObject = new JSONObject(jsonString);
            if (jsonObject.getBoolean("Success")) {
                fileURL = jsonObject.getString("URL");
                return true;
            }
            return false;
        }

        @Override
        protected String doInBackground(Void... voids) {
            GitHubClient client = new GitHubClient().setCredentials(issueModel.getUsername(), issueModel.getPassword());
            try {
                publishProgress("50", "Uploading crash files");
                uploadCrashFiles();
                publishProgress("90", "Creating issue");

                Issue issue = new Issue();
                issue.setTitle(issueModel.getTitle());
                StringBuilder body = new StringBuilder(issueModel.getDescription());
                body.append("\n");
                body.append(String.format("\n[CrashFiles.zip]{%s}\n", fileURL));
                body.append(String.format("```%s```", CustomActivityOnCrash.getAllErrorDetailsFromIntent(getApplicationContext(), getIntent())));
                issue.setBody(body.toString());
                new IssueService(client).createIssue("KpChuck", "K-Manager", issue);

                return "Successfully created new issue.";
            } catch (RequestException e) {
                if (e.getStatus() == STATUS_BAD_CREDENTIALS)
                    return "Error creating login, try again";
                e.printStackTrace();
                return "Error creating issue. Try again later.";
            } catch (JSONException l){
                l.printStackTrace();
                return "Error uploading CrashFiles.zip. Try again later";
            } catch (IOException e1) {
                e1.printStackTrace();
                return "Error creating issue. Try again later.";
            }
        }
    }

    class IssueModel{

        private String title, description, username, password;

        public IssueModel(String title, String description, String username, String password){
            this.title=title;
            this.description=description;
            this.username=username;
            this.password=password;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDescription() {
            return description;
        }

        public void setDescription(String description) {
            this.description = description;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
