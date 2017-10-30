package kpchuck.k_klock.Services;

import android.service.quicksettings.Tile;
import android.service.quicksettings.TileService;
import android.util.Log;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

import eu.chainfire.libsuperuser.Shell;

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
                Shell.SU.run("killall com.android.systemui");

            } catch (Exception e) {
                Log.e(TAG, "Error killing system UI"+ e.getMessage());
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
