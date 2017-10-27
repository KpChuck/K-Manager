package kpchuck.k_klock.Dialog;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import kpchuck.k_klock.Interfaces.PositiveClickListener;
import kpchuck.k_klock.R;

/**
 * Created by karol on 27/10/17.
 */

public class DialogHelper extends AppCompatActivity {

    public void simpleDialogText(String title, String message, final PositiveClickListener positiveClick){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(title);
        TextView tv = new TextView(this);
        tv.setText(message);
        builder.setView(tv);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
               positiveClick.onBtnClick();
            }
        });
        builder.setOnCancelListener(new DialogInterface.OnCancelListener(){

            @Override
            public void onCancel(DialogInterface dialogInterface) {

            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        builder.show();

    }
}
