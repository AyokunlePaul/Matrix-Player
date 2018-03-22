package com.dev.eipeks.lecteur.core_package.managers;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;

/**
 * Created by eipeks on 3/19/18.
 */

public class PermissionsManager {

    private Activity activity;

    public static int READ_EXTERNAL_PERMISSION_CODE = 0;
    public static int WRITE_EXTERNAL_PERMISSION_CODE = 1;

    public PermissionsManager(Activity activity){
        this.activity = activity;
    }

    public boolean checkForReadExternalPermission(){
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);
        return permission == PackageManager.PERMISSION_GRANTED;
    }

    public void requestReadExternalPermission(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.READ_EXTERNAL_STORAGE)){
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_PERMISSION_CODE);
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, READ_EXTERNAL_PERMISSION_CODE);
        }
    }

    public boolean checkForWriteExternalPermission(){
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        return permission == PackageManager.PERMISSION_GRANTED;
    }

    public void requestWriteExternalPermission(){
        if (ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)){
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_PERMISSION_CODE);
        } else {
            ActivityCompat.requestPermissions(activity, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, WRITE_EXTERNAL_PERMISSION_CODE);
        }
    }

}
