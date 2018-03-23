package com.dev.eipeks.lecteur.screen.activity;

import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.dev.eipeks.lecteur.MainApplication;
import com.dev.eipeks.lecteur.R;
import com.dev.eipeks.lecteur.core_package.dagger.component.MainComponent;
import com.dev.eipeks.lecteur.core_package.managers.PopUpManager;
import com.dev.eipeks.lecteur.core_package.view.CoreActivity;
import com.dev.eipeks.lecteur.databinding.MainLayoutBinding;
import com.dev.eipeks.lecteur.screen.viewmodel.MainVM;
import com.dev.eipeks.lecteur.service.MainService;

import javax.inject.Inject;

/**
 * Created by eipeks on 3/19/18.
 */

public class MainActivity extends CoreActivity {

    private MainLayoutBinding binding;
    private MainComponent component;

    private Intent serviceIntent;

    private PopUpManager popUpManager;

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
        binding = DataBindingUtil.setContentView(this, R.layout.main_layout);


        /*
         *Initialize PopUpManager
         */
        popUpManager = new PopUpManager(this);
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
