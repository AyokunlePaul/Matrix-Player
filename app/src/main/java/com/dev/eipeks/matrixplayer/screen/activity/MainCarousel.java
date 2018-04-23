package com.dev.eipeks.matrixplayer.screen.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.dev.eipeks.matrixplayer.MainApplication;
import com.dev.eipeks.matrixplayer.R;
import com.dev.eipeks.matrixplayer.core.dagger.component.MainComponent;
import com.dev.eipeks.matrixplayer.core.managers.PermissionsManager;
import com.dev.eipeks.matrixplayer.core.model.SongModel;
import com.dev.eipeks.matrixplayer.core.view.CoreActivity;
import com.dev.eipeks.matrixplayer.databinding.MainCarouselBinding;
import com.dev.eipeks.matrixplayer.databinding.MainLayoutBinding;
import com.dev.eipeks.matrixplayer.screen.viewmodel.MainVM;

import java.util.List;

import javax.inject.Inject;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.observers.DisposableSingleObserver;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by eipeks on 3/24/18.
 */

public class MainCarousel extends CoreActivity {

    private MainCarouselBinding binding;

    private PermissionsManager permissionsManager;

    private MainComponent component;

    @Inject
    MainVM mainVM;

    @Inject
    Context context;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = DataBindingUtil.setContentView(this, R.layout.main_carousel);

        component = MainApplication.get(this).getComponent();
        component.inject(this);

        permissionsManager = new PermissionsManager(this);

        checkForPermissions();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults[0] == PackageManager.PERMISSION_GRANTED &&
                requestCode == PermissionsManager.WRITE_EXTERNAL_PERMISSION_CODE){
            querySongs();
            return;
        }

        Toast.makeText(this, "Unable to access music files", Toast.LENGTH_SHORT).show();
        finish();
    }

    private void checkForPermissions(){
        if (!permissionsManager.checkForWriteExternalPermission()){
            new AlertDialog.Builder(this)
                    .setTitle("Permission Needed")
                    .setMessage(getResources().getString(R.string.permission_message))
                    .setCancelable(false)
                    .setPositiveButton("OKAY", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            permissionsManager.requestWriteExternalPermission();
                        }
                    })
                    .setNeutralButton("NO THANKS", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Toast.makeText(context, "Permission needed to load the\nsongs on this device", Toast.LENGTH_SHORT).show();
                            finish();
                        }
                    }).show();
        } else {
            querySongs();
        }
    }

    private void querySongs(){
        mainVM.queryLocalSongs(context)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSingleObserver<List<SongModel>>() {
            @Override
            public void onSuccess(List<SongModel> songModels) {
                startActivity(new Intent(MainCarousel.this, MainActivity.class));
                finish();
            }

            @Override
            public void onError(Throwable e) {

            }
        });
    }

}
