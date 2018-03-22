package com.dev.eipeks.lecteur;

import android.app.Activity;
import android.app.Application;
import android.app.Service;

import com.dev.eipeks.lecteur.core_package.dagger.component.DaggerMainComponent;
import com.dev.eipeks.lecteur.core_package.dagger.component.MainComponent;
import com.dev.eipeks.lecteur.core_package.dagger.modules.external.ContextModule;

/**
 * Created by eipeks on 3/19/18.
 */

public class MainApplication extends Application {

    private MainComponent component;

    @Override
    public void onCreate() {
        super.onCreate();
        component = DaggerMainComponent.builder()
                .contextModule(new ContextModule(this))
                .build();

    }

    public static MainApplication get(Activity activity){
        return (MainApplication) activity.getApplication();
    }

    public static MainApplication get(Service service){
        return (MainApplication) service.getApplication();
    }

    public MainComponent getComponent(){
        return this.component;
    }

}
