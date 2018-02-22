package kpchuck.kklock.utils;

import android.util.Log;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

/**
 * Created by karol on 11/01/18.
 */

public class SuUtils {

    public boolean hasRoot(){
        String check = runCommand("which su").get(0);
        return (check != null) && !check.isEmpty();
    }

    public ArrayList<String> runCommand(String command){
        ArrayList<String> result = new ArrayList<>();
        try {
            Process p = Runtime.getRuntime().exec(command);

            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));

            // read the output from the command
            String s = null;
            while ((s = stdInput.readLine()) != null) {
                result.add(s);
            }
        }catch (IOException e){
            Log.e("klock", e.getMessage());
        }

        return result;
    }

    public ArrayList<String> runSuCommand(String command){
        return runSuCommands(new String[]{command});
    }

    public ArrayList<String> runSuCommands(String[] command){
        ArrayList<String> result = new ArrayList<>();
        try {
            Process p = Runtime.getRuntime().exec("su");

            DataOutputStream outputStream = new DataOutputStream(p.getOutputStream());
            BufferedReader stdInput = new BufferedReader(new
                    InputStreamReader(p.getInputStream()));
            for (String c : command) {
                outputStream.writeBytes(c + "\n");
                outputStream.flush();
            }
       //     p.waitFor();
            outputStream.writeBytes("exit\n");
            outputStream.flush();


            p.waitFor();
            // read the output from the command
            String s = null;
            while ((s = stdInput.readLine()) != null) {
                result.add(s);
            }

        }catch (Exception e){
            Log.e("klock", e.getMessage());
        }

        return result;
    }

}
