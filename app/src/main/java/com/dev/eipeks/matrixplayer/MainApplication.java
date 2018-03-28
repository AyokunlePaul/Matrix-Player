package com.dev.eipeks.matrixplayer;

import android.app.Activity;
import android.app.Application;
import android.app.Service;

import com.dev.eipeks.matrixplayer.core.dagger.component.DaggerMainComponent;
import com.dev.eipeks.matrixplayer.core.dagger.component.MainComponent;
import com.dev.eipeks.matrixplayer.core.dagger.modules.external.ContextModule;
import com.facebook.drawee.backends.pipeline.Fresco;

/**
 * Created by eipeks on 3/19/18.
 */

public class MainApplication extends Application {

    private MainComponent component;

    public static boolean serviceBoundToActivity = false;

    @Override
    public void onCreate() {
        super.onCreate();
        component = DaggerMainComponent.builder()
                .contextModule(new ContextModule(this))
                .build();

        Fresco.initialize(this);

    }

    public static MainApplication get(Activity activity){
        return (MainApplication) activity.getApplicationContext();
    }

    public static MainApplication get(Service service){
        return (MainApplication) service.getApplicationContext();
    }

    public MainComponent getComponent(){
        return this.component;
    }

}
