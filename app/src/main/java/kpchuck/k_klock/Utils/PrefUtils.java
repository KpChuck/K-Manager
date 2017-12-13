package kpchuck.k_klock.Utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.widget.CheckBox;
import android.widget.Switch;

import java.util.ArrayList;

/**
 * Created by karol on 29/10/17.
 */

public class PrefUtils {

    private Context mContext;
    private String prefFile = "prefFileName";
    SharedPreferences myPref;
    SharedPreferences.Editor editor;

    public PrefUtils (Context context){
        this.mContext=context;
        this.myPref = mContext.getSharedPreferences(prefFile, Context.MODE_PRIVATE);
        this.editor = myPref.edit();

    }

    public String getString(String stringPref, String defaultString){
        return myPref.getString(stringPref, defaultString);
    }

    public void putString (String prefName, String input){
        editor.putString(prefName, input);
        editor.apply();
    }

    public void putBool (String prefName, boolean input){
        editor.putBoolean(prefName, input);
        editor.apply();
    }

    public void remove (String prefName){
        editor.remove(prefName);
        editor.apply();
    }

    public int getInt(String prefName) {
        return myPref.getInt(prefName, 0);
    }

    public boolean getBool(String boolPref){
        return myPref.getBoolean(boolPref, false);
    }

    public boolean getBoolTrue(String boolPref){
        return myPref.getBoolean(boolPref, true);
    }

    public void setSwitchPrefs(Switch mswitch, String key){
        if(mswitch.isChecked()) {
            editor.putBoolean(key, true);
            editor.apply();
        }else{
            editor.putBoolean(key, false);
            editor.apply();
        }
    }

    public void setCheckboxPrefs(CheckBox mswitch, String key){
        if(mswitch.isChecked()) {
            editor.putBoolean(key, true);
            editor.apply();
        }else{
            editor.putBoolean(key, false);
            editor.apply();
        }
    }

    public void saveArray(ArrayList<String> arrayList, String arrayListKey)
    {
        //Removing old
       int key =  myPref.getInt(arrayListKey, 0);
        for(int i = 0; i<key; i++){
            editor.remove(arrayListKey + i);
        }
        editor.remove(arrayListKey);
        editor.commit();

        //Adding new
        editor.putInt(arrayListKey, arrayList.size());

        for(int m=0;m<arrayList.size();m++)
        {
            editor.putString(arrayListKey + m, arrayList.get(m));
        }

        editor.commit();
    }

    public ArrayList<String> loadArray(String arrayListKey)
    {
        ArrayList<String> arrayList = new ArrayList<>();
        arrayList.clear();
        int size = myPref.getInt(arrayListKey, 0);

        for(int i=0;i<size;i++)
        {
            arrayList.add(myPref.getString(arrayListKey + i, null));
        }
        return arrayList;

    }

    public void deleteArrayLists (String[] arrayListsKey){
        for (String s: arrayListsKey) deleteArrayList(s);
    }

    public void deleteArrayList (String arrayListKey){
        //Removing old
        int key =  myPref.getInt(arrayListKey, 0);
        for(int i = 0; i<key; i++){
            editor.remove(arrayListKey + i);
        }
        editor.remove(arrayListKey);
        editor.apply();
    }
}
