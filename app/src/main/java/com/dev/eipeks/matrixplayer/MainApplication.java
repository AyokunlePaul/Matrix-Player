package com.dev.eipeks.matrixplayer;

import android.app.Activity;
import android.app.Application;
import android.app.Service;
import android.content.Context;

import com.dev.eipeks.matrixplayer.core.dagger.component.DaggerMainComponent;
import com.dev.eipeks.matrixplayer.core.dagger.component.MainComponent;
import com.dev.eipeks.matrixplayer.core.dagger.modules.external.ContextModule;

/**
 * Created by eipeks on 3/19/18.
 */

public class MainApplication extends Application {

    private MainComponent component;

    private static Context context;

    public static boolean serviceBoundToActivity = false;
    public static boolean shouldPlaySongFromIntent = false;

    @Override
    public void onCreate() {
        super.onCreate();
        component = DaggerMainComponent.builder()
                .contextModule(new ContextModule(this))
                .build();

        context = this;
    }

    public static MainApplication get(Activity activity){
        return (MainApplication) activity.getApplicationContext();
    }

    public static MainApplication get(Service service){
        return (MainApplication) service.getApplicationContext();
    }

    public static Context getMainApplicationContext(){
        return context;
    }

    public MainComponent getComponent(){
        return this.component;
    }

}
