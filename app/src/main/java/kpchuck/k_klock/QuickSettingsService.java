package kpchuck.k_klock;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Created by Karol Przestrzelski on 17/08/2017.
 */



public class QuickSettingsService
        extends TileService {

    String TAG = "klock";

    /**
     * Called when the tile is added to the Quick Settings.
     * @return TileService constant indicating tile state
     */

    @Override
    public void onTileAdded() {
        Log.d("QS", "Tile added");
        getQsTile().setState(Tile.STATE_ACTIVE);
    }

    /**
     * Called when this tile begins listening for events.
     */
    @Override
    public void onStartListening() {
        Log.d("QS", "Start listening");
    }

    /**
     * Called when the user taps the tile.
     */
    @Override
    public void onClick() {

        restartSystemUi();    }

    private void restartSystemUi() {

            try {
                ProcessBuilder builder = new ProcessBuilder( "su" );

                Process p = builder.start();

                //get stdin of shell
                BufferedWriter p_stdin =
                        new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));

                p_stdin.write("killall com.android.systemui");
                p_stdin.newLine();
                p_stdin.flush();

                p_stdin.write("exit");
                p_stdin.newLine();
                p_stdin.flush();
            } catch (Exception e) {
                Log.e(TAG, "Error killing system UI", e);
            }
    }

    /**
     * Called when this tile moves out of the listening state.
     */
    @Override
    public void onStopListening() {
        Log.d("QS", "Stop Listening");
    }

    /**
     * Called when the user removes this tile from Quick Settings.
     */
    @Override
    public void onTileRemoved() {
        Log.d("QS", "Tile removed");
    }



    }
