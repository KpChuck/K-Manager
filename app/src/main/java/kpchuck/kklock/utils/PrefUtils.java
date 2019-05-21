package kpchuck.kklock.utils;

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

    private String getKey(int key){
        return mContext.getString(key);
    }

    public void deleteAllPrefs(){
        editor.clear().commit();
    }

    public String getIntString(String stringPref, int defaultValue){
        return String.valueOf(myPref.getInt(stringPref, defaultValue));
    }

    public String getIntString(int stringPref, int defaultValue){
        return getIntString(getKey(stringPref), defaultValue);
    }

    public String getString(String stringPref, String defaultString){
        return myPref.getString(stringPref, defaultString);
    }

    public String getString(int stringPref, String defaultString){
        return getString(getKey(stringPref), defaultString);
    }

    public void putString (String prefName, String input){
        editor.putString(prefName, input);
        editor.apply();
    }

    public void putString(int prefName, String input){
        putString(getKey(prefName), input);
    }

    public void putBool (String prefName, boolean input){
        editor.putBoolean(prefName, input);
        editor.apply();
    }

    public void putBool(int prefName, boolean input){
        putBool(getKey(prefName), input);
    }

    public void remove (String prefName){
        editor.remove(prefName);
        editor.apply();
    }

    public void remove(int prefName){
        remove(getKey(prefName));
    }

    public int getInt(String prefName) {
        return myPref.getInt(prefName, 0);
    }

    public int getInt(int prefName){
        return getInt(getKey(prefName));
    }

    public boolean getBool(String boolPref){
        return myPref.getBoolean(boolPref, false);
    }

    public boolean getBool(int boolPref){
        return getBool(getKey(boolPref));
    }

    public boolean getBoolTrue(String boolPref){
        return myPref.getBoolean(boolPref, true);
    }

    public boolean getBoolTrue(int boolPref){
        return getBoolTrue(getKey(boolPref));
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

    public void setSwitchPrefs(Switch mswitch, int key){
        setSwitchPrefs(mswitch, getKey(key));
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

    public void setCheckboxPrefs(CheckBox checkBox, int key){
        setCheckboxPrefs(checkBox, getKey(key));
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

    public void saveArray(ArrayList<String> arrayList, int arrayListKey){
        saveArray(arrayList, getKey(arrayListKey));
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

    public ArrayList<String> loadArray(int arrayListKey){
        return loadArray(getKey(arrayListKey));
    }

    public void deleteArrayLists (String[] arrayListsKey){
        for (String s: arrayListsKey) deleteArrayList(s);
    }

    public void deleteArrayLists(int[] arrayListsKey){
        for (int key : arrayListsKey){
            deleteArrayList(getKey(key));
        }
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

    public void deleteArrayList(int arrayListKey){
        deleteArrayList(getKey(arrayListKey));
    }

}
