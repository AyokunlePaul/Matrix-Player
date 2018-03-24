package com.dev.eipeks.lecteur.screen.activity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.dev.eipeks.lecteur.MainApplication;
import com.dev.eipeks.lecteur.R;
import com.dev.eipeks.lecteur.core_package.dagger.component.MainComponent;
import com.dev.eipeks.lecteur.core_package.managers.PermissionsManager;
import com.dev.eipeks.lecteur.core_package.managers.PopUpManager;
import com.dev.eipeks.lecteur.core_package.view.CoreActivity;
import com.dev.eipeks.lecteur.databinding.SongPlayingLayoutBinding;
import com.dev.eipeks.lecteur.screen.viewmodel.MainVM;
import com.dev.eipeks.lecteur.service.MainService;

import javax.inject.Inject;

/**
 * Created by eipeks on 3/19/18.
 */

public class MainActivity extends CoreActivity {

    private SongPlayingLayoutBinding binding;
    private MainComponent component;

    private Intent serviceIntent;

    private PopUpManager popUpManager;

    private PermissionsManager permissionsManager;

    @Inject
    ServiceConnection serviceConnection;

    @Inject
    MainVM mainVM;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        * Get MainComponent
        * */
        component = MainApplication.get(this).getComponent();
        component.inject(this);

        /*
        * Initialize binding
        * */
        binding = DataBindingUtil.setContentView(this, R.layout.song_playing_layout);


        /*
         *Initialize managers
         */
        popUpManager = new PopUpManager(this);
        permissionsManager = new PermissionsManager(this);

//        checkForPermissions();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (serviceIntent == null){
            serviceIntent = new Intent(this, MainService.class);
            bindService(serviceIntent, serviceConnection, Context.BIND_AUTO_CREATE);
            startService(serviceIntent);
        }
    }

    @Override
    protected void onDestroy() {
        stopService(serviceIntent);
        serviceIntent = null;
        super.onDestroy();
    }
}
