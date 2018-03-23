package com.dev.eipeks.lecteur.core_package.dagger.modules.internal.connection;

import android.content.ComponentName;
import android.content.ServiceConnection;
import android.os.IBinder;

import com.dev.eipeks.lecteur.MainApplication;
import com.dev.eipeks.lecteur.core_package.model.MainBinder;
import com.dev.eipeks.lecteur.service.MainService;

import dagger.Module;
import dagger.Provides;

/**
 * Created by eipeks on 3/23/18.
 */

@Module
public class ConnectionModule {

    @Provides
    public ServiceConnection provideServiceConnection(){

        return new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName name, IBinder service) {
                MainApplication.serviceBoundToActivity = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName name) {
                MainApplication.serviceBoundToActivity = false;
            }
        };
    }

    @Provides
    public MainBinder provideMusicBinder(){
        return new MainBinder();
    }

}
