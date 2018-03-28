package com.dev.eipeks.matrixplayer.core.managers;

import android.app.Activity;
import android.view.Gravity;
import android.widget.Toast;

/**
 * Created by eipeks on 3/23/18.
 */

public class PopUpManager {

    private Activity activity;

    public PopUpManager(Activity activity){
        this.activity = activity;
    }

    private void makeToast(String message){
        Toast toast = Toast.makeText(this.activity, message, Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.CENTER, 0, 0);
        toast.show();
    }

}
