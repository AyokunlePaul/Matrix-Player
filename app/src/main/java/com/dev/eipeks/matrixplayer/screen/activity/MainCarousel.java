package com.dev.eipeks.matrixplayer.screen.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.dev.eipeks.matrixplayer.R;
import com.dev.eipeks.matrixplayer.core.managers.PermissionsManager;
import com.dev.eipeks.matrixplayer.core.view.CoreActivity;
import com.dev.eipeks.matrixplayer.databinding.MainLayoutBinding;

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
                requestCode == PermissionsManager.WRITE_EXTERNAL_PERMISSION_CODE){
            startActivity(new Intent(this, MainActivity.class));
            finish();
            return;
        }

        Toast.makeText(this, "Unable to access music files", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void checkForPermissions(){
        if (!permissionsManager.checkForWriteExternalPermission()){
            new AlertDialog.Builder(this)
                    .setMessage(getResources().getString(R.string.permission_message))
                    .setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            permissionsManager.requestWriteExternalPermission();
                        }
                    }).show();
        } else {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }
    }

}
