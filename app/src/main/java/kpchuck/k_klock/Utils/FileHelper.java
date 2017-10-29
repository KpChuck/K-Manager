package kpchuck.k_klock.Utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import static android.content.ContentValues.TAG;
import static android.os.Build.VERSION.SDK;
import static android.support.v4.content.FileProvider.getUriForFile;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.util.Log;
import java.io.File;
import java.util.ArrayList;
import java.util.Set;

import eu.chainfire.libsuperuser.Shell;

/**
 * Created by Karol Przestrzelski on 11/08/2017.
 */

public class FileHelper {

    private Context ctx;

    public File newFolder(String path){
        File file = new File(path);
        if (file.exists() && file.isDirectory()) {return  file;}
        else{
            file.mkdirs();
            return file;
        }

    }

    public void installApk(File file, Context context){
        this.ctx=context;

        PackageManager packageManager = context.getPackageManager();
       try {
           if (Build.VERSION.SDK_INT == 26){
               if (packageManager.canRequestPackageInstalls()) {
                   final Intent intent = new Intent(Intent.ACTION_VIEW)
                           .setDataAndType(getUriForFile(ctx,
                                   ctx.getPackageName() + ".fileprovider", file),
                                   "application/vnd.android.package-archive").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                   ctx.startActivity(intent);
               }}
           else {
                final Intent intent = new Intent(Intent.ACTION_VIEW)
                           .setDataAndType(getUriForFile(ctx,
                                   ctx.getPackageName() + ".fileprovider", file),
                                   "application/vnd.android.package-archive").addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                   ctx.startActivity(intent);
           }
       }catch (Exception e){
        Log.e("klock", e.getMessage());
    }
    }

    public ArrayList<String> deleteItemFromArray(String item, ArrayList<String> tempList){

        for(int i=0; i<tempList.size(); i++){
            String tempString = tempList.get(i);
            if(tempString.equals(item)) {
                tempList.remove(i);
                break;
            }
        }
        return tempList;

    }

}
