package com.dev.eipeks.lecteur.screen.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.dev.eipeks.lecteur.R;
import com.dev.eipeks.lecteur.core_package.managers.PermissionsManager;
import com.dev.eipeks.lecteur.core_package.view.CoreActivity;
import com.dev.eipeks.lecteur.databinding.MainLayoutBinding;

/**
 * Created by eipeks on 3/24/18.
 */

public class MainCarousel extends CoreActivity {

    private MainLayoutBinding binding;

    private PermissionsManager permissionsManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.main_layout);

        permissionsManager = new PermissionsManager(this);

        checkForPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                requestCode == PermissionsManager.READ_EXTERNAL_PERMISSION_CODE){
            Toast.makeText(this, "Access granted", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            Toast.makeText(this, "Unable to access music files", Toast.LENGTH_SHORT).show();
            finish();
        }


    }

    private void checkForPermissions(){
        if (!permissionsManager.checkForReadExternalPermission()){
            new AlertDialog.Builder(this)
                    .setMessage(getResources().getString(R.string.permission_message))
                    .setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    }).show();
            permissionsManager.requestReadExternalPermission();

            return;
        }

        startActivity(new Intent(this, MainActivity.class));
    }


}
