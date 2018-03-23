package com.dev.eipeks.lecteur.service;

import android.app.Service;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.dev.eipeks.lecteur.MainApplication;
import com.dev.eipeks.lecteur.core_package.dagger.component.MainComponent;

import javax.inject.Inject;

/**
 * Created by eipeks on 3/19/18.
 */

public class MainService extends Service {

    private MainComponent component;

    @Inject
    MediaPlayer player;

    @Override
    public void onCreate() {

        component = MainApplication.get(this).getComponent();
        component.inject(this);

        super.onCreate();

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
