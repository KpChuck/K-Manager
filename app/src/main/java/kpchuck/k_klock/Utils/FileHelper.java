package kpchuck.k_klock.Utils;

import android.content.ContentResolver;
import android.content.ContentValues;
import static android.content.ContentValues.TAG;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import java.io.File;
import java.util.ArrayList;
import java.util.Set;

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
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(getImageContentUri(ctx, file.getAbsolutePath() ), "application/vnd.android.package-archive");
        ctx.startActivity(intent);
    }

    private static Uri getImageContentUri(Context context, String absPath) {
        Log.v(TAG, "getImageContentUri: " + absPath);

        Cursor cursor = context.getContentResolver().query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI
                , new String[] { MediaStore.Images.Media._ID }
                , MediaStore.Images.Media.DATA + "=? "
                , new String[] { absPath }, null);

        if (cursor != null && cursor.moveToFirst()) {
            int id = cursor.getInt(cursor.getColumnIndex(MediaStore.MediaColumns._ID));
            return Uri.withAppendedPath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI , Integer.toString(id));

        } else if (!absPath.isEmpty()) {
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DATA, absPath);
            return context.getContentResolver().insert(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
        } else {
            return null;
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
