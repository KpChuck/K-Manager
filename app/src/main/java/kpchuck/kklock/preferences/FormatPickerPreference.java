package kpchuck.kklock.preferences;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.preference.Preference;
import androidx.annotation.ColorInt;

import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import kpchuck.kklock.R;
import kpchuck.kklock.utils.PrefUtils;

public class FormatPickerPreference extends Preference{

    private String enabledKey;
    private PrefUtils prefUtils;
    private Button editFormat;

    public FormatPickerPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(attrs);
    }

    public void init(AttributeSet attrs){
        prefUtils = new PrefUtils(getContext());
        prefUtils.putBool(enabledKey, prefUtils.getBoolTrue(enabledKey));

        setLayoutResource(R.layout.recycler_view);
        TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.FormatPickerPreference);
        enabledKey = a.getString(R.styleable.FormatPickerPreference_enabledKey);
        a.recycle();
        setLayoutResource(R.layout.format_preference);
    }

    @Override
    protected void onBindView(View view) {
        super.onBindView(view);
        Button formatHelp = view.findViewById(R.id.formatHelp);
        editFormat = view.findViewById(R.id.formatTextViewButton);

        setFormatText();

        formatHelp.setOnClickListener(v -> showFormatInfo());
        editFormat.setOnClickListener(v -> showFormatDialog());
    }

    private void setFormatText(){
        String text = prefUtils.getString(getKey(), "");
        if (!text.equals(""))
            text = " ( " + text + " )";
        if (prefUtils.getBool(enabledKey)) {
            editFormat.setText(getTitle().toString() + text);
        } else {
            editFormat.setText(getTitle());
        }
    }

    private void showFormatInfo(){
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );
        View view = inflater.inflate(R.layout.webview_layout, null);
        builder.setView(view);
        WebView webView = view.findViewById(R.id.web_view);
        String format_info = "<style> * {background-color: %s; color: %s; }</style>\n" +
                "            <b>Clock Format Codes</b>\n" +
                "            <ul>\n" +
                "                <li>d.............Day Of Month (single digit) 7</li>\n" +
                "                <li>dd.......... Day Of Month (double digit) Zero, 07</li>\n" +
                "                <li>EEEE......Day Of Week (Full) Monday</li>\n" +
                "                <li>EEE........Week Day (Short) Mon</li>\n" +
                "                <li>MMMM....Month (Full) AUGUST</li>\n" +
                "                <li>MMM.......Month (Short) AUG</li>\n" +
                "                <li>MM..........Month (double digit) 08</li>\n" +
                "                <li>M............Month (Single digit) 8</li>\n" +
                "                <li>yyyy........Year (Full) 2013</li>\n" +
                "                <li>yy............Year (Short) 13</li>\n" +
                "                <li>h..............Hour (12 hour, single digit) 8</li>\n" +
                "                <li>hh............Hour (12 hour, double digit) 08</li>\n" +
                "                <li>H.............Hour (24 hour, single digit) 8 20</li>\n" +
                "                <li>HH...........Hour (24 hour, double digit) 08 20 (Note: some roms use kk instead)</li>\n" +
                "                <li>M.............Minute (single digit) 9</li>\n" +
                "                <li>MM..........Minute (double digit) 09</li>\n" +
                "                <li>s..............Second (single digit) 9</li>\n" +
                "                <li>ss............Second (double digit) 09</li>\n" +
                "                <li>a..............Marker AM/PM</li>\n" +
                "            </ul>\n" +
                "            <br/>\n" +
                "            <b>These can then be styled with HTML</b>\n" +
                "            <ul>\n" +
                "                <li>&lt;b&gt;...&lt;/b&gt; ..................................makes the enclosed text bold</li>\n" +
                "                <li>&lt;i&gt;...&lt;/i&gt; ...................................makes the enclosed text italic</li>\n" +
                "                <li>&lt;font size =\"X\"&gt;...&lt;/font&gt; ............sets the font size of the enclosed text to X.0dip</li>\n" +
                "                <li>&lt;font fgcolor =\"#ffffffff\"&gt;...&lt;/&gt; ........sets the foreground colour of the enclosed text</li>\n" +
                "                <li>&lt;font bgcolor =\"#ff000000\"&gt;...&lt;/&gt; .sets the background colour of the enclosed text</li>\n" +
                "                <li>&lt;big&gt;...&lt;/big&gt;.....................increases the font size of the enclosed text</li>\n" +
                "                <li>&lt;small&gt;...&lt;/small&gt;......................decreases the font size of the enclosed text</li>\n" +
                "            </ul>\n" +
                "        </body>";
        String info = String.format(format_info, fetchColor(R.attr.secondaryBackgroundColor), fetchColor(R.attr.textColor));
        webView.loadDataWithBaseURL(null, info, "text/html", "utf-8", null);

        builder.setTitle("")
                .setPositiveButton(R.string.okay, (dialogInterface, i) -> dialogInterface.dismiss());
        builder.show();
    }

    private void showFormatDialog(){

        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), android.R.style.Theme_Material_Dialog_Alert);
        LayoutInflater inflater = (LayoutInflater) getContext().getSystemService( Context.LAYOUT_INFLATER_SERVICE );

        View view = inflater.inflate(R.layout.input_menu_dialog, null);
        builder.setView(view);

        TextView textView = view.findViewById(R.id.title);
        EditText nameEdit = view.findViewById(R.id.name);
        nameEdit.setVisibility(View.GONE);
        EditText valueEdit = view.findViewById(R.id.value);
        textView.setText("Enter a format");
        valueEdit.setText(prefUtils.getString(getKey(), ""));

        valueEdit.setText(prefUtils.getBool(enabledKey) ? "" : prefUtils.getString(getKey(), ""));

        builder.setPositiveButton(getContext().getString(R.string.save), (dialogInterface, i) -> {
            String value = valueEdit.getText().toString();
            persistString(value);
            setFormatText();
        });
        builder.setNegativeButton(getContext().getString(R.string.cancel), (dialogInterface, i) -> {
            dialogInterface.cancel();
        });

        AlertDialog alertDialog = builder.create();
        // Show it
        alertDialog.show();

    }

    private String fetchColor( int id ) {
        TypedValue typedValue = new TypedValue();
        Resources.Theme theme = getContext().getTheme();
        theme.resolveAttribute(id, typedValue, true);
        @ColorInt int color = typedValue.data;
        return String.format("#%06X", (0xFFFFFF & color));
    }

}


